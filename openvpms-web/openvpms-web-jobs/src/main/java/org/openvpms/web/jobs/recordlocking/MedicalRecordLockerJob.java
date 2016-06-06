/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.jobs.recordlocking;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.openvpms.archetype.rules.patient.MedicalRecordRules;
import org.openvpms.archetype.rules.practice.PracticeService;
import org.openvpms.archetype.rules.workflow.SystemMessageReason;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.rule.IArchetypeRuleService;
import org.openvpms.web.jobs.JobCompletionNotifier;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.resource.i18n.format.DateFormatter;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.StatefulJob;
import org.quartz.Trigger;

import java.util.Date;
import java.util.Set;

/**
 * .
 *
 * @author Tim Anderson
 */
public class MedicalRecordLockerJob implements InterruptableJob, StatefulJob {

    /**
     * The job configuration.
     */
    private final Entity configuration;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The practice service.
     */
    private final PracticeService practiceService;

    /**
     * The records to lock.
     */
    private final String[] shortNames;

    /**
     * Used to send messages to users on completion or failure.
     */
    private final JobCompletionNotifier notifier;

    /**
     * The record locker.
     */
    private final MedicalRecordLocker locker;

    /**
     * Determines if locking should stop.
     */
    private volatile boolean stop;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(MedicalRecordLockerJob.class);

    /**
     * Default maximum no. of records to lock per job invocation.
     */
    private static final int DEFAULT_MAX_RECORDS = 50000;

    /**
     * Default no. of records to lock at a time.
     */
    private static final int DEFAULT_BATCH_SIZE = 500;

    /**
     * Constructs a {@link MedicalRecordLockerJob}.
     *
     * @param configuration   the job configuration
     * @param service         the archetype service
     * @param practiceService the practice service
     * @param factory         the Hibernate session factory
     * @param recordRules     the medical record rules
     */
    public MedicalRecordLockerJob(Entity configuration, IArchetypeRuleService service, PracticeService practiceService,
                                  SessionFactory factory, MedicalRecordRules recordRules) {
        this.configuration = configuration;
        this.service = service;
        this.practiceService = practiceService;
        shortNames = recordRules.getLockableRecords();
        notifier = new JobCompletionNotifier(service);
        locker = new MedicalRecordLocker(factory);
    }

    /**
     * Called by the {@code {@link Scheduler}} when a user interrupts the {@code Job}.
     */
    @Override
    public void interrupt() {
        stop = true;
    }

    /**
     * Called by the {@code {@link Scheduler}} when a {@code {@link Trigger }} fires that is associated with the
     * {@code Job}.
     *
     * @param context the job execution context
     * @throws JobExecutionException if there is an exception while executing the job.
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        long begin = System.currentTimeMillis();
        int locked = 0;
        try {
            Date startTime = getStartTime();
            locked = lock(startTime);
            complete(null, locked, begin);
        } catch (Throwable exception) {
            log.error(exception, exception);
            complete(exception, locked, begin);
        }
    }

    /**
     * Locks records.
     * <p/>
     * This needs to read in records, update them, and then save them, as there is no support to run an HQL update.
     *
     * @return the no. of locked records
     */
    protected int lock(Date startTime) {
        int count = 0;
        if (log.isDebugEnabled()) {
            log.debug("MedicalRecordLockerJob locking: " + StringUtils.join(shortNames, ", "));
            log.debug("Locking records starting on or before: " + DateFormatter.formatDateTime(startTime));
        }
        IMObjectBean bean = new IMObjectBean(configuration, service);
        int batchSize = bean.getInt("batchSize", DEFAULT_BATCH_SIZE);
        if (batchSize <= 0) {
            batchSize = DEFAULT_BATCH_SIZE;
        }
        int maxRecords = bean.getInt("maxRecords", DEFAULT_MAX_RECORDS);
        if (maxRecords <= 0) {
            maxRecords = DEFAULT_MAX_RECORDS;
        }

        boolean done = false;
        while (!stop && !done) {
            int updated = locker.lock(shortNames, startTime, batchSize);
            count += updated;
            if (updated < batchSize || count >= maxRecords) {
                done = true;
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("MedicalRecordLockerJob locked=" + count);
        }
        return count;
    }


    /**
     * Invoked on completion of a job. Sends a message notifying the registered users of completion or failure of the
     * job if required.
     *
     * @param exception the exception, if the job failed, otherwise {@code null}
     * @param locked    the no. of locked records
     * @param begin     the locking start time
     */
    private void complete(Throwable exception, int locked, long begin) {
        long endTime = System.currentTimeMillis();
        long elapsed = endTime - begin;
        if (exception != null || locked != 0) {
            Set<User> users = notifier.getUsers(configuration);
            if (!users.isEmpty()) {
                notifyUsers(users, locked, elapsed, exception);
            }
        }
    }

    /**
     * Notifies users of completion or failure of the job.
     *
     * @param users     the users to notify
     * @param locked    the no. of locked records
     * @param elapsed   the elapsed time, in milliseconds
     * @param exception the exception, if the job failed, otherwise {@code null}
     */
    private void notifyUsers(Set<User> users, int locked, long elapsed, Throwable exception) {
        String subject;
        String reason;
        String text;
        if (exception != null) {
            reason = SystemMessageReason.ERROR;
            subject = Messages.format("recordlocking.exception.subject", configuration.getName());
            text = Messages.format("recordlocking.exception.message", exception.getMessage());
        } else {
            reason = SystemMessageReason.COMPLETED;
            double seconds = elapsed / 1000;
            double rate = (seconds != 0) ? locked / seconds : locked;
            subject = Messages.format("recordlocking.subject", configuration.getName(), locked);
            text = Messages.format("recordlocking.message", locked, seconds, rate);
        }
        notifier.send(users, subject, reason, text);
    }

    /**
     * Determines the record start time prior to which locking should occur.
     *
     * @return the start time
     */
    private Date getStartTime() {
        Date result;
        Period period = practiceService.getRecordLockPeriod();
        if (period == null) {
            throw new IllegalStateException("Medical record locking has been disabled");
        } else {
            result = new DateTime().minus(period).toDate();
        }
        return result;
    }


}
