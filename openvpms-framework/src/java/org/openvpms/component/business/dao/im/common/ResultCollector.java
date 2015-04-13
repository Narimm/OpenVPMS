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

package org.openvpms.component.business.dao.im.common;

import org.openvpms.component.system.common.query.IPage;


/**
 * Used by an {@link IMObjectDAO} to collect query results into an
 * {@link IPage}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface ResultCollector<T> {

    /**
     * Sets the first result.
     *
     * @param first the first result
     */
    void setFirstResult(int first);

    /**
     * Sets the page sizse.
     *
     * @param size the page size
     */
    void setPageSize(int size);

    /**
     * Sets the total no. of results matching the query criteria.
     *
     * @param total the total no. of results matching the query criteria, or
     *              <code>-1</code> if not calculated
     */
    void setTotalResults(int total);

    /**
     * Collects an object.
     *
     * @param object the object to collect
     */
    void collect(Object object);

    /**
     * Returns the collected page.
     *
     * @return the collected page
     */
    IPage<T> getPage();
}
