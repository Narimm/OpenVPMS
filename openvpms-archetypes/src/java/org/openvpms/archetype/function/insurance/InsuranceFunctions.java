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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.function.insurance;

import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.NodeSelectConstraint;
import org.openvpms.component.system.common.query.ObjectSetQueryIterator;

import static org.openvpms.component.system.common.query.Constraints.eq;
import static org.openvpms.component.system.common.query.Constraints.join;

/**
 * Patient insurance functions.
 *
 * @author Tim Anderson
 */
public class InsuranceFunctions {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Constructs an {@link InsuranceFunctions}.
     */
    public InsuranceFunctions(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Determines if an invoice item has been claimed by the supplied claim.
     *
     * @param claim the claim
     * @param item  the invoice item
     * @return {@code true} if the item has been claimed in the specified claim, otherwise {@code false}
     */
    public boolean claimed(FinancialAct claim, FinancialAct item) {
        boolean result = false;
        if (claim != null && item != null) {
            ArchetypeQuery query = new ArchetypeQuery(claim.getObjectReference())
                    .add(join("items", "c").add(join("target", "ct").add(join("items", "i").add(eq("target", item)))))
                    .add(new NodeSelectConstraint("id"));
            query.setMaxResults(1);
            result = new ObjectSetQueryIterator(service, query).hasNext();
        }
        return result;
    }
}
