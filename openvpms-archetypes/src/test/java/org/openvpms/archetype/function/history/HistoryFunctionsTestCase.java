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

package org.openvpms.archetype.function.history;

import org.apache.commons.jxpath.FunctionLibrary;
import org.apache.commons.jxpath.JXPathContext;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.finance.account.FinancialTestHelper;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.patient.PatientTestHelper;
import org.openvpms.archetype.rules.product.ProductTestHelper;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.ArchetypeServiceFunctions;
import org.openvpms.component.system.common.jxpath.JXPathHelper;
import org.openvpms.component.system.common.jxpath.ObjectFunctions;

import java.util.Date;
import java.util.Iterator;

import static java.math.BigDecimal.ONE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link HistoryFunctions} class.
 *
 * @author Tim Anderson
 */
public class HistoryFunctionsTestCase extends ArchetypeServiceTest {

    /**
     * The patient.
     */
    private Party patient;

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        patient = TestHelper.createPatient();
    }

    /**
     * Tests the {@link HistoryFunctions#charges(Party)} method.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testCharges() {
        Act item1 = createInvoiceItem();
        Act item2 = createInvoiceItem();
        item1.setActivityStartTime(TestHelper.getDate("2016-05-02"));
        item2.setActivityStartTime(TestHelper.getDate("2016-05-03"));
        save(item1, item2);

        JXPathContext ctx = createContext(patient);
        Iterable<Act> acts = (Iterable<Act>) ctx.getValue("history:charges(.)");
        checkActs(acts, item2, item1);
    }

    /**
     * Tests the {@link HistoryFunctions#charges(Party, Date)} method.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testChargesByDate() {
        Act item1 = createInvoiceItem();
        Act item2 = createInvoiceItem();
        Act item3 = createInvoiceItem();
        item1.setActivityStartTime(TestHelper.getDatetime("2016-05-03 00:00:00"));
        item2.setActivityStartTime(TestHelper.getDatetime("2016-05-03 23:59:00"));
        item3.setActivityStartTime(TestHelper.getDate("2016-05-04"));
        save(item1, item2, item3);

        JXPathContext ctx = createContext(patient);
        Iterable<Act> acts1 = (Iterable<Act>) ctx.getValue("history:charges(., null)");
        checkActs(acts1);

        Iterable<Act> acts2 = (Iterable<Act>) ctx.getValue(
                "history:charges(., java.sql.Date.valueOf('2016-05-03'))");
        checkActs(acts2, item2, item1);

        Iterable<Act> acts3 = (Iterable<Act>) ctx.getValue(
                "history:charges(., java.sql.Date.valueOf('2016-05-04'))");
        checkActs(acts3, item3);

        Iterable<Act> acts4 = (Iterable<Act>) ctx.getValue(
                "history:charges(., java.sql.Date.valueOf('2016-05-05'))");
        checkActs(acts4);
    }

    /**
     * Tests the {@link HistoryFunctions#charges(Party, Date, Date)} method.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testChargesByDateRange() {
        Act item1 = createInvoiceItem();
        Act item2 = createInvoiceItem();
        item1.setActivityStartTime(TestHelper.getDate("2016-05-03"));
        item2.setActivityStartTime(TestHelper.getDate("2016-05-04"));
        save(item1, item2);

        JXPathContext ctx = createContext(patient);
        Iterable<Act> acts1 = (Iterable<Act>) ctx.getValue("history:charges(., null, null)");
        checkActs(acts1, item2, item1);

        Iterable<Act> acts2 = (Iterable<Act>) ctx.getValue(
                "history:charges(., java.sql.Date.valueOf('2016-05-03'), java.sql.Date.valueOf('2016-05-05'))");
        checkActs(acts2, item2, item1);

        Iterable<Act> acts3 = (Iterable<Act>) ctx.getValue(
                "history:charges(., java.sql.Date.valueOf('2016-05-03'), java.sql.Date.valueOf('2016-05-04'))");
        checkActs(acts3, item1);

        Iterable<Act> acts4 = (Iterable<Act>) ctx.getValue(
                "history:charges(., null, java.sql.Date.valueOf('2016-05-04'))");
        checkActs(acts4, item1);

        Iterable<Act> acts5 = (Iterable<Act>) ctx.getValue(
                "history:charges(., null, java.sql.Date.valueOf('2016-05-05'))");
        checkActs(acts5, item2, item1);

        Iterable<Act> acts6 = (Iterable<Act>) ctx.getValue(
                "history:charges(., java.sql.Date.valueOf('2016-05-03'), null)");
        checkActs(acts6, item2, item1);
    }

    /**
     * Tests the {@link HistoryFunctions#chargesByProductType(Party, String)} method.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testChargesByProductType() {
        Entity productType1 = createProductType("Vaccination");
        Entity productType2 = createProductType("Euthanasia");
        Product product1 = createProduct(productType1);
        Product product2 = createProduct(productType2);

        Act item1 = createInvoiceItem(product1);
        Act item2 = createInvoiceItem(product2);
        save(item1, item2);

        JXPathContext ctx = createContext(patient);
        Iterable<Act> acts1 = (Iterable<Act>) ctx.getValue("history:charges(., 'Vaccination')");
        checkActs(acts1, item1);

        Iterable<Act> acts2 = (Iterable<Act>) ctx.getValue("history:charges(., 'Euth*')");
        checkActs(acts2, item2);

        Iterable<Act> acts3 = (Iterable<Act>) ctx.getValue("history:charges(., 'Procedure')");
        checkActs(acts3);
    }

    /**
     * Tests the {@link HistoryFunctions#chargesByProductType(Party, String, Date)} method.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testChargesByProductTypeAndDate() {
        Entity productType1 = createProductType("Vaccination");
        Entity productType2 = createProductType("Euthanasia");
        Product product1 = createProduct(productType1);
        Product product2 = createProduct(productType2);

        Act item1 = createInvoiceItem(product1);
        Act item2 = createInvoiceItem(product1);
        Act item3 = createInvoiceItem(product2);
        item1.setActivityStartTime(TestHelper.getDatetime("2016-05-03 00:00:00"));
        item2.setActivityStartTime(TestHelper.getDatetime("2016-05-03 23:59:00"));
        item3.setActivityStartTime(TestHelper.getDate("2016-05-04"));
        save(item1, item2, item3);

        JXPathContext ctx = createContext(patient);
        Iterable<Act> acts1 = (Iterable<Act>) ctx.getValue("history:charges(., 'Vaccination', null)");
        checkActs(acts1);

        Iterable<Act> acts2 = (Iterable<Act>) ctx.getValue(
                "history:charges(., 'Vacc*', java.sql.Date.valueOf('2016-05-03'))");
        checkActs(acts2, item2, item1);

        Iterable<Act> acts3 = (Iterable<Act>) ctx.getValue(
                "history:charges(., 'Vaccination', java.sql.Date.valueOf('2016-05-04'))");
        checkActs(acts3);
    }

    /**
     * Tests the {@link HistoryFunctions#chargesByProductType(Party, String, Date, Date)} method.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testChargesByProductTypeAndDateRange() {
        Entity productType1 = createProductType("Vaccination");
        Entity productType2 = createProductType("Euthanasia");
        Product product1 = createProduct(productType1);
        Product product2 = createProduct(productType2);

        Act item1 = createInvoiceItem(product1);
        Act item2 = createInvoiceItem(product2);
        item1.setActivityStartTime(TestHelper.getDate("2016-05-03"));
        item2.setActivityStartTime(TestHelper.getDate("2016-05-04"));
        save(item1, item2);

        JXPathContext ctx = createContext(patient);
        Iterable<Act> acts1 = (Iterable<Act>) ctx.getValue("history:charges(., 'Vaccination', null, null)");
        checkActs(acts1, item1);

        Iterable<Act> acts2 = (Iterable<Act>) ctx.getValue(
                "history:charges(., 'Vacc*', java.sql.Date.valueOf('2016-05-03'), "
                + "java.sql.Date.valueOf('2016-05-04'))");
        checkActs(acts2, item1);

        Iterable<Act> acts3 = (Iterable<Act>) ctx.getValue(
                "history:charges(., 'Vaccination', java.sql.Date.valueOf('2016-05-01'), "
                + "java.sql.Date.valueOf('2016-05-03'))");
        checkActs(acts3);
    }

    /**
     * Tests the charges methods when a null patient is supplied.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testChargesWithNullPatient() {
        Act event = (Act) create(PatientArchetypes.CLINICAL_EVENT);
        JXPathContext ctx = createContext(event);
        Iterable<Act> acts1 = (Iterable<Act>) ctx.getValue("history:charges(openvpms:get(., 'patient.entity'))");
        checkActs(acts1);

        Iterable<Act> acts2 = (Iterable<Act>) ctx.getValue(
                "history:charges(openvpms:get(., 'patient.entity'), null, null)");
        checkActs(acts2);

        Iterable<Act> acts3 = (Iterable<Act>) ctx.getValue(
                "history:charges(openvpms:get(., 'patient.entity'), 'Vaccination')");
        checkActs(acts3);

        Iterable<Act> acts4 = (Iterable<Act>) ctx.getValue(
                "history:charges(openvpms:get(., 'patient.entity'), 'Vaccination', null, null)");
        checkActs(acts4);
    }

    /**
     * Tests the {@link HistoryFunctions#medication(Party)} method.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testMedication() {
        Act medication1 = PatientTestHelper.createMedication(patient);
        Act medication2 = PatientTestHelper.createMedication(patient);
        medication1.setActivityStartTime(TestHelper.getDate("2013-09-19"));
        medication2.setActivityStartTime(TestHelper.getDate("2013-09-20"));
        save(medication1, medication2);

        JXPathContext ctx = createContext(patient);
        Iterable<Act> acts = (Iterable<Act>) ctx.getValue("history:medication(.)");
        checkActs(acts, medication2, medication1);
    }

    /**
     * Tests the {@link HistoryFunctions#medication(Party, Date)} method.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testMedicationByDate() {
        Act medication1 = PatientTestHelper.createMedication(patient);
        Act medication2 = PatientTestHelper.createMedication(patient);
        Act medication3 = PatientTestHelper.createMedication(patient);
        medication1.setActivityStartTime(TestHelper.getDatetime("2013-09-19 00:00:00"));
        medication2.setActivityStartTime(TestHelper.getDatetime("2013-09-19 23:59:00"));
        medication3.setActivityStartTime(TestHelper.getDate("2013-09-20"));
        save(medication1, medication2, medication3);

        JXPathContext ctx = createContext(patient);
        Iterable<Act> acts1 = (Iterable<Act>) ctx.getValue("history:medication(., null)");
        checkActs(acts1);

        Iterable<Act> acts2 = (Iterable<Act>) ctx.getValue(
                "history:medication(., java.sql.Date.valueOf('2013-09-19'))");
        checkActs(acts2, medication2, medication1);

        Iterable<Act> acts3 = (Iterable<Act>) ctx.getValue(
                "history:medication(., java.sql.Date.valueOf('2013-09-20'))");
        checkActs(acts3, medication3);

        Iterable<Act> acts4 = (Iterable<Act>) ctx.getValue(
                "history:medication(., java.sql.Date.valueOf('2013-09-21'))");
        checkActs(acts4);
    }

    /**
     * Tests the {@link HistoryFunctions#medication(Party, Date, Date)} method.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testMedicationByDateRange() {
        Act medication1 = PatientTestHelper.createMedication(patient);
        Act medication2 = PatientTestHelper.createMedication(patient);
        medication1.setActivityStartTime(TestHelper.getDate("2013-09-19"));
        medication2.setActivityStartTime(TestHelper.getDate("2013-09-20"));
        save(medication1, medication2);

        JXPathContext ctx = createContext(patient);
        Iterable<Act> acts1 = (Iterable<Act>) ctx.getValue("history:medication(., null, null)");
        checkActs(acts1, medication2, medication1);

        Iterable<Act> acts2 = (Iterable<Act>) ctx.getValue(
                "history:medication(., java.sql.Date.valueOf('2013-09-19'), java.sql.Date.valueOf('2013-09-21'))");
        checkActs(acts2, medication2, medication1);

        Iterable<Act> acts3 = (Iterable<Act>) ctx.getValue(
                "history:medication(., java.sql.Date.valueOf('2013-09-19'), java.sql.Date.valueOf('2013-09-20'))");
        checkActs(acts3, medication1);

        Iterable<Act> acts4 = (Iterable<Act>) ctx.getValue(
                "history:medication(., null, java.sql.Date.valueOf('2013-09-20'))");
        checkActs(acts4, medication1);

        Iterable<Act> acts5 = (Iterable<Act>) ctx.getValue(
                "history:medication(., null, java.sql.Date.valueOf('2013-09-21'))");
        checkActs(acts5, medication2, medication1);

        Iterable<Act> acts6 = (Iterable<Act>) ctx.getValue(
                "history:medication(., java.sql.Date.valueOf('2013-09-19'), null)");
        checkActs(acts6, medication2, medication1);
    }

    /**
     * Tests the {@link HistoryFunctions#medicationByProductType(Party, String)} method.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testMedicationByProductType() {
        Entity productType1 = createProductType("Vaccination");
        Entity productType2 = createProductType("Euthanasia");
        Product product1 = createProduct(productType1);
        Product product2 = createProduct(productType2);

        Act medication1 = PatientTestHelper.createMedication(patient, product1);
        Act medication2 = PatientTestHelper.createMedication(patient, product2);

        JXPathContext ctx = createContext(patient);
        Iterable<Act> acts1 = (Iterable<Act>) ctx.getValue("history:medication(., 'Vaccination')");
        checkActs(acts1, medication1);

        Iterable<Act> acts2 = (Iterable<Act>) ctx.getValue("history:medication(., 'Euth*')");
        checkActs(acts2, medication2);

        Iterable<Act> acts3 = (Iterable<Act>) ctx.getValue("history:medication(., 'Procedure')");
        checkActs(acts3);
    }

    /**
     * Tests the {@link HistoryFunctions#medicationByProductType(Party, String, Date)} method.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testMedicationByProductTypeAndDate() {
        Entity productType1 = createProductType("Vaccination");
        Entity productType2 = createProductType("Euthanasia");
        Product product1 = createProduct(productType1);
        Product product2 = createProduct(productType2);

        Act medication1 = PatientTestHelper.createMedication(patient, product1);
        Act medication2 = PatientTestHelper.createMedication(patient, product1);
        Act medication3 = PatientTestHelper.createMedication(patient, product2);
        medication1.setActivityStartTime(TestHelper.getDatetime("2013-09-19 00:00:00"));
        medication2.setActivityStartTime(TestHelper.getDatetime("2013-09-19 23:59:00"));
        medication3.setActivityStartTime(TestHelper.getDate("2013-09-20"));
        save(medication1, medication2, medication3);

        JXPathContext ctx = createContext(patient);
        Iterable<Act> acts1 = (Iterable<Act>) ctx.getValue("history:medication(., 'Vaccination', null)");
        checkActs(acts1);

        Iterable<Act> acts2 = (Iterable<Act>) ctx.getValue(
                "history:medication(., 'Vacc*', java.sql.Date.valueOf('2013-09-19'))");
        checkActs(acts2, medication2, medication1);

        Iterable<Act> acts3 = (Iterable<Act>) ctx.getValue(
                "history:medication(., 'Vaccination', java.sql.Date.valueOf('2013-09-20'))");
        checkActs(acts3);
    }

    /**
     * Tests the {@link HistoryFunctions#medicationByProductType(Party, String, Date, Date)} method.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testMedicationByProductTypeAndDateRange() {
        Entity productType1 = createProductType("Vaccination");
        Entity productType2 = createProductType("Euthanasia");
        Product product1 = createProduct(productType1);
        Product product2 = createProduct(productType2);

        Act medication1 = PatientTestHelper.createMedication(patient, product1);
        Act medication2 = PatientTestHelper.createMedication(patient, product2);
        medication1.setActivityStartTime(TestHelper.getDate("2013-09-19"));
        medication2.setActivityStartTime(TestHelper.getDate("2013-09-20"));
        save(medication1, medication2);

        JXPathContext ctx = createContext(patient);
        Iterable<Act> acts1 = (Iterable<Act>) ctx.getValue("history:medication(., 'Vaccination', null, null)");
        checkActs(acts1, medication1);

        Iterable<Act> acts2 = (Iterable<Act>) ctx.getValue(
                "history:medication(., 'Vacc*', java.sql.Date.valueOf('2013-09-19'), "
                + "java.sql.Date.valueOf('2013-09-20'))");
        checkActs(acts2, medication1);

        Iterable<Act> acts3 = (Iterable<Act>) ctx.getValue(
                "history:medication(., 'Vaccination', java.sql.Date.valueOf('2013-09-01'), "
                + "java.sql.Date.valueOf('2013-09-18'))");
        checkActs(acts3);
    }

    /**
     * Tests the medication methods when a null patient is supplied.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testMedicationWithNullPatient() {
        Act event = (Act) create(PatientArchetypes.CLINICAL_EVENT);
        JXPathContext ctx = createContext(event);
        Iterable<Act> acts1 = (Iterable<Act>) ctx.getValue("history:medication(openvpms:get(., 'patient.entity'))");
        checkActs(acts1);

        Iterable<Act> acts2 = (Iterable<Act>) ctx.getValue(
                "history:medication(openvpms:get(., 'patient.entity'), null, null)");
        checkActs(acts2);

        Iterable<Act> acts3 = (Iterable<Act>) ctx.getValue(
                "history:medication(openvpms:get(., 'patient.entity'), 'Vaccination')");
        checkActs(acts3);

        Iterable<Act> acts4 = (Iterable<Act>) ctx.getValue(
                "history:medication(openvpms:get(., 'patient.entity'), 'Vaccination', null, null)");
        checkActs(acts4);
    }

    /**
     * Verifies that a set of acts match those expected
     *
     * @param acts     the acts to check
     * @param expected the expected acts
     */
    private void checkActs(Iterable<Act> acts, Act... expected) {
        assertNotNull(acts);
        Iterator iterator = acts.iterator();
        for (Act act : expected) {
            assertTrue(iterator.hasNext());
            assertEquals(act, iterator.next());
        }
        assertFalse(iterator.hasNext());
    }

    /**
     * Helper to create an invoice item.
     *
     * @return a new invoice item
     */
    protected FinancialAct createInvoiceItem() {
        return FinancialTestHelper.createChargeItem(CustomerAccountArchetypes.INVOICE_ITEM, patient,
                                                    TestHelper.createProduct(), ONE);
    }

    /**
     * Helper to create an invoice item.
     *
     * @param product the product
     * @return a new invoice item
     */
    protected FinancialAct createInvoiceItem(Product product) {
        return FinancialTestHelper.createChargeItem(CustomerAccountArchetypes.INVOICE_ITEM, patient, product, ONE);
    }

    /**
     * Helper to create a product linked to a product type.
     *
     * @param productType the product type
     * @return a new product
     */
    private Product createProduct(Entity productType) {
        return ProductTestHelper.createMedication(productType);
    }

    /**
     * Helper to create a new product type.
     *
     * @param name the product type name
     * @return a new product type
     */
    private Entity createProductType(String name) {
        return ProductTestHelper.createProductType(name);
    }

    /**
     * Creates a new {@code JXPathContext} with the history functions registered.
     *
     * @param object the context object
     * @return a new JXPath context
     */
    private JXPathContext createContext(IMObject object) {
        HistoryFunctions history = new HistoryFunctions(getArchetypeService());
        ArchetypeServiceFunctions functions = new ArchetypeServiceFunctions(getArchetypeService(), getLookupService());
        FunctionLibrary library = new FunctionLibrary();
        library.addFunctions(history);
        library.addFunctions(new ObjectFunctions(functions, "openvpms"));
        return JXPathHelper.newContext(object, library);
    }

}
