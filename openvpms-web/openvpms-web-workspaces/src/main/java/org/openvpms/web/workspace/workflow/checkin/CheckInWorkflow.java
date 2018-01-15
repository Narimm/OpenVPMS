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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.checkin;

import nextapp.echo2.app.event.WindowPaneEvent;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.workflow.AppointmentRules;
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
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.hl7.patient.PatientContext;
import org.openvpms.hl7.patient.PatientContextFactory;
import org.openvpms.hl7.patient.PatientInformationService;
import org.openvpms.smartflow.client.FlowSheetServiceFactory;
import org.openvpms.smartflow.client.HospitalizationService;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.query.EntityQuery;
import org.openvpms.web.component.im.util.UserHelper;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.component.workflow.AbstractTask;
import org.openvpms.web.component.workflow.ConditionalCreateTask;
import org.openvpms.web.component.workflow.DefaultTaskContext;
import org.openvpms.web.component.workflow.DefaultTaskListener;
import org.openvpms.web.component.workflow.EditIMObjectTask;
import org.openvpms.web.component.workflow.LocalTask;
import org.openvpms.web.component.workflow.ReloadTask;
import org.openvpms.web.component.workflow.SelectIMObjectTask;
import org.openvpms.web.component.workflow.SynchronousTask;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.component.workflow.TaskEvent;
import org.openvpms.web.component.workflow.TaskProperties;
import org.openvpms.web.component.workflow.UpdateIMObjectTask;
import org.openvpms.web.component.workflow.WorkflowImpl;
import org.openvpms.web.echo.dialog.InformationDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.event.WindowPaneListener;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.patient.visit.FlowSheetEditDialog;
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
     * The flow sheet service factory.
     */
    private FlowSheetServiceFactory flowSheetServiceFactory;

    /**
     * The check-in workflow help topic.
     */
    private static final String HELP_TOPIC = "workflow/checkin";

    /**
     * Constructs a {@code CheckInWorkflow}.
     *
     * @param customer the customer
     * @param patient  the patient
     * @param context  the external context to access and update
     * @param help     the help context
     */
    public CheckInWorkflow(Party customer, Party patient, Context context, HelpContext help) {
        super(help.topic(HELP_TOPIC));
        initialise(null, customer, patient, null, null, null, context);
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
        User clinician = UserHelper.useLoggedInClinician(context) ? context.getUser()
                                                                  : (User) bean.getNodeParticipant("clinician");

        ArchetypeServiceFunctions functions = ServiceHelper.getBean(ArchetypeServiceFunctions.class);
        String reason = functions.lookup(appointment, "reason", "Appointment");
        String notes = bean.getString("description", "");
        String description = Messages.format("workflow.checkin.task.description", reason, notes);

        initialise(appointment, customer, patient, clinician, description, appointment.getReason(), context);
    }

    /**
     * Initialise the workflow.
     *
     * @param appointment     the appointment. May be {@code null}
     * @param customer        the customer
     * @param patient         the patient
     * @param clinician       the clinician. May be {@code null}
     * @param taskDescription the description to assign to the <em>act.customerTask</em>. May be {@code null}
     * @param reason          the visit reason code. May be {@code null}
     * @param context         the external context to access and update
     */
    private void initialise(Act appointment, Party customer, Party patient, User clinician, String taskDescription,
                            String reason, Context context) {
        flowSheetServiceFactory = ServiceHelper.getBean(FlowSheetServiceFactory.class);
        if (context.getPractice() == null) {
            throw new IllegalStateException("Context has no practice");
        }
        external = context;
        HelpContext help = getHelpContext();
        initial = new DefaultTaskContext(help);
        Entity schedule = null;
        Entity cageType = null;
        if (appointment != null) {
            initial.addObject(appointment);
            ActBean bean = new ActBean(appointment);
            schedule = bean.getNodeParticipant("schedule");
            if (schedule != null) {
                IMObjectBean scheduleBean = new IMObjectBean(schedule);
                cageType = (Entity) scheduleBean.getNodeTargetObject("cageType");
                initial.addObject(schedule);
            }
        }
        initial.setCustomer(customer);
        initial.setPatient(patient);
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

        // get the act.patientClinicalEvent.
        TaskProperties eventProps = new TaskProperties();
        eventProps.add("reason", reason);
        boolean newEvent = cageType != null;  // require a new event if the schedule has a cage type
        addTask(createGetClinicalEventTask(appointment, arrivalTime, eventProps, newEvent));

        boolean useWorkList = selectWorkList(schedule);
        if (useWorkList) {
            // optionally select a work list and edit a customer task
            addTask(new CustomerTaskWorkflow(arrivalTime, taskDescription, help));
        }

        // prompt for a patient weight.
        addTask(new PatientWeightTask(help));

        if (useWorkList && flowSheetServiceFactory.isSmartFlowSheetEnabled(initial.getLocation())) {
            addTask(new CreateFlowSheetTask());
        }

        // optionally print act.patientDocumentForm and act.patientDocumentLetters
        addTask(new PrintPatientDocumentsTask(getHelpContext()));

        // get the latest invoice, or create one if none is available
        addTask(new GetInvoiceTask());
        addTask(new ConditionalCreateTask(CustomerAccountArchetypes.INVOICE));

        // need to generate any admission messages prior to invoice editing
        addTask(new AdmissionTask());

        // edit the act.patientClinicalEvent in a local context, propagating the patient and customer on completion
        // If the task is cancelled, generate HL7 cancel admission messages
        EditVisitTask editVisitTask = createEditVisitTask();
        editVisitTask.addTaskListener(new DefaultTaskListener() {
            @Override
            public void taskEvent(TaskEvent event) {
                if (event.getType() == TaskEvent.Type.CANCELLED) {
                    CancelAdmissionTask task = new CancelAdmissionTask();
                    task.execute(getContext());
                }
            }
        });
        addTask(new LocalTask(editVisitTask, Context.PATIENT_SHORTNAME, Context.CUSTOMER_SHORTNAME));

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
     * Creates a task to get the act.patientClinicalEvent for the patient.
     *
     * @param appointment the appointment. If specified, this will be linked to new events. May be {@code null}
     * @param arrivalTime the date to use to locate the event
     * @param properties  properties to populate any created event. May be {@code null}
     * @param newEvent    if {@code true}, require a new event. If there is an In Progress event, terminate the task
     * @return a new task
     */
    protected GetClinicalEventTask createGetClinicalEventTask(Act appointment, Date arrivalTime,
                                                              TaskProperties properties, boolean newEvent) {
        return new GetClinicalEventTask(arrivalTime, properties, appointment, newEvent);
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
        return new SelectIMObjectTask<>(PatientArchetypes.PATIENT, context, patientEditor,
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
        return new SelectIMObjectTask<>(new EntityQuery<>(query, context), help);
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

    /**
     * Task to create a flow sheet if a work list has been selected and has "createFlowSheet" set.
     */
    private class CreateFlowSheetTask extends AbstractTask {

        /**
         * Creates a new {@link CreateFlowSheetTask}.
         */
        public CreateFlowSheetTask() {
            setRequired(false);
        }

        /**
         * Starts the task.
         *
         * @param context the task context
         * @throws OpenVPMSException for any error
         */
        @Override
        public void start(TaskContext context) {
            boolean popup = false;
            Party workList = context.getWorkList();
            Act visit = (Act) context.getObject(PatientArchetypes.CLINICAL_EVENT);
            Party patient = context.getPatient();
            Party location = context.getLocation();
            if (workList != null && visit != null && location != null) {
                IMObjectBean bean = new IMObjectBean(workList);
                String createFlowSheet = bean.getString("createFlowSheet");
                if (createFlowSheet != null) {
                    PatientContextFactory factory = ServiceHelper.getBean(PatientContextFactory.class);
                    final PatientContext patientContext = factory.createContext(patient, visit, location);
                    try {
                        if (patientContext.getWeight() != null) {
                            popup = addFlowSheet(createFlowSheet, bean, patientContext, context.getAppointment(),
                                                 location);
                        }
                    } catch (Throwable exception) {
                        popup = true;
                        ErrorHelper.show(exception, new WindowPaneListener() {
                            public void onClose(WindowPaneEvent event) {
                                notifyCompleted();
                            }
                        });
                    }
                }
            }
            if (!popup) {
                notifyCompleted();
            }
        }

        /**
         * Adds a flow sheet, if none exists.
         *
         * @param createFlowSheet the value of the work list <em>createFlowSheet</em> node
         * @param workList        the work list bean
         * @param patientContext  the patient context
         * @param appointment     the appointment. May be {@code null}
         * @param location        the practice location
         * @return {@code true} if a dialog is being displayed, indicating that the task is asynchronous
         */
        protected boolean addFlowSheet(String createFlowSheet, IMObjectBean workList,
                                       final PatientContext patientContext, Act appointment, Party location) {
            boolean popup = false;
            final HospitalizationService service = flowSheetServiceFactory.getHospitalizationService(location);
            if (!service.exists(patientContext)) {
                int expectedHospitalStay = workList.getInt("expectedHospitalStay");
                int defaultDepartment = workList.getInt("defaultFlowSheetDepartment", -1);
                String defaultTemplate = workList.getString("defaultFlowSheetTemplate");
                int days = 1;
                if (appointment != null) {
                    AppointmentRules rules = ServiceHelper.getBean(AppointmentRules.class);
                    days = rules.getBoardingDays(appointment);
                }
                if (expectedHospitalStay > days) {
                    days = expectedHospitalStay;
                }
                if ("PROMPT".equals(createFlowSheet)) {
                    final FlowSheetEditDialog dialog = new FlowSheetEditDialog(
                            flowSheetServiceFactory, location, defaultDepartment, defaultTemplate, days, true);
                    dialog.addWindowPaneListener(new PopupDialogListener() {
                        @Override
                        public void onOK() {
                            int departmentId = dialog.getDepartmentId();
                            String template = dialog.getTemplate();
                            int expectedStay = dialog.getExpectedStay();
                            service.add(patientContext, expectedStay, departmentId, template);
                            notifyCompleted();
                        }

                        @Override
                        public void onSkip() {
                            notifyCompleted();
                        }

                        @Override
                        public void onAction(String action) {
                            notifyCancelled();
                        }
                    });
                    dialog.show();
                    popup = true;
                } else {
                    service.add(patientContext, days, defaultDepartment, defaultTemplate);
                }
            } else {
                popup = true;
                InformationDialog.show(Messages.format("workflow.flowsheet.exists",
                                                       patientContext.getPatient().getName()),
                                       new WindowPaneListener() {
                                           public void onClose(WindowPaneEvent event) {
                                               notifyCompleted();
                                           }
                                       });
            }
            return popup;
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
        protected void populate(IMObject object, TaskProperties properties, TaskContext context) {
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

    private class AdmissionTask extends SynchronousTask {

        /**
         * Executes the task.
         *
         * @throws OpenVPMSException for any error
         */
        @Override
        public void execute(TaskContext context) {
            PatientContextFactory factory = ServiceHelper.getBean(PatientContextFactory.class);
            Act visit = (Act) context.getObject(PatientArchetypes.CLINICAL_EVENT);
            PatientContext pc = factory.createContext(context.getPatient(), context.getCustomer(), visit,
                                                      context.getLocation(), context.getClinician());
            PatientInformationService service = ServiceHelper.getBean(PatientInformationService.class);
            service.admitted(pc, context.getUser());
        }
    }

    private class CancelAdmissionTask extends SynchronousTask {

        /**
         * Executes the task.
         *
         * @throws OpenVPMSException for any error
         */
        @Override
        public void execute(TaskContext context) {
            PatientContextFactory factory = ServiceHelper.getBean(PatientContextFactory.class);
            Act visit = (Act) context.getObject(PatientArchetypes.CLINICAL_EVENT);
            PatientContext pc = factory.createContext(context.getPatient(), context.getCustomer(), visit,
                                                      context.getLocation(), context.getClinician());
            PatientInformationService service = ServiceHelper.getBean(PatientInformationService.class);
            service.admissionCancelled(pc, context.getUser());
        }
    }
}
