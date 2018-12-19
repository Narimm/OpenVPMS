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

import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.workflow.AppointmentStatus;
import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.archetype.rules.workflow.TaskStatus;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.edit.Editor;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.PatientReferenceEditor;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserDialog;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.workflow.Task;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.patient.PatientEditor;
import org.openvpms.web.workspace.workflow.EditVisitTask;
import org.openvpms.web.workspace.workflow.FinancialWorkflowRunner;
import org.openvpms.web.workspace.workflow.TestEditVisitTask;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.openvpms.web.test.EchoTestHelper.findBrowserDialog;
import static org.openvpms.web.test.EchoTestHelper.findEditDialog;
import static org.openvpms.web.test.EchoTestHelper.fireButton;
import static org.openvpms.web.test.EchoTestHelper.fireDialogButton;

/**
 * Helper to run the check-in workflow.
 *
 * @author Tim Anderson
 */
public class CheckInWorkflowRunner extends FinancialWorkflowRunner<CheckInWorkflowRunner.TestCheckInWorkflow> {

    /**
     * The appointment.
     */
    private Act appointment;


    /**
     * Constructs a {@link CheckInWorkflowRunner}.
     *
     * @param appointment the appointment
     * @param practice    the practice
     * @param context     the context
     */
    public CheckInWorkflowRunner(Act appointment, Party practice, Context context) {
        super(practice);
        context.setPractice(practice);
        this.appointment = appointment;
        setWorkflow(new TestCheckInWorkflow(appointment, context, new HelpContext("foo", null)));
    }

    /**
     * Returns the check in dialog.
     * <p/>
     * The current task must be an {@link CheckInWorkflow.CheckInDialogTask}.
     *
     * @return the dialog
     */
    public CheckInDialog getCheckInDialog() {
        Task task = getTask();
        assertTrue(task instanceof CheckInWorkflow.CheckInDialogTask);
        return ((CheckInWorkflow.CheckInDialogTask) task).getCheckInDialog();
    }

    public CheckInEditor getCheckInEditor() {
        return getCheckInDialog().getEditor();
    }

    /**
     * Sets the customer arrival time.
     *
     * @param arrivalTime the arrival time
     */
    public void setArrivalTime(Date arrivalTime) {
        getWorkflow().setArrivalTime(arrivalTime);
    }

    /**
     * Selects the specified patient in the current patient selection browser.
     *
     * @param patient the patient
     */
    public void selectPatient(Party patient) {
        BrowserDialog<Party> dialog = getSelectionDialog();
        Browser<Party> browser = dialog.getBrowser();
        fireSelection(browser, patient);
        assertEquals(patient, getContext().getPatient());
    }

    /**
     * Displays the patient browser in the check-in editor.
     *
     * @return the browser dialog
     */
    @SuppressWarnings("unchecked")
    public BrowserDialog<Party> selectPatient() {
        CheckInEditor editor = getCheckInEditor();
        PatientReferenceEditor patientEditor = editor.getPatientReferenceEditor();
        fireButton(patientEditor.getComponent(), "button.select");
        return (BrowserDialog<Party>) findBrowserDialog();
    }

    /**
     * Displays the work list browser in the check-in editor.
     *
     * @return the browser dialog
     */
    @SuppressWarnings("unchecked")
    public BrowserDialog<Entity> selectWorkList() {
        CheckInEditor editor = getCheckInEditor();
        Editor workListEditor = editor.getWorkListEditor();
        assertNotNull(workListEditor);
        fireButton(workListEditor.getComponent(), "button.select");
        return (BrowserDialog<Entity>) findBrowserDialog();
    }


    /**
     * Populates the name of a patient in a patient editor, so that it can be saved.
     * <p/>
     * There must be an {@link EditDialog}, with an {@link PatientEditor}.
     *
     * @param name the patient name
     * @return the patient edit dialog
     */
    public EditDialog editPatient(String name) {
        EditDialog dialog = findEditDialog();
        assertNotNull(dialog);
        IMObjectEditor editor = dialog.getEditor();
        assertTrue(editor instanceof PatientEditor);
        Party patient = (Party) editor.getObject();
        assertTrue(patient.isNew());
        editor.getProperty("name").setValue(name);
        editor.getProperty("species").setValue(TestHelper.getLookup("lookup.species", "CANINE").getCode());
        return dialog;
    }

