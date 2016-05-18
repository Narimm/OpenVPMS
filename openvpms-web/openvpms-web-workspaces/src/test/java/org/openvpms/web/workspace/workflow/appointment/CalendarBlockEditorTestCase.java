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
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.test.AbstractAppTest;

import java.util.Date;

import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link CalendarBlockEditor}.
 *
 * @author Tim Anderson
 */
public class CalendarBlockEditorTestCase extends AbstractAppTest {

    /**
     * Tests the {@link CalendarBlockEditor#newInstance()} method.
     */
    @Test
    public void testNewInstance() {
        Date start = DateRules.getToday();
        Date end = DateRules.getTomorrow();
        Entity schedule = ScheduleTestHelper.createSchedule(TestHelper.createLocation());
        Entity blockType = ScheduleTestHelper.createCalendarBlockType();
        Act block = ScheduleTestHelper.createCalendarBlock(start, end, schedule, blockType, null);
        LayoutContext context = new DefaultLayoutContext(new LocalContext(), new HelpContext("foo", null));
        CalendarBlockEditor editor = new CalendarBlockEditor(block, null, context);

        IMObjectEditor newInstance = editor.newInstance();
        assertTrue(newInstance instanceof CalendarBlockEditor);
    }

}
