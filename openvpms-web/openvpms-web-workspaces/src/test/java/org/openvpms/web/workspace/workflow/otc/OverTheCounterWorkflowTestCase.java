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

package org.openvpms.web.workspace.workflow.otc;

import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.customer.CustomerArchetypes;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.edit.payment.PaymentItemEditor;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.error.ErrorHandler;
import org.openvpms.web.workspace.customer.charge.AbstractCustomerChargeActEditorTest;
import org.openvpms.web.workspace.workflow.WorkflowTestHelper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static java.math.BigDecimal.ONE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.openvpms.web.test.EchoTestHelper.fireDialogButton;

/**
 * Tests the {@link OverTheCounterWorkflow}.
 *
 * @author Tim Anderson
 */
public class OverTheCounterWorkflowTestCase extends AbstractCustomerChargeActEditorTest {

    /**
     * Tracks errors logged.
     */
    private List<String> errors = new ArrayList<String>();

    /**
     * Test context.
     */
    private Context context;

    /**
     * The act authors.
     */
    private User author;

    /**
     * The over-the-counter customer.
     */
    private Party otc;

    /**
     * Tests running the workflow through to completion.
     */
    @Test
    public void testWorkflow() {
        OverTheCounterWorkflowRunner workflow = new OverTheCounterWorkflowRunner(context);
        workflow.start();

        // edit the charge
        FinancialAct charge = editCharge(workflow);
        fireDialogButton(workflow.getEditDialog(), PopupDialog.OK_ID);

        // verify the charge has been saved
        checkCharge(get(charge), otc, author, null, BigDecimal.ZERO, ONE, ActStatus.IN_PROGRESS);

        // edit the payment
        FinancialAct payment = editPayment(workflow);
        fireDialogButton(workflow.getEditDialog(), PopupDialog.OK_ID);

        // verify the payment has been saved
        checkPayment(get(payment), otc, author, ONE, ActStatus.POSTED);

        // verify the charge has been posted
        checkCharge(get(charge), otc, author, null, BigDecimal.ZERO, ONE, ActStatus.POSTED);

        workflow.print();

        assertTrue(errors.isEmpty());
        workflow.checkComplete();
    }

    /**
     * Verify that the workflow cancels if a charge editing is cancelled via the cancel button.
     */
    @Test
    public void testCancelCharge() {
        checkCancelCharge(false);
    }

    /**
     * Verify that the workflow cancels if a charge editing is cancelled via the 'user close' button.
     */
    @Test
    public void testCancelChargeByUserClose() {
        checkCancelCharge(true);
    }

    /**
     * Verifies that the workflow cancels if payment editing is cancelled via the cancel button.
     */
    @Test
    public void testCancelPayment() {
        checkCancelPayment(false);
    }

    /**
     * Verifies that the workflow cancels if payment editing is cancelled via the 'user close' button.
     */
    @Test
    public void testCancelPaymentByUserClose() {
        checkCancelPayment(false);
    }

    /**
     * Verifies that an error is raised if the charge is posted externally.
     * <p/>
     * This could happen if a user goes into the OTC account and posts the charge while the workflow is active.
     * <p/>
     * At present, the workflow will terminate with an error, and both the charge and payment will need to be
     * manually cleaned up. TODO
     */
    @Test
    public void testPostCharge() {
        OverTheCounterWorkflowRunner workflow = new OverTheCounterWorkflowRunner(context);
        workflow.start();

        // edit the charge
        FinancialAct charge = editCharge(workflow);
        fireDialogButton(workflow.getEditDialog(), PopupDialog.OK_ID);

        // verify the charge has been saved
        charge = get(charge);
        checkCharge(charge, otc, author, null, BigDecimal.ZERO, ONE, ActStatus.IN_PROGRESS);

        charge.setStatus(ActStatus.POSTED);
        save(charge);

        assertTrue(errors.isEmpty());

        // edit the payment
        FinancialAct payment = editPayment(workflow);
        fireDialogButton(workflow.getEditDialog(), PopupDialog.OK_ID);

        assertEquals(1, errors.size());

        assertEquals("Failed to save object. It may have been changed by another user.", errors.get(0));

        workflow.checkComplete();

        checkCharge(get(charge), otc, author, null, BigDecimal.ZERO, ONE, ActStatus.POSTED);
        checkPayment(get(payment), otc, author, ONE, ActStatus.IN_PROGRESS);
    }

