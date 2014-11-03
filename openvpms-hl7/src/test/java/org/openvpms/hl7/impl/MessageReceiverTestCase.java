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
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v25.message.ACK;
import ca.uhn.hl7v2.protocol.ReceivingApplication;
import ca.uhn.hl7v2.protocol.ReceivingApplicationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.hl7.io.Connector;
import org.openvpms.hl7.io.MessageService;
import org.openvpms.hl7.util.HL7MessageStatuses;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests the {@link MessageReceiver}.
 *
 * @author Tim Anderson
 */
public class MessageReceiverTestCase extends AbstractRDSTest {

    /**
     * The receiver.
     */
    private MLLPReceiver connector;

    /**
     * The message service.
     */
    private MessageService service;

    /**
     * Tracks acts saved via the message service.
     */
    private List<DocumentAct> acts;

    /**
     * The user responsible for messages.
     */
    private User user;

    /**
     * Sets up the test case.
     */
    @Before
    @Override
    public void setUp() {
        super.setUp();
        connector = HL7TestHelper.createReceiver(-1);
        user = TestHelper.createUser();

        acts = new ArrayList<DocumentAct>();
        service = new MessageServiceImpl(getArchetypeService()) {
            @Override
            public DocumentAct save(Message message, Connector connector, User user) throws HL7Exception {
                DocumentAct act = super.save(message, connector, user);
                acts.add(act);
                return act;
            }
        };
    }

    /**
     * Cleans up after the test.
     */
    @After
    public void tearDown() {
        HL7TestHelper.disable(connector);
    }

    /**
     * Verifies that the inbound message is made persistent and its status is updated to
     * {@link HL7MessageStatuses#ACCEPTED} when it is successfully processed.
     *
     * @throws Exception for any error
     */
    @Test
    public void testProcessMessage() throws Exception {
        ReceivingApplication application = new ReceivingApplication() {
            @Override
            public Message processMessage(Message theMessage, Map<String, Object> theMetadata)
                    throws ReceivingApplicationException, HL7Exception {
                assertEquals(1, acts.size());
                assertEquals(HL7MessageStatuses.PENDING, acts.get(0).getStatus());
                try {
                    return theMessage.generateACK();
                } catch (IOException exception) {
                    throw new ReceivingApplicationException(exception);
                }
            }

            @Override
            public boolean canProcess(Message theMessage) {
                return true;
            }
        };
        MessageReceiver receiver = new MessageReceiver(application, connector, service, user);
        Message message = createRDS(createProduct());
        assertTrue(receiver.canProcess(message));
        Message response = receiver.processMessage(message, new HashMap<String, Object>());
        assertTrue(response instanceof ACK);

        assertEquals(1, acts.size());
        DocumentAct act = get(acts.get(0)); // reload
        assertNotNull(act);
        assertEquals(HL7MessageStatuses.ACCEPTED, acts.get(0).getStatus());
    }

    /**
     * Verifies that when the receiving application throws an exception, the persistent message status is set to
     * {@link HL7MessageStatuses#ERROR}.
     *
     * @throws Exception for any error
     */
    @Test
    public void testExceptionInReceivingApplication() throws Exception {
        ReceivingApplication application = new ReceivingApplication() {
            @Override
            public Message processMessage(Message theMessage, Map<String, Object> theMetadata)
                    throws ReceivingApplicationException, HL7Exception {
                assertEquals(1, acts.size());
                assertEquals(HL7MessageStatuses.PENDING, acts.get(0).getStatus());
                throw new RuntimeException("Simulated ReceivingApplication Exception");
            }

            @Override
            public boolean canProcess(Message theMessage) {
                return true;
            }
        };
        MessageReceiver receiver = new MessageReceiver(application, connector, service, user);
        Message message = createRDS(createProduct());
        assertTrue(receiver.canProcess(message));
        try {
            receiver.processMessage(message, new HashMap<String, Object>());
            fail("Expected ReceivingApplicationException");
        } catch (ReceivingApplicationException expected) {
            // do nothing
        }

        assertEquals(1, acts.size());
        DocumentAct act = get(acts.get(0)); // reload
        assertNotNull(act);
        assertEquals(HL7MessageStatuses.ERROR, act.getStatus());
        IMObjectBean bean = new IMObjectBean(act);
        assertEquals("Simulated ReceivingApplication Exception", bean.getString("error"));
    }
}
