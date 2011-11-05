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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.workflow.AppointmentStatus;
import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.archetype.rules.workflow.TaskStatus;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.app.patient.PatientEditor;
import org.openvpms.web.app.workflow.EditClinicalEventTask;
import org.openvpms.web.app.workflow.WorkflowRunner;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.dialog.PopupDialog;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserDialog;
import org.openvpms.web.component.im.query.ListQuery;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.workflow.EditIMObjectTask;
import org.openvpms.web.component.workflow.SelectIMObjectTask;
import org.openvpms.web.component.workflow.Task;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.test.EchoTestHelper;
import static org.openvpms.web.test.EchoTestHelper.fireDialogButton;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Helper to run the check-in workflow.
 */
class CheckInWorkflowRunner extends WorkflowRunner<CheckInWorkflowRunner.TestCheckInWorkflow> {

    /**
     * The appointment.
     */
    private Act appointment;


    /**
     * Constructs a <tt>WorkflowRunner</tt>.
     *
     * @param appointment the appointment
     * @param context     the context
     */
    public CheckInWorkflowRunner(Act appointment, Context context) {
        this.appointment = appointment;
        setWorkflow(new TestCheckInWorkflow(appointment, context));
    }

    /**
     * Sets the patient used for patient selection.
     *
     * @param patient the patient. May be <tt>null</tt>
     */
    public void setPatient(Party patient) {
        getWorkflow().setPatient(patient);
    }

    /**
     * Sets the work list used for work list selection.
     *
     * @param workList the work list. May be <tt>null</tt>
     */
    public void setWorkList(Party workList) {
        getWorkflow().setWorkList(workList);
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
    public void checkContextPatient(Party patient, Party customer) {
        Party p = getContext().getPatient();
        assertEquals(patient, p);
        assertFalse(p.isNew());
        PatientRules rules = new PatientRules();
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
    public void printDocumentForm(String buttonId) {
        BrowserDialog<Act> dialog = getSelectionDialog();
        EchoTestHelper.fireDialogButton(dialog, buttonId);
    }

    public void clinicalEvent(String buttonId) {
        Task current = getTask();
        assertTrue(current instanceof EditClinicalEventTask);
        EditClinicalEventTask edit = (EditClinicalEventTask) current;
        EchoTestHelper.fireDialogButton(edit.getBrowserDialog(), buttonId);
    }

    public void checkTask(Party workList, Party customer, Party patient, String status) {
        Act task = (Act) getContext().getObject(ScheduleArchetypes.TASK);
        assertNotNull(task);
        assertTrue(!task.isNew());  // has been saved
        assertEquals(status, task.getStatus());
        ActBean bean = new ActBean(task);
        assertEquals(bean.getNodeParticipant("worklist"), workList);
        assertEquals(bean.getNodeParticipant("customer"), customer);
        assertEquals(bean.getNodeParticipant("patient"), patient);
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
        EditDialog dialog = getEditDialog();
        IMObjectEditor editor = dialog.getEditor();
        assertTrue(TypeHelper.isA(editor.getObject(), PatientArchetypes.PATIENT_WEIGHT));
        editor.getProperty("weight").setValue(weight);
        return dialog;
    }

    /**
     * Verifies the weight for a patient matches that expected.
     *
     * @param patient the patient
     * @param weight  the expected weight
     */
    public void checkWeight(Party patient, BigDecimal weight) {
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
        assertTrue(weight.compareTo(weightBean.getBigDecimal("weight")) == 0);
    }

    /**
     * Adds a weight for the patient.
     *
     * @param patient the patient
     * @param weight  the patient weight
     */
    public void addWeight(Party patient, BigDecimal weight) {
        EditDialog dialog = setWeight(weight);
        fireDialogButton(dialog, PopupDialog.OK_ID);
        checkWeight(patient, weight);
    }

    /**
     * Verifies that the workflow is completed.
     *
     * @param appointmentUpdated if <tt>true</tt> expect the appointment to be <em>CHECKED_IN</em>
     * @param customer           the expected context customer. May be <tt>null</tt>
     * @param patient            the expected context patient. May be <tt>null</tt>
     * @param context            the context to check
     */
    public void checkComplete(boolean appointmentUpdated, Party customer, Party patient, Context context) {
        assertEquals(patient, context.getPatient());
        assertEquals(customer, context.getCustomer());
        appointment = IMObjectHelper.reload(appointment);
        if (appointmentUpdated) {
            assertEquals(AppointmentStatus.CHECKED_IN, appointment.getStatus());
        } else {
            assertEquals(AppointmentStatus.PENDING, appointment.getStatus());
        }

    }

    protected static class TestCheckInWorkflow extends CheckInWorkflow {

        private Act appointment;

        private Context context;

        private Party patient;

        private Party workList;

        /**
         * Constructs a <tt>TestCheckInWorkflow</tt> from an appointment.
         *
         * @param appointment the appointment
         * @param context     the context
         */
        public TestCheckInWorkflow(Act appointment, Context context) {
            this.appointment = appointment;
            this.context = context;
        }

        public void setPatient(Party patient) {
            this.patient = patient;
        }

        public void setWorkList(Party workList) {
            this.workList = workList;
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
            return new SelectIMObjectTask<Party>(query, patientEditor);
        }

        /**
         * Creates a new {@link SelectIMObjectTask} to select a work list.
         *
         * @param context the context
         * @return a new task to select a worklist
         */
        @Override
        protected SelectIMObjectTask<Party> createSelectWorkListTask(TaskContext context) {
            List<Party> worklists = (workList != null) ? Arrays.asList(workList) : Collections.<Party>emptyList();
            Query<Party> query = new ListQuery<Party>(worklists, WORK_LIST_SHORTNAME, Party.class);
            return new SelectIMObjectTask<Party>(query);
        }
    }

}
