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

import org.openvpms.archetype.rules.patient.reminder.ReminderEvent;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.component.print.PrinterListener;
import org.openvpms.web.resource.i18n.Messages;

import java.util.Date;
import java.util.List;


/**
 * Processor for {@link ReminderEvent.Action#PHONE} and {@link ReminderEvent.Action#LIST} events.
 * Prints all of the reminders to a report.
 *
 * @author Tim Anderson
 */
public class ReminderListBatchProcessor extends AbstractReminderBatchProcessor {

    /**
     * The processor.
     */
    private final ReminderListProcessor processor;

    /**
     * Constructs an {@link ReminderListBatchProcessor}.
     *
     * @param query      the query
     * @param processor  the reminder processor
     * @param statistics the statistics
     */
    public ReminderListBatchProcessor(ReminderItemSource query, ReminderListProcessor processor, Statistics statistics) {
        super(query, processor.getReminderTypes(), statistics);
        this.processor = processor;
        processor.setListener(new PrinterListener() {
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
        List<ObjectSet> reminders = getReminders();
        if (!reminders.isEmpty()) {
            try {
                PatientReminderProcessor.State state = processor.prepare(reminders, new Date());
                processor.process(state);
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
