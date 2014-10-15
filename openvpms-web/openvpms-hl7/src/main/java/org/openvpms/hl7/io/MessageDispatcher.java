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

package org.openvpms.hl7.io;

import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.protocol.ReceivingApplication;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.hl7.impl.MessageConfig;

/**
 * Manages sending and receiving HL7 messages.
 *
 * @author Tim Anderson
 */
public interface MessageDispatcher {

    /**
     * Queues a message to a connector.
     *
     * @param message   the message to queue
     * @param connector the connector
     * @param config    the message population configuration
     */
    void queue(Message message, Connector connector, MessageConfig config);

    /**
     * Registers an application to handle messages from the specified connector.
     * <p/>
     * Only one application can be registered to handle messages.
     *
     * @param connector the connector
     * @param receiver  the receiver
     */
    void listen(Connector connector, ReceivingApplication receiver) throws InterruptedException;

    /**
     * Stop receiving messages from a connector.
     *
     * @param connector the connector
     */
    void stop(Connector connector);

    /**
     * Returns the statistics for a connector.
     *
     * @param connector the connector reference
     * @return the statistics, or {@code null} if the connector doesn't exist or is inactive
     */
    Statistics getStatistics(IMObjectReference connector);

}