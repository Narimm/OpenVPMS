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

package org.openvpms.web.component.im.sms;

import org.junit.Test;
import org.openvpms.archetype.rules.workflow.ScheduleTestHelper;
import org.openvpms.archetype.rules.workflow.WorkflowStatus;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.entity.Entity;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.macro.MacroVariables;
import org.openvpms.web.test.AbstractAppTest;

import java.util.Collections;
import java.util.Date;

import static org.junit.Assert.assertEquals;

/**
 * Tests the {@link SMSEditor}.
 *
 * @author Tim Anderson
 */
public class SMSEditorTestCase extends AbstractAppTest {

    /**
     * Verifies that macros are expanded.
     */
    @Test
    public void testMacroExpansion() {
        Party location = TestHelper.createLocation();
        location.addContact(TestHelper.createPhoneContact("", "912345678"));

        Party customer = TestHelper.createCustomer();
        Party patient = TestHelper.createPatient("Fido", customer, true);
        Entity schedule = ScheduleTestHelper.createSchedule(location);
        Date start = TestHelper.getDatetime("2019-02-22 09:00:00");
        Act appointment = ScheduleTestHelper.createAppointment(start, schedule, customer, patient,
                                                               WorkflowStatus.PENDING);
        Context context = new LocalContext();
        context.setLocation(location);
        context.setAppointment(appointment);

        Lookup lookup = TestHelper.getLookup("lookup.macro", "@testconfappointment", false);
        String expression = "concat(expr:if(boolean($appointment.patient), " +
                            "concat($appointment.patient.entity.name, \"'s\"), 'Your'), ' appointment at ', " +
                            "$appointment.schedule.entity.location.target.name, ' is confirmed for ', " +
                            "date:format($appointment.startTime, 'dd/MM/yy'), ' @ ', " +
                            "date:format($appointment.startTime, 'hh:mm'), $nl, 'Call us on ', " +
                            "party:getTelephone($location), ' if you need to change the appointment')";
        IMObjectBean bean = getBean(lookup);
        bean.setValue("expression", expression);
        bean.save();

        MacroVariables variables = new MacroVariables(context, getArchetypeService(), getLookupService());
        SMSEditor editor = new SMSEditor(Collections.emptyList(), variables, context);
        editor.setMessage("@testconfappointment");
        assertEquals("Fido's appointment at XLocation is confirmed for 22/02/19 @ 09:00\n" +
                     "Call us on 912345678 if you need to change the appointment", editor.getMessage());

        // verify the alternate text is used when the patient is not present
        IMObjectBean appointmentBean = getBean(appointment);
        appointmentBean.removeValues("patient");
        editor.setMessage("@testconfappointment");
        assertEquals("Your appointment at XLocation is confirmed for 22/02/19 @ 09:00\n" +
                     "Call us on 912345678 if you need to change the appointment", editor.getMessage());
    }
}
