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

package org.openvpms.web.workspace.workflow.appointment;

import org.junit.Test;
import org.mockito.Mockito;
import org.openvpms.archetype.rules.practice.LocationRules;
import org.openvpms.archetype.rules.prefs.Preferences;
import org.openvpms.archetype.rules.workflow.ScheduleTestHelper;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link AppointmentSchedules} class.
 *
 * @author Tim Anderson
 */
public class AppointmentScheduleTestCase extends ArchetypeServiceTest {

    /**
     * Verify that the {@link AppointmentSchedules#getSchedules(Entity)} method excludes inactive schedules.
     */
    @Test
    public void testGetSchedulesForInactiveSchedule() {
        Party location = TestHelper.createLocation();
        Party schedule1 = ScheduleTestHelper.createSchedule(location);
        Party schedule2 = ScheduleTestHelper.createSchedule(location);
        Entity view = ScheduleTestHelper.createScheduleView(schedule1, schedule2);

        AppointmentSchedules schedules = new AppointmentSchedules(location, Mockito.mock(Preferences.class),
                                                                  applicationContext.getBean(LocationRules.class));
        List<Entity> list1 = schedules.getSchedules(view);
        assertEquals(2, list1.size());
        assertTrue(list1.contains(schedule1));
        assertTrue(list1.contains(schedule2));

        // now deactivate schedule2 and verify it is no longer returned.
        schedule2.setActive(false);
        save(schedule2);

        List<Entity> list2 = schedules.getSchedules(view);
        assertEquals(1, list2.size());
        assertTrue(list2.contains(schedule1));
        assertFalse(list2.contains(schedule2));
    }

}
