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

package org.openvpms.web.workspace.customer.order;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.finance.order.OrderRules;
import org.openvpms.archetype.rules.math.WeightUnits;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.patient.PatientTestHelper;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.product.ProductTestHelper;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.DefaultValidator;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.workspace.customer.charge.AbstractCustomerChargeActEditorTest;
import org.openvpms.web.workspace.customer.charge.CustomerChargeActEditDialog;
import org.openvpms.web.workspace.customer.charge.CustomerChargeActEditor;
import org.openvpms.web.workspace.customer.charge.CustomerChargeActItemEditor;
import org.openvpms.web.workspace.customer.charge.CustomerChargeTestHelper;
import org.openvpms.web.workspace.customer.charge.TestChargeEditor;
import org.openvpms.web.workspace.customer.charge.TestPharmacyOrderService;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.openvpms.web.workspace.customer.order.PharmacyTestHelper.createOrder;
import static org.openvpms.web.workspace.customer.order.PharmacyTestHelper.createReturn;

/**
 * Tests the {@link PharmacyOrderInvoicer}.
 *
 * @author Tim Anderson
 */
public class PharmacyOrderInvoicerTestCase extends AbstractCustomerChargeActEditorTest {

    /**
     * The order rules.
     */
    private OrderRules rules;

    /**
     * The context.
     */
    private Context context;

    /**
     * The customer.
     */
    private Party customer;

    /**
     * The patient.
     */
    private Party patient;

    /**
     * The author.
     */
    private User author;

    /**
     * The clinician.
     */
    private User clinician;

    /**
     * The product.
     */
    private Product product;

    /**
     * The fixed price, including tax.
     */
    private BigDecimal fixedPriceIncTax;

    /**
     * The unit price, including tax.
     */
    private BigDecimal unitPriceIncTax;

    /**
     * Sets up the test case.
     */
    @Override
    @Before
    public void setUp() {
        super.setUp();
        BigDecimal fixedPrice = new BigDecimal("1.82");
        BigDecimal unitPrice = new BigDecimal("9.09");
        fixedPriceIncTax = BigDecimal.valueOf(2);
        unitPriceIncTax = TEN;

        // create a product linked to a pharmacy
        Party location = TestHelper.createLocation();
        Entity pharmacy = CustomerChargeTestHelper.createPharmacy(location);
        product = createProduct(ProductArchetypes.MEDICATION, fixedPrice, unitPrice);
        IMObjectBean productBean = new IMObjectBean(product);
        productBean.setValue("concentration", ONE);

        ProductTestHelper.addPharmacy(product, pharmacy);

        // add a dose to the product. This should always be overridden
        Entity dose = ProductTestHelper.createDose(null, BigDecimal.ZERO, TEN, TEN,
                                                   BigDecimal.ONE);
        ProductTestHelper.addDose(product, dose);

        clinician = TestHelper.createClinician();
        customer = TestHelper.createCustomer();
        patient = TestHelper.createPatient(customer);
        PatientTestHelper.createWeight(patient, BigDecimal.ONE, WeightUnits.KILOGRAMS);

        context = new LocalContext();
        context.setPractice(getPractice());

        context.setLocation(location);
        author = TestHelper.createUser();
        context.setUser(author);
        context.setClinician(clinician);
        context.setPatient(patient);
        context.setCustomer(customer);
        rules = new OrderRules(getArchetypeService());
    }

    /**
     * Tests charging an order that isn't linked to an existing invoice.
     * <p>
     * This should create a new invoice.
     */
    @Test
    public void testChargeUnlinkedOrder() {
        BigDecimal quantity = BigDecimal.valueOf(2);
        BigDecimal tax = BigDecimal.valueOf(2);
        BigDecimal total = new BigDecimal("22");
        Date startTime = TestHelper.getDate("2017-07-01");
        setProductPriceDates(startTime);

        FinancialAct order = createOrder(startTime, customer, patient, product, quantity, null);
        PharmacyOrderInvoicer charger = new TestPharmacyOrderInvoicer(order, rules);
        assertTrue(charger.isValid());
        assertTrue(charger.canInvoice());
        assertFalse(charger.canCredit());

        Date now = new Date();
        DefaultLayoutContext layoutContext = new DefaultLayoutContext(context, new HelpContext("foo", null));
        layoutContext.setEdit(true);
        CustomerChargeActEditDialog dialog = charger.charge(null, null, layoutContext);
        TestChargeEditor editor = (TestChargeEditor) dialog.getEditor();
        CustomerChargeTestHelper.checkSavePopup(editor.getQueue(), PatientArchetypes.PATIENT_MEDICATION, false);
        assertTrue(SaveHelper.save(editor));
        FinancialAct charge = get(editor.getObject());
        assertTrue(TypeHelper.isA(charge, CustomerAccountArchetypes.INVOICE));
        ActBean item = checkItem(charge, patient, product, quantity, unitPriceIncTax, fixedPriceIncTax, tax, total,
                                 quantity, null);
        assertEquals(startTime, item.getDate("startTime")); // charge item should have same date as the order
        checkCharge(charge, customer, author, clinician, tax, total);
        assertTrue(DateRules.compareTo(charge.getActivityStartTime(), now, true) >= 0);
    }

