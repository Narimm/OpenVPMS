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
import org.openvpms.component.system.common.query.IConstraint;
import org.openvpms.component.system.common.query.ShortNameConstraint;
import org.openvpms.component.system.common.query.SortConstraint;


/**
 * Result set for {@link Entity} instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-08-22 05:39:25Z $
 */
public class EntityResultSet<T extends Entity>
        extends AbstractEntityResultSet<T> {

    /**
     * Construct a new <tt>EntityResultSet</tt>.
     *
     * @param archetypes       the archetypes to query
     * @param instanceName     the instance name. May be <tt>null</tt>
     * @param searchIdentities if <tt>true</tt> search on identity name
     * @param constraints      additional query constraints. May be
     *                         <tt>null</tt>
     * @param sort             the sort criteria. May be <tt>null</tt>
     * @param rows             the maximum no. of rows per page
     * @param distinct         if <tt>true</tt> filter duplicate rows
     */
    public EntityResultSet(ShortNameConstraint archetypes,
                           String instanceName, boolean searchIdentities,
                           IConstraint constraints, SortConstraint[] sort,
                           int rows, boolean distinct) {
        super(archetypes, instanceName, searchIdentities, constraints, sort,
              rows, distinct, new DefaultQueryExecutor<T>());
    }

}
