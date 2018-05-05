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

package org.openvpms.web.workspace.workflow.checkout;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.finance.account.CustomerAccountRules;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.workflow.AppointmentRules;
import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.hl7.patient.PatientContext;
import org.openvpms.hl7.patient.PatientContextFactory;
import org.openvpms.hl7.patient.PatientInformationService;
import org.openvpms.smartflow.client.FlowSheetServiceFactory;
import org.openvpms.smartflow.client.HospitalizationService;
import org.openvpms.smartflow.event.SmartFlowSheetEventService;
import org.openvpms.smartflow.model.Hospitalization;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.ContextException;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.act.ActEditor;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.im.util.UserHelper;
import org.openvpms.web.component.retry.AbstractRetryable;
import org.openvpms.web.component.retry.Retryable;
import org.openvpms.web.component.retry.Retryer;
import org.openvpms.web.component.workflow.AbstractConfirmationTask;
import org.openvpms.web.component.workflow.AbstractTask;
import org.openvpms.web.component.workflow.ConditionalCreateTask;
import org.openvpms.web.component.workflow.ConditionalTask;
import org.openvpms.web.component.workflow.ConfirmationTask;
import org.openvpms.web.component.workflow.DefaultTaskContext;
import org.openvpms.web.component.workflow.EditIMObjectTask;
import org.openvpms.web.component.workflow.EvalTask;
import org.openvpms.web.component.workflow.ReloadTask;
import org.openvpms.web.component.workflow.SynchronousTask;
import org.openvpms.web.component.workflow.Task;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.component.workflow.TaskListener;
import org.openvpms.web.component.workflow.TaskProperties;
import org.openvpms.web.component.workflow.Tasks;
import org.openvpms.web.component.workflow.UpdateIMObjectTask;
import org.openvpms.web.component.workflow.Variable;
import org.openvpms.web.component.workflow.WorkflowImpl;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.dialog.ErrorDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.customer.charge.UndispensedOrderChecker;
import org.openvpms.web.workspace.customer.charge.UndispensedOrderDialog;
import org.openvpms.web.workspace.workflow.GetClinicalEventTask;
import org.openvpms.web.workspace.workflow.GetInvoiceTask;
import org.openvpms.web.workspace.workflow.payment.PaymentWorkflow;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.openvpms.web.component.workflow.TaskFactory.and;
import static org.openvpms.web.component.workflow.TaskFactory.eq;
import static org.openvpms.web.component.workflow.TaskFactory.ne;
import static org.openvpms.web.component.workflow.TaskFactory.when;


/**
 * Check-out workflow.
 *
 * @author Tim Anderson
 */
public class CheckOutWorkflow extends WorkflowImpl {

    /**
     * The external context to access and update.
     */
    private final Context external;

    /**
     * The collected events.
     */
    private final Visits visits;

    /**
     * The initial context.
     */
    private TaskContext initial;

    /**
     * The appointment rules.
     */
    private AppointmentRules appointmentRules;

    /**
     * The flow sheet service factory.
     */
    private FlowSheetServiceFactory flowSheetServiceFactory;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(CheckOutWorkflow.class);


    /**
     * Constructs a {@code CheckOutWorkflow} from an <em>act.customerAppointment</em> or <em>act.customerTask</em>.
     *
     * @param act     the act
     * @param context the external context to access and update
     * @param help    the help context
     */
    public CheckOutWorkflow(Act act, Context context, HelpContext help) {
        super(help.topic("workflow/checkout"));
        if (context.getPractice() == null) {
            throw new IllegalStateException("Context has no practice");
        }
        external = context;
        appointmentRules = ServiceHelper.getBean(AppointmentRules.class);
        visits = new Visits(context.getCustomer(), appointmentRules, ServiceHelper.getBean(PatientRules.class));
        flowSheetServiceFactory = ServiceHelper.getBean(FlowSheetServiceFactory.class);

        initialise(act, getHelpContext());
    }

    /**
     * Starts the workflow.
     */
    @Override
    public void start() {
        super.start(initial);
    }

    /**
     * Returns the events.
     *
     * @return the events
     */
    public Visits getVisits() {
        return visits;
    }

    /**
     * Creates a new task to performing charging.
     *
     * @param visits the events
     * @return a new task
     */
    protected CheckoutEditInvoiceTask createChargeTask(Visits visits) {
        return new CheckoutEditInvoiceTask(visits);
    }

    /**
     * Creates a new payment workflow.
     *
     * @param context the context
     * @return a new payment workflow
     */
    protected PaymentWorkflow createPaymentWorkflow(TaskContext context) {
        return new PaymentWorkflow(context, external, context.getHelpContext().subtopic("pay"));
    }