    /**
     * Tests charging a return that isn't linked to an existing invoice.
     * <p>
     * This should create a Credit.
     */
    @Test
    public void testCreditUnlinkedReturn() {
        BigDecimal quantity = BigDecimal.valueOf(2);
        BigDecimal tax = BigDecimal.valueOf(2);
        BigDecimal total = new BigDecimal("22");

        Date startTime = TestHelper.getDate("2017-07-01");
        setProductPriceDates(startTime);

        FinancialAct orderReturn = createReturn(startTime, customer, patient, product, quantity, null);
        PharmacyOrderInvoicer charger = new TestPharmacyOrderInvoicer(orderReturn, rules);
        assertTrue(charger.isValid());
        assertTrue(charger.canCredit());
        assertTrue(charger.canInvoice());

        Date now = new Date();
        DefaultLayoutContext layoutContext = new DefaultLayoutContext(context, new HelpContext("foo", null));
        layoutContext.setEdit(true);
        CustomerChargeActEditDialog dialog = charger.charge(null, null, layoutContext);
        TestChargeEditor editor = (TestChargeEditor) dialog.getEditor();
        assertTrue(SaveHelper.save(editor));
        FinancialAct charge = get(editor.getObject());
        assertTrue(TypeHelper.isA(charge, CustomerAccountArchetypes.CREDIT));
        ActBean item = checkItem(charge, patient, product, quantity, unitPriceIncTax, fixedPriceIncTax, tax, total,
                                 null, null);
        assertEquals(startTime, item.getDate("startTime")); // charge item should have same date as the order
        checkCharge(charge, customer, author, clinician, tax, total);
        assertTrue(DateRules.compareTo(charge.getActivityStartTime(), now, true) >= 0);
    }

    /**
     * Tests charging an order that is linked to an existing IN_PROGRESS invoice.
     * <p>
     * The invoice quantity should remain the same, and the receivedQuantity updated.
     */
    @Test
    public void testChargeLinkedOrderWithSameQuantity() {
        BigDecimal quantity = BigDecimal.valueOf(2);
        BigDecimal tax = BigDecimal.valueOf(2);
        BigDecimal total = new BigDecimal("22");
        TestChargeEditor editor1 = createInvoice(product, quantity);
        editor1.setStatus(ActStatus.IN_PROGRESS);
        assertTrue(SaveHelper.save(editor1));

        FinancialAct invoice = editor1.getObject();
        FinancialAct invoiceItem = getInvoiceItem(editor1);

        FinancialAct order = createOrder(customer, patient, product, quantity, invoiceItem);
        PharmacyOrderInvoicer charger = new TestPharmacyOrderInvoicer(order, rules);
        assertTrue(charger.isValid());
        assertTrue(charger.canInvoice());
        assertFalse(charger.canCredit());

        DefaultLayoutContext layoutContext = new DefaultLayoutContext(context, new HelpContext("foo", null));
        layoutContext.setEdit(true);
        CustomerChargeActEditDialog dialog = charger.charge(invoice, null, layoutContext);
        TestChargeEditor editor2 = (TestChargeEditor) dialog.getEditor();
        assertTrue(SaveHelper.save(editor2));
        FinancialAct charge = get(editor2.getObject());
        assertEquals(invoice, charge);
        checkItem(charge, patient, product, quantity, unitPriceIncTax, fixedPriceIncTax, tax, total, quantity, null);
        checkCharge(charge, customer, author, clinician, tax, total);
    }

