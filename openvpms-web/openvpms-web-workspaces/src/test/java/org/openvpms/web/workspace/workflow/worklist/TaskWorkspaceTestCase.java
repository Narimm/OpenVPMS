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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.worklist;

import org.junit.Test;
import org.openvpms.archetype.rules.prefs.PreferenceService;
import org.openvpms.archetype.rules.workflow.ScheduleTestHelper;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.app.ContextApplicationInstance;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.test.AbstractAppTest;
import org.openvpms.web.workspace.workflow.appointment.AppointmentWorkspace;

import static org.junit.Assert.assertEquals;


/**
 * Tests the {@link AppointmentWorkspace}.
 *
 * @author Tim Anderson
 */
public class TaskWorkspaceTestCase extends AbstractAppTest {

    /**
     * Verifies that the work list view changes when the location changes.
     */
    @Test
    public void testChangeLocation() {
        Party location1 = TestHelper.createLocation();
        Party workList1 = ScheduleTestHelper.createWorkList();
        Entity workListView1 = ScheduleTestHelper.createWorkListView(workList1);
        ScheduleTestHelper.addWorkListView(location1, workListView1, true);

        Party location2 = TestHelper.createLocation();
        Party workList2 = ScheduleTestHelper.createWorkList();
        Entity workListView2 = ScheduleTestHelper.createWorkListView(workList2);
        ScheduleTestHelper.addWorkListView(location2, workListView2, true);

        ContextApplicationInstance app = ContextApplicationInstance.getInstance();
        GlobalContext context = app.getContext();
        context.setLocation(location1);

        TaskWorkspace workspace = new TaskWorkspace(context, app.getPreferences());
        workspace.getComponent();
        workspace.show();
        assertEquals(workspace.getObject(), workListView1);

        context.setLocation(location2);
        assertEquals(workspace.getObject(), workListView2);

        // simulate switching to a different workspace
        workspace.hide();

        // change the location. When the workspace is displayed again, the view should update
        context.setLocation(location1);
        workspace.getComponent();
        workspace.show();
        assertEquals(workspace.getObject(), workListView1);
    }

    /**
     * Returns the preference service.
     *
     * @return the preference service
     */
    @Override
    protected PreferenceService getPreferenceService() {
        return applicationContext.getBean(PreferenceService.class);
    }
}
