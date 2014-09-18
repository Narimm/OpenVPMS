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

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

/**
 * HL7 MLLP Receiver.
 *
 * @author Tim Anderson
 */
public class MLLPReceiver extends Connector {

    /**
     * The port to listen on.
     */
    private final int port;

    private final IMObjectReference reference;

    /**
     * Constructs a {@link MLLPReceiver}.
     *
     * @param port                 the port to listen in
     * @param sendingApplication   the sending application
     * @param sendingFacility      the sending facility
     * @param receivingApplication the receiving application
     * @param receivingFacility    the receiving facility
     */
    public MLLPReceiver(int port, String sendingApplication, String sendingFacility,
                        String receivingApplication, String receivingFacility, IMObjectReference reference) {
        super(sendingApplication, sendingFacility, receivingApplication, receivingFacility);
        this.port = port;
        this.reference = reference;
    }

    /**
     * Creates an {@link MLLPReceiver} from an <em>entity.connectorReceiverHL7MLLPType</em>.
     *
     * @param object  the configuration
     * @param service the archetype service
     * @return a new {@link MLLPReceiver}
     */
    public static MLLPReceiver create(Entity object, IArchetypeService service) {
        IMObjectBean bean = new IMObjectBean(object, service);
        return new MLLPReceiver(bean.getInt("port"), bean.getString("sendingApplication"),
                                bean.getString("sendingFacility"), bean.getString("receivingApplication"),
                                bean.getString("receivingFacility"), object.getObjectReference());
    }

    /**
     * Returns the port to listen on.
     *
     * @return the port to listen on
     */
    public int getPort() {
        return port;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param obj the reference object with which to compare.
     * @return {@code true} if this object is the same as the obj argument; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        boolean result = super.equals(obj) && obj instanceof MLLPReceiver;
        if (result) {
            result = port == ((MLLPReceiver) obj).port;
        }
        return result;
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

    /**
     * Builds the hash code.
     *
     * @param builder the hash code builder
     * @return the builder
     */
    @Override
    protected HashCodeBuilder hashCode(HashCodeBuilder builder) {
        return super.hashCode(builder).append(port);
    }

}