    /**
     * Tests charging an order that is linked to an existing IN_PROGRESS invoice, with a greater quantity than that
     * invoiced.
     * <p>
     * The invoice quantity should be updated.
     */
    @Test
    public void testChargeLinkedOrderWithGreaterQuantity() {
        BigDecimal originalQty = BigDecimal.valueOf(1);

        BigDecimal newQty = BigDecimal.valueOf(2);
        BigDecimal newTax = BigDecimal.valueOf(2);
        BigDecimal newTotal = new BigDecimal("22");

        TestChargeEditor editor1 = createInvoice(product, originalQty);
        editor1.setStatus(ActStatus.IN_PROGRESS);
        assertTrue(SaveHelper.save(editor1));

        FinancialAct invoice = editor1.getObject();
        FinancialAct invoiceItem = getInvoiceItem(editor1);

        FinancialAct order = createOrder(customer, patient, product, newQty, invoiceItem);
        PharmacyOrderInvoicer charger = new TestPharmacyOrderInvoicer(order, rules);
        assertTrue(charger.isValid());
        assertTrue(charger.canInvoice());
        assertFalse(charger.canCredit());

        DefaultLayoutContext layoutContext = new DefaultLayoutContext(context, new HelpContext("foo", null));
        layoutContext.setEdit(true);
        CustomerChargeActEditDialog dialog = charger.charge(invoice, null, layoutContext);
        TestChargeEditor editor2 = (TestChargeEditor) dialog.getEditor();
        assertTrue(SaveHelper.save(editor2));
        FinancialAct charge = get(editor2.getObject());
        assertEquals(invoice, charge);
        checkItem(charge, patient, product, newQty, unitPriceIncTax, fixedPriceIncTax, newTax, newTotal, newQty, null);
        checkCharge(charge, customer, author, clinician, newTax, newTotal);
    }

    /**
     * Tests charging an order that is linked to an existing POSTED invoice, with a greater quantity than that
     * invoiced.
     * <p>
     * A new invoice should be created with the difference between that invoiced and that ordered.
     */
    @Test
    public void testChargeLinkedOrderWithGreaterQuantityToPostedInvoice() {
        BigDecimal originalQty = BigDecimal.valueOf(1);
        BigDecimal newOrderQty = BigDecimal.valueOf(2);

        BigDecimal newQty = BigDecimal.valueOf(1);
        BigDecimal newTax = new BigDecimal("1.09");
        BigDecimal newTotal = BigDecimal.valueOf(12);

        TestChargeEditor editor1 = createInvoice(product, originalQty);
        editor1.setStatus(ActStatus.POSTED);
        assertTrue(SaveHelper.save(editor1));

        FinancialAct invoice = editor1.getObject();
        FinancialAct invoiceItem = getInvoiceItem(editor1);

        FinancialAct order = createOrder(customer, patient, product, newOrderQty, invoiceItem);
        PharmacyOrderInvoicer charger = new TestPharmacyOrderInvoicer(order, rules);
        assertTrue(charger.isValid());
        assertTrue(charger.canInvoice());
        assertFalse(charger.canCredit());

        DefaultLayoutContext layoutContext = new DefaultLayoutContext(context, new HelpContext("foo", null));
        layoutContext.setEdit(true);
        CustomerChargeActEditDialog dialog = charger.charge(null, null, layoutContext);
        TestChargeEditor editor = (TestChargeEditor) dialog.getEditor();
        CustomerChargeTestHelper.checkSavePopup(editor.getQueue(), PatientArchetypes.PATIENT_MEDICATION, false);
        TestChargeEditor editor2 = (TestChargeEditor) dialog.getEditor();
        assertTrue(SaveHelper.save(editor2));
        FinancialAct charge = get(editor2.getObject());
        assertNotEquals(invoice, charge);

        // NOTE: item tax not rounded.
        checkItem(charge, patient, product, newQty, unitPriceIncTax, fixedPriceIncTax, new BigDecimal("1.091"),
                  newTotal, newOrderQty, null);
        checkCharge(charge, customer, author, clinician, newTax, newTotal);
    }

    /**
     * Tests charging an order that is linked to an existing IN_PROGRESS invoice, with a lesser quantity than that
     * invoiced.
     * <p>
     * The invoice quantity should be updated.
     */
    @Test
    public void testChargeLinkedOrderWithLesserQuantityToInProgressInvoice() {
        BigDecimal originalQty = BigDecimal.valueOf(2);

        BigDecimal newQty = BigDecimal.valueOf(1);
        BigDecimal newTax = new BigDecimal("1.09");
        BigDecimal newTotal = BigDecimal.valueOf(12);

        TestChargeEditor editor1 = createInvoice(product, originalQty);
        editor1.setStatus(ActStatus.IN_PROGRESS);
        assertTrue(SaveHelper.save(editor1));

        FinancialAct invoice = editor1.getObject();
        FinancialAct invoiceItem = getInvoiceItem(editor1);

        FinancialAct order = createOrder(customer, patient, product, newQty, invoiceItem);
        PharmacyOrderInvoicer charger = new TestPharmacyOrderInvoicer(order, rules);
        assertTrue(charger.isValid());
        assertTrue(charger.canInvoice());
        assertTrue(charger.canCredit());

        DefaultLayoutContext layoutContext = new DefaultLayoutContext(context, new HelpContext("foo", null));
        layoutContext.setEdit(true);
        CustomerChargeActEditDialog dialog = charger.charge(invoice, null, layoutContext);
        TestChargeEditor editor2 = (TestChargeEditor) dialog.getEditor();
        assertTrue(SaveHelper.save(editor2));
        FinancialAct charge = get(editor2.getObject());
        assertTrue(TypeHelper.isA(charge, CustomerAccountArchetypes.INVOICE));

        // NOTE: item tax not rounded.
        checkItem(charge, patient, product, newQty, unitPriceIncTax, fixedPriceIncTax, new BigDecimal("1.091"), newTotal,
                  newQty, null);
        checkCharge(charge, customer, author, clinician, newTax, newTotal);
    }

