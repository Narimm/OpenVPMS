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

import ca.uhn.hl7v2.AcknowledgmentCode;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.app.HL7Service;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v25.datatype.DTM;
import ca.uhn.hl7v2.model.v25.datatype.HD;
import ca.uhn.hl7v2.model.v25.message.ACK;
import ca.uhn.hl7v2.model.v25.segment.MSA;
import ca.uhn.hl7v2.model.v25.segment.MSH;
import ca.uhn.hl7v2.protocol.ReceivingApplication;
import ca.uhn.hl7v2.protocol.ReceivingApplicationException;
import ca.uhn.hl7v2.protocol.ReceivingApplicationExceptionHandler;
import ca.uhn.hl7v2.protocol.Transportable;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.security.RunAs;
import org.openvpms.hl7.io.Connector;
import org.openvpms.hl7.io.MessageService;
import org.openvpms.hl7.io.Statistics;
import org.openvpms.hl7.util.HL7MessageStatuses;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Listener for HL7 messages.
 * <p/>
 * TODO: timezones and milliseconds are included in messages if the receiver throws an exception or a nak is generated
 * by ApplicationRouterImpl.
 *
 * @author Tim Anderson
 */
class MessageReceiver implements ReceivingApplication, ReceivingApplicationExceptionHandler, Statistics {

    /**
     * The connector.
     */
    private final Connector connector;

    /**
     * The receiver to handle messages.
     */
    private final ReceivingApplication receiver;

    /**
     * The message service, to log messages.
     */
    private final MessageService service;

    /**
     * The user responsible for messages received via the connector.
     */
    private final User user;

    /**
     * Message configuration, used to format responses correctly.
     */
    private final HL7Mapping mapping;

    /**
     * The service responsible for delegating messages to this.
     */
    private final HL7Service hl7Service;

    /**
     * The timestamp of the last received message.
     */
    private Date lastReceived;

    /**
     * The time of the last failure, or {@code null} if the last receive was successful.
     */
    private Date lastError;

    /**
     * The error message, if processing the last message was unsuccessful.
     */
    private String lastErrorMessage;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(MessageReceiver.class);

    /**
     * Key used to store the message act in the meta-data.
     */
    private static String META_DATA_KEY = MessageReceiver.class.getName() + ".act";


    /**
     * Constructs an {@link MessageReceiver}.
     *
     * @param receiver   the receiver to delegate to
     * @param connector  the connector
     * @param service    the message service
     * @param user       the user responsible for messages received the connector
     * @param hl7Service the service responsible for delegating messages to this
     */
    public MessageReceiver(ReceivingApplication receiver, Connector connector, MessageService service, User user,
                           HL7Service hl7Service) {
        this.connector = connector;
        mapping = connector.getMapping();
        this.receiver = receiver;
        this.service = service;
        this.user = user;
        this.hl7Service = hl7Service;
    }

    /**
     * Returns the receiver to delegate messages to.
     *
     * @return the receiver
     */
    public ReceivingApplication getReceivingApplication() {
        return receiver;
    }

    /**
     * Returns the user responsible for messages received by the connector.
     *
     * @return the user
     */
    public User getUser() {
        return user;
    }

    /**
     * Uses the contents of the message for whatever purpose the application has for this message, and returns an
     * appropriate response message.
     *
     * @param message  an inbound HL7 message
     * @param metaData message metadata (which may include information about where the message comes from, etc).  This
     *                 is the same metadata as in {@link Transportable#getMetadata()}.
     * @return an appropriate application response
     * @throws ReceivingApplicationException if there is a problem internal to the application (for example a database
     *                                       problem)
     * @throws HL7Exception                  if there is a problem with the message
     */
    @Override
    public Message processMessage(final Message message, final Map<String, Object> metaData)
            throws ReceivingApplicationException, HL7Exception {
        Message response;
        MSH msh = getMSH(message);
        check("MSH-3: Sending Application", connector.getSendingApplication(), msh.getSendingApplication());
        check("MSH-4: Sending Facility", connector.getSendingFacility(), msh.getSendingFacility());
        check("MSH-5: Receiving Application", connector.getReceivingApplication(), msh.getReceivingApplication());
        check("MSH-6: Receiving Facility", connector.getReceivingFacility(), msh.getReceivingFacility());
        Callable<Message> callable = new Callable<Message>() {
            @Override
            public Message call() throws Exception {
                return process(message, metaData);
            }
        };
        try {
            response = RunAs.run(user, callable);
        } catch (HL7Exception | ReceivingApplicationException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new HL7Exception(exception);
        }
        return response;
    }

    /**
     * Determines if the receiver can process a message.
     *
     * @param theMessage an inbound HL7 message
     * @return true if this ReceivingApplication wishes to accept the message
     */
    @Override
    public boolean canProcess(Message theMessage) {
        return receiver.canProcess(theMessage);
    }

    /**
     * Process an exception.
     *
     * @param incomingMessage  the incoming message. This is the raw message which was received from the external
     *                         system
     * @param incomingMetadata Any metadata that accompanies the incoming message.
     * @param outgoingMessage  the outgoing message. The response NAK message generated by HAPI.
     * @param exception        the exception which was received
     * @return the new outgoing message.
     */
    @Override
    public String processException(String incomingMessage, Map<String, Object> incomingMetadata,
                                   String outgoingMessage, Exception exception) throws HL7Exception {
        DocumentAct act = (DocumentAct) incomingMetadata.get(META_DATA_KEY);
        if (act != null) {
            // the exception is for the message being processed by processMessage()
            error(act, exception);
        } else {
            log.error(exception.getMessage(), exception);
            error(exception.getMessage(), new Date());
        }
        return outgoingMessage;
    }

