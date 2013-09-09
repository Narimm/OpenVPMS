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

package org.openvpms.web.workspace.workflow.checkin;

import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.query.EntityObjectSetQuery;
import org.openvpms.web.component.im.query.EntityObjectSetResultSet;
import org.openvpms.web.component.im.query.ResultSet;

import static org.openvpms.component.system.common.query.Constraints.exists;
import static org.openvpms.component.system.common.query.Constraints.idEq;
import static org.openvpms.component.system.common.query.Constraints.join;
import static org.openvpms.component.system.common.query.Constraints.subQuery;

/**
 * A query for <em>party.organisationWorkList</em> instances, optionally linked to a
 * <em>party.organisationSchedule</em>.
 *
 * @author Tim Anderson
 */
class ScheduleWorkListQuery extends EntityObjectSetQuery {

    /**
     * The schedule. May be {@code null}
     */
    private final Entity schedule;

    private static final String[] SHORT_NAMES = {ScheduleArchetypes.ORGANISATION_WORKLIST};

    /**
     * Constructs a {@link ScheduleWorkListQuery}.
     *
     * @param schedule the schedule. May be {@code null}
     */
    public ScheduleWorkListQuery(Entity schedule) {
        super(SHORT_NAMES);
        this.schedule = schedule;
        setAuto(true);
    }

    /**
     * Creates the result set.
     *
     * @param sort the sort criteria. May be <tt>null</tt>
     * @return a new result set
     */
    protected ResultSet<ObjectSet> createResultSet(SortConstraint[] sort) {
        return new EntityObjectSetResultSet(getArchetypeConstraint(), getValue(),
                                            isIdentitySearch(), sort,
                                            getMaxResults(),
                                            isDistinct()) {
            /**
             * Creates a new archetype query.
             *
             * @return a new archetype query
             */
            @Override
            protected ArchetypeQuery createQuery() {
                ArchetypeQuery query = super.createQuery();
                if (schedule != null) {
                    IMObjectBean bean = new IMObjectBean(schedule);
                    boolean useAllWorkLists = bean.getBoolean("useAllWorkLists", true);
                    if (!useAllWorkLists) {
                        // constrain the work lists to those linked to the schedule
                        query.add(exists(subQuery(ScheduleArchetypes.ORGANISATION_SCHEDULE, "schedule")
                                                 .add(join("workLists", "w").add(idEq("entity", "w.target")))));
                    }
                }
                return query;
            }
        };
    }

}
