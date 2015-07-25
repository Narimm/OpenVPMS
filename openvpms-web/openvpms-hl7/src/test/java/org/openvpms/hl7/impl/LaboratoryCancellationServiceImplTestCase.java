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

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v25.message.ACK;
import ca.uhn.hl7v2.model.v25.message.ORM_O01;
import org.junit.Test;
import org.mockito.Mockito;
import org.openvpms.archetype.rules.finance.order.OrderArchetypes;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.user.UserRules;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.hl7.io.Connector;
import org.openvpms.hl7.io.Connectors;
import org.openvpms.hl7.io.MessageDispatcher;
import org.openvpms.hl7.laboratory.Laboratories;
import org.openvpms.hl7.util.HL7Archetypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link LaboratoryCancellationServiceImpl}.
 *
 * @author Tim Anderson
 */
public class LaboratoryCancellationServiceImplTestCase extends AbstractMessageTest {

    /**
     * Verifies that a valid ORM cancel message generates an <em>act.customerReturnInvestigation</em>.
     *
     * @throws Exception for any error
     */
    @Test
    public void testProcessMessage() throws Exception {
        ORM_O01 orm = createCancelORM();
        User user = TestHelper.createUser();
        final Entity laboratory = (Entity) create(HL7Archetypes.LABORATORY);
        EntityBean bean = new EntityBean(laboratory);
        bean.addNodeTarget("user", user);
        Party location = getContext().getLocation();
        bean.addNodeTarget("location", location);

        Connectors connectors = Mockito.mock(Connectors.class);
        Laboratories labs = createLaboratories(laboratory);
        MessageDispatcher dispatcher = Mockito.mock(MessageDispatcher.class);
        PatientRules rules = new PatientRules(null, getArchetypeService(), getLookupService());
        UserRules userRules = new UserRules(getArchetypeService());
        final Connector receiver = new MLLPReceiver(10001, "IVLS", "IDEXX", "VPMS", "VPMS",
                                                    new IMObjectReference(HL7Archetypes.MLLP_RECEIVER, -1),
                                                    new HL7Mapping());
        final List<Act> cancel = new ArrayList<>();
        LaboratoryCancellationServiceImpl service = new LaboratoryCancellationServiceImpl(
                labs, dispatcher, connectors, getArchetypeService(), rules, userRules) {

            @Override
            protected List<Act> process(ORM_O01 message, IMObjectReference location) throws HL7Exception {
                List<Act> acts = super.process(message, location);
                cancel.addAll(acts);
                return acts;
            }

            protected List<Connector> getConnectors() {
                return Collections.singletonList(receiver);
            }

        };
        assertTrue(service.canProcess(orm));
        log("ORM: ", orm);
        Message response = service.processMessage(orm, receiver.getReference());
        assertTrue(response instanceof ACK);
        ACK ack = (ACK) response;
        assertEquals("AA", ack.getMSA().getAcknowledgmentCode().getValue());
        assertEquals(2, cancel.size());
        assertTrue(TypeHelper.isA(cancel.get(0), OrderArchetypes.INVESTIGATION_RETURN));
        assertTrue(TypeHelper.isA(cancel.get(1), OrderArchetypes.INVESTIGATION_RETURN_ITEM));
        ActBean orderBean = new ActBean(cancel.get(0));
        assertEquals(location.getObjectReference(), orderBean.getNodeParticipantRef("location"));
    }

    /**
     * Creates a {@link Laboratories} instance that returns the supplied laboratory.
     *
     * @param laboratory the laboratory
     * @return a new {@link Laboratories}
     */
    protected Laboratories createLaboratories(final Entity laboratory) {
        return new Laboratories() {

            public List<Entity> getServices() {
                return Collections.singletonList(laboratory);
            }

            @Override
            public Entity getService(IMObjectReference reference) {
                return laboratory;
            }

            @Override
            public Entity getService(Entity group, IMObjectReference location) {
                return null;
            }

            @Override
            public Connector getSender(Entity laboratory) {
                return null;
            }

            @Override
            public void addListener(Listener listener) {
            }

            @Override
            public void removeListener(Listener listener) {
            }
        };
    }

    private ORM_O01 createCancelORM() {
        HapiContext hapiContext = HapiContextFactory.create();

        IArchetypeService service = getArchetypeService();
        ORMMessageFactory factory = new ORMMessageFactory(hapiContext, service, getLookupService());
        Entity idexxMapping = HL7TestHelper.createIDEXXMapping();
        return factory.cancelOrder(getContext(), 1, "1234567", new Date(), HL7Mapping.create(idexxMapping, service));
    }
}
