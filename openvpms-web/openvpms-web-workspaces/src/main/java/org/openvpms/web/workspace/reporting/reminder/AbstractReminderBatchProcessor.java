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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.reporting.reminder;

import nextapp.echo2.app.Component;
import org.openvpms.archetype.component.processor.AbstractBatchProcessor;
import org.openvpms.archetype.rules.patient.reminder.ReminderEvent;
import org.openvpms.archetype.rules.patient.reminder.ReminderType;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.web.resource.i18n.Messages;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Abstract implementation of the {@link ReminderBatchProcessor} interface.
 *
 * @author Tim Anderson
 */
public abstract class AbstractReminderBatchProcessor extends AbstractBatchProcessor implements ReminderBatchProcessor {

    /**
     * The reminder query.
     */
    private final ReminderItemSource query;

    /**
     * The processor.
     */
    private final PatientReminderProcessor processor;

    /**
     * Title localisation key.
     */
    private final String titleKey;

    /**
     * The reminders currently being processed.
     */
    private PatientReminders state;

    /**
     * Determines if reminders are being resent.
     */
    private boolean resend = false;

    /**
     * Determines if more reminders are available on completion.
     */
    private boolean moreAvailable = false;

    /**
     * The statistics.
     */
    private Statistics statistics;

    /**
     * The maximum number of reminders to process.
     */
    private static final int BATCH_SIZE = 1000;

    /**
     * Constructs an {@link AbstractReminderBatchProcessor}.
     *
     * @param query     the query
     * @param processor the processor
     * @param titleKey  the title localisation key
     */
    public AbstractReminderBatchProcessor(ReminderItemSource query, PatientReminderProcessor processor,
                                          String titleKey) {
        this.query = query;
        this.processor = processor;
        this.titleKey = titleKey;
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
     * Determines if reminders are being resent.
     *
     * @return {@code true} if reeminders are being resent
     */
    public boolean getResend() {
        return resend;
    }

    /**
     * Indicates if reminders are being reprocessed.
     * <p>
     * If set:
     * <ul>
     * <li>due dates are ignored</li>
     * <li>the reminder last sent date is not updated</li>
     * </ul>
     * <p>
     * Defaults to {@code false}.
     *
     * @param resend if {@code true} reminders are being resent
     */
    public void setResend(boolean resend) {
        this.resend = resend;
    }

    /**
     * The component.
     *
     * @return {@code null} - this doesn't render a component
     */
    public Component getComponent() {
        return null;
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
     * Determines if there are more reminders available on completion of processing.
     *
     * @return {@code true} if there are more reminders available
     */
    public boolean hasMoreReminders() {
        return moreAvailable;
    }

    /**
     * Registers the statistics.
     *
     * @param statistics the statistics
     */
    public void setStatistics(Statistics statistics) {
        this.statistics = statistics;
    }

    /**
     * Processes the batch.
     */
    public void process() {
        state = null;
        Iterator<ReminderEvent> iterator = query.all();
        List<ReminderEvent> reminders = new ArrayList<>();
        while ((moreAvailable = iterator.hasNext()) && reminders.size() < BATCH_SIZE) {
            reminders.add(iterator.next());
        }

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
     * Notifies the listener (if any) of processing completion.
     */
    @Override
    protected void notifyCompleted() {
        super.notifyCompleted();
    }

    /**
     * Updates reminders.
     */
    protected void updateReminders() {
        if (state != null) {
            setProcessed(state.getProcessed());
            processor.complete(state);
            if (statistics != null) {
                processor.addStatistics(state, statistics);
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
        if (state != null) {
            processor.failed(state, exception);
            if (statistics != null) {
                statistics.addErrors(state.getReminders().size());
                statistics.addErrors(state.getErrors().size());
            }
        }
        super.notifyError(exception);
    }
}
