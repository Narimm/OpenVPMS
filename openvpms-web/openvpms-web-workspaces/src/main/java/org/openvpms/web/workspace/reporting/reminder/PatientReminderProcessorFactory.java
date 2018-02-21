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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.reporting.reminder;

import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.patient.reminder.ReminderArchetypes;
import org.openvpms.archetype.rules.patient.reminder.ReminderConfiguration;
import org.openvpms.archetype.rules.patient.reminder.ReminderRules;
import org.openvpms.archetype.rules.patient.reminder.ReminderTypes;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.sms.ConnectionFactory;
import org.openvpms.web.component.im.print.IMPrinterFactory;
import org.openvpms.web.component.im.report.ReporterFactory;
import org.openvpms.web.component.mail.DefaultMailerFactory;
import org.openvpms.web.component.mail.EmailTemplateEvaluator;
import org.openvpms.web.component.mail.MailerFactory;
import org.openvpms.web.component.service.CurrentLocationMailService;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.customer.communication.CommunicationHelper;
import org.openvpms.web.workspace.customer.communication.CommunicationLogger;
import org.openvpms.web.workspace.customer.communication.LoggingMailerFactory;

/**
 * Factory for {@link PatientReminderProcessor} instances.
 *
 * @author Tim Anderson
 */
public class PatientReminderProcessorFactory {

    /**
     * The reminder types.
     */
    private final ReminderTypes reminderTypes;

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
     * The location.
     */
    private final Party location;

    /**
     * The practice.
     */
    private final Party practice;

    /**
     * The help context.
     */
    private final HelpContext help;

    /**
     * The mailer factory.
     */
    private MailerFactory factory;

    /**
     * The SMS connection factory.
     */
    private ConnectionFactory smsFactory;

    /**
     * The printer factory.
     */
    private IMPrinterFactory printerFactory;

    /**
     * The reporter factory.
     */
    private ReporterFactory reporterFactory;

    /**
     * The communication logger, if communications logging is enabled.
     */
    private CommunicationLogger logger;

    /**
     * The reminder configuration.
     */
    private final ReminderConfiguration config;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Constructs a {@link PatientReminderProcessorFactory}.
     *
     * @param location the practice location
     * @param practice the practice
     * @param help     the help context
     */
    public PatientReminderProcessorFactory(Party location, Party practice, HelpContext help) {
        service = ServiceHelper.getArchetypeService();
        this.reminderRules = ServiceHelper.getBean(ReminderRules.class);
        this.patientRules = ServiceHelper.getBean(PatientRules.class);
        this.practiceRules = ServiceHelper.getBean(PracticeRules.class);

        this.location = location;
        this.practice = practice;
        config = getReminderConfig(practice);
        this.help = help;
        if (CommunicationHelper.isLoggingEnabled(practice, service)) {
            logger = ServiceHelper.getBean(CommunicationLogger.class);
        }
        reminderTypes = new ReminderTypes(service);
    }

    /**
     * Creates a processor for reminder items of the specified archetype.
     *
     * @param archetype the reminder item archetype
     * @return a new processor
     * @throws IllegalArgumentException if the archetype is unsupported
     */
    public PatientReminderProcessor create(String archetype) {
        PatientReminderProcessor result;
        if (TypeHelper.matches(archetype, ReminderArchetypes.EMAIL_REMINDER)) {
            result = createEmailProcessor();
        } else if (TypeHelper.matches(archetype, ReminderArchetypes.PRINT_REMINDER)) {
            result = createPrintProcessor();
        } else if (TypeHelper.matches(archetype, ReminderArchetypes.EXPORT_REMINDER)) {
            result = createExportProcessor();
        } else if (TypeHelper.matches(archetype, ReminderArchetypes.SMS_REMINDER)) {
            result = createSMSProcessor();
        } else if (TypeHelper.matches(archetype, ReminderArchetypes.LIST_REMINDER)) {
            result = createListProcessor();
        } else {
            throw new IllegalArgumentException("Unsupported archetype: " + archetype);
        }
        return result;
    }

