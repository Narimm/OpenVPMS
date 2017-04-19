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
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.user.UserArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.smartflow.client.FlowSheetException;
import org.openvpms.smartflow.i18n.FlowSheetMessages;
import org.openvpms.smartflow.model.Hospitalization;
import org.openvpms.smartflow.model.Patient;
import org.openvpms.smartflow.model.event.Event;

/**
 * Smart Flow Sheet event processor.
 *
 * @author Tim Anderson
 */
public abstract class EventProcessor<T extends Event> {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Constructs an {@link EventProcessor}.
     *
     * @param service the archetype service
     */
    public EventProcessor(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Processes an event.
     *
     * @param event the event
     */
    public abstract void process(T event);


    /**
     * Returns the archetype service.
     *
     * @return the archetype service
     */
    protected IArchetypeService getService() {
        return service;
    }

    /**
     * Returns a visit for a hospitalization identifier.
     *
     * @param hospitalizationId the hospitalization identifier
     * @param name              the patient name, for error reporting. May be {@code null}
     * @return the visit
     */
    protected Act getVisit(String hospitalizationId, String name) {
        Act result = (Act) getObject(hospitalizationId, PatientArchetypes.CLINICAL_EVENT);
        if (result == null) {
            throw new FlowSheetException(FlowSheetMessages.noVisitForHospitalization(hospitalizationId, name));
        }
        return result;
    }

    protected Party getPatient(Hospitalization hospitalization) {
        Patient patient = hospitalization.getPatient();
        if (patient == null) {
            throw new FlowSheetException(FlowSheetMessages.noPatientForHospitalization(
                    hospitalization.getHospitalizationId(), null, null));
        }
        Party result = (Party) getObject(patient.getPatientId(), PatientArchetypes.PATIENT);
        if (result == null) {
            throw new FlowSheetException(FlowSheetMessages.noPatientForHospitalization(
                    hospitalization.getHospitalizationId(), patient.getPatientId(), patient.getName()));

        }
        return result;
    }

    protected Party getPatient(Act visit) {
        ActBean bean = new ActBean(visit, service);
        return (Party) bean.getNodeParticipant("patient");
    }

    protected User getClinician(Hospitalization hospitalization) {
        return (User) getObject(hospitalization.getMedicId(), UserArchetypes.USER);
    }

    /**
     * Returns an object given its identifier.
     *
     * @param identifier the identifier. May be {@code null}
     * @param archetype  the archetype
     * @return the corresponding object, or {@code null} if none is found
     */
    protected IMObject getObject(String identifier, String archetype) {
        IMObject result = null;
        long id = getId(identifier);
        if (id != -1) {
            IMObjectReference reference = new IMObjectReference(archetype, Long.valueOf(identifier));
            result = service.get(reference);
        }
        return result;
    }

    /**
     * Helper to parse an id from a string.
     *
     * @param value the value to parse
     * @return the id, or {@code -1} if one doesn't exist or can't be parsed
     */
    protected long getId(String value) {
        long id = -1;
        if (!StringUtils.isEmpty(value)) {
            try {
                id = Long.valueOf(value);
            } catch (NumberFormatException ignore) {
                // do nothing
            }
        }
        return id;
    }

}
