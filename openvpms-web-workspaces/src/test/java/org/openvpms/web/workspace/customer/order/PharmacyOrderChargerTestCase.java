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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.customer.order;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.finance.order.OrderArchetypes;
import org.openvpms.archetype.rules.finance.order.OrderRules;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.workspace.customer.charge.AbstractCustomerChargeActEditorTest;
import org.openvpms.web.workspace.customer.charge.CustomerChargeActEditDialog;
import org.openvpms.web.workspace.customer.charge.CustomerChargeActEditor;
import org.openvpms.web.workspace.customer.charge.CustomerChargeTestHelper;
import org.openvpms.web.workspace.customer.charge.TestChargeEditor;

import java.math.BigDecimal;
import java.util.List;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link PharmacyOrderCharger}.
 *
 * @author Tim Anderson
 */
public class PharmacyOrderChargerTestCase extends AbstractCustomerChargeActEditorTest {

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
     * The product fixed price.
     */
    private BigDecimal fixedPrice;

    /**
     * The product unit price.
     */
    private BigDecimal unitPrice;


    /**
     * Sets up the test case.
     */
    @Override
    @Before
    public void setUp() {
        super.setUp();
        fixedPrice = BigDecimal.valueOf(2);
        unitPrice = TEN;
        product = createProduct(ProductArchetypes.MEDICATION, fixedPrice, unitPrice);
        clinician = TestHelper.createClinician();
        customer = TestHelper.createCustomer();
        patient = TestHelper.createPatient(customer);
        context = new LocalContext();
        context.setPractice(getPractice());
        context.setLocation(TestHelper.createLocation());
        author = TestHelper.createUser();
        context.setUser(author);
        context.setClinician(clinician);
        context.setPatient(patient);
        context.setCustomer(customer);
        rules = new OrderRules(getArchetypeService());
    }

    /**
     * Tests charging an order that isn't linked to an existing invoice.
     * <p/>
     * This should create a new invoice.
     */
    @Test
    public void testChargeUnlinkedOrder() {
        BigDecimal quantity = BigDecimal.valueOf(2);
        BigDecimal tax = BigDecimal.valueOf(2);
        BigDecimal total = new BigDecimal("22");
        FinancialAct order = createOrder(customer, patient, product, quantity, null);
        PharmacyOrderCharger charger = new TestPharmacyOrderCharger(order, rules);
        assertTrue(charger.isValid());
        assertTrue(charger.canInvoice());
        assertFalse(charger.canCredit());

        DefaultLayoutContext layoutContext = new DefaultLayoutContext(context, new HelpContext("foo", null));
        CustomerChargeActEditDialog dialog = charger.charge(null, null, layoutContext);
        TestChargeEditor editor = (TestChargeEditor) dialog.getEditor();
        CustomerChargeTestHelper.checkSavePopup(editor.getQueue(), PatientArchetypes.PATIENT_MEDICATION, false);
        assertTrue(SaveHelper.save(editor));
        FinancialAct charge = (FinancialAct) get(editor.getObject());
        assertTrue(TypeHelper.isA(charge, CustomerAccountArchetypes.INVOICE));
        checkItem(charge, patient, product, quantity, unitPrice, fixedPrice, tax, total, quantity, null);
        checkCharge(charge, customer, author, clinician, tax, total);
    }

