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

package org.openvpms.web.workspace.supplier.vet;

import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.query.EntityQuery;

/**
 * Queries <em>party.supplierVeterinarian</em> instances on instance name, and active/inactive status.
 *
 * @author Tim Anderson
 */
public class VetQuery extends EntityQuery<Party> {

    /**
     * Constructs an {@link VetQuery} that queries entities with the specified short names.
     *
     * @param shortNames the short names
     * @throws ArchetypeQueryException if the short names don't match any archetypes
     */
    public VetQuery(String[] shortNames, Context context) {
        super(new VetObjectSetQuery(shortNames), context);
    }

}
