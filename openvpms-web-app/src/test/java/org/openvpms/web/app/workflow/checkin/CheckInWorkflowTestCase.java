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
 */

package org.openvpms.web.app.workflow.checkin;

import nextapp.echo2.app.event.WindowPaneListener;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.archetype.rules.workflow.ScheduleTestHelper;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.app.customer.charge.AbstractCustomerChargeActEditorTest;
import org.openvpms.web.app.customer.charge.CustomerChargeTestHelper;
import org.openvpms.web.app.patient.charge.VisitChargeItemEditor;
import org.openvpms.web.app.patient.visit.VisitEditorDialog;
import org.openvpms.web.app.workflow.WorkflowTestHelper;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.component.im.doc.DocumentTestHelper;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.query.BrowserDialog;
import org.openvpms.web.echo.error.ErrorHandler;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.openvpms.web.app.workflow.WorkflowTestHelper.createAppointment;
import static org.openvpms.web.app.workflow.WorkflowTestHelper.createWorkList;
import static org.openvpms.web.test.EchoTestHelper.fireDialogButton;


/**
 * Tests the {@link CheckInWorkflow}.
 *
 * @author Tim Anderson
 */
public class CheckInWorkflowTestCase extends AbstractCustomerChargeActEditorTest {

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
     * Tracks errors logged.
     */
    private List<String> errors = new ArrayList<String>();