    /**
     * Tests charging a return that isn't linked to an existing invoice.
     * <p/>
     * This should create a Credit.
     */
    @Test
    public void testCreditUnlinkedReturn() {
        BigDecimal quantity = BigDecimal.valueOf(2);
        BigDecimal tax = BigDecimal.valueOf(2);
        BigDecimal total = new BigDecimal("22");
        FinancialAct orderReturn = createReturn(customer, patient, product, quantity, null);
        PharmacyOrderCharger charger = new TestPharmacyOrderCharger(orderReturn, rules);
        assertTrue(charger.isValid());
        assertTrue(charger.canCredit());
        assertFalse(charger.canInvoice());

        DefaultLayoutContext layoutContext = new DefaultLayoutContext(context, new HelpContext("foo", null));
        CustomerChargeActEditDialog dialog = charger.charge(null, null, layoutContext);
        TestChargeEditor editor = (TestChargeEditor) dialog.getEditor();
        assertTrue(SaveHelper.save(editor));
        FinancialAct charge = (FinancialAct) get(editor.getObject());
        assertTrue(TypeHelper.isA(charge, CustomerAccountArchetypes.CREDIT));
        checkItem(charge, patient, product, quantity, unitPrice, fixedPrice, tax, total, null, null);
        checkCharge(charge, customer, author, clinician, tax, total);
    }

