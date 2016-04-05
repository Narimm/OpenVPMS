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
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.patient.MedicalRecordRules;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.rules.practice.PracticeService;
import org.openvpms.archetype.rules.workflow.SystemMessageReason;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.rule.IArchetypeRuleService;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.retry.AbstractRetryable;
import org.openvpms.web.component.retry.Retryer;
import org.openvpms.web.jobs.JobCompletionNotifier;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.resource.i18n.format.DateFormatter;
import org.openvpms.web.system.ServiceHelper;
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
public class MedicalRecordLockingJob implements InterruptableJob, StatefulJob {

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
     * The practice rules.
     */
    private final PracticeRules rules;

    /**
     * The records to lock.
     */
    private final String[] shortNames;

    /**
     * The maximum no. of records to process. If <=0, the job will continue until all records are processed.
     */
    private final int maxRecords;

    /**
     * Used to send messages to users on completion or failure.
     */
    private final JobCompletionNotifier notifier;

    /**
     * The no. of locked acts.
     */
    private int count = 0;

    /**
     * Determines if locking should stop.
     */
    private volatile boolean stop;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(MedicalRecordLockingJob.class);

    /**
     * Constructs a {@link MedicalRecordLockingJob}.
     *
     * @param configuration   the job configuration
     * @param service         the archetype service
     * @param practiceService the practice service
     * @param rules           the practice rules
     * @param recordRules     the medical record rules
     */
    public MedicalRecordLockingJob(Entity configuration, IArchetypeRuleService service, PracticeService practiceService,
                                   PracticeRules rules, MedicalRecordRules recordRules) {
        this.configuration = configuration;
        this.service = service;
        this.practiceService = practiceService;
        this.rules = rules;
        shortNames = recordRules.getLockableRecords();
        IMObjectBean bean = new IMObjectBean(configuration, service);
        maxRecords = bean.getInt("maxRecords", -1);
        notifier = new JobCompletionNotifier(service);
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
        try {
            lock();
            complete(null);
        } catch (Throwable exception) {
            log.error(exception, exception);
            complete(exception);
        }
    }

    /**
     * Locks records.
     * <p/>
     * This needs to read in records, update them, and then save them, as there is no support to run an HQL update.
     */
    protected void lock() {
        Date startTime = getStartTime();
        if (log.isDebugEnabled()) {
            log.debug("MedicalRecordLockingJob locking: " + StringUtils.join(shortNames, ", "));
            log.debug("Locking records starting on or before: " + DateFormatter.formatDateTime(startTime));
        }
        boolean found = true;
        while (!stop() && found) {
            found = false;
            ArchetypeQuery query = new ArchetypeQuery(shortNames, false, false);
            query.add(Constraints.lte("startTime", startTime));
            query.add(Constraints.or(Constraints.ne("status", ActStatus.POSTED), Constraints.isNull("status")));
            query.add(Constraints.sort("id"));
            IMObjectQueryIterator<Act> iterator = new IMObjectQueryIterator<>(service, query);
            while (iterator.hasNext() && !stop()) {
                Act act = iterator.next();
                if (!ActStatus.POSTED.equals(act.getStatus())) {
                    found = true;
                    if (lock(act)) {
                        ++count;
                    }
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("MedicalRecordLockingJob locked=" + count);
        }
    }

    /**
     * Determines if locking should stop.
     *
     * @return {@code true} if locking should stop
     */
    private boolean stop() {
        return stop || (maxRecords >= 0 && count >= maxRecords);
    }

    /**
     * Invoked on completion of a job. Sends a message notifying the registered users of completion or failure of the
     * job if required.
     *
     * @param exception the exception, if the job failed, otherwise {@code null}
     */
    private void complete(Throwable exception) {
        if (exception != null || count != 0) {
            Set<User> users = notifier.getUsers(configuration);
            if (!users.isEmpty()) {
                notifyUsers(users, exception);
            }
        }
    }

    /**
     * Notifies users of completion or failure of the job.
     *
     * @param users     the users to notify
     * @param exception the exception, if the job failed, otherwise {@code null}
     */
    private void notifyUsers(Set<User> users, Throwable exception) {
        String subject;
        String reason;
        StringBuilder text = new StringBuilder();
        if (exception != null) {
            reason = SystemMessageReason.ERROR;
            subject = Messages.format("recordlocking.subject.exception", configuration.getName());
            text.append(Messages.format("recordlocking.exception", exception.getMessage()));
        } else {
            reason = SystemMessageReason.COMPLETED;
            subject = Messages.format("recordlocking.subject.success", configuration.getName(), count);
        }
        notifier.send(users, subject, reason, text.toString());
    }

    /**
     * Locks an act.
     *
     * @param act the act to lock
     * @return {@code true} if the act was successfully locked
     */
    private boolean lock(Act act) {
        return Retryer.run(new RetryableLocker(act));
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

    /**
     * Repeatedly attempts to lock an act, to handle contention.
     */
    private class RetryableLocker extends AbstractRetryable {

        /**
         * The act to lock.
         */
        private Act act;

        /**
         * Constructs a {@link RetryableLocker}.
         *
         * @param act the act to lock
         */
        public RetryableLocker(Act act) {
            this.act = act;
        }

        /**
         * Runs the action again.
         * <p/>
         * This implementation delegates to {@link #runAction()}.
         *
         * @return {@code true} if the action completed successfully, {@code false} if it failed, and should not be
         * retried
         */
        @Override
        protected boolean runAgain() {
            act = IMObjectHelper.reload(act);
            return (act != null) && super.runAgain();
        }

        /**
         * Runs the action.
         *
         * @return {@code true} if the action completed successfully, {@code false} if it failed, and should not be
         * retried
         * @throws RuntimeException if the action fails and may be retried
         */
        @Override
        protected boolean runAction() {
            if (ActStatus.POSTED.equals(act.getStatus())) {
                act.setStatus(ActStatus.POSTED);
                ServiceHelper.getArchetypeService(false).save(act);
            }
            return true;
        }
    }

}
