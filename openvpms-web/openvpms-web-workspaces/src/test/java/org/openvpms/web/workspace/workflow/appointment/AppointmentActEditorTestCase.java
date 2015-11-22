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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.appointment;

import org.junit.Test;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.workflow.ScheduleTestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.test.AbstractAppTest;

import java.util.Date;

import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link AppointmentActEditor}.
 *
 * @author Tim Anderson
 */
public class AppointmentActEditorTestCase extends AbstractAppTest {

    /**
     * Tests the {@link AppointmentActEditor#newInstance()} method.
     */
    @Test
    public void testNewInstance() {
        Date start = DateRules.getToday();
        Date end = DateRules.getTomorrow();
        Act appointment = ScheduleTestHelper.createAppointment(start, end, ScheduleTestHelper.createSchedule());
        LayoutContext context = new DefaultLayoutContext(new LocalContext(), new HelpContext("foo", null));
        AppointmentActEditor editor = new AppointmentActEditor(appointment, null, context);

        IMObjectEditor newInstance = editor.newInstance();
        assertTrue(newInstance instanceof AppointmentActEditor);
    }

}
