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
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.hl7.Connector;

/**
 * HL7 MLLP Sender.
 *
 * @author Tim Anderson
 */
public class MLLPSender extends Connector {

    /**
     * The host to connect to.
     */
    private final String host;

    /**
     * The port to connect to.
     */
    private final int port;


    /**
     * Constructs an {@link MLLPSender}.
     *
     * @param host                 the host to connect to
     * @param port                 the port to connect to
     * @param sendingApplication   the sending application
     * @param sendingFacility      the sending facility
     * @param receivingApplication the receiving application
     * @param receivingFacility    the receiving facility
     * @param reference            the connection reference
     */
    public MLLPSender(String host, int port, String sendingApplication, String sendingFacility,
                      String receivingApplication, String receivingFacility, IMObjectReference reference) {
        this(host, port, sendingApplication, sendingFacility, receivingApplication, receivingFacility, true,
             true, reference);
    }

    /**
     * Constructs a {@link MLLPSender}.
     *
     * @param host                 the host to connect to
     * @param port                 the port to connect to
     * @param sendingApplication   the sending application
     * @param sendingFacility      the sending facility
     * @param receivingApplication the receiving application
     * @param receivingFacility    the receiving facility
     * @param includeMillis        if {@code true} include milliseconds in time fields
     * @param includeTimeZone      if {@code true} include the timezone in date/time fields
     * @param reference            the connection reference
     */
    public MLLPSender(String host, int port, String sendingApplication, String sendingFacility,
                      String receivingApplication, String receivingFacility, boolean includeMillis,
                      boolean includeTimeZone, IMObjectReference reference) {
        super(sendingApplication, sendingFacility, receivingApplication, receivingFacility, includeMillis,
              includeTimeZone, reference);
        this.host = host;
        this.port = port;
    }

    /**
     * Creates an {@link MLLPSender} from an <em>entity.HL7SenderMLLP</em>.
     *
     * @param object  the configuration
     * @param service the archetype service
     * @return a new {@link MLLPSender}
     */
    public static MLLPSender create(Entity object, IArchetypeService service) {
        EntityBean bean = new EntityBean(object, service);
        return new MLLPSender(bean.getString("host"), bean.getInt("port"), bean.getString("sendingApplication"),
                              bean.getString("sendingFacility"), bean.getString("receivingApplication"),
                              bean.getString("receivingFacility"), bean.getBoolean("includeMillis"),
                              bean.getBoolean("includeTimeZone"), object.getObjectReference());
    }

    /**
     * Returns the host to connect to.
     *
     * @return the host
     */
    public String getHost() {
        return host;
    }

    /**
     * Returns the port to connect to.
     *
     * @return the port
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
        boolean result = super.equals(obj) && obj instanceof MLLPSender;
        if (result) {
            MLLPSender other = (MLLPSender) obj;
            result = port == other.port && ObjectUtils.equals(host, other.host);
        }
        return result;
    }

    /**
     * Builds the hash code.
     *
     * @param builder the hash code builder
     * @return the builder
     */
    @Override
    protected HashCodeBuilder hashCode(HashCodeBuilder builder) {
        return super.hashCode(builder).append(host).append(port);
    }
}
