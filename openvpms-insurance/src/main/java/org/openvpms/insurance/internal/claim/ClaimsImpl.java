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

package org.openvpms.insurance.internal.claim;

import org.openvpms.archetype.rules.patient.insurance.InsuranceArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.insurance.claim.Claim;
import org.openvpms.insurance.claim.Claims;
import org.openvpms.insurance.internal.InsuranceFactory;

import static org.openvpms.component.system.common.query.Constraints.eq;
import static org.openvpms.component.system.common.query.Constraints.join;
import static org.openvpms.component.system.common.query.Constraints.shortName;
import static org.openvpms.component.system.common.query.Constraints.sort;

/**
 * Default implementation of {@link Claims}.
 *
 * @author Tim Anderson
 */
public class ClaimsImpl implements Claims {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The insurance factory.
     */
    private final InsuranceFactory factory;

    /**
     * Constructs a {@link ClaimsImpl}.
     *
     * @param service the archetype service
     * @param factory the insurance factory
     */
    public ClaimsImpl(IArchetypeService service, InsuranceFactory factory) {
        this.service = service;
        this.factory = factory;
    }

    /**
     * Returns a claim.
     * <p>
     * A claim can have a single identifier issued by an insurer. To avoid duplicates, each insurance service must
     * provide a unique archetype.
     *
     * @param archetype the identifier archetype. Must have an <em>actIdentity.insuranceClaim</em> prefix.
     * @param id        the claim identifier
     * @return the claim or {@code null} if none is found
     */
    @Override
    public Claim getClaim(String archetype, String id) {
        if (archetype == null || archetype.contains("*") ||
            !TypeHelper.matches(archetype, InsuranceArchetypes.CLAIM_IDENTITY)) {
            throw new IllegalArgumentException("Argument 'archetype' is not a valid claim identity archetype: "
                                               + archetype);
        }
        ArchetypeQuery query = new ArchetypeQuery(InsuranceArchetypes.CLAIM);
        query.add(join("insuranceId", shortName(archetype)).add(eq("identity", id)));
        query.add(sort("id"));
        query.setMaxResults(1);
        IMObjectQueryIterator<Act> iterator = new IMObjectQueryIterator<>(service, query);
        return (iterator.hasNext()) ? factory.createClaim(iterator.next()) : null;
    }
}
