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

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.event.WindowPaneEvent;
import org.openvpms.archetype.rules.patient.reminder.ReminderArchetypes;
import org.openvpms.web.component.processor.BatchProcessorTask;
import org.openvpms.web.component.processor.ProgressBarProcessor;
import org.openvpms.web.component.workflow.DefaultTaskListener;
import org.openvpms.web.component.workflow.TaskEvent;
import org.openvpms.web.component.workflow.WorkflowImpl;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.event.WindowPaneListener;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.GridFactory;
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
     * The reminder statistics.
     */
    private final Statistics statistics;

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
     * @param statistics the statistics
     * @param help       the help context
     */
    public ReminderGenerationDialog(List<ReminderBatchProcessor> processors, Statistics statistics, HelpContext help) {
        super(Messages.get("reporting.reminder.run.title"), OK_CANCEL, help);
        setModal(true);
        this.statistics = statistics;
        workflow = new WorkflowImpl(help);
        workflow.setBreakOnCancel(false);
        Grid grid = GridFactory.create(2);
        for (ReminderBatchProcessor processor : sort(processors)) {
            BatchProcessorTask task = new BatchProcessorTask(processor);
            task.setTerminateOnError(false);
            workflow.addTask(task);
            Label title = LabelFactory.create();
            title.setText(processor.getTitle());
            grid.add(title);
            grid.add(processor.getComponent());
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
     * Displays reminder generation statistics.
     */
    private void showStatistics() {
        SummaryDialog dialog = new SummaryDialog(statistics);
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
     * Displays statistics, and enables the OK button.
     */
    private void onGenerationComplete() {
        showStatistics();
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

}
