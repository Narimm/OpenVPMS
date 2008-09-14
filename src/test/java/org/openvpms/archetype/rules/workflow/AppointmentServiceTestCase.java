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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.workflow;

import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.query.ObjectSet;

import java.util.Date;
import java.util.List;


/**
 * Tests the {@link AppointmentService} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class AppointmentServiceTestCase extends ArchetypeServiceTest {

    /**
     * Tests the {@link AppointmentQuery#query()} method, when a
     * schedule and date range have been specified.
     */
    public void test() {
        final int count = 10;
        Party schedule = AppointmentTestHelper.createSchedule();
        Act[] appointments = new Act[count];
        Date[] startTimes = new Date[count];
        Date[] endTimes = new Date[count];
        Date[] arrivalTimes = new Date[count];
        Party[] customers = new Party[count];
        Party[] patients = new Party[count];
        User[] clinicians = new User[count];
        Date date = java.sql.Date.valueOf("2007-1-1");
        Entity[] appointmentTypes = new Entity[count];
        for (int i = 0; i < count; ++i) {
            Date startTime = DateRules.getDate(date, 15 * count,
                                               DateUnits.MINUTES);
            Date endTime = DateRules.getDate(startTime, 15, DateUnits.MINUTES);
            Date arrivalTime = (i % 2 == 0) ? new Date() : null;
            Party customer = TestHelper.createCustomer();
            Party patient = TestHelper.createPatient();
            User clinician = TestHelper.createClinician();
            Act appointment = AppointmentTestHelper.createAppointment(
                    startTime, endTime, schedule, customer, patient);
            ActBean bean = new ActBean(appointment);
            bean.addParticipation("participation.clinician", clinician);
            bean.setValue("arrivalTime", arrivalTime);
            appointments[i] = appointment;
            startTimes[i] = startTime;
            arrivalTimes[i] = arrivalTime;
            endTimes[i] = endTime;
            appointmentTypes[i] = bean.getNodeParticipant("appointmentType");
            customers[i] = customer;
            patients[i] = patient;
            clinicians[i] = clinician;
            bean.save();
        }

        AppointmentService service
                = (AppointmentService) applicationContext.getBean(
                "appointmentService");
        List<ObjectSet> results = service.getAppointments(schedule, date);
        assertEquals(count, results.size());
        for (int i = 0; i < results.size(); ++i) {
            ObjectSet set = results.get(i);
            assertEquals(appointments[i].getObjectReference(),
                         set.get(Appointment.ACT_REFERENCE));
            assertEquals(startTimes[i],
                         set.get(Appointment.ACT_START_TIME));
            assertEquals(endTimes[i], set.get(Appointment.ACT_END_TIME));
            assertEquals(appointments[i].getStatus(),
                         set.get(Appointment.ACT_STATUS));
            assertEquals(appointments[i].getReason(),
                         set.get(Appointment.ACT_REASON));
            assertEquals(appointments[i].getDescription(),
                         set.get(Appointment.ACT_DESCRIPTION));
            assertEquals(customers[i].getObjectReference(),
                         set.get(Appointment.CUSTOMER_REFERENCE));
            assertEquals(customers[i].getName(),
                         set.get(Appointment.CUSTOMER_NAME));
            assertEquals(patients[i].getObjectReference(),
                         set.get(Appointment.PATIENT_REFERENCE));
            assertEquals(patients[i].getName(),
                         set.get(Appointment.PATIENT_NAME));
            assertEquals(clinicians[i].getObjectReference(),
                         set.get(Appointment.CLINICIAN_REFERENCE));
            assertEquals(clinicians[i].getName(),
                         set.get(Appointment.CLINICIAN_NAME));
            assertEquals(appointmentTypes[i].getObjectReference(),
                         set.get(Appointment.APPOINTMENT_TYPE_REFERENCE));
            assertEquals(appointmentTypes[i].getName(),
                         set.get(Appointment.APPOINTMENT_TYPE_NAME));
            assertEquals(arrivalTimes[i],
                         set.get(Appointment.ARRIVAL_TIME));
        }
    }

}
