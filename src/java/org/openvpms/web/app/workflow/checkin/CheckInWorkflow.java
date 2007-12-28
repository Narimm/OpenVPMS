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
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.workflow.CreateIMObjectTask;
import org.openvpms.web.component.workflow.DefaultTaskContext;
import org.openvpms.web.component.workflow.EditIMObjectTask;
import org.openvpms.web.component.workflow.SelectIMObjectTask;
import org.openvpms.web.component.workflow.SynchronousTask;
import org.openvpms.web.component.workflow.TaskContext;
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
     * Work list archetype short name.
     */
    private static final String WORK_LIST_SHORTNAME
            = "party.organisationWorkList";

    /**
     * Task act archetype short name.
     */
    private static final String CUSTOMER_TASK_SHORTNAME = "act.customerTask";

    /**
     * Clinical event act archetype short name.
     */
    private static final String CLINICAL_EVENT = "act.patientClinicalEvent";


    /**
     * Constructs a new <tt>CheckInWorkflow</tt>.
     *
     * @param customer  the customer
     * @param patient   the patient
     * @param clinician the user. May be <tt>null</tt>
     */
    public CheckInWorkflow(Party customer, Party patient, User clinician) {
        initialise(null, customer, patient, clinician, null);
    }

    /**
     * Constructs a new <tt>CheckInWorkflow</tt> from an appointment.
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

        initialise(appointment, customer, patient, clinician, description);
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
     * @param appointment     the appointment. May be <tt>null</tt>
     * @param customer        the customer
     * @param patient         the patient
     * @param clinician       the clinician. May be <tt>null</tt>
     * @param taskDescription the description to assign to the
     *                        <em>act.customerTask</em>. May be
     *                        <tt>null</tt>
     */
    private void initialise(Act appointment, Party customer, Party patient,
                            User clinician, String taskDescription) {
        final GlobalContext global = GlobalContext.getInstance();
        initial = new DefaultTaskContext(false);
        initial.setCustomer(customer);
        initial.setPatient(patient);

        if (clinician == null) {
            clinician = global.getClinician();
        }
        initial.setClinician(clinician);
        initial.setUser(global.getUser());
        initial.setWorkListDate(new Date());
        initial.setScheduleDate(global.getScheduleDate());
        initial.setLocation(global.getLocation());

        if (patient == null) {
            // select/create a patient
            String pet = "party.patientpet";
            EditIMObjectTask patientEditor = new EditIMObjectTask(pet, true);
            addTask(new SelectIMObjectTask<Party>(pet, initial, patientEditor));
            addTask(new UpdateIMObjectTask(pet, new TaskProperties(), true));
        }

        // optionally select a worklist and edit a customer task
        addTask(new CustomerTaskWorkflow(taskDescription));

        // create a new act.patientClinicalEvent. Note that the event
        // is created prior to printing any forms in order for them to be
        // printed on check out if not printed on check in.
        TaskProperties eventProps = new TaskProperties();
        eventProps.add("reason", "Appointment");
        addTask(new CreateIMObjectTask(CLINICAL_EVENT, eventProps));

        // optionally select and print an act.patientDocumentForm
        addTask(new PrintDocumentFormTask());

        // edit the act.patientClinicalEvent
        addTask(new EditIMObjectTask(CLINICAL_EVENT));

        // prompt for a patient weight.
        addTask(new PatientWeightTask());

        if (appointment != null) {
            // update the appointment status
            TaskProperties appProps = new TaskProperties();
            appProps.add("status", AppointmentStatus.CHECKED_IN);
            addTask(new UpdateAppointmentTask(appointment, appProps));
        }

        // add a task to update the global context at the end of the workflow
        addTask(new SynchronousTask() {
            public void execute(TaskContext context) {
                global.setClinician(context.getClinician());
            }
        });
    }

    private class CustomerTaskWorkflow extends WorkflowImpl {

        /**
         * Constructs a new <code>CustomerTaskWorkflow</code>.
         */
        public CustomerTaskWorkflow(String taskDescription) {
            // select a worklist
            SelectIMObjectTask<Party> selectWorkList
                    = new SelectIMObjectTask<Party>(WORK_LIST_SHORTNAME,
                                                    initial);
            selectWorkList.setRequired(false);
            addTask(selectWorkList);

            setRequired(false);
            setBreakOnSkip(true);

            // create and edit an act.customerTask
            TaskProperties taskProps = new TaskProperties();
            taskProps.add("description", taskDescription);
            addTask(new CreateIMObjectTask(CUSTOMER_TASK_SHORTNAME, taskProps));
            addTask(new EditIMObjectTask(CUSTOMER_TASK_SHORTNAME, false,
                                         false));
        }

    }

    private class UpdateAppointmentTask extends UpdateIMObjectTask {

        /**
         * Creates a new <tt>UpdateAppointmentTask</tt>.
         *
         * @param object     the object to update
         * @param properties properties to populate the object with
         */
        public UpdateAppointmentTask(IMObject object,
                                     TaskProperties properties) {
            super(object, properties);
        }

        /**
         * Populates an object.
         *
         * @param object     the object to populate
         * @param properties the properties
         * @param context    the task context
         */
        @Override
        protected void populate(IMObject object, TaskProperties properties,
                                TaskContext context) {
            super.populate(object, properties, context);
            ActBean bean = new ActBean((Act) object);
            bean.setValue("arrivalTime", new Date());
            if (bean.getParticipantRef("participation.patient") == null) {
                bean.addParticipation("participation.patient",
                                      context.getPatient());
            }
            Act act = (Act) context.getObject("act.customerTask");
            if (act != null) {
                bean.addRelationship("actRelationship.customerAppointmentTask",
                                     act);
            }
        }
    }
}