    /**
     * Tests charging an order that is linked to an existing IN_PROGRESS invoice.
     * <p/>
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

        FinancialAct invoice = (FinancialAct) editor1.getObject();
        FinancialAct invoiceItem = getInvoiceItem(editor1);

        FinancialAct order = createOrder(customer, patient, product, quantity, invoiceItem);
        PharmacyOrderCharger charger = new TestPharmacyOrderCharger(order, rules);
        assertTrue(charger.isValid());
        assertTrue(charger.canInvoice());
        assertFalse(charger.canCredit());

        DefaultLayoutContext layoutContext = new DefaultLayoutContext(context, new HelpContext("foo", null));
        CustomerChargeActEditDialog dialog = charger.charge(invoice, null, layoutContext);
        TestChargeEditor editor2 = (TestChargeEditor) dialog.getEditor();
        assertTrue(SaveHelper.save(editor2));
        FinancialAct charge = (FinancialAct) get(editor2.getObject());
        assertEquals(invoice, charge);
        checkItem(charge, patient, product, quantity, unitPrice, fixedPrice, tax, total, quantity, null);
        checkCharge(charge, customer, author, clinician, tax, total);
    }

    /**
     * Tests charging an order that is linked to an existing IN_PROGRESS invoice, with a greater quantity than that
     * invoiced.
     * <p/>
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

        FinancialAct invoice = (FinancialAct) editor1.getObject();
        FinancialAct invoiceItem = getInvoiceItem(editor1);

        FinancialAct order = createOrder(customer, patient, product, newQty, invoiceItem);
        PharmacyOrderCharger charger = new TestPharmacyOrderCharger(order, rules);
        assertTrue(charger.isValid());
        assertTrue(charger.canInvoice());
        assertFalse(charger.canCredit());

        DefaultLayoutContext layoutContext = new DefaultLayoutContext(context, new HelpContext("foo", null));
        CustomerChargeActEditDialog dialog = charger.charge(invoice, null, layoutContext);
        TestChargeEditor editor2 = (TestChargeEditor) dialog.getEditor();
        assertTrue(SaveHelper.save(editor2));
        FinancialAct charge = (FinancialAct) get(editor2.getObject());
        assertEquals(invoice, charge);
        checkItem(charge, patient, product, newQty, unitPrice, fixedPrice, newTax, newTotal, newQty, null);
        checkCharge(charge, customer, author, clinician, newTax, newTotal);
    }

    /**
     * Tests charging an order that is linked to an existing POSTED invoice, with a greater quantity than that
     * invoiced.
     * <p/>
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

        FinancialAct invoice = (FinancialAct) editor1.getObject();
        FinancialAct invoiceItem = getInvoiceItem(editor1);

        FinancialAct order = createOrder(customer, patient, product, newOrderQty, invoiceItem);
        PharmacyOrderCharger charger = new TestPharmacyOrderCharger(order, rules);
        assertTrue(charger.isValid());
        assertTrue(charger.canInvoice());
        assertFalse(charger.canCredit());

        DefaultLayoutContext layoutContext = new DefaultLayoutContext(context, new HelpContext("foo", null));
        CustomerChargeActEditDialog dialog = charger.charge(null, null, layoutContext);
        TestChargeEditor editor = (TestChargeEditor) dialog.getEditor();
        CustomerChargeTestHelper.checkSavePopup(editor.getQueue(), PatientArchetypes.PATIENT_MEDICATION, false);
        TestChargeEditor editor2 = (TestChargeEditor) dialog.getEditor();
        assertTrue(SaveHelper.save(editor2));
        FinancialAct charge = (FinancialAct) get(editor2.getObject());
        assertNotEquals(invoice, charge);

        // NOTE: item tax not rounded.
        checkItem(charge, patient, product, newQty, unitPrice, fixedPrice, new BigDecimal("1.091"), newTotal,
                  newOrderQty, null);
        checkCharge(charge, customer, author, clinician, newTax, newTotal);
    }

    /**
     * Tests charging an order that is linked to an existing IN_PROGRESS invoice, with a lesser quantity than that
     * invoiced.
     * <p/>
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

        FinancialAct invoice = (FinancialAct) editor1.getObject();
        FinancialAct invoiceItem = getInvoiceItem(editor1);

        FinancialAct order = createOrder(customer, patient, product, newQty, invoiceItem);
        PharmacyOrderCharger charger = new TestPharmacyOrderCharger(order, rules);
        assertTrue(charger.isValid());
        assertTrue(charger.canInvoice());
        assertTrue(charger.canCredit());

        DefaultLayoutContext layoutContext = new DefaultLayoutContext(context, new HelpContext("foo", null));
        CustomerChargeActEditDialog dialog = charger.charge(invoice, null, layoutContext);
        TestChargeEditor editor2 = (TestChargeEditor) dialog.getEditor();
        assertTrue(SaveHelper.save(editor2));
        FinancialAct charge = (FinancialAct) get(editor2.getObject());
        assertTrue(TypeHelper.isA(charge, CustomerAccountArchetypes.INVOICE));

        // NOTE: item tax not rounded.
        checkItem(charge, patient, product, newQty, unitPrice, fixedPrice, new BigDecimal("1.091"), newTotal,
                  newQty, null);
        checkCharge(charge, customer, author, clinician, newTax, newTotal);
    }

    /**
     * Tests charging an order that is linked to an existing POSTED invoice, with a lesser quantity than that invoiced.
     * <p/>
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
        PharmacyOrderCharger charger = new TestPharmacyOrderCharger(order, rules);
        assertTrue(charger.isValid());
        assertFalse(charger.canInvoice());
        assertTrue(charger.canCredit());

        DefaultLayoutContext layoutContext = new DefaultLayoutContext(context, new HelpContext("foo", null));
        CustomerChargeActEditDialog dialog = charger.charge(null, null, layoutContext);
        TestChargeEditor editor2 = (TestChargeEditor) dialog.getEditor();
        assertTrue(SaveHelper.save(editor2));
        FinancialAct charge = (FinancialAct) get(editor2.getObject());
        assertTrue(TypeHelper.isA(charge, CustomerAccountArchetypes.CREDIT));

        // NOTE: item tax not rounded.
        checkItem(charge, patient, product, newQty, unitPrice, fixedPrice, new BigDecimal("1.091"), newTotal,
                  newQty, null);
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

        FinancialAct invoice = (FinancialAct) editor.getObject();
        FinancialAct invoiceItem = getInvoiceItem(editor);

        // charge two orders
        FinancialAct order1 = createOrder(customer, patient, product, ONE, invoiceItem);
        PharmacyOrderCharger charger1 = new TestPharmacyOrderCharger(order1, rules);
        assertTrue(charger1.canCharge(editor));
        charger1.charge(editor);

        FinancialAct order2 = createOrder(customer, patient, product, ONE, invoiceItem);
        PharmacyOrderCharger charger2 = new TestPharmacyOrderCharger(order2, rules);
        assertTrue(charger2.canCharge(editor));
        charger2.charge(editor);

        assertTrue(SaveHelper.save(editor));
        checkItem(invoice, patient, product, quantity, unitPrice, fixedPrice, tax, total, quantity, null);
        checkCharge(invoice, customer, author, clinician, tax, total);
    }

    /**
     * Tests applying an order and return for the same quantity to an IN_PROGRESS invoice.
     * <p/>
     * This will set the invoice quantity to 0.
     */
    @Test
    public void testOrderAndReturnToInProgressInvoice() {
        TestChargeEditor editor = createInvoice(product, ONE);
        editor.setStatus(ActStatus.IN_PROGRESS);
        assertTrue(SaveHelper.save(editor));

        FinancialAct invoice = (FinancialAct) editor.getObject();
        FinancialAct invoiceItem = getInvoiceItem(editor);

        FinancialAct order = createOrder(customer, patient, product, ONE, invoiceItem);
        PharmacyOrderCharger charger1 = new TestPharmacyOrderCharger(order, rules);
        assertTrue(charger1.canCharge(editor));
        charger1.charge(editor);

        FinancialAct orderReturn = createReturn(customer, patient, product, ONE, invoiceItem);
        PharmacyOrderCharger charger2 = new TestPharmacyOrderCharger(orderReturn, rules);
        assertTrue(charger2.canCharge(editor));
        charger2.charge(editor);

        assertTrue(SaveHelper.save(editor));
        checkItem(invoice, patient, product, ZERO, unitPrice, fixedPrice, ZERO, ZERO, ONE, ONE);
        checkCharge(invoice, customer, author, clinician, ZERO, ZERO);
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
        PharmacyOrderCharger charger1 = new TestPharmacyOrderCharger(order, rules);
        assertTrue(charger1.canCharge(editor1));
        charger1.charge(editor1);

        editor1.setStatus(ActStatus.POSTED);
        assertTrue(SaveHelper.save(editor1));

        // now return the same quantity, and verify a Credit is created
        FinancialAct orderReturn = createReturn(customer, patient, product, quantity, invoiceItem);
        PharmacyOrderCharger charger2 = new TestPharmacyOrderCharger(orderReturn, rules);
        assertFalse(charger2.canCharge(editor1));
        DefaultLayoutContext layoutContext = new DefaultLayoutContext(context, new HelpContext("foo", null));
        CustomerChargeActEditDialog dialog = charger2.charge(null, null, layoutContext);
        TestChargeEditor editor2 = (TestChargeEditor) dialog.getEditor();
        assertTrue(SaveHelper.save(editor2));
        FinancialAct charge = (FinancialAct) get(editor2.getObject());
        assertTrue(TypeHelper.isA(charge, CustomerAccountArchetypes.CREDIT));
        checkItem(charge, patient, product, quantity, unitPrice, fixedPrice, tax, total, null, null);
        checkCharge(charge, customer, author, clinician, tax, total);
    }

