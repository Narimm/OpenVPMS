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
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.component.system.common.query.SortConstraint;


/**
 * Abstract implementation of the {@link Query} interface that queries
 * {@link IMObject}s on short name, instance name, and active/inactive status.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-11-27 05:30:20Z $
 */
public abstract class AbstractIMObjectQuery<T extends IMObject>
        extends AbstractArchetypeQuery<T> {

    /**
     * Constructs a new <tt>AbstractIMObjectQuery</tt> that queries IMObjects
     * with the specified short names.
     *
     * @param shortNames the short names
     * @throws ArchetypeQueryException if the short names don't match any
     *                                 archetypes
     */
    public AbstractIMObjectQuery(String[] shortNames) {
        super(shortNames, true);
    }

    /**
     * Constructs a new <tt>AbstractIMObjectQuery</tt> that queries IMObjects
     * with the specified short names.
     *
     * @param shortNames the short names
     * @param type       the type that this query returns
     * @throws ArchetypeQueryException if the short names don't match any
     *                                 archetypes
     */
    public AbstractIMObjectQuery(String[] shortNames, Class type) {
        super(shortNames, type);
    }

    /**
     * Creates the result set.
     *
     * @param sort the sort criteria. May be <code>null</code>
     * @return a new result set
     */
    protected ResultSet<T> createResultSet(
            SortConstraint[] sort) {
        return new DefaultResultSet<T>(getArchetypeConstraint(), getValue(),
                                       getConstraints(), sort, getMaxResults(),
                                       isDistinct());
    }
}
