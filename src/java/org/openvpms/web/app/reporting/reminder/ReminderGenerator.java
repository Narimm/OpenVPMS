/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.reporting.reminder;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.component.processor.AbstractBatchProcessor;
import org.openvpms.archetype.component.processor.BatchProcessor;
import org.openvpms.archetype.component.processor.BatchProcessorListener;
import org.openvpms.archetype.rules.doc.TemplateHelper;
import org.openvpms.archetype.rules.patient.reminder.DueReminderQuery;
import org.openvpms.archetype.rules.patient.reminder.ReminderEvent;
import org.openvpms.archetype.rules.patient.reminder.ReminderProcessor;
import org.openvpms.archetype.rules.patient.reminder.ReminderProcessorException;
import org.openvpms.archetype.rules.patient.reminder.ReminderRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.app.reporting.ReportingException;
import static org.openvpms.web.app.reporting.ReportingException.ErrorCode.ReminderMissingDocTemplate;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.dialog.ConfirmationDialog;
import org.openvpms.web.component.dialog.PopupDialog;
import org.openvpms.web.component.processor.BatchProcessorComponent;
import org.openvpms.web.component.processor.BatchProcessorTask;
import org.openvpms.web.component.processor.ProgressBarProcessor;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.GridFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.workflow.TaskEvent;
import org.openvpms.web.component.workflow.TaskListener;
import org.openvpms.web.component.workflow.WorkflowImpl;
import org.openvpms.web.resource.util.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


