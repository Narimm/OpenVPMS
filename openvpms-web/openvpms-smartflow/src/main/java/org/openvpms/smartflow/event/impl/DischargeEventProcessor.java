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

package org.openvpms.smartflow.event.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.user.UserArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.smartflow.client.AccessToDocumentDeniedException;
import org.openvpms.smartflow.client.FlowSheetServiceFactory;
import org.openvpms.smartflow.client.HospitalizationService;
import org.openvpms.smartflow.model.Hospitalization;
import org.openvpms.smartflow.model.Hospitalizations;
import org.openvpms.smartflow.model.Patient;
import org.openvpms.smartflow.model.event.DischargeEvent;

/**
 * Processes {@link DischargeEvent} events.
 *
 * @author Tim Anderson
 */
public class DischargeEventProcessor extends EventProcessor<DischargeEvent> {

    /**
     * The Smart Flow Sheet service factory.
     */
    private final FlowSheetServiceFactory factory;

    /**
     * The configuration service.
     */
    private final FlowSheetConfigService configService;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(DischargeEventProcessor.class);

    /**
     * Constructs an {@link DischargeEventProcessor}.
     *
     * @param service the archetype service
     * @param factory the Smart Flow Sheet service factory
     */
    public DischargeEventProcessor(IArchetypeService service, FlowSheetServiceFactory factory,
                                   FlowSheetConfigService configService) {
        super(service);
        this.factory = factory;
        this.configService = configService;
    }

    /**
     * Processes an event.
     *
     * @param event the event
     */
    @Override
    public void process(DischargeEvent event) {
        Hospitalizations list = event.getObject();
        String apiKey = event.getClinicApiKey();
        FlowSheetConfig config = configService.getConfig();
        if (saveReportsOnDischarge(config)) {
            for (Hospitalization hospitalization : list.getHospitalizations()) {
                discharged(hospitalization, apiKey, config);
            }
        } else {
            log.debug("Saving reports on discharge is disabled");
        }
    }

    /**
     * Invoked when a patient is discharged.
     *
     * @param hospitalization the hospitalization
     * @param apiKey          the clinic API key
     * @param config          the Smart Flow Sheet configuration
     */
    protected void discharged(Hospitalization hospitalization, String apiKey, FlowSheetConfig config) {
        String reportPath = hospitalization.getReportPath();
        if (!StringUtils.isEmpty(reportPath)) {
            Act visit = getVisit(hospitalization.getHospitalizationId());
            if (visit != null) {
                Party patient = getPatient(visit);
                if (patient != null) {
                    saveReports(patient, visit, hospitalization, apiKey, config);
                }
            } else {
                log.error("No visit for hospitalization: " + toString(hospitalization));
            }
        } else {
            log.error("No reportPath for hospitalization: " + toString(hospitalization));
        }
    }

    /**
     * Downloads the reports for a patient, attaching it to the visit.
     *
     * @param patient         the patient
     * @param visit           the visit
     * @param hospitalization the hospitalization
     * @param apiKey          the clinic API key
     * @param config          the Smart Flow Sheet configuration
     */
    protected void saveReports(Party patient, Act visit, Hospitalization hospitalization, String apiKey,
                               FlowSheetConfig config) {
        HospitalizationService service = factory.getHospitalizationService(apiKey);
        User clinician = getClinician(hospitalization);
        if (config.isSaveFlowSheetReportOnDischarge()) {
            saveFlowSheet(patient, visit, clinician, service);
        }
        if (config.isSaveMedicalRecordsReportOnDischarge()) {
            saveMedicalRecords(patient, visit, clinician, service);
        }
        if (config.isSaveBillingReportOnDischarge()) {
            saveBillingReport(patient, visit, clinician, service);
        }
        if (config.isSaveNotesReportOnDischarge()) {
            saveNotesReport(patient, visit, clinician, service);
        }
        if (config.isSaveFormsReportsOnDischarge()) {
            saveForms(patient, visit, clinician, service);
        }
        if (config.isSaveAnestheticsReportsOnDischarge()) {
            saveAnestheticsReports(patient, visit, service);
        }
    }

