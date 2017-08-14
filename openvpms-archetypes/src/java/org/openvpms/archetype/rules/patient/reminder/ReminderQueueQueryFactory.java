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

import org.openvpms.component.system.common.query.ArchetypeQuery;

import java.util.Date;

import static org.openvpms.archetype.rules.patient.reminder.ReminderItemStatus.ERROR;
import static org.openvpms.archetype.rules.patient.reminder.ReminderItemStatus.PENDING;
import static org.openvpms.component.system.common.query.Constraints.eq;
import static org.openvpms.component.system.common.query.Constraints.idEq;
import static org.openvpms.component.system.common.query.Constraints.in;
import static org.openvpms.component.system.common.query.Constraints.join;
import static org.openvpms.component.system.common.query.Constraints.lt;
import static org.openvpms.component.system.common.query.Constraints.notExists;
import static org.openvpms.component.system.common.query.Constraints.shortName;
import static org.openvpms.component.system.common.query.Constraints.sort;

/**
 * A factory for queries that return reminders for queuing.
 *
 * @author Tim Anderson
 */
public class ReminderQueueQueryFactory {

    /**
     * Creates a new query.
     * <p>
     * This returns all {@code IN_PROGRESS} reminders with a {@code startTime} less than the specified date,
     * and have no items that have {@code PENDING} or {@code ERROR} status.
     *
     * @param date the date
     * @return a new query
     */
    public ArchetypeQuery createQuery(Date date) {
        ArchetypeQuery query = new ArchetypeQuery(shortName("r1", ReminderArchetypes.REMINDER));
        query.add(join("patient", "p").add(join("entity", "patient")));
        query.add(eq("status", ReminderStatus.IN_PROGRESS));
        query.add(lt("startTime", date));
        query.add(sort("id"));
        ArchetypeQuery items = new ArchetypeQuery(shortName("items", ReminderArchetypes.REMINDER_ITEMS));
        items.add(join("reminder").add(join("source", "r2")));
        items.add(idEq("r1", "r2"));
        items.add(in("status", PENDING, ERROR));
        query.add(notExists(items));
        return query;
    }
}
