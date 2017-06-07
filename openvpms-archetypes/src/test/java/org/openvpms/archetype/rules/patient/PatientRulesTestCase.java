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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.patient;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.math.WeightUnits;
import org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityIdentity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.openvpms.archetype.test.TestHelper.getDate;


/**
 * Tests the {@link PatientRules} class.
 *
 * @author Tim Anderson
 */
public class PatientRulesTestCase extends ArchetypeServiceTest {

    /**
     * The bean factory.
     */
    @Autowired
    private IMObjectBeanFactory factory;

    /**
     * The rules.
     */
    private PatientRules rules;

    /**
     * The practice rules.
     */
    private PracticeRules practiceRules;


    /**
     * Tests the {@link PatientRules#getOwner} and {@link PatientRules#getOwnerReference} methods.
     */
    @Test
    public void testGetOwner() {
        Party patient1 = TestHelper.createPatient();
        assertNull(rules.getOwner(patient1));
        assertNull(rules.getOwnerReference(patient1));

        Party customer = TestHelper.createCustomer();
        Party patient2 = TestHelper.createPatient();
        Party customer2 = TestHelper.createCustomer();
        Party customer3 = TestHelper.createCustomer();

        rules.addPatientLocationRelationship(customer2, patient2);
        rules.addPatientOwnerRelationship(customer3, patient2);
        deactivateRelationship(patient2, PatientArchetypes.PATIENT_OWNER);

        rules.addPatientOwnerRelationship(customer, patient2);

        assertEquals(customer, rules.getOwner(patient2));
        assertEquals(customer.getObjectReference(), rules.getOwnerReference(patient2));

        deactivateRelationship(patient2, PatientArchetypes.PATIENT_OWNER);
        assertNull(rules.getOwner(patient2));
        assertNull(rules.getOwnerReference(patient2));
    }

    /**
     * Tests the {@link PatientRules#getOwner(Act)} method.
     */
    @Test
    public void testGetOwnerFromAct() {
        Party patient = TestHelper.createPatient();
        Party customer1 = TestHelper.createCustomer();
        Party customer2 = TestHelper.createCustomer();


        checkOwner(patient, "2006-12-10", null); // no owner

        EntityRelationship r1 = rules.addPatientOwnerRelationship(customer1,
                                                                  patient);
        EntityRelationship r2 = rules.addPatientOwnerRelationship(customer2,
                                                                  patient);

        r1.setActiveStartTime(getDate("2007-01-01"));
        r1.setActiveEndTime(getDate("2007-01-31"));
        r2.setActiveStartTime(getDate("2007-02-02"));
        r2.setActiveEndTime(null);

        save(patient);
        save(customer1);
        save(customer2);

        checkOwner(patient, "2006-12-10", customer1); // customer1 closest
        checkOwner(patient, "2007-01-01", customer1); // exact match
        checkOwner(patient, "2007-01-31", customer2); // customer1 relationship ended
        checkOwner(patient, "2007-02-01", customer2); // customer2 closest
        checkOwner(patient, "2007-02-02", customer2); // exact match
        checkOwner(patient, "2008-01-01", customer2); // unbounded end time
    }

    /**
     * Tests the {@link PatientRules#isOwner} method.
     */
    @Test
    public void testIsOwner() {
        Party patient1 = TestHelper.createPatient();
        Party customer1 = TestHelper.createCustomer();
        Party patient2 = TestHelper.createPatient();
        Party customer2 = TestHelper.createCustomer();
        rules.addPatientOwnerRelationship(customer1, patient2);
        rules.addPatientLocationRelationship(customer2, patient2);

        assertFalse(rules.isOwner(customer1, patient1));
        assertTrue(rules.isOwner(customer1, patient2));

        deactivateRelationship(patient2, PatientArchetypes.PATIENT_OWNER);
        assertFalse(rules.isOwner(customer1, patient2));

    }

    /**
     * Test the {@link PatientRules#getLocation} methods
     */
    @Test
    public void testGetLocation() {
        Party patient1 = TestHelper.createPatient();
        assertNull(rules.getLocation(patient1));

        Party customer = TestHelper.createCustomer();
        Party patient2 = TestHelper.createPatient();
        rules.addPatientLocationRelationship(customer, patient2);
        assertEquals(customer, rules.getLocation(patient2));

        deactivateRelationship(patient2, PatientArchetypes.PATIENT_LOCATION);
        assertNull(rules.getOwner(patient2));
        assertNull(rules.getOwnerReference(patient2));
    }

