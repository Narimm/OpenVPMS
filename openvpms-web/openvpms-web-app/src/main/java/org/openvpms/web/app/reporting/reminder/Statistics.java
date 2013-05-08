/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.reporting.reminder;

import org.openvpms.archetype.rules.patient.reminder.ReminderEvent;
import org.openvpms.component.business.domain.im.common.Entity;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;


/**
 * Reminder statistics.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class Statistics {

    /**
     * Tracks statistics by reminder type.
     */
    private final Map<Entity, Map<ReminderEvent.Action, Integer>> statistics
        = new HashMap<Entity, Map<ReminderEvent.Action, Integer>>();

    /**
     * The no. of errors encountered.
     */
    private int errors;


    /**
     * Increments the count for a reminder.
     *
     * @param reminder the reminder event
     */
    public void increment(ReminderEvent reminder) {
        Entity reminderType = reminder.getReminderType().getEntity();
        ReminderEvent.Action action = reminder.getAction();
        Map<ReminderEvent.Action, Integer> stats = statistics.get(reminderType);
        if (stats == null) {
            stats = new HashMap<ReminderEvent.Action, Integer>();
            statistics.put(reminderType, stats);
        }
        Integer count = stats.get(action);
        if (count == null) {
            stats.put(action, 1);
        } else {
            stats.put(action, count + 1);
        }
    }

    /**
     * Returns the count for a processing type.
     *
     * @param type the processing type
     * @return the count for the processing type
     */
    public int getCount(ReminderEvent.Action type) {
        int result = 0;
        for (Map<ReminderEvent.Action, Integer> stats : statistics.values()) {
            Integer count = stats.get(type);
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
     * Returns the count for a reminder type and set of action.
     *
     * @param reminderType the reminder type
     * @param actions      the actions
     * @return the count
     */
    public int getCount(Entity reminderType, EnumSet<ReminderEvent.Action> actions) {
        int result = 0;
        Map<ReminderEvent.Action, Integer> stats = statistics.get(reminderType);
        if (stats != null) {
            for (ReminderEvent.Action action : actions) {
                Integer value = stats.get(action);
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
        ++errors;
    }

    /**
     * Clears the statistics.
     */
    public void clear() {
        statistics.clear();
        errors = 0;
    }

}