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
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.smartflow.client.FlowSheetServiceFactory;
import org.openvpms.smartflow.client.HospitalizationService;
import org.openvpms.smartflow.model.Anesthetic;
import org.openvpms.smartflow.model.Anesthetics;
import org.openvpms.smartflow.model.event.AnestheticsEvent;

/**
 * Processes {@link AnestheticsEvent} events.
 *
 * @author Tim Anderson
 */
public class AnestheticsEventProcessor extends EventProcessor<AnestheticsEvent> {

    /**
     * The Smart Flow Sheet service factory.
     */
    private final FlowSheetServiceFactory factory;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(AnestheticsEventProcessor.class);

    /**
     * Constructs an {@link AnestheticsEventProcessor}.
     *
     * @param service the archetype service
     * @param factory the Smart Flow Sheet service factory
     */
    public AnestheticsEventProcessor(IArchetypeService service, FlowSheetServiceFactory factory) {
        super(service);
        this.factory = factory;
    }

    /**
     * Processes an event.
     *
     * @param event the event
     */
    @Override
    public void process(AnestheticsEvent event) {
        String apiKey = event.getClinicApiKey();
        HospitalizationService service = factory.getHospitalizationService(apiKey);
        Anesthetics anesthetics = event.getObject();
        for (Anesthetic anesthetic : anesthetics.getAnesthetics()) {
            finalised(anesthetic, service);
        }
    }

    /**
     * Invoked when a anesthetic sheet has been finalised.
     *
     * @param anesthetic the anesthetic sheet
     * @param service    the hospitalization service
     */
    protected void finalised(Anesthetic anesthetic, HospitalizationService service) {
        String reportPath = anesthetic.getReportPath();
        if (!StringUtils.isEmpty(reportPath)) {
            Act visit = getVisit(anesthetic.getHospitalizationId());
            if (visit != null) {
                Party patient = getPatient(visit);
                if (patient != null) {
                    service.saveAnestheticReports(patient, visit, anesthetic);
                }
            } else {
                log.error("No visit for anesthetic: " + toString(anesthetic));
            }
        } else {
            log.error("No reportPath for anesthetic: " + toString(anesthetic));
        }
    }

    /**
     * Helper to generate a string representation of a anesthetic, for error reporting purposes.
     *
     * @param anesthetic the anesthetic
     * @return a string version of the anesthetic
     */
    private String toString(Anesthetic anesthetic) {
        return "hospitalizationId=" + anesthetic.getHospitalizationId();
    }

}
