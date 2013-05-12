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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.app.workflow.appointment;

import org.junit.Test;
import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.archetype.rules.workflow.ScheduleTestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.test.AbstractAppTest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * Tests the {@link AppointmentTypeParticipationEditor}.
 *
 * @author Tim Anderson
 */
public class AppointmentTypeParticipationEditorTestCase extends AbstractAppTest {

    /**
     * Tests validation.
     */
    @Test
    public void testValidate() {
        Act appointment = (Act) create(ScheduleArchetypes.APPOINTMENT);
        Participation type = (Participation) create(ScheduleArchetypes.APPOINTMENT_TYPE_PARTICIPATION);
        LayoutContext context = new DefaultLayoutContext(new LocalContext(), new HelpContext("foo", null));
        AppointmentTypeParticipationEditor editor = new AppointmentTypeParticipationEditor(type, appointment, context);
        assertFalse(editor.isValid());

        Entity appointmentType1 = ScheduleTestHelper.createAppointmentType();
        Entity appointmentType2 = ScheduleTestHelper.createAppointmentType();
        Entity schedule1 = ScheduleTestHelper.createSchedule(15, "MINUTES", 2, appointmentType1);
        Entity schedule2 = ScheduleTestHelper.createSchedule(15, "MINUTES", 2, appointmentType2);

        editor.setSchedule(schedule1);
        assertFalse(editor.isValid());      // need the appointment type to be valid
        editor.setEntity(appointmentType1);
        assertTrue(editor.isValid());

        editor.setEntity(appointmentType2); // appointmentType2 not associated with schedule1
        assertFalse(editor.isValid());

        editor.setSchedule(schedule2);
        assertTrue(editor.isValid());

        editor.setEntity(appointmentType1); // appointmentType2 not asasociated with schedule2
        assertFalse(editor.isValid());

        editor.setSchedule(null);
        assertTrue(editor.isValid());       // no schedule, so appointment types can be anything

        editor.setEntity(null);             // appointment type is required
        assertFalse(editor.isValid());
    }
}
