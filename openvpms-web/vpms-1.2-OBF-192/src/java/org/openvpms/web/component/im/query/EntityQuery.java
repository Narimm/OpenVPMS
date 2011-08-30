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

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.component.system.common.query.SortConstraint;


/**
 * Query implementation that queries {@link Entity} instances on short name,
 * instance name, and active/inactive status.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-08-17 06:51:11Z $
 */
public class EntityQuery extends AbstractEntityQuery<Entity> {

    /**
     * Construct a new <tt>EntityQuery</tt> that queries entities
     * with the specified short names.
     *
     * @param shortNames the short names
     * @throws ArchetypeQueryException if the short names don't match any
     *                                 archetypes
     */
    public EntityQuery(String[] shortNames) {
        super(shortNames);
    }

    /**
     * Creates the result set.
     *
     * @param sort the sort criteria. May be <tt>null</tt>
     * @return a new result set
     */
    protected ResultSet<Entity> createResultSet(SortConstraint[] sort) {
        return new EntityResultSet<Entity>(getArchetypeConstraint(), getName(),
                                           isIdentitySearch(), getConstraints(),
                                           sort, getMaxResults(), isDistinct());
    }
}
