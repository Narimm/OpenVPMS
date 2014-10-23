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
import org.junit.After;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.hl7.io.Connector;
import org.openvpms.hl7.io.Connectors;
import org.openvpms.hl7.patient.PatientEventServices;
import org.openvpms.hl7.util.HL7Archetypes;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Base class for HL7 service tests.
 *
 * @author Tim Anderson
 */
public abstract class AbstractServiceTest extends AbstractMessageTest {

    /**
     * The connectors.
     */
    private Connectors connectors;

    /**
     * The patient event services.
     */
    private PatientEventServices eventServices;

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

        IMObjectReference senderRef = new IMObjectReference(HL7Archetypes.MLLP_SENDER, -1);
        sender = HL7TestHelper.createSender(-1);    // use an invalid port
        Entity service = (Entity) create(HL7Archetypes.PATIENT_EVENT_SERVICE);
        EntityBean bean = new EntityBean(service);
        bean.addNodeTarget("sender", senderRef);
        bean.addNodeTarget("location", getContext().getLocation());

        connectors = new Connectors() {

            @Override
            public List<Connector> getConnectors() {
                return Arrays.<Connector>asList(sender);
            }

            @Override
            public Connector getConnector(IMObjectReference reference) {
                return sender;
            }
        };
        eventServices = new PatientEventServicesImpl(getArchetypeService(), connectors);
        eventServices.add(service);
        dispatcher = new TestMessageDispatcher(getArchetypeService());
        dispatcher.setTimestamp(TestHelper.getDatetime("2014-08-25 08:59:00"));
        dispatcher.setMessageControlID(1200022);
    }

    @After
    public void tearDown() throws Exception {
        dispatcher.destroy();
        HL7TestHelper.disable(sender);
    }

    /**
     * Returns the connectors.
     *
     * @return the connectors
     */
    protected Connectors getConnectors() {
        return connectors;
    }

    protected Connector getSender() {
        return sender;
    }

    protected PatientEventServices getEventServices() {
        return eventServices;
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