    /**
     * Verifies that the patient in the workflow context matches that expected, and has the expected customer
     * ownership.
     *
     * @param patient  the expected patient
     * @param customer the expected owner
     */
    public void checkPatient(Party patient, Party customer) {
        Party p = getContext().getPatient();
        assertEquals(patient, p);
        assertFalse(p.isNew());
        PatientRules rules = ServiceHelper.getBean(PatientRules.class);
        customer = IMObjectHelper.reload(customer);
        assertNotNull(customer);
        assertTrue(rules.isOwner(customer, patient));
    }

    /**
     * Verifies the context has a task with the specified attributes.
     *
     * @param workList  the expected work list
     * @param customer  the expected customer
     * @param patient   the expected patient
     * @param clinician the expected clinician
     * @param status    the expected status
     * @return the task
     */
    public Act checkTask(Party workList, Party customer, Party patient, User clinician, String status) {
        Act task = (Act) getContext().getObject(ScheduleArchetypes.TASK);
        assertNotNull(task);
        assertTrue(!task.isNew());  // has been saved
        assertEquals(status, task.getStatus());
        IMObjectBean bean = new IMObjectBean(task);
        assertEquals(0, DateRules.compareTo(getWorkflow().getArrivalTime(), task.getActivityStartTime()));
        assertEquals(bean.getTarget("worklist"), workList);
        assertEquals(bean.getTarget("customer"), customer);
        assertEquals(bean.getTarget("patient"), patient);
        assertEquals(bean.getTarget("clinician"), clinician);
        return task;
    }

    /**
     * Verifies the context has an <em>act.patientClinicalEvent</em> for the specified patient.
     *
     * @param patient   the expected patient. May be {@code null}
     * @param clinician the expected clinician. May be {@code null}
     * @param status    the expected status
     * @param location  the expected location. May be {@code null}
     * @return the event
     */
    public Act checkEvent(Party patient, User clinician, String status, Party location) {
        Act event = (Act) getContext().getObject(PatientArchetypes.CLINICAL_EVENT);
        assertNotNull(event);
        assertFalse(event.isNew());  // should be saved
        IMObjectBean bean = new IMObjectBean(event);
        Date arrivalTime = getWorkflow().getArrivalTime();
        assertEquals("Expected " + arrivalTime + ", got " + event.getActivityStartTime(),
                     0, DateRules.compareTo(arrivalTime, event.getActivityStartTime(), true));
        assertEquals(patient, bean.getTarget("patient"));
        assertEquals(clinician, bean.getTarget("clinician"));
        assertEquals(status, event.getStatus());
        assertEquals(location, bean.getTarget("location"));
        return event;
    }

    /**
     * Verifies the context has an <em>act.customerAccountChargesInvoice</em>.
     *
     * @param clinician the expected clinician. May be {@code null}
     * @param amount    the expected amount
     * @param status    the expected status
     * @param saved     if {@code} true, indicates that the invoice as been saved
     */
    public void checkInvoice(User clinician, BigDecimal amount, String status, boolean saved) {
        Act invoice = (Act) getContext().getObject(CustomerAccountArchetypes.INVOICE);
        assertNotNull(invoice);
        assertEquals(saved, !invoice.isNew());
        IMObjectBean bean = new IMObjectBean(invoice);
        assertEquals(0, bean.getBigDecimal("amount").compareTo(amount));
        assertEquals(clinician, bean.getTarget("clinician"));
        assertEquals(status, invoice.getStatus());
    }

    /**
     * Verifies the weight for a patient matches that expected.
     *
     * @param patient   the patient
     * @param weight    the expected weight
     * @param clinician the expected clinician. May be {@code null}
     */
    public void checkWeight(Party patient, BigDecimal weight, User clinician) {
        Act event = (Act) getContext().getObject(PatientArchetypes.CLINICAL_EVENT);
        assertNotNull(event);
        IMObjectBean bean = new IMObjectBean(event);
        List<Act> acts = bean.getTargets("items", Act.class);
        Act weightAct = IMObjectHelper.getObject(PatientArchetypes.PATIENT_WEIGHT, acts);
        assertNotNull(weightAct);
        acts.remove(weightAct);
        assertNull(IMObjectHelper.getObject(PatientArchetypes.PATIENT_WEIGHT, acts));
        IMObjectBean weightBean = new IMObjectBean(weightAct);
        assertEquals(patient, weightBean.getTarget("patient"));
        assertEquals(clinician, weightBean.getTarget("clinician"));
        assertEquals(0, weight.compareTo(weightBean.getBigDecimal("weight")));
    }

