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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.hl7.impl;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.app.HL7Service;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v25.datatype.HD;
import ca.uhn.hl7v2.model.v25.segment.MSH;
import ca.uhn.hl7v2.protocol.ReceivingApplication;
import ca.uhn.hl7v2.protocol.ReceivingApplicationException;
import ca.uhn.hl7v2.protocol.ReceivingApplicationExceptionHandler;
import ca.uhn.hl7v2.protocol.Transportable;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.hl7.io.Connector;
import org.openvpms.hl7.io.MessageService;

import java.util.HashMap;
import java.util.Map;

/**
 * A receiver that supports multiple connectors on the one socket.
 *
 * @author Tim Anderson
 */
class DemultiplexingReceiver implements ReceivingApplication, ReceivingApplicationExceptionHandler {

    /**
     * The message service.
     */
    private final MessageService messageService;

    /**
     * The service responsible for delegating messages to this.
     */
    private final HL7Service service;

    /**
     * The port this receiver is listening on.
     */
    private final int port;

    /**
     * The receivers to delegate messages to.
     */
    private Map<Connector, MessageReceiver> receivers = new HashMap<>();

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(DemultiplexingReceiver.class);

    /**
     * Key used to store the receiver in the meta-data, to be used by {@link #processException}
     */
    private static String META_DATA_KEY = DemultiplexingReceiver.class.getName();

    /**
     * Constructs a {@link DemultiplexingReceiver}.
     *
     * @param messageService the message service
     * @param context        the HL7 context
     * @param port           the port the receiver is listening on
     */
    public DemultiplexingReceiver(MessageService messageService, HapiContext context, int port) {
        this.messageService = messageService;
        this.port = port;
        this.service = context.newServer(port, false);
        service.registerApplication(this);
        service.setExceptionHandler(this);
    }

    /**
     * Adds a receiver to handle messages for a connector.
     *
     * @param connector the connector
     * @param receiver  the receiver to delegate to
     * @param user      the user responsible for messages received the connector
     */
    public synchronized MessageReceiver add(Connector connector, ReceivingApplication receiver, User user) {
        MessageReceiver result = new MessageReceiver(receiver, connector, messageService, user, service);
        receivers.put(connector, result);
        return result;
    }

    /**
     * Removes a receiver for a connector.
     *
     * @param connector the connector
     */
    public synchronized void remove(Connector connector) {
        receivers.remove(connector);
    }

    /**
     * Used to determine if the receiver has any registered connectors.
     *
     * @return {@code true} if the receiver has no registered connectors
     */
    public synchronized boolean isEmpty() {
        return receivers.isEmpty();
    }

    /**
     * Starts listening for connections.
     */
    public void start() {
        try {
            log.info("Starting to listen for HL7 messages on port=" + port);
            service.startAndWait();
        } catch (InterruptedException exception) {
            log.warn("Interrupted while starting DemultiplexingReceiver for port=" + port, exception);
        }
    }

    /**
     * Stops message receipt.
     */
    public synchronized void stop() {
        log.info("Stopping listening for HL7 messages on port=" + port);
        service.stopAndWait();
    }

    /**
     * Determines if the receiver is currently able to receive messages.
     *
     * @return {@code true} if the receiver is able to receive messages
     */
    public boolean isRunning() {
        return service.isRunning();
    }

    /**
     * Uses the contents of the message for whatever purpose the application
     * has for this message, and returns an appropriate response message.
     *
     * @param message  an inbound HL7 message
     * @param metaData message metadata (which may include information about where the message comes from, etc).
     *                 This is the same metadata as in {@link Transportable#getMetadata()}.
     * @return an appropriate application response
     * @throws ReceivingApplicationException if there is a problem internal to the application (for example a
     *                                       database problem)
     * @throws HL7Exception                  if there is a problem with the message
     */
    @Override
    public Message processMessage(Message message, Map<String, Object> metaData)
            throws ReceivingApplicationException, HL7Exception {
        MessageReceiver receiver = getReceiver(message);
        if (receiver == null) {
            throw new ReceivingApplicationException("No receiver to handle message");
        }
        metaData.put(META_DATA_KEY, receiver);
        return receiver.processMessage(message, metaData);
    }

