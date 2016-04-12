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

package org.openvpms.web.workspace.workflow.checkout;

import org.openvpms.archetype.rules.patient.PatientTestHelper;
import org.openvpms.archetype.rules.workflow.ScheduleTestHelper;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;

/**
 * Helper for boarding test cases.
 *
 * @author Tim Anderson
 */
public class BoardingTestHelper {

    /**
     * Creates a new visit. The appointment and event start and end times will be identical.
     *
     * @param startTime the start time
     * @param endTime   the end time
     * @param schedule  the schedule
     * @param customer  the customer
     * @param patient   the patient
     * @param visits    the visits
     * @return a new visit
     */
    public static Visit createVisit(String startTime, String endTime, Entity schedule, Party customer, Party patient,
                                    Visits visits) {
        Act appointment = createAppointment(startTime, endTime, schedule, customer, patient);
        Act event = createEvent(appointment, patient);
        return visits.create(event, appointment);
    }

    /**
     * Creates a new appointment.
     *
     * @param startTime the appointment start time
     * @param endTime   the appointment end time
     * @param schedule  the schedule
     * @param customer  the customer
     * @param patient   the patient
     * @return a new appointment
     */
    public static Act createAppointment(String startTime, String endTime, Entity schedule, Party customer,
                                        Party patient) {
        return ScheduleTestHelper.createAppointment(TestHelper.getDatetime(startTime), TestHelper.getDatetime(endTime),
                                                    schedule, customer, patient);

    }

    /**
     * Creates a new event linked to an appointment. The event start and end times will be the same as the appointment.
     *
     * @param appointment the appointment
     * @param patient     the patient
     * @return a new event
     */
    public static Act createEvent(Act appointment, Party patient) {
        Act event = PatientTestHelper.createEvent(appointment.getActivityStartTime(), appointment.getActivityEndTime(),
                                                  patient, null);
        ActBean bean = new ActBean(appointment);
        bean.addNodeRelationship("event", event);
        TestHelper.save(appointment, event);
        return event;
    }
}
