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

import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.resource.i18n.Messages;

import java.util.Date;
import java.util.List;

/**
 * Exports reminders.
 *
 * @author Tim Anderson
 */
public class ReminderExportBatchProcessor extends AbstractReminderBatchProcessor {

    private final ReminderExportProcessor processor;

    /**
     * Constructs a {@link ReminderExportBatchProcessor}.
     *
     * @param query      the query
     * @param processor  the processor
     * @param statistics the statistics
     */
    public ReminderExportBatchProcessor(ReminderItemSource query, ReminderExportProcessor processor,
                                        Statistics statistics) {
        super(query, processor.getReminderTypes(), statistics);
        this.processor = processor;
    }

    /**
     * Returns the reminder item archetype that this processes.
     *
     * @return the reminder item archetype
     */
    @Override
    public String getArchetype() {
        return processor.getArchetype();
    }

    /**
     * The processor title.
     *
     * @return the processor title
     */
    public String getTitle() {
        return Messages.get("reporting.reminder.run.export");
    }

    /**
     * Processes the batch.
     */
    public void process() {
        List<ObjectSet> reminders = getReminders();
        if (!reminders.isEmpty()) {
            try {
                PatientReminderProcessor.State state = processor.prepare(reminders, new Date());
                processor.process(state);
                updateReminders();
                notifyCompleted();
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
        setStatus(Messages.get("reporting.reminder.export.status.end"));
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
        setStatus(Messages.get("reporting.reminder.export.status.failed"));
        super.notifyError(exception);
    }

}
