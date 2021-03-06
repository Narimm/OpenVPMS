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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.hl7.impl;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v25.message.ACK;
import ca.uhn.hl7v2.model.v25.message.RDS_O13;
import org.junit.Test;
import org.mockito.Mockito;
import org.openvpms.archetype.rules.finance.order.OrderArchetypes;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.user.UserRules;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.model.act.Act;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.entity.Entity;
import org.openvpms.component.model.object.Reference;
import org.openvpms.component.model.party.Party;
import org.openvpms.component.model.product.Product;
import org.openvpms.hl7.io.Connector;
import org.openvpms.hl7.io.Connectors;
import org.openvpms.hl7.io.MessageDispatcher;
import org.openvpms.hl7.pharmacy.Pharmacies;
import org.openvpms.hl7.util.HL7Archetypes;

import java.util.ArrayList;
import java.util.Collections;
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
        Entity pharmacy = (Entity) create(HL7Archetypes.PHARMACY);
        IMObjectBean bean = getBean(pharmacy);
        bean.setTarget("user", user);
        Party location = getContext().getLocation();
        bean.setTarget("location", location);

        Pharmacies pharmacies = createPharmacies(pharmacy);
        MessageDispatcher dispatcher = Mockito.mock(MessageDispatcher.class);
        Connectors connectors = Mockito.mock(Connectors.class);
        PatientRules rules = new PatientRules(null, getArchetypeService(), getLookupService());
        UserRules userRules = new UserRules(getArchetypeService());
        final Connector receiver = new MLLPReceiver(10001, "Cubex", "Cubex", "VPMS", "VPMS",
                                                    new IMObjectReference(HL7Archetypes.MLLP_RECEIVER, -1),
                                                    new HL7Mapping());
        final List<Act> order = new ArrayList<>();
        PharmacyDispenseServiceImpl service = new PharmacyDispenseServiceImpl(pharmacies, dispatcher, connectors,
                                                                              getArchetypeService(), rules, userRules) {

            @Override
            protected List<Act> process(RDS_O13 message, Reference location) throws HL7Exception {
                List<Act> acts = super.process(message, location);
                order.addAll(acts);
                return acts;
            }

            protected List<Connector> getConnectors() {
                return Collections.singletonList(receiver);
            }

        };
        assertTrue(service.canProcess(rds));
        log("RDS: ", rds);
        Message response = service.processMessage(rds, receiver.getReference());
        assertTrue(response instanceof ACK);
        ACK ack = (ACK) response;
        assertEquals("AA", ack.getMSA().getAcknowledgmentCode().getValue());
        assertEquals(2, order.size());
        assertTrue(TypeHelper.isA(order.get(0), OrderArchetypes.PHARMACY_ORDER));
        assertTrue(TypeHelper.isA(order.get(1), OrderArchetypes.PHARMACY_ORDER_ITEM));
        IMObjectBean orderBean = getBean(order.get(0));
        assertEquals(location.getObjectReference(), orderBean.getTargetRef("location"));
    }

    /**
     * Creates a {@link Pharmacies} instance that returns the supplied pharmacy.
     *
     * @param pharmacy the pharmacy
     * @return a new {@link Pharmacies}
     */
    protected Pharmacies createPharmacies(final Entity pharmacy) {
        return new Pharmacies() {
            public List<Entity> getServices() {
                return Collections.singletonList(pharmacy);
            }

            @Override
            public Entity getService(Reference reference) {
                return pharmacy;
            }

            @Override
            public Entity getService(Entity group, Reference location) {
                return null;
            }

            @Override
            public Connector getSender(Entity pharmacy) {
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

}
