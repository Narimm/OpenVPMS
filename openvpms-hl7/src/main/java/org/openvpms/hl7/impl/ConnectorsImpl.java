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
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.AbstractArchetypeServiceListener;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.IArchetypeServiceListener;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.hl7.Connector;
import org.openvpms.hl7.MLLPSender;
import org.springframework.beans.factory.DisposableBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Enter description.
 *
 * @author Tim Anderson
 */
public class ConnectorsImpl implements Connectors, DisposableBean {

    private static final String SENDER_HL7_MLLP = "entity.connectorSenderHL7MLLPType";
    private static final String SHORT_NAME = "entity.connector*Type";
    private final IArchetypeService service;

    private Map<Long, Connector> senders = Collections.synchronizedMap(new HashMap<Long, Connector>());
    private final IArchetypeServiceListener listener;

    public ConnectorsImpl(IArchetypeService service) {
        this.service = service;
        listener = new AbstractArchetypeServiceListener() {
            @Override
            public void saved(IMObject object) {
                addConnector(object);
            }

            @Override
            public void removed(IMObject object) {
                removeConnector(object);
            }
        };
        service.addListener(SHORT_NAME, listener);
    }

    /**
     * Invoked by a BeanFactory on destruction of a singleton.
     */
    @Override
    public void destroy() {
        service.removeListener(SHORT_NAME, listener);
    }

    /**
     * Returns a connector given its reference.
     *
     * @param reference the connector reference
     * @return the connector, or {@code null} if none is found
     */
    @Override
    public Connector getConnector(IMObjectReference reference) {
        Connector connector = senders.get(reference.getId());
        if (connector == null) {
            IMObject object = service.get(reference);
            if (object != null && object.isActive()) {
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
        EntityBean bean = new EntityBean(location, service);
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

    private Connector addConnector(IMObject object) {
        Connector connector = null;
        if (TypeHelper.isA(object, SENDER_HL7_MLLP)) {
            connector = new MLLPSender((Entity) object, service);
            senders.put(object.getId(), connector);
        }
        return connector;
    }

    private void removeConnector(IMObject object) {
        senders.remove(object.getId());
    }

}