    /**
     * Returns a condition task to determine if the invoice should be posted.
     *
     * @return a new condition
     */
    protected EvalTask<Boolean> getPostCondition() {
        String invoiceTitle = Messages.get("workflow.checkout.postinvoice.title");
        String invoiceMsg = Messages.get("workflow.checkout.postinvoice.message");
        return new ConfirmationTask(invoiceTitle, invoiceMsg, getHelpContext().subtopic("post"));
    }

    /**
     * Initialise the workflow.
     *
     * @param act  the act
     * @param help the help context
     */
    private void initialise(Act act, HelpContext help) {
        ActBean bean = new ActBean(act);
        Party customer = (Party) bean.getNodeParticipant("customer");
        Party patient = (Party) bean.getNodeParticipant("patient");

        if (customer == null) {
            throw new ContextException(ContextException.ErrorCode.NoCustomer);
        }
        if (patient == null) {
            throw new ContextException(ContextException.ErrorCode.NoPatient);
        }

        User clinician;
        if (UserHelper.useLoggedInClinician(external)) {
            clinician = external.getUser();
        } else {
            clinician = (User) bean.getNodeParticipant("clinician");
            if (clinician == null) {
                clinician = external.getClinician();
            }
        }

        initial = new DefaultTaskContext(help);
        initial.addObject(act);
        initial.setCustomer(customer);
        initial.setPatient(patient);
        initial.setClinician(clinician);

        initial.setUser(external.getUser());
        initial.setPractice(external.getPractice());
        initial.setLocation(external.getLocation());

        final Act appointment = getAppointment(act, patient);
        if (appointment != null && appointmentRules.isBoardingAppointment(appointment)) {
            addTask(new GetBoardingAppointmentsTask(appointment, visits));
        } else {
            // add the most recent clinical event to the context
            addTask(new GetClinicalEventTask(act.getActivityStartTime()));

            // copy the event to Events
            addTask(new SynchronousTask() {
                @Override
                public void execute(TaskContext context) {
                    Act event = (Act) context.getObject(PatientArchetypes.CLINICAL_EVENT);
                    if (event == null) {
                        throw new ContextException(ContextException.ErrorCode.NoClinicalEvent);
                    }
                    visits.add(event, appointment);
                }
            });
        }

        if (flowSheetServiceFactory.isSmartFlowSheetEnabled(initial.getLocation())) {
            addTask(new BatchDischargeFromSmartFlowSheetTask(visits, help));
        }

        addTask(new GetInvoiceTask());
        addTask(new ConditionalCreateTask(CustomerAccountArchetypes.INVOICE));
        addTask(createChargeTask(visits));

        // on save, determine if the user wants to post the invoice, but only if its not already posted
        addTask(when(ne(CustomerAccountArchetypes.INVOICE, "status", ActStatus.POSTED), getPostTask()));

        // if the invoice is posted and the customer has a positive balance, prompt to pay the account
        PaymentWorkflow payWorkflow = createPaymentWorkflow(initial);
        payWorkflow.setRequired(false);
        addTask(when(and(eq(CustomerAccountArchetypes.INVOICE, "status", ActStatus.POSTED), new PositiveBalance()),
                     payWorkflow));

        // print acts and documents created since the events or invoice was created
        addTask(new PrintTask(visits, help.subtopic("print")));

        // add a follow-up task, if it is configured for the practice location
        if (followUp()) {
            addTask(new FollowUpTask(help));
        }

        // update the appointments and events setting their status to COMPLETED and endTime to now.
        // Use a retryable task to handle concurrent update conflicts.
        // If the invoice was posted, the events should already be completed.
        addTask(new UpdateEventTask(visits));
        addTask(new DischargeTask(visits));

        // update the act status if it is a task
        if (TypeHelper.isA(act, ScheduleArchetypes.TASK)) {
            TaskProperties appProps = new TaskProperties();
            appProps.add("status", ActStatus.COMPLETED);
            appProps.add(new Variable("endTime") {
                public Object getValue(TaskContext context) {
                    return new Date();
                }
            });
            String shortName = act.getArchetypeId().getShortName();
            addTask(new ReloadTask(shortName));
            addTask(new UpdateIMObjectTask(shortName, appProps));
        }

        addTask(new InsuranceClaimTask(help));

        // add a task to update the context at the end of the workflow
        addTask(new SynchronousTask() {
            public void execute(TaskContext context) {
                external.setCustomer(context.getCustomer());
                external.setPatient(context.getPatient());
                external.setTill(context.getTill());
                external.setClinician(context.getClinician());
            }
        });
    }

