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
import ca.uhn.hl7v2.model.v25.datatype.CWE;
import ca.uhn.hl7v2.model.v25.datatype.MSG;
import ca.uhn.hl7v2.model.v25.datatype.TS;
import ca.uhn.hl7v2.model.v25.message.ACK;
import ca.uhn.hl7v2.model.v25.segment.ERR;
import ca.uhn.hl7v2.model.v25.segment.MSA;
import ca.uhn.hl7v2.model.v25.segment.MSH;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.hl7.io.Statistics;
import org.openvpms.hl7.util.HL7ActStatuses;
import org.openvpms.hl7.util.HL7Archetypes;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * A queue of messages for a {@link MLLPSender}.
 * <p/>
 * TODO - cache counts, and pre-fetch messages to reduce database access.
 *
 * @author Tim Anderson
 */
class MessageQueue implements Statistics {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The handler used to manage <em>document.HL7</em> documents.
     */
    private final HL7DocumentHandler handler;

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
     * The hl7 mime type.
     */
    private static final String MIME_TYPE = "application/hl7-v2+er7"; // for now, will be using the PipeParser

    /**
     * Length restriction for error messages.
     */
    private static final int MAX_ERROR_LENGTH = 5000;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(MessageQueue.class);


    /**
     * Constructs an {@link MessageQueue}.
     *
     * @param connector the connector
     * @param service   the archetype service
     * @param handler   the handler used to manage <em>document.HL7</em> documents
     * @param context   the message context
     */
    public MessageQueue(MLLPSender connector, IArchetypeService service, HL7DocumentHandler handler,
                        HapiContext context) {
        this.connector = connector;
        this.service = service;
        this.handler = handler;
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
        DocumentAct act = (DocumentAct) service.create(HL7Archetypes.MESSAGE);
        ActBean bean = new ActBean(act, service);
        bean.addNodeParticipation("connector", connector.getReference());
        bean.addNodeParticipation("author", user);
        MSH header = (MSH) message.get("MSH");
        String type = getMessageType(header);
        act.setDescription(type);
        TS dateTimeOfMessage = header.getDateTimeOfMessage();
        act.setActivityStartTime(dateTimeOfMessage.getTime().getValueAsDate());
        String name = type + "_" + header.getMessageControlID().getValue() + ".hl7";
        String encoded = message.encode();
        Document document = handler.create(name, encoded, MIME_TYPE);
        act.setDocument(document.getObjectReference());
        List<IMObject> toSave = Arrays.asList(document, act);
        service.save(toSave);
        return act;
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
                handleError(ack, HL7ActStatuses.PENDING);
                waitUntil = System.currentTimeMillis() + 30 * 1000;
            } else {
                handleError(ack, HL7ActStatuses.ERROR);
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
        return countMessages(HL7ActStatuses.PENDING);
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
        return countMessages(HL7ActStatuses.ERROR);
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
     *
     * @throws IllegalStateException if there is no current message
     */
    private void processed() {
        lastSent = new Date();
        try {
            currentAct.setActivityEndTime(lastSent);
            currentAct.setStatus(HL7ActStatuses.ACCEPTED);
            ActBean bean = new ActBean(currentAct, service);
            bean.setValue("error", null);
            bean.save();
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
        String error = getErrorMessage(ack);
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
            error.append(getMessageType(header));
        } catch (HL7Exception exception) {
            log.error("Failed to determine message type", exception);
            error.append("unknown");
        }
        error.append("\nMessage: ");
        try {
            error.append(format(response));
        } catch (HL7Exception exception) {
            log.error("Failed to format message", exception);
            error.append("unknown");
        }
        error(HL7ActStatuses.ERROR, error.toString());
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
        ArchetypeQuery query = createQuery(HL7ActStatuses.PENDING);
        query.add(Constraints.sort("id"));
        query.setMaxResults(1);
        IMObjectQueryIterator<DocumentAct> iterator = new IMObjectQueryIterator<DocumentAct>(service, query);
        while (iterator.hasNext()) {
            DocumentAct act = iterator.next();
            Message message = decode(act);
            if (message != null) {
                currentAct = act;
                currentMessage = message;
                break;
            }
        }
    }

    /**
     * Returns the message type of a message.
     *
     * @param header the message header
     * @return the formatted type
     */
    private String getMessageType(MSH header) {
        MSG type = header.getMessageType();
        return type.getMessageCode() + "_" + type.getTriggerEvent();
    }

    /**
     * Generates an error message from an acknowledgement.
     *
     * @param ack the acknowledgement
     * @return the error message
     */
    private String getErrorMessage(ACK ack) {
        StringBuilder buffer = new StringBuilder();
        String text = ack.getMSA().getTextMessage().getValue(); // deprecated in HL7 2.4
        if (!StringUtils.isEmpty(text)) {
            buffer.append(text);
        }
        try {
            for (ERR err : ack.getERRAll()) {
                String hl7ErrorCode = formatCWE(err.getHL7ErrorCode());
                if (hl7ErrorCode != null) {
                    append(buffer, "HL7 Error Code: ", hl7ErrorCode);
                }
                String errorCode = formatCWE(err.getApplicationErrorCode());
                if (!StringUtils.isEmpty(errorCode)) {
                    append(buffer, "Application Error Code: ", errorCode);
                }
                String diagnostic = err.getDiagnosticInformation().getValue();
                if (!StringUtils.isEmpty(diagnostic)) {
                    append(buffer, "Diagnostic Information: ", diagnostic);
                }
                String userMessage = err.getUserMessage().getValue();
                if (!StringUtils.isEmpty(userMessage)) {
                    append(buffer, "User Message: ", userMessage);
                }
            }
        } catch (HL7Exception exception) {
            log.error("Failed to access ERR segments", exception);
        }
        if (buffer.length() == 0) {
            buffer.append("Message body: ");
            try {
                buffer.append(format(ack));
            } catch (HL7Exception exception) {
                buffer.append("unknown");
                log.error("Failed to encode message", exception);
            }
        }
        return buffer.toString();
    }

    /**
     * Formats a Coded with Exceptions message field.
     *
     * @param field the field to format
     * @return the formatted field, or {@code null} if there is nothing to format
     */
    private String formatCWE(CWE field) {
        String result = null;
        String id = field.getIdentifier().getValue();
        String text = field.getText().getValue();
        if (!StringUtils.isEmpty(id) || !StringUtils.isEmpty(text)) {
            if (!StringUtils.isEmpty(id) && !StringUtils.isEmpty(text)) {
                result = id + " - " + text;
            } else if (!StringUtils.isEmpty(id)) {
                result = id;
            } else {
                result = text;
            }

            String originalText = field.getOriginalText().getValue();
            if (!StringUtils.isEmpty(originalText)) {
                result += "\nOriginal Text: ";
                result += originalText;
            }
        }
        return result;
    }

    /**
     * Creates a query for PENDING messages.
     *
     * @param status the message status
     * @return a new query
     */
    private ArchetypeQuery createQuery(String status) {
        ArchetypeQuery query = new ArchetypeQuery(HL7Archetypes.MESSAGE);
        query.add(Constraints.join("connector").add(Constraints.eq("entity", connector.getReference())));
        query.add(Constraints.eq("status", status));
        return query;
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
        IMObjectReference ref = act.getDocument();
        Document document = (ref != null) ? (Document) service.get(ref) : null;
        if (document == null) {
            error(act, "No message content");
        } else {
            String content = handler.getStringContent(document);
            try {
                result = context.getGenericParser().parse(content);
            } catch (HL7Exception exception) {
                error(act, exception.getMessage());
            }
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
        ArchetypeQuery query = createQuery(status);
        query.setCountResults(true);
        IPage<IMObject> page = service.get(query);
        return page.getTotalResults();
    }

    /**
     * Updates the current act with a status and error message.
     *
     * @param status the act status
     * @param error  the error message
     */
    private void error(String status, String error) {
        if (error != null && error.length() > MAX_ERROR_LENGTH) {
            error = error.substring(0, MAX_ERROR_LENGTH);
        }
        Date now = new Date();
        try {
            currentAct.setActivityEndTime(now);
            currentAct.setStatus(status);
            ActBean bean = new ActBean(currentAct, service);
            bean.setValue("error", error);
            bean.save();
            log.error("Error received from " + connector + ":" + error);
        } finally {
            completed(now, error);
        }
    }

    /**
     * Sets an act status to {@link HL7ActStatuses#ERROR} and sets the error node to that supplied.
     *
     * @param act     the act
     * @param message the error message
     */
    private void error(DocumentAct act, String message) {
        act.setStatus(HL7ActStatuses.ERROR);
        ActBean bean = new ActBean(act);
        bean.setValue("error", message);
        bean.save();
    }

    /**
     * Appends values to a buffer, prepended by a new-line if the buffer is not empty.
     *
     * @param buffer the buffer
     */
    private void append(StringBuilder buffer, String... values) {
        if (buffer.length() != 0) {
            buffer.append("\n");
        }
        for (String value : values) {
            buffer.append(value);
        }
    }

    /**
     * Formats a message for logging.
     *
     * @param message the message
     * @return the formatted message
     * @throws HL7Exception if the message cannot be encoded
     */
    private String format(Message message) throws HL7Exception {
        return message.encode().replaceAll("\r", "\n");
    }

}
