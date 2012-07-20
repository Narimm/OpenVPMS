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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.patient;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.EntityIdentity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBeanFactory;
import org.openvpms.component.business.service.lookup.LookupService;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


/**
 * Tests the {@link PatientRules} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class PatientRulesTestCase extends ArchetypeServiceTest {

    @Autowired
    private LookupService lookups;

    @Autowired
    private IMObjectBeanFactory factory;

    /**
     * The rules.
     */
    private PatientRules rules;


    /**
     * Tests the {@link PatientRules#getOwner}
     * and {@link PatientRules#getOwnerReference} methods.
     */
    @Test
    public void testGetOwner() {
        Party patient1 = TestHelper.createPatient();
        assertNull(rules.getOwner(patient1));
        assertNull(rules.getOwnerReference(patient1));

        Party customer = TestHelper.createCustomer();
        Party patient2 = TestHelper.createPatient();
        rules.addPatientOwnerRelationship(customer, patient2);
        assertEquals(customer, rules.getOwner(patient2));
        assertEquals(customer.getObjectReference(),
                     rules.getOwnerReference(patient2));

        deactivateOwnerRelationship(patient2);
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
        checkOwner(patient, "2007-01-31", customer1); // exact match
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
        Party customer = TestHelper.createCustomer();
        Party patient2 = TestHelper.createPatient();
        rules.addPatientOwnerRelationship(customer, patient2);

        assertFalse(rules.isOwner(customer, patient1));
        assertTrue(rules.isOwner(customer, patient2));

        deactivateOwnerRelationship(patient2);
        assertFalse(rules.isOwner(customer, patient2));
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
        createWeight(date1, patient, new BigDecimal("5.0"), "KILOGRAMS");
        assertEquals("5 Kilograms", rules.getPatientWeight(patient));

        Date date2 = getDate("2007-02-25");
        createWeight(date2, patient, new BigDecimal("13"), "POUNDS");
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
     * Tests the {@link PatientRules#getMicrochip(Party)} method.
     */
    @Test
    public void testGetMicrochip() {
        Party patient = TestHelper.createPatient(false);
        assertNull(rules.getMicrochip(patient));

        EntityIdentity microchip = (EntityIdentity) create("entityIdentity.microchip");
        IMObjectBean tagBean = new IMObjectBean(microchip);
        tagBean.setValue("microchip", "1234567");
        patient.addIdentity(microchip);
        assertEquals("1234567", rules.getMicrochip(patient));

        microchip.setActive(false);
        assertNull(rules.getMicrochip(patient));
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
        PatientAgeFormatter formatter = new PatientAgeFormatter(lookups, new PracticeRules(), factory);
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
        PatientAgeFormatter formatter = new PatientAgeFormatter(lookups, new PracticeRules(), factory);
        String expected = formatter.format(birth, deceased);
        assertEquals(expected, age);
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        rules = new PatientRules(getArchetypeService(), null, null);
    }

    /**
     * Checks the ownership of a patient for a given date.
     *
     * @param patient  the patient
     * @param date     the date
     * @param expected thhe expected owner
     */
    private void checkOwner(Party patient, String date, Party expected) {
        Act act = new Act();
        act.setActivityStartTime(getDate(date));
        ActBean bean = new ActBean(act);
        bean.addParticipation("participation.patient", patient);
        assertEquals(expected, rules.getOwner(act));
    }

    /**
     * Marks the patient-owner relationship inactive.
     *
     * @param patient the patient
     */
    private void deactivateOwnerRelationship(Party patient) {
        EntityBean bean = new EntityBean(patient);
        for (EntityRelationship relationship
                : bean.getRelationships(PatientArchetypes.PATIENT_OWNER)) {
            relationship.setActive(false);
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignore) {
            // do nothing
        }
    }

    /**
     * Creates a new <em>act.patientWeight</em> for a patient and saves it.
     *
     * @param date    the date
     * @param patient the patient
     * @param weight  the weight
     * @param units   the weight units
     */
    private void createWeight(Date date, Party patient, BigDecimal weight,
                              String units) {
        Act act = (Act) create("act.patientWeight");
        ActBean bean = new ActBean(act);
        bean.addParticipation("participation.patient", patient);
        bean.setValue("startTime", date);
        bean.setValue("weight", weight);
        bean.setValue("units", units);
        save(act);
    }

    /**
     * Helper to convert a string to a date.
     *
     * @param value the date string
     * @return the date
     */
    private Date getDate(String value) {
        return java.sql.Date.valueOf(value);
    }

}
