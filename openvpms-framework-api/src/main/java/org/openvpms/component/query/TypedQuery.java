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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.query;

import java.util.List;

/**
 * Represents a typed query.
 *
 * @author Tim Anderson
 */
public interface TypedQuery<X> {

    /**
     * Executes a select query.
     *
     * @return the results of the query
     */
    List<X> getResultList();

    /**
     * Executes a select query that returns a single result.
     *
     * @return the result of the query
     */
    X getSingleResult();

    /**
     * Sets the maximum number of results to retrieve.
     *
     * @param maxResults the maximum number of results, or {@code Integer.MAX_VALUE} to not limit results
     * @return this query
     */
    TypedQuery<X> setMaxResults(int maxResults);

    /**
     * Sets the position of the first result to retrieve.
     *
     * @param position the first result position
     * @return this query
     */
    TypedQuery<X> setFirstResult(int position);
}
