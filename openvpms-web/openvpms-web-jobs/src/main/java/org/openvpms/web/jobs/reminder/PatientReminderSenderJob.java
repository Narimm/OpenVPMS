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
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.patient.reminder.GroupingReminderIterator;
import org.openvpms.archetype.rules.patient.reminder.ReminderArchetypes;
import org.openvpms.archetype.rules.patient.reminder.ReminderConfiguration;
import org.openvpms.archetype.rules.patient.reminder.ReminderGroupingPolicy;
import org.openvpms.archetype.rules.patient.reminder.ReminderItemQueryFactory;
import org.openvpms.archetype.rules.patient.reminder.ReminderItemStatus;
import org.openvpms.archetype.rules.patient.reminder.ReminderRules;
import org.openvpms.archetype.rules.patient.reminder.ReminderTypes;
import org.openvpms.archetype.rules.patient.reminder.Reminders;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.rules.practice.PracticeService;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.workflow.SystemMessageReason;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.rule.IArchetypeRuleService;
import org.openvpms.sms.ConnectionFactory;
import org.openvpms.web.component.im.report.ReporterFactory;
import org.openvpms.web.component.mail.DefaultMailerFactory;
import org.openvpms.web.component.mail.EmailTemplateEvaluator;
import org.openvpms.web.component.mail.MailerFactory;
import org.openvpms.web.component.service.PracticeMailService;
import org.openvpms.web.jobs.JobCompletionNotifier;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.workspace.customer.communication.CommunicationHelper;
import org.openvpms.web.workspace.customer.communication.CommunicationLogger;
import org.openvpms.web.workspace.reporting.reminder.PatientReminderProcessor;
import org.openvpms.web.workspace.reporting.reminder.PatientReminders;
import org.openvpms.web.workspace.reporting.reminder.ReminderEmailProcessor;
import org.openvpms.web.workspace.reporting.reminder.ReminderSMSEvaluator;
import org.openvpms.web.workspace.reporting.reminder.ReminderSMSProcessor;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.StatefulJob;
import org.quartz.Trigger;

import java.util.Date;
import java.util.Set;

/**
 * A job to send patient email and SMS reminders.
 *
 * @author Tim Anderson
 */
public class PatientReminderSenderJob implements InterruptableJob, StatefulJob {

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
     * The reminder rules.
     */
    private final ReminderRules reminderRules;

    /**
     * The patient rules.
     */
    private final PatientRules patientRules;

    /**
     * The practice rules.
     */
    private final PracticeRules practiceRules;

    /**
     * The mailer factory.
     */
    private final MailerFactory mailerFactory;

    /**
     * The email template evaluator.
     */
    private final EmailTemplateEvaluator emailTemplateEvaluator;

    /**
     * The reporter factory.
     */
    private final ReporterFactory reporterFactory;

    /**
     * The SMS connection factory.
     */
    private final ConnectionFactory connectionFactory;

    /**
     * The SMS template evaluator.
     */
    private final ReminderSMSEvaluator smsEvaluator;

    /**
     * The communication logger.
     */
    private final CommunicationLogger communicationLogger;

    /**
     * Used to send messages to users on completion or failure.
     */
    private final JobCompletionNotifier notifier;

    /**
     * Determines if sending should stop.
     */
    private volatile boolean stop;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(PatientReminderSenderJob.class);

