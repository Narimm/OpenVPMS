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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.checkin;

import org.junit.Test;
import org.openvpms.archetype.rules.workflow.ScheduleTestHelper;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.query.EntityQuery;
import org.openvpms.web.test.AbstractAppTest;

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
        Party schedule = createSchedule();
        Party workList1 = createWorkList();
        Party workList2 = createWorkList();
        Party workList3 = createWorkList();

        EntityBean bean = new EntityBean(schedule);
        bean.setValue("useAllWorkLists", false);
        bean.addNodeRelationship("workLists", workList1);
        bean.addNodeRelationship("workLists", workList2);
        save(schedule, workList1, workList2);

        ScheduleWorkListQuery query = new ScheduleWorkListQuery(schedule, null);
        EntityQuery adapter = new EntityQuery(query, new LocalContext());

        checkSelects(true, adapter, workList1);
        checkSelects(true, adapter, workList2);
        checkSelects(false, adapter, workList3);

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

        EntityBean bean = new EntityBean(location);
        bean.addNodeRelationship("workListViews", workListView);
        save(location, workListView);

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
        Party schedule = createSchedule();
        Party location = createLocation();
        Party workList1 = createWorkList();
        Party workList2 = createWorkList();
        Party workList3 = createWorkList();

        EntityBean scheduleBean = new EntityBean(schedule);
        scheduleBean.setValue("useAllWorkLists", false);
        scheduleBean.addNodeRelationship("workLists", workList1);
        scheduleBean.addNodeRelationship("workLists", workList2);

        Entity workListView = ScheduleTestHelper.createWorkListView(workList1, workList3);

        EntityBean locationBean = new EntityBean(location);
        locationBean.addNodeRelationship("workListViews", workListView);

        save(schedule, location, workListView, workList1, workList2);

        ScheduleWorkListQuery query = new ScheduleWorkListQuery(schedule, location);
        EntityQuery adapter = new EntityQuery(query, new LocalContext());

        checkSelects(true, adapter, workList1);
        checkSelects(true, adapter, workList2);
        checkSelects(false, adapter, workList3);

        scheduleBean.setValue("useAllWorkLists", true); // use the work lists linked to the location
        checkSelects(true, adapter, workList1);
        checkSelects(false, adapter, workList2);
        checkSelects(true, adapter, workList3);
    }

}