    /**
     * Tests charging an order that is linked to an existing POSTED invoice, with a lesser quantity than that invoiced.
     * <p>
     * A new credit should be created with the difference between that invoiced and that ordered.
     */
    @Test
    public void testChargeLinkedOrderWithLesserQuantityToPostedInvoice() {
        BigDecimal originalQty = BigDecimal.valueOf(2);
        BigDecimal newQty = BigDecimal.valueOf(1);
        BigDecimal newTax = new BigDecimal("1.09");
        BigDecimal newTotal = BigDecimal.valueOf(12);

        TestChargeEditor editor1 = createInvoice(product, originalQty);
        editor1.setStatus(ActStatus.POSTED);
        assertTrue(SaveHelper.save(editor1));

        FinancialAct invoiceItem = getInvoiceItem(editor1);

        FinancialAct order = createOrder(customer, patient, product, newQty, invoiceItem);
        PharmacyOrderInvoicer charger = new TestPharmacyOrderInvoicer(order, rules);
        assertTrue(charger.isValid());
        assertFalse(charger.canInvoice());
        assertTrue(charger.canCredit());

        DefaultLayoutContext layoutContext = new DefaultLayoutContext(context, new HelpContext("foo", null));
        layoutContext.setEdit(true);
        CustomerChargeActEditDialog dialog = charger.charge(null, null, layoutContext);
        TestChargeEditor editor2 = (TestChargeEditor) dialog.getEditor();
        assertTrue(SaveHelper.save(editor2));
        FinancialAct charge = get(editor2.getObject());
        assertTrue(TypeHelper.isA(charge, CustomerAccountArchetypes.CREDIT));

        // NOTE: item tax not rounded.
        checkItem(charge, patient, product, newQty, unitPriceIncTax, fixedPriceIncTax, new BigDecimal("1.091"),
                  newTotal, newQty, null);
        checkCharge(charge, customer, author, clinician, newTax, newTotal);
    }

    /**
     * Tests applying multiple linked orders to an IN_PROGRESS invoice.
     */
    @Test
    public void testMultipleLinkedOrderToInProgressInvoice() {
        BigDecimal quantity = BigDecimal.valueOf(2);
        BigDecimal tax = BigDecimal.valueOf(2);
        BigDecimal total = new BigDecimal("22");

        TestChargeEditor editor = createInvoice(product, quantity);
        editor.setStatus(ActStatus.IN_PROGRESS);
        assertTrue(SaveHelper.save(editor));

        FinancialAct invoice = editor.getObject();
        FinancialAct invoiceItem = getInvoiceItem(editor);

        // charge two orders
        FinancialAct order1 = createOrder(customer, patient, product, ONE, invoiceItem);
        PharmacyOrderInvoicer charger1 = new TestPharmacyOrderInvoicer(order1, rules);
        assertTrue(charger1.canCharge(editor));
        charger1.charge(editor);

        FinancialAct order2 = createOrder(customer, patient, product, ONE, invoiceItem);
        PharmacyOrderInvoicer charger2 = new TestPharmacyOrderInvoicer(order2, rules);
        assertTrue(charger2.canCharge(editor));
        charger2.charge(editor);

        assertTrue(SaveHelper.save(editor));
        checkItem(invoice, patient, product, quantity, unitPriceIncTax, fixedPriceIncTax, tax, total, quantity, null);
        checkCharge(invoice, customer, author, clinician, tax, total);
    }

