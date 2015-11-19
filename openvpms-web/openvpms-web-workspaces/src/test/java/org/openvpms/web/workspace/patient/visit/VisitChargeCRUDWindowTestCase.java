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
import org.openvpms.web.workspace.customer.charge.ChargeEditorQueue;
import org.openvpms.web.workspace.customer.charge.CustomerChargeTestHelper;
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
    }

    /**
     * Verifies that if the invoice is modified outside of the editor, it is reloaded on save.
     */
    @Test
    public void testReload() {
        Act event = PatientTestHelper.createEvent(patient);
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
        Act event = PatientTestHelper.createEvent(patient);
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

        Act event = PatientTestHelper.createEvent(patient);

        final ChargeEditorQueue queue = new ChargeEditorQueue();

        VisitChargeCRUDWindow window = new VisitChargeCRUDWindow(event, context, new HelpContext("foo", null)) {
            @Override
            protected VisitChargeEditor createVisitChargeEditor(FinancialAct charge, Act event, LayoutContext context) {
                return new TestVisitChargeEditor(queue, charge, event, context);
            }
        };
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

}