    /**
     * Tests the {@link PatientRules#getLocation(Act)} method.
     */
    @Test
    public void testGetLocationFromAct() {
        Party patient = TestHelper.createPatient();
        Party customer1 = TestHelper.createCustomer();
        Party customer2 = TestHelper.createCustomer();

        checkLocation(patient, "2006-12-10", null); // no owner

        EntityRelationship r1 = rules.addPatientLocationRelationship(customer1, patient);
        EntityRelationship r2 = rules.addPatientLocationRelationship(customer2, patient);

        r1.setActiveStartTime(getDate("2007-01-01"));
        r1.setActiveEndTime(getDate("2007-01-31"));
        r2.setActiveStartTime(getDate("2007-02-02"));
        r2.setActiveEndTime(null);

        save(patient);
        save(customer1);
        save(customer2);

        checkLocation(patient, "2006-12-10", customer1); // customer1 closest
        checkLocation(patient, "2007-01-01", customer1); // exact match
        checkLocation(patient, "2007-01-31", customer2); // customer1 relationship ended
        checkLocation(patient, "2007-02-01", customer2); // customer2 closest
        checkLocation(patient, "2007-02-02", customer2); // exact match
        checkLocation(patient, "2008-01-01", customer2); // unbounded end time
    }

    /**
     * Tests the {@link PatientRules#getReferralVet} method.
     */
    @Test
    public void testGetReferralVet() {
        Party patient = TestHelper.createPatient(false);
        Party vet = TestHelper.createSupplierVet();
        EntityBean bean = new EntityBean(patient);
        EntityRelationship referral
                = bean.addRelationship("entityRelationship.referredFrom", vet);

        // verify the vet is returned for a time > the default start time
        Party vet2 = rules.getReferralVet(patient, new Date());
        assertEquals(vet, vet2);

        // now set the start and end time and verify that there is no referrer
        // for a later time (use time addition due to system clock granularity)
        Date start = new Date();
        Date end = new Date(start.getTime() + 1);
        Date later = new Date(end.getTime() + 1);
        referral.setActiveStartTime(start);
        referral.setActiveEndTime(end);
        assertNull(rules.getReferralVet(patient, later));
    }

    /**
     * Tests the {@link PatientRules#getPatientWeight(Party)} method.
     */
    @Test
    public void testGetPatientWeight() {
        Party patient = TestHelper.createPatient();
        assertNull(rules.getPatientWeight(patient));

        Date date1 = getDate("2006-12-22");
        PatientTestHelper.createWeight(patient, date1, new BigDecimal("5.0"), WeightUnits.KILOGRAMS);
        assertEquals("5 Kilograms", rules.getPatientWeight(patient));

        Date date2 = getDate("2007-02-25");
        PatientTestHelper.createWeight(patient, date2, new BigDecimal("13"), WeightUnits.POUNDS);
        assertEquals("13 Pounds", rules.getPatientWeight(patient));
    }

    /**
     * Tests the {@link PatientRules#setInactive(Party)} method.
     */
    @Test
    public void testSetInactive() {
        Party patient = TestHelper.createPatient(false);
        assertTrue(patient.isActive());

        rules.setInactive(patient);
        patient = get(patient);
        assertFalse(patient.isActive());
    }

    /**
     * Tests the {@link PatientRules#setDeceased(Party)} and
     * {@link PatientRules#isDeceased(Party)} methods.
     */
    @Test
    public void testDeceased() {
        Party patient = TestHelper.createPatient(false);
        assertFalse(rules.isDeceased(patient));
        rules.setDeceased(patient);
        assertTrue(rules.isDeceased(patient));
    }

    /**
     * Tests the {@link PatientRules#setDesexed(Party)} and
     * {@link PatientRules#isDesexed(Party)} methods.
     */
    @Test
    public void testDesexed() {
        Party patient = TestHelper.createPatient(false);
        assertFalse(rules.isDesexed(patient));
        rules.setDesexed(patient);
        assertTrue(rules.isDesexed(patient));
    }

    /**
     * Tests the {@link PatientRules#getMicrochipNumber(Party)} method.
     */
    @Test
    public void testGetMicrochipNumber() {
        Party patient = TestHelper.createPatient(false);
        assertNull(rules.getMicrochipNumber(patient));

        EntityIdentity microchip = createMicrochip("1234567");
        patient.addIdentity(microchip);
        assertEquals("1234567", rules.getMicrochipNumber(patient));

        microchip.setActive(false);
        assertNull(rules.getMicrochipNumber(patient));
    }

    /**
     * Tests the {@link PatientRules#getMicrochipNumbers(Party)} method.
     */
    @Test
    public void testGetMicrochipNumbers() {
        Party patient = TestHelper.createPatient(false);
        assertNull(rules.getMicrochipNumbers(patient));

        EntityIdentity microchip1 = createMicrochip("123");
        patient.addIdentity(microchip1);
        save(patient);
        assertEquals("123", rules.getMicrochipNumbers(patient));

        EntityIdentity microchip2 = createMicrochip("456");
        patient.addIdentity(microchip2);
        save(patient); // 456 will be returned first as its id is higher

        assertEquals("456, 123", rules.getMicrochipNumbers(patient));
    }

    /**
     * Tests the {@link PatientRules#getPetTag(Party)} method.
     */
    @Test
    public void testGetPetTag() {
        Party patient = TestHelper.createPatient(false);
        assertNull(rules.getPetTag(patient));

        EntityIdentity tag = (EntityIdentity) create("entityIdentity.petTag");
        IMObjectBean tagBean = new IMObjectBean(tag);
        tagBean.setValue("petTag", "1234567");
        patient.addIdentity(tag);
        assertEquals("1234567", rules.getPetTag(patient));

        tag.setActive(false);
        assertNull(rules.getPetTag(patient));
    }

