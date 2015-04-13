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
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.hl7.io.Connector;

/**
 * HL7 MLLP Sender.
 *
 * @author Tim Anderson
 */
class MLLPSender extends Connector {

    /**
     * Default time to wait for responses, in seconds.
     */
    public static final int DEFAULT_RESPONSE_TIMEOUT = 30;

    /**
     * Default time to wait before resending a message, in seconds.
     */
    public static final int DEFAULT_RETRY_INTERVAL = 30;

    /**
     * The host to connect to.
     */
    private final String host;

    /**
     * The port to connect to.
     */
    private final int port;

    /**
     * The response timeout, in seconds.
     */
    private final int responseTimeout;

    /**
     * The interval between retries, in seconds.
     */
    private final int retryInterval;

    /**
     * If {@code true}, indicates that messages should be queued but not sent.
     */
    private final boolean suspended;


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
        this(host, port, sendingApplication, sendingFacility, receivingApplication, receivingFacility,
             DEFAULT_RESPONSE_TIMEOUT, DEFAULT_RETRY_INTERVAL, true, true, false, reference);
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
     * @param responseTimeout      the maximum time to wait for a response, in seconds
     * @param retryInterval        the interval to wait before resending a message after failure, in seconds
     * @param includeMillis        if {@code true} include milliseconds in time fields
     * @param includeTimeZone      if {@code true} include the timezone in date/time fields
     * @param suspended            if {@code true} indicates that messages should be queued but not sent
     * @param reference            the connection reference
     */
    public MLLPSender(String host, int port, String sendingApplication, String sendingFacility,
                      String receivingApplication, String receivingFacility, int responseTimeout, int retryInterval,
                      boolean includeMillis, boolean includeTimeZone, boolean suspended, IMObjectReference reference) {
        super(sendingApplication, sendingFacility, receivingApplication, receivingFacility, includeMillis,
              includeTimeZone, reference);
        this.host = host;
        this.port = port;
        this.responseTimeout = responseTimeout;
        this.retryInterval = retryInterval;
        this.suspended = suspended;
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
                              bean.getString("receivingFacility"),
                              bean.getInt("responseTimeout", DEFAULT_RESPONSE_TIMEOUT),
                              bean.getInt("retryInterval", DEFAULT_RETRY_INTERVAL),
                              bean.getBoolean("includeMillis"),
                              bean.getBoolean("includeTimeZone"), bean.getBoolean("suspended"),
                              object.getObjectReference());
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
     * Determines if messaging should be suspended.
     * <p/>
     * When suspended, messages are queued but not sent.
     *
     * @return {@code true} if messaging should be suspended
     */
    public boolean isSuspended() {
        return suspended;
    }

    /**
     * Returns the maximum time to wait for a response.
     *
     * @return the the maximum time to wait for a response, in seconds.
     */
    public int getResponseTimeout() {
        return responseTimeout;
    }

    /**
     * Returns the interval to wait before resending a message after failure.
     *
     * @return the interval to wait before resending a message after failure, in seconds
     */
    public int getRetryInterval() {
        return retryInterval;
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
            result = port == other.port && ObjectUtils.equals(host, other.host)
                     && responseTimeout == other.responseTimeout && retryInterval == other.retryInterval;
        }
        return result;
    }

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append("host", host)
                .append("port", port)
                .toString();
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
