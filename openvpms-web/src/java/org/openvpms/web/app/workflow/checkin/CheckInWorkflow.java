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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.workflow.checkin;

import org.openvpms.archetype.rules.workflow.AppointmentStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.component.workflow.CreateIMObjectTask;
import org.openvpms.web.component.workflow.EditIMObjectTask;
import org.openvpms.web.component.workflow.SelectIMObjectTask;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.component.workflow.TaskContextImpl;
import org.openvpms.web.component.workflow.TaskProperties;
import org.openvpms.web.component.workflow.UpdateIMObjectTask;
import org.openvpms.web.component.workflow.WorkflowImpl;
import org.openvpms.web.resource.util.Messages;

import java.util.Date;


/**
 * Check-in workflow.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class CheckInWorkflow extends WorkflowImpl {

    /**
     * The initial context.
     */
    private TaskContext initial;


    /**
     * Constructs a new <code>CheckInWorkflow</code>.
     *
     * @param customer  the customer
     * @param patient   the patient
     * @param clinician the user. May be <code>null</code>
     */
    public CheckInWorkflow(Party customer, Party patient, User clinician) {
        initialise(customer, patient, clinician, null);
    }

    /**
     * Constructs a new <code>CheckInWorkflow</code> from an appointment.
     *
     * @param appointment the appointment
     */
    public CheckInWorkflow(Act appointment) {
        ActBean bean = new ActBean(appointment);
        Party customer = (Party) bean.getParticipant("participation.customer");
        Party patient = (Party) bean.getParticipant("participation.patient");
        final User clinician
                = (User) bean.getParticipant("participation.clinician");

        String reason = bean.getString("reason", "");
        String notes = bean.getString("description", "");
        String description = Messages.get("workflow.checkin.task.description",
                                          reason, notes);

        initialise(customer, patient, clinician, description);

        // update the appointment status
        TaskProperties appProps = new TaskProperties();
        appProps.add("status", AppointmentStatus.CHECKED_IN);
        addTask(new UpdateIMObjectTask(appointment, appProps));
    }

    /**
     * Starts the workflow.
     */
    @Override
    public void start() {
        super.start(initial);
    }

    /**
     * Initialise the workflow.
     *
     * @param customer        the customer
     * @param patient         the patient
     * @param clinician       the clinician. May be <code>null</code>
     * @param taskDescription the description to assign to the
     *                        <em>act.customerTask</em>. May be
     *                        <code>null</code>
     */
    private void initialise(Party customer, Party patient, User clinician,
                            String taskDescription) {
        initial = new TaskContextImpl();
        initial.setCustomer(customer);
        initial.setPatient(patient);
        initial.setClinician(clinician);
        initial.setWorkListDate(new Date());

        String workList = "party.organisationWorkList";
        String task = "act.customerTask";
        String event = "act.patientClinicalEvent";

        // select a worklist
        addTask(new SelectIMObjectTask<Party>(workList, initial));

        // create and edit an act.customerTask
        TaskProperties taskProps = new TaskProperties();
        taskProps.add("description", taskDescription);
        addTask(new CreateIMObjectTask(task, taskProps));
        addTask(new EditCustomerTask(task));

        // optionally select and print an act.patientDocumentForm
        addTask(new PrintDocumentFormTask());

        // create a new act.patientClinicalEvent
        TaskProperties eventProps = new TaskProperties();
        eventProps.add("reason", "Appointment");
        addTask(new EditIMObjectTask(event, eventProps, true));

        // prompt for a patient weight.
        addTask(new PatientWeightTask());
    }
}