    /**
     * Tests applying an order and return for the same quantity to an IN_PROGRESS invoice.
     * <p>
     * This will set the invoice quantity to 0.
     */
    @Test
    public void testOrderAndReturnToInProgressInvoice() {
        TestChargeEditor editor = createInvoice(product, ONE);
        editor.setStatus(ActStatus.IN_PROGRESS);
        assertTrue(SaveHelper.save(editor));

        FinancialAct invoice = editor.getObject();
        FinancialAct invoiceItem = getInvoiceItem(editor);

        FinancialAct order = createOrder(customer, patient, product, ONE, invoiceItem);
        PharmacyOrderInvoicer charger1 = new TestPharmacyOrderInvoicer(order, rules);
        assertTrue(charger1.canCharge(editor));
        charger1.charge(editor);

        FinancialAct orderReturn = createReturn(customer, patient, product, ONE, invoiceItem);
        PharmacyOrderInvoicer charger2 = new TestPharmacyOrderInvoicer(orderReturn, rules);
        assertTrue(charger2.canCharge(editor));
        charger2.charge(editor);

        assertTrue(SaveHelper.save(editor));
        checkItem(invoice, patient, product, ZERO, unitPriceIncTax, fixedPriceIncTax, new BigDecimal("0.182"),
                  fixedPriceIncTax, ONE, ONE);
        checkCharge(invoice, customer, author, clinician, new BigDecimal("0.18"), fixedPriceIncTax);
    }

    /**
     * Tests charging an order that is linked to an existing POSTED invoice, with a the same quantity that was
     * invoiced.
     * <p>
     * No new invoice should be created.
     */
    @Test
    public void testOrderToPostedInvoice() {
        BigDecimal quantity = BigDecimal.valueOf(2);
        BigDecimal tax = BigDecimal.valueOf(2);
        BigDecimal total = new BigDecimal("22");

        TestChargeEditor editor = createInvoice(product, quantity);
        editor.setStatus(ActStatus.POSTED);
        assertTrue(SaveHelper.save(editor));
        FinancialAct invoiceItem = getInvoiceItem(editor);

        checkItem(editor.getObject(), patient, product, quantity, unitPriceIncTax, fixedPriceIncTax, tax, total, null,
                  null);

        FinancialAct order1 = createOrder(customer, patient, product, quantity, invoiceItem);
        PharmacyOrderInvoicer charger1 = new TestPharmacyOrderInvoicer(order1, rules);
        assertTrue(charger1.isValid());
        assertTrue(charger1.canInvoice());
        assertFalse(charger1.canCredit());
        assertFalse(charger1.requiresEdit());
        charger1.charge();

        checkItem(editor.getObject(), patient, product, quantity, unitPriceIncTax, fixedPriceIncTax, tax, total,
                  quantity, null);

        // verify that if another order for the same item is created, editing is required, as it would exceed the
        // original order quantity.
        FinancialAct order2 = createOrder(customer, patient, product, quantity, invoiceItem);
        PharmacyOrderInvoicer charger2 = new TestPharmacyOrderInvoicer(order2, rules);
        assertTrue(charger2.isValid());
        assertTrue(charger2.canInvoice());
        assertFalse(charger2.canCredit());
        assertTrue(charger2.requiresEdit());

        // verify that a return requires editing
        FinancialAct return1 = createReturn(customer, patient, product, quantity, invoiceItem);
        PharmacyOrderInvoicer charger3 = new TestPharmacyOrderInvoicer(return1, rules);
        assertTrue(charger3.isValid());
        assertFalse(charger3.canInvoice());
        assertTrue(charger3.canCredit());
        assertTrue(charger3.requiresEdit());
    }

    /**
     * Verifies that a return for a POSTED invoice creates a new Credit.
     */
    @Test
    public void testReturnToPostedInvoice() {
        BigDecimal quantity = BigDecimal.valueOf(2);
        BigDecimal tax = BigDecimal.valueOf(2);
        BigDecimal total = new BigDecimal("22");

        TestChargeEditor editor1 = createInvoice(product, quantity);
        editor1.setStatus(ActStatus.IN_PROGRESS);
        assertTrue(SaveHelper.save(editor1));

        FinancialAct invoiceItem = getInvoiceItem(editor1);

        FinancialAct order = createOrder(customer, patient, product, quantity, invoiceItem);
        PharmacyOrderInvoicer charger1 = new TestPharmacyOrderInvoicer(order, rules);
        assertTrue(charger1.canCharge(editor1));
        charger1.charge(editor1);

        editor1.setStatus(ActStatus.POSTED);
        assertTrue(SaveHelper.save(editor1));

        // now return the same quantity, and verify a Credit is created
        FinancialAct orderReturn = createReturn(customer, patient, product, quantity, invoiceItem);
        PharmacyOrderInvoicer charger2 = new TestPharmacyOrderInvoicer(orderReturn, rules);
        assertFalse(charger2.canCharge(editor1));
        DefaultLayoutContext layoutContext = new DefaultLayoutContext(context, new HelpContext("foo", null));
        layoutContext.setEdit(true);
        CustomerChargeActEditDialog dialog = charger2.charge(null, null, layoutContext);
        TestChargeEditor editor2 = (TestChargeEditor) dialog.getEditor();
        assertTrue(SaveHelper.save(editor2));
        FinancialAct charge = get(editor2.getObject());
        assertTrue(TypeHelper.isA(charge, CustomerAccountArchetypes.CREDIT));
        checkItem(charge, patient, product, quantity, unitPriceIncTax, fixedPriceIncTax, tax, total, null, null);
        checkCharge(charge, customer, author, clinician, tax, total);
    }

