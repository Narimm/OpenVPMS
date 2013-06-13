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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.query;

import org.apache.commons.collections.map.ReferenceMap;
import org.openvpms.component.business.dao.im.Page;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.web.component.util.ErrorHelper;

import java.util.List;


/**
 * An {@link ResultSet} that caches results.
 *
 * @author Tim Anderson
 */
public abstract class AbstractCachingResultSet<T> extends AbstractResultSet<T> {

    /**
     * A cache of retrieved pages. These are referenced via soft references,
     * so that they can be reclaimed by the garbage collector if necessary.
     */
    private ReferenceMap cache = new ReferenceMap();

    /**
     * The count of results matching the criteria, or {@code -1} if it is not known.
     */
    private int count = -1;

    /**
     * Determines if the count is an estimation or the actual no. of results.
     */
    private boolean estimation = true;

    /**
     * The no. of pages to prefetch and cache.
     */
    private int prefetchPages;

    /**
     * The default no. of pages to prefetch.
     */
    private static final int PREFETCH = 4;

    /**
     * Constructs an {@link AbstractCachingResultSet}.
     *
     * @param pageSize the maximum no. of results per page
     */
    public AbstractCachingResultSet(int pageSize) {
        this(pageSize, PREFETCH);
    }

    /**
     * Constructs an {@link AbstractCachingResultSet}.
     *
     * @param pageSize      the maximum no. of results per page
     * @param prefetchPages the no. of pages to prefetch and cache
     */
    public AbstractCachingResultSet(int pageSize, int prefetchPages) {
        super(pageSize);
        this.prefetchPages = prefetchPages;
    }

    /**
     * Reset the iterator.
     */
    @Override
    public void reset() {
        cache.clear();
        count = -1;
        estimation = true;
        super.reset();
    }

    /**
     * Returns the total number of results matching the query criteria.
     * For complex queries, this operation can be expensive. If an exact
     * count is not required, use {@link #getEstimatedResults()}.
     *
     * @return the total number of results
     */
    public int getResults() {
        if (count == -1 || estimation) {
            count = countResults();
            estimation = false;
        }
        return count;
    }

    /**
     * Returns an estimation of the total no. of results matching the query
     * criteria.
     *
     * @return an estimation of the total no. of results
     */
    public int getEstimatedResults() {
        return (count == -1) ? 0 : count;
    }

    /**
     * Determines if the estimated no. of results is the actual total, i.e
     * if {@link #getEstimatedResults()} would return the same as
     * {@link #getResults()}, and {@link #getEstimatedPages()} would return
     * the same as {@link #getPages()}.
     *
     * @return {@code true} if the estimated results equals the actual no.
     *         of results
     */
    public boolean isEstimatedActual() {
        return !estimation;
    }

    /**
     * Performs a query.
     *
     * @param firstResult the first result of the page to retrieve
     * @param maxResults  the maximum no. of results in the page
     * @return the page, or {@code null}
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected abstract IPage<T> query(int firstResult, int maxResults);

    /**
     * Counts the no. of results matching the query criteria.
     *
     * @return the total number of results
     */
    protected abstract int countResults();

    /**
     * Returns the specified page.
     *
     * @param page the page no.
     * @return the page, or {@code null} if there is no such page
     * @throws ArchetypeServiceException for any archetype service error
     */
    @SuppressWarnings("unchecked")
    protected IPage<T> get(int page) {
        IPage<T> result = (IPage<T>) cache.get(page);
        if (result == null) {
            if (page > 0 && getPageSize() == ArchetypeQuery.ALL_RESULTS) {
                // nothing to do - results should have been returned in the first page
            } else {
                result = query(page);
            }
        }
        return result;
    }

    /**
     * Queries the specified page.
     *
     * @param page the page to query
     * @return the page, or {@code null}
     */
    protected IPage<T> query(int page) {
        IPage<T> result = null;
        int firstResult = getFirstResult(page);
        int pageSize = getPageSize();
        int maxResults = pageSize;
        int pages = 1;  // no. of requested pages
        if (maxResults != ArchetypeQuery.ALL_RESULTS && prefetchPages != 0) {
            maxResults = pageSize * prefetchPages;
            pages = prefetchPages;
        }
        try {
            IPage<T> matches = query(firstResult, maxResults);

            List<T> results = matches.getResults();
            if (results.isEmpty()) {
                cache.remove(page);
            } else if (pages == 1) {
                result = matches;
                cache.put(page, result);
            } else {
                // need to split the matches into multiple pages.
                // Each page will be cached, and the first returned.
                for (int i = 0; i < pages; ++i) {
                    int from = i * pageSize;
                    int to;
                    if (from < results.size()) {
                        if (((from + pageSize) >= results.size())) {
                            to = results.size();
                        } else {
                            to = from + pageSize;
                        }
                        List<T> subResults = results.subList(from, to);
                        IPage<T> subPage = new Page<T>(
                                subResults, firstResult + from, pageSize, count);
                        if (i == 0) {
                            result = subPage;
                        }
                        cache.put(page + i, subPage);
                    } else {
                        cache.remove(i);
                    }
                }
            }

            // update the count of total results if necessary
            if (matches.getTotalResults() != -1) {
                count = matches.getTotalResults();
                estimation = false;
            } else if (results.isEmpty()) {
                if (count > firstResult) {
                    count = firstResult - 1;
                    estimation = true;
                }
            } else {
                int lastResult = firstResult + results.size();
                if (lastResult > count) {
                    count = lastResult;
                    estimation = (results.size() == maxResults);
                }
            }
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
        return result;
    }

}
