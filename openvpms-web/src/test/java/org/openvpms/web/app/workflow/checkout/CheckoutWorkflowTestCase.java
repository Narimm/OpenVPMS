/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package org.openvpms.web.app.workflow.checkout;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.FinancialTestHelper;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.web.app.customer.charge.AbstractCustomerChargeActEditorTest;
import org.openvpms.web.app.workflow.WorkflowTestHelper;
import static org.openvpms.web.app.workflow.WorkflowTestHelper.cancelDialog;
import static org.openvpms.web.app.workflow.WorkflowTestHelper.createTask;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.dialog.PopupDialog;
import org.openvpms.web.component.im.edit.EditDialog;
import static org.openvpms.web.test.EchoTestHelper.fireDialogButton;

import java.math.BigDecimal;


/**
 * Tests the {@link CheckOutWorkflow}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class CheckoutWorkflowTestCase extends AbstractCustomerChargeActEditorTest {

    /**
     * The context.
     */
    private Context context;

    /**
     * The patient.
     */
    private Party patient;

    /**
     * The customer.
     */
    private Party customer;

    /**
     * The clinician.
     */
    private User clinician;

    /**
     * The till.
     */
    private Party till;


    /**
     * Tests the check-out workflow when started with an appointment.
     */
    @Test
    public void testCheckOutWorkflowForAppointment() {
        Act appointment = createAppointment();
        checkWorkflow(appointment);
    }

    /**
     * Tests the check-out workflow when started with a task.
     */
    @Test
    public void testCheckoutWorkflowForTask() {
        Act task = createTask(customer, patient, clinician);
        checkWorkflow(task);
    }

    /**
     * Verifies that closing the invoice edit dialog by the 'x' button cancels the workflow.
     */
    @Test
    public void testCancelInvoiceByUserCloseNoSave() {
        checkCancelInvoice(false, true);
    }

    /**
     * Verifies that closing the invoice edit dialog by the 'x' button cancels the workflow.
     */
    @Test
    public void testCancelInvoiceByUserCloseAfterSave() {
        checkCancelInvoice(true, true);
    }

    /**
     * Verifies that cancelling the invoice edit dialog by the 'Cancel' button cancels the workflow.
     */
    @Test
    public void testCancelInvoiceByCancelButtonNoSave() {
        checkCancelInvoice(false, false);
    }

    /**
     * Verifies that cancelling the invoice edit dialog by the 'Cancel' button cancels the workflow.
     */
    @Test
    public void testCancelInvoiceByCancelButtonAfterSave() {
        checkCancelInvoice(true, false);
    }

    /**
     * Tests the behaviour of clicking the 'no' button on the finalise invoice confirmation dialog.
     */
    @Test
    public void testNoFinaliseInvoice() {
        CheckoutWorkflowRunner workflow = new CheckoutWorkflowRunner(createAppointment(), getPractice(), context);
        workflow.start();
        BigDecimal amount = workflow.addInvoice(patient, clinician, false);
        workflow.confirm(PopupDialog.NO_ID);        // skip posting the invoice. Payment is skipped
        workflow.print();
        workflow.checkComplete(true);
        workflow.checkContext(context, customer, patient, null, clinician);
        workflow.checkInvoice(ActStatus.IN_PROGRESS, amount);
        assertNull(workflow.getPayment());
    }

    /**
     * Tests the behaviour of clicking the 'cancel' button on the finalise invoice confirmation dialog.
     */
    @Test
    public void testCancelFinaliseInvoice() {
        checkCancelFinaliseInvoice(false);
    }

    /**
     * Tests the behaviour of clicking the 'user close' button on the finalise invoice confirmation dialog.
     */
    @Test
    public void testUserCloseFinaliseInvoice() {
        checkCancelFinaliseInvoice(true);
    }

    /**
     * Verifies that the payment can be skipped.
     */
    @Test
    public void testSkipPayment() {
        CheckoutWorkflowRunner workflow = new CheckoutWorkflowRunner(createAppointment(), getPractice(), context);
        workflow.start();
        BigDecimal amount = workflow.addInvoice(patient, clinician, true);

        workflow.confirm(PopupDialog.NO_ID); // skip payment

        workflow.print();
        workflow.checkComplete(true);
        workflow.checkContext(context, customer, patient, null, clinician);
        workflow.checkInvoice(ActStatus.POSTED, amount);
        assertNull(workflow.getPayment());
    }

    /**
     * Verifies that the workflow cancels if the 'Cancel' button is pressed at the payment confirmation.
     */
    @Test
    public void testCancelPaymentConfirmation() {
        checkCancelPaymentConfirmation(false);
    }

    /**
     * Verifies that the workflow cancels if the 'user close' button is pressed at the payment confirmation.
     */
    @Test
    public void testUserClosePaymentConfirmation() {
        checkCancelPaymentConfirmation(true);
    }

    /**
     * Verifies that the workflow cancels after payment is cancelled.
     */
    @Test
    public void testCancelPayment() {
        checkCancelPayment(false);
    }

    /**
     * Verifies that the workflow cancels after payment is cancelled by pressing the 'user close' button.
     */
    @Test
    public void testCancelPaymentByUserClose() {
        checkCancelPayment(true);
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        super.setUp();
        Party location = TestHelper.createLocation();
        User user = TestHelper.createUser();
        context = new LocalContext();
        context.setLocation(location);
        context.setUser(user);

        customer = TestHelper.createCustomer();
        patient = TestHelper.createPatient(customer);
        clinician = TestHelper.createClinician();
        till = FinancialTestHelper.createTill();
    }

    /**
     * Runs the workflow for the specified act.
     *
     * @param act the act
     */
    private void checkWorkflow(Act act) {
        CheckoutWorkflowRunner workflow = new CheckoutWorkflowRunner(act, getPractice(), context);
        workflow.start();

        // first task to pause should by the invoice edit task.
        BigDecimal amount = workflow.addInvoice(patient, clinician, false);

        // second task to pause should be a confirmation, prompting to post the invoice
        workflow.confirm(PopupDialog.YES_ID);

        // verify the invoice has been posted
        workflow.checkInvoice(ActStatus.POSTED, amount);

        // third task to pause should be a confirmation prompting to pay the invoice
        workflow.confirm(PopupDialog.YES_ID);

        // 4th task to pause should be payment editor
        workflow.addPayment(till);
        workflow.checkPayment(ActStatus.POSTED, amount);

        // 5th task to pause should be print dialog
        workflow.print();

        workflow.checkComplete(true);
        workflow.checkContext(context, customer, patient, till, clinician);
    }

    /**
     * Verifies that cancelling the invoice cancels the workflow.
     *
     * @param save      if <tt>true</tt> save the invoice before cancelling
     * @param userClose if <tt>true</tt> cancel via the 'user close' button, otherwise use the 'cancel' button
     */
    private void checkCancelInvoice(boolean save, boolean userClose) {
        CheckoutWorkflowRunner workflow = new CheckoutWorkflowRunner(createAppointment(), getPractice(), context);
        workflow.start();

        // first task to pause should by the invoice edit task.
        BigDecimal amount = BigDecimal.valueOf(20);
        EditDialog dialog = workflow.addInvoiceItem(patient, amount, clinician);
        if (save) {
            fireDialogButton(dialog, PopupDialog.APPLY_ID);          // save the invoice
        }
        workflow.addInvoiceItem(patient, amount, clinician);                    // add another item. Won't be saved

        // close the dialog
        cancelDialog(dialog, userClose);

        if (save) {
            workflow.checkInvoice(ActStatus.IN_PROGRESS, amount);
        } else {
            FinancialAct invoice = workflow.getInvoice();
            assertNotNull(invoice);
            assertTrue(invoice.isNew()); // unsaved
        }

        workflow.checkComplete(false);
        workflow.checkContext(context, null, null, null, null);
        assertNull(workflow.getPayment());
    }

    /**
     * Verifies that the workflow cancels if the invoice confirmation dialog is cancelled.
     * @param userClose if <tt>true</tt> cancel via the 'user close' button, otherwise use the 'cancel' button
     */
    private void checkCancelFinaliseInvoice(boolean userClose) {
        CheckoutWorkflowRunner workflow = new CheckoutWorkflowRunner(createAppointment(), getPractice(), context);
        workflow.start();
        BigDecimal amount = workflow.addInvoice(patient, clinician, false);
        String id = (userClose) ? null : PopupDialog.CANCEL_ID;
        workflow.confirm(id);
        workflow.checkComplete(false);
        workflow.checkContext(context, null, null, null, null);
        workflow.checkInvoice(ActStatus.IN_PROGRESS, amount);
        assertNull(workflow.getPayment());
    }

    /**
     * Verifies that the workflow cancels if the payment confirmation is cancelled.
     * @param userClose if <tt>true</tt> cancel via the 'user close' button, otherwise use the 'cancel' button
     */
    private void checkCancelPaymentConfirmation(boolean userClose) {
        CheckoutWorkflowRunner workflow = new CheckoutWorkflowRunner(createAppointment(), getPractice(), context);
        workflow.start();
        BigDecimal amount = workflow.addInvoice(patient, clinician, true);

        String id = (userClose) ? null : PopupDialog.CANCEL_ID;
        workflow.confirm(id); // cancel payment

        workflow.checkComplete(false);
        workflow.checkContext(context, null, null, null, null);
        workflow.checkInvoice(ActStatus.POSTED, amount);
        assertNull(workflow.getPayment());
    }



    /**
     * Verifies that the workflow completes after payment is cancelled.
     *
     * @param userClose if <tt>true</tt> cancel via the 'user close' button, otherwise use the 'cancel' button
     */
    private void checkCancelPayment(boolean userClose) {
        CheckoutWorkflowRunner workflow = new CheckoutWorkflowRunner(createAppointment(), getPractice(), context);
        workflow.start();
        BigDecimal amount = workflow.addInvoice(patient, clinician, true);

        workflow.confirm(PopupDialog.YES_ID);
        EditDialog dialog = workflow.addPaymentItem(till);
        cancelDialog(dialog, userClose);

        workflow.checkComplete(false);
        workflow.checkContext(context, null, null, null, null);
        workflow.checkInvoice(ActStatus.POSTED, amount);
        FinancialAct payment = workflow.getPayment();
        assertNotNull(payment);
        assertTrue(payment.isNew()); // unsaved
    }

    /**
     * Helper to create an appointment.
     *
     * @return a new appointment
     */
    private Act createAppointment() {
        return WorkflowTestHelper.createAppointment(customer, patient, clinician);
    }

}
