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
import ca.uhn.hl7v2.ErrorCode;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v25.message.RDE_O11;
import ca.uhn.hl7v2.protocol.ReceivingApplication;
import ca.uhn.hl7v2.protocol.ReceivingApplicationException;
import ca.uhn.hl7v2.util.StandardSocketFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.hl7.io.Connector;
import org.openvpms.hl7.io.MessageService;
import org.openvpms.hl7.io.Statistics;
import org.openvpms.hl7.util.HL7MessageStatuses;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests the {@link MessageDispatcherImpl}.
 *
 * @author Tim Anderson
 */
public class MessageDispatcherImplTestCase extends ArchetypeServiceTest {

    /**
     * The first sender.
     */
    private MLLPSender sender1;

    /**
     * The second sender.
     */
    private MLLPSender sender2;

    /**
     * The message context.
     */
    private HapiContext context;

    /**
     * The dispatcher.
     */
    private TestMessageDispatcher dispatcher;

    /**
     * The user.
     */
    private User user;

    /**
     * The message configuration.
     */
    private HL7Mapping config = new HL7Mapping();

    /**
     * The socket factory.
     */
    private TestSocketFactory factory;

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        context = HapiContextFactory.create();
        factory = new TestSocketFactory();
        context.setSocketFactory(factory);
        user = TestHelper.createUser();
        context.setSocketFactory(factory);
        ConnectorsImpl connectors = new ConnectorsImpl(getArchetypeService()) {

            @Override
            public List<Connector> getConnectors() {
                return new ArrayList<>();
            }

            @Override
            protected void load() {
                // do nothing - don't want to pick up existing connectors
            }
        };

        PracticeRules rules = new PracticeRules(getArchetypeService(), null) {
            @Override
            public User getServiceUser(Party practice) {
                return user;
            }
        };

