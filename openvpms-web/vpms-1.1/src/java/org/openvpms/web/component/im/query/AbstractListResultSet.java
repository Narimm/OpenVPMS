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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.query;

import org.openvpms.component.business.dao.im.Page;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;

import java.util.ArrayList;
import java.util.List;


/**
 * Paged result set where the results are pre-loaded from list.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractListResultSet<T> extends AbstractResultSet<T> {

    /**
     * The query objects.
     */
    private final List<T> objects;


    /**
     * Constructs a new <tt>AbstractListResultSet</tt>.
     *
     * @param objects  the objects
     * @param pageSize the maximum no. of results per page
     */
    public AbstractListResultSet(List<T> objects, int pageSize) {
        super(pageSize);
        this.objects = objects;
        reset();
    }

    /**
     * Returns the total number of results matching the query criteria.
     * For complex queries, this operation can be expensive. If an exact
     * count is not required, use {@link #getEstimatedResults()}.
     *
     * @return the total number of results
     */
    public int getResults() {
        return objects.size();
    }

    /**
     * Returns an estimation of the total no. of results matching the query
     * criteria.
     *
     * @return an estimation of the total no. of results
     */
    public int getEstimatedResults() {
        return getResults();
    }

    /**
     * Determines if the estimated no. of results is the actual total, i.e
     * if {@link #getEstimatedResults()} would return the same as
     * {@link #getResults()}, and {@link #getEstimatedPages()} would return
     * the same as {@link #getPages()}.
     *
     * @return <tt>true</tt> if the estimated results equals the actual no.
     *         of results
     */
    public boolean isEstimatedActual() {
        return true;
    }

    /**
     * Determines if duplicate results should be filtered.
     *
     * @param distinct if true, remove duplicate results
     */
    public void setDistinct(boolean distinct) {
        // no-op
    }

    /**
     * Determines if duplicate results should be filtered.
     *
     * @return <tt>true</tt> if duplicate results should be removed;
     *         otherwise <tt>false</tt>
     */
    public boolean isDistinct() {
        return false;
    }

    /**
     * Returns the specified page.
     *
     * @param page the page no.
     * @return the page, or <tt>null</tt> if there is no such page
     */
    protected IPage<T> get(int page) {
        Page<T> result = null;
        int from = page * getPageSize();
        if (from < objects.size()) {
            int to;
            int maxResults = getPageSize();
            if (maxResults == ArchetypeQuery.ALL_RESULTS
                    || ((from + maxResults) >= objects.size())) {
                to = objects.size();
            } else {
                to = from + maxResults;
            }
            List<T> rows = new ArrayList<T>(objects.subList(from, to));
            result = new Page<T>(rows, page, maxResults, objects.size());
        }
        return result;
    }

    /**
     * Returns the underlying list.
     *
     * @return the underlying list
     */
    protected List<T> getObjects() {
        return objects;
    }

}
