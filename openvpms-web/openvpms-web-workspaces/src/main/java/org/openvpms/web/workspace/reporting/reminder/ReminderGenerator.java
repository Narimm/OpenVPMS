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

package org.openvpms.web.workspace.reporting.reminder;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.WindowPaneEvent;
import org.openvpms.archetype.component.processor.AbstractBatchProcessor;
import org.openvpms.archetype.component.processor.BatchProcessor;
import org.openvpms.archetype.component.processor.BatchProcessorListener;
import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.archetype.rules.doc.TemplateHelper;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.patient.reminder.DueReminderQuery;
import org.openvpms.archetype.rules.patient.reminder.ReminderEvent;
import org.openvpms.archetype.rules.patient.reminder.ReminderProcessor;
import org.openvpms.archetype.rules.patient.reminder.ReminderProcessorException;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.sms.ConnectionFactory;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.sms.SMSHelper;
import org.openvpms.web.component.mail.MailContext;
import org.openvpms.web.component.mail.MailerFactory;
import org.openvpms.web.component.processor.BatchProcessorTask;
import org.openvpms.web.component.processor.ProgressBarProcessor;
import org.openvpms.web.component.workflow.DefaultTaskListener;
import org.openvpms.web.component.workflow.TaskEvent;
import org.openvpms.web.component.workflow.WorkflowImpl;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.dialog.InformationDialog;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.event.WindowPaneListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.GridFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.customer.communication.CommunicationHelper;
import org.openvpms.web.workspace.customer.communication.CommunicationLogger;
import org.openvpms.web.workspace.customer.communication.LoggingMailerFactory;
import org.openvpms.web.workspace.reporting.ReportingException;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


/**
 * Reminder generator.
 *
 * @author Tim Anderson
 */
public class ReminderGenerator extends AbstractBatchProcessor {

    /**
     * The reminder processors.
     */
    private List<ReminderBatchProcessor> processors = new ArrayList<>();

    /**
     * If {@code true}, pop up a dialog to perform generation.
     */
    private boolean popup = true;

    /**
     * The reminder statistics.
     */
    private Statistics statistics = new Statistics();

    /**
     * The practice.
     */
    private final Party practice;

    /**
     * The template for grouped reminders.
     */
    private final DocumentTemplate groupTemplate;

    /**
     * The mail context.
     */
    private final MailContext mailContext;

    /**
     * The context.
     */
    private final Context context;

    /**
     * The help context.
     */
    private final HelpContext help;

    /**
     * Determines if reminders can be sent via SMS.
     */
    private final boolean sms;

    /**
     * The mailer factory.
     */
    private MailerFactory factory;

    /**
     * The SMS connection factory.
     */
    private ConnectionFactory smsFactory;

    /**
     * The communication logger, if communications logging is enabled.
     */
    private ReminderCommunicationLogger logger;

    /**
     * Constructs a {@link ReminderGenerator} to process a single reminder.
     *
     * @param event       the reminder event
     * @param context     the context
     * @param mailContext the mail context, used when printing reminders interactively. May be {@code null}
     * @param help        the help context
     */
    public ReminderGenerator(ReminderEvent event, Context context, MailContext mailContext, HelpContext help) {
        this(context, mailContext, help);
        List<List<ReminderEvent>> reminders = new ArrayList<>();
        List<ReminderEvent> group = new ArrayList<>();
        group.add(event);
        reminders.add(group);

        switch (event.getAction()) {
            case EMAIL:
                processors.add(createBatchEmailProcessor(reminders));
                break;
            case PRINT:
                processors.add(createBatchPrintProcessor(reminders, true));
                break;
            case EXPORT:
                processors.add(createExportProcessor(reminders));
                break;
            case SMS:
                if (sms) {
                    processors.add(createBatchSMSProcessor(reminders));
                } else {
                    processors.add(createListProcessor(reminders));
                }
                break;
            case LIST:
            case PHONE:
                processors.add(createListProcessor(reminders));
                break;
            case CANCEL:
                processors.add(createCancelProcessor(reminders));
        }
        popup = false;
    }

