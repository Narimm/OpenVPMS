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

package org.openvpms.web.app.workflow.consult;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.workflow.WorkflowStatus;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.web.app.customer.charge.AbstractCustomerChargeActEditorTest;
import org.openvpms.web.app.patient.visit.VisitEditorDialog;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.dialog.PopupDialog;

import java.math.BigDecimal;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.openvpms.web.app.workflow.WorkflowTestHelper.cancelDialog;
import static org.openvpms.web.app.workflow.WorkflowTestHelper.createAppointment;
import static org.openvpms.web.app.workflow.WorkflowTestHelper.createTask;
import static org.openvpms.web.test.EchoTestHelper.fireDialogButton;


/**
 * Tests the {@link ConsultWorkflow}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class ConsultWorkflowTestCase extends AbstractCustomerChargeActEditorTest {

    /**
     * The customer.
     */
    private Party customer;

    /**
     * The patient.
     */
    private Party patient;

    /**
     * The clinician.
     */
    private User clinician;

    /**
     * The context.
     */
    private Context context;

    /**
     * Tests running a consult from an appointment.
     */
    @Test
    public void testConsultForAppointment() {
        Act appointment = createAppointment(customer, patient, clinician);
        checkConsultWorkflow(appointment);
    }

    /**
     * Tests running a consult from an test.
     */
    @Test
    public void testConsultForTask() {
        Act task = createTask(customer, patient, clinician);
        checkConsultWorkflow(task);
    }

    /**
     * Verifies that closing the invoice edit dialog by the 'x' button cancels the workflow.
     */
    @Test
    public void testCancelInvoiceByUserCloseNoSave() {
        checkCancelInvoice(false, true);
    }

    /**
     * Verifies that closing the invoice edit dialog by the 'user close' button cancels the workflow.
     * and that unsaved amounts don't affect the invoice.
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
     * Verifies that when the invoice is set to <em>COMPLETE</em>, the appointment status is changed to <em>BILLED</em>.
     */
    @Test
    public void testCompleteInvoiceStatusForAppointment() {
        Act appointment = createAppointment(customer, patient, clinician);
        checkCompleteInvoiceStatus(appointment);
    }

    /**
     * Verifies that when the invoice is set to <em>COMPLETE</em>, the appointment status is changed to <em>BILLED</em>.
     */
    @Test
    public void testCompleteInvoiceStatusForTask() {
        Act task = createTask(customer, patient, clinician);
        checkCompleteInvoiceStatus(task);
    }

    /**
     * Verifies that cancelling the invoice edit dialog by the 'Cancel' button cancels the workflow,
     * and that unsaved amounts don't affect the invoice.
     */
    @Test
    public void testCancelInvoiceByCancelButtonAfterSave() {
        checkCancelInvoice(true, false);
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        super.setUp();
        customer = TestHelper.createCustomer();
        patient = TestHelper.createPatient(customer);
        clinician = TestHelper.createClinician();
        context = new LocalContext();
        context.setLocation(TestHelper.createLocation());
        context.setUser(TestHelper.createUser());
    }

    /**
     * Tests the consult workflow.
     *
     * @param act the appointment/task
     */
    private void checkConsultWorkflow(Act act) {
        ConsultWorkflowRunner workflow = new ConsultWorkflowRunner(act, getPractice(), context);
        workflow.start();

        PopupDialog event = workflow.editVisit();
        workflow.addNote();
        workflow.addVisitInvoiceItem(patient, clinician);
        fireDialogButton(event, PopupDialog.OK_ID);

        workflow.checkComplete(ActStatus.IN_PROGRESS);
        workflow.checkContext(context, customer, patient, clinician);
    }

    /**
     * Verifies that cancelling the invoice cancels the workflow.
     *
     * @param save      if <tt>true</tt> save the invoice. and add an unsaved item before cancelling
     * @param userClose if <tt>true</tt> cancel by clicking the 'x' button, otherwise cancel via the 'Cancel' button
     */
    private void checkCancelInvoice(boolean save, boolean userClose) {
        Act appointment = createAppointment(customer, patient, clinician);
        ConsultWorkflowRunner workflow = new ConsultWorkflowRunner(appointment, getPractice(), context);
        workflow.start();

        // first task is to edit the clinical event 
        VisitEditorDialog dialog = workflow.editVisit();
        BigDecimal amount = BigDecimal.valueOf(20);
        workflow.addVisitInvoiceItem(patient, amount, clinician);

        // next is to edit the invoice
        if (save) {
            dialog.getEditor().selectCharges();
            fireDialogButton(dialog, PopupDialog.APPLY_ID);          // save the invoice
        }
        workflow.addVisitInvoiceItem(patient, amount, clinician);    // add another item. Won't be saved

        // close the dialog
        cancelDialog(dialog, userClose);

        if (save) {
            workflow.checkInvoice(ActStatus.IN_PROGRESS, amount);
        } else {
            FinancialAct invoice = workflow.getInvoice();
            assertNotNull(invoice);
            assertTrue(invoice.isNew()); // unsaved
        }

        workflow.checkComplete(ActStatus.IN_PROGRESS);
        workflow.checkContext(context, null, null, null);
    }

    /**
     * Verifies that when the invoice is set to <em>COMPLETE</em>, the appointment/task status is changed to
     * <em>BILLED</em>.
     *
     * @param act the appointment/task
     */
    private void checkCompleteInvoiceStatus(Act act) {
        ConsultWorkflowRunner workflow = new ConsultWorkflowRunner(act, getPractice(), context);
        workflow.start();

        VisitEditorDialog dialog = workflow.editVisit();
        workflow.addVisitInvoiceItem(patient, clinician);
        dialog.getEditor().getChargeEditor().setStatus(ActStatus.COMPLETED);
        fireDialogButton(dialog, PopupDialog.OK_ID);

        workflow.checkComplete(WorkflowStatus.BILLED);
        workflow.checkContext(context, customer, patient, clinician);
    }

}