    /**
     * Constructs a {@link PatientReminderSenderJob}.
     *
     * @param configuration          the configuration
     * @param service                the archetype service
     * @param practiceService        the practice service
     * @param reminderRules          the reminder rules
     * @param patientRules           the patient rules
     * @param mailService            the mail service
     * @param handlers               the document handlers
     * @param emailTemplateEvaluator the email template evaluator
     * @param reporterFactory        the reporter factory
     * @param connectionFactory      the connection factory
     * @param smsEvaluator           the SMS template evaluator
     * @param communicationLogger    the communication logger
     */
    public PatientReminderSenderJob(Entity configuration, IArchetypeRuleService service,
                                    PracticeService practiceService, ReminderRules reminderRules,
                                    PatientRules patientRules, PracticeRules practiceRules,
                                    PracticeMailService mailService, DocumentHandlers handlers,
                                    EmailTemplateEvaluator emailTemplateEvaluator, ReporterFactory reporterFactory,
                                    ConnectionFactory connectionFactory, ReminderSMSEvaluator smsEvaluator,
                                    CommunicationLogger communicationLogger) {
        this.configuration = configuration;
        this.service = service;
        this.practiceService = practiceService;
        this.reminderRules = reminderRules;
        this.patientRules = patientRules;
        this.practiceRules = practiceRules;
        this.mailerFactory = new DefaultMailerFactory(mailService, handlers);
        this.emailTemplateEvaluator = emailTemplateEvaluator;
        this.reporterFactory = reporterFactory;
        this.connectionFactory = connectionFactory;
        this.smsEvaluator = smsEvaluator;
        this.communicationLogger = communicationLogger;
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
     * Called by the {@code {@link Scheduler }} when a {@code {@link Trigger }} fires that is associated with the
     * {@code Job}.
     *
     * @param context the job execution context
     */
    @Override
    public void execute(JobExecutionContext context) {
        long begin = System.currentTimeMillis();
        Stats total = Stats.ZERO;
        try {
            Party practice = practiceService.getPractice();
            if (practice == null) {
                throw new IllegalStateException("Practice is not configured");
            }
            CommunicationLogger logger = (CommunicationHelper.isLoggingEnabled(practice, service))
                                         ? communicationLogger : null;
            ReminderTypes reminderTypes = new ReminderTypes(service);
            ReminderConfiguration config = getReminderConfig(practice);
            if (config.getLocation() == null) {
                throw new IllegalStateException("Reminder Configuration does not specify a Location");
            }
            Date date = getSendDate();
            total = sendEmailReminders(date, reminderTypes, practice, config, logger);
            if (!stop) {
                Stats stats = sendSMSReminders(date, reminderTypes, practice, config, logger);
                total = total.add(stats);
            }
            complete(null, begin, total);
        } catch (Throwable exception) {
            log.error(exception, exception);
            complete(exception, begin, total);
        }
    }

    /**
     * Sends email reminders.
     *
     * @param date          all email reminders with a startTime prior to this will be sent
     * @param reminderTypes the reminder types
     * @param practice      the practice
     * @param config        the reminder configuration
     * @param logger        the communication logger, or {@code null} if communication is not being logged
     * @return the statistics
     */
    protected Stats sendEmailReminders(Date date, ReminderTypes reminderTypes, Party practice,
                                       ReminderConfiguration config, CommunicationLogger logger) {
        ReminderEmailProcessor processor = new ReminderEmailProcessor(mailerFactory, emailTemplateEvaluator,
                                                                      reporterFactory, reminderTypes, practice,
                                                                      reminderRules, patientRules, practiceRules,
                                                                      service, config, logger);
        GroupingReminderIterator iterator = createIterator(ReminderArchetypes.EMAIL_REMINDER, reminderTypes, date,
                                                           config);
        return send(date, processor, iterator);
    }

    /**
     * Sends SMS reminders.
     *
     * @param date          all SMS reminders with a startTime prior to this will be sent
     * @param reminderTypes the reminder types
     * @param practice      the practice
     * @param config        the reminder configuration
     * @param logger        the communication logger, or {@code null} if communication is not being logged
     * @return the statistics
     */
    protected Stats sendSMSReminders(Date date, ReminderTypes reminderTypes, Party practice,
                                     ReminderConfiguration config, CommunicationLogger logger) {
        ReminderSMSProcessor sender = new ReminderSMSProcessor(connectionFactory, smsEvaluator, reminderTypes,
                                                               practice, reminderRules, patientRules, practiceRules,
                                                               service, config, logger);
        GroupingReminderIterator iterator = createIterator(ReminderArchetypes.SMS_REMINDER, reminderTypes, date,
                                                           config);
        return send(date, sender, iterator);
    }

    /**
     * Returns the reminder configuration.
     *
     * @return the reminder configuration
     */
    protected ReminderConfiguration getReminderConfig(Party practice) {
        ReminderConfiguration config = reminderRules.getConfiguration(practice);
        if (config == null) {
            throw new IllegalStateException("Patient reminders have not been configured");
        }
        return config;
    }

    protected Date getSendDate() {
        return DateRules.getTomorrow();
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
     * Sends reminders.
     *
     * @param date      the date to use when determining if a reminder item should be cancelled
     * @param processor the processor to use
     * @param iterator  the reminder iterator
     * @return the statistics
     */
    private Stats send(Date date, PatientReminderProcessor processor, GroupingReminderIterator iterator) {
        Stats total = new Stats();
        while (!stop && iterator.hasNext()) {
            Reminders reminders = iterator.next();
            try {
                PatientReminders state = processor.prepare(reminders.getReminders(), reminders.getGroupBy(), date,
                                                           false);
                Stats stats = send(state, processor, iterator);
                total = total.add(stats);
            } catch (Throwable exception) {
                log.error("Failed to send reminders", exception);
                total = total.add(new Stats(0, 0, reminders.getReminders().size()));
            }
        }
        return total;
    }

    /**
     * Sends reminders.
     *
     * @param reminders the reminders to send
     * @param processor the processor to use
     * @param iterator  the reminder iterator
     * @return the send statistics
     */
    private Stats send(PatientReminders reminders, PatientReminderProcessor processor,
                       GroupingReminderIterator iterator) {
        int processed;
        int errors;
        boolean updated;
        try {
            if (!reminders.getReminders().isEmpty()) {
                processor.process(reminders);
            }
            updated = processor.complete(reminders);
            processed = reminders.getProcessed();
            errors = reminders.getErrors().size();
        } catch (Throwable exception) {
            // give each of the reminders that failed to be sent ERROR status
            processed = 0;
            errors = reminders.getErrors().size() + reminders.getReminders().size();
            updated = processor.failed(reminders, exception);
        }
        if (updated) {
            iterator.updated();
        }
        int cancelled = reminders.getCancelled().size();
        return new Stats(processed, cancelled, errors);
    }

    /**
     * Invoked when the job completes.
     *
     * @param exception the exception, if the job terminated on error, otherwise {@code null}
     * @param begin     the start time
     * @param stats     the statistics
     */
    private void complete(Throwable exception, long begin, Stats stats) {
        if (log.isInfoEnabled()) {
            long elapsed = System.currentTimeMillis() - begin;
            double seconds = elapsed / 1000;
            log.info("Reminders sent=" + stats.getSent() + ", cancelled=" + stats.getCancelled() + ", errors="
                     + stats.getErrors() + ", in " + seconds + "s");
        }
        if (exception != null || stats.getSent() != 0 || stats.getCancelled() != 0 || stats.getErrors() != 0) {
            Set<User> users = notifier.getUsers(configuration);
            if (!users.isEmpty()) {
                notifyUsers(users, exception, stats);
            }
        }
    }

    /**
     * Notifies users of completion or failure of the job.
     *
     * @param users     the users to notify
     * @param exception the exception, if the job failed, otherwise {@code null}
     * @param stats     the statistics
     */
    private void notifyUsers(Set<User> users, Throwable exception, Stats stats) {
        String subject;
        String reason;
        StringBuilder text = new StringBuilder();
        if (exception != null) {
            reason = SystemMessageReason.ERROR;
            subject = Messages.format("patientremindersender.subject.exception", configuration.getName());
            text.append(Messages.format("patientremindersender.exception", exception.getMessage())).append("\n\n");
        } else {
            if (stats.getErrors() != 0) {
                reason = SystemMessageReason.ERROR;
                subject = Messages.format("patientremindersender.subject.errors", configuration.getName(),
                                          stats.getErrors());
            } else {
                reason = SystemMessageReason.COMPLETED;
                subject = Messages.format("patientremindersender.subject.success", configuration.getName(),
                                          stats.getSent());
            }
        }
        text.append(Messages.format("patientremindersender.sent", stats.getSent())).append('\n');
        text.append(Messages.format("patientremindersender.cancelled", stats.getCancelled())).append('\n');
        text.append(Messages.format("patientremindersender.errors", stats.getErrors())).append('\n');
        notifier.send(users, subject, reason, text.toString());
    }

    /**
     * Creates an iterator of {@code PENDING} reminder items.
     *
     * @param shortName     the reminder item short name
     * @param reminderTypes the reminder types
     * @param date          the upper bound of the send date
     * @param config        the reminder configuration
     * @return a new iterator
     */
    private GroupingReminderIterator createIterator(String shortName, ReminderTypes reminderTypes, Date date,
                                                    ReminderConfiguration config) {
        ReminderItemQueryFactory factory = new ReminderItemQueryFactory(shortName, ReminderItemStatus.PENDING);
        factory.setTo(date);
        ReminderGroupingPolicy groupByCustomer = config.getGroupByCustomerPolicy();
        ReminderGroupingPolicy groupByPatient = config.getGroupByPatientPolicy();
        return new GroupingReminderIterator(factory, reminderTypes, getPageSize(), groupByCustomer, groupByPatient,
                                            service);
    }


}