    /**
     * Verifies that an error is raised if the payment is posted externally.
     * <p/>
     * This could happen if a user goes into the OTC account and posts the payment while the workflow is active.
     * <p/>
     * At present, the workflow will terminate with an error, and both the charge and payment will need to be
     * manually cleaned up. TODO
     */
    @Test
    public void testPostPayment() {
        OverTheCounterWorkflowRunner workflow = new OverTheCounterWorkflowRunner(context);
        workflow.start();

        // edit the charge
        FinancialAct charge = editCharge(workflow);
        fireDialogButton(workflow.getEditDialog(), PopupDialog.OK_ID);

        // verify the charge has been saved
        charge = get(charge);
        checkCharge(get(charge), otc, author, null, BigDecimal.ZERO, ONE, ActStatus.IN_PROGRESS);

        assertTrue(errors.isEmpty());

        // edit the payment
        FinancialAct payment = editPayment(workflow);

        payment = get(payment);
        payment.setStatus(ActStatus.POSTED);
        save(payment);

        fireDialogButton(workflow.getEditDialog(), PopupDialog.OK_ID);

        assertEquals(1, errors.size());

        assertEquals("Failed to save object. It may have been changed by another user.", errors.get(0));

        workflow.checkComplete();

        checkCharge(get(charge), otc, author, null, BigDecimal.ZERO, ONE, ActStatus.IN_PROGRESS);
        checkPayment(get(payment), otc, author, ONE, ActStatus.POSTED);
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        super.setUp();
        context = new LocalContext();
        Party location = TestHelper.createLocation();
        Party till = TestHelper.createTill();
        otc = (Party) create(CustomerArchetypes.OTC);
        otc.setName("X-OTC");
        EntityBean bean = new EntityBean(location);
        bean.addNodeRelationship("OTC", otc);
        bean.addNodeRelationship("tills", till);
        save(location, otc, till);
        context.setLocation(location);
        author = TestHelper.createUser();
        context.setUser(author);
        context.setTill(till);


        // register an ErrorHandler to collect errors
        ErrorHandler.setInstance(new ErrorHandler() {
            @Override
            public void error(Throwable cause) {
                errors.add(cause.getMessage());
            }

            public void error(String title, String message, Throwable cause, WindowPaneListener listener) {
                errors.add(message);
                if (listener != null) {
                    listener.windowPaneClosing(new WindowPaneEvent(this));
                }
            }
        });
    }

    /**
     * Verifies that if charging is cancelled, the workflow is ended and the charge is deleted.
     *
     * @param userClose if {@code true} cancel via the 'user close' button, otherwise use the 'cancel' button
     */
    private void checkCancelCharge(boolean userClose) {
        OverTheCounterWorkflowRunner workflow = new OverTheCounterWorkflowRunner(context);
        workflow.start();

        // edit the charge
        FinancialAct charge = editCharge(workflow);
        fireDialogButton(workflow.getEditDialog(), PopupDialog.OK_ID);

        // now cancel. The charge should be deleted.
        WorkflowTestHelper.cancelDialog(workflow.getEditDialog(), userClose);
        assertNull(get(charge));

        workflow.checkComplete();
    }

    /**
     * Verifies that if payment is cancelled, the workflow is ended and both the payment and charge is deleted.
     *
     * @param userClose if {@code true} cancel via the 'user close' button, otherwise use the 'cancel' button
     */
    private void checkCancelPayment(boolean userClose) {
        OverTheCounterWorkflowRunner workflow = new OverTheCounterWorkflowRunner(context);
        workflow.start();

        // edit the charge
        FinancialAct charge = editCharge(workflow);
        fireDialogButton(workflow.getEditDialog(), PopupDialog.OK_ID);

        // verify the charge has been saved
        checkCharge(get(charge), otc, author, null, BigDecimal.ZERO, ONE, ActStatus.IN_PROGRESS);

        // edit the payment
        FinancialAct payment = editPayment(workflow);

        // verify the payment has been saved
        checkPayment(get(payment), otc, author, ONE, ActStatus.IN_PROGRESS);

        // now cancel the payment
        WorkflowTestHelper.cancelDialog(workflow.getEditDialog(), userClose);

        // the charge and payment should have been removed
        assertNull(get(charge));
        assertNull(get(payment));

        workflow.checkComplete();
    }

