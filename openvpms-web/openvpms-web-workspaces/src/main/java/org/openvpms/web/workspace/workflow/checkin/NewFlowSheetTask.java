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
     * @param act  the appointment/task
     * @param help the help context
     */
    public NewFlowSheetTask(Act act, Party location, FlowSheetServiceFactory factory, HelpContext help) {
        super(help);
        this.act = act;
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
            visit = getVisit();
            if (!visit.isNew()) {
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
                } else {
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