    /**
     * Helper to run the workflow through to completion.
     *
     * @param patient     the patient
     * @param customer    the customer
     * @param workList    the work list
     * @param arrivalTime the customer arrival time
     * @param clinician   the clinician
     * @param location    the location
     * @return the event
     */
    public Act runWorkflow(Party patient, Party customer, Party workList, Date arrivalTime, User clinician, Party location) {

        setArrivalTime(arrivalTime);
        start();

        CheckInEditor editor = getCheckInEditor();

        if (clinician != null) {
            editor.setClinician(clinician);
        }

        IMObjectBean bean = new IMObjectBean(appointment);
        if (bean.getTargetRef("patient") == null) {
            editor.setPatient(patient);
        } else {
            assertEquals(patient, editor.getPatient());
        }

        if (workList != null) {
            editor.setWorkList(workList);
        }

        // set the patient weight
        editor.setWeight(BigDecimal.TEN);

        fireDialogButton(getCheckInDialog(), PopupDialog.OK_ID);

        checkWeight(patient, BigDecimal.TEN, clinician);

        if (workList != null) {
            checkTask(workList, customer, patient, clinician, TaskStatus.PENDING);
        }

        // edit the clinical event
        PopupDialog eventDialog = editVisit();
        fireDialogButton(eventDialog, PopupDialog.OK_ID);
        return checkEvent(patient, clinician, ActStatus.IN_PROGRESS, location);
    }

    /**
     * Verifies that the workflow is completed.
     *
     * @param appointmentUpdated if {@code true} expect the appointment to be <em>CHECKED_IN</em>
     * @param customer           the expected context customer. May be {@code null}
     * @param patient            the expected context patient. May be {@code null}
     * @param context            the context to check
     */
    public void checkComplete(boolean appointmentUpdated, Party customer, Party patient, Context context) {
        assertNull(getTask());

        assertEquals(patient, context.getPatient());
        assertEquals(customer, context.getCustomer());
        appointment = IMObjectHelper.reload(appointment);
        if (appointmentUpdated) {
            assertEquals(AppointmentStatus.CHECKED_IN, appointment.getStatus());
            IMObjectBean bean = new IMObjectBean(appointment);
            assertEquals(0, DateRules.compareTo(getWorkflow().getArrivalTime(), bean.getDate("arrivalTime")));
        } else {
            assertEquals(AppointmentStatus.PENDING, appointment.getStatus());
        }
    }


    protected static class TestCheckInWorkflow extends CheckInWorkflow {

        /**
         * The appointment.
         */
        private Act appointment;

        /**
         * The context.
         */
        private Context context;

        /**
         * The patient to pre-populate the patient selection browser with.
         */
        private Party patient;

        /**
         * The work-list to pre-populate the work-list selection browser with.
         */
        private Party workList;

        /**
         * The customer arrival time.
         */
        private Date arrivalTime;

        /**
         * Constructs a {@code TestCheckInWorkflow} from an appointment.
         *
         * @param appointment the appointment
         * @param context     the context
         */
        public TestCheckInWorkflow(Act appointment, Context context, HelpContext help) {
            super(help);
            this.appointment = appointment;
            this.context = context;
        }

        /**
         * Sets the customer arrival time.
         *
         * @param arrivalTime the arrival time
         */
        public void setArrivalTime(Date arrivalTime) {
            this.arrivalTime = arrivalTime;
        }

        /**
         * Returns the time that the customer arrived for the appointment.
         *
         * @return the arrival time. Defaults to now.
         */
        @Override
        public Date getArrivalTime() {
            if (arrivalTime == null) {
                arrivalTime = super.getArrivalTime();
            }
            return arrivalTime;
        }

        /**
         * Starts the workflow.
         */
        @Override
        public void start() {
            initialise(appointment, context);
            super.start();
        }

        /**
         * Creates a new {@link org.openvpms.web.workspace.workflow.EditVisitTask}.
         *
         * @return a new task to edit the visit
         */
        @Override
        protected EditVisitTask createEditVisitTask() {
            return new TestEditVisitTask();
        }
    }

}
