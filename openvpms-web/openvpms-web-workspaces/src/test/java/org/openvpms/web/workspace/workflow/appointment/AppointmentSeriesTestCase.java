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

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.rules.workflow.ScheduleTestHelper;
import org.openvpms.archetype.rules.workflow.Times;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.workspace.workflow.appointment.repeat.AppointmentSeries;
import org.openvpms.web.workspace.workflow.appointment.repeat.RepeatCondition;
import org.openvpms.web.workspace.workflow.appointment.repeat.RepeatExpression;
import org.openvpms.web.workspace.workflow.appointment.repeat.Repeats;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.openvpms.web.workspace.workflow.appointment.repeat.Repeats.daily;
import static org.openvpms.web.workspace.workflow.appointment.repeat.Repeats.monthly;
import static org.openvpms.web.workspace.workflow.appointment.repeat.Repeats.once;
import static org.openvpms.web.workspace.workflow.appointment.repeat.Repeats.times;
import static org.openvpms.web.workspace.workflow.appointment.repeat.Repeats.yearly;

/**
 * Tests the {@link AppointmentSeries} class.
 *
 * @author Tim Anderson
 */
public class AppointmentSeriesTestCase extends ArchetypeServiceTest {

    /**
     * Appointment start time.
     */
    private Date startTime;

    /**
     * Appointment end time.
     */
    private Date endTime;

    /**
     * The schedule.
     */
    private Party schedule;

