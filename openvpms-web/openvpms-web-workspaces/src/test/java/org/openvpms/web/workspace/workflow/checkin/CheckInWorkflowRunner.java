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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
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
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.act.DefaultActEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserDialog;
import org.openvpms.web.component.im.query.DefaultIMObjectTableBrowser;
import org.openvpms.web.component.im.query.ListQuery;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.workflow.EditIMObjectTask;
import org.openvpms.web.component.workflow.SelectIMObjectTask;
import org.openvpms.web.component.workflow.Task;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.test.EchoTestHelper;
import org.openvpms.web.workspace.patient.PatientEditor;
import org.openvpms.web.workspace.workflow.EditVisitTask;
import org.openvpms.web.workspace.workflow.FinancialWorkflowRunner;
import org.openvpms.web.workspace.workflow.TestEditVisitTask;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
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
        this.appointment = appointment;
        setWorkflow(new TestCheckInWorkflow(appointment, context, new HelpContext("foo", null)));
    }

    /**
     * Sets the patient used for patient selection.
     *
     * @param patient the patient. May be {@code null}
     */
    public void setPatient(Party patient) {
        getWorkflow().setPatient(patient);
    }

    /**
     * Sets the work list used for work list selection.
     *
     * @param workList the work list. May be {@code null}
     */
    public void setWorkList(Party workList) {
        getWorkflow().setWorkList(workList);
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
     * Populates the name of a patient in a patient editor, so that it can be saved.
     * <p/>
     * The current task must be an {@link EditIMObjectTask}, with an {@link PatientEditor}.
     *
     * @param name the patient name
     * @return the patient edit dialog
     */
    public EditDialog editPatient(String name) {
        EditDialog dialog = getEditDialog();
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
        PatientRules rules = new PatientRules(ServiceHelper.getArchetypeService(), ServiceHelper.getLookupService());
        customer = IMObjectHelper.reload(customer);
        assertNotNull(customer);
        assertTrue(rules.isOwner(customer, patient));
    }

    /**
     * Selects the specified work list in the work list selection browser, and verifies that a corresponding task
     * is created.
     * <p/>
     * The current task must be an {@link SelectIMObjectTask}.
     *
     * @param workList the work list
     * @param customer the expected customer
     * @param patient  the expected patient
     */
    public void selectWorkList(Party workList, Party customer, Party patient) {
        BrowserDialog<Party> dialog = getSelectionDialog();
        Browser<Party> browser = dialog.getBrowser();
        fireSelection(browser, workList);
        assertEquals(workList, getContext().getWorkList());

        // verify a task has been created
        checkTask(workList, customer, patient, TaskStatus.PENDING);
    }

    /**
     * Selects the specified button of the document selection dialog.
     *
     * @param buttonId the button identifier
     */
    public void printPatientDocuments(String buttonId) {
        BrowserDialog<Entity> dialog = getPrintDocumentsDialog();
        EchoTestHelper.fireDialogButton(dialog, buttonId);
    }

    /**
     * Returns the dialog to print patient documents.
     *
     * @return the dialog
     */
    public BrowserDialog<Entity> getPrintDocumentsDialog() {
        Task current = getTask();
        assertTrue(current instanceof PrintPatientDocumentsTask);
        BrowserDialog<Entity> dialog = ((PrintPatientDocumentsTask) current).getBrowserDialog();
        assertNotNull(dialog);
        return dialog;
    }

    /**
     * Verifies the context has a task with the specified attributes.
     *
     * @param workList the expected work list
     * @param customer the expected customer
     * @param patient  the expected patient
     * @param status   the expected status
     * @return the task
     */
    public Act checkTask(Party workList, Party customer, Party patient, String status) {
        Act task = (Act) getContext().getObject(ScheduleArchetypes.TASK);
        assertNotNull(task);
        assertTrue(!task.isNew());  // has been saved
        assertEquals(status, task.getStatus());
        ActBean bean = new ActBean(task);
        assertEquals(0, DateRules.compareTo(getWorkflow().getArrivalTime(), task.getActivityStartTime()));
        assertEquals(bean.getNodeParticipant("worklist"), workList);
        assertEquals(bean.getNodeParticipant("customer"), customer);
        assertEquals(bean.getNodeParticipant("patient"), patient);
        return task;
    }

    /**
     * Returns the editor for the patient weight.
     *
     * @return the patient weight editor
     */
    public EditDialog getWeightEditor() {
        EditDialog dialog = getEditDialog();
        IMObjectEditor editor = dialog.getEditor();
        assertTrue(TypeHelper.isA(editor.getObject(), PatientArchetypes.PATIENT_WEIGHT));
        return dialog;
    }

    /**
     * Sets the weight for a patient.
     * <p/>
     * The current task must be an {@link EditIMObjectTask} editing an <em>act.patientWeight</em>.
     *
     * @param weight the patient weight
     * @return the edit dialog
     */
    public EditDialog setWeight(BigDecimal weight) {
        EditDialog dialog = getWeightEditor();
        DefaultActEditor editor = (DefaultActEditor) dialog.getEditor();
        editor.getProperty("weight").setValue(weight);
        return dialog;
    }

    /**
     * Verifies the context has an <em>act.patientClinicalEvent</em> for the specified patient.
     *
     * @param patient   the expected patient. May be {@code null}
     * @param clinician the expected clinician. May be {@code null}
     * @param status    the expected status
     * @return the event
     */
    public Act checkEvent(Party patient, User clinician, String status) {
        Act event = (Act) getContext().getObject(PatientArchetypes.CLINICAL_EVENT);
        assertNotNull(event);
        assertFalse(event.isNew());  // should be saved
        ActBean bean = new ActBean(event);
        assertEquals("Expected " + getWorkflow().getArrivalTime() + ", got " + event.getActivityStartTime(),
                     0, DateRules.compareTo(getWorkflow().getArrivalTime(), event.getActivityStartTime()));
        assertEquals(patient, bean.getNodeParticipant("patient"));
        assertEquals(clinician, bean.getNodeParticipant("clinician"));
        assertEquals(status, event.getStatus());
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
        ActBean bean = new ActBean(invoice);
        assertEquals(0, bean.getBigDecimal("amount").compareTo(amount));
        assertEquals(clinician, bean.getNodeParticipant("clinician"));
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
        ActBean bean = new ActBean(event);
        List<Act> acts = bean.getNodeActs("items");
        Act weightAct = IMObjectHelper.getObject(PatientArchetypes.PATIENT_WEIGHT, acts);
        assertNotNull(weightAct);
        acts.remove(weightAct);
        assertNull(IMObjectHelper.getObject(PatientArchetypes.PATIENT_WEIGHT, acts));
        ActBean weightBean = new ActBean(weightAct);
        assertEquals(patient, weightBean.getNodeParticipant("patient"));
        assertEquals(clinician, weightBean.getNodeParticipant("clinician"));
        assertTrue(weight.compareTo(weightBean.getBigDecimal("weight")) == 0);
    }

    /**
     * Adds a weight for the patient.
     *
     * @param patient   the patient
     * @param weight    the patient weight
     * @param clinician the expected clinician. May be {@code null}
     */
    public void addWeight(Party patient, BigDecimal weight, User clinician) {
        EditDialog dialog = setWeight(weight);
        fireDialogButton(dialog, PopupDialog.OK_ID);
        checkWeight(patient, weight, clinician);
    }

    /**
     * Helper to run the workflow through to completion.
     *
     * @param patient     the patient
     * @param customer    the customer
     * @param workList    the work list
     * @param arrivalTime the customer arrival time
     * @param clinician   the clinician
     * @return the event
     */
    public Act runWorkflow(Party patient, Party customer, Party workList, Date arrivalTime, User clinician) {
        setPatient(patient);                              // need to pre-set patient and work list
        setWorkList(workList);                            // so they can be selected in popups
        setArrivalTime(arrivalTime);
        start();

        ActBean bean = new ActBean(appointment);
        if (bean.getNodeParticipantRef("patient") == null) {
            // as the appointment has no patient, a pop should be displayed to select one
            selectPatient(patient);
        }

        // select the work list and verify a task has been created.
        selectWorkList(workList, customer, patient);

        // add the patient weight
        addWeight(patient, BigDecimal.valueOf(10), clinician);

        printPatientDocuments(PopupDialog.SKIP_ID);

        // edit the clinical event
        PopupDialog eventDialog = editVisit();
        fireDialogButton(eventDialog, PopupDialog.OK_ID);
        return checkEvent(patient, clinician, ActStatus.IN_PROGRESS);
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
            ActBean bean = new ActBean(appointment);
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
         * Sets the patient to pre-populate the patient selection browser with.
         *
         * @param patient the patient. May be {@code null}
         */
        public void setPatient(Party patient) {
            this.patient = patient;
        }

        /**
         * Sets the work-list to pre-populate the work-list selection browser with.
         *
         * @param workList the work-list. May be {@code null}
         */
        public void setWorkList(Party workList) {
            this.workList = workList;
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
         * Creates a new {@link SelectIMObjectTask} to select a patient.
         *
         * @param context       the context
         * @param patientEditor the patient editor, if a new patient is selected
         * @return a new task to select a patient
         */
        @Override
        protected SelectIMObjectTask<Party> createSelectPatientTask(TaskContext context,
                                                                    EditIMObjectTask patientEditor) {
            List<Party> patients = (patient != null) ? Arrays.asList(patient) : Collections.<Party>emptyList();
            Query<Party> query = new ListQuery<Party>(patients, PatientArchetypes.PATIENT, Party.class);
            return new SelectIMObjectTask<Party>(query, patientEditor, context.getHelpContext()) {
                @Override
                protected Browser<Party> createBrowser(Query<Party> query, LayoutContext layout) {
                    return new DefaultIMObjectTableBrowser<Party>(query, layout);
                }
            };
        }

        /**
         * Creates a new {@link SelectIMObjectTask} to select a work list.
         *
         * @param context the context
         * @return a new task to select a work list
         */
        @Override
        protected SelectIMObjectTask<Entity> createSelectWorkListTask(TaskContext context) {
            List<Entity> list = (workList != null) ? Arrays.<Entity>asList(workList) : Collections.<Entity>emptyList();
            Query<Entity> query = new ListQuery<Entity>(list, ScheduleArchetypes.ORGANISATION_WORKLIST, Entity.class);
            return new SelectIMObjectTask<Entity>(query, context.getHelpContext()) {
                @Override
                protected Browser<Entity> createBrowser(Query<Entity> query, LayoutContext layout) {
                    return new DefaultIMObjectTableBrowser<Entity>(query, layout);
                }
            };
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
