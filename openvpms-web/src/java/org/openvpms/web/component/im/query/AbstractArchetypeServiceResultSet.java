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

package org.openvpms.web.component.im.query;

import org.apache.commons.collections.map.ReferenceMap;
import org.openvpms.component.business.dao.im.Page;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IConstraint;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.util.ErrorHelper;

import java.util.List;


/**
 * Abstract implementation of the {@link ResultSet} interface for result sets
 * that query the {@link IArchetypeService}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class AbstractArchetypeServiceResultSet<T>
        extends AbstractResultSet<T> {

    /**
     * Additional constraints to associate with the query. May be <tt>null</tt>.
     */
    private final IConstraint constraints;

    /**
     * Determines if duplicate results should be filtered.
     */
    private boolean distinct;

    /**
     * The sort criteria. May be <tt>null</tt>.
     */
    private SortConstraint[] sort;

    /**
     * The reference to constrain results to. May be <tt>null</tt>
     */
    private IMObjectReference reference;

    /**
     * The query executor.
     */
    private final QueryExecutor<T> executor;

    /**
     * A cache of retrieved pages. These are referenced via soft references,
     * so that they can be reclaimed by the garbage collector if necessary.
     */
    private ReferenceMap cache = new ReferenceMap();

    /**
     * The count of results matching the criteria, or <tt>-1</tt> if it
     * is not known.
     */
    private int count = -1;

    /**
     * Determines if the count is an estimation or the actual no. of results.
     */
    private boolean estimation = true;

    /**
     * The no. of pages to prefetch and cache.
     */
    private int prefetchPages = 4;


    /**
     * Construct a new <tt>AbstractArchetypeServiceResultSet</tt>.
     *
     * @param pageSize the maximum no. of results per page
     * @param sort     the sort criteria. May be <tt>null</tt>
     * @param executor the query executor
     */
    public AbstractArchetypeServiceResultSet(int pageSize,
                                             SortConstraint[] sort,
                                             QueryExecutor<T> executor) {
        this(null, pageSize, sort, executor);
    }

    /**
     * Construct a new <tt>AbstractArchetypeServiceResultSet</tt>.
     *
     * @param constraints query constraints. May be <tt>null</tt>
     * @param pageSize    the maximum no. of results per page
     * @param sort        the sort criteria. May be <tt>null</tt>
     * @param executor    the query executor
     */
    public AbstractArchetypeServiceResultSet(IConstraint constraints,
                                             int pageSize,
                                             SortConstraint[] sort,
                                             QueryExecutor<T> executor) {
        super(pageSize);
        this.constraints = constraints;
        this.executor = executor;
        setSortConstraint(sort);
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
            ArchetypeQuery query = createQuery(0, 0);
            query.setCountResults(true);
            IArchetypeService service
                    = ArchetypeServiceHelper.getArchetypeService();
            IPage<IMObject> results = service.get(query);
            count = results.getTotalResults();
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
     * @return <tt>true</tt> if the estimated results equals the actual no.
     *         of results
     */
    public boolean isEstimatedActual() {
        return !estimation;
    }

    /**
     * Sort the set. This resets the iterator.
     *
     * @param sort the sort criteria. May be <tt>null</tt>
     */
    public void sort(SortConstraint[] sort) {
        setSortConstraint(sort);
        reset();
    }

    /**
     * Determines if the node is sorted ascending or descending.
     *
     * @return <tt>true</tt> if the node is sorted ascending or no sort
     *         constraint was specified; <tt>false</tt> if it is sorted
     *         descending
     */
    public boolean isSortedAscending() {
        return (sort.length == 0 || sort[0].isAscending());
    }

    /**
     * Returns the sort criteria.
     *
     * @return the sort criteria. Never null
     */
    public SortConstraint[] getSortConstraints() {
        return sort;
    }

    /**
     * Determines if duplicate results should be filtered.
     *
     * @param distinct if true, remove duplicate results
     */
    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    /**
     * Determines if duplicate results should be filtered.
     *
     * @return <tt>true</tt> if duplicate results should be removed;
     *         otherwise <tt>false</tt>
     */
    public boolean isDistinct() {
        return distinct;
    }

    /**
     * Sets a reference to constrain the query on.
     *
     * @param reference the reference. May be <tt>null</tt>
     */
    public void setReferenceConstraint(IMObjectReference reference) {
        this.reference = reference;
    }

    /**
     * Returns the reference to constrain the query on.
     *
     * @return the reference. May be <tt>null</tt>
     */
    public IMObjectReference getReferenceConstraint() {
        return reference;
    }

    /**
     * Sets the sort criteria.
     *
     * @param sort the sort criteria. May be <tt>null</tt>
     */
    protected void setSortConstraint(SortConstraint[] sort) {
        this.sort = (sort != null) ? sort : new SortConstraint[0];
    }

    /**
     * Returns the query constraints.
     *
     * @return the query constraints
     */
    protected IConstraint getConstraints() {
        return constraints;
    }

    /**
     * Creates a new archetype query.
     *
     * @return a new archetype query
     */
    protected abstract ArchetypeQuery createQuery();

    /**
     * Returns a new archetype query.
     * This implementation delegates creation to {@link #createQuery},
     * before adding any {@link #getConstraints()} and
     * invoking {@link #addSortConstraints}.
     *
     * @param firstResult the first result of the page to retrieve
     * @param maxResults  the maximun no of results in the page
     * @return a new query
     */
    protected ArchetypeQuery createQuery(int firstResult, int maxResults) {
        ArchetypeQuery query = createQuery();
        query.setFirstResult(firstResult);
        query.setMaxResults(maxResults);
        query.setDistinct(isDistinct());
        query.setCountResults(false);
        IConstraint constraints = getConstraints();
        if (constraints != null) {
            query.add(constraints);
        }
        if (reference != null) {
            addReferenceConstraint(query, reference);
        }
        addSortConstraints(query);
        return query;
    }

    /**
     * Adds a reference constraint.
     *
     * @param query     the archetype query
     * @param reference the reference to constrain the query on
     */
    protected void addReferenceConstraint(ArchetypeQuery query, IMObjectReference reference) {
        // NOTE: can't use an ObjectRefConstraint as this adds a join.
        // Also, this won't detect if an object has changed type, but this is not a very likely scenario
        query.add(new NodeConstraint("id", reference.getId()));
    }

    /**
     * Adds sort constraints.
     * This implementation adds all those returned by
     * {@link #getSortConstraints()}.
     *
     * @param query the query to add the constraints to
     */
    protected void addSortConstraints(ArchetypeQuery query) {
        for (SortConstraint sort : getSortConstraints()) {
            query.add(sort);
        }
    }

    /**
     * Returns the specified page.
     *
     * @param page the page no.
     * @return the page, or <tt>null</tt> if there is no such page
     * @throws ArchetypeServiceException for any archetype service error
     */
    @SuppressWarnings("unchecked")
    protected IPage<T> get(int page) {
        IPage<T> result = (IPage<T>) cache.get(page);
        if (result == null) {
            result = query(page);
        }
        return result;
    }

    /**
     * Queries the specified page.
     *
     * @param page the page to query
     * @return the page, or <tt>null</tt>
     * @throws ArchetypeServiceException for any archetype service error
     */
    private IPage<T> query(int page) {
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
            ArchetypeQuery query = createQuery(firstResult, maxResults);
            String[] nodes = getNodes();
            IPage<T> matches = executor.query(query, nodes);

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
                                subResults, firstResult + from, pageSize,
                                count);
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
