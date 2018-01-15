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

package org.openvpms.web.workspace.workflow.consult;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.patient.PatientTestHelper;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.workflow.ScheduleTestHelper;
import org.openvpms.archetype.rules.workflow.TaskStatus;
import org.openvpms.archetype.rules.workflow.WorkflowStatus;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.workspace.customer.charge.AbstractCustomerChargeActEditorTest;
import org.openvpms.web.workspace.customer.charge.CustomerChargeActItemEditor;
import org.openvpms.web.workspace.patient.visit.VisitEditorDialog;
import org.openvpms.web.workspace.workflow.checkin.CheckInWorkflowRunner;

import java.math.BigDecimal;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.openvpms.web.test.EchoTestHelper.fireDialogButton;
import static org.openvpms.web.workspace.workflow.WorkflowTestHelper.cancelDialog;
import static org.openvpms.web.workspace.workflow.WorkflowTestHelper.createAppointment;
import static org.openvpms.web.workspace.workflow.WorkflowTestHelper.createTask;
import static org.openvpms.web.workspace.workflow.WorkflowTestHelper.createWorkList;


/**
 * Tests the {@link ConsultWorkflow}.
 *
 * @author Tim Anderson
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
     * The practice location.
     */
    private Party location;

    /**
     * The context.
     */
    private Context context;

    /**
     * Tests running a consult from an appointment.
     */
    @Test
    public void testConsultForAppointment() {
        Date date = new Date();
        Act appointment = createAppointment(date, customer, patient, clinician, location);

        // create a COMPLETED event for the previous date, with no end date. This should not be used by check-in
        // or consult
        Act previousEvent = PatientTestHelper.createEvent(DateRules.getYesterday(), null, patient, clinician);
        previousEvent.setStatus(ActStatus.COMPLETED);
        save(previousEvent);

        Entity taskType = ScheduleTestHelper.createTaskType();
        Party workList = createWorkList(taskType, 1);
        CheckInWorkflowRunner runner = new CheckInWorkflowRunner(appointment, getPractice(), context);

        Act event = runner.runWorkflow(patient, customer, workList, date, clinician, location);
        assertNotEquals(previousEvent, event); // new event should have been created

        checkConsultWorkflow(appointment, event, clinician);
    }

    /**
     * Tests running a consult from a task.
     */
    @Test
    public void testConsultForTask() {
        patient = TestHelper.createPatient(customer);
        Date date = new Date();

        // create a COMPLETED event for the previous date, with no end date. This should not be used by check-in
        // or consult
        Act previousEvent = PatientTestHelper.createEvent(DateRules.getYesterday(), null, patient, clinician);
        previousEvent.setStatus(ActStatus.COMPLETED);
        save(previousEvent);

        Act appointment = createAppointment(date, customer, patient, clinician, location);
        Entity taskType = ScheduleTestHelper.createTaskType();
        Party workList = createWorkList(taskType, 1);
        CheckInWorkflowRunner runner = new CheckInWorkflowRunner(appointment, getPractice(), context);

        Act event = runner.runWorkflow(patient, customer, workList, date, clinician, location);
        assertNotEquals(previousEvent, event); // new event should have been created

        Act task = runner.checkTask(workList, customer, patient, TaskStatus.PENDING);

        checkConsultWorkflow(task, event, clinician);
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
        Act appointment = createAppointment(customer, patient, clinician, location);
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
     * Verifies that when the invoice is set to <em>IN_PROGRESS</em> from COMPLETE, the appointment status is remains
     * <em>BILLED</em>.
     */
    @Test
    public void testInProgressInvoiceStatusForBilledAppointment() {
        Act appointment = createAppointment(customer, patient, clinician, location);
        checkChangeCompleteInvoiceToInProgress(appointment, WorkflowStatus.BILLED);
    }

    /**
     * Verifies that when the invoice is set to {@code IN_PROGRESS} from {@code COMPLETE}, the appointment status
     * remains {@code COMPLETE}.
     */
    @Test
    public void testInProgressInvoiceStatusForCompletedAppointment() {
        Act appointment = createAppointment(customer, patient, clinician, location);
        checkChangeCompleteInvoiceToInProgress(appointment, WorkflowStatus.COMPLETED);
    }

    /**
     * Verifies that when the invoice is set to <em>IN_PROGRESS</em> from COMPLETE, the appointment status is remains
     * <em>BILLED</em>.
     */
    @Test
    public void testInProgressInvoiceStatusForBilledTask() {
        Act task = createTask(customer, patient, clinician);
        checkChangeCompleteInvoiceToInProgress(task, WorkflowStatus.BILLED);
    }

    /**
     * Verifies that when the invoice is set to {@code IN_PROGRESS} from {@code COMPLETE}, the appointment status
     * remains {@code COMPLETE}.
     */
    @Test
    public void testInProgressInvoiceStatusForCompletedTask() {
        Act task = createTask(customer, patient, clinician);
        checkChangeCompleteInvoiceToInProgress(task, WorkflowStatus.COMPLETED);
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
     * Verifies that when the practice 'usedLoggedInClinician' is set true, the clinician comes from the context
     * user rather than the appointment.
     */
    @Test
    public void testUseLoggedInClinicianForClinician() {
        checkUseLoggedInClinician(true, clinician, TestHelper.createClinician(), clinician);
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
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        super.setUp();
        customer = TestHelper.createCustomer();
        patient = TestHelper.createPatient(customer);
        clinician = TestHelper.createClinician();
        context = new LocalContext();
        location = TestHelper.createLocation();
        context.setLocation(location);
        context.setUser(TestHelper.createUser());
    }

    /**
     * Tests the effects of the practice useLoggedInClinician option during the Consult workflow.
     *
     * @param enabled              if {@code true}, enable the option, otherwise disable it
     * @param user                 the current user
     * @param appointmentClinician the appointment clinician
     * @param expectedClinician    the expected clinician on new acts
     */
    private void checkUseLoggedInClinician(boolean enabled, User user, User appointmentClinician,
                                           User expectedClinician) {
        IMObjectBean bean = new IMObjectBean(getPractice());
        bean.setValue("useLoggedInClinician", enabled);

        context.setUser(user);
        context.setClinician(clinician);

        Date date = new Date();
        Act appointment = createAppointment(date, customer, patient, appointmentClinician, location);

        // create a COMPLETED event for the previous date, with no end date. This should not be used by check-in
        // or consult
        Act previousEvent = PatientTestHelper.createEvent(DateRules.getYesterday(), null, patient,
                                                          appointmentClinician);
        previousEvent.setStatus(ActStatus.COMPLETED);
        save(previousEvent);

        Entity taskType = ScheduleTestHelper.createTaskType();
        Party workList = createWorkList(taskType, 1);
        CheckInWorkflowRunner runner = new CheckInWorkflowRunner(appointment, getPractice(), context);

        Act event = runner.runWorkflow(patient, customer, workList, date, expectedClinician, location);
        assertNotEquals(previousEvent, event); // new event should have been created

        checkConsultWorkflow(appointment, event, expectedClinician);
    }

    /**
     * Tests the consult workflow.
     *
     * @param act       the appointment/task
     * @param event     the event created at check-in
     * @param clinician the expected clinician
     */
    private void checkConsultWorkflow(Act act, Act event, User clinician) {
        ConsultWorkflowRunner workflow = new ConsultWorkflowRunner(act, getPractice(), context);
        workflow.start();

        VisitEditorDialog dialog = workflow.editVisit();
        assertEquals(event, dialog.getEditor().getEvent());
        workflow.addNote();
        CustomerChargeActItemEditor itemEditor = workflow.addVisitInvoiceItem(patient, new BigDecimal(20));
        assertEquals(clinician, itemEditor.getClinician());
        fireDialogButton(dialog, PopupDialog.OK_ID);

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
        Act appointment = createAppointment(customer, patient, clinician, location);
        ConsultWorkflowRunner workflow = new ConsultWorkflowRunner(appointment, getPractice(), context);
        workflow.start();

        // first task is to edit the clinical event 
        VisitEditorDialog dialog = workflow.editVisit();
        BigDecimal fixedPrice = new BigDecimal("18.18");
        workflow.addVisitInvoiceItem(patient, fixedPrice);

        // next is to edit the invoice
        if (save) {
            dialog.getEditor().selectCharges();
            fireDialogButton(dialog, PopupDialog.APPLY_ID);          // save the invoice
        }
        workflow.addVisitInvoiceItem(patient, fixedPrice);    // add another item. Won't be saved

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
        workflow.addVisitInvoiceItem(patient, new BigDecimal(20));
        dialog.getEditor().getChargeEditor().setStatus(ActStatus.COMPLETED);
        fireDialogButton(dialog, PopupDialog.OK_ID);

        workflow.checkComplete(WorkflowStatus.BILLED);
        workflow.checkContext(context, customer, patient, clinician);
    }

    /**
     * Transitions a {@code COMPLETED} invoice to {@code IN_PROGRESS} and verifies that an appointment/task that
     * is {@code COMPLETED} or {@code BILLED} does not change status.
     *
     * @param act            the appointment or task
     * @param expectedStatus the expected appointment/task status
     */
    private void checkChangeCompleteInvoiceToInProgress(Act act, String expectedStatus) {
        // runs the workflow to create a COMPLETE invoice, and sets the appointment/task BILLED
        checkCompleteInvoiceStatus(act);
        if (!act.getStatus().equals(expectedStatus)) {
            act.setStatus(expectedStatus);
            save(act);
        }

        // run the workflow again, but this time mark the invoice IN_PROGRESS
        ConsultWorkflowRunner workflow = new ConsultWorkflowRunner(act, getPractice(), context);
        workflow.start();

        VisitEditorDialog dialog = workflow.editVisit();
        workflow.addVisitInvoiceItem(patient, new BigDecimal(20));
        dialog.getEditor().getChargeEditor().setStatus(ActStatus.IN_PROGRESS);
        fireDialogButton(dialog, PopupDialog.OK_ID);

        // verify the invoice is now IN_PROGRESS
        Act invoice = get(workflow.getInvoice());
        assertNotNull(invoice);
        assertEquals(ActStatus.IN_PROGRESS, invoice.getStatus());

        // verify the appointment/task is that expected
        workflow.checkComplete(expectedStatus);
        workflow.checkContext(context, customer, patient, clinician);
    }

}
