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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.system.common.query;

import java.util.List;


/**
 * This interface is used to support pagination of large result sets.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public interface IPage<T> {

    /**
     * Returns the query results.
     *
     * @return the results
     */
    public List<T> getResults();

    /**
     * Return the first result requested.
     *
     * @return the first result requested
     */
    public int getFirstResult();

    /**
     * Returns the number of results requested. The {@link #getResults()}
     * method will return up to this count.
     *
     * @return the number of results requested
     */
    public int getPageSize();

    /**
     * Returns the total no. of results matching the query criteria.
     *
     * @return the total no. of results matching the query criteria, or
     *         <code>-1</code> if not calculated
     */
    public int getTotalResults();

}