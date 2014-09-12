package org.openvpms.hl7.impl;

import ca.uhn.hl7v2.model.Message;

/**
 * Listener for {@link MessageDispatcher} events.
 *
 * @author Tim Anderson
 */
public interface ConnectorManagerListener {

    /**
     * Invoked when a message is sent.
     *
     * @param message  the message
     * @param response the message response
     */
    void sent(Message message, Message response);
}