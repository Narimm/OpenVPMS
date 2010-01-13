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

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.SortConstraint;


/**
 * Queries entities, returning their names and descriptions.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class EntityObjectSetQuery extends AbstractEntityQuery<ObjectSet> {

    /**
     * Construct a new <tt>EntityObjectSetQuery</tt> that queries entities
     * instances with the specified short names.
     *
     * @param shortNames the short names
     * @throws ArchetypeQueryException if the short names don't match any
     *                                 archetypes
     */
    public EntityObjectSetQuery(String[] shortNames) {
        super(shortNames, ObjectSet.class);
    }

    /**
     * Determines if the query selects a particular object.
     *
     * @param object the object to check
     * @return <tt>true</tt> if the object is selected by the query
     */
    public boolean selects(Entity object) {
        EntityObjectSetResultSet set = (EntityObjectSetResultSet) createResultSet(null);
        if (object != null) {
            set.setReferenceConstraint(object.getObjectReference());
        }
        return set.hasNext();
    }

    /**
     * Creates the result set.
     *
     * @param sort the sort criteria. May be <tt>null</tt>
     * @return a new result set
     */
    protected ResultSet<ObjectSet> createResultSet(SortConstraint[] sort) {
        return new EntityObjectSetResultSet(getArchetypeConstraint(), getName(),
                                            isIdentitySearch(), sort,
                                            getMaxResults(),
                                            isDistinct());
    }
}