    /**
     * Returns the appointment associated with an act, falling back to the active appointment if none exists.
     *
     * @param act     the act
     * @param patient the patient
     * @return the appointment, or {@code null} if none exists
     */
    private Act getAppointment(Act act, Party patient) {
        Act result;
        if (TypeHelper.isA(act, ScheduleArchetypes.APPOINTMENT)) {
            result = act;
        } else {
            ActBean bean = new ActBean(act);
            result = (Act) bean.getNodeSourceObject("appointments");
            if (result == null) {
                result = ServiceHelper.getBean(AppointmentRules.class).getActiveAppointment(patient);
            }
        }
        return result;
    }

    /**
     * Returns a task to post the invoice.
     * <p>
     * This first confirms that the invoice should be posted, and then checks if there are any undispensed orders.
     * If so, displays a confirmation dialog before posting.
     *
     * @return a task to post the invoice
     */
    private Task getPostTask() {
        HelpContext help = getHelpContext().subtopic("post");
        Tasks postTasks = new Tasks(help);
        postTasks.addTask(new PostInvoiceTask());
        postTasks.setRequired(false);

        EvalTask<Boolean> confirmPost = getPostCondition();
        EvalTask<Boolean> checkUndispensed = new UndispensedOrderTask(help);
        ConditionalTask post = new ConditionalTask(confirmPost, when(checkUndispensed, postTasks));
        post.setRequired(false);
        return post;
    }

    /**
     * Determines if a follow-up task should be created.
     *
     * @return {@code true} if a follow-up task should be created
     */
    private boolean followUp() {
        boolean result = false;
        Party practice = external.getPractice();
        if (practice != null) {
            IMObjectBean bean = new IMObjectBean(practice);
            result = bean.getBoolean("followUpAtCheckOut");
        }
        return result;
    }

    /**
     * Task to import flow sheet reports for a patient, if a patient has a Smart Flow Sheet hospitalisation.
     */
    private static class DischargeFromSmartFlowSheetTask extends AbstractTask {

        private final PatientContext patientContext;

        private final HospitalizationService service;

        public DischargeFromSmartFlowSheetTask(PatientContext context, HospitalizationService service) {
            this.patientContext = context;
            this.service = service;
        }

        /**
         * Starts the task.
         * <p>
         * The registered {@link TaskListener} will be notified on completion or failure.
         *
         * @param context the task context
         * @throws OpenVPMSException for any error
         */
        @Override
        public void start(final TaskContext context) {
            SmartFlowSheetEventService eventService = ServiceHelper.getBean(SmartFlowSheetEventService.class);
            try {
                service.discharge(patientContext.getPatient(), patientContext.getVisit());
                eventService.poll();                    // wake up the event service if it is inactive
                notifyCompleted();
            } catch (Exception exception) {
                log.error(exception, exception);
                String title = Messages.get("workflow.checkout.flowsheet.discharge.title");
                String message = Messages.format("workflow.checkout.flowsheet.discharge.error", exception.getMessage());
                PopupDialogListener listener = new PopupDialogListener() {
                    @Override
                    public void onYes() {
                        start(context);
                    }

                    /**
                     * Invoked when the 'no' button is pressed.
                     * <p/>
                     * If not overridden in subclasses, delegates to {@link #onAction(String)}.
                     */
                    @Override
                    public void onNo() {
                        notifyCompleted();
                    }

                    @Override
                    public void onCancel() {
                        notifyCancelled();
                    }
                };
                ErrorDialog.show(title, message, ErrorDialog.YES_NO_CANCEL, context.getHelpContext(), listener);
            }
        }
    }

    /**
     * Updates the patient clinical event, if it is not already completed.
     */
    private static class UpdateEventTask extends SynchronousTask {

        /**
         * The events to update.
         */
        private final Visits visits;

        /**
         * Constructs an {@link UpdateEventTask}.
         */
        public UpdateEventTask(Visits visits) {
            this.visits = visits;
        }

        /**
         * Executes the task.
         *
         * @param context the context
         * @throws OpenVPMSException for any error
         */
        @Override
        public void execute(TaskContext context) {
            for (final Visit event : visits) {
                Retryable action = new AbstractRetryable() {
                    @Override
                    protected boolean runFirst() {
                        return update(true, event);
                    }

                    @Override
                    protected boolean runAction() {
                        return update(false, event);
                    }
                };
                if (!Retryer.run(action)) {
                    notifyCancelled();
                }
            }
        }

