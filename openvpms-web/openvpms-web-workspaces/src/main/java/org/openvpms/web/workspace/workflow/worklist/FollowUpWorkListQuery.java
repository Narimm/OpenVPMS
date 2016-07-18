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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.worklist;

import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.query.AbstractEntityQuery;
import org.openvpms.web.component.im.query.FilteredResultSet;
import org.openvpms.web.component.im.query.ListResultSet;
import org.openvpms.web.component.im.query.QueryHelper;
import org.openvpms.web.component.im.query.ResultSet;

import java.util.List;
import java.util.regex.Pattern;

/**
 * A query for follow-up work lists.
 *
 * @author Tim Anderson
 */
public class FollowUpWorkListQuery extends AbstractEntityQuery<Entity> {

    /**
     * The work lists to filter.
     */
    private final List<Entity> workLists;

    /**
     * Constructs a {@link FollowUpWorkListQuery} that queries objects with the specified primary short names.
     */
    public FollowUpWorkListQuery(List<Entity> workLists) {
        super(new String[]{ScheduleArchetypes.ORGANISATION_WORKLIST}, Entity.class);
        setAuto(true);
        setContains(true);
        this.workLists = workLists;
    }

    /**
     * Creates the result set.
     *
     * @param sort the sort criteria. May be {@code null}
     * @return a new result set
     */
    @Override
    protected ResultSet<Entity> createResultSet(SortConstraint[] sort) {
        String value = getValue();
        final Pattern pattern = !StringUtils.isEmpty(value) ? QueryHelper.getWildcardPattern(value) : null;
        ResultSet<Entity> set = new ListResultSet<>(workLists, getMaxResults());
        if (pattern != null) {
            set = new FilteredResultSet<Entity>(set) {
                @Override
                protected void filter(Entity object, List<Entity> results) {
                    String name = object.getName();
                    if (name != null && pattern.matcher(name).matches()) {
                        results.add(object);
                    }
                }
            };
        }
        return set;
    }

    /**
     * Determines if the query selects a particular object reference.
     *
     * @param reference the object reference to check
     * @return {@code true} if the object reference is selected by the query
     */
    @Override
    public boolean selects(IMObjectReference reference) {
        return QueryHelper.selects(query(), reference);
    }

}
