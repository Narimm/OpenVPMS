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
import org.openvpms.component.business.domain.im.common.Entity;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * Reminder statistics.
 *
 * @author Tim Anderson
 */
public class Statistics {

    /**
     * Tracks statistics by reminder type.
     */
    private final Map<Entity, Map<String, Integer>> statistics = new HashMap<>();

    /**
     * The no. of errors encountered.
     */
    private int errors;

    /**
     * The no. of cancelled reminders.
     */
    private int cancelled;


    /**
     * Increments the count for a reminder.
     *
     * @param reminder     the reminder
     * @param reminderType the reminder type
     */
    public void increment(ReminderEvent reminder, ReminderType reminderType) {
        Act item = reminder.getItem();
        Entity entity = reminderType.getEntity();
        Map<String, Integer> stats = statistics.get(entity);
        if (stats == null) {
            stats = new HashMap<>();
            statistics.put(entity, stats);
        }
        String shortName = item.getArchetypeId().getShortName();
        Integer count = stats.get(shortName);
        if (count == null) {
            stats.put(shortName, 1);
        } else {
            stats.put(shortName, count + 1);
        }
    }

    /**
     * Returns the sent count for all reminder items.
     *
     * @return the sent count
     */
    public int getCount() {
        int result = 0;
        for (Map<String, Integer> stats : statistics.values()) {
            for (Integer count : stats.values()) {
                if (count != null) {
                    result += count;
                }
            }
        }
        return result;
    }

    /**
     * Returns the count for a reminder item.
     *
     * @param shortName the reminder item archetype short name
     * @return the count for the reminder item
     */
    public int getCount(String shortName) {
        int result = 0;
        for (Map<String, Integer> stats : statistics.values()) {
            Integer count = stats.get(shortName);
            if (count != null) {
                result += count;
            }
        }
        return result;
    }

    /**
     * Returns all reminder types for which there are statistics.
     *
     * @return the reminder types
     */
    public Collection<Entity> getReminderTypes() {
        return statistics.keySet();
    }

    /**
     * Returns the count for a reminder type and set of reminder items.
     *
     * @param reminderType the reminder type
     * @param shortNames   the reminder item short names
     * @return the count
     */
    public int getCount(Entity reminderType, String... shortNames) {
        int result = 0;
        Map<String, Integer> stats = statistics.get(reminderType);
        if (stats != null) {
            for (String shortName : shortNames) {
                Integer value = stats.get(shortName);
                if (value != null) {
                    result += value;
                }
            }
        }
        return result;
    }

    /**
     * Returns the no. of errors encountered.
     *
     * @return the no. of errors
     */
    public int getErrors() {
        return errors;
    }

    /**
     * Increments the error count.
     */
    public void incErrors() {
        addErrors(1);
    }

    /**
     * Adds errors.
     *
     * @param errors the no. of errors
     */
    public void addErrors(int errors) {
        this.errors += errors;
    }

    /**
     * Returns the no. of cancelled reminder items.
     *
     * @return the no. of cancelled reminder items
     */
    public int getCancelled() {
        return cancelled;
    }

    /**
     * Adds to the no. of cancelled reminder items.
     *
     * @param cancelled the no. of cancelled reminder items
     */
    public void addCancelled(int cancelled) {
        this.cancelled += cancelled;
    }

    /**
     * Clears the statistics.
     */
    public void clear() {
        statistics.clear();
        errors = 0;
    }
}