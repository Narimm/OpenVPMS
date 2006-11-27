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
import org.openvpms.component.system.common.query.BaseArchetypeConstraint;
import org.openvpms.component.system.common.query.ShortNameConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.list.ArchetypeShortNameListModel;


/**
 * Query component for {@link Entity} instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractEntityQuery extends AbstractQuery<Entity> {

    /**
     * Construct a new <code>AbstractEntityQuery</code> that queries Entity instances
     * with the specified short names.
     *
     * @param shortNames the short names
     * @throws ArchetypeQueryException if the short names don't match any
     *                                 archetypes
     */
    public AbstractEntityQuery(String[] shortNames) {
        super(shortNames);
    }

    /**
     * Construct a new <code>AbstractEntityQuery</code> that queries Entity instances
     * with the specified criteria.
     *
     * @param refModelName the archetype reference model name
     * @param entityName   the archetype entity name
     * @param conceptName  the archetype concept name
     */
    public AbstractEntityQuery(String refModelName, String entityName,
                               String conceptName) {
        super(refModelName, entityName, conceptName);
    }

    /**
     * Performs the query.
     *
     * @param sort the sort constraint. May be <code>null</code>
     * @return the query result set
     */
    @Override
    public ResultSet<Entity> query(SortConstraint[] sort) {
        String type = getShortName();
        String name = getName();
        boolean activeOnly = !includeInactive();

        BaseArchetypeConstraint archetypes;
        if (type == null || type.equals(ArchetypeShortNameListModel.ALL)) {
            archetypes = getArchetypeConstraint();
            archetypes.setActiveOnly(activeOnly);
        } else {
            archetypes = new ShortNameConstraint(type, true, activeOnly);
        }
        return new EntityResultSet(archetypes, name, getConstraints(), sort,
                                   getMaxRows(), isDistinct());
    }
}
