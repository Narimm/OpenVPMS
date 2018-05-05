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

package org.openvpms.web.workspace.patient.visit;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.patient.PatientTestHelper;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.test.AbstractAppTest;
import org.openvpms.web.workspace.customer.charge.CustomerChargeActItemEditor;
import org.openvpms.web.workspace.customer.charge.CustomerChargeTestHelper;
import org.openvpms.web.workspace.customer.charge.DefaultEditorQueue;
import org.openvpms.web.workspace.customer.charge.EditorQueue;
import org.openvpms.web.workspace.customer.order.PharmacyTestHelper;
import org.openvpms.web.workspace.patient.charge.VisitChargeEditor;
import org.openvpms.web.workspace.workflow.TestVisitChargeEditor;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link VisitChargeCRUDWindow}.
 *
 * @author Tim Anderson
 */
public class VisitChargeCRUDWindowTestCase extends AbstractAppTest {

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
     * The clinical event.
     */
    private Act event;


    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        super.setUp();
        customer = TestHelper.createCustomer();
        patient = TestHelper.createPatient(customer);

        context = new LocalContext();
        context.setPractice(TestHelper.getPractice());
        context.setCustomer(customer);
        context.setPatient(patient);
        context.setUser(TestHelper.createUser());
        context.setLocation(TestHelper.createLocation());

        event = PatientTestHelper.createEvent(patient);
    }

    /**
     * Verifies that if the invoice is modified outside of the editor, it is reloaded on save.
     */
    @Test
    public void testReload() {
        FinancialAct charge = (FinancialAct) create(CustomerAccountArchetypes.INVOICE);
        VisitChargeCRUDWindow window = new VisitChargeCRUDWindow(event, context, new HelpContext("foo", null));
        window.setObject(charge);
        assertTrue(window.save());

        // change the invoice outside of the editor
        FinancialAct copy = get(charge);
        copy.setStatus(ActStatus.COMPLETED);
        save(copy);

        // verify the editor fails to save
        VisitChargeEditor editor1 = window.getEditor();
        editor1.setClinician(TestHelper.createClinician());
        assertFalse(window.save());

        // verify a new editor has been created, and has the new version
        VisitChargeEditor editor2 = window.getEditor();
        assertNotNull(editor2);
        assertNotEquals(editor2, editor1);
        assertEquals(ActStatus.COMPLETED, editor2.getStatus());

        assertEquals(ActStatus.COMPLETED, window.getObject().getStatus());
    }

    /**
     * Verifies that if the invoice is POSTED outside of the editor, the editor cannot be saved, and the invoice is
     * no longer editable.
     */
    @Test
    public void testPostOutsideEditor() {
        FinancialAct charge = (FinancialAct) create(CustomerAccountArchetypes.INVOICE);
        VisitChargeCRUDWindow window = new VisitChargeCRUDWindow(event, context, new HelpContext("foo", null));
        window.setObject(charge);
        assertTrue(window.save());

        // post the charge outside of the editor
        FinancialAct copy = get(charge);
        copy.setStatus(ActStatus.POSTED);
        save(copy);

        // verify the editor fails to save
        window.getEditor().setClinician(TestHelper.createClinician());
        assertFalse(window.save());

        // verify the object is no longer editable
        assertNull(window.getEditor());

        // verify the object has been updated in the CRUD window
        assertEquals(ActStatus.POSTED, window.getObject().getStatus());
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

        EditorQueue queue = new DefaultEditorQueue(context);
        VisitChargeCRUDWindow window = new TestVisitChargeCRUDWindow(event, queue);
        window.setObject(charge);
        window.show();
        CustomerChargeTestHelper.checkSavePopup(queue, PatientArchetypes.PATIENT_MEDICATION, false);
        VisitChargeEditor editor1 = window.getEditor();
        assertEquals(1, editor1.getItems().getActs().size());

        FinancialAct copy = get(charge);
        copy.setStatus(ActStatus.COMPLETED);
        save(copy);

        assertFalse(window.save()); // save should fail, as the act has been modified outside the editor

        // verify the order hasn't changed
        order = get(order);
        assertEquals(ActStatus.IN_PROGRESS, order.getStatus());

        // verify a new editor has been created
        VisitChargeEditor editor2 = window.getEditor();
        assertNotNull(editor2);
        assertNotEquals(editor2, editor1);
        assertEquals(ActStatus.COMPLETED, editor2.getStatus());
        assertEquals(0, editor2.getItems().getActs().size()); // orders are not charged again

        editor2.setStatus(ActStatus.IN_PROGRESS);
        assertTrue(window.save());

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

        EditorQueue queue = new DefaultEditorQueue(context);
        TestVisitChargeCRUDWindow window = new TestVisitChargeCRUDWindow(event, queue);
        window.setObject(charge);
        window.show();

        TestVisitChargeEditor editor = window.getEditor();
        assertNotNull(editor);
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
        assertTrue(window.save());

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
        assertTrue(window.save());
        checkInvoice(editor, ActStatus.POSTED, 5);

        assertEquals(4, window.getSaves());
        // save should now succeed even if POSTED. The editor save won't actually be invoked
        assertTrue(window.save());
        assertEquals(4, window.getSaves());
    }

    /**
     * Verifies an invoice has been saved and has the expected status and no. of items.
     *
     * @param editor the invoice editor
     * @param status the expected status
     * @param items  the expected no. of items
     */
    private void checkInvoice(VisitChargeEditor editor, String status, int items) {
        FinancialAct charge = get(editor.getObject());
        assertNotNull(charge);
        assertEquals(status, charge.getStatus());
        ActBean bean = new ActBean(charge);
        assertEquals(items, bean.getNodeActs("items").size());
    }

    private class TestVisitChargeCRUDWindow extends VisitChargeCRUDWindow {

        private final EditorQueue queue;

        private int saves;

        public TestVisitChargeCRUDWindow(Act event, EditorQueue queue) {
            super(event, VisitChargeCRUDWindowTestCase.this.context, new HelpContext("foo", null));
            this.queue = queue;
        }

        /**
         * Returns the charge editor.
         *
         * @return the charge editor. May be {@code null}
         */
        @Override
        public TestVisitChargeEditor getEditor() {
            return (TestVisitChargeEditor) super.getEditor();
        }

        /**
         * Returns the no. of invocations of {@link #doSave()}.
         *
         * @return the no. of saves
         */
        public int getSaves() {
            return saves;
        }

        /**
         * Saves the invoice.
         *
         * @return {@code true} if the invoice was saved
         */
        @Override
        protected boolean doSave() {
            ++saves;
            return super.doSave();
        }

        @Override
        protected VisitChargeEditor createVisitChargeEditor(FinancialAct charge, Act event, LayoutContext context) {
            return new TestVisitChargeEditor(queue, charge, event, context);
        }
    }
}
