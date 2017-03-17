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

package org.openvpms.archetype.rules.patient.reminder;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.system.common.query.ObjectSet;

import java.util.Collections;
import java.util.List;

/**
 * A collection of reminders, contained in {@link ObjectSet}s.
 * <p/>
 * Each set contains:
 * <ul>
 * <li>item - an <em>act.patientReminderItem*</em></li>
 * <li>reminder - the associated <em>act.patientReminder</em></li>
 * <li>patient - the patient linked to <em>reminder</em></li>
 * <li>customer - the customer linked to <em>patient</em></li>
 * </ul>
 *
 * @author Tim Anderson
 * @see GroupingReminderIterator
 */
public class Reminders {

    /**
     * The reminders.
     */
    private List<ObjectSet> reminders;

    /**
     * The reminder grouping policy.
     */
    private ReminderType.GroupBy groupBy;

    /**
     * Constructs a {@link Reminders}.
     *
     * @param reminder the reminder
     */
    public Reminders(ObjectSet reminder) {
        this(Collections.singletonList(reminder), ReminderType.GroupBy.NONE);
    }

    /**
     * Constructs a {@link Reminders}.
     *
     * @param reminders the reminders
     * @param groupBy   the reminder grouping policy
     */
    public Reminders(List<ObjectSet> reminders, ReminderType.GroupBy groupBy) {
        this.reminders = reminders;
        this.groupBy = groupBy;
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
     * Returns the reminder grouping policy.
     *
     * @return the reminder grouping policy
     */
    public ReminderType.GroupBy getGroupBy() {
        return groupBy;
    }

    /**
     * Determines if the reminders contains the specified reminder item.
     *
     * @param item the reminder item, an <em>act.patientReminderItem*</em>.
     * @return {@code true} if the reminders contains the reminder item, otherwise {@code false}
     */
    public boolean contains(Act item) {
        return contains(item, reminders);
    }

    /**
     * Determines if the reminders contains the specified reminder item.
     *
     * @param item      the reminder item, an <em>act.patientReminderItem*</em>.
     * @param reminders the reminders
     * @return {@code true} if the reminders contains the reminder item, otherwise {@code false}
     */
    public static boolean contains(Act item, List<ObjectSet> reminders) {
        return find(item, reminders) != null;
    }

    /**
     * Finds a reminder {@link ObjectSet} containing the specified reminder item.
     *
     * @param item      the reminder item, an <em>act.patientReminderItem*</em>.
     * @param reminders the reminders
     * @return the reminder, or {@code null} if none is found
     */
    public static ObjectSet find(Act item, List<ObjectSet> reminders) {
        IMObjectReference reference = item.getObjectReference();
        for (ObjectSet set : reminders) {
            Act act = getItem(set);
            if (act.getObjectReference().equals(reference)) {
                return set;
            }
        }
        return null;
    }

    /**
     * Returns the reminder item from a reminder {@link ObjectSet}.
     *
     * @param reminder the reminder. May be {@code null}
     * @return the reminder item, or {@code null} if {@code reminder} is {@code null}
     */
    public static Act getItem(ObjectSet reminder) {
        return (reminder != null) ? (Act) reminder.get("item") : null;
    }

}
