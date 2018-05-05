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
import ca.uhn.hl7v2.model.v25.message.RDE_O11;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.lookup.LookupRelationship;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.hl7.io.Connector;
import org.openvpms.hl7.util.HL7Archetypes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * HL7 test helper.
 *
 * @author Tim Anderson
 */
public class HL7TestHelper {

    /**
     * Creates a mapping for Cubex.
     *
     * @return a new <em>entity.HL7MappingCubex</em>
     */
    public static Entity createCubexMapping() {
        Entity entity = (Entity) TestHelper.create(HL7Archetypes.CUBEX_MAPPING);
        TestHelper.save(entity);
        return entity;
    }

    /**
     * Creates a mapping for IDEXX.
     *
     * @return a new <em>entity.HL7MappingIDEXX</em>
     */
    public static Entity createIDEXXMapping() {
        Entity entity = (Entity) TestHelper.create(HL7Archetypes.IDEXX_MAPPING);
        TestHelper.save(entity);
        return entity;
    }

    /**
     * Creates an MLLP sender.
     *
     * @param port    the port
     * @param mapping the mapping. May be {@code null}
     * @return a new sender
     */
    public static MLLPSender createSender(int port, Entity mapping) {
        return createSender(port, mapping, "Cubex", "Cubex");
    }

    /**
     * Creates an MLLP sender.
     *
     * @param port              the port
     * @param mapping           the mapping. May be {@code null}
     * @param receivingApp      the receiving application
     * @param receivingFacility the receiving facility
     * @return a new sender
     */
    public static MLLPSender createSender(int port, Entity mapping, String receivingApp, String receivingFacility) {
        return createSender(port, mapping, "VPMS", "Main Clinic", receivingApp, receivingFacility);
    }

    /**
     * Creates an MLLP sender.
     *
     * @param port              the port
     * @param mapping           the mapping
     * @param receivingApp      the receiving application
     * @param receivingFacility the receiving facility
     * @return a new sender
     */
    public static MLLPSender createSender(int port, Entity mapping, String sendingApp, String sendingFacility,
                                          String receivingApp, String receivingFacility) {
        Entity sender = (Entity) TestHelper.create(HL7Archetypes.MLLP_SENDER);
        EntityBean bean = new EntityBean(sender);
        bean.setValue("name", "ZTest MLLP Sender");
        bean.setValue("host", "localhost");
        bean.setValue("port", port);
        bean.setValue("sendingApplication", sendingApp);
        bean.setValue("sendingFacility", sendingFacility);
        bean.setValue("receivingApplication", receivingApp);
        bean.setValue("receivingFacility", receivingFacility);
        if (mapping != null) {
            bean.addNodeTarget("mapping", mapping);
        }
        bean.save();
        return MLLPSender.create(sender, ArchetypeServiceHelper.getArchetypeService());
    }

    /**
     * Creates a MLLP receiver.
     *
     * @param port the port
     * @return a new receiver
     */
    public static MLLPReceiver createReceiver(int port, String receivingApplication, String receivingFacility) {
        Entity mapping = getMapping(HL7Archetypes.CUBEX_MAPPING);
        Entity receiver = (Entity) TestHelper.create(HL7Archetypes.MLLP_RECEIVER);
        EntityBean bean = new EntityBean(receiver);
        bean.setValue("name", "ZTest MLLP Receiver");
        bean.setValue("port", port);
        bean.setValue("sendingApplication", "Cubex");
        bean.setValue("sendingFacility", "Cubex");
        bean.setValue("receivingApplication", receivingApplication);
        bean.setValue("receivingFacility", receivingFacility);
        bean.addNodeTarget("mapping", mapping);
        bean.save();
        return MLLPReceiver.create(receiver, ArchetypeServiceHelper.getArchetypeService());
    }

    public static Entity getMapping(String shortName) {
        IArchetypeService service = ArchetypeServiceHelper.getArchetypeService();
        ArchetypeQuery query = new ArchetypeQuery(shortName);
        query.add(Constraints.sort("id"));
        Iterator<Entity> iter = new IMObjectQueryIterator<>(query);
        return (iter.hasNext()) ? iter.next() : (Entity) service.create(shortName);
    }

    /**
     * Disables a connector in the database.
     *
     * @param connector the connector
     */
    public static void disable(Connector connector) {
        IArchetypeService service = ArchetypeServiceHelper.getArchetypeService();
        Entity config = (Entity) service.get(connector.getReference());
        if (config != null) {
            config.setActive(false);
            service.save(config);
        }
    }

    /**
     * Suspends/resumes sending.
     *
     * @param sender  the sender
     * @param suspend if {@code true} suspend sends, otherwise resume sends
     */
    public static void suspend(MLLPSender sender, boolean suspend) {
        IArchetypeService service = ArchetypeServiceHelper.getArchetypeService();
        Entity config = (Entity) service.get(sender.getReference());
        if (config != null) {
            IMObjectBean bean = new IMObjectBean(config);
            bean.setValue("suspended", suspend);
            bean.save();
        }
    }

    /**
     * Creates an order.
     *
     * @param context the message context
     * @return a new order
     * @throws HL7Exception for any HL7 error
     * @throws IOException  for any I/O error
     */
    public static RDE_O11 createOrder(HapiContext context) throws IOException, HL7Exception {
        RDE_O11 message = new RDE_O11(context.getModelClassFactory());
        message.setParser(context.getGenericParser());
        message.initQuickstart("RDE", "O11", "P");
        return message;
    }

    /**
     * Removes mapping relationships from a species.
     *
     * @param species the species
     */
    public static void removeRelationships(Lookup species) {
        IMObjectBean bean = new IMObjectBean(species);
        List<LookupRelationship> mappings = bean.getValues("mapping", LookupRelationship.class);
        List<IMObject> toSave = new ArrayList<IMObject>();
        for (LookupRelationship mapping : mappings) {
            species.removeLookupRelationship(mapping);
            Lookup target = (Lookup) ArchetypeServiceHelper.getArchetypeService().get(mapping.getTarget());
            target.removeLookupRelationship(mapping);
            toSave.add(target);
        }
        if (!toSave.isEmpty()) {
            toSave.add(species);
            TestHelper.save(toSave);
        }
    }

    /**
     * Adds a mapping relationship.
     *
     * @param species      the species
     * @param idexxSpecies the species to map to
     */
    public static void addMapping(Lookup species, Lookup idexxSpecies) {
        LookupRelationship relationship
                = (LookupRelationship) TestHelper.create("lookupRelationship.speciesMappingIDEXX");
        relationship.setSource(species.getObjectReference());
        relationship.setTarget(idexxSpecies.getObjectReference());
        species.addLookupRelationship(relationship);
        idexxSpecies.addLookupRelationship(relationship);
        TestHelper.save(species, idexxSpecies);
    }
}