    /**
     * Constructs a {@link ReminderGenerator} for reminders returned by a query.
     *
     * @param query       the query
     * @param context     the context
     * @param mailContext the mail context, used when printing reminders interactively. May be {@code null}
     * @param help        the help context
     * @throws ArchetypeServiceException  for any archetype service error
     * @throws ReminderProcessorException for any error
     */
    public ReminderGenerator(DueReminderQuery query, Context context, MailContext mailContext,
                             HelpContext help) {
        this(getReminders(query), query.getFrom(), query.getTo(), context, mailContext, help);
        // TODO: all of the reminders are cached in memory, as the reminder
        // processing affects the paging of the reminder query. A better
        // approach to reduce memory requirements would be
        // to cache the reminder IMObjectReferences
    }

    /**
     * Constructs a {@link ReminderGenerator}.
     *
     * @param reminders   the reminders to process
     * @param from        only process reminder if its next due date &gt;= from
     * @param to          only process reminder if its next due date &lt;= to
     * @param context     the context
     * @param mailContext the mail context, used when printing reminders interactively. May be {@code null}
     * @param help        the help context
     * @throws ArchetypeServiceException  for any archetype service error
     * @throws ReportingException         for any configuration error
     * @throws ReminderProcessorException for any error
     */
    public ReminderGenerator(Iterator<Act> reminders, Date from, Date to, Context context, MailContext mailContext,
                             HelpContext help) {
        this(context, mailContext, help);

        ReminderProcessor processor = new ReminderProcessor(from, to, new Date(), !sms,
                                                            ServiceHelper.getArchetypeService(),
                                                            ServiceHelper.getBean(PatientRules.class));
        ReminderCollector cancelCollector = new ReminderCollector();
        ReminderCollector listCollector = new ReminderCollector();
        ReminderCollector emailCollector = new ReminderCollector();
        ReminderCollector printCollector = new ReminderCollector();
        ReminderCollector exportCollector = new ReminderCollector();
        ReminderCollector smsCollector = new ReminderCollector();

        processor.addListener(ReminderEvent.Action.CANCEL, cancelCollector);
        processor.addListener(ReminderEvent.Action.EMAIL, emailCollector);
        processor.addListener(ReminderEvent.Action.PRINT, printCollector);
        processor.addListener(ReminderEvent.Action.PHONE, listCollector);
        processor.addListener(ReminderEvent.Action.LIST, listCollector);
        processor.addListener(ReminderEvent.Action.EXPORT, exportCollector);
        // phone and list reminders get sent to the same report

        if (sms) {
            processor.addListener(ReminderEvent.Action.SMS, smsCollector);
        } else {
            processor.addListener(ReminderEvent.Action.SMS, listCollector);
        }

        while (reminders.hasNext()) {
            processor.process(reminders.next());
        }

        List<List<ReminderEvent>> cancelReminders = cancelCollector.getReminders();
        List<List<ReminderEvent>> emailReminders = emailCollector.getReminders();
        List<List<ReminderEvent>> listReminders = listCollector.getReminders();
        List<List<ReminderEvent>> printReminders = printCollector.getReminders();
        List<List<ReminderEvent>> exportReminders = exportCollector.getReminders();
        List<List<ReminderEvent>> smsReminders = smsCollector.getReminders();

        if (!cancelReminders.isEmpty()) {
            processors.add(createCancelProcessor(cancelReminders));
        }
        if (!listReminders.isEmpty()) {
            processors.add(createListProcessor(listReminders));
        }

        if (!printReminders.isEmpty()) {
            processors.add(createBatchPrintProcessor(printReminders, false));
        }
        if (!emailReminders.isEmpty()) {
            processors.add(createBatchEmailProcessor(emailReminders));
        }
        if (!smsReminders.isEmpty()) {
            processors.add(createBatchSMSProcessor(smsReminders));
        }
        if (!exportReminders.isEmpty()) {
            processors.add(createExportProcessor(exportReminders));
        }
    }

