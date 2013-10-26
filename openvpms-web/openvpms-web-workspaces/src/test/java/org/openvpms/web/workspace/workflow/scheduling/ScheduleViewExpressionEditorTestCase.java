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

package org.openvpms.web.workspace.workflow.scheduling;

import org.junit.Test;
import org.openvpms.archetype.rules.workflow.AppointmentStatus;
import org.openvpms.archetype.rules.workflow.ScheduleEvent;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.test.AbstractAppTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link ScheduleViewExpressionEditor}.
 *
 * @author Tim Anderson
 */
public class ScheduleViewExpressionEditorTestCase extends AbstractAppTest {


    /**
     * Verifies that an empty expression returns {@code null}.
     */
    @Test
    public void testNullExpression() {
        String expected = null;
        String expression = null;
        checkExpression(expected, expression, true);
        checkExpression(expected, expression, false);
    }

    /**
     * Checks evaluation of literal expressions.
     */
    @Test
    public void testLiteralExpression() {
        String expected = "abc";
        String expression = "'abc'";
        checkExpression(expected, expression, true);
        checkExpression(expected, expression, false);
    }

    /**
     * Checks evaluation of complex expressions.
     */
    @Test
    public void testComplexExpression() {
        String expected = "Customer name-Patient name";
        String expression = "concat(openvpms:get(.,'customer.name'),'-',openvpms:get(.,'patient.name'))";
        checkExpression(expected, expression, true);
        checkExpression(expected, expression, false);
    }

    /**
     * Verifies that the waiting time is available, if the appointment status is {@code CHECKED_IN}.
     */
    @Test
    public void testScheduleWaitingTime() {
        Property property = new SimpleProperty("expression", String.class);
        ScheduleViewExpressionEditor editor = new ScheduleViewExpressionEditor(property, true);
        editor.getComponent();

        property.setValue("openvpms:get(., 'waiting')");
        assertEquals("", editor.evaluate());  // must be CHECKED_IN

        setStatus(editor, AppointmentStatus.CHECKED_IN);
        assertEquals("(0:00)", editor.evaluate());

        setStatus(editor, AppointmentStatus.BILLED);
        assertEquals("", editor.evaluate());
    }

    /**
     * Verifies that the consult waiting time is available.
     *
     * @see SchedulingHelper#getWaitingTime(org.openvpms.component.system.common.util.PropertySet)
     */
    @Test
    public void testConsultWaitingTime() {
        Property property = new SimpleProperty("expression", String.class);
        ScheduleViewExpressionEditor editor = new ScheduleViewExpressionEditor(property, false);
        editor.getComponent();

        property.setValue("openvpms:get(., 'waiting')");
        assertEquals("(0:00)", editor.evaluate());
    }

    /**
     * Evaluates an expression, verifying it matches the expected result.
     *
     * @param expected     the expected result
     * @param expression   the expression. May be {@code null}
     * @param scheduleView if {@code true} the editor is for an <em>entity.organisationScheduleView</em>, else it is
     *                     for an <em>entity.organisationWorklistView</em>
     */
    private void checkExpression(String expected, String expression, boolean scheduleView) {
        Property property = new SimpleProperty("expression", String.class);
        ScheduleViewExpressionEditor editor = new ScheduleViewExpressionEditor(property, scheduleView);
        editor.getComponent();
        property.setValue(expression);
        assertEquals(expected, editor.evaluate());
    }

    /**
     * Helper to set the act status property.
     *
     * @param editor the editor
     * @param status the status to set
     */
    private void setStatus(ScheduleViewExpressionEditor editor, String status) {
        boolean found = false;
        for (Property property : editor.getProperties()) {
            if (property.getName().equals(ScheduleEvent.ACT_STATUS)) {
                found = true;
                property.setValue(status);
                break;
            }
        }
        assertTrue(found);
    }

}