    /**
     * Tests the check-in workflow when launched from an appointment with no patient.
     */
    @Test
    public void testCheckInFromAppointmentNoPatient() {
        Act appointment = createAppointment(customer, null, clinician);
        CheckInWorkflowRunner workflow = new CheckInWorkflowRunner(appointment, getPractice(), context);
        workflow.setPatient(patient);                              // need to pre-set patient and worklist
        workflow.setWorkList(workList);                            // so they can be selected in popups
        workflow.start();

        // as the appointment has no patient, a pop should be displayed to select one
        workflow.selectPatient(patient);

        // select the work list and verify a task has been created.
        workflow.selectWorkList(workList, customer, patient);

        // add the patient weight
        workflow.addWeight(patient, BigDecimal.valueOf(10), clinician);

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
        CheckInWorkflowRunner workflow = new CheckInWorkflowRunner(appointment, getPractice(), context);
        workflow.setWorkList(workList);        // need to pre-set work list so it can be selected in popup
        workflow.start();

        workflow.selectWorkList(workList, customer, patient);

        workflow.addWeight(patient, BigDecimal.valueOf(20), clinician);

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
        CheckInWorkflowRunner workflow = new CheckInWorkflowRunner(appointment, getPractice(), context);
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

        workflow.addWeight(newPatient, BigDecimal.ONE, clinician);

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
        CheckInWorkflowRunner workflow = new CheckInWorkflowRunner(appointment, getPractice(), context);
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
        CheckInWorkflowRunner workflow = new CheckInWorkflowRunner(appointment, getPractice(), context);
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
        CheckInWorkflowRunner workflow = new CheckInWorkflowRunner(appointment, getPractice(), context);
        workflow.setWorkList(workList);        // need to pre-set work list so it can be selected in popup
        workflow.start();

        // skip work-list selection and verify no task is created
        BrowserDialog<Act> browser = workflow.getSelectionDialog();
        fireDialogButton(browser, PopupDialog.SKIP_ID);
        assertNull(workflow.getContext().getObject(ScheduleArchetypes.TASK));

        // add the patient weight
        workflow.addWeight(patient, BigDecimal.valueOf(10), clinician);

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
     * Verifies that if there is no clinician in the appointment, it defaults to that of the context.
     */
    @Test
    public void testDefaultClinicianFromContext() {
        Act appointment = createAppointment(customer, patient, null);  // no clinician on appointment
        context.setClinician(clinician);
        checkClinician(appointment, clinician, context);
    }

    /**
     * Verifies that if there is no clinician on the appointment or context, then no clinician is populated.
     */
    @Test
    public void testNoClinician() {
        Act appointment = createAppointment(customer, patient, null);  // no clinician on appointment
        checkClinician(appointment, null, context);
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
        CheckInWorkflowRunner workflow = new CheckInWorkflowRunner(appointment, getPractice(), context);
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

    @Test
    public void testChangeClinicianOnInvoiceItem() {
        Act appointment = createAppointment(customer, patient, clinician);
        CheckInWorkflowRunner workflow = new CheckInWorkflowRunner(appointment, getPractice(), context);
        workflow.start();

        fireDialogButton(workflow.getSelectionDialog(), PopupDialog.SKIP_ID);
        fireDialogButton(workflow.getWeightEditor(), PopupDialog.SKIP_ID);
        workflow.printDocumentForm(PopupDialog.SKIP_ID);

        Product product = CustomerChargeTestHelper.createProduct(ProductArchetypes.MEDICATION, BigDecimal.TEN,
                                                                 getPractice());
        Entity template1 = addDocumentTemplate(product);
        Entity template2 = addDocumentTemplate(product);

        // edit the charge
        VisitEditorDialog dialog = workflow.getVisitEditorDialog();
        dialog.getEditor().selectCharges(); // make sure the charges tab is selected, to enable the Apply button
        VisitChargeItemEditor itemEditor = workflow.addVisitInvoiceItem(patient, clinician, product);
        itemEditor.setClinician(clinician);
        fireDialogButton(dialog, PopupDialog.APPLY_ID);

        List<Act> documents1 = getDocuments(itemEditor);
        assertEquals(2, documents1.size());

        itemEditor.setClinician(TestHelper.createClinician());
        fireDialogButton(dialog, PopupDialog.OK_ID);

        List<Act> documents2 = getDocuments(itemEditor);
        assertEquals(2, documents2.size());

        workflow.checkEvent(patient, clinician, ActStatus.IN_PROGRESS);
        workflow.checkComplete(true, customer, patient, context);

        assertTrue(errors.isEmpty());
    }

    private Act getPatientDocumentForm(VisitChargeItemEditor itemEditor) {
        List<Act> documents = getDocuments(itemEditor);
        assertEquals(1, documents.size());
        Act document = documents.get(0);
        assertTrue(TypeHelper.isA(document, PatientArchetypes.DOCUMENT_FORM));
        return document;
    }

    private List<Act> getDocuments(VisitChargeItemEditor itemEditor) {
        ActBean bean = new ActBean((Act) itemEditor.getObject());
        return bean.getNodeActs("documents");
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

        // register an ErrorHandler to collect errors
        ErrorHandler.setInstance(new ErrorHandler() {
            public void error(String title, String message, Throwable cause, WindowPaneListener listener) {
                errors.add(message);
            }
        });
    }

    /**
     * Verify that the workflow cancels if a patient selection is cancelled.
     *
     * @param userClose if <tt>true</tt> cancel via the 'user close' button, otherwise use the 'cancel' button
     */
    private void checkCancelSelectPatient(boolean userClose) {
        Act appointment = createAppointment(customer, null, clinician);
        CheckInWorkflowRunner workflow = new CheckInWorkflowRunner(appointment, getPractice(), context);
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
        CheckInWorkflowRunner workflow = new CheckInWorkflowRunner(appointment, getPractice(), context);
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
        CheckInWorkflowRunner workflow = new CheckInWorkflowRunner(appointment, getPractice(), context);
        workflow.setWorkList(workList);        // need to pre-set work-list so it can be selected in popup
        workflow.start();

        workflow.selectWorkList(workList, customer, patient);

        workflow.addWeight(patient, BigDecimal.ONE, clinician);

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
        CheckInWorkflowRunner workflow = new CheckInWorkflowRunner(appointment, getPractice(), context);
        workflow.setWorkList(workList);        // need to pre-set work list so it can be selected in popup
        workflow.start();

        workflow.selectWorkList(workList, customer, patient);

        workflow.addWeight(patient, BigDecimal.ONE, clinician);

        workflow.printDocumentForm(PopupDialog.SKIP_ID);

        // edit the clinical event
        PopupDialog eventDialog = workflow.editVisit();
        WorkflowTestHelper.cancelDialog(eventDialog, userClose);

        // event is saved regardless of cancel
        workflow.checkEvent(patient, clinician, ActStatus.COMPLETED);
        workflow.checkInvoice(clinician, BigDecimal.ZERO, ActStatus.IN_PROGRESS, false);
        workflow.checkComplete(false, null, null, context);
    }

    /**
     * Verify that the workflow cancels if weight input is cancelled via the 'user close' button.
     *
     * @param userClose if <tt>true</tt> cancel via the 'user close' button, otherwise use the 'cancel' button
     */
    private void checkCancelPatientWeight(boolean userClose) {
        Act appointment = createAppointment(customer, patient, clinician);
        CheckInWorkflowRunner workflow = new CheckInWorkflowRunner(appointment, getPractice(), context);
        workflow.setWorkList(workList);        // need to pre-set work list so it can be selected in popup
        workflow.start();

        workflow.selectWorkList(workList, customer, patient);

        EditDialog editor = workflow.getWeightEditor();
        WorkflowTestHelper.cancelDialog(editor, userClose);
        workflow.checkComplete(false, null, null, context);
    }

    /**
     * Verifies that the clinician is populated correctly.
     *
     * @param appointment the appointment
     * @param clinician   the expected clinician. May be {@code null}
     * @param context     the context
     */
    private void checkClinician(Act appointment, User clinician, Context context) {
        CheckInWorkflowRunner workflow = new CheckInWorkflowRunner(appointment, getPractice(), context);
        workflow.setWorkList(workList);        // need to pre-set work list so it can be selected in popup
        workflow.start();

        workflow.selectWorkList(workList, customer, patient);

        workflow.addWeight(patient, BigDecimal.valueOf(20), clinician); // clinician defaults from context

        workflow.printDocumentForm(PopupDialog.SKIP_ID);

        // edit the clinical event
        PopupDialog eventDialog = workflow.editVisit();
        fireDialogButton(eventDialog, PopupDialog.OK_ID);
        workflow.checkEvent(patient, clinician, ActStatus.IN_PROGRESS);
        workflow.checkInvoice(clinician, BigDecimal.ZERO, ActStatus.IN_PROGRESS, true);
        workflow.checkComplete(true, customer, patient, context);
    }

    private Entity addDocumentTemplate(Product product) {
        Entity template = DocumentTestHelper.createDocumentTemplate(PatientArchetypes.DOCUMENT_FORM);
        EntityBean bean = new EntityBean(product);
        bean.addNodeRelationship("documents", template);
        bean.save();
        return template;
    }
}
