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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.query;

import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.NodeSelectConstraint;
import org.openvpms.component.system.common.query.ObjectRefSelectConstraint;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.ShortNameConstraint;
import org.openvpms.component.system.common.query.SortConstraint;


/**
 * A {@link ResultSet} implementation that queries entities.
 * <p/>
 * The returned {@link ObjectSet ObjectSet}s contain the following:
 * <ul>
 * <li><em>entity</em> - the entity's object reference</li>
 * <li><em>entity.name</em> - the entity's name</li>
 * <li><em>entity.description</em> - the entity's description</li>
 * </ul>
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class EntityObjectSetResultSet
    extends AbstractEntityResultSet<ObjectSet> {

    /**
     * Creates a new <tt>EntityObjectSetResultSet</tt>.
     *
     * @param archetypes       the archetypes to query
     * @param value            the value to query on. May be <tt>null</tt>
     * @param searchIdentities if <tt>true</tt> search on identity name
     * @param sort             the sort criteria. May be <tt>null</tt>
     * @param rows             the maximum no. of rows per page
     * @param distinct         if <tt>true</tt> filter duplicate rows
     */
    public EntityObjectSetResultSet(ShortNameConstraint archetypes, String value, boolean searchIdentities,
                                    SortConstraint[] sort, int rows, boolean distinct) {
        super(archetypes, value, searchIdentities, null, sort,
              rows, distinct, new ObjectSetQueryExecutor());
        archetypes.setAlias("entity");
    }

    /**
     * Creates a new archetype query.
     *
     * @return a new archetype query
     */
    @Override
    protected ArchetypeQuery createQuery() {
        ArchetypeQuery query = super.createQuery();
        query.add(new ObjectRefSelectConstraint("entity"));
        query.add(new NodeSelectConstraint("entity.name"));
        query.add(new NodeSelectConstraint("entity.description"));
        return query;
    }

}
