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

package org.openvpms.web.workspace.workflow.appointment;

import org.junit.Test;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.workflow.ScheduleTestHelper;
import org.openvpms.archetype.rules.workflow.Times;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.test.AbstractAppTest;
import org.openvpms.web.workspace.workflow.appointment.repeat.AppointmentSeries;
import org.openvpms.web.workspace.workflow.appointment.repeat.Repeats;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link AppointmentEditor}.
 *
 * @author Tim Anderson
 */
public class AppointmentEditorTestCase extends AbstractAppTest {

    /**
     * Tests the {@link AppointmentEditor#newInstance()} method.
     */
    @Test
    public void testNewInstance() {
        Date start = DateRules.getToday();
        Date end = DateRules.getTomorrow();
        Party schedule = ScheduleTestHelper.createSchedule(TestHelper.createLocation());
        Act appointment = ScheduleTestHelper.createAppointment(start, end, schedule);
        LayoutContext context = new DefaultLayoutContext(new LocalContext(), new HelpContext("foo", null));
        AppointmentEditor editor = new AppointmentEditor(appointment, null, context);

        IMObjectEditor newInstance = editor.newInstance();
        assertTrue(newInstance instanceof AppointmentEditor);
    }

    /**
     * Tests the {@link AppointmentEditor#getEventTimes()} method.
     */
    @Test
    public void testGetEventTimes() {
        Date start = DateRules.getToday();
        Date end = DateRules.getTomorrow();
        Party schedule = ScheduleTestHelper.createSchedule(TestHelper.createLocation());
        Act appointment = ScheduleTestHelper.createAppointment(start, end, schedule);

        // create a series of 3 appointments
        AppointmentSeries series = new AppointmentSeries(appointment, getArchetypeService());
        series.setExpression(Repeats.daily());
        series.setCondition(Repeats.twice());
        series.save();

        // create an editor to edit the series
        LayoutContext context = new DefaultLayoutContext(new LocalContext(), new HelpContext("foo", null));
        AppointmentEditor editor1 = new AppointmentEditor(appointment, null, true, context);

        // verify that there are 3 appointments in the series
        List<Times> times1 = editor1.getEventTimes();
        assertNotNull(times1);
        assertEquals(3, times1.size());

        // now create an editor for the first appointment in the series.
        AppointmentEditor editor2 = new AppointmentEditor(appointment, null, false, context);

        // verify that getEventTimes() returns the one appointment, corresponding to that of the first appointment
        List<Times> times2 = editor2.getEventTimes();
        assertNotNull(times2);
        assertEquals(1, times2.size());
        assertEquals(appointment.getId(), times2.get(0).getId());
        assertEquals(appointment.getActivityStartTime(), times2.get(0).getStartTime());
        assertEquals(appointment.getActivityEndTime(), times2.get(0).getEndTime());
    }

}
