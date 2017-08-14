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

package org.openvpms.web.workspace.reporting.reminder;

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.event.WindowPaneEvent;
import org.openvpms.archetype.rules.patient.reminder.ReminderArchetypes;
import org.openvpms.archetype.rules.patient.reminder.ReminderEvent;
import org.openvpms.archetype.rules.patient.reminder.ReminderType;
import org.openvpms.web.component.im.layout.ComponentGrid;
import org.openvpms.web.component.processor.BatchProcessorTask;
import org.openvpms.web.component.processor.ProgressBarProcessor;
import org.openvpms.web.component.workflow.DefaultTaskListener;
import org.openvpms.web.component.workflow.Task;
import org.openvpms.web.component.workflow.TaskEvent;
import org.openvpms.web.component.workflow.WorkflowImpl;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.event.WindowPaneListener;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Dialog to generates reminders.
 *
 * @author Tim Anderson
 */
class ReminderGenerationDialog extends PopupDialog {

    /**
     * The workflow.
     */
    private WorkflowImpl workflow;

    /**
     * The ok button.
     */
    private final Button ok;

    /**
     * The cancel button.
     */
    private final Button cancel;


    /**
     * Constructs a {@link ReminderGenerationDialog}.
     *
     * @param processors the processors
     * @param help       the help context
     */
    public ReminderGenerationDialog(List<ReminderBatchProcessor> processors, HelpContext help) {
        super(Messages.get("reporting.reminder.run.title"), "ReminderGenerationDialog.Large", OK_CANCEL, help);
        setModal(true);
        workflow = new WorkflowImpl(help);
        workflow.setBreakOnCancel(false);
        ComponentGrid grid = new ComponentGrid();
        if (processors.size() == 1) {
            // if there is only one dialog, use the small layout
            setStyleName("ReminderGenerationDialog.Small");
        }
        for (ReminderBatchProcessor processor : sort(processors)) {
            ReminderBatchProcessorTask task = new ReminderBatchProcessorTask(processor);
            task.setTerminateOnError(false);
            workflow.addTask(task);
            Label title = LabelFactory.create(null, Styles.BOLD);
            title.setText(processor.getTitle());

            SendStats statistics = new SendStats();
            Component component = processor.getComponent();
            if (component == null) {
                component = LabelFactory.create();
            }

            grid.add(title, component, task.getStatus());
            grid.add(new Label(), LabelFactory.create("reporting.reminder.run.sent"), statistics.getSentComponent());
            grid.add(new Label(), LabelFactory.create("reporting.reminder.run.errors"),
                     statistics.getErrorComponent());
            grid.add(new Label(), LabelFactory.create("reporting.reminder.run.cancelled"),
                     statistics.getCancelledComponent());
            processor.setStatistics(statistics);
        }
        getLayout().add(ColumnFactory.create(Styles.LARGE_INSET, grid.createGrid()));
        workflow.addTaskListener(new DefaultTaskListener() {
            public void taskEvent(TaskEvent event) {
                onGenerationComplete();
            }
        });
        ButtonSet buttons = getButtons();
        ok = buttons.getButton(OK_ID);
        cancel = getButtons().getButton(CANCEL_ID);

        // disable OK button
        ok.setEnabled(false);
        cancel.setEnabled(true);
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
        final ConfirmationDialog dialog = new ConfirmationDialog(title, msg, YES_NO);
        dialog.addWindowPaneListener(new WindowPaneListener() {
            public void onClose(WindowPaneEvent e) {
                if (ConfirmationDialog.YES_ID.equals(dialog.getAction())) {
                    workflow.cancel();
                    ReminderGenerationDialog.this.close(CANCEL_ID);
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
     * Enables the OK button.
     */
    private void onGenerationComplete() {
        ok.setEnabled(true);
        cancel.setEnabled(false);
    }

    /**
     * Sorts processors based on their archetype.
     *
     * @param processors the processors
     * @return the sorted processors
     */
    protected List<ReminderBatchProcessor> sort(List<ReminderBatchProcessor> processors) {
        List<ReminderBatchProcessor> result = new ArrayList<>(processors);
        Collections.sort(result, new Comparator<ReminderBatchProcessor>() {
            @Override
            public int compare(ReminderBatchProcessor o1, ReminderBatchProcessor o2) {
                return Integer.compare(getSortIndex(o1), getSortIndex(o2));
            }
        });
        return result;
    }

    private int getSortIndex(ReminderBatchProcessor processor) {
        switch (processor.getArchetype()) {
            case ReminderArchetypes.EMAIL_REMINDER:
                return 0;
            case ReminderArchetypes.SMS_REMINDER:
                return 1;
            case ReminderArchetypes.PRINT_REMINDER:
                return 2;
            case ReminderArchetypes.EXPORT_REMINDER:
                return 3;
            default:
                return 4;
        }
    }

    private static class ReminderBatchProcessorTask extends BatchProcessorTask {

        private final Label status;

        /**
         * Creates a new {@link ReminderBatchProcessorTask}.
         *
         * @param processor the processor
         */
        public ReminderBatchProcessorTask(ReminderBatchProcessor processor) {
            super(processor);
            status = LabelFactory.create(null, Styles.BOLD);
        }

        public Label getStatus() {
            return status;
        }

        /**
         * Notifies the registered listener of a task about to start.
         *
         * @param task the task
         */
        @Override
        protected void notifyStarting(Task task) {
            super.notifyStarting(task);
            status.setText(Messages.get("reporting.reminder.run.running"));
        }

        /**
         * Notifies any registered listener that the task has completed.
         *
         * @throws IllegalStateException if notification has already occurred
         */
        @Override
        protected void notifyCompleted() {
            super.notifyCompleted();
            ReminderBatchProcessor processor = (ReminderBatchProcessor) getProcessor();
            if (processor.hasMoreReminders()) {
                status.setText(Messages.get("reporting.reminder.run.completedmoreavail"));
            } else {
                status.setText(Messages.get("reporting.reminder.run.completed"));
            }
        }

        /**
         * Notifies any registered listener that the task has been cancelled.
         *
         * @throws IllegalStateException if notification has already occurred
         */
        @Override
        protected void notifyCancelled() {
            super.notifyCancelled();
            status.setText(Messages.get("reporting.reminder.run.cancelled"));
        }
    }

    private static class SendStats extends Statistics {

        private Label sentLabel;
        private Label errorLabel;
        private Label cancelledLabel;

        public SendStats() {
            sentLabel = createLabel();
            errorLabel = createLabel();
            cancelledLabel = createLabel();
        }

        public Label getSentComponent() {
            return sentLabel;
        }

        public Label getErrorComponent() {
            return errorLabel;
        }

        public Label getCancelledComponent() {
            return cancelledLabel;
        }

        /**
         * Increments the count for a reminder.
         *
         * @param reminder     the reminder
         * @param reminderType the reminder type
         */
        @Override
        public void increment(ReminderEvent reminder, ReminderType reminderType) {
            super.increment(reminder, reminderType);
            sentLabel.setText(Integer.toString(getCount()));
        }

        /**
         * Adds errors.
         *
         * @param errors the no. of errors
         */
        @Override
        public void addErrors(int errors) {
            super.addErrors(errors);
            errorLabel.setText(Integer.toString(getErrors()));
        }

        /**
         * Adds to the no. of cancelled reminder items.
         *
         * @param cancelled the no. of cancelled reminder items
         */
        @Override
        public void addCancelled(int cancelled) {
            super.addCancelled(cancelled);
            cancelledLabel.setText(Integer.toString(getCancelled()));
        }

        private Label createLabel() {
            final Label label = new Label();
            label.setLayoutData(ComponentGrid.layout(Alignment.ALIGN_RIGHT));
            label.setText("0");
            return label;
        }
    }

}
