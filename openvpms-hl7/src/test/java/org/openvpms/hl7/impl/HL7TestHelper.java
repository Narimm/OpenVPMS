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

import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.hl7.util.HL7Archetypes;

/**
 * HL7 test helper.
 *
 * @author Tim Anderson
 */
public class HL7TestHelper {

    /**
     * Creates a MLLP sender.
     *
     * @return a new sender
     */
    public static MLLPSender createSender() {
        Entity sender = (Entity) TestHelper.create(HL7Archetypes.MLLP_SENDER);
        EntityBean bean = new EntityBean(sender);
        bean.setValue("name", "ZTest MLLP Sender");
        bean.setValue("host", "localhost");
        bean.setValue("port", 2026);
        bean.setValue("sendingApplication", "VPMS");
        bean.setValue("sendingFacility", "Main Clinic");
        bean.setValue("receivingApplication", "Cubex");
        bean.setValue("receivingFacility", "Cubex");
        bean.setValue("includeMillis", false);
        bean.setValue("includeTimeZone", true);
        bean.save();
        return MLLPSender.create(sender, ArchetypeServiceHelper.getArchetypeService());
    }
}