    /**
     * Constructs a {@link ReminderGenerator}.
     *
     * @param context     the context
     * @param mailContext the mail context
     * @param help        the help context
     */
    private ReminderGenerator(Context context, MailContext mailContext, HelpContext help) {
        practice = context.getPractice();
        if (practice == null) {
            throw new ReportingException(ReportingException.ErrorCode.NoPractice);
        }
        this.context = context;
        this.mailContext = mailContext;
        this.help = help;
        TemplateHelper helper = new TemplateHelper(ServiceHelper.getArchetypeService());
        groupTemplate = helper.getDocumentTemplate("GROUPED_REMINDERS");
        if (groupTemplate == null) {
            throw new ReportingException(ReportingException.ErrorCode.NoGroupedReminderTemplate);
        }
        sms = groupTemplate.getSMSTemplate() != null && SMSHelper.isSMSEnabled(context.getPractice());

        if (CommunicationHelper.isLoggingEnabled(practice)) {
            logger = new ReminderCommunicationLogger(ServiceHelper.getBean(CommunicationLogger.class));
        }
    }

    /**
     * Processes the reminders.
     */
    public void process() {
        if (!processors.isEmpty()) {
            if (popup) {
                GenerationDialog dialog = new GenerationDialog(help);
                dialog.show();
            } else {
                // only processing a single reminder
                for (BatchProcessor processor : processors) {
                    processor.setListener(new BatchProcessorListener() {
                        public void completed() {
                            onCompletion();
                        }

                        public void error(Throwable exception) {
                            onError(exception);
                        }
                    });
                    processor.process();
                }
            }
        } else {
            InformationDialog.show(Messages.get("reporting.reminder.none.title"),
                                   Messages.get("reporting.reminder.none.message"));
        }
    }

    /**
     * Determines if reminders should be updated on completion.
     * <p/>
     * If set, the {@code reminderCount} is incremented and the {@code lastSent} timestamp set on completed reminders.
     * <p/>
     * Defaults to {@code true}.
     *
     * @param update if {@code true} update reminders on completion
     */
    public void setUpdateOnCompletion(boolean update) {
        for (ReminderBatchProcessor processor : processors) {
            processor.setUpdateOnCompletion(update);
        }
    }

