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
     * Queues a message to be sent via multiple connectors.
     *
     * @param message    the message to queue
     * @param connectors the connectors
     */
    void queue(Message message, List<Connector> connectors);
}