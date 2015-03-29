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


/**
 * Simple query implementation that indicates that the query should be run automatically.
 *
 * @author Tim Anderson
 */
public class AutoQuery extends AbstractIMObjectQuery<IMObject> {

    /**
     * Constructs a {@link AutoQuery} that queries IMObjects with the specified short names.
     *
     * @param shortNames the short names
     * @throws ArchetypeQueryException if the short names don't match any archetypes
     */
    public AutoQuery(String[] shortNames) {
        super(shortNames);
        setDefaultSortConstraint(NAME_SORT_CONSTRAINT);
    }

    /**
     * Determines if the query should be run automatically.
     *
     * @return {@code true} if the query should be run automatically; otherwise {@code false}
     */
    @Override
    public boolean isAuto() {
        return true;
    }

}
