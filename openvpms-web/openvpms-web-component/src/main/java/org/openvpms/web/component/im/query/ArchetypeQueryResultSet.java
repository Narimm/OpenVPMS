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

package org.openvpms.web.component.im.query;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.SortConstraint;

/**
 * A {@link ResultSet} that uses an {@link ArchetypeQuery}.
 *
 * @author Tim Anderson
 */
public class ArchetypeQueryResultSet<T> extends AbstractResultSet<T> {

    /**
     * The query.
     */
    private final ArchetypeQuery query;

    /**
     * The sort constraint.
     */
    private SortConstraint[] sort;

    /**
     * The query executor.
     */
    private final QueryExecutor<T> executor;

    /**
     * The last returned page.
     */
    private IPage<T> lastPage;

    /**
     * The last returned page index.
     */
    private int lastIndex = -1;


    /**
     * Construct an {@link ArchetypeQueryResultSet}.
     *
     * @param pageSize the maximum no. of results per page
     */
    public ArchetypeQueryResultSet(ArchetypeQuery query, int pageSize, QueryExecutor<T> executor) {
        super(pageSize);
        this.query = query;
        this.executor = executor;
    }

    /**
     * Sorts the set. This resets the iterator.
     *
     * @param sort the sort criteria. May be {@code null}
     */
    @Override
    public void sort(SortConstraint[] sort) {
        removeSort();
        addSort(sort);
        reset();
    }

    /**
     * Reset the iterator.
     */
    @Override
    public void reset() {
        super.reset();
        lastPage = null;
        lastIndex = -1;
    }

    /**
     * Returns the total number of results matching the query criteria.
     * For complex queries, this operation can be expensive. If an exact
     * count is not required, use {@link #getEstimatedResults()}.
     *
     * @return the total number of results
     */
    @Override
    public int getResults() {
        query.setCountResults(true);
        IArchetypeService service = ArchetypeServiceHelper.getArchetypeService();
        IPage<IMObject> results = service.get(query);
        return results.getTotalResults();
    }

    /**
     * Returns an estimation of the total no. of results matching the query
     * criteria.
     *
     * @return an estimation of the total no. of results
     */
    @Override
    public int getEstimatedResults() {
        return getResults();
    }

    /**
     * Determines if the estimated no. of results is the actual total, i.e
     * if {@link #getEstimatedResults()} would return the same as
     * {@link #getResults()}, and {@link #getEstimatedPages()} would return
     * the same as {@link #getPages()}.
     *
     * @return {@code true} if the estimated results equals the actual no. of results
     */
    @Override
    public boolean isEstimatedActual() {
        return true;  //
    }

    /**
     * Determines if the node is sorted ascending or descending.
     *
     * @return {@code true} if the node is sorted ascending or no sort constraint was specified; {@code false} if it is
     *         sorted descending
     */
    @Override
    public boolean isSortedAscending() {
        return sort != null && sort.length != 0 && sort[0].isAscending();
    }

    /**
     * Returns the sort criteria.
     *
     * @return the sort criteria. Never null
     */
    @Override
    public SortConstraint[] getSortConstraints() {
        return sort != null ? sort : new SortConstraint[0];
    }

    /**
     * Determines if duplicate results should be filtered.
     *
     * @param distinct if true, remove duplicate results
     */
    @Override
    public void setDistinct(boolean distinct) {
        query.setDistinct(distinct);
    }

    /**
     * Determines if duplicate results should be filtered.
     *
     * @return {@code true} if duplicate results should be removed; otherwise {@code false}
     */
    @Override
    public boolean isDistinct() {
        return query.isDistinct();
    }

    /**
     * Returns the specified page.
     *
     * @param page the page no.
     * @return the page, or {@code null} if there is no such page
     */
    @Override
    protected IPage<T> get(int page) {
        IPage<T> result;
        if (page != lastIndex) {
            int firstResult = getFirstResult(page);
            int pageSize = getPageSize();
            query.setCountResults(false);
            query.setFirstResult(firstResult);
            query.setMaxResults(pageSize);
            result = executor.query(query, getNodes());
            if (result.getResults().isEmpty()) {
                result = null;
            }
            lastIndex = page;
            lastPage = result;
        } else {
            result = lastPage;
        }
        return result;
    }

    /**
     * Removes the existing sort constraints.
     */
    private void removeSort() {
        if (sort != null) {
            for (SortConstraint s : sort) {
                query.remove(s);
            }
        }
        sort = null;
    }

    /**
     * Adds a sort constraint.
     *
     * @param sort the sort constraints. May be {@code null}
     */
    private void addSort(SortConstraint[] sort) {
        if (sort != null) {
            for (SortConstraint s : sort) {
                query.add(s);
            }
        }
        this.sort = sort;
    }
}
