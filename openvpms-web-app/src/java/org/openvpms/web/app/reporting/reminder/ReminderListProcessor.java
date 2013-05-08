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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.reporting.reminder;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import org.openvpms.archetype.component.processor.AbstractBatchProcessor;
import org.openvpms.archetype.rules.patient.reminder.ReminderArchetypes;
import org.openvpms.archetype.rules.patient.reminder.ReminderEvent;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.component.im.print.IMObjectReportPrinter;
import org.openvpms.web.component.im.print.InteractiveIMPrinter;
import org.openvpms.web.component.im.report.ContextDocumentTemplateLocator;
import org.openvpms.web.component.im.report.DocumentTemplateLocator;
import org.openvpms.web.component.print.PrinterListener;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.echo.i18n.Messages;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Processor for {@link ReminderEvent.Action#PHONE} and {@link ReminderEvent.Action#LIST} events.
 * Prints all of the reminders to a report.
 *
 * @author Tim Anderson
 */
class ReminderListProcessor extends AbstractBatchProcessor implements ReminderBatchProcessor {

    /**
     * Reminders that need to be listed.
     */
    private final List<ReminderEvent> reminders = new ArrayList<ReminderEvent>();

    /**
     * The statistics.
     */
    private final Statistics statistics;

    /**
     * The context.
     */
    private final Context context;

    /**
     * The help context.
     */
    private final HelpContext help;

    /**
     * The component layout row.
     */
    private Row row;

    /**
     * Determines if reminders should be updated on completion.
     */
    private boolean update = true;

    /**
     * The set of completed reminder ids, used to avoid updating reminders that are being reprocessed.
     */
    private Set<IMObjectReference> completed = new HashSet<IMObjectReference>();


    /**
     * Constructs an {@code ReminderListProcessor}.
     *
     * @param reminders  the reminders
     * @param statistics the reminder statistics
     * @param context    the context
     * @param help       the help context
     */
    public ReminderListProcessor(List<List<ReminderEvent>> reminders, Statistics statistics, Context context,
                                 HelpContext help) {
        for (List<ReminderEvent> list : reminders) {
            for (ReminderEvent reminder : list) {
                this.reminders.add(reminder);
            }
        }
        this.statistics = statistics;
        this.context = context;
        this.help = help;
        row = RowFactory.create();
    }

    /**
     * Determines if reminders should be updated on completion.
     * <p/>
     * If set, the {@code reminderCount} is incremented the {@code lastSent} timestamp set on completed reminders.
     *
     * @param update if {@code true} update reminders on completion
     */
    public void setUpdateOnCompletion(boolean update) {
        this.update = update;
    }

    /**
     * The component.
     *
     * @return the component
     */
    public Component getComponent() {
        return row;
    }

    /**
     * The processor title.
     *
     * @return the processor title
     */
    public String getTitle() {
        return Messages.get("reporting.reminder.run.list");
    }

    /**
     * Processes the batch.
     */
    public void process() {
        setStatus(Messages.get("reporting.reminder.list.status.begin"));
        if (!reminders.isEmpty()) {
            try {
                List<Act> acts = new ArrayList<Act>();
                for (ReminderEvent event : reminders) {
                    acts.add(event.getReminder());
                }
                DocumentTemplateLocator locator = new ContextDocumentTemplateLocator(ReminderArchetypes.REMINDER,
                                                                                     context);
                IMObjectReportPrinter<Act> printer = new IMObjectReportPrinter<Act>(acts, locator, context);
                final InteractiveIMPrinter<Act> iPrinter = new InteractiveIMPrinter<Act>(
                    Messages.get("reporting.reminder.list.print.title"), printer, true, context, help);

                iPrinter.setListener(new PrinterListener() {
                    public void printed(String printer) {
                        try {
                            updateReminders();
                            notifyCompleted();
                        } catch (Throwable error) {
                            notifyError(error);
                        }
                    }

                    public void cancelled() {
                        notifyCompleted();
                    }

                    public void skipped() {
                        notifyCompleted();
                    }

                    public void failed(Throwable cause) {
                        notifyError(cause);
                    }
                });
                iPrinter.print();
            } catch (OpenVPMSException exception) {
                notifyError(exception);
            }
        } else {
            notifyCompleted();
        }
    }

    /**
     * Restarts processing.
     */
    public void restart() {
        // no-op
    }

    /**
     * Notifies the listener (if any) of processing completion.
     */
    @Override
    protected void notifyCompleted() {
        setStatus(Messages.get("reporting.reminder.list.status.end"));
        super.notifyCompleted();
    }

    /**
     * Invoked if an error occurs processing the batch.
     * <p/>
     * Sets the error message on each reminder, and notifies any listener.
     *
     * @param exception the cause
     */
    @Override
    protected void notifyError(Throwable exception) {
        setStatus(Messages.get("reporting.reminder.list.status.failed"));
        if (update) {
            for (ReminderEvent event : reminders) {
                ReminderHelper.setError(event.getReminder(), exception);
            }
        }
        super.notifyError(exception);
    }

    /**
     * Sets the status.
     *
     * @param status the status message
     */
    private void setStatus(String status) {
        row.removeAll();
        Label label = LabelFactory.create();
        label.setText(status);
        row.add(label);
    }

    /**
     * Updates reminders.
     */
    private void updateReminders() {
        setProcessed(reminders.size());
        Date date = new Date();
        for (ReminderEvent reminder : reminders) {
            IMObjectReference ref = reminder.getReminder().getObjectReference();
            if (update && !completed.contains(ref)) {
                Act act = reminder.getReminder();
                if (ReminderHelper.update(act, date)) {
                    statistics.increment(reminder);
                    completed.add(ref);
                } else {
                    statistics.incErrors();
                }
            } else {
                statistics.increment(reminder);
            }
        }
    }

}
