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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.worklist;

import org.junit.Test;
import org.openvpms.archetype.rules.workflow.ScheduleTestHelper;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.query.EntityQuery;
import org.openvpms.web.test.AbstractAppTest;

import static org.openvpms.archetype.rules.workflow.ScheduleTestHelper.addWorkLists;
import static org.openvpms.archetype.rules.workflow.ScheduleTestHelper.createSchedule;
import static org.openvpms.archetype.rules.workflow.ScheduleTestHelper.createWorkList;
import static org.openvpms.archetype.test.TestHelper.createLocation;
import static org.openvpms.web.component.im.query.QueryTestHelper.checkSelects;

/**
 * Tests the {@link ScheduleWorkListQuery}.
 *
 * @author Tim Anderson
 */
public class ScheduleWorklistQueryTestCase extends AbstractAppTest {

    /**
     * Tests querying work lists, when no schedule or location is supplied.
     */
    @Test
    public void testQueryAll() {
        Party workList1 = createWorkList();
        Party workList2 = createWorkList();
        ScheduleWorkListQuery query = new ScheduleWorkListQuery(null, null);
        EntityQuery adapter = new EntityQuery(query, new LocalContext());
        checkSelects(true, adapter, workList1);
        checkSelects(true, adapter, workList2);
    }


    /**
     * Tests querying work lists, when only a schedule is supplied.
     */
    @Test
    public void testQueryBySchedule() {
        Party schedule = createSchedule(TestHelper.createLocation());
        Party workList1 = createWorkList();
        Party workList2 = createWorkList();
        Party workList3 = createWorkList();

        addWorkLists(schedule, workList1, workList2);

        ScheduleWorkListQuery query = new ScheduleWorkListQuery(schedule, null);
        EntityQuery adapter = new EntityQuery(query, new LocalContext());

        checkSelects(true, adapter, workList1);
        checkSelects(true, adapter, workList2);
        checkSelects(false, adapter, workList3);

        IMObjectBean bean = getBean(schedule);
        bean.setValue("useAllWorkLists", true);
        checkSelects(true, adapter, workList1);
        checkSelects(true, adapter, workList2);
        checkSelects(true, adapter, workList3);
    }

    /**
     * Tests querying work lists, when only a location is supplied.
     */
    @Test
    public void testQueryByLocation() {
        Party location = createLocation();
        Party workList1 = createWorkList();
        Party workList2 = createWorkList();
        Party workList3 = createWorkList();
        Entity workListView = ScheduleTestHelper.createWorkListView(workList1, workList2);

        ScheduleTestHelper.addWorkListView(location, workListView, true);

        ScheduleWorkListQuery query = new ScheduleWorkListQuery(null, location);
        EntityQuery adapter = new EntityQuery(query, new LocalContext());

        checkSelects(true, adapter, workList1);
        checkSelects(true, adapter, workList2);
        checkSelects(false, adapter, workList3);
    }

    /**
     * Tests querying work lists, when both a schedule and a location is supplied.
     */
    @Test
    public void testQueryByScheduleAndLocation() {
        Party location = createLocation();
        Entity schedule = createSchedule(location);
        Entity workList1 = createWorkList();
        Entity workList2 = createWorkList();
        Entity workList3 = createWorkList();

        addWorkLists(schedule, workList1, workList2);

        Entity workListView = ScheduleTestHelper.createWorkListView(workList1, workList3);
        ScheduleTestHelper.addWorkListView(location, workListView, true);

        ScheduleWorkListQuery query = new ScheduleWorkListQuery(schedule, location);
        EntityQuery adapter = new EntityQuery(query, new LocalContext());

        checkSelects(true, adapter, workList1);
        checkSelects(true, adapter, workList2);
        checkSelects(false, adapter, workList3);

        IMObjectBean bean = getBean(schedule);
        bean.setValue("useAllWorkLists", true); // use the work lists linked to the location
        checkSelects(true, adapter, workList1);
        checkSelects(false, adapter, workList2);
        checkSelects(true, adapter, workList3);
    }

}