    /**
     * Tests the {@link PatientRules#getRabiesTag(Party)} method.
     */
    @Test
    public void testGetRabiesTag() {
        Party patient = TestHelper.createPatient(false);
        assertNull(rules.getRabiesTag(patient));

        EntityIdentity tag = (EntityIdentity) create("entityIdentity.rabiesTag");
        tag.setIdentity("1234567");
        patient.addIdentity(tag);
        assertEquals("1234567", rules.getRabiesTag(patient));

        tag.setActive(false);
        assertNull(rules.getRabiesTag(patient));
    }

    /**
     * Tests {@link PatientRules#getPatientAge} for a patient with no birth date.
     */
    @Test
    public void testGetPatientAgeNoBirthDate() {
        Party patient = TestHelper.createPatient(false);
        String age = rules.getPatientAge(patient);
        assertEquals("No Birthdate", age);
    }

    /**
     * Tests {@link PatientRules#getPatientAge} for a patient with a birth date.
     */
    @Test
    public void testGetPatientAgeWithBirthDate() {
        Party patient = TestHelper.createPatient(false);
        IMObjectBean bean = new IMObjectBean(patient);
        Date birthDate = getDate("2010-01-01");
        bean.setValue("dateOfBirth", birthDate);
        String age = rules.getPatientAge(patient);
        PatientAgeFormatter formatter = new PatientAgeFormatter(getLookupService(), practiceRules, factory);
        String expected = formatter.format(birthDate);
        assertEquals(expected, age);
    }

    /**
     * Tests {@link PatientRules#getPatientAge} for a deceased patient.
     */
    @Test
    public void testGetPatientAgeWithDeceasedDate() {
        Party patient = TestHelper.createPatient(false);
        IMObjectBean bean = new IMObjectBean(patient);
        Date birth = getDate("2010-01-01");
        Date deceased = getDate("2011-05-01");
        bean.setValue("dateOfBirth", birth);
        bean.setValue("deceasedDate", deceased);
        String age = rules.getPatientAge(patient);
        PatientAgeFormatter formatter = new PatientAgeFormatter(getLookupService(), practiceRules, factory);
        String expected = formatter.format(birth, deceased);
        assertEquals(expected, age);
    }

    /**
     * Tests the {@link PatientRules#isAllergy(Act)} method.
     */
    @Test
    public void testIsAllergy() {
        Party patient = TestHelper.createPatient();
        Entity other = ReminderTestHelper.createAlertType("Z Alert Type 1");
        Entity allergy = ReminderTestHelper.createAlertType("Z Alert Type 2", "ALLERGY");
        Entity aggression = ReminderTestHelper.createAlertType("Z Alert Type 3", "AGGRESSION");
        Act act1 = ReminderTestHelper.createAlert(patient, other);
        assertFalse(rules.isAllergy(act1));

        Act act2 = ReminderTestHelper.createAlert(patient, allergy);
        assertTrue(rules.isAllergy(act2));

        Act act3 = ReminderTestHelper.createAlert(patient, aggression);
        assertFalse(rules.isAllergy(act3));
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        practiceRules = new PracticeRules(getArchetypeService(), null);
        rules = new PatientRules(practiceRules, getArchetypeService(), getLookupService());
    }

    /**
     * Checks the ownership of a patient for a given date.
     *
     * @param patient  the patient
     * @param date     the date
     * @param expected the expected owner
     */
    private void checkOwner(Party patient, String date, Party expected) {
        Act act = new Act();
        act.setActivityStartTime(getDate(date));
        ActBean bean = new ActBean(act);
        bean.addParticipation(PatientArchetypes.PATIENT_PARTICIPATION, patient);
        assertEquals(expected, rules.getOwner(act));
    }

    /**
     * Checks the location customer of a patient for a given date.
     *
     * @param patient  the patient
     * @param date     the date
     * @param expected the expected location customer
     */
    private void checkLocation(Party patient, String date, Party expected) {
        Act act = new Act();
        act.setActivityStartTime(getDate(date));
        ActBean bean = new ActBean(act);
        bean.addParticipation(PatientArchetypes.PATIENT_PARTICIPATION, patient);
        assertEquals(expected, rules.getLocation(act));
    }

    /**
     * Marks a relationship inactive.
     *
     * @param patient   the patient
     * @param shortName the relationship short name
     */
    private void deactivateRelationship(Party patient, String shortName) {
        EntityBean bean = new EntityBean(patient);
        for (EntityRelationship relationship : bean.getRelationships(shortName)) {
            relationship.setActive(false);
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignore) {
            // do nothing
        }
    }

    /**
     * Helper to create a microchip.
     *
     * @param microchip the chip value
     * @return the microchip
     */
    private EntityIdentity createMicrochip(String microchip) {
        EntityIdentity result = (EntityIdentity) create("entityIdentity.microchip");
        IMObjectBean bean = new IMObjectBean(result);
        bean.setValue("microchip", microchip);
        return result;
    }

}
