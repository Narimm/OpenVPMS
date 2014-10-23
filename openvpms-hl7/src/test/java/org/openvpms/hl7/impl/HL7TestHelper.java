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
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.v25.message.RDE_O11;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.hl7.io.Connector;
import org.openvpms.hl7.util.HL7Archetypes;

import java.io.IOException;

/**
 * HL7 test helper.
 *
 * @author Tim Anderson
 */
public class HL7TestHelper {

    /**
     * Creates a MLLP sender.
     *
     * @param port the port
     * @return a new sender
     */
    public static MLLPSender createSender(int port) {
        Entity sender = (Entity) TestHelper.create(HL7Archetypes.MLLP_SENDER);
        EntityBean bean = new EntityBean(sender);
        bean.setValue("name", "ZTest MLLP Sender");
        bean.setValue("host", "localhost");
        bean.setValue("port", port);
        bean.setValue("sendingApplication", "VPMS");
        bean.setValue("sendingFacility", "Main Clinic");
        bean.setValue("receivingApplication", "Cubex");
        bean.setValue("receivingFacility", "Cubex");
        bean.setValue("includeMillis", false);
        bean.setValue("includeTimeZone", true);
        bean.save();
        return MLLPSender.create(sender, ArchetypeServiceHelper.getArchetypeService());
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
     * @throws HL7Exception for any HL7 erroor
     * @throws IOException  for any I/O error
     */
    public static RDE_O11 createOrder(HapiContext context) throws IOException, HL7Exception {
        RDE_O11 message = new RDE_O11(context.getModelClassFactory());
        message.setParser(context.getGenericParser());
        message.initQuickstart("RDE", "O11", "P");
        return message;
    }

}