    /**
     * Creates a batch processor.
     *
     * @param items the reminder items to process
     * @return a new batch processor
     * @throws IllegalArgumentException if the items return more than one archetype, or an unsupported archetype
     */
    public ReminderBatchProcessor createBatchProcessor(ReminderItemSource items) {
        ReminderBatchProcessor result;
        String[] archetypes = items.getArchetypes();
        if (archetypes.length != 1) {
            throw new IllegalArgumentException("Argument 'query' must a single archetype");
        }
        String archetype = archetypes[0];

        if (TypeHelper.matches(archetype, ReminderArchetypes.EMAIL_REMINDER)) {
            result = createBatchEmailProcessor(items);
        } else if (TypeHelper.matches(archetype, ReminderArchetypes.PRINT_REMINDER)) {
            result = createBatchPrintProcessor(items);
        } else if (TypeHelper.matches(archetype, ReminderArchetypes.EXPORT_REMINDER)) {
            result = createExportProcessor(items);
        } else if (TypeHelper.matches(archetype, ReminderArchetypes.SMS_REMINDER)) {
            result = createBatchSMSProcessor(items);
        } else if (TypeHelper.matches(archetype, ReminderArchetypes.LIST_REMINDER)) {
            result = createListProcessor(items);
        } else {
            throw new IllegalArgumentException("Unsupported archetype : " + archetype);
        }
        return result;
    }

    /**
     * Returns the practice.
     *
     * @return the practice
     */
    public Party getPractice() {
        return practice;
    }

    /**
     * Returns the practice location.
     *
     * @return the practice location
     */
    public Party getLocation() {
        return location;
    }

    /**
     * Returns the reminder types.
     *
     * @return the reminder types
     */
    public ReminderTypes getReminderTypes() {
        return reminderTypes;
    }

    /**
     * Returns the reminder configuration.
     *
     * @return the reminder configuration
     */
    public ReminderConfiguration getConfiguration() {
        return config;
    }

    /**
     * Returns the help context.
     *
     * @return the help context
     */
    protected HelpContext getHelp() {
        return help;
    }

    /**
     * Creates a new processor to email reminders.
     *
     * @return a new processor
     */
    protected ReminderEmailProcessor createEmailProcessor() {
        EmailTemplateEvaluator evaluator = ServiceHelper.getBean(EmailTemplateEvaluator.class);
        return new ReminderEmailProcessor(getMailerFactory(), evaluator, getReporterFactory(), reminderTypes, practice,
                                          reminderRules, patientRules, practiceRules, service, config, logger);
    }

    /**
     * Creates a processor to email a batch of reminders.
     *
     * @param query the reminder item query
     * @return a new processor
     */
    protected ReminderBatchProcessor createBatchEmailProcessor(ReminderItemSource query) {
        ReminderEmailProcessor processor = createEmailProcessor();
        return new ReminderEmailProgressBarProcessor(query, processor);
    }

    /**
     * Creates a new processor to print a batch of reminders.
     *
     * @param query the reminder item query
     * @return a new processor
     */
    protected ReminderBatchProcessor createBatchPrintProcessor(ReminderItemSource query) {
        ReminderPrintProcessor processor = createPrintProcessor();
        return new ReminderPrintProgressBarProcessor(query, processor);
    }

    /**
     * Creates a new processor to SMS reminders.
     *
     * @return a new processor
     */
    protected ReminderSMSProcessor createSMSProcessor() {
        ReminderSMSEvaluator evaluator = ServiceHelper.getBean(ReminderSMSEvaluator.class);
        return new ReminderSMSProcessor(getConnectionFactory(), evaluator, reminderTypes, practice, reminderRules,
                                        patientRules, practiceRules, service, config, logger);
    }

