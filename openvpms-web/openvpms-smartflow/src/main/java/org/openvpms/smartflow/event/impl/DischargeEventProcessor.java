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
import org.openvpms.archetype.rules.doc.DocumentHandler;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.doc.DocumentRules;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.smartflow.client.FlowSheetException;
import org.openvpms.smartflow.client.MediaTypeHelper;
import org.openvpms.smartflow.i18n.FlowSheetMessages;
import org.openvpms.smartflow.model.Hospitalization;
import org.openvpms.smartflow.model.HospitalizationList;
import org.openvpms.smartflow.model.Patient;
import org.openvpms.smartflow.model.event.DischargeEvent;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.openvpms.smartflow.client.MediaTypeHelper.APPLICATION_PDF;

/**
 * Processes {@link DischargeEvent} events.
 *
 * @author Tim Anderson
 */
public class DischargeEventProcessor extends EventProcessor<DischargeEvent> {

    /**
     * The document handlers.
     */
    private final DocumentHandlers handlers;

    /**
     * The document rules.
     */
    private final DocumentRules rules;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(DischargeEventProcessor.class);

    /**
     * Constructs an {@link DischargeEventProcessor}.
     *
     * @param service  the archetype service
     * @param handlers the document handlers
     */
    public DischargeEventProcessor(IArchetypeService service, DocumentHandlers handlers) {
        super(service);
        this.handlers = handlers;
        rules = new DocumentRules(service);
    }

    /**
     * Processes an event.
     *
     * @param event the event
     */
    @Override
    public void process(DischargeEvent event) {
        HospitalizationList list = event.getObject();
        for (Hospitalization hospitalization : list.getHospitalizations()) {
            discharged(hospitalization);
        }
    }

    /**
     * Invoked when a patient is discharged.
     *
     * @param hospitalization the hospitalization
     */
    protected void discharged(Hospitalization hospitalization) {
        String reportPath = hospitalization.getReportPath();
        if (!StringUtils.isEmpty(reportPath)) {
            IArchetypeService service = getService();
            Act visit = getVisit(hospitalization);
            Party patient = getPatient(hospitalization);
            User clinician = getClinician(hospitalization);
            Client client = ClientBuilder.newClient();
            WebTarget target = client.target(reportPath);
            Response response = target.request("application/pdf").get();
            if (response.hasEntity() && MediaTypeHelper.isA(response.getMediaType(),
                                                            MediaTypeHelper.APPLICATION_PDF_TYPE,
                                                            MediaType.APPLICATION_OCTET_STREAM_TYPE)) {
                try (InputStream stream = (InputStream) response.getEntity()) {
                    String fileName = "Flow Sheet.pdf";
                    DocumentHandler documentHandler = handlers.find(fileName, APPLICATION_PDF);
                    Document document = documentHandler.create(fileName, stream, APPLICATION_PDF, -1);
                    DocumentAct act = (DocumentAct) service.create(PatientArchetypes.DOCUMENT_ATTACHMENT);
                    ActBean bean = new ActBean(act, service);
                    ActBean visitBean = new ActBean(visit, service);
                    visitBean.addNodeRelationship("items", act);
                    bean.addNodeParticipation("patient", patient);
                    if (clinician != null) {
                        bean.addNodeParticipation("clinician", clinician);
                    }
                    List<IMObject> objects = rules.addDocument(act, document);
                    objects.add(act);
                    service.save(objects);
                } catch (IOException exception) {
                    throw new FlowSheetException(FlowSheetMessages.failedToDownloadPDF(patient, reportPath), exception);
                }
            } else {
                log.error("Failed to get " + reportPath + " for hospitalizationId="
                          + hospitalization.getHospitalizationId() + ", status="
                          + response.getStatus() + ", mediaType=" + response.getMediaType());
                throw new FlowSheetException(FlowSheetMessages.failedToDownloadPDF(patient, reportPath));
            }
        }
    }

    private Act getVisit(Hospitalization hospitalization) {
        String hospitalizationId = hospitalization.getHospitalizationId();
        Patient patient = hospitalization.getPatient();
        String name = (patient != null) ? patient.getName() : null;
        return getVisit(hospitalizationId, name);
    }

}
