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

package org.openvpms.web.component.im.query;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.util.IMObjectHelper;


/**
 * Query implementation that queries {@link Entity} instances on short name,
 * instance name, and active/inactive status.
 *
 * @author Tim Anderson
 */
public class EntityQuery<T extends Entity> extends QueryAdapter<ObjectSet, T> {

    /**
     * The context.
     */
    private final Context context;


    /**
     * Constructs an {@link EntityQuery} that queries entities with the specified short names.
     *
     * @param shortNames the short names
     * @throws ArchetypeQueryException if the short names don't match any archetypes
     */
    public EntityQuery(String[] shortNames, Context context) {
        this(new EntityObjectSetQuery(shortNames), context);
    }

    /**
     * Constructs an {@link EntityQuery} that delegates to the supplied query.
     *
     * @param query   the query
     * @param context the context
     */
    public EntityQuery(EntityObjectSetQuery query, Context context) {
        super(query, IMObjectHelper.getType(query.getShortNames()));
        this.context = context;
        // verify that the specified type matches what the query actually returns
        if (!Entity.class.isAssignableFrom(getType())) {
            throw new QueryException(QueryException.ErrorCode.InvalidType, Entity.class, getType());
        }
    }

    /**
     * Returns the selected archetype short name.
     *
     * @return the archetype short name. May be {@code null}
     */
    public String getShortName() {
        return ((EntityObjectSetQuery) getQuery()).getShortName();
    }

    /**
     * Determines if the query selects a particular object.
     *
     * @param object the object to check
     * @return {@code true} if the object is selected by the query
     */
    @Override
    public boolean selects(T object) {
        return ((EntityObjectSetQuery) getQuery()).selects(object);
    }

    /**
     * Converts a result set.
     *
     * @param set the set to convert
     * @return the converted set
     */
    @Override
    protected ResultSet<T> convert(ResultSet<ObjectSet> set) {
        return new EntityResultSetAdapter<T>((EntityObjectSetResultSet) set, context);
    }

}
