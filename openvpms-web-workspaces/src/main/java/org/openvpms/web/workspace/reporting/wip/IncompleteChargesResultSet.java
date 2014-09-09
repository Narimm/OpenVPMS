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

package org.openvpms.web.workspace.reporting.wip;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IConstraint;
import org.openvpms.component.system.common.query.JoinConstraint;
import org.openvpms.component.system.common.query.OrConstraint;
import org.openvpms.component.system.common.query.ShortNameConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.query.ActResultSet;
import org.openvpms.web.component.im.query.ParticipantConstraint;

import java.util.Date;
import java.util.List;

import static org.openvpms.component.system.common.query.Constraints.idEq;
import static org.openvpms.component.system.common.query.Constraints.join;
import static org.openvpms.component.system.common.query.Constraints.notExists;
import static org.openvpms.component.system.common.query.Constraints.subQuery;

/**
 * Result set for incomplete charges. i.e invoices, credit and counter acts that
 * are IN_PROGRESS, COMPLETE, or ON_HOLD.
 *
 * @author Tim Anderson
 */
class IncompleteChargesResultSet extends ActResultSet<Act> {

    /**
     * The selected location.
     */
    private final Party location;

    /**
     * The locations to query.
     */
    private final List<Party> locations;

    /**
     * Constructs an {@link IncompleteChargesResultSet}.
     *
     * @param archetypes   the act archetype constraint
     * @param participants the participant constraints. May be {@code null}
     * @param from         the act start-from date. May be {@code null}
     * @param to           the act start-to date. May be {@code null}
     * @param statuses     the act statuses. If empty, indicates all acts
     * @param exclude      if {@code true} exclude acts with status in {@code statuses}; otherwise include them.
     * @param pageSize     the maximum no. of results per page
     * @param sort         the sort criteria. May be {@code null}
     */
    public IncompleteChargesResultSet(ShortNameConstraint archetypes, ParticipantConstraint[] participants,
                                      Party location, List<Party> locations,
                                      Date from, Date to, String[] statuses, boolean exclude,
                                      int pageSize, SortConstraint[] sort) {
        super(archetypes, participants, from, to, statuses, exclude, null, pageSize, sort);
        this.location = location;
        this.locations = locations;
        setDistinct(true);
    }

    /**
     * Creates a new archetype query.
     *
     * @return a new archetype query
     */
    @Override
    protected ArchetypeQuery createQuery() {
        ArchetypeQuery query = super.createQuery();
        query.getArchetypeConstraint().setAlias("i1");
        if (location != null || locations.size() == 1) {
            Party l = (location != null) ? location : locations.get(0);
            query.add(new ParticipantConstraint("location", "participation.location", l));
        } else if (!locations.isEmpty()) {
            OrConstraint or = new OrConstraint();
            or.add(getLocations());
            or.add(notExists(subQuery(getArchetypes().getShortNames(), "i2").add(
                    join("location").add(join("entity").add(idEq("i1", "i2"))))));
            query.add(or);
        }
        return query;
    }

    /**
     * Creates a constraint on locations, restricting them to those passed at construction.
     *
     * @return the locations
     */
    private IConstraint getLocations() {
        JoinConstraint result = Constraints.join("location");
        OrConstraint or = new OrConstraint();
        result.add(or);
        for (Party l : locations) {
            or.add(Constraints.eq("entity", l.getObjectReference()));
        }
        return result;
    }
}