/**
 * Reminder generator.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class ReminderGenerator extends AbstractBatchProcessor {

    /**
     * The reminder processors.
     */
    private List<BatchProcessorComponent> processors = new ArrayList<BatchProcessorComponent>();

    /**
     * If <tt>true</tt>, only print reminders.
     */
    private boolean printOnly;

    /**
     * The current generation dialog.
     */
    private GenerationDialog dialog;

    /**
     * The reminder statistics.
     */
    private Statistics statistics = new Statistics();


    /**
     * Constructs a new <tt>ReminderGenerator</tt> to print a single reminder.
     *
     * @param reminder the reminder
     * @param from     only process reminder if its next due date &gt;= from
     * @param to       only process reminder if its next due date &lt;= to
     * @throws ArchetypeServiceException  for any archetype service error
     * @throws ReminderProcessorException for any reminder error
     * @throws ReportingException         if the reminder has no associated document template
     */
    public ReminderGenerator(Act reminder, Date from, Date to) {
        ReminderProcessor processor = new ReminderProcessor(from, to);
        ReminderCollector collector = new ReminderCollector();

        processor.addListener(collector);
        processor.process(reminder);
        List<List<ReminderEvent>> reminders = collector.getReminders();
        if (reminders.size() == 1) {
            ReminderEvent event = reminders.get(0).get(0);
            if (event.getDocumentTemplate() == null) {
                throw new ReportingException(ReminderMissingDocTemplate);
            }
            processors.add(new ReminderPrintProcessor(reminders, event.getDocumentTemplate(), statistics));
        }
        printOnly = true;
    }

    /**
     * Constructs a new <tt>ReminderGenerator</tt> for reminders returned by a
     * query.
     *
     * @param query   the query
     * @param context the context
     * @throws ArchetypeServiceException  for any archetype service error
     * @throws ReminderProcessorException for any error
     */
    public ReminderGenerator(DueReminderQuery query, Context context) {
        this(getReminders(query), query.getFrom(), query.getTo(), context);
        // NOTE: all of the reminders are cached in memory, as the reminder
        // processing affects the paging of the reminder query. A better
        // approach to reduce memory requirements would be
        // to cache the reminder IMObjectReferences
    }

    /**
     * Creates a new <tt>ReminderGenerator</tt>.
     *
     * @param reminders the reminders to process
     * @param from      only process reminder if its next due date &gt;= from
     * @param to        only process reminder if its next due date &lt;= to
     * @param context   the context
     * @throws ArchetypeServiceException  for any archetype service error
     * @throws ReportingException         for any configuration error
     * @throws ReminderProcessorException for any error
     */
    public ReminderGenerator(Iterator<Act> reminders, Date from, Date to, Context context) {
        Party practice = context.getPractice();
        if (practice == null) {
            throw new ReportingException(ReportingException.ErrorCode.NoPractice);
        }
        ReminderRules rules = new ReminderRules();
        Contact email = rules.getEmailContact(practice.getContacts());
        if (email == null) {
            throw new ReportingException(ReportingException.ErrorCode.NoReminderContact, practice.getName());
        }
        IMObjectBean bean = new IMObjectBean(email);
        String address = bean.getString("emailAddress");
        if (StringUtils.isEmpty(address)) {
            throw new ReportingException(ReportingException.ErrorCode.InvalidEmailAddress, address, practice.getName());
        }
        TemplateHelper helper = new TemplateHelper();
        Entity groupTemplate = helper.getTemplateForArchetype("GROUPED_REMINDERS");
        if (groupTemplate == null) {
            throw new ReportingException(ReportingException.ErrorCode.NoGroupReminderTemplate);
        }

        ReminderProcessor processor = new ReminderProcessor(from, to);

        ReminderCollector cancelCollector = new ReminderCollector();
        ReminderCollector listCollector = new ReminderCollector();
        ReminderCollector emailCollector = new ReminderCollector();
        ReminderCollector printCollector = new ReminderCollector();

        processor.addListener(ReminderEvent.Action.CANCEL, cancelCollector);
        processor.addListener(ReminderEvent.Action.EMAIL, emailCollector);
        processor.addListener(ReminderEvent.Action.PRINT, printCollector);

        processor.addListener(ReminderEvent.Action.PHONE, listCollector);
        processor.addListener(ReminderEvent.Action.LIST, listCollector);
        // phone and list reminders get sent to the same report

        while (reminders.hasNext()) {
            processor.process(reminders.next());
        }

        List<List<ReminderEvent>> cancelReminders = cancelCollector.getReminders();
        List<List<ReminderEvent>> emailReminders = emailCollector.getReminders();
        List<List<ReminderEvent>> listReminders = listCollector.getReminders();
        List<List<ReminderEvent>> printReminders = printCollector.getReminders();

        if (!cancelReminders.isEmpty()) {
            processors.add(new ReminderCancelProcessor(cancelReminders, statistics));
        }
        if (!listReminders.isEmpty()) {
            processors.add(new ReminderListProcessor(listReminders, statistics));
        }

        if (!printReminders.isEmpty()) {
            processors.add(new ReminderPrintProcessor(printReminders, groupTemplate, statistics));
        }
        if (!emailReminders.isEmpty()) {
            processors.add(new ReminderEmailProcessor(emailReminders, ServiceHelper.getMailSender(), address,
                                                      practice.getName(), groupTemplate, statistics));
        }
    }

    /**
     * Processes the reminders.
     */
    public void process() {
        if (!printOnly) {
            GenerationDialog dialog = new GenerationDialog();
            dialog.show();
        } else {
            // should only contain the print processor
            for (BatchProcessor processor : processors) {
                processor.setListener(new BatchProcessorListener() {
                    public void completed() {
                        confirmUpdate();
                    }

                    public void error(Throwable exception) {
                        onError(exception);
                    }
                });
                processor.process();
            }
        }
    }

    /**
     * Confirms if reminders should be updated.
     */
    private void confirmUpdate() {
        final ConfirmationDialog dialog = new ConfirmationDialog(
                Messages.get("reporting.reminder.update.title"),
                Messages.get("reporting.reminder.update.message"));
        dialog.addWindowPaneListener(new WindowPaneListener() {
            public void windowPaneClosing(WindowPaneEvent event) {
                if (ConfirmationDialog.OK_ID.equals(dialog.getAction())) {
                }
            }
        });
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
     * Closes the dialog and notifies any listener.
     */
    private void onCompletion() {
        if (dialog != null) {
            dialog.close();
            dialog = null;
        }
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
        List<Act> reminders = new ArrayList<Act>();
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
        private List<Button> restartButtons = new ArrayList<Button>();


        /**
         * Creates a new <tt>GenerationDialog</tt>.
         */
        public GenerationDialog() {
            super(Messages.get("reporting.reminder.run.title"), OK_CANCEL);
            setModal(true);
            workflow = new WorkflowImpl();
            workflow.setBreakOnCancel(false);
            Grid grid = GridFactory.create(3);
            for (BatchProcessorComponent processor : processors) {
                BatchProcessorTask task = new BatchProcessorTask(processor);
                workflow.addTask(task);
                Label title = LabelFactory.create();
                title.setText(processor.getTitle());
                grid.add(title);
                grid.add(processor.getComponent());
                if (processor instanceof ReminderListProcessor || processor instanceof ReminderPrintProcessor) {
                    Button button = addReprintButton(processor);
                    grid.add(button);
                } else {
                    grid.add(LabelFactory.create());
                }
            }
            getLayout().add(grid);
            workflow.addTaskListener(new TaskListener() {
                public void taskEvent(TaskEvent event) {
                    switch (event.getType()) {
                        case COMPLETED:
                            onGenerationComplete();
                            break;
                        default:
                            onCompletion();
                    }
                }
            });
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
         * Invoked when the 'cancel' button is pressed. This prompts for confirmation.
         */
        @Override
        protected void onCancel() {
            String title = Messages.get("reporting.reminder.run.cancel.title");
            String msg = Messages.get("reporting.reminder.run.cancel.message");
            final ConfirmationDialog dialog = new ConfirmationDialog(title, msg);
            dialog.addWindowPaneListener(new WindowPaneListener() {
                public void windowPaneClosing(WindowPaneEvent e) {
                    if (ConfirmationDialog.OK_ID.equals(dialog.getAction())) {
                        workflow.cancel();
                        GenerationDialog.this.close(CANCEL_ID);
                    } else {
                        BatchProcessorComponent processor = getCurrent();
                        if (processor instanceof ProgressBarProcessor) {
                            processor.process();
                        }
                    }
                }
            });
            BatchProcessorComponent processor = getCurrent();
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
        private Button addReprintButton(final BatchProcessorComponent processor) {
            Button button = ButtonFactory.create("reprint", new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    restart(processor);
                }
            });
            restartButtons.add(button);
            return button;
        }

        /**
         * Returns the current batch processor.
         *
         * @return the current batch processor, or <tt>null</tt> if there
         *         is none
         */
        private BatchProcessorComponent getCurrent() {
            BatchProcessorTask task = (BatchProcessorTask) workflow.getCurrent();
            if (task != null) {
                return (BatchProcessorComponent) task.getProcessor();
            }
            return null;
        }

        /**
         * Invoked when generation is complete.
         * Displays statistics.
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
        private void restart(BatchProcessorComponent processor) {
            enableButtons(false);
            statistics.clear();
            processor.restart();
            workflow = new WorkflowImpl();
            workflow.addTask(new BatchProcessorTask(processor));
            workflow.addTaskListener(new TaskListener() {
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
         * Enables/disables the OK/Cancel and restart buttons.
         * <p/>
         * When the OK button is enabled, the cancel button is disabled, and vice-versa.
         *
         * @param enable if <tt>true</tt> enable the buttons; otherwise disable them
         */
        private void enableButtons(boolean enable) {
            getButtons().getButton(OK_ID).setEnabled(enable);
            getButtons().getButton(CANCEL_ID).setEnabled(!enable);
            for (Button button : restartButtons) {
                button.setEnabled(enable);
            }
        }
    }

}
