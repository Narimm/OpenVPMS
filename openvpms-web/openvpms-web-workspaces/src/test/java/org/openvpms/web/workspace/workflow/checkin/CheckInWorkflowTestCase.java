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

package org.openvpms.web.workspace.workflow.checkin;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.archetype.rules.workflow.ScheduleTestHelper;
import org.openvpms.archetype.rules.workflow.TaskStatus;
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
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.doc.DocumentTestHelper;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.BrowserDialog;
import org.openvpms.web.echo.dialog.PopupDialog;
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
     * The practice location.
     */
    private Party location;

    /**
     * The work list.
     */
    private Party workList;

    /**
     * Tracks errors logged.
     */
    private List<String> errors = new ArrayList<>();

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
        location = TestHelper.createLocation();
        context.setLocation(location);
        context.setUser(user);

        // register an ErrorHandler to collect errors
        initErrorHandler(errors);
    }

    /**
     * Tests the check-in workflow when launched from an appointment with no patient.
     */
    @Test
    public void testCheckInFromAppointmentNoPatient() {
        Date startTime = TestHelper.getDatetime("2013-01-01 09:00:00");
        Date arrivalTime = TestHelper.getDatetime("2013-01-01 08:50:00"); // arrived early
        Act appointment = createAppointment(startTime, customer, null, clinician, location);

        CheckInWorkflowRunner workflow = new CheckInWorkflowRunner(appointment, getPractice(), context);
        workflow.setArrivalTime(arrivalTime);
        workflow.start();

        // as the appointment has no patient, the patient should be null
        CheckInEditor editor = workflow.getCheckInEditor();
        assertNull(editor.getPatient());
        editor.setPatient(patient);
        editor.setWorkList(workList);
        editor.setWeight(BigDecimal.TEN);

        fireDialogButton(workflow.getCheckInDialog(), PopupDialog.OK_ID);

        // verify a patient weight has been created
        workflow.checkWeight(patient, BigDecimal.TEN, clinician);

        // verify a task has been created
        workflow.checkTask(workList, customer, patient, clinician, TaskStatus.PENDING);

        // edit the clinical event
        PopupDialog eventDialog = workflow.editVisit();
        fireDialogButton(eventDialog, PopupDialog.OK_ID);
        workflow.checkEvent(patient, clinician, ActStatus.IN_PROGRESS, location);

        // verify the workflow is complete
        workflow.checkComplete(true, customer, patient, context);
    }

    /**
     * Tests the check-in workflow when launched from an appointment with a patient.
     */
    @Test
    public void testCheckInFromAppointmentWithPatient() {
        Date startTime = TestHelper.getDatetime("2013-01-01 09:00:00");
        Date arrivalTime = TestHelper.getDatetime("2013-01-01 09:33:00"); // arrived late
        Act appointment = createAppointment(startTime, customer, patient, clinician, location);
        CheckInWorkflowRunner workflow = new CheckInWorkflowRunner(appointment, getPractice(), context);
        workflow.setArrivalTime(arrivalTime);
        runCheckInToVisit(workflow);

        // edit the clinical event
        PopupDialog eventDialog = workflow.editVisit();
        fireDialogButton(eventDialog, PopupDialog.OK_ID);
        workflow.checkEvent(patient, clinician, ActStatus.IN_PROGRESS, location);
        workflow.checkComplete(true, customer, patient, context);
    }

    /**
     * Verifies that a new patient can be created if the appointment doesn't have one.
     */
    @Test
    public void testCreatePatient() {
        Act appointment = createAppointment(customer, null, clinician, location);
        CheckInWorkflowRunner workflow = new CheckInWorkflowRunner(appointment, getPractice(), context);
        workflow.start();

        // create the new patient
        BrowserDialog dialog = workflow.selectPatient();
        fireDialogButton(dialog, BrowserDialog.NEW_ID);

        EditDialog editDialog = workflow.editPatient("Fluffy");
        Party newPatient = (Party) editDialog.getEditor().getObject();
        fireDialogButton(editDialog, PopupDialog.OK_ID);

        fireDialogButton(workflow.getCheckInDialog(), PopupDialog.OK_ID);

        // verify the patient has been created and is owned by the customer
        workflow.checkPatient(newPatient, customer);

        PopupDialog eventDialog = workflow.editVisit();
        fireDialogButton(eventDialog, PopupDialog.OK_ID);
        workflow.checkEvent(newPatient, clinician, ActStatus.IN_PROGRESS, location);

        workflow.checkComplete(true, customer, newPatient, context);
    }

    /**
     * Verify that the workflow cancels if a new patient is created but editing cancelled via the cancel button.
     */
    @Test
    public void testCancelCreatePatient() {
        Act appointment = createAppointment(customer, null, clinician, location);
        CheckInWorkflowRunner workflow = new CheckInWorkflowRunner(appointment, getPractice(), context);
        workflow.start();

        BrowserDialog<Party> dialog = workflow.selectPatient();
        fireDialogButton(dialog, BrowserDialog.NEW_ID);
        EditDialog editDialog = workflow.editPatient("Fluffy");
        Party patient = (Party) editDialog.getEditor().getObject();
        fireDialogButton(editDialog, PopupDialog.CANCEL_ID);
        assertNull(get(patient));

        fireDialogButton(workflow.getCheckInDialog(), PopupDialog.CANCEL_ID);
        workflow.checkComplete(false, null, null, context);
    }

    /**
     * Verify that the workflow cancels if a new patient is created but editing cancelled via the 'user close' button.
     */
    @Test
    public void testCancelCreatePatientByUserClose() {
        Act appointment = createAppointment(customer, null, clinician, location);
        CheckInWorkflowRunner workflow = new CheckInWorkflowRunner(appointment, getPractice(), context);
        workflow.start();

        BrowserDialog<Party> dialog = workflow.selectPatient();
        fireDialogButton(dialog, BrowserDialog.NEW_ID);
        EditDialog editDialog = workflow.editPatient("Fluffy");
        Party patient = (Party) editDialog.getEditor().getObject();
        editDialog.userClose();
        assertNull(get(patient));

        fireDialogButton(workflow.getCheckInDialog(), PopupDialog.CANCEL_ID);
        workflow.checkComplete(false, null, null, context);
    }

    /**
     * Verify that the workflow cancels if the check-in edit dialog is cancelled via the cancel button.
     */
    @Test
    public void testCancelCheckInDialog() {
        checkCancelCheckInEditDialog(false);
    }

    /**
     * Verify that the workflow cancels if the check in edit dialog is cancelled via the 'user close' button.
     */
    @Test
    public void testCancelCheckInDialogByUserClose() {
        checkCancelCheckInEditDialog(true);
    }

    /**
     * Verifies that selecting a work list can be cancelled, and no task is created.
     */
    @Test
    public void testCancelSelectWorkList() {
        Date startTime = TestHelper.getDatetime("2013-01-01 09:00:00");
        Act appointment = createAppointment(startTime, customer, patient, clinician, location);
        CheckInWorkflowRunner workflow = new CheckInWorkflowRunner(appointment, getPractice(), context);
        workflow.setArrivalTime(startTime);
        workflow.start();

        // skip work-list selection and verify no task is created
        BrowserDialog<Entity> browser = workflow.selectWorkList();
        browser.getBrowser().setSelected(workList);
        fireDialogButton(browser, PopupDialog.CANCEL_ID);

        fireDialogButton(workflow.getCheckInDialog(), PopupDialog.OK_ID);

        assertNull(workflow.getContext().getObject(ScheduleArchetypes.TASK));

        // edit the clinical event
        PopupDialog eventDialog = workflow.editVisit();
        fireDialogButton(eventDialog, PopupDialog.OK_ID);
        workflow.checkEvent(patient, clinician, ActStatus.IN_PROGRESS, location);

        // verify the workflow is complete
        workflow.checkComplete(true, customer, patient, context);
    }

    /**
     * Verifies that if there is no clinician in the appointment, but there is one in the context, it is NOT used,
     * as per OVPMS-1637.
     */
    @Test
    public void testNoDefaultClinicianFromContext() {
        Act appointment = createAppointment(customer, patient, null, location);  // no clinician on appointment
        context.setClinician(clinician);
        checkClinician(appointment, null, false, context);
    }

    /**
     * Verifies that if there is no clinician on the appointment or context, then no clinician is populated.
     */
    @Test
    public void testNoClinician() {
        Act appointment = createAppointment(customer, patient, null, location);  // no clinician on appointment
        checkClinician(appointment, null, false, context);
    }


    /**
     * Verifies that when the clinican is set in the Check-In editor, it propagates to the created acts.
     */
    @Test
    public void testSetClincianInCheckInEditor() {
        Act appointment = createAppointment(customer, patient, TestHelper.createClinician(), location);
        checkClinician(appointment, clinician, true, context);
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
     * Verifies that no patient weight act is created if it is not populated.
     */
    @Test
    public void testSkipPatientWeight() {
        Act appointment = createAppointment(customer, patient, clinician, location);
        CheckInWorkflowRunner workflow = new CheckInWorkflowRunner(appointment, getPractice(), context);
        workflow.start();

        // skip the weight entry and verify that the context has a weight act that is unsaved
        fireDialogButton(workflow.getCheckInDialog(), PopupDialog.OK_ID);
        IMObject weight = workflow.getContext().getObject(PatientArchetypes.PATIENT_WEIGHT);
        assertNull(weight);

        // edit the clinical event
        PopupDialog eventDialog = workflow.editVisit();
        fireDialogButton(eventDialog, PopupDialog.OK_ID);
        workflow.checkEvent(patient, clinician, ActStatus.IN_PROGRESS, location);

        workflow.checkComplete(true, customer, patient, context);
    }

    /**
     * Performs a check in, one day after a patient was invoiced but using the same invoice.
     * <p>
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
        layoutContext.setEdit(true);

        TestChargeEditor editor = new TestChargeEditor(charge, layoutContext, false);
        editor.getComponent();
        CustomerChargeActItemEditor itemEditor1 = editor.addItem();
        itemEditor1.setStartTime(date1);  // otherwise will default to now
        setItem(editor, itemEditor1, patient, medication1, BigDecimal.TEN, editor.getQueue());

        assertTrue(SaveHelper.save(editor));

        // Verify that an event has been created, linked to the charge
        Act item1 = itemEditor1.getObject();
        ActBean bean = new ActBean(item1);
        Act event1 = bean.getSourceAct(PatientArchetypes.CLINICAL_EVENT_CHARGE_ITEM);
        assertNotNull(event1);
        assertEquals(date1, event1.getActivityStartTime());
        assertEquals(ActStatus.IN_PROGRESS, event1.getStatus());

        // complete the event. This will force a new event to be created on subsequent check-in
        event1.setActivityEndTime(getDatetime("2012-01-01 11:30:00"));
        event1.setStatus(ActStatus.COMPLETED);
        save(event1);

        Act appointment = createAppointment(date2, customer, patient, clinician, location);
        CheckInWorkflowRunner workflow = new CheckInWorkflowRunner(appointment, getPractice(), context);
        workflow.setArrivalTime(date2);

        runCheckInToVisit(workflow);

        // Add another invoice item.
        Product medication2 = TestHelper.createProduct();
        VisitChargeItemEditor itemEditor2 = workflow.addVisitInvoiceItem(patient, medication2);

        // close the dialog
        PopupDialog eventDialog = workflow.editVisit();
        fireDialogButton(eventDialog, PopupDialog.OK_ID);
        Act event2 = workflow.checkEvent(patient, clinician, ActStatus.IN_PROGRESS, location);
        workflow.checkComplete(true, customer, patient, context);

        // verify the second item is linked to event2
        Act item2 = itemEditor2.getObject();
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
        Date startTime = TestHelper.getDatetime("2013-01-01 09:00:00");
        Act appointment = createAppointment(startTime, customer, patient, clinician, location);
        CheckInWorkflowRunner workflow = new CheckInWorkflowRunner(appointment, getPractice(), context);
        workflow.setArrivalTime(startTime);
        workflow.start();

        fireDialogButton(workflow.getCheckInDialog(), PopupDialog.OK_ID);

        Product product = CustomerChargeTestHelper.createProduct(ProductArchetypes.MEDICATION, BigDecimal.TEN);
        addDocumentTemplate(product);
        addDocumentTemplate(product);

        // edit the charge
        VisitEditorDialog dialog = workflow.getVisitEditorDialog();
        dialog.getEditor().selectCharges(); // make sure the charges tab is selected, to enable the Apply button
        VisitChargeItemEditor itemEditor = workflow.addVisitInvoiceItem(patient, product);
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

        workflow.checkEvent(patient, clinician, ActStatus.IN_PROGRESS, location);
        workflow.checkComplete(true, customer, patient, context);

        assertTrue(errors.isEmpty());
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
        checkUseLoggedInClinician(true, TestHelper.createUser(), clinician, clinician);
    }

    /**
     * Verifies that when the practice 'usedLoggedInClinician' is set false, the clinician comes from the
     * appointment, even when the logged in user is a clinician.
     */
    @Test
    public void testUseLoggedInClinicianDisabledForClinician() {
        User clinician2 = TestHelper.createClinician();
        checkUseLoggedInClinician(false, clinician2, clinician, clinician);
    }

    /**
     * Verifies that when the practice 'usedLoggedInClinician' is set false, the clinician comes from the
     * appointment.
     */
    @Test
    public void testUseLoggedInClinicianDisabledForNonClinician() {
        checkUseLoggedInClinician(false, TestHelper.createUser(), clinician, clinician);
    }

    /**
     * Tests the effects of the practice useLoggedInClinician option during the Check-In workflow.
     *
     * @param enabled              if {@code true}, enable the option, otherwise disable it
     * @param user                 the current user
     * @param appointmentClinician the appointment clinician
     * @param expectedClinician    the expected clinician to appear on the event and invoice
     */
    private void checkUseLoggedInClinician(boolean enabled, User user, User appointmentClinician,
                                           User expectedClinician) {
        IMObjectBean bean = new IMObjectBean(getPractice());
        bean.setValue("useLoggedInClinician", enabled);
        Date startTime = TestHelper.getDatetime("2018-01-01 09:00:00");
        Date arrivalTime = TestHelper.getDatetime("2018-01-01 09:33:00"); // arrived late
        Act appointment = createAppointment(startTime, customer, patient, appointmentClinician, location);
        context.setUser(user);
        context.setClinician(TestHelper.createClinician()); // should not be used, as the appointment has a clinician
        CheckInWorkflowRunner workflow = new CheckInWorkflowRunner(appointment, getPractice(), context);
        workflow.setArrivalTime(arrivalTime);
        runCheckInToVisit(workflow);

        // edit the clinical event
        PopupDialog eventDialog = workflow.editVisit();
        fireDialogButton(eventDialog, PopupDialog.OK_ID);
        workflow.checkEvent(patient, expectedClinician, ActStatus.IN_PROGRESS, location);
        workflow.checkInvoice(expectedClinician, BigDecimal.ZERO, ActStatus.IN_PROGRESS, true);
        workflow.checkComplete(true, customer, patient, context);
    }

    /**
     * Verify that the workflow cancels if the check-in dialog is cancelled.
     *
     * @param userClose if {@code true} cancel via the 'user close' button, otherwise use the 'cancel' button
     */
    private void checkCancelCheckInEditDialog(boolean userClose) {
        Act appointment = createAppointment(customer, null, clinician, location);
        CheckInWorkflowRunner workflow = new CheckInWorkflowRunner(appointment, getPractice(), context);
        workflow.start();

        CheckInDialog dialog = workflow.getCheckInDialog();
        CheckInEditor editor = dialog.getEditor();
        editor.setPatient(patient);

        WorkflowTestHelper.cancelDialog(dialog, userClose);
        workflow.checkComplete(false, null, null, context);
    }

    /**
     * Tests the behaviour of cancelling the clinical event edit. The event should save, and the workflow cancel.
     *
     * @param userClose if <tt>true</tt> cancel the edit by the 'user close' button otherwise via the cancel button
     */
    private void checkCancelEditEvent(boolean userClose) {
        Act appointment = createAppointment(customer, patient, clinician, location);
        CheckInWorkflowRunner workflow = new CheckInWorkflowRunner(appointment, getPractice(), context);
        runCheckInToVisit(workflow);

        // edit the clinical event
        PopupDialog eventDialog = workflow.editVisit();
        WorkflowTestHelper.cancelDialog(eventDialog, userClose);

        // event is saved regardless of cancel
        workflow.checkEvent(patient, clinician, ActStatus.IN_PROGRESS, location);
        workflow.checkInvoice(clinician, BigDecimal.ZERO, ActStatus.IN_PROGRESS, false);
        workflow.checkComplete(false, null, null, context);
    }

    /**
     * Runs the check-in workflow up to the visit editing step.
     *
     * @param workflow the workflow
     */
    private void runCheckInToVisit(CheckInWorkflowRunner workflow) {
        workflow.start();
        fireDialogButton(workflow.getCheckInDialog(), PopupDialog.OK_ID);
    }

    /**
     * Verifies that the clinician is populated correctly.
     *
     * @param appointment  the appointment
     * @param clinician    the expected clinician. May be {@code null}
     * @param setClinician if {@code true}, set the clinician in the check-in editor
     * @param context      the context
     */
    private void checkClinician(Act appointment, User clinician, boolean setClinician, Context context) {
        CheckInWorkflowRunner workflow = new CheckInWorkflowRunner(appointment, getPractice(), context);
        workflow.setArrivalTime(new Date());
        workflow.start();

        CheckInEditor editor = workflow.getCheckInEditor();
        editor.setWorkList(workList);
        editor.setWeight(BigDecimal.TEN);
        if (setClinician) {
            editor.setClinician(clinician);
        }
        fireDialogButton(workflow.getCheckInDialog(), PopupDialog.OK_ID);

        // edit the clinical event
        PopupDialog eventDialog = workflow.editVisit();
        fireDialogButton(eventDialog, PopupDialog.OK_ID);
        workflow.checkWeight(patient, BigDecimal.TEN, clinician);
        workflow.checkTask(workList, customer, patient, clinician, TaskStatus.PENDING);
        workflow.checkEvent(patient, clinician, ActStatus.IN_PROGRESS, location);
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
        ActBean bean = new ActBean(itemEditor.getObject());
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
     */
    private void addDocumentTemplate(Product product) {
        Entity template = DocumentTestHelper.createDocumentTemplate(PatientArchetypes.DOCUMENT_FORM);
        EntityBean bean = new EntityBean(product);
        bean.addNodeRelationship("documents", template);
        bean.save();
    }
}
