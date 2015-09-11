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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.checkin;

import org.openvpms.archetype.rules.math.Weight;
import org.openvpms.archetype.rules.patient.MedicalRecordRules;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.hl7.patient.PatientContext;
import org.openvpms.hl7.patient.PatientContextFactory;
import org.openvpms.smartflow.client.FlowSheetServiceFactory;
import org.openvpms.smartflow.client.HospitalizationService;
import org.openvpms.web.component.workflow.InformationTask;
import org.openvpms.web.component.workflow.SynchronousTask;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.component.workflow.Tasks;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

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
     * The patient context.
     */
    private PatientContext patientContext;

    /**
     * The patient visit.
     */
    private Act visit;

    /**
     * If {@code false} don't display a note if a hospitalisation already exists.
     */
    private final boolean ignoreExisting;

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
     * @param location       the practice location
     * @param ignoreExisting if {@code true}, ignore any existing hospitalisation, otherwise display a note
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
                client = factory.getHospitalisationService(location);
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

    private class AddFlowSheet extends SynchronousTask {

        /**
         * Executes the task.
         *
         * @param context the task context
         * @throws OpenVPMSException for any error
         */
        @Override
        public void execute(TaskContext context) {
            if (patientContext == null) {
                patientContext = getPatientContext(visit);
            }
            Weight weight = patientContext.getWeight();
            if (weight == null || weight.isZero()) {
                // no weight entered
                notifyCancelled();
            } else {
                client.add(patientContext);
            }
        }
    }
}
