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

import nextapp.echo2.app.Table;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.workflow.AppointmentStatus;
import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.archetype.rules.workflow.ScheduleTestHelper;
import org.openvpms.archetype.rules.workflow.TaskStatus;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.app.patient.PatientEditor;
import org.openvpms.web.app.workflow.EditClinicalEventTask;
import org.openvpms.web.app.workflow.TaskTracker;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.dialog.ConfirmationDialog;
import org.openvpms.web.component.dialog.PopupDialog;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserDialog;
import org.openvpms.web.component.im.query.ListQuery;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.workflow.ConfirmationTask;
import org.openvpms.web.component.workflow.EditIMObjectTask;
import org.openvpms.web.component.workflow.SelectIMObjectTask;
import org.openvpms.web.component.workflow.Task;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.test.AbstractAppTest;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.math.BigDecimal;


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


    @Test
    public void testCheckInFromAppointmentNoPatient() {
        Act appointment = createAppointment(null);
        WorkflowRunner workflow = new WorkflowRunner(appointment);
        workflow.setPatient(patient);                              // need to pre-set patient and worklist
        workflow.setWorkList(workList);                            // so they can be selected in popups
        workflow.start();

        // as the appointment has no patient, a pop should be displayed to select one
        workflow.checkSelectPatient(patient);
        workflow.checkSelectWorkList(workList);
        workflow.checkTask(TaskStatus.PENDING, customer, patient);

        workflow.checkPrintDocumentForm(PopupDialog.SKIP_ID);
        workflow.checkClinicalEvent(PopupDialog.OK_ID);
        workflow.checkAddWeight(BigDecimal.valueOf(10));
        workflow.checkComplete(true, customer, patient);
    }

    @Test
    public void testCheckInFromAppointmentWithPatient() {
        Act appointment = createAppointment(patient);
        WorkflowRunner workflow = new WorkflowRunner(appointment);
        workflow.setWorkList(workList);        // need to pre-set work list so it can be selected in popup
        workflow.start();

        workflow.checkSelectWorkList(workList);
        workflow.checkTask(TaskStatus.PENDING, customer, patient);

        workflow.checkPrintDocumentForm(PopupDialog.SKIP_ID);
        workflow.checkClinicalEvent(PopupDialog.OK_ID);
        workflow.checkAddWeight(BigDecimal.valueOf(20));
        workflow.checkComplete(true, customer, patient);
    }

    @Test
    public void testCreatePatient() {
        Act appointment = createAppointment(null);
        WorkflowRunner workflow = new WorkflowRunner(appointment);
        workflow.setWorkList(workList); // need to pre-set work list so it can be selected in popup
        workflow.start();

        BrowserDialog<Party> dialog = workflow.checkSelectionTask();
        fireDialogButton(dialog, BrowserDialog.NEW_ID);
        EditDialog editDialog = workflow.checkCreatePatient("Fluffy");
        Party newPatient = (Party) editDialog.getEditor().getObject();
        fireDialogButton(editDialog, PopupDialog.OK_ID);
        workflow.checkPatient(newPatient, customer);

        workflow.checkSelectWorkList(workList);
        workflow.checkTask(TaskStatus.PENDING, customer, newPatient);

        workflow.checkPrintDocumentForm(PopupDialog.SKIP_ID);
        workflow.checkClinicalEvent(PopupDialog.OK_ID);
        workflow.checkAddWeight(BigDecimal.ONE);
        workflow.checkComplete(true, customer, newPatient);
    }

    @Test
    public void testCancelCreatePatient() {
        Act appointment = createAppointment(null);
        WorkflowRunner workflow = new WorkflowRunner(appointment);
        workflow.start();

        BrowserDialog<Party> dialog = workflow.checkSelectionTask();
        fireDialogButton(dialog, BrowserDialog.NEW_ID);
        EditDialog editDialog = workflow.checkCreatePatient("Fluffy");
        Party patient = (Party) editDialog.getEditor().getObject();
        fireDialogButton(editDialog, PopupDialog.CANCEL_ID);
        assertNull(get(patient));
        workflow.checkComplete(false, null, null);
    }

    @Test
    public void testCancelCreatePatientByUserClose() {
        Act appointment = createAppointment(null);
        WorkflowRunner workflow = new WorkflowRunner(appointment);
        workflow.start();

        BrowserDialog<Party> dialog = workflow.checkSelectionTask();
        fireDialogButton(dialog, BrowserDialog.NEW_ID);
        EditDialog editDialog = workflow.checkCreatePatient("Fluffy");
        Party patient = (Party) editDialog.getEditor().getObject();
        editDialog.userClose();
        assertNull(get(patient));
        workflow.checkComplete(false, null, null);
    }

    @Test
    public void testCancelCheckInAtSelectPatient() {
        Act appointment = createAppointment(null);
        WorkflowRunner workflow = new WorkflowRunner(appointment);
        workflow.setPatient(patient);         // need to pre-set patient so it can be selected in popup
        workflow.start();

        BrowserDialog<Party> dialog = workflow.checkSelectionTask();
        fireDialogButton(dialog, PopupDialog.CANCEL_ID);
        workflow.checkComplete(false, null, null);
    }

    @Test
    public void testCancelCheckInAtSelectPatientByUserClose() {
        Act appointment = createAppointment(null);
        WorkflowRunner workflow = new WorkflowRunner(appointment);
        workflow.setPatient(patient);         // need to pre-set patient so it can be selected in popup
        workflow.start();

        BrowserDialog<Party> dialog = workflow.checkSelectionTask();
        dialog.userClose();
        workflow.checkComplete(false, null, null);
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
        context.setUser(user);
    }

    /**
     * Helper to create an appointment.
     *
     * @param patient the patient. May be <tt>null</tt>
     * @return a new appointment
     */
    private Act createAppointment(Party patient) {
        Date startTime = new Date();
        Date endTime = new Date();
        Party schedule = ScheduleTestHelper.createSchedule();

        Act act = ScheduleTestHelper.createAppointment(startTime, endTime, schedule, customer, patient, clinician,
                                                       null);
        act.setStatus(AppointmentStatus.PENDING);
        save(act);
        return act;
    }

    /**
     * Helper to create and save new <tt>party.organisationWorkList</em>.
     *
     * @param taskType the task type. May be <tt>null</tt>
     * @param noSlots  the no. of slots the task type takes up
     * @return a new schedule
     */
    private Party createWorkList(Entity taskType, int noSlots) {
        Party workList = (Party) create("party.organisationWorkList");
        EntityBean bean = new EntityBean(workList);
        bean.setValue("name", "XWorkList");
        if (taskType != null) {
            EntityRelationship relationship = bean.addNodeRelationship("taskTypes", taskType);
            IMObjectBean relBean = new IMObjectBean(relationship);
            relBean.setValue("noSlots", noSlots);
            relBean.setValue("default", true);
            save(workList, taskType);
        } else {
            bean.save();
        }
        return workList;
    }

    private class WorkflowRunner {

        private TestCheckInWorkflow workflow;
        private Act appointment;
        private TaskTracker tracker = new TaskTracker();

        public WorkflowRunner(Act appointment) {
            workflow = new TestCheckInWorkflow(appointment, context);
            workflow.addTaskListener(tracker);
            this.appointment = appointment;
        }

        public void setPatient(Party patient) {
            workflow.setPatient(patient);
        }

        public void setWorkList(Party workList) {
            workflow.setWorkList(workList);
        }

        public void start() {
            workflow.start();
        }

        @SuppressWarnings("unchecked")
        public BrowserDialog<Party> checkSelectionTask() {
            Task current = tracker.getCurrent();
            assertTrue(current instanceof SelectIMObjectTask);
            return ((SelectIMObjectTask<Party>) current).getBrowserDialog();
        }

        public void checkSelectPatient(Party patient) {
            BrowserDialog<Party> dialog = checkSelectionTask();
            Browser<Party> browser = dialog.getBrowser();
            fireSelection(browser, patient);
            assertEquals(patient, workflow.getContext().getPatient());
        }

        public EditDialog checkCreatePatient(String name) {
            Task current = tracker.getCurrent();
            assertTrue(current instanceof EditIMObjectTask);
            EditIMObjectTask edit = (EditIMObjectTask) current;
            EditDialog dialog = edit.getEditDialog();
            IMObjectEditor editor = dialog.getEditor();
            assertTrue(editor instanceof PatientEditor);
            Party patient = (Party) editor.getObject();
            assertTrue(patient.isNew());
            editor.getProperty("name").setValue(name);
            editor.getProperty("species").setValue(TestHelper.getLookup("lookup.species", "CANINE").getCode());
            return dialog;
        }

        public void checkPatient(Party patient, Party customer) {
            Party p = workflow.getContext().getPatient();
            assertEquals(patient, p);
            assertFalse(p.isNew());
            PatientRules rules = new PatientRules();
            customer = get(customer);
            assertNotNull(customer);
            assertTrue(rules.isOwner(customer, patient));
        }

        public void checkSelectWorkList(Party workList) {
            BrowserDialog<Party> dialog = checkSelectionTask();
            Browser<Party> browser = dialog.getBrowser();
            fireSelection(browser, workList);
            assertEquals(workList, workflow.getContext().getWorkList());
        }

        /**
         * Verifies that the current task is a confirmation, and selects the specified button.
         *
         * @param button the button identifier. If <tt>null</tt>, use the <tt>userClose</tt> method.
         */
        public void checkConfirmation(String button) {
            Task current = tracker.getCurrent();
            assertTrue(current instanceof ConfirmationTask);
            ConfirmationTask post = (ConfirmationTask) current;
            ConfirmationDialog dialog = post.getConfirmationDialog();
            if (button != null) {
                fireDialogButton(dialog, button);
            } else {
                dialog.userClose();
            }
        }

        public void checkPrintDocumentForm(String buttonId) {
            Task current = tracker.getCurrent();
            assertTrue(current instanceof SelectIMObjectTask);
            SelectIMObjectTask select = (SelectIMObjectTask) current;
            fireDialogButton(select.getBrowserDialog(), buttonId);
        }

        public void checkClinicalEvent(String buttonId) {
            Task current = tracker.getCurrent();
            assertTrue(current instanceof EditClinicalEventTask);
            EditClinicalEventTask edit = (EditClinicalEventTask) current;
            fireDialogButton(edit.getBrowserDialog(), buttonId);
        }

        public void checkTask(String status, Party customer, Party patient) {
            Act task = (Act) workflow.getContext().getObject(ScheduleArchetypes.TASK);
            assertNotNull(task);
            assertTrue(!task.isNew());  // has been saved
            assertEquals(status, task.getStatus());
            ActBean bean = new ActBean(task);
            assertEquals(bean.getNodeParticipant("customer"), customer);
            assertEquals(bean.getNodeParticipant("patient"), patient);
            assertEquals(bean.getNodeParticipant("worklist"), workList);
        }

        public EditDialog checkSetWeight(BigDecimal weight) {
            Task current = tracker.getCurrent();
            assertTrue(current instanceof EditIMObjectTask);
            EditIMObjectTask edit = (EditIMObjectTask) current;
            EditDialog dialog = edit.getEditDialog();
            IMObjectEditor editor = dialog.getEditor();
            assertTrue(TypeHelper.isA(editor.getObject(), PatientArchetypes.PATIENT_WEIGHT));
            editor.getProperty("weight").setValue(weight);
            return dialog;
        }

        public void checkWeight(BigDecimal weight) {
            Act event = (Act) workflow.getContext().getObject(PatientArchetypes.CLINICAL_EVENT);
            assertNotNull(event);
            ActBean bean = new ActBean(event);
            List<Act> acts = bean.getNodeActs("items");
            Act weightAct = IMObjectHelper.getObject(PatientArchetypes.PATIENT_WEIGHT, acts);
            assertNotNull(weightAct);
            acts.remove(weightAct);
            assertNull(IMObjectHelper.getObject(PatientArchetypes.PATIENT_WEIGHT, acts));
            ActBean weightBean = new ActBean(weightAct);
            assertTrue(weight.compareTo(weightBean.getBigDecimal("weight")) == 0);
        }

        public void checkAddWeight(BigDecimal weight) {
            EditDialog dialog = checkSetWeight(weight);
            fireDialogButton(dialog, PopupDialog.OK_ID);
            checkWeight(weight);
        }

        public void checkComplete(boolean appointmentUpdated, Party customer, Party patient) {
            assertEquals(patient, context.getPatient());
            assertEquals(customer, context.getCustomer());
            appointment = get(appointment);
            if (appointmentUpdated) {
                assertEquals(AppointmentStatus.CHECKED_IN, appointment.getStatus());
            } else {
                assertEquals(AppointmentStatus.PENDING, appointment.getStatus());
            }
        }

        /**
         * Helper to generate a table row selection event in a browser.
         *
         * @param browser the browser
         * @param object  the object to select. <em>Note:</em> must be present in the table
         */
        private void fireSelection(Browser<Party> browser, Party object) {
            browser.setSelected(object);

            // this is a bit brittle... TODO
            Table table = findComponent(browser.getComponent(), Table.class);
            assertNotNull(table);
            table.processInput(Table.INPUT_ACTION, null);
        }


    }

    private static class TestCheckInWorkflow extends CheckInWorkflow {

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