    /**
     * Verifies that if an existing order is charged and the created invoice item deleted prior to being saved,
     * no cancellation is generated.
     */
    @Test
    public void testRemoveUnsavedInvoiceItemLinkedToOrder() {
        BigDecimal quantity = BigDecimal.valueOf(2);

        TestChargeEditor editor = createEditor();
        editor.setStatus(ActStatus.IN_PROGRESS);

        FinancialAct order = createOrder(customer, patient, product, quantity, null); // not linked to an invoice item

        // charge the order
        PharmacyOrderInvoicer charger = new TestPharmacyOrderInvoicer(order, rules);
        assertTrue(charger.canCharge(editor));
        charger.charge(editor);

        // remove the new item
        List<Act> items = editor.getItems().getCurrentActs();
        assertEquals(1, items.size());
        editor.getItems().remove(items.get(0));

        assertTrue(SaveHelper.save(editor));

        // verify no orders were submitted
        TestPharmacyOrderService pharmacyOrderService = editor.getPharmacyOrderService();
        assertTrue(pharmacyOrderService.getOrders().isEmpty());

        // verifies that the unsaved order is POSTED, but that the saved version is still IN_PROGRESS
        assertEquals(ActStatus.POSTED, order.getStatus());
        order = get(order);
        assertEquals(ActStatus.IN_PROGRESS, order.getStatus());
    }

    /**
     * Verifies a validation error is produced if an order or return is missing a customer.
     */
    @Test
    public void testMissingCustomer() {
        String expected = "Customer is required";
        FinancialAct act1 = createOrder(null, patient, product, ONE, null);
        checkRequired(act1, expected);

        FinancialAct act2 = createReturn(null, patient, product, ONE, null);
        checkRequired(act2, expected);
    }

    /**
     * Verifies a validation error is produced if an order or return is missing a patient.
     */
    @Test
    public void testMissingPatient() {
        String expected = "Patient is required";
        FinancialAct act1 = createOrder(customer, null, product, ONE, null);
        checkRequired(act1, expected);

        FinancialAct act2 = createReturn(customer, null, product, ONE, null);
        checkRequired(act2, expected);
    }

    /**
     * Verifies a validation error is produced if an order or return is missing a product.
     */
    @Test
    public void testMissingProduct() {
        String expected = "Product is required";
        FinancialAct act1 = createOrder(customer, patient, null, ONE, null);
        checkRequired(act1, expected);

        FinancialAct act2 = createReturn(customer, patient, null, ONE, null);
        checkRequired(act2, expected);
    }

    /**
     * Verifies a validation error is produced if an order or return has an invalid product.
     */
    @Test
    public void testInvalidProduct() {
        String expected = "Product does not exist or is not valid for this field";
        Product template = ProductTestHelper.createTemplate("ZTemplate");
        FinancialAct act1 = createOrder(customer, patient, template, ONE, null);
        checkRequired(act1, expected);

        FinancialAct act2 = createReturn(customer, patient, template, ONE, null);
        checkRequired(act2, expected);
    }

