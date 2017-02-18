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
import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.archetype.rules.doc.TemplateHelper;
import org.openvpms.archetype.rules.patient.reminder.GroupingReminderIterator;
import org.openvpms.archetype.rules.patient.reminder.ReminderArchetypes;
import org.openvpms.archetype.rules.patient.reminder.ReminderConfiguration;
import org.openvpms.archetype.rules.patient.reminder.ReminderItemQueryFactory;
import org.openvpms.archetype.rules.patient.reminder.ReminderItemStatus;
import org.openvpms.archetype.rules.patient.reminder.ReminderRules;
import org.openvpms.archetype.rules.patient.reminder.ReminderTypes;
import org.openvpms.archetype.rules.practice.PracticeService;
import org.openvpms.archetype.rules.workflow.SystemMessageReason;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.sms.ConnectionFactory;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.sms.SMSHelper;
import org.openvpms.web.component.mail.MailerFactory;
import org.openvpms.web.jobs.JobCompletionNotifier;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.workspace.customer.communication.CommunicationHelper;
import org.openvpms.web.workspace.customer.communication.CommunicationLogger;
import org.openvpms.web.workspace.reporting.ReportingException;
import org.openvpms.web.workspace.reporting.reminder.PatientReminderProcessor;
import org.openvpms.web.workspace.reporting.reminder.ReminderEmailProcessor;
import org.openvpms.web.workspace.reporting.reminder.ReminderSMSEvaluator;
import org.openvpms.web.workspace.reporting.reminder.ReminderSMSProcessor;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.StatefulJob;
import org.quartz.Trigger;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * A job to send patient email and SMS reminders.
 *
 * @author Tim Anderson
 */
public class PatientReminderSenderJob implements InterruptableJob, StatefulJob {

    /**
     * The job configuration archetype.
     */
    protected static final String JOB_SHORT_NAME = "entity.jobPatientReminderSender";

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
    private final ReminderRules rules;

    /**
     * The mailer factory.
     */
    private final MailerFactory mailerFactory;

    /**
     * The SMS connection factory.
     */
    private final ConnectionFactory connectionFactory;

    /**
     * The SMS template evaluator.
     */
    private final ReminderSMSEvaluator evaluator;

    /**
     * The communication loggger.
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
     * @param configuration       the configuration
     * @param service             the archetype service
     * @param practiceService     the practice service
     * @param rules               the reminder rules
     * @param mailerFactory       the mailer factory
     * @param connectionFactory   the connection factory
     * @param evaluator           the SMS template evaluator
     * @param communicationLogger the communication logger
     */
    public PatientReminderSenderJob(Entity configuration, IArchetypeService service, PracticeService practiceService,
                                    ReminderRules rules, MailerFactory mailerFactory,
                                    ConnectionFactory connectionFactory, ReminderSMSEvaluator evaluator,
                                    CommunicationLogger communicationLogger) {
        this.configuration = configuration;
        this.service = service;
        this.practiceService = practiceService;
        this.rules = rules;
        this.mailerFactory = mailerFactory;
        this.connectionFactory = connectionFactory;
        this.evaluator = evaluator;
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
            TemplateHelper helper = new TemplateHelper(service);
            DocumentTemplate groupTemplate = helper.getDocumentTemplate("GROUPED_REMINDERS");
            if (groupTemplate == null) {
                throw new ReportingException(ReportingException.ErrorCode.NoGroupedReminderTemplate);
            }
            boolean sms = groupTemplate.getSMSTemplate() != null && SMSHelper.isSMSEnabled(practice);

            CommunicationLogger logger = (CommunicationHelper.isLoggingEnabled(practiceService.getPractice()))
                                         ? communicationLogger : null;
            ReminderTypes reminderTypes = new ReminderTypes(service);
            ReminderConfiguration config = getReminderConfig(practice);
            total = sendEmailReminders(groupTemplate, reminderTypes, practice, config, logger);
            if (sms && !stop) {
                Stats stats = sendSMSReminders(groupTemplate, reminderTypes, practice, config, logger);
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
     * @param groupTemplate the grouped reminder template. May be {@code null}
     * @param reminderTypes the reminder types
     * @param practice      the practice
     * @param config        the reminder configuration
     * @param logger        the communication logger, or {@code null} if communication is not being logged
     * @return the statistics
     */
    protected Stats sendEmailReminders(DocumentTemplate groupTemplate, ReminderTypes reminderTypes, Party practice,
                                       ReminderConfiguration config, CommunicationLogger logger) {
        ReminderEmailProcessor processor = new ReminderEmailProcessor(mailerFactory, groupTemplate, reminderTypes,
                                                                      rules, practice, service, config, logger,
                                                                      new LocalContext());
        GroupingReminderIterator iterator = createIterator(ReminderArchetypes.EMAIL_REMINDER, reminderTypes);
        return send(processor, iterator);
    }

    /**
     * Sends email reminders.
     *
     * @param groupTemplate the grouped reminder template. May be {@code null}
     * @param reminderTypes the reminder types
     * @param practice      the practice
     * @param config        the reminder configuration
     * @param logger        the communication logger, or {@code null} if communication is not being logged
     * @return the statistics
     */
    protected Stats sendSMSReminders(DocumentTemplate groupTemplate, ReminderTypes reminderTypes,
                                     Party practice, ReminderConfiguration config, CommunicationLogger logger) {
        ReminderSMSProcessor sender = new ReminderSMSProcessor(connectionFactory, evaluator, groupTemplate,
                                                               reminderTypes, rules, practice, service, config,
                                                               logger);
        GroupingReminderIterator iterator = createIterator(ReminderArchetypes.SMS_REMINDER, reminderTypes);
        return send(sender, iterator);
    }

    /**
     * Returns the reminder lead times.
     *
     * @return the reminder lead times
     */
    protected ReminderConfiguration getReminderConfig(Party practice) {
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
     * Sends reminders.
     *
     * @param processor the processor to use
     * @param iterator  the reminder iterator
     * @return the statistics
     */
    private Stats send(PatientReminderProcessor processor, GroupingReminderIterator iterator) {
        Date from = getStartTime();
        Stats total = new Stats();
        while (!stop && iterator.hasNext()) {
            List<ObjectSet> sets = iterator.next();
            PatientReminderProcessor.State state = processor.prepare(sets, from);
            processor.process(state);
            int cancelled = state.getCancelled().size();
            int errors = state.getErrors().size();
            processor.complete(state);
            total = total.add(new Stats(state.getProcessed(), cancelled, errors));
            iterator.updated();
        }
        return total;
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
            text.append(Messages.format("patientremindersender.exception", exception.getMessage()));
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
        text.append(Messages.format("patientremindersender.sent", stats.getSent()));
        text.append(Messages.format("patientremindersender.cancelled", stats.getCancelled()));
        text.append(Messages.format("patientremindersender.errors", stats.getErrors()));
        notifier.send(users, subject, reason, text.toString());
    }

    /**
     * Creates an iterator of {@code PENDING} reminder items.
     *
     * @param shortName     the reminder item short name
     * @param reminderTypes the reminder types
     * @return a new iterator
     */
    private GroupingReminderIterator createIterator(String shortName, ReminderTypes reminderTypes) {
        ReminderItemQueryFactory factory = new ReminderItemQueryFactory(shortName, ReminderItemStatus.PENDING);
        return new GroupingReminderIterator(factory, reminderTypes, getPageSize(), service);
    }

}
