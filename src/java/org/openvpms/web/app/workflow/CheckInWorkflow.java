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
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.im.query.DefaultActQuery;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.util.IMObjectHelper;
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
import java.util.List;


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
        appProps.add("status", "Checked In");
        addTask(new UpdateIMObjectTask(appointment, appProps));
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
        String document = "act.patientDocumentForm";
        String event = "act.patientClinicalEvent";
        String weight = "act.patientWeight";

        // select a worklist
        addTask(new SelectIMObjectTask<Party>(workList));

        // create and edit an act.customerTask
        TaskProperties taskProps = new TaskProperties();
        taskProps.add("description", taskDescription);
        addTask(new CreateIMObjectTask(task, taskProps));
        addTask(new EditCustomerTask(task));

        // optionally select and print an act.patientDocumentForm
        String[] shortNames = {document};
        String[] statuses = new String[0];
        Query<Act> query = new DefaultActQuery(patient, "patient",
                                               "participation.patient",
                                               shortNames, statuses);
        SelectIMObjectTask<Act> docTask = new SelectIMObjectTask<Act>(query);
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
    }

    /**
     * Starts the workflow.
     */
    @Override
    public void start() {
        super.start(initial);
    }

    class EditCustomerTask extends EditIMObjectTask {

        /**
         * Constructs a new <code>EditCustomerTask</code>.
         *
         * @param shortName the object short name
         */
        public EditCustomerTask(String shortName) {
            super(shortName, true);
        }

        /**
         * Edits an object.
         *
         * @param object  the object to edit
         * @param context the task context
         */
        @Override
        protected void edit(IMObject object, TaskContext context) {
            ActBean bean = new ActBean((Act) object);
            Party workList = context.getWorkList();
            Entity taskType = getDefaultTaskType(workList);
            if (taskType != null) {
                bean.setParticipant("participation.taskType", taskType);
            }
            super.edit(object, context);
        }

        /**
         * Returns the default task type associated with a work list.
         *
         * @param workList the work list
         * @return a the default task types associated with
         *         <code>workList</code>, or <code>null</code> if there is no
         *         default task type
         */
        private Entity getDefaultTaskType(Party workList) {
            Entity type = null;
            EntityBean bean = new EntityBean(workList);
            List<IMObject> relationships = bean.getValues("taskTypes");
            for (IMObject object : relationships) {
                EntityRelationship relationship = (EntityRelationship) object;
                IMObjectBean relBean = new IMObjectBean(relationship);
                if (relBean.getBoolean("default")) {
                    type = (Entity) IMObjectHelper.getObject(
                            relationship.getTarget());
                    if (type != null) {
                        break;
                    }
                }
            }
            return type;
        }
    }
}