        /**
         * Updates an event.
         *
         * @param first  if {@code true} this is the first time it is being updated.
         * @param event  the event
         * @param toSave collects objects to save
         * @return {@code true} if the object exists, otherwise {@code false}
         */
        protected boolean updateEvent(boolean first, Act event, List<Act> toSave) {
            if (!first) {
                event = IMObjectHelper.reload(event);
            }
            if (event != null) {
                if (ActStatus.IN_PROGRESS.equals(event.getStatus())) {
                    event.setStatus(ActStatus.COMPLETED);
                    event.setActivityEndTime(new Date());
                    toSave.add(event);
                }
            }
            return event != null;
        }

        /**
         * Updates an object and saves it.
         *
         * @param first if {@code true}, this is the first invocation
         * @param event the event
         * @return {@code true} if the object exists, otherwise {@code false}
         */
        private boolean update(boolean first, Visit event) {
            List<Act> toSave = new ArrayList<>();
            boolean result = updateAppointment(first, event.getAppointment(), toSave);
            result |= updateEvent(first, event.getEvent(), toSave);
            if (!toSave.isEmpty()) {
                ServiceHelper.getArchetypeService().save(toSave);
            }
            return result;
        }

        /**
         * Updates an appointment.
         *
         * @param first       if {@code true} this is the first time it is being updated.
         * @param appointment the appointment
         * @param toSave      collects objects to save
         * @return {@code true} if the object exists, otherwise {@code false}
         */
        private boolean updateAppointment(boolean first, Act appointment, List<Act> toSave) {
            if (!first) {
                appointment = IMObjectHelper.reload(appointment);
            }
            if (appointment != null && !ActStatus.COMPLETED.equals(appointment.getStatus())) {
                appointment.setStatus(ActStatus.COMPLETED);
                toSave.add(appointment);
            }
            return appointment != null;
        }
    }

    /**
     * Task to post an invoice.
     * <p>
     * This uses an editor to ensure that any HL7 Pharmacy Orders associated with the invoice are discontinued.
     * This is workaround for Cubex.
     */
    private class PostInvoiceTask extends EditIMObjectTask {

        /**
         * Constructs a {@link PostInvoiceTask}.
         */
        public PostInvoiceTask() {
            super(CustomerAccountArchetypes.INVOICE, false, false);
            setShowEditorOnError(false);
        }

        /**
         * Edits an object in the background.
         *
         * @param editor  the editor
         * @param context the task context
         */
        @Override
        protected void edit(IMObjectEditor editor, TaskContext context) {
            ActEditor actEditor = (ActEditor) editor;
            actEditor.setStatus(ActStatus.POSTED);
            actEditor.setStartTime(new Date()); // for OVPMS-734 - TODO
        }
    }

    /**
     * Displays a warning dialog if the invoice is to be finalised, but there are undispensed orders.
     */
    private class UndispensedOrderTask extends AbstractConfirmationTask {

        /**
         * The undispensed order checker.
         */
        private UndispensedOrderChecker checker;

        /**
         * Constructs an {@link UndispensedOrderTask}.
         *
         * @param help the help context
         */
        public UndispensedOrderTask(HelpContext help) {
            super(help);
        }

        /**
         * Starts the task.
         * <p>
         * The registered {@link TaskListener} will be notified on completion or failure.
         *
         * @param context the task context
         */
        @Override
        public void start(TaskContext context) {
            Act invoice = (Act) context.getObject(CustomerAccountArchetypes.INVOICE);
            if (invoice != null) {
                checker = new UndispensedOrderChecker(invoice);
                if (checker.hasUndispensedItems()) {
                    // display the confirmation dialog
                    super.start(context);
                } else {
                    setValue(true);
                }
            } else {
                setValue(false);
            }
        }

        /**
         * Creates a new confirmation dialog.
         *
         * @param context the context
         * @param help    the help context
         * @return a new confirmation dialog
         */
        @Override
        protected ConfirmationDialog createConfirmationDialog(TaskContext context, HelpContext help) {
            return new UndispensedOrderDialog(checker.getUndispensedItems(), help);
        }
    }

    private class PositiveBalance extends EvalTask<Boolean> {

        /**
         * Starts the task.
         * <p>
         * The registered {@link TaskListener} will be notified on completion or failure.
         *
         * @param context the task context
         * @throws OpenVPMSException for any error
         */
        @Override
        public void start(TaskContext context) {
            Party customer = context.getCustomer();
            CustomerAccountRules rules = ServiceHelper.getBean(CustomerAccountRules.class);
            BigDecimal balance = rules.getBalance(customer);
            if (balance.compareTo(BigDecimal.ZERO) > 0) {
                setValue(true);
            } else {
                setValue(false);
            }
        }
    }

