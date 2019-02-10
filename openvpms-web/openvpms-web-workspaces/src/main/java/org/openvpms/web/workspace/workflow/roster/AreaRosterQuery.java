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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.roster;

import nextapp.echo2.app.Component;
import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.system.common.query.JoinConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.query.EntityResultSet;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.echo.focus.FocusHelper;

import static org.openvpms.component.system.common.query.Constraints.eq;
import static org.openvpms.component.system.common.query.Constraints.join;

/**
 * Queries roster areas.
 *
 * @author Tim Anderson
 */
public class AreaRosterQuery extends RosterQuery<Entity> {

    /**
     * Constructs a {@link RosterQuery}.
     *
     * @param context the context
     */
    public AreaRosterQuery(Context context) {
        super(new String[]{ScheduleArchetypes.ROSTER_AREA}, false, Entity.class, context);
    }

    @Override
    protected void doQueryLayout(Component container) {
        addDate(container);
        addSearchField(container);
        addActive(container);
        FocusHelper.setFocus(getSearchField());
    }

    /**
     * Creates the result set.
     *
     * @param sort the sort criteria. May be {@code null}
     * @return a new result set
     */
    @Override
    protected ResultSet<Entity> createResultSet(SortConstraint[] sort) {
        JoinConstraint constraint = join("location").add(eq("target", getContext().getLocation()));
        return new EntityResultSet<>(getArchetypeConstraint(), getValue(), false, constraint, sort, getMaxResults(),
                                     isDistinct());
    }
}
