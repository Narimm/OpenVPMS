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
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.hl7.Connector;
import org.openvpms.hl7.MLLPSender;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Enter description.
 *
 * @author Tim Anderson
 */
public abstract class AbstractServiceTest extends AbstractMessageTest {

    /**
     * The connectors.
     */
    private Connectors connectors;

    /**
     * The connector manager.
     */
    private TestMessageDispatcher dispatcher;

    /**
     * The sender.
     */
    private MLLPSender sender;


    /**
     * Sets up the test case.
     */
    @Override
    public void setUp() {
        super.setUp();

        sender = new MLLPSender("dummy", 2026, "VPMS", "Main Clinic", "Cubex", "Cubex");

        connectors = new Connectors() {

            @Override
            public Connector getConnector(IMObjectReference reference) {
                return sender;
            }

            @Override
            public List<Connector> getSenders(Party location) {
                return Arrays.<Connector>asList(sender);
            }

        };
        dispatcher = new TestMessageDispatcher();
        dispatcher.setTimestamp(TestHelper.getDatetime("2014-08-25 08:59:00"));
        dispatcher.setSequence(1200022);
        dispatcher.addListener(new ConnectorManagerListener() {
            @Override
            public void sent(Message message, Message response) {
                log("sent: ", message);
                log("received: ", response);
            }
        });
    }

    protected Connectors getConnectors() {
        return connectors;
    }

    protected Connector getSender() {
        return sender;
    }

    protected TestMessageDispatcher getDispatcher() {
        return dispatcher;
    }

    protected void checkMessage(String expected) throws HL7Exception {
        List<Message> messages = dispatcher.getMessages();
        assertEquals(1, messages.size());

        String encode = messages.get(0).encode();
        assertEquals(expected, encode);
    }


}
