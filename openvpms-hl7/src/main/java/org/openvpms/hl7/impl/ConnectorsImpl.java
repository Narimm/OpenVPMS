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

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.hl7.Connector;
import org.openvpms.hl7.HL7Archetypes;

import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of the {@link Connectors} interface.
 *
 * @author Tim Anderson
 */
public class ConnectorsImpl extends AbstractMonitoringIMObjectCache<Entity> implements Connectors {

    /**
     * The connectors, keyed on id.
     */
    private final Map<Long, State> connectors = new HashMap<Long, State>();


    /**
     * Constructs a {@link ConnectorsImpl}.
     *
     * @param service the archetype service
     */
    public ConnectorsImpl(IArchetypeService service) {
        super(service, HL7Archetypes.SENDERS, Entity.class);
    }

    /**
     * Returns a connector given its reference.
     *
     * @param reference the connector reference
     * @return the connector, or {@code null} if none is found
     */
    @Override
    public Connector getConnector(IMObjectReference reference) {
        Connector connector = null;
        State state;
        synchronized (connectors) {
            state = connectors.get(reference.getId());
        }
        if (state != null) {
            connector = state.connector;
        } else {
            Entity object = get(reference);
            if (object != null) {
                connector = addConnector(object);
            }
        }
        return connector;
    }

    /**
     * Adds an object to the cache.
     * <p/>
     * Implementations may ignore the object if it is older than any cached instance, or is inactive
     *
     * @param object the object to add
     */
    @Override
    protected void addObject(Entity object) {
        addConnector(object);
    }

    /**
     * Removes an object.
     *
     * @param object the object to remove
     */
    @Override
    protected void removeObject(Entity object) {
        synchronized (connectors) {
            connectors.remove(object.getId());
        }
    }

    private Connector addConnector(Entity object) {
        Connector result = null;
        if (!object.isActive()) {
            removeObject(object);
        } else {
            synchronized (connectors) {
                State state = connectors.get(object.getId());
                if (state == null || state.version < object.getVersion()) {
                    result = create(object);
                    if (result != null) {
                        connectors.put(object.getId(), new State(result, object.getVersion()));
                    }
                }
            }
        }
        return result;
    }

    private Connector create(Entity object) {
        Connector result = null;
        if (TypeHelper.isA(object, HL7Archetypes.MLLP_SENDER)) {
            result = MLLPSender.create(object, getService());
        } else if (TypeHelper.isA(object, HL7Archetypes.MLLP_RECEIVER)) {
            result = MLLPReceiver.create(object, getService());
        }
        return result;
    }

    private static class State {

        private final long version;

        private final Connector connector;

        public State(Connector connector, long version) {
            this.connector = connector;
            this.version = version;
        }

    }
}
