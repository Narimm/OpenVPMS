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
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.ShortNameConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.query.ActResultSet;
import org.openvpms.web.component.im.query.ParticipantConstraint;

import java.util.Date;

import static org.openvpms.component.system.common.query.Constraints.eq;
import static org.openvpms.component.system.common.query.Constraints.join;

/**
 * Result set for <em>act.patientInvestigation</em> acts.
 *
 * @author Tim Anderson
 */
public class InvestigationResultSet extends ActResultSet<Act> {

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
                                  Date from, Date to, String[] statuses, int pageSize, SortConstraint[] sort) {
        super(archetypes, value, participants, from, to, statuses, false, null, pageSize, sort);
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
        return query;
    }
}
