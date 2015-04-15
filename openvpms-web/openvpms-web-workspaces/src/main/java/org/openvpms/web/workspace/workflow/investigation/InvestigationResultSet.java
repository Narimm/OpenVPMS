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

package org.openvpms.web.workspace.workflow.investigation;

import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.patient.InvestigationArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IConstraint;
import org.openvpms.component.system.common.query.JoinConstraint;
import org.openvpms.component.system.common.query.NotConstraint;
import org.openvpms.component.system.common.query.OrConstraint;
import org.openvpms.component.system.common.query.ShortNameConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.query.ActResultSet;
import org.openvpms.web.component.im.query.ParticipantConstraint;

import java.util.Date;
import java.util.List;

import static org.openvpms.component.system.common.query.Constraints.eq;
import static org.openvpms.component.system.common.query.Constraints.idEq;
import static org.openvpms.component.system.common.query.Constraints.join;
import static org.openvpms.component.system.common.query.Constraints.notExists;
import static org.openvpms.component.system.common.query.Constraints.subQuery;

/**
 * Result set for <em>act.patientInvestigation</em> acts.
 *
 * @author Tim Anderson
 */
public class InvestigationResultSet extends ActResultSet<Act> {

    /**
     * The selected location.
     */
    private final Party location;

    /**
     * The locations to query.
     */
    private final List<Party> locations;

    /**
     * Constructs an {@link InvestigationResultSet}.
     *
     * @param archetypes   the act archetype constraint
     * @param value        the value being searched on. If non-null, can be used to search on investigation identifier
     *                     or patient name
     * @param participants the participant constraints. May be {@code null}
     * @param from         the act start-from date. May be {@code null}
     * @param to           the act start-to date. May be {@code null}
     * @param statuses     the act statuses. If empty, indicates all acts
     * @param pageSize     the maximum no. of results per page
     * @param sort         the sort criteria. May be {@code null}
     */
    public InvestigationResultSet(ShortNameConstraint archetypes, String value, ParticipantConstraint[] participants,
                                  Party location, List<Party> locations, Date from, Date to, String[] statuses,
                                  int pageSize, SortConstraint[] sort) {
        super(archetypes, value, participants, from, to, statuses, false, null, pageSize, sort);
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
        String value = getValue();
        if (!StringUtils.isEmpty(value) && getId(value) == null) {
            query.add(join("patient").add(join("entity").add(eq("name", value))));
        }
        if (location != null || !locations.isEmpty()) {
            OrConstraint or = new OrConstraint();
            if (location != null) {
                or.add(new ParticipantConstraint("location", "participation.location", location));
            } else {
                or.add(getLocations());
            }
            or.add(getNoLocation());
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

    /**
     * Creates a constraint that finds investigations that have no location.
     *
     * @return a new constraint
     */
    private NotConstraint getNoLocation() {
        return notExists(subQuery(InvestigationArchetypes.PATIENT_INVESTIGATION, "i2").add(
                join("location").add(join("entity").add(idEq("act", "i2")))));
        // NB: "act" alias comes from ActQuery. TODO - brittle
    }

}
