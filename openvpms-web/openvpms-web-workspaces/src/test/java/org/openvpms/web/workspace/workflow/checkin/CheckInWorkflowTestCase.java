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

package org.openvpms.web.workspace.workflow.checkin;

import nextapp.echo2.app.event.WindowPaneListener;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.archetype.rules.workflow.ScheduleTestHelper;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.doc.DocumentTestHelper;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.BrowserDialog;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.error.ErrorHandler;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.workspace.customer.charge.AbstractCustomerChargeActEditorTest;
import org.openvpms.web.workspace.customer.charge.CustomerChargeActItemEditor;
import org.openvpms.web.workspace.customer.charge.CustomerChargeTestHelper;
import org.openvpms.web.workspace.customer.charge.TestChargeEditor;
import org.openvpms.web.workspace.patient.charge.VisitChargeItemEditor;
import org.openvpms.web.workspace.patient.visit.VisitEditorDialog;
import org.openvpms.web.workspace.workflow.WorkflowTestHelper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.openvpms.archetype.test.TestHelper.getDatetime;
import static org.openvpms.web.test.EchoTestHelper.fireDialogButton;
import static org.openvpms.web.workspace.workflow.WorkflowTestHelper.createAppointment;
import static org.openvpms.web.workspace.workflow.WorkflowTestHelper.createWorkList;


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
        runCheckInToVisit(workflow);

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
        runCheckInToWeight(workflow);

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
     * Performs a check in, one day after a patient was invoiced but using the same invoice.
     * <p/>
     * This verifies the fix for OVPMS-1302.
     */
    @Test
    public void testCheckInWithInProgressInvoice() {
        // Two visits should be created, one on date1, the other on date2
        Date date1 = getDatetime("2012-01-01 10:00:00");
        Date date2 = getDatetime("2012-01-02 12:00:00");

        // Invoice the customer for a medication1 for the patient on 1/1/2012.
        // Leave the invoice IN_PROGRESS
        Product medication1 = TestHelper.createProduct();
        FinancialAct charge = (FinancialAct) create(CustomerAccountArchetypes.INVOICE);
        charge.setActivityStartTime(date1);

        context.setCustomer(customer);
        context.setPatient(patient);
        context.setPractice(getPractice());
        context.setClinician(clinician);
        LayoutContext layoutContext = new DefaultLayoutContext(context, new HelpContext("foo", null));

        TestChargeEditor editor = new TestChargeEditor(charge, layoutContext, false);
        editor.getComponent();
        CustomerChargeActItemEditor itemEditor1 = editor.addItem();
        itemEditor1.setStartTime(date1);  // otherwise will default to now
        setItem(editor, itemEditor1, patient, medication1, BigDecimal.TEN, editor.getQueue());

        assertTrue(SaveHelper.save(editor));

        // Verify that an event has been created, linked to the charge
        Act item1 = (Act) itemEditor1.getObject();
        ActBean bean = new ActBean(item1);
        Act event1 = bean.getSourceAct(PatientArchetypes.CLINICAL_EVENT_CHARGE_ITEM);
        assertNotNull(event1);
        assertEquals(date1, event1.getActivityStartTime());
        assertEquals(ActStatus.COMPLETED, event1.getStatus());

        Act appointment = createAppointment(date2, customer, patient, clinician);
        CheckInWorkflowRunner workflow = new CheckInWorkflowRunner(appointment, getPractice(), context);

        runCheckInToVisit(workflow);

        // Add another invoice item.
        Product medication2 = TestHelper.createProduct();
        VisitChargeItemEditor itemEditor2 = workflow.addVisitInvoiceItem(patient, clinician, medication2);

        // close the dialog
        PopupDialog eventDialog = workflow.editVisit();
        fireDialogButton(eventDialog, PopupDialog.OK_ID);
        Act event2 = workflow.checkEvent(patient, clinician, ActStatus.IN_PROGRESS);
        workflow.checkComplete(true, customer, patient, context);

        // verify the second item is linked to event2
        Act item2 = (Act) itemEditor2.getObject();
        ActBean bean2 = new ActBean(item2);
        assertEquals(event2, bean2.getSourceAct(PatientArchetypes.CLINICAL_EVENT_CHARGE_ITEM));

        // verify the second event is not the same as the first, and that none of the acts in the second event
        // are the same as those in the first
        assertNotEquals(event1, event2);
        ActBean event1Bean = new ActBean(event1);
        ActBean event2Bean = new ActBean(event2);
        List<Act> event1Items = event1Bean.getActs();
        List<Act> event2Items = event2Bean.getActs();
        Collection inBoth = CollectionUtils.intersection(event1Items, event2Items);
        assertTrue(inBoth.isEmpty());
    }

    /**
     * Verifies that changing the clinician on an invoice item propagates through to the documents associated
     * with the invoice.
     */
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
        addDocumentTemplate(product);
        addDocumentTemplate(product);

        // edit the charge
        VisitEditorDialog dialog = workflow.getVisitEditorDialog();
        dialog.getEditor().selectCharges(); // make sure the charges tab is selected, to enable the Apply button
        VisitChargeItemEditor itemEditor = workflow.addVisitInvoiceItem(patient, clinician, product);
        itemEditor.setClinician(clinician);
        fireDialogButton(dialog, PopupDialog.APPLY_ID);

        List<Act> documents1 = getDocuments(itemEditor);
        assertEquals(2, documents1.size());

        User clinician2 = TestHelper.createClinician();
        itemEditor.setClinician(clinician2);
        fireDialogButton(dialog, PopupDialog.OK_ID);

        List<Act> documents2 = getDocuments(itemEditor);
        assertEquals(2, documents2.size());
        assertEquals(clinician2, getClinician(documents2.get(0)));
        assertEquals(clinician2, getClinician(documents2.get(1)));

        workflow.checkEvent(patient, clinician, ActStatus.IN_PROGRESS);
        workflow.checkComplete(true, customer, patient, context);

        assertTrue(errors.isEmpty());
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
            @Override
            public void error(Throwable cause) {
                errors.add(cause.getMessage());
            }

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
        runCheckInToWeight(workflow);

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
        runCheckInToVisit(workflow);

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
        runCheckInToWeight(workflow);

        EditDialog editor = workflow.getWeightEditor();
        WorkflowTestHelper.cancelDialog(editor, userClose);
        workflow.checkComplete(false, null, null, context);
    }

    /**
     * Runs the check-in workflow up to the weight editing step.
     *
     * @param workflow the workflow
     */
    private void runCheckInToWeight(CheckInWorkflowRunner workflow) {
        workflow.setWorkList(workList);        // need to pre-set work list so it can be selected in popup
        workflow.start();
        workflow.selectWorkList(workList, customer, patient);
    }

    /**
     * Runs the check-in workflow up to the visit editing step.
     *
     * @param workflow the workflow
     */
    private void runCheckInToVisit(CheckInWorkflowRunner workflow) {
        runCheckInToWeight(workflow);

        workflow.addWeight(patient, BigDecimal.valueOf(20), clinician);
        workflow.printDocumentForm(PopupDialog.SKIP_ID);
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

    /**
     * Returns the documents associated with a charge item.
     *
     * @param itemEditor the item editor
     * @return the documents
     */
    private List<Act> getDocuments(VisitChargeItemEditor itemEditor) {
        ActBean bean = new ActBean((Act) itemEditor.getObject());
        return bean.getNodeActs("documents");
    }

    /**
     * Returns the clinician from an act.
     *
     * @param act the act
     * @return the clinician. May be {@code null}
     */
    private User getClinician(Act act) {
        ActBean bean = new ActBean(act);
        return (User) bean.getNodeParticipant("clinician");
    }

    /**
     * Helper to add a document template to a product.
     *
     * @param product the product
     * @return the document template
     */
    private Entity addDocumentTemplate(Product product) {
        Entity template = DocumentTestHelper.createDocumentTemplate(PatientArchetypes.DOCUMENT_FORM);
        EntityBean bean = new EntityBean(product);
        bean.addNodeRelationship("documents", template);
        bean.save();
        return template;
    }
}