    /**
     * Creates a new invoice in an editor.
     *
     * @param product  the product to invoice
     * @param quantity the quantity to invoice
     * @return the invoice editor
     */
    private TestChargeEditor createInvoice(Product product, BigDecimal quantity) {
        FinancialAct charge = (FinancialAct) create(CustomerAccountArchetypes.INVOICE);
        LayoutContext layoutContext = new DefaultLayoutContext(context, new HelpContext("foo", null));
        TestChargeEditor editor = new TestChargeEditor(charge, layoutContext, false);
        editor.getComponent();
        addItem(editor, patient, product, quantity, editor.getQueue());
        return editor;
    }

    /**
     * Creates a pharmacy order.
     *
     * @param customer    the customer
     * @param patient     the patient
     * @param product     the product
     * @param quantity    the order quantity
     * @param invoiceItem the related invoice item. May be {@code null}
     * @return a new order
     */
    private FinancialAct createOrder(Party customer, Party patient, Product product, BigDecimal quantity,
                                     FinancialAct invoiceItem) {
        return createOrderReturn(true, customer, patient, product, quantity, invoiceItem);
    }

    /**
     * Creates a pharmacy return.
     *
     * @param customer    the customer
     * @param patient     the patient
     * @param product     the product
     * @param quantity    the order quantity
     * @param invoiceItem the related invoice item. May be {@code null}
     * @return a new return
     */
    private FinancialAct createReturn(Party customer, Party patient, Product product, BigDecimal quantity,
                                      FinancialAct invoiceItem) {
        return createOrderReturn(false, customer, patient, product, quantity, invoiceItem);
    }

