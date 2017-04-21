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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
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
import org.openvpms.smartflow.client.FlowSheetServiceFactory;
import org.openvpms.smartflow.client.HospitalizationService;
import org.openvpms.smartflow.i18n.FlowSheetMessages;
import org.openvpms.smartflow.model.Hospitalization;
import org.openvpms.smartflow.model.HospitalizationList;
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
     * The logger.
     */
    private static final Log log = LogFactory.getLog(DischargeEventProcessor.class);

    /**
     * Constructs an {@link DischargeEventProcessor}.
     *
     * @param service the archetype service
     * @param factory the Smart Flow Sheet service factory
     */
    public DischargeEventProcessor(IArchetypeService service, FlowSheetServiceFactory factory) {
        super(service);
        this.factory = factory;
    }

    /**
     * Processes an event.
     *
     * @param event the event
     */
    @Override
    public void process(DischargeEvent event) {
        HospitalizationList list = event.getObject();
        String apiKey = event.getClinicApiKey();
        for (Hospitalization hospitalization : list.getHospitalizations()) {
            discharged(hospitalization, apiKey);
        }
    }

    /**
     * Invoked when a patient is discharged.
     *
     * @param hospitalization the hospitalization
     * @param apiKey          the clinic API key
     */
    protected void discharged(Hospitalization hospitalization, String apiKey) {
        String reportPath = hospitalization.getReportPath();
        if (!StringUtils.isEmpty(reportPath)) {
            Act visit = getVisit(hospitalization.getHospitalizationId());
            if (visit != null) {
                Party patient = getPatient(visit);
                if (patient != null) {
                    downloadFlowSheet(patient, visit, hospitalization, apiKey);
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
     */
    protected void downloadFlowSheet(Party patient, Act visit, Hospitalization hospitalization, String apiKey) {
        HospitalizationService hospitalizationService = factory.getHospitalizationService(apiKey);
        User clinician = getClinician(hospitalization);
        hospitalizationService.saveFlowSheetReport(FlowSheetMessages.flowSheetReportName(), patient, visit, clinician);
        hospitalizationService.saveMedicalRecordsReport(FlowSheetMessages.medicalRecordsReportName(), patient, visit,
                                                        clinician);
        hospitalizationService.saveBillingReport(FlowSheetMessages.billingReportName(), patient, visit,
                                                 clinician);
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
