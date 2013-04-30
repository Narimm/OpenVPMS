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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.app.workflow.checkin;

import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.workflow.AppointmentStatus;
import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.ArchetypeServiceFunctions;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.app.workflow.EditVisitTask;
import org.openvpms.web.app.workflow.GetClinicalEventTask;
import org.openvpms.web.app.workflow.GetInvoiceTask;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.help.HelpContext;
import org.openvpms.web.component.workflow.ConditionalCreateTask;
import org.openvpms.web.component.workflow.CreateIMObjectTask;
import org.openvpms.web.component.workflow.DefaultTaskContext;
import org.openvpms.web.component.workflow.EditIMObjectTask;
import org.openvpms.web.component.workflow.ReloadTask;
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
 * @author Tim Anderson
 */
public class CheckInWorkflow extends WorkflowImpl {

    /**
     * The initial context.
     */
    private TaskContext initial;

    /**
     * The external context to access and update.
     */
    private Context external;

    /**
     * The check-in workflow help topic.
     */
    private static final String HELP_TOPIC = "workflow/checkin";

    /**
     * Constructs a {@code CheckInWorkflow}.
     *
     * @param customer  the customer
     * @param patient   the patient
     * @param clinician the user. May be {@code null}
     * @param context   the external context to access and update
     * @param help      the help context
     */
    public CheckInWorkflow(Party customer, Party patient, User clinician, Context context, HelpContext help) {
        super(help.topic(HELP_TOPIC));
        initialise(null, customer, patient, clinician, null, null, context);
    }

    /**
     * Constructs a {@code CheckInWorkflow} from an appointment.
     *
     * @param appointment the appointment
     * @param context     the external context to access and update
     * @param help        the help context
     */
    public CheckInWorkflow(Act appointment, Context context, HelpContext help) {
        super(help.topic(HELP_TOPIC));
        initialise(appointment, context);
    }

    /**
     * Constructs a {@code CheckInWorkflow}.
     * <p/>
     * The workflow must be initialised via {@link #initialise} prior to use.
     *
     * @param help the help context
     */
    protected CheckInWorkflow(HelpContext help) {
        super(help);
    }

    /**
     * Starts the workflow.
     */
    @Override
    public void start() {
        super.start(initial);
    }

    /**
     * Initialises the workflow from an appointment.
     *
     * @param appointment the appointment
     * @param context     the external context to access and update
     */
    protected void initialise(Act appointment, Context context) {
        ActBean bean = new ActBean(appointment);
        Party customer = (Party) bean.getParticipant("participation.customer");
        Party patient = (Party) bean.getParticipant("participation.patient");
        User clinician = (User) bean.getParticipant("participation.clinician");

        String reason = ArchetypeServiceFunctions.lookup(appointment, "reason", "Appointment");
        String notes = bean.getString("description", "");
        String description = Messages.get("workflow.checkin.task.description", reason, notes);

        initialise(appointment, customer, patient, clinician, description, reason, context);
    }