    /**
     * Prints all unprinted acts and documents for the customer and patient.
     * This uses the minimum startTime of the <em>act.patientClinicalEvent</em>,
     * <em>act.customerAccountChargesInvoice</em> and time now to select the
     * objects to print.
     */
    private class PrintTask extends Tasks {

        private final Visits visits;

        /**
         * Constructs a {@link PrintTask}.
         *
         * @param visits the events
         * @param help   the help context
         */
        public PrintTask(Visits visits, HelpContext help) {
            super(help);
            this.visits = visits;
        }

        /**
         * Initialise any tasks.
         *
         * @param context the task context
         */
        @Override
        protected void initialise(TaskContext context) {
            Date min = new Date();
            for (Visit event : visits) {
                min = getMin(min, event.getEvent().getActivityStartTime());
            }
            min = getMinStartTime(CustomerAccountArchetypes.INVOICE, min, context);
            min = DateRules.getDate(min);  // print all documents done on or after the min date
            PrintDocumentsTask printDocs = new PrintDocumentsTask(visits.getPatients(), min, context.getHelpContext());
            printDocs.setRequired(false);
            addTask(printDocs);
        }

        /**
         * Returns the minimum of two start times, one obtained from act
         * identified by short name in the task context, the other supplied.
         *
         * @param shortName the act short name
         * @param startTime the start time to compare with
         * @param context   the task context
         * @return the minimum of the two start times
         */
        private Date getMinStartTime(String shortName, Date startTime, TaskContext context) {
            Act act = (Act) context.getObject(shortName);
            if (act != null) {
                startTime = getMin(startTime, act.getActivityStartTime());
            }
            return startTime;
        }

        /**
         * Returns the minimum of two dates.
         *
         * @param date1 the first date
         * @param date2 the second date
         * @return the minimum of the two dates
         */
        private Date getMin(Date date1, Date date2) {
            Date min = date1;
            if (date1.getTime() > date2.getTime()) {
                min = date2;
            }
            return min;
        }
    }

    private class BatchDischargeFromSmartFlowSheetTask extends Tasks {

        private final Visits visits;

        public BatchDischargeFromSmartFlowSheetTask(Visits visits, HelpContext help) {
            super(help);
            setRequired(false);
            this.visits = visits;
        }

        /**
         * Initialise any tasks.
         *
         * @param context the task context
         */
        @Override
        protected void initialise(TaskContext context) {
            Party location = context.getLocation();
            boolean completed = true;
            if (location != null && !visits.isEmpty()) {
                PatientContextFactory factory = ServiceHelper.getBean(PatientContextFactory.class);
                HospitalizationService service = flowSheetServiceFactory.getHospitalizationService(location);
                for (Visit event : visits) {
                    Act act = event.getEvent();
                    PatientContext patientContext = factory.createContext(act, location);
                    Hospitalization hospitalization = service.getHospitalization(patientContext);
                    if (hospitalization != null && Hospitalization.ACTIVE_STATUS.equals(hospitalization.getStatus())) {
                        completed = false;
                        String title = Messages.get("workflow.checkout.flowsheet.discharge.title");
                        String message = Messages.format("workflow.checkout.flowsheet.discharge.message",
                                                         patientContext.getPatientFirstName());
                        ConfirmationTask confirm = new ConfirmationTask(title, message, true, getHelpContext());
                        DischargeFromSmartFlowSheetTask discharge
                                = new DischargeFromSmartFlowSheetTask(patientContext, service);
                        addTask(new ConditionalTask(confirm, discharge));
                    }
                }
            }
            if (completed) {
                notifyCompleted();
            }
        }
    }

    private class DischargeTask extends SynchronousTask {

        private final Visits visits;

        public DischargeTask(Visits visits) {
            this.visits = visits;
        }

        /**
         * Executes the task.
         *
         * @throws OpenVPMSException for any error
         */
        @Override
        public void execute(TaskContext context) {
            PatientContextFactory factory = ServiceHelper.getBean(PatientContextFactory.class);
            PatientInformationService service = ServiceHelper.getBean(PatientInformationService.class);
            for (Visit event : visits) {
                Act act = event.getEvent();
                PatientContext pc = factory.createContext(event.getPatient(), context.getCustomer(), act,
                                                          context.getLocation(), context.getClinician());
                service.discharged(pc, context.getUser());
            }
        }
    }

}
