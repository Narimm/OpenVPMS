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
import org.openvpms.archetype.rules.math.Weight;
import org.openvpms.archetype.rules.patient.MedicalRecordRules;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.workflow.AppointmentRules;
import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.hl7.patient.PatientContext;
import org.openvpms.hl7.patient.PatientContextFactory;
import org.openvpms.smartflow.client.FlowSheetException;
import org.openvpms.smartflow.client.FlowSheetServiceFactory;
import org.openvpms.smartflow.client.HospitalizationService;
import org.openvpms.web.component.workflow.AbstractTask;
import org.openvpms.web.component.workflow.InformationTask;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.component.workflow.Tasks;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.error.ErrorHandler;
import org.openvpms.web.echo.event.WindowPaneListener;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.patient.visit.FlowSheetEditDialog;

/**
 * Creates a new flow sheet.
 *
 * @author Tim Anderson
 */
public class NewFlowSheetTask extends Tasks {

    /**
     * The appointment/task.
     */
    private final Act act;

    /**
     * The customer.
     */
    private final Party customer;

    /**
     * The patient.
     */
    private final Party patient;

    /**
     * The clinician.
     */
    private final User clinician;

    /**
     * The practice location.
     */
    private final Party location;

    /**
     * The flow sheet service factory.
     */
    private final FlowSheetServiceFactory factory;

    /**
     * If {@code false} don't display a note if a hospitalisation already exists.
     */
    private final boolean ignoreExisting;

    /**
     * The patient context.
     */
    private PatientContext patientContext;

    /**
     * The patient visit.
     */
    private Act visit;

    /**
     * The hospitalisation service.
     */
    private HospitalizationService client;

    /**
     * The medical record rules.
     */
    private MedicalRecordRules rules;

    /**
     * The patient context factory.
     */
    private PatientContextFactory contextFactory;

    /**
     * Constructs a {@link NewFlowSheetTask}.
     *
     * @param act      the appointment/task
     * @param location the practice location
     * @param help     the help context
     */
    public NewFlowSheetTask(Act act, Party location, FlowSheetServiceFactory factory, HelpContext help) {
        this(act, null, false, location, factory, help);
    }

    /**
     * Constructs a {@link NewFlowSheetTask}.
     *
     * @param act            the appointment/task
     * @param visit          the patient visit. May be {@code null}
     * @param ignoreExisting if {@code true}, ignore any existing hospitalisation, otherwise display a note
     * @param location       the practice location
     * @param factory        the FlowSheet service factory
     * @param help           the help context
     */
    public NewFlowSheetTask(Act act, Act visit, boolean ignoreExisting, Party location, FlowSheetServiceFactory factory,
                            HelpContext help) {
        super(help);
        this.act = act;
        this.visit = visit;
        this.ignoreExisting = ignoreExisting;
        ActBean bean = new ActBean(act);
        customer = (Party) bean.getNodeParticipant("customer");
        patient = (Party) bean.getNodeParticipant("patient");
        clinician = (User) bean.getNodeParticipant("clinician");
        this.location = location;
        this.factory = factory;
        rules = ServiceHelper.getBean(MedicalRecordRules.class);
        contextFactory = ServiceHelper.getBean(PatientContextFactory.class);
    }

    /**
     * Initialise any tasks.
     *
     * @param context the task context
     */
    @Override
    protected void initialise(TaskContext context) {
        if (customer != null && patient != null && location != null) {
            if (visit == null) {
                visit = getVisit();
            }
            if (!visit.isNew()) {
                context.setCustomer(customer);
                context.setLocation(location);
                context.setPatient(patient);
                context.addObject(visit);
                client = factory.getHospitalizationService(location);
                patientContext = getPatientContext(visit);
                if (!client.exists(patientContext)) {
                    Weight weight = patientContext.getWeight();
                    if (weight == null || weight.isZero() || DateRules.compareDateToToday(weight.getDate()) != 0) {
                        addTask(new PatientWeightTask(weight, context.getHelpContext()));
                        patientContext = null; // need to recreate to get the latest weight
                    }
                    addTask(new AddFlowSheet());
                    addTask(new InformationTask(Messages.format("workflow.flowsheet.created", patient.getName())));
                } else if (!ignoreExisting) {
                    addTask(new InformationTask(Messages.format("workflow.flowsheet.exists", patient.getName())));
                }
            } else {
                addTask(new InformationTask(Messages.format("workflow.flowsheet.novisit", patient.getName())));
            }
        } else {
            notifyCancelled();
        }
    }

    /**
     * Returns a patient visit around the time of the appointment/task.
     *
     * @return a visit. A new one will be created if none exists
     */
    private Act getVisit() {
        return rules.getEventForAddition(patient, act.getActivityStartTime(), null);
    }

    /**
     * Creates a patient context.
     *
     * @param visit the patient visit
     * @return a new patient context
     */
    private PatientContext getPatientContext(Act visit) {
        return contextFactory.createContext(patient, customer, visit, location, clinician);
    }

    private class AddFlowSheet extends AbstractTask {

        /**
         * The appointment rules.
         */
        private final AppointmentRules rules;

        /**
         * Constructs an {@link AddFlowSheet}.
         */
        public AddFlowSheet() {
            rules = ServiceHelper.getBean(AppointmentRules.class);
        }

        /**
         * Starts the task.
         *
         * @param context the task context
         * @throws OpenVPMSException for any error
         */
        @Override
        public void start(TaskContext context) {
            if (patientContext == null) {
                patientContext = getPatientContext(visit);
            }
            Weight weight = patientContext.getWeight();
            if (weight == null || weight.isZero()) {
                // no weight entered
                notifyCancelled();
            } else {
                final FlowSheetEditDialog dialog = new FlowSheetEditDialog(factory, location, -1, null, getDays(),
                                                                           false);
                dialog.addWindowPaneListener(new PopupDialogListener() {
                    @Override
                    public void onOK() {
                        int days = dialog.getExpectedStay();
                        int departmentId = dialog.getDepartmentId();
                        String template = dialog.getTemplate();
                        try {
                            client.add(patientContext, days, departmentId, template);
                            notifyCompleted();
                        } catch (FlowSheetException exception) {
                            // want to display the SFS exception, not the root cause
                            ErrorHandler.getInstance().error(exception.getMessage(), exception,
                                                             new WindowPaneListener() {
                                                                 @Override
                                                                 public void onClose(WindowPaneEvent event) {
                                                                     notifyCancelled();
                                                                 }
                                                             });
                        } catch (Exception exception) {
                            notifyCancelledOnError(exception);
                        }
                    }

                    @Override
                    public void onAction(String action) {
                        notifyCancelled();
                    }
                });
                dialog.show();
            }
        }

        /**
         * Returns the estimated no. of days stay, based on the current appointment.
         *
         * @return the estimated days stay
         */
        protected int getDays() {
            int days = 1;
            Act appointment = getAppointment();
            if (appointment != null) {
                days = rules.getBoardingDays(appointment);
            }
            return days;
        }

        /**
         * Returns the current appointment.
         *
         * @return the current appointment. May be {@code null}
         */
        private Act getAppointment() {
            Act appointment = TypeHelper.isA(act, ScheduleArchetypes.APPOINTMENT) ? act : null;
            if (appointment == null) {
                ActBean bean = new ActBean(act); // its a task
                appointment = (Act) bean.getNodeSourceObject("appointments");
            }
            if (appointment == null) {
                appointment = rules.getActiveAppointment(patient);
            }
            return appointment;
        }
    }
}
