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


package org.openvpms.component.business.dao.im;

// java core

import org.apache.commons.lang.builder.ToStringBuilder;
import org.openvpms.component.system.common.query.IPage;

import java.io.Serializable;
import java.util.List;

/**
 * This object is used to support pagination, where a subset of the query
 * result set is returned to the caller. The object contains the first result
 * and number of results in the page. In addition it also contains the total
 * number of results that the query would return if pagination was not used.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class Page<T> implements Serializable, IPage<T> {

    /**
     * Default UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The results matching the criteria.
     */
    private List<T> results;

    /**
     * The first row in the page.
     */
    private int firstResult;

    /**
     * The number of results that were requested.
     */
    private int pageSize;

    /**
     * The total number of results matching the query criteria, or
     * <code>-1</code> if it was.
     */
    private int totalResults;


    /**
     * Default constructor.
     */
    public Page() {
        // do nothing
    }

    /**
     * Constructs a new <code>Page</code>.
     *
     * @param results      the results matching the query criteria
     * @param firstResult  the first result
     * @param pageSize     the number of results requested, which may not always
     *                     be equal to the number of results
     * @param totalResults the total number of results matching the criteria,
     *                     or <code>-1</code> if not calculated
     */
    public Page(List<T> results, int firstResult, int pageSize,
                int totalResults) {
        this.results = results;
        this.firstResult = firstResult;
        this.pageSize = pageSize;
        this.totalResults = totalResults;
    }

    /**
     * Returns the query results.
     *
     * @return the results
     */
    public List<T> getResults() {
        return results;
    }

    /**
     * Sets the query results.
     *
     * @param results the query results
     */
    public void setResults(List<T> results) {
        this.results = results;
    }

    /**
     * Return the first result requested.
     *
     * @return the first result requested
     */
    public int getFirstResult() {
        return firstResult;
    }

    /**
     * Sets the first result.
     *
     * @param firstResult the first result
     */
    public void setFirstResult(int firstResult) {
        this.firstResult = firstResult;
    }

    /**
     * Returns the number of results requested.
     * The {@link #getResults()} method will return up to this count.
     *
     * @return the number of results requested
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * Sets the page size.
     *
     * @param size the page size
     */
    public void setPageSize(int size) {
        pageSize = size;
    }

    /**
     * Returns the total no. of results matching the query criteria.
     *
     * @return the total no. of results matching the query criteria, or
     *         <code>-1</code> if not calculated
     */
    public int getTotalResults() {
        return totalResults;
    }

    /**
     * Sets the total no. of results matching the query criteria.
     *
     * @param total the total no. of results matching the query criteria, or
     *              <code>-1</code> if not calculated
     */
    public void setTotalResults(int total) {
        totalResults = total;
    }

    /**
     * (non-Javadoc)
     *
     * @see Object#toString()
     */
    @Override
    public String toString() {
        ToStringBuilder str = new ToStringBuilder(this)
                .appendSuper(null)
                .append("firstResult", firstResult)
                .append("pageSize", pageSize)
                .append("totalResults", totalResults);

        // now display the name of each entity in the row
        int index = 0;
        for (T obj : results) {
            str.append("obj-" + index++, obj.toString());
        }

        return str.toString();
    }

}
