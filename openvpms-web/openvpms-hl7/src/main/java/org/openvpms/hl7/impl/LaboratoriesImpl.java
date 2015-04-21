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

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.hl7.io.Connectors;
import org.openvpms.hl7.laboratory.Laboratories;
import org.openvpms.hl7.util.HL7Archetypes;

/**
 * Caches <em>entity.HL7ServiceLaboratory</em> objects.
 *
 * @author Tim Anderson
 */
public class LaboratoriesImpl extends ServicesImpl implements Laboratories {

    /**
     * Constructs a {@link LaboratoriesImpl}.
     *
     * @param service    the archetype service
     * @param connectors the connectors
     */
    public LaboratoriesImpl(IArchetypeService service, Connectors connectors) {
        super(service, HL7Archetypes.LABORATORY, Entity.class, false, connectors);
        load();
    }

}
