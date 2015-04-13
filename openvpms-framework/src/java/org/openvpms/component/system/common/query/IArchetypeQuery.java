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

package org.openvpms.component.system.common.query;


/**
 * Archetype query interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface IArchetypeQuery {

    /**
     * Indicates that all the results should be returned.
     */
    final int ALL_RESULTS = -1;

    /**
     * Returns the first row.
     *
     * @return the first row
     */
    int getFirstResult();

    /**
     * Sets the first row.
     *
     * @param firstResult the first row
     * @return this query
     */
    IArchetypeQuery setFirstResult(int firstResult);

    /**
     * Returns the maximum number of results to retrieve.
     *
     * @return the maximum no. of results to retrieve or {@link #ALL_RESULTS} to
     *         retrieve all results
     */
    int getMaxResults();

    /**
     * Sets the maximum number of results to retrieve.  If not set,
     * there is no limit to the number of results retrieved.
     *
     * @param maxResults the maximum no. of results to retrieve
     * @return this query
     */
    IArchetypeQuery setMaxResults(int maxResults);

    /**
     * Determines if the total no. of results should be counted and returned
     * in the resulting {@link IPage}.
     * Only applies when <code>getMaxResults() != ALL_RESULTS</code>.
     *
     * @return <code>true</code> if the total no. of results should be counted,
     *         otherwise <code>false</code>. Defaults to <code>false</code>
     */
    boolean countResults();

    /**
     * Determines if the total no. of rows should be counted and returned
     * in the resulting {@link IPage}.
     *
     * @param count if <code>true</code> count the no. of rows
     * @return this query
     */
    IArchetypeQuery setCountResults(boolean count);
}
