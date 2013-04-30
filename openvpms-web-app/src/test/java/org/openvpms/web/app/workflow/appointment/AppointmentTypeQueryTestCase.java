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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.app.workflow.appointment;

import org.openvpms.archetype.rules.workflow.ScheduleTestHelper;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.app.workflow.ScheduleTypeQueryTest;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.query.Query;


/**
 * Tests the {@link AppointmentTypeQuery} class.
 *
 * @author Tim Anderson
 */
public class AppointmentTypeQueryTestCase extends ScheduleTypeQueryTest {

    /**
     * Creates a new query.
     *
     * @return a new query
     */
    protected Query<Entity> createQuery() {
        return new AppointmentTypeQuery(null, new LocalContext());
    }

    /**
     * Creates a new object, selected by the query.
     *
     * @param value a value that can be used to uniquely identify the object
     * @param save  if <tt>true</tt> save the object, otherwise don't save it
     * @return the new object
     */
    protected Entity createObject(String value, boolean save) {
        return ScheduleTestHelper.createAppointmentType(value, save);
    }

    /**
     * Generates a unique value which may be used for querying objects on.
     *
     * @return a unique value
     */
    protected String getUniqueValue() {
        return getUniqueValue("ZAppointmentType");
    }

    /**
     * Creates a new schedule.
     *
     * @return a new schedule
     */
    protected Party createSchedule() {
        return ScheduleTestHelper.createSchedule(15, "MINUTES", 2, null);
    }

    /**
     * Adds a schedule type to a schedule.
     *
     * @param schedule     the schedule
     * @param scheduleType type the schedule type
     */
    protected void addScheduleType(Party schedule, Entity scheduleType) {
        ScheduleTestHelper.addAppointmentType(schedule, scheduleType, 1, false);
    }
}
