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

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.llp.LLPException;
import ca.uhn.hl7v2.model.Message;
import org.mockito.Mockito;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.hl7.io.Connectors;
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

    private Date timestamp;

    private long messageControlID = -1;

    private Semaphore semaphore = new Semaphore(0);

    /**
     * Constructs an {@link TestMessageDispatcher}.
     *
     * @param service the archetype service
     */
    public TestMessageDispatcher(IArchetypeService service) {
        super(Mockito.mock(ConnectorsImpl.class), service);
    }

    /**
     * Waits at most {@code time} seconds for a message to be sent.
     *
     * @param time the no. of seconds to wait
     * @return {@code true} if a message was sent
     */
    public boolean waitForMessages(int time) {
        boolean result = false;
        try {
            if (semaphore.tryAcquire(time, TimeUnit.SECONDS)) {
                result = true;
                semaphore.release();
            }
        } catch (InterruptedException ignore) {
            // do nothing
        }
        return result;
    }

    /**
     * Returns the sent messages.
     *
     * @return the sent messages
     */
    public List<Message> getMessages() {
        return messages;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public void setMessageControlID(long id) {
        this.messageControlID = id;
    }

    /**
     * Initialise the connector queues.
     *
     * @param connectors the connectors
     */
    @Override
    protected void initialise(Connectors connectors) {
        // No-op. Don't want to trigger sends for existing connectors
    }

    @Override
    protected Message send(Message message, MLLPSender sender) throws HL7Exception, LLPException, IOException {
        messages.add(message);
        semaphore.release();
        return message.generateACK();
    }


    @Override
    protected Date createMessageTimestamp() {
        return timestamp != null ? timestamp : super.createMessageTimestamp();
    }

    @Override
    protected String createMessageControlID() throws IOException {
        return messageControlID != -1 ? Long.toString(messageControlID) : super.createMessageControlID();
    }

}