    /**
     * Returns the no. of errors encountered during processing.
     *
     * @return the no. of errors
     */
    public int getErrors() {
        return statistics.getErrors();
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
     * Returns the mailer factory.
     * <p/>
     * Note that the default factory isn't used as it could be an instance of {@link LoggingMailerFactory};
     * logging is handled by the {@link ReminderCommunicationLogger} when logging is enabled.
     *
     * @return the mailer factory
     */
    protected MailerFactory getMailerFactory() {
        if (factory == null) {
            factory = new MailerFactory();
        }
        return factory;
    }

    /**
     * Returns the context.
     *
     * @return the context
     */
    protected Context getContext() {
        return context;
    }

    /**
     * Returns the mail context.
     *
     * @return the mail context
     */
    protected MailContext getMailContext() {
        return mailContext;
    }

    /**
     * Returns the reminder communication logger.
     *
     * @return the logger, or {@code null} if logging is disabled
     */
    protected ReminderCommunicationLogger getLogger() {
        return logger;
    }

    /**
     * Creates a processor to email a batch of reminders.
     *
     * @param reminders the reminders to email
     * @return a new processor
     */
    protected ReminderBatchProcessor createBatchEmailProcessor(List<List<ReminderEvent>> reminders) {
        ReminderEmailProcessor processor = createEmailProcessor(practice, groupTemplate, context);
        return new ReminderEmailProgressBarProcessor(reminders, processor, statistics);
    }

    /**
     * Creates a new processor to email reminders.
     *
     * @param practice      the practice
     * @param groupTemplate the template for grouped reminders
     * @param context       the context
     * @return a new processor
     */
    protected ReminderEmailProcessor createEmailProcessor(Party practice, DocumentTemplate groupTemplate,
                                                          Context context) {
        return new ReminderEmailProcessor(getMailerFactory(), practice, groupTemplate, context, logger);
    }

    /**
     * Creates a new processor to print a batch of reminders.
     *
     * @param reminders   the print reminders
     * @param interactive if {@code true}, reminders should always be printed interactively. If {@code false},
     *                    reminders will only be printed interactively if a printer needs to be selected
     * @return a new processor
     */
    protected ReminderBatchProcessor createBatchPrintProcessor(List<List<ReminderEvent>> reminders,
                                                               boolean interactive) {
        ReminderPrintProcessor processor = createPrintProcessor(groupTemplate, context, mailContext, help, interactive);
        return new ReminderPrintProgressBarProcessor(reminders, processor, statistics);
    }

    /**
     * Creates a new processor to print reminders.
     *
     * @param groupTemplate the grouped reminder document template
     * @param context       the context
     * @param mailContext   the mail context, used when printing interactively. May be {@code null}
     * @param help          the help context
     * @param interactive   if {@code true}, reminders should always be printed interactively. If {@code false},
     *                      reminders will only be printed interactively if a printer needs to be selected
     * @return a new processor
     */
    protected ReminderPrintProcessor createPrintProcessor(DocumentTemplate groupTemplate, Context context,
                                                          MailContext mailContext, HelpContext help,
                                                          boolean interactive) {
        ReminderPrintProcessor processor = new ReminderPrintProcessor(groupTemplate, context, mailContext, help,
                                                                      logger);
        processor.setInteractiveAlways(interactive);
        return processor;
    }

    /**
     * Creates a processor to SMS a batch of reminders.
     *
     * @param reminders the reminders to SMS
     * @return a new processor
     */
    protected ReminderBatchProcessor createBatchSMSProcessor(List<List<ReminderEvent>> reminders) {
        ReminderSMSProcessor processor = createSMSProcessor(groupTemplate, context);
        return new ReminderSMSProgressBarProcessor(reminders, processor, statistics);
    }

    /**
     * Creates a processor for SMS reminders.
     *
     * @param groupTemplate the template for grouped reminders
     * @param context       the context
     * @return a new processor
     */
    protected ReminderSMSProcessor createSMSProcessor(DocumentTemplate groupTemplate, Context context) {
        ReminderSMSEvaluator evaluator = ServiceHelper.getBean(ReminderSMSEvaluator.class);
        return new ReminderSMSProcessor(getConnectionFactory(), groupTemplate, context, logger, evaluator);
    }

    /**
     * Creates a new export processor.
     *
     * @param reminders the reminders to export
     * @return a new processor
     */
    protected ReminderBatchProcessor createExportProcessor(List<List<ReminderEvent>> reminders) {
        return createExportProcessor(reminders, statistics);
    }

    /**
     * Creates a new export processor.
     *
     * @param reminders  the reminders to export
     * @param statistics the statistics
     * @return a new processor
     */
    protected ReminderExportProcessor createExportProcessor(List<List<ReminderEvent>> reminders,
                                                            Statistics statistics) {
        return new ReminderExportProcessor(reminders, statistics, context, logger);
    }

    /**
     * Creates a new list processor.
     *
     * @param reminders the reminders to list
     * @return a new processor
     */
    protected ReminderBatchProcessor createListProcessor(List<List<ReminderEvent>> reminders) {
        return createListProcessor(reminders, statistics, context, help);
    }

    /**
     * Creates a new reminder list processor.
     *
     * @param reminders  the reminders
     * @param statistics the reminder statistics
     * @param context    the context
     * @param help       the help context
     * @return a new list processor
     */
    protected ReminderListProcessor createListProcessor(List<List<ReminderEvent>> reminders, Statistics statistics,
                                                        Context context, HelpContext help) {
        return new ReminderListProcessor(reminders, statistics, context, help, logger);
    }

    /**
     * Creates a new cancel processor.
     *
     * @param reminders the reminders to cancel
     * @return a new processor
     */
    protected ReminderBatchProcessor createCancelProcessor(List<List<ReminderEvent>> reminders) {
        return new ReminderCancelProcessor(reminders, statistics);
    }

    /**
     * Displays reminder generation statistics.
     */
    private void showStatistics() {
        SummaryDialog dialog = new SummaryDialog(statistics);
        dialog.show();
    }

    /**
     * Invoked when generation is complete.
     * Notifies any listener.
     */
    private void onCompletion() {
        updateProcessed();
        notifyCompleted();
    }

    /**
     * Invoked if an error occurs processing the batch.
     * Notifies any listener.
     *
     * @param exception the cause
     */
    private void onError(Throwable exception) {
        updateProcessed();
        notifyError(exception);
    }

    /**
     * Updates the count of processed reminders.
     */
    private void updateProcessed() {
        int processed = 0;
        for (BatchProcessor processor : processors) {
            processed += processor.getProcessed();
        }
        setProcessed(processed);
    }

    /**
     * Helper to return an iterator over the reminders.
     *
     * @param query the query
     * @return an iterator over the reminders
     */
    private static Iterator<Act> getReminders(DueReminderQuery query) {
        List<Act> reminders = new ArrayList<>();
        for (Act reminder : query.query()) {
            reminders.add(reminder);
        }
        return reminders.iterator();
    }

    private class GenerationDialog extends PopupDialog {

        /**
         * The workflow.
         */
        private WorkflowImpl workflow;

        /**
         * The restart buttons.
         */
        private List<Button> restartButtons = new ArrayList<>();

        /**
         * The ok button.
         */
        private final Button ok;

        /**
         * The cancel button.
         */
        private final Button cancel;


        /**
         * Constructs a {@code GenerationDialog}.
         */
        public GenerationDialog(HelpContext help) {
            super(Messages.get("reporting.reminder.run.title"), OK_CANCEL, help);
            setModal(true);
            workflow = new WorkflowImpl(help);
            workflow.setBreakOnCancel(false);
            Grid grid = GridFactory.create(3);
            for (ReminderBatchProcessor processor : processors) {
                BatchProcessorTask task = new BatchProcessorTask(processor);
                task.setTerminateOnError(false);
                workflow.addTask(task);
                Label title = LabelFactory.create();
                title.setText(processor.getTitle());
                grid.add(title);
                grid.add(processor.getComponent());
                if (processor instanceof ReminderListProcessor
                    || processor instanceof ReminderPrintProgressBarProcessor) {
                    Button button = addReprintButton(processor);
                    grid.add(button);
                } else if (processor instanceof ReminderExportProcessor) {
                    Button button = addExportButton(processor);
                    grid.add(button);
                } else {
                    grid.add(LabelFactory.create());
                }
            }
            getLayout().add(ColumnFactory.create(Styles.INSET, grid));
            workflow.addTaskListener(new DefaultTaskListener() {
                public void taskEvent(TaskEvent event) {
                    onGenerationComplete();
                }
            });
            ButtonSet buttons = getButtons();
            ok = buttons.getButton(OK_ID);
            cancel = getButtons().getButton(CANCEL_ID);

            // disable OK, restart buttons
            enableButtons(false);
        }

        /**
         * Shows the dialog, and starts the reminder generation workflow.
         */
        public void show() {
            super.show();
            workflow.start();
        }

        /**
         * Invoked when the 'OK' button is pressed. Closes the dialog and invokes
         * {@link ReminderGenerator#onCompletion()}.
         */
        @Override
        protected void onOK() {
            super.onOK();
            onCompletion();
        }

        /**
         * Invoked when the 'cancel' button is pressed. This prompts for confirmation.
         */
        @Override
        protected void onCancel() {
            String title = Messages.get("reporting.reminder.run.cancel.title");
            String msg = Messages.get("reporting.reminder.run.cancel.message");
            final ConfirmationDialog dialog = new ConfirmationDialog(title, msg);
            dialog.addWindowPaneListener(new WindowPaneListener() {
                public void onClose(WindowPaneEvent e) {
                    if (ConfirmationDialog.OK_ID.equals(dialog.getAction())) {
                        workflow.cancel();
                        GenerationDialog.this.close(CANCEL_ID);
                    } else {
                        ReminderBatchProcessor processor = getCurrent();
                        if (processor instanceof ProgressBarProcessor) {
                            processor.process();
                        }
                    }
                }
            });
            ReminderBatchProcessor processor = getCurrent();
            if (processor instanceof ProgressBarProcessor) {
                ((ProgressBarProcessor) processor).setSuspend(true);
            }
            dialog.show();
        }


        /**
         * Adds a button to restart a processor to reprint reminders.
         *
         * @param processor the processor
         * @return a new button
         */
        private Button addReprintButton(final ReminderBatchProcessor processor) {
            Button button = ButtonFactory.create("reprint", new ActionListener() {
                public void onAction(ActionEvent e) {
                    restart(processor);
                }
            });
            restartButtons.add(button);
            return button;
        }

        /**
         * Adds a button to restart a processor to export reminders.
         *
         * @param processor the processor
         * @return a new button
         */
        private Button addExportButton(final ReminderBatchProcessor processor) {
            Button button = ButtonFactory.create("button.reexport", new ActionListener() {
                public void onAction(ActionEvent e) {
                    restart(processor);
                }
            });
            restartButtons.add(button);
            return button;
        }

        /**
         * Returns the current batch processor.
         *
         * @return the current batch processor, or {@code null} if there
         * is none
         */
        private ReminderBatchProcessor getCurrent() {
            BatchProcessorTask task = (BatchProcessorTask) workflow.getCurrent();
            if (task != null) {
                return (ReminderBatchProcessor) task.getProcessor();
            }
            return null;
        }

        /**
         * Invoked when generation is complete.
         * Displays statistics, and enables the reprint and OK buttons.
         */
        private void onGenerationComplete() {
            showStatistics();
            enableButtons(true);
        }

        /**
         * Restarts a batch processor.
         *
         * @param processor the processor to restart
         */
        private void restart(ReminderBatchProcessor processor) {
            enableButtons(false);
            statistics.clear();
            processor.restart();
            workflow = new WorkflowImpl(help);
            BatchProcessorTask task = new BatchProcessorTask(processor);
            task.setTerminateOnError(false);
            workflow.addTask(task);
            workflow.addTaskListener(new DefaultTaskListener() {
                public void taskEvent(TaskEvent event) {
                    if (TaskEvent.Type.COMPLETED.equals(event.getType())) {
                        showStatistics();
                    }
                    enableButtons(true);
                }
            });
            workflow.start();
        }

        /**
         * Enables/disables restart buttons and OK/Cancel buttons.
         * <p/>
         * When the restart buttons are enabled, the OK button is present. When the buttons are disabled,
         * the cancel button is present.
         *
         * @param enable if {@code true} enable the buttons; otherwise disable them
         */
        private void enableButtons(boolean enable) {
            for (Button button : restartButtons) {
                button.setEnabled(enable);
            }
            ButtonSet buttons = getButtons();
            buttons.removeAll();
            if (enable) {
                buttons.add(ok);
            } else {
                buttons.add(cancel);
            }
        }
    }

}

