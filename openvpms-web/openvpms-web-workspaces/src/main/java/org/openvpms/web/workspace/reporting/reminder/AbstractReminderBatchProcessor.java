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

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import org.openvpms.archetype.component.processor.AbstractBatchProcessor;
import org.openvpms.archetype.rules.patient.reminder.ReminderEvent;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Enter description.
 *
 * @author Tim Anderson
 */
public abstract class AbstractReminderBatchProcessor extends AbstractBatchProcessor implements ReminderBatchProcessor {

    /**
     * The reminders.
     */
    private final List<ReminderEvent> reminders = new ArrayList<ReminderEvent>();

    /**
     * The statistics.
     */
    private final Statistics statistics;

    /**
     * The component layout row.
     */
    private final Row row;

    /**
     * Determines if reminders should be updated on completion.
     */
    private boolean update = true;

    /**
     * The set of completed reminder ids, used to avoid updating reminders that are being reprocessed.
     */
    private Set<IMObjectReference> completed = new HashSet<IMObjectReference>();

    /**
     * Constructs an {@link AbstractReminderBatchProcessor}.
     *
     * @param reminders  the reminders
     * @param statistics the statistics
     */
    public AbstractReminderBatchProcessor(List<List<ReminderEvent>> reminders, Statistics statistics) {
        for (List<ReminderEvent> list : reminders) {
            for (ReminderEvent reminder : list) {
                this.reminders.add(reminder);
            }
        }
        this.statistics = statistics;
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
     * Returns the reminders.
     *
     * @return the reminders
     */
    public List<ReminderEvent> getReminders() {
        return reminders;
    }

    /**
     * Sets the status.
     *
     * @param status the status message
     */
    protected void setStatus(String status) {
        row.removeAll();
        Label label = LabelFactory.create();
        label.setText(status);
        row.add(label);
    }

    /**
     * Updates reminders.
     */
    protected void updateReminders() {
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

    /**
     * Invoked if an error occurs processing the batch.
     * Notifies any listener.
     *
     * @param exception the cause
     */
    @Override
    protected void notifyError(Throwable exception) {
        for (ReminderEvent event : reminders) {
            if (update) {
                ReminderHelper.setError(event.getReminder(), exception);
            }
            statistics.incErrors();
        }
        super.notifyError(exception);
    }
}
