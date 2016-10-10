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

import org.apache.commons.lang.time.DateUtils;
import org.joda.time.Period;
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

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.openvpms.web.workspace.workflow.appointment.repeat.Repeats.daily;
import static org.openvpms.web.workspace.workflow.appointment.repeat.Repeats.monthly;
import static org.openvpms.web.workspace.workflow.appointment.repeat.Repeats.times;
import static org.openvpms.web.workspace.workflow.appointment.repeat.Repeats.weekly;

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
     * Verifies that the sendReminder flag can be updated.
     */
    @Test
    public void testSendReminder() {
        Date startTime = DateRules.getDate(DateRules.getYesterday(), 9, DateUnits.HOURS);
        Date endTime = DateRules.getDate(startTime, 15, DateUnits.MINUTES);
        Act appointment1 = createEvent(startTime, endTime);

        // Set the first appointment to send reminders. Note that this is not possible via the appointment editor as it
        // is within no reminder period
        setSendReminder(appointment1, true);

        // verify sendReminder is propagated to the series
        CalendarEventSeries series1 = createSeries(appointment1, weekly(), times(2));
        List<Act> acts1 = checkSeries(series1, appointment1, 1, DateUnits.WEEKS, 3);
        checkSendReminder(acts1.get(0), true);
        checkSendReminder(acts1.get(1), true);
        checkSendReminder(acts1.get(2), true);

        // now change the second appointment in the series and turn off sendReminder. This should be propagated
        // to the last appointment, but not the first
        Act appointment2 = acts1.get(1);
        CalendarEventSeries series2 = createSeries(appointment2);
        setSendReminder(appointment2, false);
        assertTrue(series2.isModified());
        save(appointment2);
        series2.save();
        List<Act> acts2 = checkSeries(series2, appointment1, 1, DateUnits.WEEKS, 3);
        checkSendReminder(acts2.get(0), true);
        checkSendReminder(acts2.get(1), false);
        checkSendReminder(acts2.get(2), false);
    }

    /**
     * Verifies that sendReminder=true is not initially propagated to acts within the no reminder period.
     * NOTE: AppointmentSeries determines sendReminder on the current date/time
     */
    @Test
    public void testSendReminderNotEnabledWithinNoReminderPeriod() {
        Date startTime = DateUtils.truncate(new Date(), Calendar.SECOND); // truncate to seconds as ms not stored
        Date endTime = DateRules.getDate(startTime, 15, DateUnits.MINUTES);
        Act appointment = createEvent(startTime, endTime);

        // Set the first appointment to send reminders. Note that this is not possible via the appointment editor as it
        // is within no reminder period
        setSendReminder(appointment, true);

        // verify sendReminder is propagated to the series
        CalendarEventSeries series = createSeries(appointment, daily(), times(2));
        List<Act> acts1 = checkSeries(series, appointment, 1, DateUnits.DAYS, 3);
        checkSendReminder(acts1.get(0), true);  // the original appointment
        checkSendReminder(acts1.get(1), false); // 1 day after, and within the no reminder period
        checkSendReminder(acts1.get(2), true);  // 2 days after, and outside the no reminder period

        // now turn off sendReminder, and verify it propagates to each act in the series
        setSendReminder(appointment, false);
        assertTrue(series.isModified());
        save(appointment);
        series.save();
        List<Act> acts2 = checkSeries(series, appointment, 1, DateUnits.DAYS, 3);
        checkSendReminder(acts2.get(0), false);
        checkSendReminder(acts2.get(1), false);
        checkSendReminder(acts2.get(2), false);
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
                checkAppointment(act, from, to, schedule, appointmentType, status, customer, patient, clinician,
                                 author);
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
     * Creates a new {@link AppointmentSeries}.
     * <p/>
     * This implementation sets the no reminder period to 1 day.
     *
     * @param event the event
     * @return a new series
     */
    @Override
    protected CalendarEventSeries createSeries(Act event) {
        AppointmentSeries series = new AppointmentSeries(event, getArchetypeService());
        series.setNoReminderPeriod(Period.days(1));
        return series;
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

    /**
     * Sets the {@code sendReminder} flag of an appointment.
     *
     * @param appointment  the appointment
     * @param sendReminder if {@code true}, indicates to send reminders
     */
    protected void setSendReminder(Act appointment, boolean sendReminder) {
        ActBean bean = new ActBean(appointment);
        bean.setValue("sendReminder", sendReminder);
    }

    /**
     * Verifies an appointment {@code sendReminder} flag matches that expected.
     *
     * @param appointment  the appointment
     * @param sendReminder the expected value of the {@code sendReminder} flag
     */
    private void checkSendReminder(Act appointment, boolean sendReminder) {
        ActBean bean = new ActBean(appointment);
        assertEquals(sendReminder, bean.getBoolean("sendReminder"));
    }

}
