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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.checkin;

import nextapp.echo2.app.event.WindowPaneEvent;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.workflow.AppointmentStatus;
import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.hl7.patient.PatientContext;
import org.openvpms.hl7.patient.PatientContextFactory;
import org.openvpms.hl7.patient.PatientInformationService;
import org.openvpms.smartflow.client.FlowSheetServiceFactory;
import org.openvpms.smartflow.client.HospitalizationService;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.util.UserHelper;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.component.workflow.AbstractTask;
import org.openvpms.web.component.workflow.ConditionalCreateTask;
import org.openvpms.web.component.workflow.DefaultTaskContext;
import org.openvpms.web.component.workflow.DefaultTaskListener;
import org.openvpms.web.component.workflow.LocalTask;
import org.openvpms.web.component.workflow.PrintIMObjectTask;
import org.openvpms.web.component.workflow.ReloadTask;
import org.openvpms.web.component.workflow.SynchronousTask;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.component.workflow.TaskEvent;
import org.openvpms.web.component.workflow.TaskProperties;
import org.openvpms.web.component.workflow.Tasks;
import org.openvpms.web.component.workflow.UpdateIMObjectTask;
import org.openvpms.web.component.workflow.WorkflowImpl;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.event.WindowPaneListener;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.customer.CustomerMailContext;
import org.openvpms.web.workspace.workflow.EditVisitTask;
import org.openvpms.web.workspace.workflow.GetInvoiceTask;

import java.util.Collection;
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
        User clinician = UserHelper.useLoggedInClinician(context) ? context.getUser() : null;
        initialise(null, customer, patient, clinician, context);
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
                                                                  : bean.getTarget("clinician", User.class);

        initialise(appointment, customer, patient, clinician, context);
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
     * Creates a new {@link EditVisitTask}.
     *
     * @return a new task to edit the visit
     */
    protected EditVisitTask createEditVisitTask() {
        return new EditVisitTask();
    }

    /**
     * Initialise the workflow.
     *
     * @param appointment the appointment. May be {@code null}
     * @param customer    the customer
     * @param patient     the patient
     * @param clinician   the clinician. May be {@code null}
     * @param context     the external context to access and update
     */
    private void initialise(Act appointment, Party customer, Party patient, User clinician, Context context) {
        flowSheetServiceFactory = ServiceHelper.getBean(FlowSheetServiceFactory.class);
        if (context.getPractice() == null) {
            throw new IllegalStateException("Context has no practice");
        }
        external = context;
        HelpContext help = getHelpContext();
        initial = new DefaultTaskContext(help);
        if (appointment != null) {
            initial.addObject(appointment);
            IMObjectBean bean = new IMObjectBean(appointment);
            Entity schedule = bean.getTarget("schedule", Entity.class);
            if (schedule != null) {
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

        addTask(new CheckInDialogTask(getHelpContext()));

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
            addTask(new ReloadTask(ScheduleArchetypes.APPOINTMENT));
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

    class CheckInDialogTask extends Tasks {

        private CheckInDialog dialog;

        /**
         * Constructs a {@code Tasks}.
         *
         * @param help the help context
         */
        CheckInDialogTask(HelpContext help) {
            super(help);
        }

        /**
         * Starts the task.
         *
         * @param context the task context
         * @throws OpenVPMSException for any error
         */
        @Override
        public void start(TaskContext context) {
            dialog = new CheckInDialog(context.getCustomer(), context.getPatient(), context.getSchedule(),
                                       context.getClinician(), context.getLocation(), getArrivalTime(),
                                       context.getAppointment(), context.getUser(),
                                       context.getHelpContext());
            dialog.addWindowPaneListener(new PopupDialogListener() {
                @Override
                public void onOK() {
                    context.setPatient(dialog.getPatient());
                    context.setClinician(dialog.getClinician());
                    context.addObject(dialog.getEvent());

                    context.setTask(dialog.getTask());

                    FlowSheetInfo flowsheet = dialog.getFlowSheetInfo();
                    boolean completed = true;
                    if (flowsheet != null) {
                        addTask(new CreateFlowSheetTask(flowsheet));
                        completed = false;
                    }

                    Collection<Entity> templates = dialog.getTemplates();
                    if (!templates.isEmpty()) {
                        addPrintTasks(templates, context);
                        completed = false;
                    }
                    if (completed) {
                        notifyCompleted();
                    } else {
                        CheckInDialogTask.super.start(context);
                    }
                }

                @Override
                public void onAction(String action) {
                    notifyCancelled();
                }
            });
            dialog.show();
        }

        /**
         * Returns the dialog.
         *
         * @return the dialog, or {@code null} if it hasn't been displayed
         */
        public CheckInDialog getCheckInDialog() {
            return dialog;
        }

        /**
         * Generate documents from a list of templates, and queue tasks to print them.
         *
         * @param templates the templates
         * @param context   the context
         */
        private void addPrintTasks(Collection<Entity> templates, TaskContext context) {
            CustomerMailContext mailContext = new CustomerMailContext(context, context.getHelpContext());
            for (Entity template : templates) {
                IMObjectBean templateBean = new IMObjectBean(template);
                Act document = (Act) ServiceHelper.getArchetypeService().create(templateBean.getString("archetype"));
                IMObjectBean bean = new IMObjectBean(document);
                bean.setTarget("patient", context.getPatient());
                bean.setTarget("documentTemplate", template);
                bean.setTarget("clinician", context.getClinician());
                addTask(new PrintPatientActTask(document, mailContext, PrintIMObjectTask.PrintMode.BACKGROUND));
            }
        }
    }

    /**
     * Task to create a flow sheet.
     */
    private class CreateFlowSheetTask extends AbstractTask {

        private final FlowSheetInfo info;

        /**
         * Creates a new {@link CreateFlowSheetTask}.
         */
        CreateFlowSheetTask(FlowSheetInfo info) {
            this.info = info;
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
            Act visit = (Act) context.getObject(PatientArchetypes.CLINICAL_EVENT);
            Party patient = context.getPatient();
            Party location = context.getLocation();
            if (visit != null && location != null) {
                PatientContextFactory factory = ServiceHelper.getBean(PatientContextFactory.class);
                PatientContext patientContext = factory.createContext(patient, visit, location);
                try {
                    HospitalizationService service = flowSheetServiceFactory.getHospitalizationService(location);
                    service.add(patientContext, info.getExpectedStay(), info.getDepartmentId(), info.getTemplate());
                } catch (Throwable exception) {
                    popup = true;
                    ErrorHelper.show(exception, new WindowPaneListener() {
                        public void onClose(WindowPaneEvent event) {
                            notifyCompleted();
                        }
                    });
                }
            }
            if (!popup) {
                notifyCompleted();
            }
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
        UpdateAppointmentTask(IMObject object, Date arrivalTime, TaskProperties properties) {
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
            IMObjectBean bean = new IMObjectBean(object);
            bean.setValue("arrivalTime", arrivalTime);
            Act task = context.getTask();
            if (task != null) {
                bean.setTarget("tasks", task);
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
