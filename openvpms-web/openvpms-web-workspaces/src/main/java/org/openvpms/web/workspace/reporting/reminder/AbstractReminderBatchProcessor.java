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
import org.openvpms.archetype.rules.patient.reminder.ReminderType;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.resource.i18n.Messages;

import java.util.Date;
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
     * The processor.
     */
    private final PatientReminderProcessor processor;

    /**
     * The reminders currently being processed.
     */
    private PatientReminders state;

    /**
     * The statistics.
     */
    private final Statistics statistics;

    /**
     * Title localisation key.
     */
    private final String titleKey;

    /**
     * Begin localisation key.
     */
    private final String beginKey;

    /**
     * End localisation key.
     */
    private final String completedKey;

    /**
     * Failure localisation key.
     */
    private final String failedKey;

    /**
     * The component layout row.
     */
    private final Row row;

    /**
     * Determines if reminders are being resent.
     */
    private boolean resend = false;

    /**
     * Constructs an {@link AbstractReminderBatchProcessor}.
     *
     * @param query        the query
     * @param processor    the processor
     * @param statistics   the statistics
     * @param titleKey     the title localisation key
     * @param beginKey     the begin localisation key
     * @param completedKey the completed localisation key
     * @param failedKey    the failed localisation key
     */
    public AbstractReminderBatchProcessor(ReminderItemSource query, PatientReminderProcessor processor,
                                          Statistics statistics, String titleKey, String beginKey,
                                          String completedKey, String failedKey) {
        reminders = query.all();
        this.processor = processor;
        this.statistics = statistics;
        this.titleKey = titleKey;
        this.beginKey = beginKey;
        this.completedKey = completedKey;
        this.failedKey = failedKey;
        row = RowFactory.create();
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
     * Indicates if reminders are being reprocessed.
     * <p/>
     * If set:
     * <ul>
     * <li>due dates are ignored</li>
     * <li>the reminder last sent date is not updated</li>
     * </ul>
     * <p/>
     * Defaults to {@code false}.
     *
     * @param resend if {@code true} reminders are being resent
     */
    public void setResend(boolean resend) {
        this.resend = resend;
    }

    /**
     * Determines if reminders are being resent.
     *
     * @return {@code true} if reeminders are being resent
     */
    public boolean getResend() {
        return resend;
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
     * Returns the processor title.
     *
     * @return the processor title
     */
    @Override
    public String getTitle() {
        return Messages.get(titleKey);
    }

    /**
     * Processes the batch.
     */
    public void process() {
        if (beginKey != null) {
            setStatus(Messages.get(beginKey));
        } else {
            setStatus(null);
        }
        state = null;
        List<ObjectSet> reminders = getReminders();
        if (!reminders.isEmpty()) {
            try {
                state = processor.prepare(reminders, ReminderType.GroupBy.NONE, new Date(), getResend());
                if (!state.getReminders().isEmpty()) {
                    processor.process(state);
                    if (!processor.isAsynchronous()) {
                        completed();
                    }
                } else {
                    completed();
                }
            } catch (OpenVPMSException exception) {
                notifyError(exception);
            }
        } else {
            notifyCompleted();
        }
    }

    protected void completed() {
        updateReminders();
        notifyCompleted();
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
        setStatus(Messages.format(completedKey, reminders.size()));
        super.notifyCompleted();
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
        if (state != null) {
            setProcessed(reminders.size());
            processor.complete(state);
            processor.addStatistics(state, statistics);
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
        setStatus(Messages.get(failedKey));
        for (ObjectSet set : reminders) {
            if (!resend) {
                Act reminder = (Act) set.get("item");
                ReminderHelper.setError(reminder, exception);
            }
            statistics.incErrors();
        }
        super.notifyError(exception);
    }
}
