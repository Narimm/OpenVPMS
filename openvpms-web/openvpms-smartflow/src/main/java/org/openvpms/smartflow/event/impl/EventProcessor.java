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

import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActIdentity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.smartflow.model.event.Event;

/**
 * Smart Flow Sheet event processor.
 *
 * @author Tim Anderson
 */
public abstract class EventProcessor<T extends Event> {

    /**
     * Archetype to link Smart Flow Sheet ids to acts.
     */
    protected static final String SFS_IDENTITY = "actIdentity.smartflowsheet";

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
     * Returns a visit given its identifier.
     *
     * @param hospitalizationId the hospitalization identifier
     * @return the visit, or {@code null} if it is not found
     */
    protected Act getVisit(String hospitalizationId) {
        return (Act) getObject(hospitalizationId, PatientArchetypes.CLINICAL_EVENT);
    }

    /**
     * Returns the patient associated with a visit.
     *
     * @param visit the visit
     * @return the patient. May be {@code null}
     */
    protected Party getPatient(Act visit) {
        ActBean bean = new ActBean(visit, service);
        return (Party) bean.getNodeParticipant("patient");
    }

    /**
     * Returns an object given its identifier.
     *
     * @param identifier the identifier. May be {@code null}
     * @param archetype  the archetype
     * @return the corresponding object, or {@code null} if none is found
     */
    public IMObject getObject(String identifier, String archetype) {
        return SmartFlowSheetHelper.getObject(archetype, identifier, service);
    }

    /**
     * Creates a new identity for a Smart Flow Sheet identifier.
     *
     * @param guid the Smart Flow Sheet identifier
     * @return a new identity
     */
    protected ActIdentity createIdentity(String guid) {
        ActIdentity identity = (ActIdentity) service.create(SFS_IDENTITY);
        identity.setIdentity(guid);
        return identity;
    }

}
