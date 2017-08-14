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
import org.openvpms.archetype.rules.patient.reminder.Reminders;

import java.util.Iterator;

/**
 * The reminder item source, used by the {@link ReminderGenerator} to generate reminders.
 *
 * @author Tim Anderson
 */
public interface ReminderItemSource {

    /**
     * Returns the reminder item archetype short names.
     *
     * @return the reminder item archetype short names
     */
    String[] getArchetypes();

    /**
     * Returns all items that match the query.
     *
     * @return all items that match the query
     */
    Iterator<ReminderEvent> all();

    /**
     * Executes the query.
     *
     * @return the items matching the query
     */
    Iterable<Reminders> query();

    /**
     * Counts the number of items matching the criteria.
     *
     * @return the number of items matching
     */
    int count();
}
