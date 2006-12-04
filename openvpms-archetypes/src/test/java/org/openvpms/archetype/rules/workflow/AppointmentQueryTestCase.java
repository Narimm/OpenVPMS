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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.workflow;

import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.ObjectSet;

import java.util.Date;


/**
 * Tests the {@link AppointmentQuery} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class AppointmentQueryTestCase extends ArchetypeServiceTest {

    public void testQuery() {
        final int count = 10;
        Party schedule = AppointmentTestHelper.createSchedule();
        Date from = new Date();
        for (int i = 0; i < count; ++i) {
            Date startTime = new Date();
            Date endTime = new Date();
            Act appointment = AppointmentTestHelper.createAppointment(startTime,
                                                                      endTime,
                                                                      schedule);
            save(appointment);
        }
        Date to = new Date();

        AppointmentQuery query = new AppointmentQuery();
        query.setSchedule(schedule);
        query.setDateRange(from, to);
        IPage<ObjectSet> page = query.query();
        assertNotNull(page);
        assertEquals(count, page.getResults().size());
    }
}
