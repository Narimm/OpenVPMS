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

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.component.system.common.query.SortConstraint;


/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class BasicQuery<T extends IMObject> extends NonRenderingQuery<T> {

    /**
     * Construct a new <tt>BasicQuery</tt> that queries objects with
     * the specified primary short names.
     *
     * @param shortNames the archetype short names
     * @param type       the type that this query returns
     * @throws ArchetypeQueryException if the short names don't match any
     *                                 archetypes
     */
    public BasicQuery(String[] shortNames, Class type) {
        super(shortNames, type);
    }

    /**
     * Performs the query.
     *
     * @param sort the sort constraint. May be <tt>null</tt>
     * @return the query result set
     * @throws ArchetypeServiceException if the query fails
     */
    public ResultSet<T> query(SortConstraint[] sort) {
        return new DefaultResultSet<T>(getArchetypes(), getValue(),
                                       getConstraints(), sort, getMaxResults(),
                                       isDistinct());
    }

    /**
     * Determines if the query selects a particular object reference.
     *
     * @param reference the object reference to check
     * @return <tt>true</tt> if the object reference is selected by the query
     */
    public boolean selects(IMObjectReference reference) {
        DefaultResultSet<T> set = (DefaultResultSet<T>) query(null);
        set.setReferenceConstraint(reference);
        return set.hasNext();
    }
}
