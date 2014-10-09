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
import org.openvpms.archetype.rules.finance.account.FinancialTestHelper;
import org.openvpms.archetype.rules.finance.order.OrderArchetypes;
import org.openvpms.archetype.rules.finance.order.OrderRules;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.test.TestHelper;
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

import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link PharmacyOrderCharger}.
 *
 * @author Tim Anderson
 */
public class PharmacyOrderChargerTestCase extends AbstractCustomerChargeActEditorTest {

    private Context context;
    private User author;
    private User clinician;
    private OrderRules rules;
    private Party customer;
    private Party patient;

    /**
     * Sets up the test case.
     */
    @Override
    @Before
    public void setUp() {
        super.setUp();
        context = new LocalContext();
        context.setPractice(getPractice());
        context.setLocation(TestHelper.createLocation());
        author = TestHelper.createUser();
        context.setUser(author);
        clinician = TestHelper.createClinician();
        customer = TestHelper.createCustomer();
        patient = TestHelper.createPatient();
        context.setClinician(clinician);
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
        BigDecimal fixedPrice = BigDecimal.valueOf(2);
        BigDecimal unitPrice = TEN;
        BigDecimal tax = BigDecimal.valueOf(2);
        BigDecimal total = new BigDecimal("22");
        Product product = createProduct(ProductArchetypes.MEDICATION, fixedPrice, unitPrice);
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
        checkItem(charge, patient, product, quantity, unitPrice, fixedPrice, tax, total);
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
        BigDecimal fixedPrice = BigDecimal.valueOf(2);
        BigDecimal unitPrice = TEN;
        BigDecimal tax = BigDecimal.valueOf(2);
        BigDecimal total = new BigDecimal("22");
        Product product = createProduct(ProductArchetypes.MEDICATION, fixedPrice, unitPrice);
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
        checkItem(charge, patient, product, quantity, unitPrice, fixedPrice, tax, total);
        checkCharge(charge, customer, author, clinician, tax, total);
    }

    /**
     * Tests charging an order that is linked to an existing invoice, with a greater quantity than that invoiced.
     * <p/>
     * The invoice quantity should be updated.
     */
    @Test
    public void testChargeLinkedOrderWithGreaterQuantity() {
        BigDecimal originalQty = BigDecimal.valueOf(1);
        BigDecimal fixedPrice = BigDecimal.valueOf(2);
        BigDecimal unitPrice = TEN;
        BigDecimal originalTax = new BigDecimal("1.09");

        BigDecimal newQty = BigDecimal.valueOf(2);
        BigDecimal newTax = BigDecimal.valueOf(2);
        BigDecimal newTotal = new BigDecimal("22");

        Product product = createProduct(ProductArchetypes.MEDICATION, fixedPrice, unitPrice);

        List<FinancialAct> invoice = FinancialTestHelper.createChargesInvoice(customer, patient, product, originalQty,
                                                                              fixedPrice, unitPrice, originalTax,
                                                                              ActStatus.POSTED);
        save(invoice);

        FinancialAct order = createOrder(customer, patient, product, newQty, invoice.get(1));
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
        checkItem(charge, patient, product, newQty, unitPrice, fixedPrice, newTax, newTotal);
        checkCharge(charge, customer, author, clinician, newTax, newTotal);
    }

    /**
     * Tests charging an order that is linked to an existing invoice, with a lesser quantity than that invoiced.
     * <p/>
     * The invoice quantity should be updated.
     */
    @Test
    public void testChargeLinkedOrderWithLesserQuantity() {
        BigDecimal originalQty = BigDecimal.valueOf(2);
        BigDecimal fixedPrice = BigDecimal.valueOf(2);
        BigDecimal unitPrice = TEN;
        BigDecimal originalTax = BigDecimal.valueOf(2);

        BigDecimal newQty = BigDecimal.valueOf(1);
        BigDecimal newTax = new BigDecimal("1.09");
        BigDecimal newTotal = BigDecimal.valueOf(12);

        Product product = createProduct(ProductArchetypes.MEDICATION, fixedPrice, unitPrice);

        List<FinancialAct> invoice = FinancialTestHelper.createChargesInvoice(customer, patient, product, originalQty,
                                                                              fixedPrice, unitPrice, originalTax,
                                                                              ActStatus.POSTED);
        save(invoice);

        FinancialAct order = createOrder(customer, patient, product, newQty, invoice.get(1));
        PharmacyOrderCharger charger = new TestPharmacyOrderCharger(order, rules);
        assertTrue(charger.isValid());
        assertTrue(charger.canInvoice());
        assertTrue(charger.canCredit());

        DefaultLayoutContext layoutContext = new DefaultLayoutContext(context, new HelpContext("foo", null));
        CustomerChargeActEditDialog dialog = charger.charge(null, null, layoutContext);
        TestChargeEditor editor = (TestChargeEditor) dialog.getEditor();
        CustomerChargeTestHelper.checkSavePopup(editor.getQueue(), PatientArchetypes.PATIENT_MEDICATION, false);
        assertTrue(SaveHelper.save(editor));
        FinancialAct charge = (FinancialAct) get(editor.getObject());
        assertTrue(TypeHelper.isA(charge, CustomerAccountArchetypes.INVOICE));

        // NOTE: item tax not rounded.
        checkItem(charge, patient, product, newQty, unitPrice, fixedPrice, new BigDecimal("1.091"), newTotal);
        checkCharge(charge, customer, author, clinician, newTax, newTotal);
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

    private void checkItem(FinancialAct charge, Party patient, Product product, BigDecimal quantity,
                           BigDecimal unitPrice, BigDecimal fixedPrice, BigDecimal tax, BigDecimal total) {
        ActBean bean = new ActBean(charge);
        List<FinancialAct> items = bean.getNodeActs("items", FinancialAct.class);
        assertEquals(1, items.size());

        int childActs = TypeHelper.isA(charge, CustomerAccountArchetypes.INVOICE) ? 1 : 0;
        // for invoices, there should be a medication act

        checkItem(items, patient, product, quantity, unitPrice, fixedPrice, tax, total, childActs);
    }

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