    /**
     * The appointment type.
     */
    private Entity appointmentType;

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
     * The author.
     */
    private User author;


    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        startTime = TestHelper.getDatetime("2015-01-01 09:30:00");
        endTime = TestHelper.getDatetime("2015-01-01 09:45:00");
        customer = TestHelper.createCustomer();
        patient = TestHelper.createPatient();
        clinician = TestHelper.createClinician();
        author = TestHelper.createUser();
        appointmentType = ScheduleTestHelper.createAppointmentType();
        schedule = ScheduleTestHelper.createSchedule(15, DateUnits.MINUTES.toString(), 1, appointmentType);
    }

    /**
     * Verifies that an appointment can be repeated daily.
     */
    @Test
    public void testRepeatDaily() {
        checkCreateSeries(Repeats.daily(), startTime, endTime, 1, DateUnits.DAYS);
    }

    /**
     * Verifies that an appointment can be repeated weekly.
     */
    @Test
    public void testRepeatWeekly() {
        checkCreateSeries(Repeats.weekly(), startTime, endTime, 1, DateUnits.WEEKS);
    }

    /**
     * Verifies that an appointment can be repeated monthly.
     */
    @Test
    public void testRepeatMonthly() {
        checkCreateSeries(monthly(), startTime, endTime, 1, DateUnits.MONTHS);
    }

    /**
     * Verifies that an appointment can be repeated yearly.
     */
    @Test
    public void testRepeatYearly() {
        checkCreateSeries(Repeats.yearly(), startTime, endTime, 1, DateUnits.YEARS);
    }

    /**
     * Verifies that changing the series expression updates the appointment times.
     */
    @Test
    public void testChangeSeriesExpression() {
        Act appointment = createAppointment(startTime, endTime);

        AppointmentSeries series = createSeries(appointment, monthly(), times(11));

        checkSeries(series, appointment, 1, DateUnits.MONTHS, 12);
        series.setExpression(Repeats.yearly());
        series.save();
        checkSeries(series, appointment, 1, DateUnits.YEARS, 12);
    }

    /**
     * Verifies that changing the series condition to fewer appointments deletes those no longer included.
     */
    @Test
    public void testChangeSeriesConditionToFewerAppointments() {
        Act appointment = createAppointment(startTime, endTime);

        AppointmentSeries series = createSeries(appointment, monthly(), times(11));
        checkSeries(series, appointment, 1, DateUnits.MONTHS, 12);
        List<Act> oldAppointments = series.getAppointments();
        assertEquals(12, oldAppointments.size());

        List<Act> toRemove = oldAppointments.subList(10, 12);

        series.setCondition(times(9));
        series.save();
        checkSeries(series, appointment, 1, DateUnits.MONTHS, 10);
        List<Act> newAppointments = series.getAppointments();
        assertEquals(10, newAppointments.size());

        for (Act act : oldAppointments) {
            if (!toRemove.contains(act)) {
                assertTrue(newAppointments.contains(act));
            } else {
                // verify it has been deleted
                assertNull(get(act));
            }
        }
    }

    /**
     * Verifies that changing the series condition to more appointments adds new appointments on save.
     */
    @Test
    public void testChangeSeriesConditionToMoreAppointments() {
        Act appointment = createAppointment(startTime, endTime);

        AppointmentSeries series = createSeries(appointment, monthly(), times(9));
        checkSeries(series, appointment, 1, DateUnits.MONTHS, 10);
        List<Act> oldAppointments = series.getAppointments();
        assertEquals(10, oldAppointments.size());

        series.setCondition(times(11));
        series.save();
        checkSeries(series, appointment, 1, DateUnits.MONTHS, 12);
        List<Act> newAppointments = series.getAppointments();
        assertEquals(12, newAppointments.size());

        // verify the original appointments have been retained
        for (Act act : oldAppointments) {
            assertTrue(newAppointments.contains(act));
        }
    }

    /**
     * Verifies that a series with overlapping appointments is detected.
     * <p/>
     * Note that this only checks overlaps with appointments in the series, not with existing appointments.
     */
    @Test
    public void testSeriesWithOverlappingAppointments() {
        Act appointment = createAppointment(startTime, DateRules.getDate(startTime, 2, DateUnits.DAYS));
        AppointmentSeries series = createSeries(appointment);
        series.setExpression(daily());  // the next appointment overlaps the previous
        series.setCondition(once());
        AppointmentSeries.Overlap overlap = series.getFirstOverlap();
        assertNotNull(overlap);
        assertEquals(overlap.getAppointment1(), Times.create(appointment));
    }

    /**
     * Verifies a series can be deleted.
     * <p/>
     * This deletes all non-expired appointments bar the current one.
     */
    @Test
    public void testDeleteSeriesWithNoExpiredAppointments() {
        Act appointment = createAppointment(startTime, endTime);
        AppointmentSeries series = createSeries(appointment, Repeats.yearly(), times(9));
        Act act = series.getSeries();
        assertNotNull(act);

        List<Act> appointments = series.getAppointments();
        assertEquals(10, appointments.size());

        series.setExpression(null);
        series.setCondition(null);
        series.save();
        appointments = series.getAppointments();
        assertEquals(0, appointments.size());
        assertNull(series.getSeries());
        assertNotNull(get(appointment));
        assertNull(get(act));
    }

    /**
     * Verifies that the schedule can be updated.
     */
    @Test
    public void testChangeSchedule() {
        Act appointment = ScheduleTestHelper.createAppointment(startTime, endTime, schedule, appointmentType, customer,
                                                               patient, clinician, author);

        AppointmentSeries series = createSeries(appointment, Repeats.yearly(), times(2));
        checkSeries(series, appointment, 1, DateUnits.YEARS, 3);

        Entity schedule2 = ScheduleTestHelper.createSchedule(15, DateUnits.MINUTES.toString(), 1, appointmentType);

        ActBean bean = new ActBean(appointment);
        bean.setNodeParticipant("schedule", schedule2);

        checkSave(series);
        assertEquals(schedule2, bean.getNodeParticipant("schedule"));
        checkSeries(series, appointment, 1, DateUnits.YEARS, 3);
    }

    /**
     * Verifies that the appointment type can be updated.
     */
    @Test
    public void testChangeAppointmentType() {
        Entity appointmentType2 = ScheduleTestHelper.createAppointmentType();
        ScheduleTestHelper.addAppointmentType(schedule, appointmentType2, 1, false);
        save(schedule, appointmentType2);

        Act appointment = ScheduleTestHelper.createAppointment(startTime, endTime, schedule, appointmentType, customer,
                                                               patient, clinician, author);

        AppointmentSeries series = createSeries(appointment, Repeats.weekly(), times(2));
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
        Act appointment = ScheduleTestHelper.createAppointment(startTime, endTime, schedule, appointmentType, customer,
                                                               null, clinician, author);

        AppointmentSeries series = createSeries(appointment, monthly(), times(2));
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
        Act appointment = ScheduleTestHelper.createAppointment(startTime, endTime, schedule, appointmentType, customer,
                                                               null, clinician, author);

        AppointmentSeries series = createSeries(appointment, monthly(), times(2));
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
        Act appointment = ScheduleTestHelper.createAppointment(startTime, endTime, schedule, appointmentType, customer,
                                                               null, clinician, author);

        AppointmentSeries series = createSeries(appointment, monthly(), times(2));
        checkSeries(series, appointment, 1, DateUnits.MONTHS, 3);

        User clinician2 = TestHelper.createClinician();
        ActBean bean = new ActBean(appointment);
        bean.setNodeParticipant("clinician", clinician2);

        checkSave(series);
        assertEquals(clinician2, bean.getNodeParticipant("clinician"));
        checkSeries(series, appointment, 1, DateUnits.MONTHS, 3);
    }

    /**
     * Verifies that the author can be updated.
     */
    @Test
    public void testCannotChangeAuthor() {
        Act appointment = ScheduleTestHelper.createAppointment(startTime, endTime, schedule, appointmentType, customer,
                                                               null, clinician, author);

        AppointmentSeries series = createSeries(appointment, monthly(), times(2));
        checkSeries(series, appointment, 1, DateUnits.MONTHS, 3);

        User author2 = TestHelper.createUser();
        ActBean bean = new ActBean(appointment);
        bean.setNodeParticipant("author", author2);

        checkSave(series);

        assertEquals(author2, bean.getNodeParticipant("author"));

        // series appointments should have the original author
        checkSeries(series, appointment, 1, DateUnits.MONTHS, 3, author);
    }

    /**
     * Verifies that changing the expression on a non-initial appointment in the series creates a new series.
     */
    @Test
    public void testNewSeriesCreatedForNonInitialAppointment() {
        Act first = createAppointment(startTime, endTime);
        AppointmentSeries series1 = createSeries(first, monthly(), times(4));
        List<Act> appointments = checkSeries(series1, first, 1, DateUnits.MONTHS, 5);

        // get the third appointment, and create a new series
        Act third = appointments.get(2);
        AppointmentSeries series2 = createSeries(third);
        RepeatCondition condition = series2.getCondition();
        assertEquals(times(2), condition);                   // times reflects the position in the series

        // change the expression
        series2.setExpression(yearly());
        checkSave(series2);
        checkSeries(series2, third, 1, DateUnits.YEARS, 3);

        // verify the original series is now shortened
        series1 = createSeries(first);
        checkSeries(series1, first, 1, DateUnits.MONTHS, 2);
    }

    /**
     * Verifies that changing the date on the first appointment moves the entire series.
     */
    @Test
    public void testChangeAppointmentDate() {
        Act appointment = createAppointment(startTime, endTime);

        AppointmentSeries series = createSeries(appointment, monthly(), times(11));

        checkSeries(series, appointment, 1, DateUnits.MONTHS, 12);

        startTime = DateRules.getDate(startTime, 1, DateUnits.WEEKS);
        endTime = DateRules.getDate(this.endTime, 1, DateUnits.WEEKS);
        appointment.setActivityStartTime(startTime);
        appointment.setActivityEndTime(endTime);

        checkSave(series);
        checkSeries(series, appointment, 1, DateUnits.MONTHS, 12);
    }

    /**
     * Creates an {@link AppointmentSeries}, and verifies the expected appointments have been created.
     *
     * @param expression the expression
     * @param startTime  the first appointment start time
     * @param endTime    the first appointment end time
     * @param interval   the interval between appointments
     * @param units      the interval units
     */
    private void checkCreateSeries(RepeatExpression expression, Date startTime, Date endTime, int interval,
                                   DateUnits units) {
        Act appointment = createAppointment(startTime, endTime);

        AppointmentSeries series = createSeries(appointment, expression, times(9));
        checkSeries(series, appointment, interval, units, 10);
        assertFalse(series.isModified());
    }

    /**
     * Checks a series that was generated using calendar intervals.
     *
     * @param series      the series
     * @param appointment the initial appointment
     * @param interval    the interval
     * @param units       the interval units
     * @param count       the expected no. of appointments int the series
     * @return the appointments
     */
    private List<Act> checkSeries(AppointmentSeries series, Act appointment, int interval, DateUnits units, int count) {
        ActBean bean = new ActBean(appointment);
        return checkSeries(series, appointment, interval, units, count, (User) bean.getNodeParticipant("author"));
    }

    /**
     * Checks a series that was generated using calendar intervals.
     *
     * @param series      the series
     * @param appointment the initial appointment
     * @param interval    the interval
     * @param units       the interval units
     * @param count       the expected no. of appointments int the series
     * @param author      the expected author
     * @return the appointments
     */
    private List<Act> checkSeries(AppointmentSeries series, Act appointment, int interval, DateUnits units, int count,
                                  User author) {
        List<Act> acts = series.getAppointments();
        assertEquals(count, acts.size());
        Date from = appointment.getActivityStartTime();
        Date to = appointment.getActivityEndTime();
        assertEquals(appointment, acts.get(0));
        ActBean bean = new ActBean(appointment);
        Entity schedule = bean.getNodeParticipant("schedule");
        Entity appointmentType = bean.getNodeParticipant("appointmentType");
        Party customer = (Party) bean.getNodeParticipant("customer");
        Party patient = (Party) bean.getNodeParticipant("patient");
        User clinician = (User) bean.getNodeParticipant("clinician");
        for (Act act : acts) {
            if (act.equals(appointment)) {
                User appointmentAuthor = (User) bean.getNodeParticipant("author");
                checkAppointment(act, from, to, schedule, appointmentType, customer, patient, clinician,
                                 appointmentAuthor);
            } else {
                checkAppointment(act, from, to, schedule, appointmentType, customer, patient, clinician, author);
            }
            from = DateRules.getDate(from, interval, units);
            to = DateRules.getDate(to, interval, units);
        }
        return acts;
    }

    /**
     * Saves an appointment series if there are no overlaps detected.
     *
     * @param series the series
     */
    private void checkSave(AppointmentSeries series) {
        assertNull(series.getFirstOverlap());
        series.save();
    }

    /**
     * Creates a new series, generating appointments.
     *
     * @param appointment the first appointment
     * @param expression  the repeat expression
     * @param condition   the repeat condition
     * @return the series
     */
    private AppointmentSeries createSeries(Act appointment, RepeatExpression expression, RepeatCondition condition) {
        AppointmentSeries series = createSeries(appointment);
        assertEquals(0, series.getAppointments().size());
        assertTrue(series.isModified());
        assertNull(series.getSeries());

        series.setExpression(expression);
        series.setCondition(condition);
        assertTrue(series.isModified());
        checkSave(series);
        assertFalse(series.isModified());
        assertNotNull(series.getSeries());
        return series;
    }

    /**
     * Creates a new {@link AppointmentSeries}.
     *
     * @param appointment the appointment
     * @return a new series
     */
    private AppointmentSeries createSeries(final Act appointment) {
        return new AppointmentSeries(appointment, getArchetypeService());
    }

    /**
     * Verifies an appointment matches that expected.
     *
     * @param act             the appointment
     * @param startTime       the expected start time
     * @param endTime         the expected end time
     * @param schedule        the expected schedule
     * @param appointmentType the expected appointment type
     * @param customer        the expected customer
     * @param patient         the expected patient
     * @param clinician       the expected clinician
     * @param author          the expected author
     */
    private void checkAppointment(Act act, Date startTime, Date endTime, Entity schedule, Entity appointmentType,
                                  Party customer, Party patient, User clinician, User author) {
        assertEquals(0, DateRules.compareTo(startTime, act.getActivityStartTime()));
        assertEquals(0, DateRules.compareTo(endTime, act.getActivityEndTime()));
        ActBean bean = new ActBean(act);
        assertEquals(schedule, bean.getNodeParticipant("schedule"));
        assertEquals(appointmentType, bean.getNodeParticipant("appointmentType"));
        assertEquals(customer, bean.getNodeParticipant("customer"));
        assertEquals(patient, bean.getNodeParticipant("patient"));
        assertEquals(clinician, bean.getNodeParticipant("clinician"));
        assertEquals(author, bean.getNodeParticipant("author"));
    }

    /**
     * Helper to create an appointment.
     *
     * @param startTime the appointment start time
     * @param endTime   the appointment end time
     * @return a new appointment
     */
    private Act createAppointment(Date startTime, Date endTime) {
        return ScheduleTestHelper.createAppointment(startTime, endTime, schedule, appointmentType, customer,
                                                    patient, clinician, author);
    }

}
