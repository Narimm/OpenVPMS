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
 *  Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.component.system.common.query;

/**
 * A constraint that determines if rows in the specified sub-query exist.
 *
 * @author Tim Anderson
 */
public class ExistsConstraint implements IConstraint {

    /**
     * The sub-query.
     */
    private final ArchetypeQuery subQuery;

    /**
     * Constructs an {@code ExistsConstraint}.
     *
     * @param subQuery the sub-query
     */
    public ExistsConstraint(ArchetypeQuery subQuery) {
        this.subQuery = subQuery;
    }

    /**
     * Returns the sub-query.
     *
     * @return the sub-query
     */
    public ArchetypeQuery getSubQuery() {
        return subQuery;
    }
}