    /**
     * Creates a processor to SMS a batch of reminders.
     *
     * @param query the reminder query
     * @return a new processor
     */
    protected ReminderBatchProcessor createBatchSMSProcessor(ReminderItemSource query) {
        ReminderSMSProcessor processor = createSMSProcessor();
        return new ReminderSMSProgressBarProcessor(query, processor);
    }

    /**
     * Creates a new processor to print reminders.
     *
     * @return a new processor
     */
    protected ReminderPrintProcessor createPrintProcessor() {
        ReminderPrintProcessor processor = new ReminderPrintProcessor(help, reminderTypes, reminderRules, patientRules, practice,
                                                                      service, config, getPrinterFactory(), logger);
        processor.setInteractiveAlways(true);
        return processor;
    }

    /**
     * Creates a new export processor.
     *
     * @return a new processor
     */
    protected ReminderExportProcessor createExportProcessor() {
        return new ReminderExportProcessor(reminderTypes, reminderRules, patientRules, location, practice, service, config, logger);
    }

    /**
     * Creates a new export processor.
     *
     * @param query the reminder item query
     * @return a new processor
     */
    protected ReminderBatchProcessor createExportProcessor(ReminderItemSource query) {
        return new ReminderExportBatchProcessor(query, createExportProcessor());
    }

    /**
     * Creates a new reminder list processor.
     *
     * @return a new list processor
     */
    protected ReminderListProcessor createListProcessor() {
        return new ReminderListProcessor(reminderTypes, reminderRules, patientRules, location, practice, service, config,
                                         getPrinterFactory(), logger, help);
    }

    /**
     * Creates a new list processor.
     *
     * @param query the reminder item query
     * @return a new processor
     */
    protected ReminderBatchProcessor createListProcessor(ReminderItemSource query) {
        return new ReminderListBatchProcessor(query, createListProcessor());
    }

    /**
     * Returns the mailer factory.
     * <p>
     * Note that the default factory isn't used as it could be an instance of {@link LoggingMailerFactory};
     * logging is handled by the {@link CommunicationLogger} when logging is enabled.
     *
     * @return the mailer factory
     */
    protected MailerFactory getMailerFactory() {
        if (factory == null) {
            factory = new DefaultMailerFactory(ServiceHelper.getBean(CurrentLocationMailService.class),
                                               ServiceHelper.getBean(DocumentHandlers.class));
        }
        return factory;
    }

    /**
     * Returns the SMS connection factory.
     *
     * @return the SMS connection factory
     */
    protected ConnectionFactory getConnectionFactory() {
        if (smsFactory == null) {
            smsFactory = ServiceHelper.getBean(ConnectionFactory.class);
        }
        return smsFactory;
    }

    /**
     * Returns the printer factory.
     *
     * @return the printer factory
     */
    protected IMPrinterFactory getPrinterFactory() {
        if (printerFactory == null) {
            printerFactory = ServiceHelper.getBean(IMPrinterFactory.class);
        }
        return printerFactory;
    }

    /**
     * Returns the reporter factory.
     *
     * @return the reporter factory
     */
    protected ReporterFactory getReporterFactory() {
        if (reporterFactory == null) {
            reporterFactory = ServiceHelper.getBean(ReporterFactory.class);
        }
        return reporterFactory;
    }

    /**
     * Returns the communication logger.
     *
     * @return the logger, or {@code null} if logging is disabled
     */
    protected CommunicationLogger getLogger() {
        return logger;
    }

    /**
     * Returns the reminder configuration.
     *
     * @return the reminder configuration
     */
    protected ReminderConfiguration getReminderConfig(Party practice) {
        IMObjectBean bean = new IMObjectBean(practice, service);
        IMObject config = bean.getNodeTargetObject("reminderConfiguration");
        if (config == null) {
            throw new IllegalStateException("Patient reminders have not been configured");
        }
        return new ReminderConfiguration(config, service);
    }

}

