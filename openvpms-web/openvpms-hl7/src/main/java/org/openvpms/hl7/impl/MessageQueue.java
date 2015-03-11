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

import ca.uhn.hl7v2.AcknowledgmentCode;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v25.message.ACK;
import ca.uhn.hl7v2.model.v25.segment.MSA;
import ca.uhn.hl7v2.model.v25.segment.MSH;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.hl7.io.MessageService;
import org.openvpms.hl7.io.Statistics;
import org.openvpms.hl7.util.HL7MessageStatuses;

import java.util.Date;

/**
 * A queue of messages for a {@link MLLPSender}.
 * <p/>
 * TODO - cache counts, and pre-fetch messages to reduce database access.
 *
 * @author Tim Anderson
 */
class MessageQueue implements Statistics {

    /**
     * The message service.
     */
    private final MessageService service;

    /**
     * The message context.
     */
    private final HapiContext context;

    /**
     * The connector that this queue manages messages for.
     */
    private MLLPSender connector;

    /**
     * The current message act.
     */
    private DocumentAct currentAct;

    /**
     * The current message.
     */
    private Message currentMessage;

    /**
     * The time when a message was last successfully sent.
     */
    private Date lastSent;

    /**
     * The error message if the last send was unsuccessful.
     */
    private Date lastError;

    /**
     * The error message if the last send was unsuccessful.
     */
    private String lastErrorMessage;

    /**
     * Used to set a time when messaging should resume.
     */
    private long waitUntil = -1;

    /**
     * Determines if messaging is suspended.
     */
    private boolean suspended;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(MessageQueue.class);


    /**
     * Constructs an {@link MessageQueue}.
     *
     * @param connector the connector
     * @param service   the message service
     * @param context   the message context
     */
    public MessageQueue(MLLPSender connector, MessageService service, HapiContext context) {
        this.connector = connector;
        this.service = service;
        this.context = context;
    }

    /**
     * Adds a message to the end of the queue.
     *
     * @param message the message to add
     * @param user    the user responsible for the message
     * @return the added act
     * @throws HL7Exception if the message cannot be encoded
     */
    public DocumentAct add(Message message, User user) throws HL7Exception {
        return service.save(message, connector, user);
    }

    /**
     * Retrieves, but does not remove, the first message in the queue.
     *
     * @return the head of this queue, or {@code null} if the queue is empty
     */
    public synchronized Message peekFirst() {
        if (currentMessage == null) {
            getNext();
        }
        return currentMessage;
    }

    /**
     * Retrieves, but does not remove, the first act in the queue.
     *
     * @return the head of this queue, or {@code null} if the queue is empty
     */
    public synchronized DocumentAct peekFirstAct() {
        peekFirst();
        return currentAct;
    }

    /**
     * Invoked when a message is sent.
     *
     * @param response the response
     * @return the act the act corresponding to the sent message
     */
    public synchronized DocumentAct sent(Message response) {
        if (currentAct == null) {
            throw new IllegalStateException("No current message");
        }
        long waitUntil = -1;
        DocumentAct result = currentAct;
        if (response instanceof ACK) {
            ACK ack = (ACK) response;
            MSA msa = ack.getMSA();
            String ackCode = msa.getAcknowledgmentCode().getValue();
            if (AcknowledgmentCode.AA.toString().equals(ackCode)) {
                processed();
            } else if (AcknowledgmentCode.AE.toString().equals(ackCode)) {
                handleError(ack, HL7MessageStatuses.PENDING);
                waitUntil = System.currentTimeMillis() + 30 * 1000;
            } else {
                handleError(ack, HL7MessageStatuses.ERROR);
            }
        } else {
            unsupportedResponse(response);
        }
        setWaitUntil(waitUntil);
        return result;
    }

    /**
     * Invoked when an exception occurs sending a message.
     *
     * @param exception the exception
     */
    public synchronized void error(Throwable exception) {
        completed(new Date(), exception.getMessage());
    }

    /**
     * Updates the connector.
     *
     * @param connector the connector
     */
    public synchronized void setConnector(MLLPSender connector) {
        this.connector = connector;
        setSuspended(connector.isSuspended());
    }

    /**
     * Returns the connector.
     *
     * @return the connector
     */
    public synchronized MLLPSender getConnector() {
        return connector;
    }

    /**
     * Determines if messaging should be suspended.
     *
     * @param suspend if {@code true}, suspend messaging, otherwise resume it
     */
    public synchronized void setSuspended(boolean suspend) {
        this.suspended = suspend;
    }

    /**
     * Determines if messaging should be suspended.
     *
     * @return {@code true} if messaging should be suspended
     */
    public synchronized boolean isSuspended() {
        return suspended;
    }

