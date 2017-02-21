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

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import org.openvpms.archetype.component.processor.AbstractBatchProcessor;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;

import java.util.List;

/**
 * Abstract implementation of the {@link ReminderBatchProcessor} interface.
 *
 * @author Tim Anderson
 */
public abstract class AbstractReminderBatchProcessor extends AbstractBatchProcessor implements ReminderBatchProcessor {

    /**
     * The reminders.
     */
    private final List<ObjectSet> reminders;

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
     * Constructs an {@link AbstractReminderBatchProcessor}.
     *
     * @param query      the query
     * @param statistics the statistics
     */
    public AbstractReminderBatchProcessor(ReminderItemSource query, Statistics statistics) {
        reminders = query.all();
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
    public List<ObjectSet> getReminders() {
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
     * Updates the progress bar with the count of processed reminders and calculates statistics.
     *
     * @param processor the processor
     * @param state     the processed reminders
     */
    protected void updateReminders(PatientReminderProcessor processor, PatientReminderProcessor.State state) {
        setProcessed(reminders.size());
        processor.addStatistics(state, statistics);
    }

    /**
     * Invoked if an error occurs processing the batch.
     * Notifies any listener.
     *
     * @param exception the cause
     */
    @Override
    protected void notifyError(Throwable exception) {
        for (ObjectSet set : reminders) {
            if (update) {
                Act reminder = (Act) set.get("item");
                ReminderHelper.setError(reminder, exception);
            }
            statistics.incErrors();
        }
        super.notifyError(exception);
    }
}
