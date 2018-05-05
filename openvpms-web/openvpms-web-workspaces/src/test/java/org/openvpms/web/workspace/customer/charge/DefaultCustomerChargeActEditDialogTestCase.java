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

package org.openvpms.web.workspace.customer.charge;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.workspace.customer.order.PharmacyTestHelper;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link DefaultCustomerChargeActEditDialogTestCase}.
 *
 * @author Tim Anderson
 */
public class DefaultCustomerChargeActEditDialogTestCase extends AbstractCustomerChargeActEditorTest {

    /**
     * The layout context.
     */
    private LayoutContext layoutContext;

    /**
     * The customer.
     */
    private Party customer;

    /**
     * The patient.
     */
    private Party patient;


    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        super.setUp();
        customer = TestHelper.createCustomer();
        patient = TestHelper.createPatient(customer);

        layoutContext = new DefaultLayoutContext(new LocalContext(), new HelpContext("foo", null));
        layoutContext.getContext().setPractice(getPractice());
        layoutContext.getContext().setCustomer(customer);
        layoutContext.getContext().setPatient(patient);
        layoutContext.getContext().setUser(TestHelper.createUser());
        layoutContext.getContext().setClinician(TestHelper.createClinician());
        layoutContext.getContext().setLocation(TestHelper.createLocation());
    }

    /**
     * Verifies that pending orders are automatically charged when the dialog is shown.
     */
    @Test
    public void testOrderCharger() {
        FinancialAct charge = (FinancialAct) create(CustomerAccountArchetypes.INVOICE);

        Product product = TestHelper.createProduct();
        FinancialAct order1 = PharmacyTestHelper.createOrder(customer, patient, product, BigDecimal.ONE, null);
        FinancialAct order2 = PharmacyTestHelper.createOrder(customer, patient, product, BigDecimal.ONE, null);

        TestChargeEditor editor = new TestChargeEditor(charge, layoutContext, false);
        DefaultCustomerChargeActEditDialog dialog = new DefaultCustomerChargeActEditDialog(editor, layoutContext.getContext());
        assertEquals(0, editor.getItems().getActs().size());
        dialog.show();
        CustomerChargeTestHelper.checkSavePopup(editor.getQueue(), PatientArchetypes.PATIENT_MEDICATION, false);
        CustomerChargeTestHelper.checkSavePopup(editor.getQueue(), PatientArchetypes.PATIENT_MEDICATION, false);
        assertEquals(2, editor.getItems().getActs().size());

        assertTrue(editor.isValid());

        assertTrue(dialog.save());

        // orders should now be posted
        order1 = get(order1);
        order2 = get(order2);
        assertEquals(ActStatus.POSTED, order1.getStatus());
        assertEquals(ActStatus.POSTED, order2.getStatus());
    }

    /**
     * Verifies that when an invoice is modified outside of the editor and is reloaded, pending orders are not
     * affected.
     */
    @Test
    public void testReloadWithChargedOrders() {
        FinancialAct charge = (FinancialAct) create(CustomerAccountArchetypes.INVOICE);
        ActBean bean = new ActBean(charge);
        bean.addNodeParticipation("customer", customer);
        bean.save();

        Product product = TestHelper.createProduct();
        FinancialAct order = PharmacyTestHelper.createOrder(customer, patient, product, BigDecimal.ONE, null);

        TestChargeEditor editor1 = new TestChargeEditor(charge, layoutContext, false);
        DefaultCustomerChargeActEditDialog dialog = new DefaultCustomerChargeActEditDialog(editor1, layoutContext.getContext());
        dialog.show();
        CustomerChargeTestHelper.checkSavePopup(editor1.getQueue(), PatientArchetypes.PATIENT_MEDICATION, false);
        assertEquals(1, editor1.getItems().getActs().size());

        FinancialAct copy = get(charge);
        copy.setStatus(ActStatus.COMPLETED);
        save(copy);

        assertFalse(dialog.save());   // save should fail, as the act has been modified outside the editor

        // verify the order hasn't changed
        order = get(order);
        assertEquals(ActStatus.IN_PROGRESS, order.getStatus());

        // verify the editor was reloaded
        CustomerChargeActEditor editor2 = dialog.getEditor();
        assertNotEquals(editor1, editor2);
        assertEquals(0, editor2.getItems().getActs().size()); // orders are not charged again

        editor2.setStatus(ActStatus.IN_PROGRESS);
        assertTrue(dialog.save());

        // verify the order hasn't changed
        order = get(order);
        assertEquals(ActStatus.IN_PROGRESS, order.getStatus());
    }

    /**
     * Verifies that the invoice is automatically saved when Add is clicked, and the invoice is:
     * <ul>
     * <li>not new</li>
     * <li>not POSTED</li>
     * <li>valid</li>
     * </ul>
     */
    @Test
    public void testAutoSave() {
        FinancialAct charge = (FinancialAct) create(CustomerAccountArchetypes.INVOICE);
        TestChargeEditor editor = new TestChargeEditor(charge, layoutContext, false);
        DefaultCustomerChargeActEditDialog dialog = new DefaultCustomerChargeActEditDialog(editor, layoutContext.getContext());
        assertEquals(0, editor.getItems().getActs().size());
        dialog.show();

        CustomerChargeActItemEditor item1 = (CustomerChargeActItemEditor) editor.getItems().onAdd();
        assertNotNull(item1);

        // verify the invoice hasn't auto-saved, as it is new
        assertTrue(editor.getObject().isNew());

        // populate the item
        item1.setProduct(TestHelper.createProduct());
        item1.setQuantity(BigDecimal.TEN);
        CustomerChargeTestHelper.checkSavePopup(editor.getQueue(), PatientArchetypes.PATIENT_MEDICATION, false);

        // add a new item
        CustomerChargeActItemEditor item2 = (CustomerChargeActItemEditor) editor.getItems().onAdd();
        assertNotNull(item2);

        // verify the invoice hasn't auto-saved, as it is still new
        assertTrue(editor.getObject().isNew());

        // now save it.
        assertTrue(dialog.save());

        // populate the second item
        Product product2 = TestHelper.createProduct();
        item2.setProduct(product2);
        item2.setQuantity(BigDecimal.ONE);
        CustomerChargeTestHelper.checkSavePopup(editor.getQueue(), PatientArchetypes.PATIENT_MEDICATION, false);

        // add a new item
        CustomerChargeActItemEditor item3 = (CustomerChargeActItemEditor) editor.getItems().onAdd();
        assertNotNull(item3);

        // verify the invoice has saved, and has 2 items
        checkInvoice(editor, ActStatus.IN_PROGRESS, 2);

        // populate the third item
        item3.setProduct(TestHelper.createProduct());
        item3.setQuantity(BigDecimal.TEN);
        CustomerChargeTestHelper.checkSavePopup(editor.getQueue(), PatientArchetypes.PATIENT_MEDICATION, false);

        // mark the second item invalid. Auto-save should be disabled
        item2.setProduct(null);

        // add a new item
        CustomerChargeActItemEditor item4 = (CustomerChargeActItemEditor) editor.getItems().onAdd();
        assertNotNull(item4);
        item4.setProduct(TestHelper.createProduct());
        item4.setQuantity(BigDecimal.ONE);
        CustomerChargeTestHelper.checkSavePopup(editor.getQueue(), PatientArchetypes.PATIENT_MEDICATION, false);

        // verify the invoice has not auto-saved, and still has 2 items
        checkInvoice(editor, ActStatus.IN_PROGRESS, 2);

        // now make the invoice valid again
        item2.setProduct(TestHelper.createProduct());
        CustomerChargeTestHelper.checkSavePopup(editor.getQueue(), PatientArchetypes.PATIENT_MEDICATION, false);
        assertTrue(editor.isValid());

        // add a new item
        CustomerChargeActItemEditor item5 = (CustomerChargeActItemEditor) editor.getItems().onAdd();
        assertNotNull(item5);

        // verify the invoice has saved, and has 4 items
        checkInvoice(editor, ActStatus.IN_PROGRESS, 4);

        item5.setProduct(TestHelper.createProduct());
        item5.setQuantity(BigDecimal.TEN);
        CustomerChargeTestHelper.checkSavePopup(editor.getQueue(), PatientArchetypes.PATIENT_MEDICATION, false);

        // set the invoice POSTED. Auto-save should now be disabled
        editor.setStatus(ActStatus.POSTED);

        CustomerChargeActItemEditor item6 = (CustomerChargeActItemEditor) editor.getItems().onAdd();
        assertNotNull(item6);

        // verify the invoice has not auto-saved, and still has 2 items
        checkInvoice(editor, ActStatus.IN_PROGRESS, 4);

        // now save, and verify it has 5 items and is posted.
        assertTrue(dialog.save());
        checkInvoice(editor, ActStatus.POSTED, 5);

        // save should now fail as it is POSTED
        assertFalse(dialog.save());
    }

    /**
     * Verifies an invoice has been saved and has the expected status and no. of items.
     *
     * @param editor the invoice editor
     * @param status the expected status
     * @param items  the expected no. of items
     */
    private void checkInvoice(DefaultCustomerChargeActEditor editor, String status, int items) {
        FinancialAct charge = get(editor.getObject());
        assertNotNull(charge);
        assertEquals(status, charge.getStatus());
        ActBean bean = new ActBean(charge);
        assertEquals(items, bean.getNodeActs("items").size());
    }
}
