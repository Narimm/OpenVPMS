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
import ca.uhn.hl7v2.ErrorCode;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.hl7.io.Connector;
import org.openvpms.hl7.io.MessageService;
import org.openvpms.hl7.io.Statistics;
import org.openvpms.hl7.util.HL7MessageStatuses;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
     * The sender.
     */
    private MLLPSender sender;

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
    private MessageConfig config = new MessageConfig();

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        sender = HL7TestHelper.createSender(-1); // dummy port
        context = HapiContextFactory.create();
        user = TestHelper.createUser();
        ConnectorsImpl connectors = new ConnectorsImpl(getArchetypeService()) {

            @Override
            public List<Connector> getConnectors() {
                return Arrays.<Connector>asList(sender);
            }

            @Override
            public Connector getConnector(IMObjectReference reference) {
                return sender;
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
        dispatcher = new TestMessageDispatcher(messageService, connectors, rules);
        dispatcher.afterPropertiesSet();
    }

    /**
     * Cleans up after the test case.
     */
    @After
    public void tearDown() throws Exception {
        dispatcher.destroy();
        HL7TestHelper.disable(sender);
    }

    /**
     * Tests sending messages.
     *
     * @throws Exception for any error
     */
    @Test
    public void testSend() throws Exception {
        final int count = 10;

        List<DocumentAct> queued = new ArrayList<DocumentAct>();
        checkQueued(0, sender);

        for (int i = 0; i < count; i++) {
            DocumentAct message = dispatcher.queue(HL7TestHelper.createOrder(context), sender, config, user);
            assertEquals(HL7MessageStatuses.PENDING, message.getStatus());
            queued.add(message);
        }

        // wait for the messages to be sent
        if (!dispatcher.waitForMessages(count)) {
            fail("Failed to receive " + count + " messages");
        }

        checkQueued(0, sender);

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

        List<DocumentAct> queued = new ArrayList<DocumentAct>();
        DocumentAct message = dispatcher.queue(HL7TestHelper.createOrder(context), sender, config, user);
        assertEquals(HL7MessageStatuses.PENDING, message.getStatus());
        queued.add(message);

        assertTrue(dispatcher.waitForMessage());

        HL7TestHelper.suspend(sender, true); // now suspend sends for the sender

        for (int i = 0; i < count - 1; i++) {
            message = dispatcher.queue(HL7TestHelper.createOrder(context), sender, config, user);
            assertEquals(HL7MessageStatuses.PENDING, message.getStatus());
            queued.add(message);
        }

        // verify the messages are queued
        checkQueued(count - 1, sender);
        checkErrors(0, sender);

        // now enable the connector
        HL7TestHelper.suspend(sender, false);

        // wait for the messages to be sent
        assertTrue(dispatcher.waitForMessages(count - 1));

        checkQueued(0, sender);

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

        List<DocumentAct> queued = new ArrayList<DocumentAct>();
        dispatcher.setExceptionOnSend(true);

        for (int i = 0; i < count; i++) {
            DocumentAct message = dispatcher.queue(HL7TestHelper.createOrder(context), sender, config, user);
            assertEquals(HL7MessageStatuses.PENDING, message.getStatus());
            queued.add(message);
        }

        // wait for a dispatch attempt
        assertTrue(dispatcher.waitForDispatch());

        // make sure the first message is pending
        checkStatus(queued.get(0), HL7MessageStatuses.PENDING);

        assertEquals("simulated send exception", dispatcher.getStatistics(sender.getReference()).getErrorMessage());
        checkQueued(count, sender);

        dispatcher.setExceptionOnSend(false);

        // force the sender to suspend and resume, to remove the delay to resend
        HL7TestHelper.suspend(sender, true);
        HL7TestHelper.suspend(sender, false);

        // wait for the messages to be sent
        assertTrue(dispatcher.waitForMessages(count));
        assertNull(dispatcher.getStatistics(sender.getReference()).getErrorMessage());
    }

    /**
     * Verifies that if a application sends back an application error (AE) acknowledgment, the message is resubmitted.
     */
    @Test
    public void testApplicationError() throws Exception {
        MessageConfig config = new MessageConfig();

        dispatcher.setAcknowledgmentCode(AcknowledgmentCode.AE);
        dispatcher.setAcknowledgmentException(new HL7Exception("simulated application exception"));

        DocumentAct message = dispatcher.queue(HL7TestHelper.createOrder(context), sender, config, user);

        // wait for a dispatch attempt
        assertTrue(dispatcher.waitForDispatch());

        // make sure the message is still pending
        checkStatus(message, HL7MessageStatuses.PENDING);

        assertEquals("HL7 Error Code: 207 - Application internal error\n" +
                     "Original Text: simulated application exception",
                     dispatcher.getStatistics(sender.getReference()).getErrorMessage());
        checkQueued(1, sender);
        checkErrors(0, sender);

        dispatcher.setAcknowledgmentCode(AcknowledgmentCode.AA); // now flag to accept messages
        dispatcher.setAcknowledgmentException(null);

        // force the sender to suspend and resume, to remove the delay to resend
        HL7TestHelper.suspend(sender, true);
        HL7TestHelper.suspend(sender, false);

        // wait for the messages to be sent
        assertTrue(dispatcher.waitForMessage());
        assertNull(dispatcher.getStatistics(sender.getReference()).getErrorMessage());
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

        DocumentAct message = dispatcher.queue(HL7TestHelper.createOrder(context), sender, config, user);

        // wait for a dispatch attempt
        assertTrue(dispatcher.waitForDispatch());

        // make sure the message has been rejected
        checkStatus(message, HL7MessageStatuses.ERROR);

        assertEquals("HL7 Error Code: 200 - Unsupported message type\n" +
                     "Original Text: simulated application reject",
                     dispatcher.getStatistics(sender.getReference()).getErrorMessage());
        checkQueued(0, sender);
        checkErrors(1, sender);
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

}
