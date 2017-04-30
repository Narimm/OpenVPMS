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

package org.openvpms.archetype.rules.user;

import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.ArchetypeIdConstraint;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IArchetypeQuery;

import static org.openvpms.archetype.rules.user.UserArchetypes.USER;
import static org.openvpms.component.system.common.query.Constraints.eq;
import static org.openvpms.component.system.common.query.Constraints.idEq;
import static org.openvpms.component.system.common.query.Constraints.join;
import static org.openvpms.component.system.common.query.Constraints.leftJoin;
import static org.openvpms.component.system.common.query.Constraints.notExists;
import static org.openvpms.component.system.common.query.Constraints.or;
import static org.openvpms.component.system.common.query.Constraints.subQuery;

/**
 * Clinician query factory.
 *
 * @author Tim Anderson
 */
public class ClinicianQueryFactory {

    /**
     * Creates a query to return clinicians.
     *
     * @param location if non-null, return all clinicians that have a link to the location or no link to any location
     * @return a new query
     */
    public static IArchetypeQuery create(Party location) {
        ArchetypeQuery query = new ArchetypeQuery(USER, true, true);
        query.getArchetypeConstraint().setAlias("u");
        query.add(join("classifications", new ArchetypeIdConstraint("lookup.userType")).add(eq("code", "CLINICIAN")));
        if (location != null) {
            query.add(leftJoin("locations", "l"));
            query.add(or(eq("l.target", location),
                         notExists(subQuery(USER, "u2").add(join("locations", "l2").add(idEq("u", "u2"))))));
            query.add(Constraints.sort("id"));
        }
        return query;
    }
}
