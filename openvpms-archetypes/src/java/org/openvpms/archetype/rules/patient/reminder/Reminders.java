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

import java.util.Collections;
import java.util.List;

/**
 * A collection of {@link ReminderEvent}s.
 *
 * @author Tim Anderson
 * @see GroupingReminderIterator
 */
public class Reminders {

    /**
     * The reminders.
     */
    private List<ReminderEvent> reminders;

    /**
     * The reminder grouping policy.
     */
    private ReminderType.GroupBy groupBy;

    /**
     * Constructs a {@link Reminders}.
     *
     * @param reminder the reminder
     */
    public Reminders(ReminderEvent reminder) {
        this(Collections.singletonList(reminder), ReminderType.GroupBy.NONE);
    }

    /**
     * Constructs a {@link Reminders}.
     *
     * @param reminders the reminders
     * @param groupBy   the reminder grouping policy
     */
    public Reminders(List<ReminderEvent> reminders, ReminderType.GroupBy groupBy) {
        this.reminders = reminders;
        this.groupBy = groupBy;
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
    public static boolean contains(Act item, List<ReminderEvent> reminders) {
        return find(item, reminders) != null;
    }

    /**
     * Finds a reminder event containing the specified reminder item.
     *
     * @param item      the reminder item, an <em>act.patientReminderItem*</em>.
     * @param reminders the reminders
     * @return the reminder, or {@code null} if none is found
     */
    public static ReminderEvent find(Act item, List<ReminderEvent> reminders) {
        IMObjectReference reference = item.getObjectReference();
        for (ReminderEvent event : reminders) {
            Act act = event.getItem();
            if (act.getObjectReference().equals(reference)) {
                return event;
            }
        }
        return null;
    }

    public static Act findItem(Act item, List<ReminderEvent> reminders) {
        ReminderEvent event = find(item, reminders);
        return event != null ? event.getItem() : null;
    }


}
