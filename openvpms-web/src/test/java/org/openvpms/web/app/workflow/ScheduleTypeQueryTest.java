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
 *
 *  $Id$
 */
package org.openvpms.web.app.workflow;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.im.query.AbstractEntityQueryTest;

import java.util.List;


/**
 * Tests the {@link ScheduleTypeQuery} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class ScheduleTypeQueryTest extends AbstractEntityQueryTest<Entity> {

    /**
     * Tests limiting the query to return only those schedule types associated with a schedule.
     */
    @Test
    public void testQueryBySchedule() {
        Party schedule = createSchedule();
        Entity type1 = createObject(false);
        Entity type2 = createObject(false);
        Entity type3 = createObject(false);
        addScheduleType(schedule, type1);
        addScheduleType(schedule, type2);
        save(schedule, type1, type2, type3);

        ScheduleTypeQuery query = (ScheduleTypeQuery) createQuery();
        query.setSchedule(schedule);

        List<IMObjectReference> references = getObjectRefs(query);
        assertEquals(2, references.size());
        checkExists(type1, query, references, true);
        checkExists(type2, query, references, true);
        checkExists(type3, query, references, false);
    }

    /**
     * Creates a new schedule.
     *
     * @return a new schedule
     */
    protected abstract Party createSchedule();

    /**
     * Adds a schedule type to a schedule.
     *
     * @param schedule     the schedule
     * @param scheduleType the schedule type
     */
    protected abstract void addScheduleType(Party schedule, Entity scheduleType);

}
