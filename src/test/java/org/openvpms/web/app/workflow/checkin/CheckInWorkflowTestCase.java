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

import static org.junit.Assert.assertNull;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.workflow.AppointmentStatus;
import org.openvpms.archetype.rules.workflow.ScheduleTestHelper;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.dialog.PopupDialog;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.query.BrowserDialog;
import org.openvpms.web.test.AbstractAppTest;
import static org.openvpms.web.test.EchoTestHelper.fireDialogButton;

import java.math.BigDecimal;
import java.util.Date;


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
        Act appointment = createAppointment(null);
        CheckInWorkflowRunner workflow = new CheckInWorkflowRunner(appointment, context);
        workflow.setPatient(patient);                              // need to pre-set patient and worklist
        workflow.setWorkList(workList);                            // so they can be selected in popups
        workflow.start();

        // as the appointment has no patient, a pop should be displayed to select one
        workflow.selectPatient(patient);

        // select the work list and verify a task has been created.
        workflow.selectWorkList(workList, customer, patient);

        workflow.printDocumentForm(PopupDialog.SKIP_ID);
        workflow.clinicalEvent(PopupDialog.OK_ID);
        workflow.addWeight(patient, BigDecimal.valueOf(10));
        workflow.checkComplete(true, customer, patient, context);
    }

    /**
     * Tests the check-in workflow when launched from an appointment with a patient.
     * <p/>
     * No patient selection dialog should be displayed.
     */
    @Test
    public void testCheckInFromAppointmentWithPatient() {
        Act appointment = createAppointment(patient);
        CheckInWorkflowRunner workflow = new CheckInWorkflowRunner(appointment, context);
        workflow.setWorkList(workList);        // need to pre-set work list so it can be selected in popup
        workflow.start();

        workflow.selectWorkList(workList, customer, patient);

        workflow.printDocumentForm(PopupDialog.SKIP_ID);
        workflow.clinicalEvent(PopupDialog.OK_ID);
        workflow.addWeight(patient, BigDecimal.valueOf(20));
        workflow.checkComplete(true, customer, patient, context);
    }

    /**
     * Verifies that a new patient can be created if the appointment doesn't have one.
     */
    @Test
    public void testCreatePatient() {
        Act appointment = createAppointment(null);
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
        workflow.checkContextPatient(newPatient, customer);

        workflow.selectWorkList(workList, customer, newPatient);

        workflow.printDocumentForm(PopupDialog.SKIP_ID);
        workflow.clinicalEvent(PopupDialog.OK_ID);
        workflow.addWeight(newPatient, BigDecimal.ONE);
        workflow.checkComplete(true, customer, newPatient, context);
    }

    /**
     * Verify that the workflow cancels if a new patient is created but editing cancelled via the cancel button.
     */
    @Test
    public void testCancelCreatePatient() {
        Act appointment = createAppointment(null);
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
        Act appointment = createAppointment(null);
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
        Act appointment = createAppointment(null);
        CheckInWorkflowRunner workflow = new CheckInWorkflowRunner(appointment, context);
        workflow.setPatient(patient);         // need to pre-set patient so it can be selected in popup
        workflow.start();

        BrowserDialog<Party> dialog = workflow.getSelectionDialog();
        fireDialogButton(dialog, PopupDialog.CANCEL_ID);
        workflow.checkComplete(false, null, null, context);
    }

    /**
     * Verify that the workflow cancels if a patient selection is cancelled via the 'user close' button.
     */
    @Test
    public void testCancelSelectPatientByUserClose() {
        Act appointment = createAppointment(null);
        CheckInWorkflowRunner workflow = new CheckInWorkflowRunner(appointment, context);
        workflow.setPatient(patient);         // need to pre-set patient so it can be selected in popup
        workflow.start();

        BrowserDialog<Party> dialog = workflow.getSelectionDialog();
        dialog.userClose();
        workflow.checkComplete(false, null, null, context);
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

}
