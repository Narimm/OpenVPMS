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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.visit;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.ShortNameConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.query.AbstractActResultSet;
import org.openvpms.web.component.im.query.DefaultQueryExecutor;
import org.openvpms.web.component.im.query.ParticipantConstraint;

import java.util.Date;

import static org.openvpms.component.system.common.query.Constraints.eq;
import static org.openvpms.component.system.common.query.Constraints.idEq;
import static org.openvpms.component.system.common.query.Constraints.join;
import static org.openvpms.component.system.common.query.Constraints.ne;
import static org.openvpms.component.system.common.query.Constraints.notExists;
import static org.openvpms.component.system.common.query.Constraints.subQuery;

/**
 * A result set for estimates for a particular patient.
 * <p/>
 * This only returns estimates that include the specified patient, and no other.
 *
 * @author Tim Anderson
 */
public class VisitEstimateResultSet extends AbstractActResultSet<Act> {

    /**
     * The patient.
     */
    private final Party patient;

    /**
     * Constructs a new {@code AbstractActResultSet}.
     *
     * @param patient     the patient
     * @param archetypes  the act archetype constraint
     * @param participant the participant constraint. May be {@code null}
     * @param from        the act from date. May be {@code null}
     * @param to          the act to date, inclusive. May be {@code null}
     * @param statuses    the act statuses. If empty, indicates all acts
     * @param pageSize    the maximum no. of results per page
     * @param sort        the sort criteria. May be {@code null}
     */
    public VisitEstimateResultSet(Party patient, ShortNameConstraint archetypes, ParticipantConstraint participant,
                                  Date from, Date to, String[] statuses, int pageSize, SortConstraint[] sort) {
        super(archetypes, participant, from, to, statuses, pageSize, sort, new DefaultQueryExecutor<Act>());
        this.patient = patient;
    }

    /**
     * Creates a new archetype query.
     *
     * @return a new archetype query
     */
    @Override
    protected ArchetypeQuery createQuery() {
        getArchetypes().setAlias("a");
        String[] shortNames = getArchetypes().getShortNames();
        ArchetypeQuery query = super.createQuery();
        IMObjectReference reference = patient.getObjectReference();

        // only return acts that have items for the specified patient
        query.add(join("items").add(join("target").add(join("patient").add(eq("entity", reference)))));

        // ... that don't also have links to other patients
        query.add(notExists(subQuery(shortNames, "a2").add(join("items").add(join("target").add(join("patient").add(
                ne("entity", reference))))).add(idEq("a2", "a"))));
        return query;
    }
}
