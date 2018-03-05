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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.mr.prescription;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.finance.account.FinancialTestHelper;
import org.openvpms.archetype.rules.finance.discount.DiscountRules;
import org.openvpms.archetype.rules.finance.discount.DiscountTestHelper;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.patient.prescription.PrescriptionTestHelper;
import org.openvpms.archetype.rules.product.ProductTestHelper;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.workspace.customer.charge.AbstractCustomerChargeActEditorTest;
import org.openvpms.web.workspace.customer.charge.CustomerChargeActEditor;
import org.openvpms.web.workspace.customer.charge.CustomerChargeActItemEditor;
import org.openvpms.web.workspace.customer.charge.CustomerChargeTestHelper;
import org.openvpms.web.workspace.customer.charge.DefaultCustomerChargeActEditor;
import org.openvpms.web.workspace.customer.charge.EditorQueue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.openvpms.archetype.rules.act.ActStatus.IN_PROGRESS;
import static org.openvpms.web.workspace.customer.charge.CustomerChargeTestHelper.createFixedPrice;
import static org.openvpms.web.workspace.customer.charge.CustomerChargeTestHelper.createUnitPrice;

/**
 * Tests the {@link PrescriptionDispenser}.
 *
 * @author Tim Anderson
 */
public class PrescriptionDispenserTestCase extends AbstractCustomerChargeActEditorTest {

    /**
     * The context.
     */
    private Context context;

    /**
     * Test test patient.
     */
    private Party patient;

    /**
     * The test customer.
     */
    private Party customer;

    /**
     * The test clinician.
     */
    private User clinician;

    /**
     * The test user.
     */
    private User author;

    /**
     * The test product.
     */
    private Product product;

    /**
     * The unit cost.
     */
    private BigDecimal unitCost;

    /**
     * The test prescription.
     */
    private Act prescription;

    /**
     * The dispense completion listener.
     */
    private Runnable completionListener;

    /**
     * The number of times the dispense completion listener has been invoked.
     */
    private int completionCount;

    /**
     * Collects errors.
     */
    private final List<String> errors = new ArrayList<>();


    /**
     * Sets up the test case.
     */
    @Override
    @Before
    public void setUp() {
        super.setUp();
        context = new LocalContext();

        customer = TestHelper.createCustomer();
        patient = TestHelper.createPatient(customer);
        author = TestHelper.createUser();
        clinician = TestHelper.createClinician();

        context.setPractice(getPractice());
        context.setLocation(TestHelper.createLocation());
        context.setUser(author);
        context.setClinician(clinician);

        product = ProductTestHelper.createMedication();
        BigDecimal fixedPriceExTax = BigDecimal.valueOf(3);
        BigDecimal unitPriceExTax = new BigDecimal("0.10");
        unitCost = new BigDecimal("0.05");
        product.addProductPrice(createFixedPrice(ZERO, fixedPriceExTax));
        product.addProductPrice(createUnitPrice(unitCost, unitPriceExTax));
        save(product);

        Entity discountType = DiscountTestHelper.createDiscount(BigDecimal.valueOf(50), true, DiscountRules.PERCENTAGE);
        addDiscount(patient, discountType);
        addDiscount(product, discountType);

        prescription = PrescriptionTestHelper.createPrescription(patient, product, clinician, 10);

        // register an ErrorHandler to collect errors
        initErrorHandler(errors);

        completionListener = () -> completionCount++;
    }

    /**
     * Tests dispensing a prescription of quantity=10, where the product and patient both have a 50% discount.
     */
    @Test
    public void testDispense() {
        TestDispenser dispenser = new TestDispenser(context);
        dispenser.dispense(prescription, customer, completionListener);

        checkInvoice(dispenser);

        assertEquals(1, completionCount);
        assertTrue(errors.isEmpty());
    }

    /**
     * Verifies that dispensing adds to an existing IN_PROGRESS invoice.
     */
    @Test
    public void testDispenseAddsToExistingInvoice() {
        List<FinancialAct> existing = FinancialTestHelper.createChargesInvoice(customer, clinician, IN_PROGRESS);
        save(existing);

        TestDispenser dispenser = new TestDispenser(context);
        dispenser.dispense(prescription, customer, completionListener);

        FinancialAct invoice = dispenser.getInvoice();
        assertNotNull(invoice);
        assertEquals(existing.get(0), invoice);

        assertEquals(1, completionCount);
        assertTrue(errors.isEmpty());
    }

    /**
     * Verifies that a deactivated product can't be dispensed.
     */
    @Test
    public void testDispenseDeactivatedProduct() {
        product.setActive(false);
        save(product);

        TestDispenser dispenser = new TestDispenser(context);
        dispenser.dispense(prescription, customer, completionListener);

        FinancialAct invoice = dispenser.getInvoice();
        assertNull(invoice);

        assertEquals(0, completionCount);
        assertEquals(1, errors.size());
        assertEquals("The medication cannot be dispensed.", errors.get(0));
    }

