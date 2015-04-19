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

package org.openvpms.component.system.common.query;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;


/**
 * Abstract implementation of the {@link IArchetypeQuery} interface.
 *
 * @author Jim Alateras
 * @author Tim Anderson
 */
public class AbstractArchetypeQuery implements IArchetypeQuery {

    /**
     * The default maximum no. of results to retrieve.
     */
    public static final int DEFAULT_MAX_RESULTS = 25;

    /**
     * Define the first result to be retrieve if paging is being used.
     */
    private int firstResult = 0;

    /**
     * Define the maximum number of results to be retrieve.
     */
    private int maxResults = DEFAULT_MAX_RESULTS;

    /**
     * Determines if the total no. of results should be counted and returned
     * in the resulting {@link IPage}.
     */
    private boolean count = false;


    /**
     * Returns the first result.
     *
     * @return the first result
     */
    public int getFirstResult() {
        return firstResult;
    }

    /**
     * Sets the first result.
     *
     * @param firstResult the first result
     * @return this query
     */
    public IArchetypeQuery setFirstResult(int firstResult) {
        this.firstResult = firstResult;
        return this;
    }

    /**
     * Returns the maximum number of results to retrieve.
     *
     * @return the maximum no. of results to retrieve or {@link #ALL_RESULTS} to
     *         retrieve all results
     */
    public int getMaxResults() {
        return maxResults;
    }

    /**
     * Sets the maximum number of results to retrieve.  If not set,
     * defaults to {@link #DEFAULT_MAX_RESULTS}.
     *
     * @param maxResults the maximum no. of results to retrieve
     * @return this query
     */
    public IArchetypeQuery setMaxResults(int maxResults) {
        this.maxResults = maxResults;
        return this;
    }

    /**
     * Determines if the total no. of results should be counted and returned
     * in the resulting {@link IPage}.
     * Only applies when <code>getMaxResults() != ALL_RESULTS</code>.
     *
     * @return <code>true</code> if the total no. of results should be counted,
     *         otherwise <code>false</code>. Defaults to <code>true</code>
     */
    public boolean countResults() {
        return count;
    }

    /**
     * Determines if the total no. of results should be counted and returned
     * in the resulting {@link IPage}.
     *
     * @param count if <code>true</code> count the no. of results
     * @return this query
     */
    public IArchetypeQuery setCountResults(boolean count) {
        this.count = count;
        return this;
    }

    /**
     * (non-Javadoc)
     *
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof AbstractArchetypeQuery)) {
            return false;
        }

        AbstractArchetypeQuery rhs = (AbstractArchetypeQuery) obj;
        return new EqualsBuilder()
                .appendSuper(super.equals(rhs))
                .append(firstResult, rhs.firstResult)
                .append(maxResults, rhs.maxResults)
                .append(count, rhs.count)
                .isEquals();
    }

    /**
     * (non-Javadoc)
     *
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append("firstResult", firstResult)
                .append("maxResults", maxResults)
                .append("count", count)
                .toString();
    }

    /**
     * (non-Javadoc)
     *
     * @see Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
