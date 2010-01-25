/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.query;

import org.openvpms.component.business.dao.im.Page;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.component.im.util.IMObjectHelper;

import java.util.ArrayList;
import java.util.List;


/**
 * Query implementation that queries {@link Entity} instances on short name,
 * instance name, and active/inactive status.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-08-17 06:51:11Z $
 */
public class EntityQuery extends QueryAdapter<ObjectSet, Entity> {

    /**
     * Construct a new <tt>EntityQuery</tt> that queries entities
     * with the specified short names.
     *
     * @param shortNames the short names
     * @throws ArchetypeQueryException if the short names don't match any
     *                                 archetypes
     */
    public EntityQuery(String[] shortNames) {
        super(new EntityObjectSetQuery(shortNames),
              IMObjectHelper.getType(shortNames));
        // verify that the specified type matches what the query actually
        // returns
        if (!Entity.class.isAssignableFrom(getType())) {
            throw new QueryException(QueryException.ErrorCode.InvalidType,
                                     Entity.class, getType());

        }
    }

    /**
     * Returns the selected archetype short name.
     *
     * @return the archetype short name. May be <tt>null</tt>
     */
    public String getShortName() {
        return ((EntityObjectSetQuery) getQuery()).getShortName();
    }

    /**
     * Determines if the query selects a particular object.
     *
     * @param object the object to check
     * @return <tt>true</tt> if the object is selected by the query
     */
    public boolean selects(Entity object) {
        return ((EntityObjectSetQuery) getQuery()).selects(object);
    }

    /**
     * Converts a result set.
     *
     * @param set the set to convert
     * @return the converted set
     */
    protected ResultSet<Entity> convert(ResultSet<ObjectSet> set) {
        return new ResultSetAdapter<ObjectSet, Entity>(set) {

            protected IPage<Entity> convert(IPage<ObjectSet> page) {
                List<Entity> objects = new ArrayList<Entity>();
                for (ObjectSet set : page.getResults()) {
                    IMObjectReference ref
                            = set.getReference("entity.reference");
                    Entity entity = (Entity) IMObjectHelper.getObject(ref);
                    objects.add(entity);
                }
                return new Page<Entity>(objects, page.getFirstResult(),
                                        page.getPageSize(),
                                        page.getTotalResults());
            }
        };
    }

}
