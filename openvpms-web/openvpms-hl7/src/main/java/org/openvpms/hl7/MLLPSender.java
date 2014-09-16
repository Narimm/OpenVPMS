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

package org.openvpms.hl7;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

/**
 * Enter description.
 *
 * @author Tim Anderson
 */
public class MLLPSender extends Connector {

    private String host;

    private int port;

    private final IMObjectReference reference;

    public MLLPSender(String host, int port, String sendingApplication, String sendingFacility,
                      String receivingApplication, String receivingFacility) {
        setHost(host);
        setPort(port);
        setSendingApplication(sendingApplication);
        setSendingFacility(sendingFacility);
        setReceivingApplication(receivingApplication);
        setReceivingFacility(receivingFacility);
        reference = new IMObjectReference("entity.connectorSenderHL7MLLPType", -1);
    }

    public MLLPSender(Entity object, IArchetypeService service) {
        IMObjectBean bean = new IMObjectBean(object, service);
        setHost(bean.getString("host"));
        setPort(bean.getInt("port"));
        setSendingApplication(bean.getString("sendingApplication"));
        setReceivingApplication(bean.getString("receivingApplication"));
        setReceivingFacility(bean.getString("receivingFacility"));
        reference = object.getObjectReference();
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Returns the connector reference.
     *
     * @return the connector reference
     */
    @Override
    public IMObjectReference getReference() {
        return reference;
    }
}
