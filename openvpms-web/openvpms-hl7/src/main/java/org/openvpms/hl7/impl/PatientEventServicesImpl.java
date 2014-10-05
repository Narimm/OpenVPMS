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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.hl7.impl;

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.hl7.Connector;
import org.openvpms.hl7.patient.PatientEventServices;
import org.openvpms.hl7.util.HL7Archetypes;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of the {@link PatientEventServices} interface.
 *
 * @author Tim Anderson
 */
public class PatientEventServicesImpl extends MonitoringIMObjectCache<Entity> implements PatientEventServices {

    /**
     * The connectors.
     */
    private final Connectors connectors;

    /**
     * Constructs a {@link PatientEventServicesImpl}.
     *
     * @param service    the archetype service
     * @param connectors the connectors
     */
    public PatientEventServicesImpl(IArchetypeService service, Connectors connectors) {
        super(service, HL7Archetypes.PATIENT_EVENT_SERVICE, Entity.class, false);
        this.connectors = connectors;
        load();
    }

    /**
     * Registers a service to be notified of patient events.
     *
     * @param service the service
     */
    @Override
    public void add(Entity service) {
        addObject(service);
    }

    /**
     * Removes a service.
     *
     * @param service the service to remove
     */
    @Override
    public void remove(Entity service) {
        removeObject(service);
    }

    /**
     * Returns the connections to services for a given practice location.
     *
     * @param location the practice location
     * @return the connections
     */
    @Override
    public Collection<Connector> getConnections(Party location) {
        return getConnections(location.getObjectReference());
    }

    /**
     * Returns the connections to services for a given practice location.
     *
     * @param location the practice location reference
     * @return the connections
     */
    @Override
    public Collection<Connector> getConnections(IMObjectReference location) {
        Map<IMObjectReference, Connector> result = new HashMap<IMObjectReference, Connector>();
        for (Entity object : getObjects()) {
            EntityBean bean = new EntityBean(object, getService());
            if (ObjectUtils.equals(bean.getNodeTargetObjectRef("location"), location)) {
                IMObjectReference sender = bean.getNodeTargetObjectRef("sender");
                if (sender != null && !result.containsKey(sender)) {
                    Connector connector = connectors.getConnector(sender);
                    if (connector != null) {
                        result.put(sender, connector);
                    }
                }
            }
        }
        return result.values();
    }
}
