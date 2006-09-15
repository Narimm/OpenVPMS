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

package org.openvpms.web.app.workflow;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.workflow.AddActRelationshipTask;
import org.openvpms.web.component.workflow.CreateIMObjectTask;
import org.openvpms.web.component.workflow.EditIMObjectTask;
import org.openvpms.web.component.workflow.PrintIMObjectTask;
import org.openvpms.web.component.workflow.SelectIMObjectTask;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.component.workflow.TaskContextImpl;
import org.openvpms.web.component.workflow.TaskProperties;
import org.openvpms.web.component.workflow.Tasks;
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
     * @param appointment the appointment
     */
    public CheckInWorkflow(Act appointment) {
        ActBean bean = new ActBean(appointment);
        Party customer = (Party) bean.getParticipant("participation.customer");
        Party patient = (Party) bean.getParticipant("participation.patient");
        final User clinician
                = (User) bean.getParticipant("participation.clinician");
        initial = new TaskContextImpl();
        initial.setCustomer(customer);
        initial.setPatient(patient);
        initial.setClinician(clinician);
        initial.setWorkListDate(new Date());

        String workList = "party.organisationWorkList";
        String task = "act.customerTask";
        String document = "act.patientDocumentForm";
        String event = "act.patientClinicalEvent";
        String weight = "act.patientWeight";

        // select a worklist
        addTask(new SelectIMObjectTask<Party>(workList));

        // create and edit an act.customerTask
        TaskProperties taskProps = new TaskProperties();
        taskProps.add("description", getTaskDescription(appointment));
        addTask(new CreateIMObjectTask(task, taskProps));
        addTask(new EditIMObjectTask(task, true));

        // otionally select and print an act.patientDocumentForm
        SelectIMObjectTask<Act> docTask = new SelectIMObjectTask<Act>(document);
        docTask.setRequired(false);
        Tasks selectAndPrint = new Tasks();
        selectAndPrint.addTask(docTask);
        selectAndPrint.addTask(new PrintIMObjectTask(document));
        addTask(selectAndPrint);

        // create a new act.patientClinicalEvent
        TaskProperties eventProps = new TaskProperties();
        eventProps.add("reason", "Appointment");
        addTask(new CreateIMObjectTask(event, eventProps));
        addTask(new EditIMObjectTask(event, true));

        // prompt for a patient weight.
        addTask(new CreateIMObjectTask(weight));
        addTask(new EditIMObjectTask(weight));
        addTask(new AddActRelationshipTask(
                event, weight, "actRelationship.patientClinicalEventItem"));

        // update the appointment status
        TaskProperties appProps = new TaskProperties();
        appProps.add("status", "Checked In");
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
     * Returns the description for the task.
     *
     * @param appointment the appointment to derive the description from
     * @return the description for the task
     */
    private String getTaskDescription(Act appointment) {
        IMObjectBean bean = new IMObjectBean(appointment);
        String reason = bean.getString("reason", "");
        String notes = bean.getString("description", "");
        return Messages.get("workflow.checkin.task.description", reason, notes);
    }

}
