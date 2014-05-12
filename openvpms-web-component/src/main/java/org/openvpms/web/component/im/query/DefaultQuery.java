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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.query;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.component.system.common.query.SortConstraint;

import static org.openvpms.component.system.common.query.Constraints.sort;


/**
 * Query implementation that queries {@link IMObject} instances on short name,
 * instance name, and active/inactive status.
 *
 * @author Tim Anderson
 */
public class DefaultQuery<T extends IMObject> extends AbstractIMObjectQuery<T> {

    /**
     * The sort constraint.
     */
    private static final SortConstraint[] SORT_CONSTRAINT = new SortConstraint[]{sort("name"), sort("id")};

    /**
     * Constructs a new <tt>DefaultQuery</tt> that queries IMObjects with the
     * specified short names.
     *
     * @param shortNames the short names
     * @param type       the type that this query returns
     * @throws ArchetypeQueryException if the short names don't match any archetypes
     */
    public DefaultQuery(String[] shortNames, Class<T> type) {
        super(shortNames, type);
        setDefaultSortConstraint(SORT_CONSTRAINT);
    }

}
