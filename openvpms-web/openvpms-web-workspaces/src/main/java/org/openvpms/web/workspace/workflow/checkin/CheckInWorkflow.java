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

import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.workflow.AppointmentStatus;
import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.ArchetypeServiceFunctions;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.query.EntityQuery;
import org.openvpms.web.component.workflow.ConditionalCreateTask;
import org.openvpms.web.component.workflow.DefaultTaskContext;
import org.openvpms.web.component.workflow.EditIMObjectTask;
import org.openvpms.web.component.workflow.LocalTask;
import org.openvpms.web.component.workflow.ReloadTask;
import org.openvpms.web.component.workflow.SelectIMObjectTask;
import org.openvpms.web.component.workflow.SynchronousTask;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.component.workflow.TaskProperties;
import org.openvpms.web.component.workflow.UpdateIMObjectTask;
import org.openvpms.web.component.workflow.WorkflowImpl;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.workspace.workflow.EditVisitTask;
import org.openvpms.web.workspace.workflow.GetClinicalEventTask;
import org.openvpms.web.workspace.workflow.GetInvoiceTask;

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
        String description = Messages.format("workflow.checkin.task.description", reason, notes);

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
        if (context.getPractice() == null) {
            throw new IllegalStateException("Context has no practice");
        }
        external = context;
        HelpContext help = getHelpContext();
        initial = new DefaultTaskContext(help);
        Entity schedule = null;
        if (appointment != null) {
            initial.addObject(appointment);
            ActBean bean = new ActBean(appointment);
            schedule = bean.getNodeParticipant("schedule");
            if (schedule != null) {
                initial.addObject(schedule);
            }
        }
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

        Date arrivalTime = getArrivalTime();

        if (patient == null) {
            // select/create a patient
            EditIMObjectTask patientEditor = new EditIMObjectTask(PatientArchetypes.PATIENT, true);
            addTask(createSelectPatientTask(initial, patientEditor));
            addTask(new UpdateIMObjectTask(PatientArchetypes.PATIENT, new TaskProperties(), true));
        }

        if (selectWorkList(schedule)) {
            // optionally select a work list and edit a customer task
            addTask(new CustomerTaskWorkflow(arrivalTime, taskDescription, help));
        }

        // get the act.patientClinicalEvent.
        TaskProperties eventProps = new TaskProperties();
        eventProps.add("reason", reason);
        addTask(new GetClinicalEventTask(arrivalTime, eventProps));

        // prompt for a patient weight.
        addTask(new PatientWeightTask(help));

        // optionally print act.patientDocumentForm and act.patientDocumentLetters
        addTask(new PrintPatientDocumentsTask(getHelpContext()));

        // get the latest invoice, or create one if none is available
        addTask(new GetInvoiceTask());
        addTask(new ConditionalCreateTask(CustomerAccountArchetypes.INVOICE));

        // edit the act.patientClinicalEvent in a local context, propagating the patient and customer on completion
        addTask(new LocalTask(createEditVisitTask(), Context.PATIENT_SHORTNAME, Context.CUSTOMER_SHORTNAME));

        // Reload the task to refresh the context with any edits made
        addTask(new ReloadTask(PatientArchetypes.CLINICAL_EVENT));

        if (appointment != null) {
            // update the appointment status
            TaskProperties appProps = new TaskProperties();
            appProps.add("status", AppointmentStatus.CHECKED_IN);
            addTask(new UpdateAppointmentTask(appointment, arrivalTime, appProps));
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
     * Returns the time that the customer arrived for the appointment.
     * <p/>
     * This is used to:
     * <ul>
     * <li>select a Visit</li>
     * <li>set the start time of a customer task</li>
     * <li>set the arrivalTime on the appointment</li>
     * </ul>
     *
     * @return the arrival time. Defaults to now.
     */
    protected Date getArrivalTime() {
        return new Date();
    }

    /**
     * Returns the initial context.
     *
     * @return the initial context
     */
    protected Context getInitialContext() {
        return initial;
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
    protected SelectIMObjectTask<Entity> createSelectWorkListTask(TaskContext context) {
        HelpContext help = context.getHelpContext().topic("worklist");
        ScheduleWorkListQuery query = new ScheduleWorkListQuery(context.getSchedule(), context.getLocation());
        return new SelectIMObjectTask<Entity>(new EntityQuery(query, context), help);
    }

    /**
     * Creates a new {@link EditVisitTask}.
     *
     * @return a new task to edit the visit
     */
    protected EditVisitTask createEditVisitTask() {
        return new EditVisitTask();
    }

    /**
     * Determines a work list should be selected.
     *
     * @param schedule the appointment schedule. May be {@code null}
     * @return {@code true} if work-lists should be selected
     */
    private boolean selectWorkList(Entity schedule) {
        boolean result = true;
        if (schedule != null) {
            IMObjectBean bean = new IMObjectBean(schedule);
            boolean useAllWorkLists = bean.getBoolean("useAllWorkLists", true);
            if (!useAllWorkLists) {
                result = !bean.getValues("workLists").isEmpty();
            }
        }
        return result;
    }

    private class CustomerTaskWorkflow extends WorkflowImpl {

        /**
         * Constructs a {@code CustomerTaskWorkflow}.
         *
         * @param date            the task date
         * @param taskDescription the task description
         * @param help            the help context
         */
        public CustomerTaskWorkflow(Date date, String taskDescription, HelpContext help) {
            super(help);
            // select a work list
            SelectIMObjectTask<Entity> selectWorkList = createSelectWorkListTask(initial);
            selectWorkList.setRequired(false);
            addTask(selectWorkList);

            setRequired(false);
            setBreakOnSkip(true);

            // create and edit an act.customerTask
            TaskProperties taskProps = new TaskProperties();
            taskProps.add("description", taskDescription);
            taskProps.add("startTime", date);
            addTask(new EditIMObjectTask(ScheduleArchetypes.TASK, taskProps, false));

            // update the task with the startTime, as it loses second accuracy. Without this, there is a small chance
            // that the incorrect visit will be selected, when performing a consult from the task.
            addTask(new UpdateIMObjectTask(ScheduleArchetypes.TASK, taskProps, true));
        }

    }

    private class UpdateAppointmentTask extends UpdateIMObjectTask {

        /**
         * The customer arrival time.
         */
        private final Date arrivalTime;

        /**
         * Constructs an {@code UpdateAppointmentTask}.
         *
         * @param object      the object to update
         * @param arrivalTime the customer arrival time
         * @param properties  properties to populate the object with
         */
        public UpdateAppointmentTask(IMObject object, Date arrivalTime, TaskProperties properties) {
            super(object, properties);
            this.arrivalTime = arrivalTime;
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
            bean.setValue("arrivalTime", arrivalTime);
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