    /**
     * Determines if a receiver can process a message.
     *
     * @param message an inbound HL7 message
     * @return true if this ReceivingApplication wishes to accept the message.  By returning
     * true, this Application declares itself the recipient of the message, accepts
     * responsibility for it, and must be able to respond appropriately to the sending system.
     */
    @Override
    public boolean canProcess(Message message) {
        MessageReceiver receiver = getReceiver(message);
        return receiver != null && receiver.canProcess(message);
    }

    /**
     * Process an exception.
     *
     * @param incomingMessage  the incoming message. This is the raw message which was received from the external
     *                         system
     * @param incomingMetadata Any metadata that accompanies the incoming message.
     * @param outgoingMessage  the outgoing message. The response NAK message generated by HAPI.
     * @param exception        the exception which was received
     * @return The new outgoing message. This can be set to the value provided
     * by HAPI in {@code outgoingMessage}, or may be replaced with
     * another message. <b>This method may not return {@code null}</b>.
     */
    @Override
    public String processException(String incomingMessage, Map<String, Object> incomingMetadata,
                                   String outgoingMessage, Exception exception) throws HL7Exception {
        String result = outgoingMessage;
        MessageReceiver receiver = (MessageReceiver) incomingMetadata.get(META_DATA_KEY);
        if (receiver != null) {
            result = receiver.processException(incomingMessage, incomingMetadata, outgoingMessage, exception);
        } else {
            log.error(exception.getMessage(), exception);
        }
        return result;
    }

    /**
     * Returns a receiver for a message, based on the sending application, sending facility, receiving application
     * and receiving facility.
     *
     * @param message the message
     * @return the receiver for the message, or {@code null} if none is found
     */
    private MessageReceiver getReceiver(Message message) {
        MSH msh = HL7MessageHelper.getMSH(message);
        String sendingApp = null;
        String sendingFacility = null;
        String receivingApp = null;
        String receivingFacility = null;
        if (msh != null) {
            sendingApp = getNamespaceID(msh.getSendingApplication());
            sendingFacility = getNamespaceID(msh.getSendingFacility());
            receivingApp = getNamespaceID(msh.getReceivingApplication());
            receivingFacility = getNamespaceID(msh.getReceivingFacility());
            for (Map.Entry<Connector, MessageReceiver> entry : receivers.entrySet()) {
                Connector connector = entry.getKey();
                if (ObjectUtils.equals(connector.getSendingApplication(), sendingApp)
                    && ObjectUtils.equals(connector.getSendingFacility(), sendingFacility)
                    && ObjectUtils.equals(connector.getReceivingApplication(), receivingApp)
                    && ObjectUtils.equals(connector.getReceivingFacility(), receivingFacility)) {
                    return entry.getValue();
                }
            }
        }
        String type = (msh != null) ? HL7MessageHelper.getMessageName(msh) : null;
        String header = null;
        if (msh != null) {
            try {
                header = HL7MessageHelper.toString(msh);
            } catch (HL7Exception exception) {
                log.warn("Failed to encode header", exception);
            }
        }

        log.warn("No receiver for message of type=" + type + ", Sending Facility=" + sendingFacility
                 + ", Sending Application=" + sendingApp + ", Receiving Facility=" + receivingFacility
                 + ", Receiving Application=" + receivingApp + ", received on port=" + port + ", header=" + header);
        return null;
    }

    /**
     * Helper to return the namespace id for an {@link HD}.
     *
     * @param hd the hd
     * @return the corresponding namespace id
     */
    private String getNamespaceID(HD hd) {
        return hd.getNamespaceID().getValue();
    }

}
