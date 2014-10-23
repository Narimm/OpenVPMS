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
import org.openvpms.hl7.io.Connector;
import org.openvpms.hl7.io.Connectors;
import org.openvpms.hl7.util.HL7Archetypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of the {@link Connectors} interface.
 *
 * @author Tim Anderson
 */
public class ConnectorsImpl extends AbstractMonitoringIMObjectCache<Entity> implements Connectors {

    /**
     * Listener for connector events.
     */
    public interface Listener {

        /**
         * Invoked when a connector is added or updated.
         *
         * @param connector the connector
         */
        void added(Connector connector);

        /**
         * Invoked when a connector is removed or de-activated.
         *
         * @param connector the connector
         */
        void removed(Connector connector);
    }

    /**
     * The connectors, keyed on id.
     */
    private final Map<Long, State> connectors = new HashMap<Long, State>();

    /**
     * Listeners to notify when a connector changes.
     */
    private final List<Listener> listeners = new ArrayList<Listener>();

    /**
     * Constructs a {@link ConnectorsImpl}.
     *
     * @param service the archetype service
     */
    public ConnectorsImpl(IArchetypeService service) {
        super(service, HL7Archetypes.CONNECTIONS, Entity.class);
        load();
    }

    /**
     * Returns the active connectors.
     *
     * @return the active connectors
     */
    @Override
    public List<Connector> getConnectors() {
        List<Connector> result = new ArrayList<Connector>();
        synchronized (connectors) {
            for (State state : connectors.values()) {
                result.add(state.connector);
            }
        }
        return result;
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
                connector = update(object);
            }
        }
        return connector;
    }

    /**
     * Adds a listener to be notified of connector updates.
     *
     * @param listener the listener to add
     */
    public void addListener(Listener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    /**
     * Removes a listener.
     *
     * @param listener the listener to remove
     */
    public void removeListener(Listener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
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
        Connector connector = update(object);
        if (connector != null) {
            for (Listener listener : getListeners()) {
                listener.added(connector);
            }
        }
    }

    /**
     * Removes an object.
     *
     * @param object the object to remove
     */
    @Override
    protected void removeObject(Entity object) {
        State state;
        synchronized (connectors) {
            state = connectors.remove(object.getId());
        }
        if (state != null) {
            for (Listener listener : getListeners()) {
                listener.removed(state.connector);
            }
        }
    }

    /**
     * Returns the listeners.
     *
     * @return the listeners
     */
    protected Listener[] getListeners() {
        Listener[] result;
        synchronized (listeners) {
            result = listeners.toArray(new Listener[listeners.size()]);
        }
        return result;
    }

    /**
     * Updates a connector.
     *
     * @param object the connector configuration
     * @return the updated connector, or {@code null} if the connector is inactive
     */
    private Connector update(Entity object) {
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
