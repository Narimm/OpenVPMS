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

package org.openvpms.web.app.patient.reminder;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import org.openvpms.archetype.component.processor.BasicBatchProcessor;
import org.openvpms.archetype.rules.patient.reminder.ReminderEvent;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.im.print.IMObjectReportPrinter;
import org.openvpms.web.component.im.print.InteractiveIMPrinter;
import org.openvpms.web.component.print.PrinterListener;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.RowFactory;

import java.util.ArrayList;
import java.util.List;


/**
 * Processor for {@link ReminderEvent.Action.PHONE} events.
 * Prints all of the reminders to a report.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class ReminderPhoneProcessor extends BasicBatchProcessor
        implements BatchProcessorComponent {

    /**
     * Reminders that need to be listed to phone.
     */
    private final List<Act> reminders = new ArrayList<Act>();

    private Row row;


    /**
     * Creates a new <tt>ReminderPhoneProcessor</tt>.
     *
     * @param reminders the reminders
     */
    public ReminderPhoneProcessor(List<ReminderEvent> reminders) {
        for (ReminderEvent reminder : reminders) {
            this.reminders.add(reminder.getReminder());
        }
        row = RowFactory.create();
    }

    public Component getComponent() {
        return row;
    }

    public String getTitle() {
        return "Phone";
    }

    /**
     * Processes the batch.
     */
    public void process() {
        setStatus("Printing phone list");
        if (!reminders.isEmpty()) {
            try {
                IMObjectReportPrinter<Act> printer
                        = new IMObjectReportPrinter<Act>(reminders,
                                                         "act.patientReminder");
                final InteractiveIMPrinter<Act> iPrinter
                        = new InteractiveIMPrinter<Act>("Print phone listing",
                                                        printer, true);

                iPrinter.setListener(new PrinterListener() {
                    public void printed() {
                        setProcessed(reminders.size());
                        notifyCompleted();
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

    public void restart() {
        process();
    }

    /**
     * Notifies the listener (if any) of processing completion.
     */
    @Override
    protected void notifyCompleted() {
        setStatus("Done");
        super.notifyCompleted();
    }

    /**
     * Invoked if an error occurs processing the batch.
     * Notifies any listener.
     *
     * @param exception the cause
     */
    @Override
    protected void notifyError(Throwable exception) {
        setStatus("Failed");
        super.notifyError(exception);
    }

    private void setStatus(String status) {
        row.removeAll();
        Label label = LabelFactory.create();
        label.setText(status);
        row.add(label);
    }


}
