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

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.rules.workflow.ScheduleTestHelper;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.workspace.workflow.appointment.repeat.AppointmentSeries;
import org.openvpms.web.workspace.workflow.appointment.repeat.CalendarEventSeries;
import org.openvpms.web.workspace.workflow.appointment.repeat.CalendarEventSeriesTest;
import org.openvpms.web.workspace.workflow.appointment.repeat.Repeats;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.openvpms.web.workspace.workflow.appointment.repeat.Repeats.monthly;
import static org.openvpms.web.workspace.workflow.appointment.repeat.Repeats.times;

/**
 * Tests the {@link AppointmentSeries} class.
 *
 * @author Tim Anderson
 */
public class AppointmentSeriesTestCase extends CalendarEventSeriesTest {

    /**
     * The customer.
     */
    private Party customer;

    /**
     * The patient.
     */
    private Party patient;

    /**
     * The clinician.
     */
    private User clinician;

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        super.setUp();
        customer = TestHelper.createCustomer();
        patient = TestHelper.createPatient();
        clinician = TestHelper.createClinician();
    }

    /**
     * Verifies that the appointment type can be updated.
     */
    @Test
    public void testChangeAppointmentType() {
        Entity appointmentType2 = ScheduleTestHelper.createAppointmentType();
        Entity schedule = getSchedule();
        ScheduleTestHelper.addAppointmentType(schedule, appointmentType2, 1, false);
        save(schedule, appointmentType2);

        Act appointment = createEvent();

        CalendarEventSeries series = createSeries(appointment, Repeats.weekly(), times(2));
        checkSeries(series, appointment, 1, DateUnits.WEEKS, 3);

        ActBean bean = new ActBean(appointment);
        bean.setNodeParticipant("appointmentType", appointmentType2);

        checkSave(series);
        assertEquals(appointmentType2, bean.getNodeParticipant("appointmentType"));
        checkSeries(series, appointment, 1, DateUnits.WEEKS, 3);
    }

    /**
     * Verifies that the customer can be updated.
     */
    @Test
    public void testChangeCustomer() {
        Act appointment = createEvent();

        CalendarEventSeries series = createSeries(appointment, monthly(), times(2));
        checkSeries(series, appointment, 1, DateUnits.MONTHS, 3);

        Party customer2 = TestHelper.createCustomer();
        ActBean bean = new ActBean(appointment);
        bean.setNodeParticipant("customer", customer2);

        checkSave(series);
        assertEquals(customer2, bean.getNodeParticipant("customer"));
        checkSeries(series, appointment, 1, DateUnits.MONTHS, 3);
    }

    /**
     * Verifies that the patient can be updated.
     */
    @Test
    public void testChangePatient() {
        Act appointment = createEvent();

        CalendarEventSeries series = createSeries(appointment, monthly(), times(2));
        checkSeries(series, appointment, 1, DateUnits.MONTHS, 3);

        ActBean bean = new ActBean(appointment);
        bean.setNodeParticipant("patient", patient);

        checkSave(series);
        assertEquals(patient, bean.getNodeParticipant("patient"));
        checkSeries(series, appointment, 1, DateUnits.MONTHS, 3);
    }

    /**
     * Verifies that the clinician can be updated.
     */
    @Test
    public void testChangeClinician() {
        Act appointment = createEvent();

        CalendarEventSeries series = createSeries(appointment, monthly(), times(2));
        checkSeries(series, appointment, 1, DateUnits.MONTHS, 3);

        User clinician2 = TestHelper.createClinician();
        ActBean bean = new ActBean(appointment);
        bean.setNodeParticipant("clinician", clinician2);

        checkSave(series);
        assertEquals(clinician2, bean.getNodeParticipant("clinician"));
        checkSeries(series, appointment, 1, DateUnits.MONTHS, 3);
    }

    /**
     * Verifies that the status can be updated.
     */
    @Test
    public void testChangeStatus() {
        Act appointment = createEvent();

        CalendarEventSeries series = createSeries(appointment, monthly(), times(2));
        checkSeries(series, appointment, 1, DateUnits.MONTHS, 3);

        appointment.setStatus(ActStatus.COMPLETED);

        checkSave(series);
        assertEquals(ActStatus.COMPLETED, appointment.getStatus());
        checkSeries(series, appointment, 1, DateUnits.MONTHS, 3);
    }

    /**
     * Checks a series that was generated using calendar intervals.
     *
     * @param series   the series
     * @param event    the initial event
     * @param interval the interval
     * @param units    the interval units
     * @param count    the expected no. of events in the series
     * @param author   the expected author
     * @return the events
     */
    @Override
    protected List<Act> checkSeries(CalendarEventSeries series, Act event, int interval, DateUnits units, int count,
                                    User author) {
        List<Act> acts = series.getEvents();
        assertEquals(count, acts.size());
        Date from = event.getActivityStartTime();
        Date to = event.getActivityEndTime();
        assertEquals(event, acts.get(0));
        ActBean bean = new ActBean(event);
        Entity schedule = bean.getNodeParticipant("schedule");
        Entity appointmentType = bean.getNodeParticipant("appointmentType");
        String status = event.getStatus();
        Party customer = (Party) bean.getNodeParticipant("customer");
        Party patient = (Party) bean.getNodeParticipant("patient");
        User clinician = (User) bean.getNodeParticipant("clinician");
        for (Act act : acts) {
            if (act.equals(event)) {
                User appointmentAuthor = (User) bean.getNodeParticipant("author");
                checkAppointment(act, from, to, schedule, appointmentType, status, customer, patient, clinician,
                                 appointmentAuthor);
            } else {
                checkAppointment(act, from, to, schedule, appointmentType, status, customer, patient, clinician, author);
            }
            from = DateRules.getDate(from, interval, units);
            to = DateRules.getDate(to, interval, units);
        }
        return acts;
    }

    /**
     * Creates a new calendar event.
     *
     * @param startTime       the event start time
     * @param endTime         the event end time
     * @param schedule        the schedule
     * @param appointmentType the appointment type
     * @param author          the author. May be {@code null}  @return a new event
     */
    @Override
    protected Act createEvent(Date startTime, Date endTime, Entity schedule, Entity appointmentType, User author) {
        return ScheduleTestHelper.createAppointment(startTime, endTime, schedule, appointmentType, customer,
                                                    patient, clinician, author);
    }

    /**
     * Creates a new {@link CalendarEventSeries}.
     *
     * @param event the event
     * @return a new series
     */
    @Override
    protected CalendarEventSeries createSeries(Act event) {
        return new AppointmentSeries(event, getArchetypeService());
    }

    /**
     * Verifies an appointment matches that expected.
     *
     * @param act             the appointment
     * @param startTime       the expected start time
     * @param endTime         the expected end time
     * @param schedule        the expected schedule
     * @param appointmentType the expected appointment type
     * @param status          the expected status
     * @param customer        the expected customer
     * @param patient         the expected patient
     * @param clinician       the expected clinician
     * @param author          the expected author
     */
    private void checkAppointment(Act act, Date startTime, Date endTime, Entity schedule, Entity appointmentType,
                                  String status, Party customer, Party patient, User clinician, User author) {
        assertEquals(0, DateRules.compareTo(startTime, act.getActivityStartTime()));
        assertEquals(0, DateRules.compareTo(endTime, act.getActivityEndTime()));
        ActBean bean = new ActBean(act);
        assertEquals(schedule, bean.getNodeParticipant("schedule"));
        assertEquals(appointmentType, bean.getNodeParticipant("appointmentType"));
        assertEquals(status, act.getStatus());
        assertEquals(customer, bean.getNodeParticipant("customer"));
        assertEquals(patient, bean.getNodeParticipant("patient"));
        assertEquals(clinician, bean.getNodeParticipant("clinician"));
        assertEquals(author, bean.getNodeParticipant("author"));
    }

}