    /**
     * Helper to edit the charge.
     *
     * @param workflow the workflow
     * @return the charge act
     */
    private FinancialAct editCharge(OverTheCounterWorkflowRunner workflow) {
        Product product = createProduct(ProductArchetypes.MERCHANDISE, ONE);

        TestOTCChargeTask chargeTask = workflow.getChargeTask();
        OTCChargeEditor chargeEditor = workflow.getChargeEditor();
        addItem(chargeEditor, null, product, ONE, chargeTask.getEditorQueue());
        fireDialogButton(chargeTask.getEditDialog(), PopupDialog.APPLY_ID); // force the charge to save

        FinancialAct charge = (FinancialAct) get(chargeEditor.getObject());
        assertNotNull(charge);
        checkCharge(charge, otc, author, null, BigDecimal.ZERO, ONE);
        assertEquals(ActStatus.IN_PROGRESS, charge.getStatus());
        return charge;
    }

    /**
     * Helper to edit the payment.
     *
     * @param workflow the workflow
     * @return the payment act
     */
    private FinancialAct editPayment(OverTheCounterWorkflowRunner workflow) {
        OTCPaymentTask paymentTask = workflow.getPaymentTask();
        OTCPaymentEditor paymentEditor = workflow.getPaymentEditor();
        PaymentItemEditor paymentItemEditor = paymentEditor.addItem();
        paymentItemEditor.getProperty("amount").setValue(ONE);
        assertTrue(paymentEditor.isValid());
        fireDialogButton(paymentTask.getEditDialog(), PopupDialog.APPLY_ID);  // force the payment to save

        // verify the payment has been saved
        FinancialAct payment = (FinancialAct) get(paymentEditor.getObject());
        payment = get(payment);
        assertNotNull(payment);
        checkPayment(payment, otc, author, ONE, ActStatus.IN_PROGRESS);
        return payment;
    }

    /**
     * Verifies a charge matches that expected.
     *
     * @param charge    the charge
     * @param customer  the expected customer
     * @param author    the expected author
     * @param clinician the expected clinician
     * @param tax       the expected tax
     * @param total     the expected total
     * @param status    the expected status
     */
    protected void checkCharge(FinancialAct charge, Party customer, User author, User clinician, BigDecimal tax,
                               BigDecimal total, String status) {
        assertNotNull(charge);
        ActBean bean = new ActBean(charge);
        assertEquals(customer.getObjectReference(), bean.getNodeParticipantRef("customer"));
        assertEquals(author.getObjectReference(), bean.getNodeParticipantRef("author"));
        if (bean.hasNode("clinician")) {
            assertEquals(clinician.getObjectReference(), bean.getNodeParticipantRef("clinician"));
        }
        checkEquals(tax, bean.getBigDecimal("tax"));
        checkEquals(total, bean.getBigDecimal("amount"));
        assertEquals(status, charge.getStatus());
    }

    /**
     * Verifies a payment matches that expected.
     *
     * @param payment  the payment
     * @param customer the expected customer
     * @param author   the expected author
     * @param total    the expected total
     * @param status   the expected status
     */
    protected void checkPayment(FinancialAct payment, Party customer, User author, BigDecimal total, String status) {
        assertNotNull(payment);
        ActBean bean = new ActBean(payment);
        assertEquals(customer.getObjectReference(), bean.getNodeParticipantRef("customer"));
        assertEquals(author.getObjectReference(), bean.getNodeParticipantRef("author"));
        checkEquals(total, bean.getBigDecimal("amount"));
        assertEquals(status, payment.getStatus());
    }

}
