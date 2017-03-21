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
import org.openvpms.archetype.rules.patient.reminder.ReminderType;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Patient reminders prepared for processing by an {@link PatientReminderProcessor}.
 *
 * @author Tim Anderson
 */
public class PatientReminders {

    /**
     * The reminders to process.
     */
    private final List<ReminderEvent> reminders;

    /**
     * The reminder grouping policy. This is used to determine which document template, if any, is selected to process
     * reminders.
     */
    private final ReminderType.GroupBy groupBy;

    /**
     * Cancelled reminders.
     */
    private final List<ReminderEvent> cancelled;

    /**
     * Reminders in error.
     */
    private final List<ReminderEvent> errors;

    /**
     * Reminders or their items that have been updated.
     */
    private final List<Act> updated;

    /**
     * Determines if reminders are being resent.
     */
    private final boolean resend;

    /**
     * Constructs a {@link PatientReminders}.
     *
     * @param reminders the reminders to send
     * @param groupBy   the reminder grouping policy. This is used to determine which document template, if any, is
     *                  selected to process reminders.
     * @param cancelled reminders that have been cancelled
     * @param errors    reminders that are in error
     * @param updated   reminders/reminder items that have been updated
     * @param resend    determines if reminders are being resent
     */
    public PatientReminders(List<ReminderEvent> reminders, ReminderType.GroupBy groupBy, List<ReminderEvent> cancelled,
                            List<ReminderEvent> errors, List<Act> updated, boolean resend) {
        this.reminders = reminders;
        this.groupBy = groupBy;
        this.cancelled = cancelled;
        this.errors = errors;
        this.updated = updated;
        this.resend = resend;
    }

    /**
     * Returns the reminders to send.
     *
     * @return the reminders
     */
    public List<ReminderEvent> getReminders() {
        return reminders;
    }

    /**
     * Returns the reminder grouping policy.
     *
     * @return the reminder grouping policy
     */
    public ReminderType.GroupBy getGroupBy() {
        return groupBy;
    }

    /**
     * Adds an updated act, to be saved on completion of processing.
     *
     * @param act the updated act
     */
    public void updated(Act act) {
        updated.add(act);
    }

    /**
     * Returns the updated acts.
     *
     * @return the updated acts
     */
    public List<Act> getUpdated() {
        return updated;
    }

    /**
     * Returns the cancelled reminders.
     *
     * @return the cancelled reminders
     */
    public List<ReminderEvent> getCancelled() {
        return cancelled;
    }

    /**
     * Returns the reminders in error.
     *
     * @return the reminders in error
     */
    public List<ReminderEvent> getErrors() {
        return errors;
    }

    /**
     * Determines if reminders are being resent.
     *
     * @return {@code true} if reminders are being resent
     */
    public boolean getResend() {
        return resend;
    }

    /**
     * Creates a context for the reminders.
     *
     * @param practice the practice
     * @return a new context
     */
    public Context createContext(Party practice) {
        Context context = new LocalContext();
        context.setPractice(practice);
        return context;
    }

    /**
     * Returns the number of processed reminder items.
     *
     * @return the number of processed reminder items
     */
    public int getProcessed() {
        Set<ObjectSet> sets = new HashSet<>();
        sets.addAll(reminders);
        sets.removeAll(cancelled);
        sets.removeAll(errors);
        return sets.size();
    }

    /**
     * Returns the supplied remidners as a collection of {@link ObjectSet}s.
     *
     * @param reminders the reminders
     * @return the reminders as {@link ObjectSet}s
     */
    public List<ObjectSet> getObjectSets(List<ReminderEvent> reminders) {
        List<ObjectSet> sets = new ArrayList<>();
        sets.addAll(reminders);
        return sets;
    }

}
