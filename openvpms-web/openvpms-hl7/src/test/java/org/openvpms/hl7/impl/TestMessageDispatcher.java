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
import org.openvpms.hl7.io.Connector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Enter description.
 *
 * @author Tim Anderson
 */
public class TestMessageDispatcher extends MessageDispatcherImpl {

    /**
     * The queued messages.
     */
    private List<Message> messages = new ArrayList<Message>();

    private Date timestamp;

    private long sequence = -1;

    private Semaphore semaphore = new Semaphore(0);

    /**
     * Constructs an {@link TestMessageDispatcher}.
     */
    public TestMessageDispatcher() {
        super(Mockito.mock(ConnectorsImpl.class));
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

    public void setSequence(long sequence) {
        this.sequence = sequence;
    }

    @Override
    protected Message send(Message message, MLLPSender sender) throws HL7Exception, LLPException, IOException {
        messages.add(message);
        semaphore.release();
        return message.generateACK();
    }


    @Override
    protected Date getTimestamp() {
        return timestamp != null ? timestamp : super.getTimestamp();
    }

    @Override
    protected long getSequence(Connector connector) {
        return sequence != -1 ? sequence : super.getSequence(connector);
    }

}
