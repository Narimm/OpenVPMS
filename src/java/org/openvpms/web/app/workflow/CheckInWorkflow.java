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
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.workflow.AddActRelationshipTask;
import org.openvpms.web.component.workflow.CreateIMObjectTask;
import org.openvpms.web.component.workflow.EditIMObjectTask;
import org.openvpms.web.component.workflow.PrintIMObjectTask;
import org.openvpms.web.component.workflow.SelectIMObjectTask;
import org.openvpms.web.component.workflow.Tasks;
import org.openvpms.web.component.workflow.UpdateIMObjectTask;
import org.openvpms.web.component.workflow.WorkflowImpl;

import java.util.HashMap;
import java.util.Map;


/**
 * Check-in workflow.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class CheckInWorkflow extends WorkflowImpl {

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
        Context context = Context.getInstance();
        context.setCustomer(customer);
        context.setPatient(patient);
        context.setClinician(clinician);

        String workList = "party.organisationWorkList";
        String task = "act.customerTask";
        String document = "act.patientDocumentForm";
        String event = "act.patientClinicalEvent";
        String weight = "act.patientWeight";

        // select a worklist
        addTask(new SelectIMObjectTask<Party>(workList, true));

        // create and edit an act.customerTask
        addTask(new CreateIMObjectTask(task));
        addTask(new EditIMObjectTask(task));

        // otionally select and print an act.patientDocumentForm
        SelectIMObjectTask<Act> docTask = new SelectIMObjectTask<Act>(document);
        docTask.setRequired(false);
        Tasks selectAndPrint = new Tasks();
        selectAndPrint.addTask(docTask);
        selectAndPrint.addTask(new PrintIMObjectTask(document));
        addTask(selectAndPrint);

        // create a new act.patientClinicalEvent
        Map<String, Object> taskProps = new HashMap<String, Object>();
        taskProps.put("reason", "Appointment");
        addTask(new CreateIMObjectTask(event, taskProps));
        addTask(new EditIMObjectTask(event, true));

        // prompt for a patient weight.
        addTask(new CreateIMObjectTask(weight));
        addTask(new EditIMObjectTask(weight));
        addTask(new AddActRelationshipTask(
                event, weight, "actRelationship.patientClinicalEventItem"));

        // update the appointment status
        Map<String, Object> appProps = new HashMap<String, Object>();
        appProps.put("status", "Checked In");
        addTask(new UpdateIMObjectTask(appointment, appProps));
    }

}
