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

import ca.uhn.hl7v2.model.Message;
import org.openvpms.hl7.Connector;

import java.util.List;

/**
 * Manages sending HL7 messages.
 *
 * @author Tim Anderson
 */
public interface MessageDispatcher {

    /**
     * Adds a listener to be notified when a message is sent.
     *
     * @param listener the listener
     */
    void addListener(ConnectorManagerListener listener);

    /**
     * Removes a listener.
     *
     * @param listener the listener to remove
     */
    void removeListener(ConnectorManagerListener listener);

    /**
     * Queues a message to a connector.
     *
     * @param message   the message to queue
     * @param connector the connector
     */
    void queue(Message message, Connector connector);

    /**
     * Queues a message to be sent via multiple connectors.
     *
     * @param message    the message to queue
     * @param connectors the connectors
     */
    void queue(Message message, List<Connector> connectors);
}