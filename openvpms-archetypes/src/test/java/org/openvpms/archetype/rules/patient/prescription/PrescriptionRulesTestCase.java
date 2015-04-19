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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.patient.prescription;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link PrescriptionRules} class.
 *
 * @author Tim Anderson
 */
public class PrescriptionRulesTestCase extends ArchetypeServiceTest {

    /**
     * The rules.
     */
    private PrescriptionRules rules;

    /**
     * The patient.
     */
    private Party patient;

    /**
     * The product to dispense.
     */
    private Product product;

    /**
     * The clinician.
     */
    private User clinician;


    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        rules = new PrescriptionRules(getArchetypeService());
        patient = TestHelper.createPatient();
        product = TestHelper.createProduct();
        clinician = TestHelper.createClinician();
    }

    /**
     * Tests the {@link PrescriptionRules#canDispense(Act)} method.
     */
    @Test
    public void testCanDispense() {
        Act act1 = createPrescription(1, 1, DateRules.getToday());
        Act act2 = createPrescription(1, 1, DateRules.getTomorrow());
        Act act3 = createPrescription(1, 1, DateRules.getYesterday());

        assertTrue(rules.canDispense(act1));
        assertTrue(rules.canDispense(act2));
        assertFalse(rules.canDispense(act3));

        dispense(act1, 1);
        dispense(act1, 1);
        assertFalse(rules.canDispense(act1));

        dispense(act2, 1);
        assertTrue(rules.canDispense(act2));

        dispense(act2, 1);
        assertFalse(rules.canDispense(act2));
    }

    /**
     * Tests the {@link PrescriptionRules#getQuantity(Act)} method.
     */
    @Test
    public void testQuantity() {
        Act act = createPrescription(2, 5, DateRules.getTomorrow());
        checkEquals(new BigDecimal("2"), rules.getQuantity(act));
    }

    /**
     * Tests the {@link PrescriptionRules#getRepeats(Act)} method.
     */
    @Test
    public void testRepeats() {
        Act act = createPrescription(2, 5, DateRules.getTomorrow());
        assertEquals(5, rules.getRepeats(act));
    }

    /**
     * Tests the {@link PrescriptionRules#getDispensed(Act)} method.
     */
    @Test
    public void testDispensed() {
        Act act1 = createPrescription(2, 0, DateRules.getTomorrow());
        Act act2 = createPrescription(2, 5, DateRules.getTomorrow());

        assertEquals(0, rules.getDispensed(act1));
        dispense(act1, 2);
        assertEquals(1, rules.getDispensed(act1));

        dispense(act2, 2);
        dispense(act2, 2);
        dispense(act2, 2);
        assertEquals(3, rules.getDispensed(act2));
    }

    /**
     * Tests the {@link PrescriptionRules#getPrescription)} methods.
     */
    @Test
    public void testGetPrescription() {
        Date yesterday = DateRules.getYesterday();
        Date today = DateRules.getToday();
        Act act1 = createPrescription(1, 1, today);
        Act act2 = createPrescription(1, 1, DateRules.getTomorrow());
        Act act3 = createPrescription(1, 1, yesterday); // expired

        assertEquals(act1, rules.getPrescription(patient, product));
        assertEquals(act1, rules.getPrescription(patient, product, today));

        // check excluding act1
        assertEquals(act2, rules.getPrescription(patient, product, Arrays.asList(act1)));
        assertEquals(act2, rules.getPrescription(patient, product, today, Arrays.asList(act1)));
        dispense(act1, 1);
        dispense(act1, 1);

        assertEquals(act2, rules.getPrescription(patient, product));
        dispense(act2, 1);
        dispense(act2, 1);

        assertNull(rules.getPrescription(patient, product));

        assertEquals(act3, rules.getPrescription(patient, product, yesterday));
        dispense(act3, 1);
        dispense(act3, 1);
        assertNull(rules.getPrescription(patient, product, yesterday));
    }

    /**
     * Dispenses a prescription.
     *
     * @param prescription the prescription
     * @param quantity     the quantity to dispense
     */
    private void dispense(Act prescription, int quantity) {
        dispense(prescription, BigDecimal.valueOf(quantity));
    }

    /**
     * Dispenses a prescription.
     *
     * @param prescription the prescription
     * @param quantity     the quantity to dispense
     */
    private void dispense(Act prescription, BigDecimal quantity) {
        Act medication = (Act) create(PatientArchetypes.PATIENT_MEDICATION);
        ActBean bean = new ActBean(prescription);
        bean.addNodeRelationship("dispensing", medication);

        ActBean medBean = new ActBean(medication);
        medBean.setValue("quantity", quantity);
        medBean.addNodeParticipation("patient", patient);
        medBean.addNodeParticipation("product", product);

        save(prescription, medication);
    }

    /**
     * Creates a prescription.
     *
     * @param quantity   the quantity to dispense
     * @param repeats    the no. of repeats
     * @param expiryDate the expiry date
     * @return a new prescription
     */
    private Act createPrescription(int quantity, int repeats, Date expiryDate) {
        return PrescriptionTestHelper.createPrescription(patient, product, clinician, quantity, repeats, expiryDate);
    }
}
