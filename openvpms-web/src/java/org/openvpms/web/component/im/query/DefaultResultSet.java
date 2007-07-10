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

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.query.IConstraint;
import org.openvpms.component.system.common.query.ShortNameConstraint;
import org.openvpms.component.system.common.query.SortConstraint;


/**
 * Result set where archetype names are used as the criteria.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class DefaultResultSet<T extends IMObject> extends NameResultSet<T> {


    /**
     * Construct a new <tt>DefaultResultSet</tt>.
     *
     * @param archetypes   the archetypes to query
     * @param instanceName the instance name
     * @param constraints  additional query constraints. May be
     *                     <code<null</code>
     * @param sort         the sort criteria. May be <code>null</code>
     * @param rows         the maximum no. of rows per page
     * @param distinct     if <code>true</code> filter duplicate rows
     */
    public DefaultResultSet(ShortNameConstraint archetypes,
                            String instanceName, IConstraint constraints,
                            SortConstraint[] sort, int rows, boolean distinct) {
        super(archetypes, instanceName, constraints, sort, rows, distinct);
    }

}
