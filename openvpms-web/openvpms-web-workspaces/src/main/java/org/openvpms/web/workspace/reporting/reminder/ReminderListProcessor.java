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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.reporting.reminder;

import org.openvpms.archetype.rules.patient.reminder.ReminderArchetypes;
import org.openvpms.archetype.rules.patient.reminder.ReminderEvent;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.print.IMObjectReportPrinter;
import org.openvpms.web.component.im.print.InteractiveIMPrinter;
import org.openvpms.web.component.im.report.ContextDocumentTemplateLocator;
import org.openvpms.web.component.im.report.DocumentTemplateLocator;
import org.openvpms.web.component.print.PrinterListener;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;

import java.util.ArrayList;
import java.util.List;


/**
 * Processor for {@link ReminderEvent.Action#PHONE} and {@link ReminderEvent.Action#LIST} events.
 * Prints all of the reminders to a report.
 *
 * @author Tim Anderson
 */
class ReminderListProcessor extends AbstractReminderBatchProcessor {

    /**
     * The context.
     */
    private final Context context;

    /**
     * The help context.
     */
    private final HelpContext help;


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
        super(reminders, statistics);
        this.context = context;
        this.help = help;
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
        List<ReminderEvent> reminders = getReminders();
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
        super.notifyError(exception);
    }

}
