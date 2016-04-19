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

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.math.WeightUnits;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.patient.PatientTestHelper;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.workflow.AppointmentRules;
import org.openvpms.archetype.rules.workflow.ScheduleTestHelper;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link Visits} class.
 *
 * @author Tim Anderson
 */
@ContextConfiguration("/applicationContext.xml")
public class VisitsTestCase extends ArchetypeServiceTest {

    /**
     * The appointment rules.
     */
    @Autowired
    private AppointmentRules appointmentRules;

    /**
     * The patient rules.
     */
    @Autowired
    private PatientRules patientRules;

    /**
     * The test visits.
     */
    private Visits visits;

    /**
     * The test customer.
     */
    private Party customer;

    /**
     * The first test patient.
     */
    private Party patient1;

    /**
     * The second test patient.
     */
    private Party patient2;

    /**
     * The test location.
     */
    private Party location;

    /**
     * The cage type.
     */
    private Entity cageType;


    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        location = TestHelper.createLocation();
        customer = TestHelper.createCustomer();
        patient1 = TestHelper.createPatient(customer);
        patient2 = TestHelper.createPatient(customer);
        visits = new Visits(customer, appointmentRules, patientRules);

        Product firstPetProductDay = TestHelper.createProduct(ProductArchetypes.SERVICE, null);
        Product firstPetProductNight = TestHelper.createProduct(ProductArchetypes.SERVICE, null);
        Product secondPetProductDay = TestHelper.createProduct(ProductArchetypes.SERVICE, null);
        Product secondPetProductNight = TestHelper.createProduct(ProductArchetypes.SERVICE, null);
        cageType = ScheduleTestHelper.createCageType("Z Test Cage", firstPetProductDay, firstPetProductNight,
                                                     secondPetProductDay, secondPetProductNight);
    }

    /**
     * Verifies that double bookings on the same day are rated so that:
     * <ul>
     * <li>the heaviest patient attracts the first pet rate, and subsequent pets attract the second pet rate</li>
     * <li>when no weight is specified, the lowest id patient attracts the first pet rate</li>
     * </ul>
     */
    @Test
    public void testRateDoubleBookingOnSameDay() {
        Party patient3 = TestHelper.createPatient(customer);
        Party schedule = ScheduleTestHelper.createSchedule(location, cageType);

        // create some visits all starting and ending at the same time
        Visit visit1 = createVisit("2016-03-24 10:00:00", "2016-03-24 17:00:00", schedule, customer, patient1);
        Visit visit2 = createVisit("2016-03-24 10:00:00", "2016-03-24 17:00:00", schedule, customer, patient2);
        Visit visit3 = createVisit("2016-03-24 10:00:00", "2016-03-24 17:00:00", schedule, customer, patient3);

        // make sure rate() updates all visits. As no patient has a weight, their ids will be used to select the
        // appropriate one
        visit1.setFirstPet(false);
        visit2.setFirstPet(true);
        visit3.setFirstPet(true);

        visits.rate(Arrays.asList(visit1, visit2, visit3), new Date());
        assertTrue(visit1.isFirstPet());
        assertFalse(visit2.isFirstPet());
        assertFalse(visit3.isFirstPet());

        // now assign weights to each patient.
        PatientTestHelper.createWeight(patient1, BigDecimal.ONE, WeightUnits.KILOGRAMS);
        PatientTestHelper.createWeight(patient2, BigDecimal.valueOf(2), WeightUnits.KILOGRAMS);
        PatientTestHelper.createWeight(patient3, BigDecimal.TEN, WeightUnits.KILOGRAMS);
        visit1 = refresh(visit1); // need to refresh as the weight is cached.
        visit2 = refresh(visit2);
        visit3 = refresh(visit3);
        visits.rate(Arrays.asList(visit1, visit2, visit3), new Date());
        assertFalse(visit1.isFirstPet());
        assertFalse(visit2.isFirstPet());
        assertTrue(visit3.isFirstPet());
    }

    /**
     * Verifies that double bookings on the same day but different times still attract the first and second pet rates.
     * NOTE: visits ending at midnight are treated as having ended on the previous day.
     */
    @Test
    public void testRateDoubleBookingOnSameDayDifferentTimes() {
        Party patient3 = TestHelper.createPatient();
        Party schedule = ScheduleTestHelper.createSchedule(location, cageType);

        // create some visits all starting and ending at different times on the same day (midnight included)
        Visit visit1 = createVisit("2016-03-24 09:00:00", "2016-03-24 15:00:00", schedule, customer, patient1);
        Visit visit2 = createVisit("2016-03-24 10:00:00", "2016-03-25 00:00:00", schedule, customer, patient2);
        Visit visit3 = createVisit("2016-03-24 11:00:00", "2016-03-24 17:00:00", schedule, customer, patient3);

        // make sure rate() updates all visits. As no patient has a weight, their ids will be used to select the
        // appropriate one
        visit1.setFirstPet(false);
        visit2.setFirstPet(true);
        visit3.setFirstPet(true);

        visits.rate(Arrays.asList(visit1, visit2, visit3), new Date());
        assertTrue(visit1.isFirstPet());
        assertFalse(visit2.isFirstPet());
        assertFalse(visit3.isFirstPet());
    }

    /**
     * Verifies that when two bookings occupy the same schedule but start or end on different days, they both
     * attract the first pet rate.
     */
    @Test
    public void testRateDoubleBookingOnDifferentDaySameSchedule() {
        Party schedule = ScheduleTestHelper.createSchedule(location, cageType);

        // create some overlapping visits
        Visit visit1 = createVisit("2016-03-24 09:00:00", "2016-03-25 15:00:00", schedule, customer, patient1);
        Visit visit2 = createVisit("2016-03-25 10:00:00", "2016-03-26 16:00:00", schedule, customer, patient2);
        visit1.setFirstPet(true);
        visit2.setFirstPet(true);

        visits.rate(Arrays.asList(visit1, visit2), new Date());
        assertTrue(visit1.isFirstPet());
        assertTrue(visit2.isFirstPet());
    }

    /**
     * Verifies that if there is a double booking for the same dates and schedule and the first appointment is
     * completed, the second booking gets charged at the second pet rate.
     * NOTE: visits ending at midnight are treated as having ended on the previous day.
     */
    @Test
    public void testRateDoubleBookingForCompletedAppointment() {
        Party patient3 = TestHelper.createPatient();
        Party schedule = ScheduleTestHelper.createSchedule(location, cageType);

        Visit visit1 = createVisit("2016-03-24 09:00:00", "2016-03-24 15:00:00", schedule, customer, patient1);
        Visit visit2 = createVisit("2016-03-24 10:00:00", "2016-03-24 16:00:00", schedule, customer, patient2);
        Visit visit3 = createVisit("2016-03-24 10:00:00", "2016-03-25 00:00:00", schedule, customer, patient3);
        assertFalse(visit1.isCharged());
        assertFalse(visit2.isCharged());
        assertFalse(visit3.isCharged());

        visit1.setFirstPet(true);
        visit1.setCharged(true);
        visit1.save();
        visit1 = visits.create(visit1.getEvent(), visit1.getAppointment());
        assertTrue(visit1.isFirstPet());
        assertTrue(visit1.isCharged());

        visits.rate(Collections.singletonList(visit2), new Date());
        assertFalse(visit2.isFirstPet());

        visits.rate(Collections.singletonList(visit3), new Date());
        assertFalse(visit3.isFirstPet());
    }

    /**
     * Creates a new visit. The appointment and event start and end times will be identical.
     *
     * @param startTime the start time
     * @param endTime   the end time
     * @param schedule  the schedule
     * @param customer  the customer
     * @param patient   the patient
     * @return a new visit
     */
    private Visit createVisit(String startTime, String endTime, Entity schedule, Party customer, Party patient) {
        return BoardingTestHelper.createVisit(startTime, endTime, schedule, customer, patient, visits);
    }

    /**
     * Recreates a visit. Use this to get around caching of patient weights.
     *
     * @param visit the visit
     * @return the visit
     */
    private Visit refresh(Visit visit) {
        return visits.create(visit.getEvent(), visit.getAppointment());
    }

}
