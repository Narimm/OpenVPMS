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
import ca.uhn.hl7v2.model.v25.message.RDS_O13;
import org.junit.Test;
import org.mockito.Mockito;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.user.UserRules;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.business.service.lookup.LookupServiceHelper;
import org.openvpms.hl7.Connector;
import org.openvpms.hl7.pharmacy.Pharmacies;
import org.openvpms.hl7.util.HL7Archetypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link PharmacyDispenseServiceImpl} class.
 *
 * @author Tim Anderson
 */
public class PharmacyDispenseServiceImplTestCase extends AbstractRDSTest {

    /**
     * Verifies that a valid RDS O13 message generates an <em>act.customerPharmacyOrder</em>.
     *
     * @throws Exception for any error
     */
    @Test
    public void testProcessMessage() throws Exception {
        Product product = createProduct();
        RDS_O13 rds = createRDS(product);
        User user = TestHelper.createUser();
        final Entity pharmacy = (Entity) create(HL7Archetypes.PHARMACY);
        EntityBean bean = new EntityBean(pharmacy);
        bean.addNodeTarget("user", user);

        Pharmacies pharmacies = new Pharmacies() {
            public List<Entity> getPharmacies() {
                return Arrays.asList(pharmacy);
            }

            @Override
            public Entity getPharmacy(IMObjectReference reference) {
                return pharmacy;
            }

            @Override
            public Entity getPharmacy(Entity group, IMObjectReference location) {
                return null;
            }

            @Override
            public Connector getOrderConnection(Entity pharmacy) {
                return null;
            }

            @Override
            public void addListener(Listener listener) {
            }

            @Override
            public void removeListener(Listener listener) {
            }
        };
        MessageDispatcher dispatcher = Mockito.mock(MessageDispatcher.class);
        Connectors connectors = Mockito.mock(Connectors.class);
        PatientRules rules = new PatientRules(getArchetypeService(), LookupServiceHelper.getLookupService());
        UserRules userRules = new UserRules(getArchetypeService());
        final Connector receiver = new MLLPReceiver(10001, "Cubex", "Cubex", "VPMS", "VPMS",
                                                    new IMObjectReference(HL7Archetypes.MLLP_RECEIVER, -1));
        final List<Act> order = new ArrayList<Act>();
        PharmacyDispenseServiceImpl service = new PharmacyDispenseServiceImpl(pharmacies, dispatcher, connectors,
                                                                              getArchetypeService(), rules, userRules) {

            @Override
            protected List<Act> process(RDS_O13 message) throws HL7Exception {
                List<Act> acts = super.process(message);
                order.addAll(acts);
                return acts;
            }

            protected List<Connector> getConnectors() {
                return Arrays.asList(receiver);
            }

        };
        assertTrue(service.canProcess(rds));
        log("RDS: ", rds);
        Message response = service.processMessage(rds, new HashMap<String, Object>());
        assertTrue(response instanceof ACK);
        ACK ack = (ACK) response;
        assertEquals("AA", ack.getMSA().getAcknowledgmentCode().getValue());
        assertEquals(2, order.size());
        assertTrue(TypeHelper.isA(order.get(0), "act.customerOrderPharmacy"));
        assertTrue(TypeHelper.isA(order.get(1), "act.customerOrderItemPharmacy"));
    }

    /**
     * Verifies that messages are rejected if the message header has incorrect sending or receiving application or
     * facility details
     *
     * @throws Exception for any error
     */
    @Test
    public void testUnknownSender() throws Exception {
        Product product = createProduct();
        RDS_O13 rds = createRDS(product);

        Pharmacies pharmacies = Mockito.mock(Pharmacies.class);
        MessageDispatcher dispatcher = Mockito.mock(MessageDispatcher.class);
        Connectors connectors = Mockito.mock(Connectors.class);
        PatientRules rules = new PatientRules(getArchetypeService(), LookupServiceHelper.getLookupService());
        UserRules userRules = new UserRules(getArchetypeService());
        PharmacyDispenseServiceImpl service = new PharmacyDispenseServiceImpl(pharmacies, dispatcher, connectors,
                                                                              getArchetypeService(), rules, userRules);
        assertTrue(service.canProcess(rds));
        Message response = service.processMessage(rds, new HashMap<String, Object>());
        assertTrue(response instanceof ACK);
        ACK ack = (ACK) response;
        assertEquals("AR", ack.getMSA().getAcknowledgmentCode().getValue());
        assertEquals("Unrecognised application details", ack.getERR().getHL7ErrorCode().getOriginalText().getValue());
    }

}
