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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.query;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.component.system.common.query.BaseArchetypeConstraint;
import org.openvpms.component.system.common.query.SortConstraint;


/**
 * A non-rendering query that uses {@link DefaultResultSet}.
 *
 * @author Tim Anderson
 */
public class BasicQuery<T extends IMObject> extends NonRenderingQuery<T> {

    /**
     * Construct a new {@code BasicQuery} that queries objects with the specified primary short names.
     *
     * @param shortNames the archetype short names
     * @param type       the type that this query returns
     * @throws ArchetypeQueryException if the short names don't match any archetypes
     */
    public BasicQuery(String[] shortNames, Class type) {
        super(shortNames, type);
    }

    /**
     * Performs the query.
     *
     * @param sort the sort constraint. May be {@code null}
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
     * @return {@code true} if the object reference is selected by the query
     */
    public boolean selects(IMObjectReference reference) {
        DefaultResultSet<T> set = (DefaultResultSet<T>) query(null);
        set.setReferenceConstraint(reference);
        return set.hasNext();
    }

    /**
     * Determines if active and/or inactive instances should be returned.
     *
     * @return the active state
     */
    @Override
    public BaseArchetypeConstraint.State getActive() {
        return getArchetypes().getState();
    }
}
