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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link CustomerChargeActEditDialogTestCase}.
 *
 * @author Tim Anderson
 */
public class CustomerChargeActEditDialogTestCase extends AbstractCustomerChargeActEditorTest {

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
        CustomerChargeActEditDialog dialog = new CustomerChargeActEditDialog(editor, layoutContext.getContext());
        assertEquals(0, editor.getItems().getActs().size());
        dialog.show();
        CustomerChargeTestHelper.checkSavePopup(editor.getQueue(), PatientArchetypes.PATIENT_MEDICATION, false);
        CustomerChargeTestHelper.checkSavePopup(editor.getQueue(), PatientArchetypes.PATIENT_MEDICATION, false);
        assertEquals(2, editor.getItems().getActs().size());

        assertTrue(editor.isValid());

        dialog.save(false);

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
        CustomerChargeActEditDialog dialog = new CustomerChargeActEditDialog(editor1, layoutContext.getContext());
        dialog.show();
        CustomerChargeTestHelper.checkSavePopup(editor1.getQueue(), PatientArchetypes.PATIENT_MEDICATION, false);
        assertEquals(1, editor1.getItems().getActs().size());

        FinancialAct copy = get(charge);
        copy.setStatus(ActStatus.COMPLETED);
        save(copy);

        dialog.save(false);   // save should fail, as the act has been modified outside the editor

        // verify the order hasn't changed
        order = get(order);
        assertEquals(ActStatus.IN_PROGRESS, order.getStatus());

        // verify the editor was reloaded
        CustomerChargeActEditor editor2 = dialog.getEditor();
        assertNotEquals(editor1, editor2);
        assertEquals(0, editor2.getItems().getActs().size()); // orders are not charged again


        editor2.setStatus(ActStatus.IN_PROGRESS);
        dialog.save(true);

        // verify the order hasn't changed
        order = get(order);
        assertEquals(ActStatus.IN_PROGRESS, order.getStatus());
    }
}