    /**
     * Set the time after which messaging should continue.
     *
     * @param millis the time, in milliseconds, or {@code -1} if there is no delay
     */
    public synchronized void setWaitUntil(long millis) {
        this.waitUntil = millis;
    }

    /**
     * Set the time after which messaging should continue.
     *
     * @return the time, in milliseconds, or {@code -1} if there is no delay
     */
    public synchronized long getWaitUntil() {
        return waitUntil;
    }

    /**
     * Returns the number of messages in the queue.
     *
     * @return the number of messages
     */
    public int getQueued() {
        return countMessages(HL7MessageStatuses.PENDING);
    }

    /**
     * Returns the number of messages in the error queue.
     * <p/>
     * Only applies to sending connectors.
     *
     * @return the number of messages
     */
    @Override
    public int getErrors() {
        return countMessages(HL7MessageStatuses.ERROR);
    }

    /**
     * Returns the time of the last processed message.
     * <p/>
     * For senders, this indicates the time when a message was last sent, and an acknowledgment received.
     * <p/>
     * For receivers, this indicates the time when a message was last received and processed.
     *
     * @return the time when a message was last processed, or {@code null} if none have been processed
     */
    @Override
    public synchronized Date getProcessedTimestamp() {
        return lastSent;
    }

    /**
     * Returns the time of the last error.
     *
     * @return the time of the last error, or {@code null} if the last message was successfully processed
     */
    @Override
    public synchronized Date getErrorTimestamp() {
        return lastError;
    }

    /**
     * Returns the error message of the last error.
     *
     * @return the last error message. May be {@code null}
     */
    @Override
    public synchronized String getErrorMessage() {
        return lastErrorMessage;
    }

    /**
     * Invoked when a message is successfully processed.
     */
    private void processed() {
        lastSent = new Date();
        try {
            service.accepted(currentAct, lastSent);
        } finally {
            completed(null, null);
        }
    }

    /**
     * Invoked when an ack indicates an error.
     *
     * @param ack    the message acknowledgment
     * @param status the new act status
     */
    private void handleError(ACK ack, String status) {
        String error = HL7MessageHelper.getErrorMessage(ack);
        error(status, error);
    }

    /**
     * Invoked when an unsupported response is received.
     *
     * @param response the response
     */
    private void unsupportedResponse(Message response) {
        StringBuilder error = new StringBuilder();
        error.append("Unsupported response: ");
        try {
            MSH header = (MSH) response.get("MSH");
            error.append(HL7MessageHelper.getMessageName(header));
        } catch (HL7Exception exception) {
            log.error("Failed to determine message type", exception);
            error.append("unknown");
        }
        error.append("\nMessage: ");
        try {
            error.append(HL7MessageHelper.toString(response));
        } catch (HL7Exception exception) {
            log.error("Failed to format message", exception);
            error.append("unknown");
        }
        error(HL7MessageStatuses.ERROR, error.toString());
    }

    /**
     * Invoked after sending/attempting to send a message.
     *
     * @param errorDate the error date/time, or {@code null} if there was no error
     * @param error     the error message, or {@code null} if there was no error
     */
    private void completed(Date errorDate, String error) {
        currentMessage = null;
        currentAct = null;
        lastError = errorDate;
        lastErrorMessage = error;
    }

    /**
     * Retrieves the next message.
     */
    private void getNext() {
        DocumentAct act;
        while ((act = service.next(connector)) != null) {
            Message message = decode(act);
            if (message != null) {
                currentAct = act;
                currentMessage = message;
                break;
            }
        }
    }

    /**
     * Decodes an HL7 message from an act.
     * <p/>
     * If the message cannot be decoded, the act status will be set to ERROR.
     *
     * @param act the <em>act.HL7Message</em> act
     * @return the message, or {@code null} if the message cannot be decoded
     */
    private Message decode(DocumentAct act) {
        Message result = null;
        try {
            result = service.get(act, context.getGenericParser());
        } catch (HL7Exception exception) {
            log.error(exception.getMessage(), exception);
            service.error(act, HL7MessageStatuses.ERROR, new Date(), exception.getMessage());
        }
        return result;
    }

    /**
     * Count messages with the specified status.
     *
     * @param status the message status
     * @return the no. of messages with th status
     */
    private int countMessages(String status) {
        return service.getMessages(connector, status);
    }

    /**
     * Updates the current act with a status and error message.
     *
     * @param status the act status
     * @param error  the error message
     */
    private void error(String status, String error) {
        Date now = new Date();
        try {
            log.error("Error received from " + connector + ":" + error);
            service.error(currentAct, status, now, error);
        } finally {
            completed(now, error);
        }
    }

}
