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
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.model.party.Party;
import org.openvpms.web.component.app.ContextApplicationInstance;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.test.AbstractAppTest;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Tests the {@link TaskCRUDWindow}.
 *
 * @author Tim Anderson
 */
public class TaskCRUDWindowTestCase extends AbstractAppTest {

    /**
     * Verifies that selecting a task updates the context.
     * <p/>
     * This allows the task to be made available for macros (e.g. to SMS to the customer).
     */
    @Test
    public void testUpdateContext() {
        GlobalContext context = ContextApplicationInstance.getInstance().getContext();
        TaskCRUDWindow window = new TaskCRUDWindow(context, new HelpContext("foo", null));

        assertNull(context.getTask());
        Party worklist = ScheduleTestHelper.createWorkList();
        Party customer = TestHelper.createCustomer();
        Act task = ScheduleTestHelper.createTask(new Date(), null, worklist, customer, null);
        window.setObject(task);

        assertEquals(task, context.getTask());

        window.setObject(null);
        assertNull(context.getTask());
    }
}