    /**
     * Verifies that only those products at the practice can be dispensed, when the {@code useLocationProducts} option
     * is enabled.
     */
    @Test
    public void testDispenseForProductNotAtLocation() {
        IMObjectBean practice = new IMObjectBean(getPractice());
        practice.setValue("useLocationProducts", true);

        TestDispenser dispenser = new TestDispenser(context);
        dispenser.dispense(prescription, customer, completionListener);

        // verify the product wasn't dispensed
        FinancialAct invoice = dispenser.getInvoice();
        assertNull(invoice);
        assertEquals(0, completionCount);
        assertEquals(1, errors.size());
        assertEquals(product.getName() + " cannot be dispensed at this location.", errors.get(0));

        // now add a stock location linked to the location
        Party stockLocation = ProductTestHelper.createStockLocation(context.getLocation());
        IMObjectBean bean = new IMObjectBean(product);
        bean.addTarget("stockLocations", stockLocation);
        bean.save();

        // verify it dispenses
        errors.clear();
        dispenser.dispense(prescription, customer, completionListener);
        assertNotNull(dispenser.getInvoice());
        assertEquals(1, completionCount);
        assertTrue(errors.isEmpty());
    }

    /**
     * Verifies that only those products at the invoice location can be dispensed, when
     * the {@code useLocationProducts} option is enabled.
     */
    @Test
    public void testDispenseForProductNotAtInvoiceLocation() {
        IMObjectBean practice = new IMObjectBean(getPractice());
        practice.setValue("useLocationProducts", true);

        List<FinancialAct> existing = FinancialTestHelper.createChargesInvoice(customer, clinician, IN_PROGRESS);
        IMObjectBean bean = new IMObjectBean(existing.get(0)); // give the invoice a location
        Party location2 = TestHelper.createLocation();
        bean.setTarget("location", location2);
        bean.save();

        TestDispenser dispenser = new TestDispenser(context);
        dispenser.dispense(prescription, customer, completionListener);

        // verify the product wasn't dispensed
        FinancialAct invoice = dispenser.getInvoice();
        assertNull(invoice);
        assertEquals(0, completionCount);
        assertEquals(1, errors.size());
        assertEquals(product.getName() + " cannot be dispensed at the invoice location, " + location2.getName() + ".",
                     errors.get(0));
    }

    /**
     * Verifies that dispensing can be performed to an existing editor.
     */
    @Test
    public void testDispenseToChargeEditor() {
        DefaultLayoutContext layout = new DefaultLayoutContext(context, new HelpContext("foo", null));

        List<FinancialAct> acts = FinancialTestHelper.createChargesInvoice(customer, clinician, IN_PROGRESS);

        CustomerChargeActEditor editor = new DefaultCustomerChargeActEditor(acts.get(0), null, layout, false);
        editor.getComponent();
        TestDispenser dispenser = new TestDispenser(context);
        dispenser.dispense(prescription, editor, completionListener);

        checkInvoice(dispenser);

        assertTrue(errors.isEmpty());
        assertEquals(1, completionCount);
    }

    /**
     * Verifies the invoice matches that expected.
     *
     * @param dispenser the dispenser
     */
    protected void checkInvoice(TestDispenser dispenser) {
        FinancialAct invoice = dispenser.getInvoice();
        assertNotNull(invoice);
        List<FinancialAct> items = new IMObjectBean(invoice).getTargets("items", FinancialAct.class);
        assertEquals(1, items.size());

        BigDecimal fixedPrice = new BigDecimal("3.30");
        BigDecimal unitPrice = new BigDecimal("0.11");
        BigDecimal total = new BigDecimal("2.20");
        BigDecimal discount = new BigDecimal("2.20");
        BigDecimal tax = new BigDecimal("0.20");

        checkItem(items.get(0), patient, product, null, author, clinician, ZERO, TEN, unitCost, unitPrice, ZERO,
                  fixedPrice, discount, tax, total);

        checkCharge(invoice, customer, author, clinician, tax, total);
    }

    private static class TestDispenser extends PrescriptionDispenser {

        private FinancialAct invoice;

        /**
         * Constructs a {@link TestDispenser}.
         *
         * @param context the context
         */
        public TestDispenser(Context context) {
            super(context, new HelpContext("foo", null));
        }

        /**
         * Returns the invoice.
         *
         * @return the invoice
         */
        public FinancialAct getInvoice() {
            return invoice;
        }

        /**
         * Dispenses a prescription.
         *
         * @param prescription       the prescription state
         * @param editor             the charge editor
         * @param completionListener the listener to notify on successful completion. May be {@code null}
         * @return the charge item editor
         */
        @Override
        protected CustomerChargeActItemEditor dispense(Prescription prescription, CustomerChargeActEditor editor,
                                                       Runnable completionListener) {
            invoice = editor.getObject();
            CustomerChargeActItemEditor item = super.dispense(prescription, editor, completionListener);
            EditorQueue editorQueue = item.getEditorQueue();
            CustomerChargeTestHelper.checkSavePopup(editorQueue, PatientArchetypes.PATIENT_MEDICATION, false);
            return item;
        }
    }
}