    /**
     * Creates a pharmacy order/return.
     *
     * @param isOrder     if {@code true}, create an order, else create a return
     * @param customer    the customer
     * @param patient     the patient
     * @param product     the product
     * @param quantity    the order quantity
     * @param invoiceItem the related invoice item. May be {@code null}
     * @return a new order/rr
     */
    private FinancialAct createOrderReturn(boolean isOrder, Party customer, Party patient, Product product,
                                           BigDecimal quantity, FinancialAct invoiceItem) {
        FinancialAct act = (FinancialAct) create(isOrder ? OrderArchetypes.PHARMACY_ORDER
                                                         : OrderArchetypes.PHARMACY_RETURN);
        FinancialAct item = (FinancialAct) create(isOrder ? OrderArchetypes.PHARMACY_ORDER_ITEM
                                                          : OrderArchetypes.PHARMACY_RETURN_ITEM);
        ActBean bean = new ActBean(act);
        bean.addNodeParticipation("customer", customer);
        bean.addNodeRelationship("items", item);

        ActBean itemBean = new ActBean(item);
        itemBean.addNodeParticipation("patient", patient);
        itemBean.addNodeParticipation("product", product);
        if (invoiceItem != null) {
            itemBean.setValue("sourceInvoiceItem", invoiceItem.getObjectReference());
        }
        item.setQuantity(quantity);
        save(act, item);
        return act;
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
     */
    private void checkItem(FinancialAct charge, Party patient, Product product, BigDecimal quantity,
                           BigDecimal unitPrice, BigDecimal fixedPrice, BigDecimal tax, BigDecimal total,
                           BigDecimal receivedQuantity, BigDecimal returnedQuantity) {
        ActBean bean = new ActBean(charge);
        List<FinancialAct> items = bean.getNodeActs("items", FinancialAct.class);
        assertEquals(1, items.size());

        int childActs = TypeHelper.isA(charge, CustomerAccountArchetypes.INVOICE) ? 1 : 0;
        // for invoices, there should be a medication act

        checkItem(items, patient, product, quantity, unitPrice, fixedPrice, tax, total, childActs);
        if (bean.isA(CustomerAccountArchetypes.INVOICE)) {
            FinancialAct item = items.get(0);
            ActBean itemBean = new ActBean(item);
            checkEquals(receivedQuantity, itemBean.getBigDecimal("receivedQuantity"));
            checkEquals(returnedQuantity, itemBean.getBigDecimal("returnedQuantity"));
        }
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
     */
    private void checkItem(List<FinancialAct> items, Party patient, Product product, BigDecimal quantity,
                           BigDecimal unitPrice, BigDecimal fixedPrice, BigDecimal tax, BigDecimal total,
                           int childActs) {
        checkItem(items, patient, product, author, clinician, quantity, ZERO, unitPrice, ZERO, fixedPrice, ZERO, tax,
                  total, null, childActs);
    }

    private static class TestPharmacyOrderCharger extends PharmacyOrderCharger {

        /**
         * Constructs a {@link TestPharmacyOrderCharger}.
         *
         * @param act   the order/return act
         * @param rules the order rules
         */
        public TestPharmacyOrderCharger(FinancialAct act, OrderRules rules) {
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
