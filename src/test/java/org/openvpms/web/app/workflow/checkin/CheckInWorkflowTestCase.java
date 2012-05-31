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

package org.openvpms.web.app.workflow.checkin;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.archetype.rules.workflow.ScheduleTestHelper;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.web.app.workflow.WorkflowTestHelper;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.dialog.PopupDialog;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.query.BrowserDialog;
import org.openvpms.web.test.AbstractAppTest;

import java.math.BigDecimal;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.openvpms.web.app.workflow.WorkflowTestHelper.createAppointment;
import static org.openvpms.web.app.workflow.WorkflowTestHelper.createWorkList;
import static org.openvpms.web.test.EchoTestHelper.fireDialogButton;


/**
 * Tests the {@link CheckInWorkflow}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class CheckInWorkflowTestCase extends AbstractAppTest {

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
     * The context to pass to the workflow.
     */
    private Context context;

    /**
     * The work list.
     */
    private Party workList;


    /**
     * Tests the check-in workflow when launched from an appointment with no patient.
     */
    @Test
    public void testCheckInFromAppointmentNoPatient() {
        Act appointment = createAppointment(customer, null, clinician);
        CheckInWorkflowRunner workflow = new CheckInWorkflowRunner(appointment, context);
        workflow.setPatient(patient);                              // need to pre-set patient and worklist
        workflow.setWorkList(workList);                            // so they can be selected in popups
        workflow.start();

        // as the appointment has no patient, a pop should be displayed to select one
        workflow.selectPatient(patient);

        // select the work list and verify a task has been created.
        workflow.selectWorkList(workList, customer, patient);

        // add the patient weight
        workflow.addWeight(patient, BigDecimal.valueOf(10));

        workflow.printDocumentForm(PopupDialog.SKIP_ID);

        // edit the clinical event
        PopupDialog eventDialog = workflow.editVisit();
        fireDialogButton(eventDialog, PopupDialog.OK_ID);
        workflow.checkEvent(patient, clinician, ActStatus.IN_PROGRESS);

        // verify the workflow is complete
        workflow.checkComplete(true, customer, patient, context);
    }

    /**
     * Tests the check-in workflow when launched from an appointment with a patient.
     * <p/>
     * No patient selection dialog should be displayed.
     */
    @Test
    public void testCheckInFromAppointmentWithPatient() {
        Act appointment = createAppointment(customer, patient, clinician);
        CheckInWorkflowRunner workflow = new CheckInWorkflowRunner(appointment, context);
        workflow.setWorkList(workList);        // need to pre-set work list so it can be selected in popup
        workflow.start();

        workflow.selectWorkList(workList, customer, patient);

        workflow.addWeight(patient, BigDecimal.valueOf(20));

        workflow.printDocumentForm(PopupDialog.SKIP_ID);

        // edit the clinical event
        PopupDialog eventDialog = workflow.editVisit();
        fireDialogButton(eventDialog, PopupDialog.OK_ID);
        workflow.checkEvent(patient, clinician, ActStatus.IN_PROGRESS);
        workflow.checkComplete(true, customer, patient, context);
    }

    /**
     * Verifies that a new patient can be created if the appointment doesn't have one.
     */
    @Test
    public void testCreatePatient() {
        Act appointment = createAppointment(customer, null, clinician);
        CheckInWorkflowRunner workflow = new CheckInWorkflowRunner(appointment, context);
        workflow.setWorkList(workList); // need to pre-set work list so it can be selected in popup
        workflow.start();

        // create the new patient
        BrowserDialog<Party> dialog = workflow.getSelectionDialog();
        fireDialogButton(dialog, BrowserDialog.NEW_ID);
        EditDialog editDialog = workflow.editPatient("Fluffy");
        Party newPatient = (Party) editDialog.getEditor().getObject();
        fireDialogButton(editDialog, PopupDialog.OK_ID);

        // verify the patient has been created and is owned by the customer
        workflow.checkPatient(newPatient, customer);

        workflow.selectWorkList(workList, customer, newPatient);

        workflow.addWeight(newPatient, BigDecimal.ONE);

        workflow.printDocumentForm(PopupDialog.SKIP_ID);

        PopupDialog eventDialog = workflow.editVisit();
        fireDialogButton(eventDialog, PopupDialog.OK_ID);
        workflow.checkEvent(newPatient, clinician, ActStatus.IN_PROGRESS);

        workflow.checkComplete(true, customer, newPatient, context);
    }

    /**
     * Verify that the workflow cancels if a new patient is created but editing cancelled via the cancel button.
     */
    @Test
    public void testCancelCreatePatient() {
        Act appointment = createAppointment(customer, null, clinician);
        CheckInWorkflowRunner workflow = new CheckInWorkflowRunner(appointment, context);
        workflow.start();

        BrowserDialog<Party> dialog = workflow.getSelectionDialog();
        fireDialogButton(dialog, BrowserDialog.NEW_ID);
        EditDialog editDialog = workflow.editPatient("Fluffy");
        Party patient = (Party) editDialog.getEditor().getObject();
        fireDialogButton(editDialog, PopupDialog.CANCEL_ID);
        assertNull(get(patient));
        workflow.checkComplete(false, null, null, context);
    }

    /**
     * Verify that the workflow cancels if a new patient is created but editing cancelled via the 'user close' button.
     */
    @Test
    public void testCancelCreatePatientByUserClose() {
        Act appointment = createAppointment(customer, null, clinician);
        CheckInWorkflowRunner workflow = new CheckInWorkflowRunner(appointment, context);
        workflow.start();

        BrowserDialog<Party> dialog = workflow.getSelectionDialog();
        fireDialogButton(dialog, BrowserDialog.NEW_ID);
        EditDialog editDialog = workflow.editPatient("Fluffy");
        Party patient = (Party) editDialog.getEditor().getObject();
        editDialog.userClose();
        assertNull(get(patient));
        workflow.checkComplete(false, null, null, context);
    }

    /**
     * Verify that the workflow cancels if a patient selection is cancelled via the cancel button.
     */
    @Test
    public void testCancelSelectPatient() {
        checkCancelSelectPatient(false);
    }

    /**
     * Verify that the workflow cancels if a patient selection is cancelled via the 'user close' button.
     */
    @Test
    public void testCancelSelectPatientByUserClose() {
        checkCancelSelectPatient(true);
    }

    /**
     * Verifies that selecting a work list can be skipped, and that no task is created.
     */
    @Test
    public void testSkipSelectWorkList() {
        Act appointment = createAppointment(customer, patient, clinician);
        CheckInWorkflowRunner workflow = new CheckInWorkflowRunner(appointment, context);
        workflow.setWorkList(workList);        // need to pre-set work list so it can be selected in popup
        workflow.start();

        // skip work-list selection and verify no task is created
        BrowserDialog<Act> browser = workflow.getSelectionDialog();
        fireDialogButton(browser, PopupDialog.SKIP_ID);
        assertNull(workflow.getContext().getObject(ScheduleArchetypes.TASK));

        // add the patient weight
        workflow.addWeight(patient, BigDecimal.valueOf(10));

        // skip form printing
        workflow.printDocumentForm(PopupDialog.SKIP_ID);

        // edit the clinical event
        PopupDialog eventDialog = workflow.editVisit();
        fireDialogButton(eventDialog, PopupDialog.OK_ID);
        workflow.checkEvent(patient, clinician, ActStatus.IN_PROGRESS);

        // verify the workflow is complete
        workflow.checkComplete(true, customer, patient, context);
    }

    /**
     * Verify that the workflow cancels if a work-list selection is cancelled via the cancel button.
     */
    @Test
    public void testCancelSelectWorklist() {
        checkCancelSelectWorkList(false);
    }

    /**
     * Verify that the workflow cancels if a work-list selection is cancelled via the 'user close' button.
     */
    @Test
    public void testCancelSelectWorklistByUserClose() {
        checkCancelSelectWorkList(true);
    }

    /**
     * Verify that the workflow cancels if document selection is cancelled via the cancel button.
     */
    @Test
    public void testCancelSelectDocument() {
        checkCancelSelectDialog(false);
    }

    /**
     * Verify that the workflow cancels if document selection is cancelled via the 'user close' button.
     */
    @Test
    public void testCancelSelectDocumentByUserClose() {
        checkCancelSelectDialog(true);
    }

    /**
     * Tests the behaviour of cancelling the clinical event edit using the cancel button.
     */
    @Test
    public void testCancelEditEvent() {
        checkCancelEditEvent(false);
    }

    /**
     * Tests the behaviour of cancelling the clinical event edit using the 'user close' button.
     */
    @Test
    public void testCancelEditEventByUserClose() {
        checkCancelEditEvent(true);
    }

    /**
     * Verifies that no patient weight act is created if it is skipped.
     */
    @Test
    public void testSkipPatientWeight() {
        Act appointment = createAppointment(customer, patient, clinician);
        CheckInWorkflowRunner workflow = new CheckInWorkflowRunner(appointment, context);
        workflow.setWorkList(workList);        // need to pre-set work list so it can be selected in popup
        workflow.start();

        workflow.selectWorkList(workList, customer, patient);

        // skip the weight entry and verify that the context has a weight act that is unsaved
        fireDialogButton(workflow.getEditDialog(), PopupDialog.SKIP_ID);
        IMObject weight = workflow.getContext().getObject(PatientArchetypes.PATIENT_WEIGHT);
        assertNotNull(weight);
        assertTrue(weight.isNew());

        workflow.printDocumentForm(PopupDialog.SKIP_ID);

        // edit the clinical event
        PopupDialog eventDialog = workflow.editVisit();
        fireDialogButton(eventDialog, PopupDialog.OK_ID);
        workflow.checkEvent(patient, clinician, ActStatus.IN_PROGRESS);

        workflow.checkComplete(true, customer, patient, context);
    }

    /**
     * Verify that the workflow cancels if weight input is cancelled via the cancel button.
     */
    @Test
    public void testCancelEditPatientWeight() {
        checkCancelPatientWeight(false);
    }

    /**
     * Verify that the workflow cancels if weight input is cancelled via the 'user close' button.
     */
    @Test
    public void testCancelEditPatientWeightByUserClose() {
        checkCancelPatientWeight(true);
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
        User user = TestHelper.createUser();
        Entity taskType = ScheduleTestHelper.createTaskType();
        workList = createWorkList(taskType, 1);
        context = new LocalContext();
        context.setLocation(TestHelper.createLocation());
        context.setUser(user);
    }

    /**
     * Verify that the workflow cancels if a patient selection is cancelled.
     *
     * @param userClose if <tt>true</tt> cancel via the 'user close' button, otherwise use the 'cancel' button
     */
    private void checkCancelSelectPatient(boolean userClose) {
        Act appointment = createAppointment(customer, null, clinician);
        CheckInWorkflowRunner workflow = new CheckInWorkflowRunner(appointment, context);
        workflow.setPatient(patient);         // need to pre-set patient so it can be selected in popup
        workflow.start();

        BrowserDialog<Party> dialog = workflow.getSelectionDialog();
        WorkflowTestHelper.cancelDialog(dialog, userClose);
        workflow.checkComplete(false, null, null, context);
    }

    /**
     * Tests that the workflow cancels if the work-list selection is cancelled.
     *
     * @param userClose if <tt>true</tt> cancel via the 'user close' button, otherwise use the 'cancel' button
     */
    private void checkCancelSelectWorkList(boolean userClose) {
        Act appointment = createAppointment(customer, patient, clinician);
        CheckInWorkflowRunner workflow = new CheckInWorkflowRunner(appointment, context);
        workflow.setWorkList(workList);        // need to pre-set work-list so it can be selected in popup
        workflow.start();

        // cancel work-list selection and verify no task is created
        PopupDialog dialog = workflow.getSelectionDialog();
        WorkflowTestHelper.cancelDialog(dialog, userClose);
        assertNull(workflow.getContext().getObject(ScheduleArchetypes.TASK));

        // verify the workflow is complete
        workflow.checkComplete(false, null, null, context);
    }

    /**
     * Verify that the workflow cancels if document selection is cancelled via the cancel button.
     *
     * @param userClose if <tt>true</tt> cancel the selection by the 'user close' button otherwise via the cancel button
     */
    private void checkCancelSelectDialog(boolean userClose) {
        Act appointment = createAppointment(customer, patient, clinician);
        CheckInWorkflowRunner workflow = new CheckInWorkflowRunner(appointment, context);
        workflow.setWorkList(workList);        // need to pre-set work-list so it can be selected in popup
        workflow.start();

        workflow.selectWorkList(workList, customer, patient);

        workflow.addWeight(patient, BigDecimal.ONE);

        BrowserDialog<Act> dialog = workflow.getSelectionDialog();
        WorkflowTestHelper.cancelDialog(dialog, userClose);

        workflow.checkComplete(false, null, null, context);
    }

    /**
     * Tests the behaviour of cancelling the clinical event edit. The event should save, and the workflow cancel.
     *
     * @param userClose if <tt>true</tt> cancel the edit by the 'user close' button otherwise via the cancel button
     */
    private void checkCancelEditEvent(boolean userClose) {
        Act appointment = createAppointment(customer, patient, clinician);
        CheckInWorkflowRunner workflow = new CheckInWorkflowRunner(appointment, context);
        workflow.setWorkList(workList);        // need to pre-set work list so it can be selected in popup
        workflow.start();

        workflow.selectWorkList(workList, customer, patient);

        workflow.addWeight(patient, BigDecimal.ONE);

        workflow.printDocumentForm(PopupDialog.SKIP_ID);

        // edit the clinical event
        PopupDialog eventDialog = workflow.editVisit();
        WorkflowTestHelper.cancelDialog(eventDialog, userClose);

        // event is saved regardless of cancel
        workflow.checkEvent(patient, clinician, ActStatus.COMPLETED);
        workflow.checkComplete(false, null, null, context);
    }

    /**
     * Verify that the workflow cancels if weight input is cancelled via the 'user close' button.
     *
     * @param userClose if <tt>true</tt> cancel via the 'user close' button, otherwise use the 'cancel' button
     */
    private void checkCancelPatientWeight(boolean userClose) {
        Act appointment = createAppointment(customer, patient, clinician);
        CheckInWorkflowRunner workflow = new CheckInWorkflowRunner(appointment, context);
        workflow.setWorkList(workList);        // need to pre-set work list so it can be selected in popup
        workflow.start();

        workflow.selectWorkList(workList, customer, patient);

        EditDialog editor = workflow.getWeightEditor();
        WorkflowTestHelper.cancelDialog(editor, userClose);
        workflow.checkComplete(false, null, null, context);
    }

}
