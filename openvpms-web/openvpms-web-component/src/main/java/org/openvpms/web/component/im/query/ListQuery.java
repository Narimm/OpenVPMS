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

package org.openvpms.web.component.im.query;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.system.common.query.BaseArchetypeConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.util.IMObjectSorter;

import java.util.ArrayList;
import java.util.List;


/**
 * Query where the results are pre-loaded from a list.
 *
 * @author Tim Anderson
 */
public class ListQuery<T> extends NonRenderingQuery<T> {

    /**
     * The objects to return.
     */
    private final List<T> objects;

    /**
     * Constructs a {@link ListQuery}.
     *
     * @param objects   the objects that the query returns
     * @param shortName the archetype short name(s) of the objects. May contain wildcards
     * @param type      the type that this query returns
     */
    public ListQuery(List<T> objects, String shortName, Class<T> type) {
        this(objects, new String[]{shortName}, type);
    }

    /**
     * Constructs a {@link ListQuery}.
     *
     * @param objects    the objects that the query returns
     * @param shortNames the archetype short names of the objects. May contain wildcards
     * @param type       the type that this query returns
     */
    public ListQuery(List<T> objects, String[] shortNames, Class<T> type) {
        super(shortNames, type);
        this.objects = objects;
        setAuto(true);
    }

    /**
     * Performs the query.
     *
     * @param sort the sort constraint. May be {@code null}
     * @return the query result set
     * @throws ArchetypeServiceException if the query fails
     */
    @SuppressWarnings("unchecked")
    public ResultSet<T> query(SortConstraint[] sort) {
        if (sort != null && IMObject.class.isAssignableFrom(getType())) {
            List sorted = new ArrayList(objects);
            IMObjectSorter.sort(sorted, sort);
            return new ListResultSet<T>(sorted, getMaxResults());
        }
        return new ListResultSet<T>(objects, getMaxResults());
    }

    /**
     * Determines if the query selects a particular object reference.
     *
     * @param reference the object reference to check
     * @return {@code true} if the object reference is selected by the query
     */
    public boolean selects(IMObjectReference reference) {
        if (IMObject.class.isAssignableFrom(getType())) {
            for (Object object : objects) {
                IMObject o = (IMObject) object;
                if (o.getObjectReference().equals(reference)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Determines if active and/or inactive instances should be returned.
     *
     * @return the active state
     */
    @Override
    public BaseArchetypeConstraint.State getActive() {
        return BaseArchetypeConstraint.State.BOTH;
    }
}
