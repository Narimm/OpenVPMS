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
import ca.uhn.hl7v2.llp.LLPException;
import ca.uhn.hl7v2.model.Message;
import org.mockito.Mockito;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.hl7.io.MessageDispatcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Test implementation of the {@link MessageDispatcher}.
 *
 * @author Tim Anderson
 */
public class TestMessageDispatcher extends MessageDispatcherImpl {

    /**
     * The queued messages.
     */
    private List<Message> messages = new ArrayList<Message>();

    /**
     * The processed messages.
     */
    private List<DocumentAct> acts = new ArrayList<DocumentAct>();

    /**
     * Optional timestamp to assign to messages.
     */
    private Date timestamp;

    /**
     * Optional message control ID to assign to messages.
     */
    private long messageControlID = -1;

    /**
     * Used to wait for message send attempts.
     */
    private Semaphore dispatchSemaphore = new Semaphore(0);

    /**
     * Used to wait for messages to be sent.
     */
    private Semaphore sentSemaphore = new Semaphore(0);

    /**
     * If {@code true}, generate an exception on send.
     */
    private boolean exceptionOnSend;

    /**
     * The acknowledgment code to return in ACK messages.
     */
    private AcknowledgmentCode acknowledgmentCode;

    /**
     * The exception to populate ACK messages with.
     */
    private HL7Exception acknowledgmentException;

    /**
     * Constructs an {@link TestMessageDispatcher} using mock connectors.
     *
     * @param service the archetype service
     * @param user    the user to initialise the security context in the dispatch thread
     */
    public TestMessageDispatcher(IArchetypeService service, final User user) {
        this(Mockito.mock(ConnectorsImpl.class), service, new PracticeRules(service) {
            @Override
            public User getServiceUser(Party practice) {
                return user;
            }
        });
    }

    /**
     * Constructs a {@link TestMessageDispatcher}.
     *
     * @param connectors the connectors
     * @param service    the service
     * @param rules      the practice rules
     */
    public TestMessageDispatcher(ConnectorsImpl connectors, IArchetypeService service, PracticeRules rules) {
        super(connectors, service, rules);
    }

    /**
     * Waits at most 30 seconds for a send attempt.
     *
     * @return {@code true} if an attempt was made to send a message
     */
    public boolean waitForDispatch() {
        return tryAcquire(1, dispatchSemaphore);
    }

    /**
     * Waits at most 30 seconds for a message to be sent.
     *
     * @return {@code true} if a message was sent
     */
    public boolean waitForMessage() {
        return waitForMessages(1);
    }

    /**
     * Waits at most 30 seconds for message to be sent.
     *
     * @param count the no. of messages to wait for
     * @return {@code true} if a message was sent
     */
    public boolean waitForMessages(int count) {
        return tryAcquire(count, sentSemaphore);
    }

    /**
     * Returns the sent messages.
     *
     * @return the sent messages
     */
    public List<Message> getMessages() {
        return messages;
    }

    /**
     * Returns the processed acts.
     *
     * @return the acts
     */
    public List<DocumentAct> getProcessed() {
        return acts;
    }

    /**
     * Overrides the default message timestamp.
     *
     * @param timestamp the timestamp, or {@code null} to use the default
     */
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Overrides the message control ID.
     *
     * @param id the message control ID, or {@code -1} to use the default
     */
    public void setMessageControlID(long id) {
        this.messageControlID = id;
    }

    /**
     * Determines if an exception should be simulated on send.
     *
     * @param exception if {@code true}, throw an exception on send
     */
    public void setExceptionOnSend(boolean exception) {
        exceptionOnSend = exception;
    }

    /**
     * Sets the acknowledgment code to return in ACK messages.
     *
     * @param code the code. May be {@code null} to indicate the default
     */
    public void setAcknowledgmentCode(AcknowledgmentCode code) {
        acknowledgmentCode = code;
    }

    /**
     * Sets the exception to return in negative-ACK messages.
     *
     * @param exception the exception. May be {@code null}
     */
    public void setAcknowledgmentException(HL7Exception exception) {
        acknowledgmentException = exception;
    }

    /**
     * Determines if an application error (AE) ack should be generated.
     */

    /**
     * Sends the first message in a queue, if any are present.
     *
     * @param queue the queue
     * @return {@code true} if there was a message
     */
    @Override
    protected boolean sendFirst(MessageQueue queue) {
        DocumentAct act = queue.peekFirstAct();
        Message message = queue.peekFirst();
        boolean result = super.sendFirst(queue);
        if (act != null) {
            dispatchSemaphore.release();
            acts.add(act);
            messages.add(message);

            if (queue.getErrorTimestamp() == null) {
                sentSemaphore.release();
            }
        }
        return result;
    }

    /**
     * Sends a message, and returns the response.
     *
     * @param message the message to send
     * @param sender  the sender configuration
     * @return the response
     * @throws HL7Exception if the connection can not be initialised for any reason
     * @throws LLPException for any LLP error
     * @throws IOException  for any I/O error
     */
    @Override
    protected Message send(Message message, MLLPSender sender) throws HL7Exception, LLPException, IOException {
        if (exceptionOnSend) {
            throw new IOException("simulated send exception");
        }
        return (acknowledgmentCode == null) ? message.generateACK()
                                            : message.generateACK(acknowledgmentCode, acknowledgmentException);
    }

    /**
     * Creates a new timestamp for MSH-7.
     *
     * @return a new timestamp
     */
    @Override
    protected Date createMessageTimestamp() {
        return timestamp != null ? timestamp : super.createMessageTimestamp();
    }

    /**
     * Creates a new message control ID, for MSH-10.
     *
     * @return a new ID
     * @throws IOException if the ID cannot be generated
     */
    @Override
    protected String createMessageControlID() throws IOException {
        return messageControlID != -1 ? Long.toString(messageControlID) : super.createMessageControlID();
    }

    /**
     * Waits at most 30 seconds for a semaphore to be released.
     *
     * @param count     the no. of permits to acquire
     * @param semaphore the semaphore
     * @return if the permits were acquired
     */
    private boolean tryAcquire(int count, Semaphore semaphore) {
        boolean result = false;
        try {
            result = semaphore.tryAcquire(count, 30, TimeUnit.SECONDS);
        } catch (InterruptedException ignore) {
            // do nothing
        }
        return result;

    }

}
