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

package org.openvpms.web.workspace.workflow.appointment;

import org.junit.Test;
import org.mockito.Mockito;
import org.openvpms.archetype.rules.workflow.ScheduleTestHelper;
import org.openvpms.archetype.rules.workflow.WorkflowStatus;
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
 * Tests the {@link AppointmentCRUDWindow}.
 *
 * @author Tim Anderson
 */
public class AppointmentCRUDWindowTestCase extends AbstractAppTest {

    /**
     * Verifies that selecting an appointment updates the context.
     * <p/>
     * This allows the appointment to be made available for macros (e.g. to SMS confirmation to the customer).
     */
    @Test
    public void testUpdateContext() {
        GlobalContext context = ContextApplicationInstance.getInstance().getContext();
        AppointmentCRUDWindow window = new AppointmentCRUDWindow(Mockito.mock(AppointmentBrowser.class),
                                                                 context, new HelpContext("foo", null));

        assertNull(context.getAppointment());
        Party location = TestHelper.createLocation();
        Party schedule = ScheduleTestHelper.createSchedule(location);
        org.openvpms.component.business.domain.im.party.Party customer = TestHelper.createCustomer();
        Act appointment = ScheduleTestHelper.createAppointment(new Date(), schedule,
                                                               customer, null, WorkflowStatus.PENDING);
        window.setObject(appointment);

        assertEquals(appointment, context.getAppointment());

        window.setObject(null);
        assertNull(context.getAppointment());
    }
}