    /**
     * Tests applying order returns to an invoice where the order returns aren't related to any invoice item.
     */
    @Test
    public void testApplyUnlinkedOrderReturnsToInvoice() {
        BigDecimal fixedPrice = new BigDecimal("1.82");
        BigDecimal unitPrice = new BigDecimal("9.09");
        Product product = createProduct(ProductArchetypes.MEDICATION, fixedPrice, unitPrice);

        BigDecimal five = BigDecimal.valueOf(5);
        FinancialAct act1 = createReturn(customer, patient, product, five, null); // not linked to an invoice item
        TestChargeEditor editor = createEditor();
        editor.setStatus(ActStatus.IN_PROGRESS);
        PharmacyOrderInvoicer invoicer1 = new TestPharmacyOrderInvoicer(act1, rules);
        OrderInvoicer.Status status1 = invoicer1.getChargeStatus(editor);
        assertFalse(status1.canCharge());
        String message1 = Messages.format("customer.order.return.notinvoiced", act1.getId(), "Pharmacy Return",
                                          "Invoice", product.getName());
        assertEquals(message1, status1.getReason());

        // now add one of the product to the invoice
        CustomerChargeActItemEditor itemEditor = addItem(editor, patient, product, BigDecimal.ONE, editor.getQueue());

        OrderInvoicer.Status status2 = invoicer1.getChargeStatus(editor);
        assertFalse(status2.canCharge());
        String message2 = Messages.format("customer.order.return.qtyexceeded", act1.getId(), "Pharmacy Return",
                                          "Invoice", product.getName());
        assertEquals(message2, status2.getReason());

        itemEditor.setQuantity(BigDecimal.valueOf(6));
        OrderInvoicer.Status status3 = invoicer1.getChargeStatus(editor);
        assertTrue(status3.canCharge());
        assertNull(status3.getReason());

        invoicer1.charge(editor);
        assertEquals(ActStatus.POSTED, act1.getStatus());

        assertTrue(SaveHelper.save(editor));
        FinancialAct charge1 = get(editor.getObject());
        assertTrue(TypeHelper.isA(charge1, CustomerAccountArchetypes.INVOICE));
        BigDecimal tax = new BigDecimal("1.091");
        BigDecimal total = BigDecimal.valueOf(12);
        checkItem(charge1, patient, product, ONE, unitPriceIncTax, fixedPriceIncTax, tax, total, null, null);
        checkCharge(charge1, customer, author, clinician, new BigDecimal("1.09"), total);

        // now do another return this time reducing the quantity to zero. This should remove the line item.
        FinancialAct act2 = createReturn(customer, patient, product, ONE, null);
        PharmacyOrderInvoicer invoicer2 = new TestPharmacyOrderInvoicer(act2, rules);
        invoicer2.charge(editor);
        assertEquals(ActStatus.POSTED, act2.getStatus());

        assertTrue(SaveHelper.save(editor));
        FinancialAct charge2 = get(editor.getObject());
        assertEquals(0, new IMObjectBean(charge2).getValues("items").size()); // no items
        checkCharge(charge2, customer, author, clinician, ZERO, ZERO);
    }

    /**
     * Verifies that when an order return that is not related to any invoice item is applied to an invoice item with a
     * mininum quantity, the invoice item is not deleted when its quantity falls to zero.
     */
    @Test
    public void testApplyUnlinkedOrderReturnsToInvoiceWithMinimumQuantity() {
        BigDecimal fixedPrice = new BigDecimal("1.82");
        BigDecimal unitPrice = new BigDecimal("9.09");
        Product product = createProduct(ProductArchetypes.MEDICATION, fixedPrice, unitPrice);

        TestChargeEditor editor = createEditor();
        editor.setStatus(ActStatus.IN_PROGRESS);

        // now add one of the product to the invoice, and set the minimum quantity to one
        CustomerChargeActItemEditor itemEditor = addItem(editor, patient, product, ONE, editor.getQueue());
        itemEditor.setMinimumQuantity(ONE);
        assertTrue(SaveHelper.save(editor));

        // create a return for the product, and invoice it
        FinancialAct act1 = createReturn(customer, patient, product, ONE, null); // not linked to an invoice item
        PharmacyOrderInvoicer invoicer1 = new TestPharmacyOrderInvoicer(act1, rules);
        OrderInvoicer.Status status1 = invoicer1.getChargeStatus(editor);
        assertTrue(status1.canCharge());
        invoicer1.charge(editor);
        assertEquals(ActStatus.POSTED, act1.getStatus());

        // shouldn't be able to save the item, as its quantity is now zero
        checkEquals(ZERO, itemEditor.getQuantity());
        assertFalse(SaveHelper.save(editor));

        // set the minimum quantity to allow the invoice to save
        itemEditor.setMinimumQuantity(ZERO);
        assertTrue(SaveHelper.save(editor));
    }


    /**
     * Verifies that a validation error is raised if a required field is missing.
     * <p>
     * Validation cannot occur using the archetype as as the delivery processor must be able to save incomplete/invalid
     * orders and returns.
     *
     * @param act      the order/return
     * @param expected the expected validation error
     */
    private void checkRequired(FinancialAct act, String expected) {
        PharmacyOrderInvoicer charger = new TestPharmacyOrderInvoicer(act, rules);
        assertFalse(charger.isValid());
        Validator validator = new DefaultValidator();
        assertFalse(charger.validate(validator));
        assertEquals(1, validator.getInvalid().size());
        Modifiable modifiable = validator.getInvalid().iterator().next();
        List<ValidatorError> errors = validator.getErrors(modifiable);
        assertEquals(1, errors.size());
        assertEquals(expected, errors.get(0).getMessage());
    }

