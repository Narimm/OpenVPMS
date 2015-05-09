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
import org.openvpms.archetype.rules.workflow.AppointmentRules;
import org.openvpms.archetype.rules.workflow.ScheduleTestHelper;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.rule.IArchetypeRuleService;
import org.openvpms.web.workspace.workflow.appointment.repeat.AppointmentSeries;
import org.openvpms.web.workspace.workflow.appointment.repeat.RepeatExpression;
import org.openvpms.web.workspace.workflow.appointment.repeat.RepeatExpressions;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link AppointmentSeries} class.
 *
 * @author Tim Anderson
 */
public class AppointmentSeriesTestCase extends ArchetypeServiceTest {

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
     * The appointment rules.
     */
    private AppointmentRules appointmentRules;

    /**
     * The archetype service.
     */
    private IArchetypeService service;

    /**
     * The rule-based archetype service.
     */
    private IArchetypeRuleService ruleService;

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        customer = TestHelper.createCustomer();
        patient = TestHelper.createPatient();
        clinician = TestHelper.createClinician();
        author = TestHelper.createUser();
        appointmentType = ScheduleTestHelper.createAppointmentType();
        schedule = ScheduleTestHelper.createSchedule(15, DateUnits.MINUTES.toString(), 1, appointmentType);
        appointmentRules = new AppointmentRules(getArchetypeService());
        service = applicationContext.getBean("defaultArchetypeService", IArchetypeService.class);
        ruleService = applicationContext.getBean("archetypeService", IArchetypeRuleService.class);
    }

    /**
     * Verifies that an appointment can be repeated daily.
     */
    @Test
    public void testRepeatDaily() {
        Date startTime = TestHelper.getDatetime("2015-01-01 09:30:00");
        Date endTime = TestHelper.getDatetime("2015-01-01 09:45:00");

        checkCreateSeries(RepeatExpressions.daily(), startTime, endTime, 1, DateUnits.DAYS);
    }

    /**
     * Verifies that an appointment can be repeated weekly.
     */
    @Test
    public void testRepeatWeekly() {
        Date startTime = TestHelper.getDatetime("2015-01-01 09:30:00");
        Date endTime = TestHelper.getDatetime("2015-01-01 09:45:00");

        checkCreateSeries(RepeatExpressions.weekly(), startTime, endTime, 1, DateUnits.WEEKS);
    }

    /**
     * Verifies that an appointment can be repeated monthly.
     */
    @Test
    public void testRepeatMonthly() {
        Date startTime = TestHelper.getDatetime("2015-01-01 09:30:00");
        Date endTime = TestHelper.getDatetime("2015-01-01 09:45:00");

        checkCreateSeries(RepeatExpressions.monthly(), startTime, endTime, 1, DateUnits.MONTHS);
    }

    /**
     * Verifies that an appointment can be repeated yearly.
     */
    @Test
    public void testRepeatYearly() {
        Date startTime = TestHelper.getDatetime("2015-01-01 09:30:00");
        Date endTime = TestHelper.getDatetime("2015-01-01 09:45:00");

        checkCreateSeries(RepeatExpressions.yearly(), startTime, endTime, 1, DateUnits.YEARS);
    }

    /**
     * Verifies that a series with overlapping appointments cannot be saved.
     */
    @Test
    public void testSeriesWithOverlappingAppointments() {

    }

    /**
     * Verifies a series can be deleted.
     * <p/>
     * This deletes all non-expired appointments bar the current one.
     */
    @Test
    public void testDeleteSeriesWithNoExpiredAppointments() {
        Date startTime = TestHelper.getDatetime("2015-01-01 09:30:00");
        Date endTime = TestHelper.getDatetime("2015-01-01 09:45:00");

        Act appointment = ScheduleTestHelper.createAppointment(startTime, endTime, schedule, appointmentType, customer,
                                                               patient, clinician, author);

        AppointmentSeries series = new AppointmentSeries(appointment, service, ruleService, appointmentRules, 10) {
            @Override
            protected boolean isExpired(Date startTime, Date now) {
                return false;
            }
        };
        series.setExpression(RepeatExpressions.yearly());
        assertTrue(series.save());

        List<Act> appointments = series.getAppointments();
        assertEquals(10, appointments.size());

        series.setExpression(null);
        assertTrue(series.save());
        appointments = series.getAppointments();
        assertEquals(0, appointments.size());
        assertNotNull(get(appointment));
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
        Act appointment = ScheduleTestHelper.createAppointment(startTime, endTime, schedule, appointmentType, customer,
                                                               patient, clinician, author);

        AppointmentSeries series = new AppointmentSeries(appointment, service, ruleService, appointmentRules, 10) {
            protected boolean isExpired(Date startTime, Date now) {
                return false;
            }
        };
        assertEquals(0, series.getAppointments().size());

        assertFalse(series.save());

        series.setExpression(expression);
        assertTrue(series.save());

        List<Act> acts = series.getAppointments();
        assertEquals(10, acts.size());
        Date from = startTime;
        Date to = endTime;
        assertEquals(appointment, acts.get(0));
        for (Act act : acts) {
            checkAppointment(act, from, to, schedule, appointmentType, customer, patient, clinician, author);
            from = DateRules.getDate(from, interval, units);
            to = DateRules.getDate(to, interval, units);
        }

        assertFalse(series.save());
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
}
