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

package org.openvpms.web.workspace.workflow.checkout;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.FinancialTestHelper;
import org.openvpms.archetype.rules.patient.PatientTestHelper;
import org.openvpms.archetype.rules.workflow.AppointmentStatus;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.workspace.customer.charge.AbstractCustomerChargeActEditorTest;
import org.openvpms.web.workspace.workflow.WorkflowTestHelper;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.openvpms.web.test.EchoTestHelper.fireDialogButton;
import static org.openvpms.web.workspace.workflow.WorkflowTestHelper.cancelDialog;
import static org.openvpms.web.workspace.workflow.WorkflowTestHelper.createTask;


/**
 * Tests the {@link CheckOutWorkflow}.
 *
 * @author Tim Anderson
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
        Act appointment = createAppointment(clinician);
        checkWorkflow(appointment, clinician);
    }

    /**
     * Tests the check-out workflow when started with a task.
     */
    @Test
    public void testCheckoutWorkflowForTask() {
        Act appointment = createAppointment(clinician);
        appointment.setStatus(AppointmentStatus.CHECKED_IN);
        Act task = createTask(customer, patient, clinician);
        ActBean bean = new ActBean(appointment);
        bean.addNodeRelationship("tasks", task);
        save(appointment, task);
        checkWorkflow(task, clinician);
        appointment = get(appointment);
        assertEquals(AppointmentStatus.COMPLETED, appointment.getStatus());
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
        Act appointment = createAppointment(clinician);
        createEvent(appointment);
        CheckoutWorkflowRunner workflow = new CheckoutWorkflowRunner(appointment, getPractice(), context);
        workflow.start();
        BigDecimal amount = workflow.addInvoice(patient, false);
        workflow.confirm(PopupDialog.NO_ID);        // skip posting the invoice. Payment is skipped
        workflow.print();
        workflow.checkComplete(true);
        workflow.checkContext(context, customer, patient, null, clinician);
        workflow.checkInvoice(ActStatus.IN_PROGRESS, amount, clinician);
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
        CheckoutWorkflowRunner workflow = new CheckoutWorkflowRunner(createAppointment(clinician), getPractice(), context);
        workflow.start();
        BigDecimal amount = workflow.addInvoice(patient, true);

        workflow.confirm(PopupDialog.NO_ID); // skip payment

        workflow.print();
        workflow.checkComplete(true);
        workflow.checkContext(context, customer, patient, null, clinician);
        workflow.checkInvoice(ActStatus.POSTED, amount, clinician);
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
        clinician = TestHelper.createClinician();
        patient = TestHelper.createPatient(customer);
        till = FinancialTestHelper.createTill();
        EntityBean bean = new EntityBean(location);
        bean.addNodeRelationship("tills", till);
        save(location, till);
    }

    /**
     * Verifies that when the practice 'usedLoggedInClinician' is set true, the clinician comes from the context
     * user rather than the appointment.
     */
    @Test
    public void testUseLoggedInClinicianForClinician() {
        User clinician2 = TestHelper.createClinician();
        checkUseLoggedInClinician(true, clinician2, clinician, clinician2);
    }

    /**
     * Verifies that when the practice 'usedLoggedInClinician' is set true, and the logged in user is not a
     * clinician, the clinician comes from the appointment.
     */
    @Test
    public void testUseLoggedInClinicianForNonClinician() {
        User user = TestHelper.createUser();
        checkUseLoggedInClinician(true, user, clinician, clinician);
    }

    /**
     * Verifies that when the practice 'usedLoggedInClinician' is set false, the clinician comes from the context
     * user rather than the appointment.
     */
    @Test
    public void testUseLoggedInClinicianDisabledForClinician() {
        User clinician2 = TestHelper.createClinician();
        checkUseLoggedInClinician(false, clinician, clinician2, clinician2);
    }

    /**
     * Verifies that when the practice 'usedLoggedInClinician' is set false, and the logged in user is not a
     * clinician, the clinician comes from the appointment.
     */
    @Test
    public void testUseLoggedInClinicianDisabledForNonClinician() {
        User user = TestHelper.createUser();
        checkUseLoggedInClinician(false, user, clinician, clinician);
    }

    /**
     * Tests the effects of the practice useLoggedInClinician option during the Check-Out workflow.
     *
     * @param enabled              if {@code true}, enable the option, otherwise disable it
     * @param user                 the current user
     * @param appointmentClinician the appointment clinician
     * @param expectedClinician    the expected clinician on new acts
     */
    private void checkUseLoggedInClinician(boolean enabled, User user, User appointmentClinician,
                                           User expectedClinician) {
        context.setUser(user);
        context.setClinician(clinician);
        IMObjectBean bean = new IMObjectBean(getPractice());
        bean.setValue("useLoggedInClinician", enabled);
        Act appointment = createAppointment(appointmentClinician);
        checkWorkflow(appointment, expectedClinician);
    }

    /**
     * Runs the workflow for the specified act.
     *
     * @param act       the act
     * @param clinician the expected clinician
     */
    private void checkWorkflow(Act act, User clinician) {
        CheckoutWorkflowRunner workflow = new CheckoutWorkflowRunner(act, getPractice(), context);
        workflow.start();

        // first task to pause should by the invoice edit task.
        BigDecimal amount = workflow.addInvoice(patient, false);

        // second task to pause should be a confirmation, prompting to post the invoice
        workflow.confirm(PopupDialog.YES_ID);

        // verify the invoice has been posted
        workflow.checkInvoice(ActStatus.POSTED, amount, clinician);

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
        context.setClinician(clinician);
        CheckoutWorkflowRunner workflow = new CheckoutWorkflowRunner(createAppointment(clinician), getPractice(), context);
        workflow.start();

        // first task to pause should by the invoice edit task.
        BigDecimal fixedPrice = new BigDecimal("18.18");
        EditDialog dialog = workflow.addInvoiceItem(patient, fixedPrice);
        if (save) {
            fireDialogButton(dialog, PopupDialog.APPLY_ID);          // save the invoice
        }
        workflow.addInvoiceItem(patient, fixedPrice);     // add another item. Won't be saved

        // close the dialog
        cancelDialog(dialog, userClose);

        if (save) {
            BigDecimal fixedPriceIncTax = BigDecimal.valueOf(20);
            workflow.checkInvoice(ActStatus.IN_PROGRESS, fixedPriceIncTax, clinician);
        } else {
            FinancialAct invoice = workflow.getInvoice();
            assertNotNull(invoice);
            assertTrue(invoice.isNew()); // unsaved
        }

        workflow.checkComplete(false);
        workflow.checkContext(context, null, null, null, clinician);
        assertNull(workflow.getPayment());
    }

    /**
     * Verifies that the workflow cancels if the invoice confirmation dialog is cancelled.
     *
     * @param userClose if <tt>true</tt> cancel via the 'user close' button, otherwise use the 'cancel' button
     */
    private void checkCancelFinaliseInvoice(boolean userClose) {
        CheckoutWorkflowRunner workflow = new CheckoutWorkflowRunner(createAppointment(clinician), getPractice(), context);
        workflow.start();
        BigDecimal amount = workflow.addInvoice(patient, false);
        String id = (userClose) ? null : PopupDialog.CANCEL_ID;
        workflow.confirm(id);
        workflow.checkComplete(false);
        workflow.checkContext(context, null, null, null, null);
        workflow.checkInvoice(ActStatus.IN_PROGRESS, amount, clinician);
        assertNull(workflow.getPayment());
    }

    /**
     * Verifies that the workflow cancels if the payment confirmation is cancelled.
     *
     * @param userClose if <tt>true</tt> cancel via the 'user close' button, otherwise use the 'cancel' button
     */
    private void checkCancelPaymentConfirmation(boolean userClose) {
        CheckoutWorkflowRunner workflow = new CheckoutWorkflowRunner(createAppointment(clinician), getPractice(), context);
        workflow.start();
        BigDecimal amount = workflow.addInvoice(patient, true);

        String id = (userClose) ? null : PopupDialog.CANCEL_ID;
        workflow.confirm(id); // cancel payment

        workflow.checkComplete(false);
        workflow.checkContext(context, null, null, null, null);
        workflow.checkInvoice(ActStatus.POSTED, amount, clinician);
        assertNull(workflow.getPayment());
    }


    /**
     * Verifies that the workflow completes after payment is cancelled.
     *
     * @param userClose if <tt>true</tt> cancel via the 'user close' button, otherwise use the 'cancel' button
     */
    private void checkCancelPayment(boolean userClose) {
        CheckoutWorkflowRunner workflow = new CheckoutWorkflowRunner(createAppointment(clinician), getPractice(),
                                                                     context);
        workflow.start();
        BigDecimal amount = workflow.addInvoice(patient, true);

        workflow.confirm(PopupDialog.YES_ID);
        EditDialog dialog = workflow.addPaymentItem(till);
        cancelDialog(dialog, userClose);

        workflow.checkComplete(false);
        workflow.checkContext(context, null, null, null, null);
        workflow.checkInvoice(ActStatus.POSTED, amount, clinician);
        FinancialAct payment = workflow.getPayment();
        assertNotNull(payment);
        assertTrue(payment.isNew()); // unsaved
    }

    /**
     * Helper to create an appointment.
     *
     * @param clinician the clinician
     * @return a new appointment
     */
    private Act createAppointment(User clinician) {
        return WorkflowTestHelper.createAppointment(customer, patient, clinician, context.getLocation());
    }

    private Act createEvent(Act appointment) {
        Act event = PatientTestHelper.createEvent(patient);
        ActBean bean = new ActBean(appointment);
        bean.addNodeTarget("event", event);
        save(event, appointment);
        return event;
    }

}