    /**
     * Creates a new invoice in an editor.
     *
     * @param product  the product to invoice
     * @param quantity the quantity to invoice
     * @return the invoice editor
     */
    private TestChargeEditor createInvoice(Product product, BigDecimal quantity) {
        TestChargeEditor editor = createEditor();
        addItem(editor, patient, product, quantity, editor.getQueue());
        return editor;
    }

    /**
     * Creates a new invoice editor.
     *
     * @return a new editor
     */
    private TestChargeEditor createEditor() {
        FinancialAct charge = (FinancialAct) create(CustomerAccountArchetypes.INVOICE);
        LayoutContext layoutContext = new DefaultLayoutContext(context, new HelpContext("foo", null));
        layoutContext.setEdit(true);
        TestChargeEditor editor = new TestChargeEditor(charge, layoutContext, false);
        editor.getComponent();
        return editor;
    }

    /**
     * Returns the invoice item from an invoice editor.
     *
     * @param editor the editor
     * @return the invoice item
     */
    private FinancialAct getInvoiceItem(TestChargeEditor editor) {
        List<Act> acts = editor.getItems().getCurrentActs();
        assertEquals(1, acts.size());
        return (FinancialAct) acts.get(0);
    }

    /**
     * Verifies the charge has an item with the expected details.
     *
     * @param charge           the charge
     * @param patient          the expected patient
     * @param product          the expected product
     * @param quantity         the expected quantity
     * @param unitPrice        the expected unit price
     * @param fixedPrice       the expected fixed price
     * @param tax              the expected tax
     * @param total            the expected total
     * @param receivedQuantity the expected received quantity
     * @param returnedQuantity the expected returned quantity. May be {@code null}
     * @return the item
     */
    private ActBean checkItem(FinancialAct charge, Party patient, Product product, BigDecimal quantity,
                              BigDecimal unitPrice, BigDecimal fixedPrice, BigDecimal tax, BigDecimal total,
                              BigDecimal receivedQuantity, BigDecimal returnedQuantity) {
        ActBean bean = new ActBean(charge);
        List<FinancialAct> items = bean.getNodeActs("items", FinancialAct.class);
        assertEquals(1, items.size());

        int childActs = TypeHelper.isA(charge, CustomerAccountArchetypes.INVOICE) ? 1 : 0;
        // for invoices, there should be a medication act

        ActBean itemBean = checkItem(items, patient, product, quantity, unitPrice, fixedPrice, tax, total, childActs);
        if (bean.isA(CustomerAccountArchetypes.INVOICE)) {
            checkEquals(receivedQuantity, itemBean.getBigDecimal("receivedQuantity"));
            checkEquals(returnedQuantity, itemBean.getBigDecimal("returnedQuantity"));
        }
        return itemBean;
    }

    /**
     * Verifies an item's properties match that expected.
     *
     * @param items      the items to search
     * @param patient    the expected patient
     * @param product    the expected product
     * @param quantity   the expected quantity
     * @param unitPrice  the expected unit price
     * @param fixedPrice the expected fixed price
     * @param tax        the expected tax
     * @param total      the expected total
     * @param childActs  the expected no. of child acts
     * @return the item
     */
    private ActBean checkItem(List<FinancialAct> items, Party patient, Product product, BigDecimal quantity,
                              BigDecimal unitPrice, BigDecimal fixedPrice, BigDecimal tax, BigDecimal total,
                              int childActs) {
        return checkItem(items, patient, product, null, author, clinician, ZERO, quantity, ZERO, unitPrice, ZERO,
                         fixedPrice, ZERO, tax, total, null, childActs);
    }

    /**
     * Sets the product price dates.
     *
     * @param startTime the start time
     */
    private void setProductPriceDates(Date startTime) {
        // back-date the product price from dates so that they will be charged.
        for (org.openvpms.component.model.product.ProductPrice prices : product.getProductPrices()) {
            prices.setFromDate(startTime);
        }
        save(product);
    }

    private static class TestPharmacyOrderInvoicer extends PharmacyOrderInvoicer {

        /**
         * Constructs a {@link TestPharmacyOrderInvoicer}.
         *
         * @param act   the order/return act
         * @param rules the order rules
         */
        public TestPharmacyOrderInvoicer(FinancialAct act, OrderRules rules) {
            super(act, rules);
        }

        /**
         * Creates a new {@link CustomerChargeActEditor}.
         *
         * @param charge  the charge
         * @param context the layout context
         * @return a new charge editor
         */
        @Override
        protected CustomerChargeActEditor createChargeEditor(FinancialAct charge, LayoutContext context) {
            return new TestChargeEditor(charge, context, true);
        }
    }

}
