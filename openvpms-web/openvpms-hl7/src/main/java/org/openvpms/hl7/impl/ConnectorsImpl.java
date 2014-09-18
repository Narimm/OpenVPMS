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
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.hl7.Connector;
import org.openvpms.hl7.MLLPReceiver;
import org.openvpms.hl7.MLLPSender;

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
     * The connectors, keyed on id.
     */
    private final Map<Long, State> connectors = new HashMap<Long, State>();

    private static final String SENDER_HL7_MLLP = "entity.connectorSenderHL7MLLPType";

    private static final String RECEIVER_HL7_MLLP = "entity.connectorReceiverHL7MLLPType";

    private static final String SHORT_NAME = "entity.connector*Type";


    /**
     * Constructs a {@link ConnectorsImpl}.
     *
     * @param service the archetype service
     */
    public ConnectorsImpl(IArchetypeService service) {
        super(service, SHORT_NAME, Entity.class);
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
     * Returns sending connectors active at the practice location.
     *
     * @param location the location
     * @return the connectors
     */
    @Override
    public List<Connector> getSenders(Party location) {
        EntityBean bean = new EntityBean(location, getService());
        List<IMObjectReference> refs = bean.getNodeTargetEntityRefs("connectors");
        List<Connector> result = new ArrayList<Connector>();
        for (IMObjectReference ref : refs) {
            if (TypeHelper.isA(ref, "entity.connectorSender*")) {
                Connector connector = getConnector(ref);
                if (connector != null) {
                    result.add(connector);
                }
            }
        }
        return result;
    }

    /**
     * Adds an object to the cache.
     * <p/>
     * Implementations may ignore the object if it is older than any cached instance, or is inactive
     *
     * @param object the object to add
     */
    @Override
    protected void add(Entity object) {
        addConnector(object);
    }

    /**
     * Removes an object.
     *
     * @param object the object to remove
     */
    @Override
    protected void remove(Entity object) {
        synchronized (connectors) {
            connectors.remove(object.getId());
        }
    }

    private Connector addConnector(Entity object) {
        Connector result = null;
        if (!object.isActive()) {
            remove(object);
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
        if (TypeHelper.isA(object, SENDER_HL7_MLLP)) {
            result = MLLPSender.create(object, getService());
        } else if (TypeHelper.isA(object, RECEIVER_HL7_MLLP)) {
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
