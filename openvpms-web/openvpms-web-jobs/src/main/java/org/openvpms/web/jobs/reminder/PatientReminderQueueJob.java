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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.jobs.reminder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.patient.reminder.ReminderConfiguration;
import org.openvpms.archetype.rules.patient.reminder.ReminderProcessor;
import org.openvpms.archetype.rules.patient.reminder.ReminderQueueQueryFactory;
import org.openvpms.archetype.rules.patient.reminder.ReminderStatus;
import org.openvpms.archetype.rules.practice.PracticeService;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.rules.workflow.SystemMessageReason;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.rule.IArchetypeRuleService;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.web.component.retry.AbstractRetryable;
import org.openvpms.web.component.retry.Retryer;
import org.openvpms.web.jobs.JobCompletionNotifier;
import org.openvpms.web.resource.i18n.Messages;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.StatefulJob;
import org.quartz.Trigger;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Queues patient reminders using the {@link ReminderProcessor}.
 *
 * @author Tim Anderson
 */
public class PatientReminderQueueJob implements InterruptableJob, StatefulJob {

    /**
     * The job configuration archetype.
     */
    protected static final String JOB_SHORT_NAME = "entity.jobPatientReminderQueue";

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
     * The patient rules.
     */
    private final PatientRules rules;

    /**
     * The transaction manager.
     */
    private final PlatformTransactionManager transactionManager;

    /**
     * The reminder queue query factory.
     */
    private final ReminderQueueQueryFactory factory;

    /**
     * Used to send messages to users on completion or failure.
     */
    private final JobCompletionNotifier notifier;

    /**
     * Determines if queueing should stop.
     */
    private volatile boolean stop;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(PatientReminderQueueJob.class);

    /**
     * The no. of queued reminders.
     */
    private int queued;

    /**
     * The no. of cancelled reminders.
     */
    private int cancelled;

    /**
     * The no. of reminders skipped due to error.
     */
    private int skipped;

