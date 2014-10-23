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
import ca.uhn.hl7v2.model.v25.message.RDE_O11;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.hl7.util.HL7ActStatuses;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link MessageQueue}.
 *
 * @author Tim Anderson
 */
public class MessageQueueTestCase extends ArchetypeServiceTest {

    /**
     * The sender.
     */
    private MLLPSender sender;

    /**
     * The message context.
     */
    private HapiContext context;


    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        sender = HL7TestHelper.createSender(-1);
        context = HapiContextFactory.create();
    }

    /**
     * Tears down the test.
     */
    @After
    public void tearDown() {
        HL7TestHelper.disable(sender);
    }

    /**
     * Tests queuing.
     *
     * @throws Exception for any error
     */
    @Test
    public void testQueue() throws Exception {
        final int count = 10;
        HL7DocumentHandler handler = new HL7DocumentHandler(getArchetypeService());
        MessageQueue queue = new MessageQueue(sender, getArchetypeService(), handler, context);
        assertFalse(queue.isSuspended());
        assertEquals(0, queue.getQueued());

        List<DocumentAct> acts = new ArrayList<DocumentAct>();
        for (int i = 0; i < count; ++i) {
            RDE_O11 message = createMessage();
            message.getMSH().getMessageControlID().setValue(Integer.toString(i));
            DocumentAct act = queue.add(message);
            acts.add(act);
            assertEquals(HL7ActStatuses.PENDING, act.getStatus());
            assertNotNull(act);
            assertEquals(i + 1, queue.getQueued());
        }

        for (int i = 0; i < count; ++i) {
            Message message = queue.peekFirst();
            assertTrue(message instanceof RDE_O11);
            RDE_O11 order = (RDE_O11) message;
            assertEquals(Integer.toString(i), order.getMSH().getMessageControlID().getValue());
            DocumentAct act = queue.sent(order.generateACK());
            assertEquals(count - i - 1, queue.getQueued());
            assertEquals(HL7ActStatuses.ACCEPTED, act.getStatus());
            assertNotNull(act);
            assertEquals(acts.get(i), act);
        }
    }

    /**
     * Verifies that an application reject (AR) sets the <em>act.HL7Message</em> status to ERROR.
     *
     * @throws Exception for any error
     */
    @Test
    public void testApplicationReject() throws Exception {
        HL7DocumentHandler handler = new HL7DocumentHandler(getArchetypeService());
        MessageQueue queue = new MessageQueue(sender, getArchetypeService(), handler, context);
        assertEquals(0, queue.getQueued());

        RDE_O11 message = createMessage();
        DocumentAct act1 = queue.add(message);

        assertNotNull(queue.peekFirst());
        ACK response = (ACK) message.generateACK(AcknowledgmentCode.AR, null);
        response.getERR().getUserMessage().setValue("Some error");

        DocumentAct act2 = queue.sent(response);
        assertNotNull(act2);
        assertEquals(act1.getId(), act2.getId());
        assertEquals(HL7ActStatuses.ERROR, act2.getStatus());
        ActBean bean = new ActBean(act2);
        assertEquals("User Message: Some error", bean.getString("error"));
    }

    /**
     * Verifies that an application error (AE) leaves the <em>act.HL7Message</em> status as PENDING, but
     * updates the error node.
     *
     * @throws Exception for any error
     */
    @Test
    public void testApplicationError() throws Exception {
        HL7DocumentHandler handler = new HL7DocumentHandler(getArchetypeService());
        MessageQueue queue = new MessageQueue(sender, getArchetypeService(), handler, context);
        assertEquals(0, queue.getQueued());

        RDE_O11 message = createMessage();
        DocumentAct act1 = queue.add(message);

        assertNotNull(queue.peekFirst());
        ACK response = (ACK) message.generateACK(AcknowledgmentCode.AE, null);
        response.getERR().getUserMessage().setValue("Some error");

        DocumentAct act2 = queue.sent(response);
        assertNotNull(act2);
        assertEquals(act1.getId(), act2.getId());
        assertEquals(HL7ActStatuses.PENDING, act2.getStatus());
        ActBean bean = new ActBean(act2);
        assertEquals("User Message: Some error", bean.getString("error"));

        // subsequent send should set the status and clear the error node
        message = (RDE_O11) queue.peekFirst();
        DocumentAct act3 = queue.sent(message.generateACK());
        assertEquals(act1.getId(), act3.getId());
        assertEquals(HL7ActStatuses.ACCEPTED, act3.getStatus());
        bean = new ActBean(act3);
        assertNull(bean.getString("error"));
    }

    /**
     * Verifies that an unsupported response (AR) sets the <em>act.HL7Message</em> status to ERROR.
     *
     * @throws Exception for any error
     */
    @Test
    public void testUnsupportedResponse() throws Exception {
        HL7DocumentHandler handler = new HL7DocumentHandler(getArchetypeService());
        MessageQueue queue = new MessageQueue(sender, getArchetypeService(), handler, context);
        assertEquals(0, queue.getQueued());

        RDE_O11 message = createMessage();
        DocumentAct act1 = queue.add(message);

        assertNotNull(queue.peekFirst());
        RDE_O11 response = createMessage();
        DocumentAct act2 = queue.sent(response);

        assertEquals(act1.getId(), act2.getId());
        assertEquals(HL7ActStatuses.ERROR, act2.getStatus());
        ActBean bean = new ActBean(act2);
        assertEquals("Unsupported response: RDE_O11\nMessage: " + response.encode().replaceAll("\r", "\n"),
                     bean.getString("error"));
    }

    /**
     * Tests the {@link MessageQueue#error(Throwable)} method.
     */
    @Test
    public void testError() throws Exception {
        HL7DocumentHandler handler = new HL7DocumentHandler(getArchetypeService());
        MessageQueue queue = new MessageQueue(sender, getArchetypeService(), handler, context);
        assertEquals(0, queue.getQueued());

        RDE_O11 message = createMessage();
        DocumentAct act1 = queue.add(message);

        assertNull(queue.getErrorMessage());
        assertNull(queue.getErrorTimestamp());
        queue.error(new IOException("Some error"));
        assertEquals("Some error", queue.getErrorMessage());
        assertNotNull(queue.getErrorTimestamp());

        // verify the act isn't changed by the error
        act1 = get(act1);
        ActBean bean = new ActBean(act1);
        assertEquals(HL7ActStatuses.PENDING, act1.getStatus());
        assertNull(bean.getString("error"));

        assertNotNull(queue.peekFirst());
        DocumentAct act2 = queue.sent(message.generateACK());
        assertEquals(act1.getId(), act2.getId());
        assertEquals(HL7ActStatuses.ACCEPTED, act2.getStatus());
    }

    /**
     * Helper to create an RDE_O11 message.
     *
     * @return the new message
     * @throws HL7Exception for any HL7 error
     * @throws IOException  for any I/O error
     */
    private RDE_O11 createMessage() throws HL7Exception, IOException {
        RDE_O11 message = new RDE_O11(context.getModelClassFactory());
        message.setParser(context.getGenericParser());
        message.initQuickstart("RDE", "O11", "P");
        return message;
    }
}