    /**
     * Initialise the workflow.
     *
     * @param appointment     the appointment. May be {@code null}
     * @param customer        the customer
     * @param patient         the patient
     * @param clinician       the clinician. May be {@code null}
     * @param taskDescription the description to assign to the <em>act.customerTask</em>. May be {@code null}
     * @param reason          the description to assign to the <em>act.patientClinicalEvent</em>. May be {@code null}
     * @param context         the external context to access and update
     */
    private void initialise(Act appointment, Party customer, Party patient, User clinician, String taskDescription,
                            String reason, Context context) {
        external = context;
        HelpContext help = getHelpContext();
        initial = new DefaultTaskContext(help);
        initial.setCustomer(customer);
        initial.setPatient(patient);

        if (clinician == null) {
            clinician = context.getClinician();
        }
        initial.setClinician(clinician);
        initial.setUser(external.getUser());
        initial.setWorkListDate(new Date());
        initial.setScheduleDate(external.getScheduleDate());
        initial.setPractice(external.getPractice());
        initial.setLocation(external.getLocation());

        if (patient == null) {
            // select/create a patient
            EditIMObjectTask patientEditor = new EditIMObjectTask(PatientArchetypes.PATIENT, true);
            addTask(createSelectPatientTask(initial, patientEditor));
            addTask(new UpdateIMObjectTask(PatientArchetypes.PATIENT, new TaskProperties(), true));
        }

        // optionally select a worklist and edit a customer task
        addTask(new CustomerTaskWorkflow(taskDescription, help));

        // get the act.patientClinicalEvent.
        TaskProperties eventProps = new TaskProperties();
        eventProps.add("reason", reason);
        Date date = (appointment != null) ? appointment.getActivityStartTime() : new Date();
        addTask(new GetClinicalEventTask(date, eventProps));

        // prompt for a patient weight.
        addTask(new PatientWeightTask(help));

        // optionally select and print an act.patientDocumentForm
        addTask(new PrintDocumentFormTask(initial));

        // get the latest invoice, or create one if none is available
        addTask(new GetInvoiceTask());
        addTask(new ConditionalCreateTask(CustomerAccountArchetypes.INVOICE));

        // edit the act.patientClinicalEvent
        addTask(createEditVisitTask());

        // Reload the task to refresh the context with any edits made
        addTask(new ReloadTask(PatientArchetypes.CLINICAL_EVENT));

        if (appointment != null) {
            // update the appointment status
            TaskProperties appProps = new TaskProperties();
            appProps.add("status", AppointmentStatus.CHECKED_IN);
            addTask(new UpdateAppointmentTask(appointment, appProps));
        }

        // add a task to update the global context at the end of the workflow
        addTask(new SynchronousTask() {
            public void execute(TaskContext context) {
                external.setPatient(context.getPatient());
                external.setCustomer(context.getCustomer());
            }
        });
    }

    /**
     * Creates a new {@link SelectIMObjectTask} to select a patient.
     *
     * @param context       the context
     * @param patientEditor the patient editor, if a new patient is selected
     * @return a new task to select a patient
     */
    protected SelectIMObjectTask<Party> createSelectPatientTask(TaskContext context, EditIMObjectTask patientEditor) {
        return new SelectIMObjectTask<Party>(PatientArchetypes.PATIENT, context, patientEditor,
                                             context.getHelpContext().topic("patient"));
    }

    /**
     * Creates a new {@link SelectIMObjectTask} to select a work list.
     *
     * @param context the context
     * @return a new task to select a work list
     */
    protected SelectIMObjectTask<Party> createSelectWorkListTask(TaskContext context) {
        HelpContext help = context.getHelpContext().topic("worklist");
        return new SelectIMObjectTask<Party>(ScheduleArchetypes.ORGANISATION_WORKLIST, context, help);
    }

    /**
     * Creates a new {@link EditVisitTask}.
     *
     * @return a new task to edit the visit
     */
    protected EditVisitTask createEditVisitTask() {
        return new EditVisitTask();
    }

    private class CustomerTaskWorkflow extends WorkflowImpl {

        /**
         * Constructs a {@code CustomerTaskWorkflow}.
         *
         * @param taskDescription the task description
         * @param help            the help context
         */
        public CustomerTaskWorkflow(String taskDescription, HelpContext help) {
            super(help);
            // select a worklist
            SelectIMObjectTask<Party> selectWorkList = createSelectWorkListTask(initial);
            selectWorkList.setRequired(false);
            addTask(selectWorkList);

            setRequired(false);
            setBreakOnSkip(true);

            // create and edit an act.customerTask
            TaskProperties taskProps = new TaskProperties();
            taskProps.add("description", taskDescription);
            addTask(new CreateIMObjectTask(ScheduleArchetypes.TASK, taskProps));
            addTask(new EditIMObjectTask(ScheduleArchetypes.TASK, false, false));
        }

    }

    private class UpdateAppointmentTask extends UpdateIMObjectTask {

        /**
         * Constructs an {@code UpdateAppointmentTask}.
         *
         * @param object     the object to update
         * @param properties properties to populate the object with
         */
        public UpdateAppointmentTask(IMObject object, TaskProperties properties) {
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
                bean.addParticipation("participation.patient", context.getPatient());
            }
            Act act = (Act) context.getObject("act.customerTask");
            if (act != null) {
                bean.addRelationship("actRelationship.customerAppointmentTask", act);
            }
        }
    }
}