    /**
     * Constructs a {@link PatientReminderQueueJob}.
     *
     * @param configuration      the configuration
     * @param service            the archetype service
     * @param practiceService    the practice service
     * @param transactionManager the transaction manager
     */
    public PatientReminderQueueJob(Entity configuration, IArchetypeRuleService service, PracticeService practiceService,
                                   PatientRules rules, PlatformTransactionManager transactionManager) {
        this.configuration = configuration;
        this.service = service;
        this.practiceService = practiceService;
        this.transactionManager = transactionManager;
        this.rules = rules;
        factory = new ReminderQueueQueryFactory();
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
     * Called by the {@code {@link Scheduler }} when a {@code {@link Trigger }} fires that is associated with the
     * {@code Job}.
     *
     * @param context the job execution context
     */
    @Override
    public void execute(JobExecutionContext context) {
        long begin = System.currentTimeMillis();
        try {
            queue();
            complete(null, begin);
        } catch (Throwable exception) {
            log.error(exception, exception);
            complete(exception, begin);
        }
    }

    /**
     * Queue reminders.
     */
    protected void queue() {
        ReminderConfiguration config = getConfiguration();
        Date maxLeadTime = config.getMaxLeadTime(getStartTime());
        Date date = DateRules.getDate(maxLeadTime, 1, DateUnits.DAYS); // process all reminders due up to max lead + 1
        ArchetypeQuery query = factory.createQuery(date);
        int pageSize = getPageSize();
        query.setMaxResults(pageSize);
        // pull in pageSize results at a time. Note that queueing affects paging, so the query needs to be re-issued
        // from the start if any have updated.
        boolean done = false;
        queued = 0;
        cancelled = 0;
        skipped = 0;
        ReminderProcessor processor = createProcessor(date, config);

        Set<Long> exclude = new HashSet<>(); // ids of reminders to exclude
        while (!stop && !done) {
            IPage<IMObject> page = service.get(query);
            boolean updated = false;  // flag to indicate if any reminders were updated
            for (IMObject reminder : page.getResults()) {
                long id = reminder.getId();
                if (!exclude.contains(id)) {
                    exclude.add(id);
                    QueueStatus status = queue((Act) reminder, processor);
                    if (status == QueueStatus.QUEUED || status == QueueStatus.CANCELLED) {
                        if (status == QueueStatus.QUEUED) {
                            ++queued;
                        } else {
                            ++cancelled;
                        }
                        updated = true;
                    } else {
                        skipped++;
                    }
                }
            }
            if (page.getResults().size() < pageSize) {
                done = true;
            } else if (!updated) {
                // nothing updated, so pull in the next page
                query.setFirstResult(query.getFirstResult() + page.getResults().size());
            }
        }
    }

    /**
     * Creates a new {@link ReminderProcessor}.
     *
     * @param date   process reminders due on or before this date
     * @param config the reminder configuration
     * @return a new {@link ReminderProcessor}
     */
    protected ReminderProcessor createProcessor(Date date, ReminderConfiguration config) {
        return new ReminderProcessor(date, config, true, service, rules);
    }

    /**
     * Queues a reminder.
     * <p/>
     * This will attempt to process the reminder again, if the first attempt fails. This is to handle the case where
     * a user updates or deletes a reminder while processing is underway.
     *
     * @param reminder  the reminder to queue
     * @param processor the reminder processor
     * @return the status of queueing
     */
    protected QueueStatus queue(final Act reminder, final ReminderProcessor processor) {
        RetryableProcessor action = createRetryableProcessor(reminder, processor);
        Retryer.run(action, 2);
        return action.getStatus();
    }

    /**
     * Creates a processor for a reminder that can retry if necessary.
     *
     * @param reminder  the reminder to queue
     * @param processor the reminder processor
     * @return a new processor
     */
    protected RetryableProcessor createRetryableProcessor(Act reminder, ReminderProcessor processor) {
        return new RetryableProcessor(reminder, processor);
    }

    /**
     * Returns the reminder lead times.
     *
     * @return the reminder lead times
     */
    protected ReminderConfiguration getConfiguration() {
        Party practice = practiceService.getPractice();
        if (practice == null) {
            throw new IllegalStateException("The practice has not been configured");
        }
        IMObjectBean bean = new IMObjectBean(practice, service);
        IMObject config = bean.getNodeTargetObject("reminderConfiguration");
        if (config == null) {
            throw new IllegalStateException("Patient reminders have not been configured");
        }
        return new ReminderConfiguration(config, service);
    }

    protected Date getStartTime() {
        return new Date();
    }

    /**
     * Returns the maximum number of reminders to process at once.
     *
     * @return the maximum number of reminders to process at a time
     */
    protected int getPageSize() {
        return 1000;
    }

    /**
     * Invoked when the job completes.
     *
     * @param exception the exception, if the job terminated due to error, or {@code null} otherwise
     * @param begin     the time when the job started
     */
    private void complete(Throwable exception, long begin) {
        if (log.isInfoEnabled()) {
            long elapsed = System.currentTimeMillis() - begin;
            double seconds = elapsed / 1000;
            log.info("Reminders queued=" + queued + ", cancelled=" + cancelled + ", skipped=" + skipped + ", in "
                     + seconds + "s");
        }
        if (exception != null || queued != 0 || cancelled != 0 || skipped != 0) {
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
            subject = Messages.format("patientreminderqueue.subject.exception", configuration.getName());
            text.append(Messages.format("patientreminderqueue.exception", exception.getMessage())).append("\n\n");
        } else {
            if (skipped != 0) {
                reason = SystemMessageReason.ERROR;
                subject = Messages.format("patientreminderqueue.subject.errors", configuration.getName(), skipped);
            } else {
                reason = SystemMessageReason.COMPLETED;
                subject = Messages.format("patientreminderqueue.subject.success", configuration.getName(), queued);
            }
        }
        text.append(Messages.format("patientreminderqueue.queued", queued)).append('\n');
        text.append(Messages.format("patientreminderqueue.cancelled", cancelled)).append('\n');
        text.append(Messages.format("patientreminderqueue.skipped", skipped)).append('\n');
        notifier.send(users, subject, reason, text.toString());
    }

    enum QueueStatus {
        SKIPPED,
        QUEUED,
        CANCELLED
    }

    /**
     * Processes a reminder, optionally re-processing it if the first attempt fails.
     */
    protected class RetryableProcessor extends AbstractRetryable {

        /**
         * The reminder to process.
         */
        private final Act reminder;

        /**
         * The reminder processor.
         */
        private final ReminderProcessor processor;

        /**
         * Determines if reminder items were queued/cancelled/skipped.
         */
        private QueueStatus status;

        /**
         * The status prior to commit.
         */
        private QueueStatus preCommitStatus;


        public RetryableProcessor(Act reminder, ReminderProcessor processor) {
            this.reminder = reminder;
            this.processor = processor;
        }

        /**
         * Determines if reminder items were queued, cancelled, or skipped.
         *
         * @return the status
         */
        public QueueStatus getStatus() {
            return status != null ? status : QueueStatus.SKIPPED;
        }

        /**
         * Runs the action for the first time.
         * <p/>
         * This implementation delegates to {@link #runAction()}.
         *
         * @return {@code true} if the action completed successfully, {@code false} if it failed, and should not be
         * retried
         * @throws RuntimeException if the action fails and may be retried
         */
        @Override
        protected boolean runFirst() {
            return queue(reminder, processor, true);
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
            return queue(reminder, processor, false);
        }

        /**
         * Queues a reminder.
         *
         * @param reminder  the reminder to queue
         * @param processor the reminder processor
         * @param first     if {@code true}, this the first attempt at queueing
         * @return {@code true} if the action completed successfully, {@code false} if it failed, and should not be
         * retried
         * @throws RuntimeException if the action fails and may be retried
         */
        protected boolean queue(final Act reminder, final ReminderProcessor processor, final boolean first) {
            status = null;
            preCommitStatus = null;
            TransactionTemplate template = new TransactionTemplate(transactionManager);
            boolean result = template.execute(new TransactionCallback<Boolean>() {
                public Boolean doInTransaction(TransactionStatus status) {
                    Act act;
                    if (!first) {
                        // queuing is being re-attempted. Reload the reminder to ensure working with the latest
                        // instance.
                        act = (Act) service.get(reminder.getObjectReference());
                        if (act == null) {
                            return false;
                        }
                    } else {
                        act = reminder;
                    }
                    List<Act> acts = processor.process(act);
                    if (!acts.isEmpty()) {
                        service.save(acts);
                        if (ReminderStatus.CANCELLED.equals(reminder.getStatus())) {
                            preCommitStatus = QueueStatus.CANCELLED;
                        } else {
                            preCommitStatus = QueueStatus.QUEUED;
                        }
                    }
                    return true;
                }
            });
            status = preCommitStatus; // action won't be updated if the transaction rolls back
            return result;
        }
    }

}