    /**
     * Returns the number of messages in the queue.
     *
     * @return {@code 0} - the receiver doesn't support queuing
     */
    @Override
    public int getQueued() {
        return 0;
    }

    /**
     * Returns the number of messages in the error queue.
     *
     * @return {@code 0} - the receiver doesn't support queuing
     */
    @Override
    public int getErrors() {
        return 0;
    }

    /**
     * Determines if the connector is running.
     *
     * @return {@code true} if the connector is running
     */
    public boolean isRunning() {
        return hl7Service.isRunning();
    }

    /**
     * Returns the time when a message was last successfully received or sent.
     *
     * @return the time when a message was last successfully sent, or {@code null} if none have been sent
     */
    @Override
    public synchronized Date getProcessedTimestamp() {
        return lastReceived;
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
     * Returns the last error message, if the last send was unsuccessful.
     *
     * @return the last error message. May be {@code null}
     */
    @Override
    public synchronized String getErrorMessage() {
        return lastErrorMessage;
    }

    /**
     * Returns the connector used to send messages via this queue.
     *
     * @return the connector
     */
    @Override
    public Connector getConnector() {
        return connector;
    }

    /**
     * Checks a header field against its expected value.
     *
     * @param name     the header field name
     * @param expected the expected value
     * @param actual   the actual value
     * @throws HL7Exception if they don't match
     */
    private void check(String name, String expected, HD actual) throws HL7Exception {
        String value = actual.getNamespaceID().getValue();
        if (!ObjectUtils.equals(expected, value)) {
            log.error("Unrecognised value for " + name + ": '" + value + "'");
            throw new HL7Exception("Unrecognised application details");
        }
    }

    /**
     * Processes a message.
     *
     * @param message  an inbound HL7 message
     * @param metaData message metadata
     * @return an appropriate application response
     * @throws ReceivingApplicationException if there is a problem internal to the application
     * @throws HL7Exception                  if there is a problem with the message
     */
    private Message process(Message message, Map<String, Object> metaData)
            throws HL7Exception, ReceivingApplicationException {
        DocumentAct act = service.save(message, connector, user);
        metaData.put(META_DATA_KEY, act);
        Message response;
        try {
            response = receiver.processMessage(message, metaData);
            if (!mapping.includeMillis() || !mapping.includeTimeZone()) {
                // correct the date/time format
                try {
                    MSH msh = getMSH(response);
                    DTM time = msh.getDateTimeOfMessage().getTime();
                    Calendar calendar = time.getValueAsCalendar();
                    PopulateHelper.populateDTM(time, calendar, mapping);
                } catch (HL7Exception ignore) {
                    // do nothing
                }
            }
            if (isAccepted(response)) {
                service.accepted(act, new Date());
                processed();
            } else {
                error(act, getError(response));
            }
        } catch (ReceivingApplicationException | HL7Exception exception) {
            error(act, exception);
            throw exception;
        } catch (Throwable exception) {
            error(act, exception);
            throw new ReceivingApplicationException(exception);
        }
        return response;
    }

    /**
     * Returns the message header segment.
     *
     * @param message the message
     * @return the message header segment
     * @throws HL7Exception if the message has no MSH
     */
    private MSH getMSH(Message message) throws HL7Exception {
        return (MSH) message.get("MSH");
    }

    /**
     * Invoked when a received message cannot be processed due to an exception.
     *
     * @param act       the persistent version of the message
     * @param exception the exception
     */
    private void error(DocumentAct act, Throwable exception) {
        String message = exception.getMessage();
        log.error(message, exception);
        error(act, message);
    }

    /**
     * Invoked when a received message cannot be processed.
     *
     * @param act     the persistent version of the message
     * @param message the error message
     */
    private void error(DocumentAct act, String message) {
        Date timestamp = new Date();
        service.error(act, HL7MessageStatuses.ERROR, timestamp, message);
        error(message, timestamp);
    }

    /**
     * Invoked when a message is successfully processed.
     */
    private synchronized void processed() {
        lastReceived = new Date();
        lastError = null;
        lastErrorMessage = null;
    }

    /**
     * Invoked when a message cannot be processed,
     *
     * @param message   the error message
     * @param timestamp the time the error occurred
     */
    private synchronized void error(String message, Date timestamp) {
        lastErrorMessage = message;
        lastError = timestamp;
    }

    /**
     * Determines if a message was accepted.
     *
     * @param response the response to check
     * @return {@code true} if the message was accepted
     */
    private boolean isAccepted(Message response) {
        boolean result = false;
        if (response instanceof ACK) {
            ACK ack = (ACK) response;
            MSA msa = ack.getMSA();
            String ackCode = msa.getAcknowledgmentCode().getValue();
            if (AcknowledgmentCode.AA.toString().equals(ackCode)) {
                result = true;
            }
        }
        return result;
    }

    /**
     * Helper to build an error message from a response message.
     *
     * @param response the response message
     * @return the error message
     */
    private String getError(Message response) {
        if (response instanceof ACK) {
            return HL7MessageHelper.getErrorMessage(response);
        }
        return "Unknown error";
    }

}