    /**
     * Returns the clinician associated with a hospitalization.
     *
     * @param hospitalization the hospitalization
     * @return the clinician. May be {@code null}
     */
    protected User getClinician(Hospitalization hospitalization) {
        return (User) getObject(hospitalization.getMedicId(), UserArchetypes.USER);
    }

    /**
     * Determines if any reports are being saved on discharge.
     *
     * @param config the configuration
     * @return {@code true} if any reports are being saved on discharge
     */
    private boolean saveReportsOnDischarge(FlowSheetConfig config) {
        return config.isSaveFlowSheetReportOnDischarge()
               || config.isSaveMedicalRecordsReportOnDischarge()
               || config.isSaveBillingReportOnDischarge()
               || config.isSaveNotesReportOnDischarge()
               || config.isSaveFormsReportsOnDischarge()
               || config.isSaveAnestheticsReportsOnDischarge();
    }

    /**
     * Saves the Flow Sheet report for the patient visit.
     *
     * @param patient   the patient
     * @param visit     the visit
     * @param clinician the clinician to link to the report
     * @param service   the hospitalisation service
     */
    private void saveFlowSheet(Party patient, Act visit, User clinician, HospitalizationService service) {
        runProtected(patient, "flow sheet", () -> service.saveFlowSheetReport(patient, visit, clinician));
    }

    /**
     * Saves the Medical Records report for the patient visit.
     *
     * @param patient   the patient
     * @param visit     the visit
     * @param clinician the clinician to link to the report
     * @param service   the hospitalisation service
     */
    private void saveMedicalRecords(Party patient, Act visit, User clinician, HospitalizationService service) {
        runProtected(patient, "medical records", () -> service.saveMedicalRecordsReport(patient, visit, clinician));
    }

    /**
     * Saves the Billing report for the patient visit.
     *
     * @param patient   the patient
     * @param visit     the visit
     * @param clinician the clinician to link to the report
     * @param service   the hospitalisation service
     */
    private void saveBillingReport(Party patient, Act visit, User clinician, HospitalizationService service) {
        runProtected(patient, "billing report", () -> service.saveBillingReport(patient, visit, clinician));
    }

    /**
     * Saves the Notes report for the patient visit.
     *
     * @param patient   the patient
     * @param visit     the visit
     * @param clinician the clinician to link to the report
     * @param service   the hospitalisation service
     */
    private void saveNotesReport(Party patient, Act visit, User clinician, HospitalizationService service) {
        runProtected(patient, "notes report", () -> service.saveNotesReport(patient, visit, clinician));
    }

    /**
     * Saves the forms reports for the patient visit.
     *
     * @param patient   the patient
     * @param visit     the visit
     * @param clinician the clinician to link to the reports
     * @param service   the hospitalisation service
     */
    private void saveForms(Party patient, Act visit, User clinician, HospitalizationService service) {
        runProtected(patient, "forms report", () -> service.saveFormsReports(patient, visit, clinician));
    }

    /**
     * Saves the anesthetics reports for the patient visit.
     *
     * @param patient the patient
     * @param visit   the visit
     * @param service the hospitalisation service
     */
    private void saveAnestheticsReports(Party patient, Act visit, HospitalizationService service) {
        runProtected(patient, "anesthetics", () -> service.saveAnestheticsReports(patient, visit));
    }

    /**
     * Saves a report, swallowing any {@link AccessToDocumentDeniedException}.
     *
     * @param patient    the patient
     * @param reportName the report name
     * @param runnable   the code to save the report
     */
    private void runProtected(Party patient, String reportName, Runnable runnable) {
        try {
            runnable.run();
        } catch (AccessToDocumentDeniedException exception) {
            log.warn("Unable to save " + reportName + " for patient=[id=" + patient.getId() + ", name="
                     + patient.getName() + "]. Accesss is denied", exception);
        }
    }

    /**
     * Helper to generate a string representation of a hospitalization, for error reporting purposes.
     *
     * @param hospitalization the hospitalization
     * @return a string version of the hospitalization
     */
    private String toString(Hospitalization hospitalization) {
        Patient patient = hospitalization.getPatient();
        String patientId = (patient != null) ? patient.getPatientId() : null;
        String patientName = (patient != null) ? patient.getName() : null;
        return "hospitalizationId=" + hospitalization.getHospitalizationId()
               + ", patient=[id=" + patientId + ", name=" + patientName + "]";
    }

}
