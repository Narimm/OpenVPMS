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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.jobs.pharmacy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.openvpms.archetype.rules.practice.PracticeService;
import org.openvpms.archetype.rules.workflow.SystemMessageReason;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.rule.IArchetypeRuleService;
import org.openvpms.component.business.service.scheduler.SingletonJob;
import org.openvpms.component.model.act.FinancialAct;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.entity.Entity;
import org.openvpms.component.model.party.Party;
import org.openvpms.component.model.user.User;
import org.openvpms.component.system.common.cache.SoftRefIMObjectCache;
import org.openvpms.web.jobs.JobCompletionNotifier;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.resource.i18n.format.DateFormatter;
import org.openvpms.web.workspace.customer.charge.OrderPlacer;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.Trigger;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Job to discontinue pharmacy orders.
 * <p/>
 * This looks for POSTED invoices that have ORDERED invoice items that have been completed prior to the
 * current time - the practice pharmacyOrderDiscontinuePeriod.
 * <p/>
 * It discontinues these orders using {@link OrderPlacer#discontinue}, and updates the invoice item statuses to
 * DISCONTINUED.
 * <p/>
 * This status change also applies to invoice items associated with laboratory orders, but no HL7 message is sent.<br/>
 * This is required to ensure that the same POSTED invoices aren't reprocessed indefinitely (which would be the case
 * if the statuses remained as ORDERED).
 *
 * @author Tim Anderson
 */
public class PharmacyOrderDiscontinuationJob implements InterruptableJob, SingletonJob {

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
     * The order services.
     */
    private final OrderDiscontinuer discontinuer;

    /**
     * Used to send messages to users on completion or failure.
     */
    private final JobCompletionNotifier notifier;

    /**
     * Determines if processing should stop.
     */
    private volatile boolean stop;

    /**
     * The job archetype.
     */
    static final String JOB_ARCHETYPE = "entity.jobPharmacyOrderDiscontinuation";

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(PharmacyOrderDiscontinuationJob.class);

    /**
     * Default maximum no. of invoices to process per job invocation.
     */
    private static final int DEFAULT_MAX_RECORDS = 1000;

    /**
     * Default no. of invoices to process at a time.
     */
    private static final int DEFAULT_BATCH_SIZE = 100;

    /**
     * Constructs a {@link PharmacyOrderDiscontinuationJob}.
     *
     * @param configuration   the job configuration
     * @param service         the archetype service
     * @param practiceService the practice service
     * @param discontinuer    the order discontinuer
     */
    public PharmacyOrderDiscontinuationJob(Entity configuration, IArchetypeRuleService service,
                                           PracticeService practiceService, OrderDiscontinuer discontinuer) {
        this.configuration = configuration;
        this.service = service;
        this.practiceService = practiceService;
        this.discontinuer = discontinuer;
        notifier = new JobCompletionNotifier(service);
    }

    /**
     * Called by the {@code {@link Scheduler }} when a user interrupts the {@code Job}.
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
        int processed = 0;
        try {
            Date time = discontinueBefore();
            processed = discontinue(time, practiceService.getServiceUser());
            complete(null, processed, begin);
        } catch (Throwable exception) {
            log.error(exception, exception);
            complete(exception, processed, begin);
        }
    }

    /**
     * Discontinues pharmacy orders associated with invoices completed on or before the specified time.
     *
     * @param time process invoices completed on or before this time
     * @return the no. of locked records
     */
    private int discontinue(Date time, User user) {
        int count = 0;
        if (log.isDebugEnabled()) {
            log.debug("PharmacyOrderDiscontinuationJob discontinuing orders associated with invoices on or before: "
                      + DateFormatter.formatDateTime(time));
        }
        IMObjectBean bean = service.getBean(configuration);
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
            int updated = discontinue(time, batchSize, user);
            count += updated;
            if (updated < batchSize || count >= maxRecords) {
                done = true;
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("PharmacyOrderDiscontinuationJob processed=" + count);
        }
        return count;
    }

    /**
     * Discontinues invoices.
     *
     * @param time      process invoices completed on or before this time
     * @param batchSize the maximum no. of invoices to process
     * @param user      the user to
     * @return the number of discontinued invoices
     */
    private int discontinue(Date time, int batchSize, User user) {
        SoftRefIMObjectCache cache = new SoftRefIMObjectCache(service);
        List<FinancialAct> invoices = discontinuer.getInvoices(time, batchSize);
        Party practice = practiceService.getPractice();
        for (FinancialAct invoice : invoices) {
            discontinuer.discontinue(invoice, user, practice, cache);
        }
        return invoices.size();
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
            subject = Messages.format("pharmacyorderjob.exception.subject", configuration.getName());
            text = Messages.format("pharmacyorderjob.exception.message", exception.getMessage());
        } else {
            reason = SystemMessageReason.COMPLETED;
            double seconds = elapsed / 1000d;
            double rate = (seconds != 0) ? locked / seconds : locked;
            subject = Messages.format("pharmacyorderjob.subject", configuration.getName(), locked);
            text = Messages.format("pharmacyorderjob.message", locked, seconds, rate);
        }
        notifier.send(users, subject, reason, text);
    }

    /**
     * Determines the time prior to which POSTED invoices should be processed.
     *
     * @return the time
     */
    private Date discontinueBefore() {
        Period period = practiceService.getPharmacyOrderDiscontinuePeriod();
        if (period == null) {
            period = Period.ZERO;
        }
        return new DateTime().minus(period).toDate();
    }

}
