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
import org.openvpms.archetype.component.processor.AbstractBatchProcessor;
import org.openvpms.archetype.rules.patient.reminder.ReminderEvent;
import org.openvpms.archetype.rules.patient.reminder.ReminderType;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.system.common.exception.OpenVPMSException;
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
    private final List<ReminderEvent> reminders;

    /**
     * The processor.
     */
    private final PatientReminderProcessor processor;

    /**
     * The reminders currently being processed.
     */
    private PatientReminders state;

    /**
     * Title localisation key.
     */
    private final String titleKey;

    /**
     * Determines if reminders are being resent.
     */
    private boolean resend = false;

    /**
     * The statistics.
     */
    private Statistics statistics;

    /**
     * Constructs an {@link AbstractReminderBatchProcessor}.
     *
     * @param query        the query
     * @param processor    the processor
     * @param titleKey     the title localisation key
     */
    public AbstractReminderBatchProcessor(ReminderItemSource query, PatientReminderProcessor processor,
                                          String titleKey) {
        reminders = query.all();
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
     * @return {@code null} - this doesn't render a component
     */
    public Component getComponent() {
        return null;
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
     * Returns the processor title.
     *
     * @return the processor title
     */
    @Override
    public String getTitle() {
        return Messages.get(titleKey);
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
        List<ReminderEvent> reminders = getReminders();
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
            setProcessed(reminders.size());
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
        for (ReminderEvent event : reminders) {
            if (!resend) {
                Act item = event.getItem();
                ReminderHelper.setError(item, exception);
            }
            if (statistics != null) {
                statistics.incErrors();
            }
        }
        super.notifyError(exception);
    }
}