        MessageService messageService = new MessageServiceImpl(getArchetypeService());
        dispatcher = new TestMessageDispatcher(messageService, connectors, rules, context);
        dispatcher.afterPropertiesSet();
    }

    /**
     * Cleans up after the test case.
     */
    @After
    public void tearDown() throws Exception {
        dispatcher.destroy();
        if (sender1 != null) {
            HL7TestHelper.disable(sender1);
        }
        if (sender2 != null) {
            HL7TestHelper.disable(sender2);
        }
    }

    /**
     * Tests sending messages.
     *
     * @throws Exception for any error
     */
    @Test
    public void testSend() throws Exception {
        final int count = 10;

        sender1 = HL7TestHelper.createSender(-1, HL7TestHelper.createCubexMapping());

        List<DocumentAct> queued = new ArrayList<>();
        checkQueued(0, sender1);

        for (int i = 0; i < count; i++) {
            DocumentAct message = dispatcher.queue(HL7TestHelper.createOrder(context), sender1, config, user);
            assertEquals(HL7MessageStatuses.PENDING, message.getStatus());
            queued.add(message);
        }

        // wait for the messages to be sent
        if (!dispatcher.waitForMessages(count)) {
            fail("Failed to receive " + count + " messages");
        }

        checkQueued(0, sender1);

        // make sure the expected no. of messages were sent, in the correct order
        List<DocumentAct> processed = dispatcher.getProcessed();
        assertEquals(count, processed.size());
        for (int i = 0; i < queued.size(); ++i) {
            DocumentAct act = queued.get(i);
            checkStatus(act, HL7MessageStatuses.ACCEPTED);
            assertEquals(act, processed.get(i));
        }
    }

    /**
     * Verifies that messages are queued but not sent to a suspended {@link MLLPSender}.
     *
     * @throws Exception for any error
     */
    @Test
    public void testSuspendSender() throws Exception {
        final int count = 10;

        List<DocumentAct> queued = new ArrayList<>();
        sender1 = HL7TestHelper.createSender(-1, HL7TestHelper.createCubexMapping());
        DocumentAct message = dispatcher.queue(HL7TestHelper.createOrder(context), sender1, config, user);
        assertEquals(HL7MessageStatuses.PENDING, message.getStatus());
        queued.add(message);

        assertTrue(dispatcher.waitForMessages(1));

        HL7TestHelper.suspend(sender1, true); // now suspend sends for the sender

        for (int i = 0; i < count - 1; i++) {
            message = dispatcher.queue(HL7TestHelper.createOrder(context), sender1, config, user);
            assertEquals(HL7MessageStatuses.PENDING, message.getStatus());
            queued.add(message);
        }

        // verify the messages are queued
        checkQueued(count - 1, sender1);
        checkErrors(0, sender1);

        // now enable the connector
        HL7TestHelper.suspend(sender1, false);

        // wait for the messages to be sent
        assertTrue(dispatcher.waitForMessages(count - 1));

        checkQueued(0, sender1);

        // make sure the expected no. of messages were sent, in the correct order
        List<DocumentAct> processed = dispatcher.getProcessed();
        assertEquals(count, processed.size());
        for (int i = 0; i < queued.size(); ++i) {
            DocumentAct act = queued.get(i);
            checkStatus(act, HL7MessageStatuses.ACCEPTED);
            assertEquals(act, processed.get(i));
        }
    }

    /**
     * Tests that message sends are resumed on error.
     *
     * @throws Exception for any error
     */
    @Test
    public void testErrorOnSend() throws Exception {
        final int count = 10;

        sender1 = HL7TestHelper.createSender(-1, HL7TestHelper.createCubexMapping());

        List<DocumentAct> queued = new ArrayList<>();
        dispatcher.setExceptionOnSend(true);

        for (int i = 0; i < count; i++) {
            DocumentAct message = dispatcher.queue(HL7TestHelper.createOrder(context), sender1, config, user);
            assertEquals(HL7MessageStatuses.PENDING, message.getStatus());
            queued.add(message);
        }

        // wait for a dispatch attempt
        assertTrue(dispatcher.waitForDispatch());

        // make sure the first message is pending
        checkStatus(queued.get(0), HL7MessageStatuses.PENDING);

        assertEquals("simulated send exception", dispatcher.getStatistics(sender1.getReference()).getErrorMessage());
        checkQueued(count, sender1);

        dispatcher.setExceptionOnSend(false);

        // force the sender to suspend and resume, to remove the delay to resend
        HL7TestHelper.suspend(sender1, true);
        HL7TestHelper.suspend(sender1, false);

        // wait for the messages to be sent
        assertTrue(dispatcher.waitForMessages(count));
        assertNull(dispatcher.getStatistics(sender1.getReference()).getErrorMessage());
    }

    /**
     * Verifies that if a application sends back an application error (AE) acknowledgment, the message is resubmitted.
     */
    @Test
    public void testApplicationError() throws Exception {
        HL7Mapping config = new HL7Mapping();

        sender1 = HL7TestHelper.createSender(-1, HL7TestHelper.createCubexMapping());

        dispatcher.setAcknowledgmentCode(AcknowledgmentCode.AE);
        dispatcher.setAcknowledgmentException(new HL7Exception("simulated application exception"));

        DocumentAct message = dispatcher.queue(HL7TestHelper.createOrder(context), sender1, config, user);

        // wait for a dispatch attempt
        assertTrue(dispatcher.waitForDispatch());

        // make sure the message is still pending
        checkStatus(message, HL7MessageStatuses.PENDING);

        assertEquals("HL7 Error Code: 207 - Application internal error\n" +
                     "Original Text: simulated application exception",
                     dispatcher.getStatistics(sender1.getReference()).getErrorMessage());
        checkQueued(1, sender1);
        checkErrors(0, sender1);

        dispatcher.setAcknowledgmentCode(AcknowledgmentCode.AA); // now flag to accept messages
        dispatcher.setAcknowledgmentException(null);

        // force the sender to suspend and resume, to remove the delay to resend
        HL7TestHelper.suspend(sender1, true);
        HL7TestHelper.suspend(sender1, false);

        // wait for the messages to be sent
        assertTrue(dispatcher.waitForMessage());
        assertNull(dispatcher.getStatistics(sender1.getReference()).getErrorMessage());
    }

    /**
     * Verifies that if a application sends back an application reject (AE) acknowledgment, the message status
     * is set to {@link HL7MessageStatuses#ERROR}.
     */
    @Test
    public void testApplicationReject() throws Exception {
        dispatcher.setAcknowledgmentCode(AcknowledgmentCode.AR);
        dispatcher.setAcknowledgmentException(new HL7Exception("simulated application reject",
                                                               ErrorCode.UNSUPPORTED_MESSAGE_TYPE));

        sender1 = HL7TestHelper.createSender(-1, HL7TestHelper.createCubexMapping());
        DocumentAct message = dispatcher.queue(HL7TestHelper.createOrder(context), sender1, config, user);

        // wait for a dispatch attempt
        assertTrue(dispatcher.waitForDispatch());

        // make sure the message has been rejected
        checkStatus(message, HL7MessageStatuses.ERROR);

        assertEquals("HL7 Error Code: 200 - Unsupported message type\n" +
                     "Original Text: simulated application reject",
                     dispatcher.getStatistics(sender1.getReference()).getErrorMessage());
        checkQueued(0, sender1);
        checkErrors(1, sender1);
    }

    /**
     * Verifies that two receivers can receive messages from the same socket connection.
     */
    @Test
    public void testMultiplex() throws Exception {
        dispatcher.setSimulateSend(false);
        MLLPReceiver receiver1 = HL7TestHelper.createReceiver(0, "OpenVPMS", "MainClinic");
        MLLPReceiver receiver2 = HL7TestHelper.createReceiver(0, "OpenVPMS", "BranchClinic");

        ReceivingApplication app = new ReceivingApplication() {
            @Override
            public Message processMessage(Message message, Map<String, Object> theMetadata)
                    throws ReceivingApplicationException, HL7Exception {
                try {
                    return message.generateACK();
                } catch (IOException exception) {
                    throw new ReceivingApplicationException(exception);
                }
            }

            @Override
            public boolean canProcess(Message message) {
                return message instanceof RDE_O11;
            }
        };
        dispatcher.listen(receiver1, app, user);
        dispatcher.listen(receiver2, app, user);
        dispatcher.start();

        int port = getPort();

        sender1 = HL7TestHelper.createSender(port, HL7TestHelper.createCubexMapping(), "Cubex", "Cubex",
                                             "OpenVPMS", "MainClinic");
        sender2 = HL7TestHelper.createSender(port, HL7TestHelper.createCubexMapping(), "Cubex", "Cubex",
                                             "OpenVPMS", "BranchClinic");
        MLLPSender sender3 = HL7TestHelper.createSender(port, HL7TestHelper.createIDEXXMapping(), "Cubex", "Cubex",
                                                        "OpenVPMS", "InvalidClinic");
        DocumentAct message1 = dispatcher.queue(HL7TestHelper.createOrder(context), sender1, config, user);

        assertTrue(dispatcher.waitForDispatch());
        assertTrue(dispatcher.waitForMessage());
        checkStatus(message1, HL7MessageStatuses.ACCEPTED);

        DocumentAct message2 = dispatcher.queue(HL7TestHelper.createOrder(context), sender2, config, user);
        assertTrue(dispatcher.waitForDispatch());
        assertTrue(dispatcher.waitForMessage());
        checkStatus(message2, HL7MessageStatuses.ACCEPTED);

        // message sent via sender3 should fail as there is no receiver for it
        DocumentAct message3 = dispatcher.queue(HL7TestHelper.createOrder(context), sender3, config, user);
        assertTrue(dispatcher.waitForDispatch());
        checkStatus(message3, HL7MessageStatuses.ERROR);
        assertEquals("HL7 Error Code: 207 - Application internal error\n" +
                     "Original Text: No appropriate destination could be found to which this message could be routed.",
                     dispatcher.getStatistics(sender3.getReference()).getErrorMessage());
        HL7TestHelper.disable(sender3);
    }

    /**
     * Returns the port used by the receiver.
     * <p/>
     * Note that the receiver must have been created with a port of {@code 0}, and the dispatcher started
     *
     * @return the port
     */
    private int getPort() {
        // there should only be one socket created
        List<ServerSocket> sockets = factory.getSockets();
        assertEquals(1, sockets.size());
        return sockets.get(0).getLocalPort();
    }

    /**
     * Verifies the expected number of messages are queued to a connector.
     *
     * @param expected  the expected no. of messages
     * @param connector the connector
     */
    private void checkQueued(int expected, MLLPSender connector) {
        Statistics statistics = dispatcher.getStatistics(connector.getReference());
        assertNotNull(statistics);
        assertEquals(expected, statistics.getQueued());
    }

    /**
     * Verifies the expected number of messages are in the error queue for a connector.
     *
     * @param expected  the expected no. of messages
     * @param connector the connector
     */
    private void checkErrors(int expected, MLLPSender connector) {
        Statistics statistics = dispatcher.getStatistics(connector.getReference());
        assertNotNull(statistics);
        assertEquals(expected, statistics.getErrors());
    }

    /**
     * Verifies a message has the expected status.
     *
     * @param message the message
     * @param status  the expected status
     */
    private void checkStatus(DocumentAct message, String status) {
        message = get(message);
        assertEquals(status, message.getStatus());
    }

    private static class TestSocketFactory extends StandardSocketFactory {

        private List<ServerSocket> sockets = new ArrayList<>();

        public List<ServerSocket> getSockets() {
            return sockets;
        }

        @Override
        public ServerSocket createServerSocket() throws IOException {
            ServerSocket socket = super.createServerSocket();
            sockets.add(socket);
            return socket;
        }
    }

}
