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

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IConstraint;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.ObjectRefConstraint;
import org.openvpms.component.system.common.query.SortConstraint;


/**
 * Abstract implementation of the {@link ResultSet} interface for result sets
 * that query the {@link IArchetypeService}.
 *
 * @author Tim Anderson
 */
public abstract class AbstractArchetypeServiceResultSet<T>
        extends AbstractCachingResultSet<T> {

    /**
     * Additional constraints to associate with the query. May be {@code null}.
     */
    private final IConstraint constraints;

    /**
     * Determines if duplicate results should be filtered.
     */
    private boolean distinct;

    /**
     * The sort criteria. May be {@code null}.
     */
    private SortConstraint[] sort;

    /**
     * The reference to constrain results to. May be {@code null}
     */
    private IMObjectReference reference;

    /**
     * The query executor.
     */
    private final QueryExecutor<T> executor;


    /**
     * Construct a new {@code AbstractArchetypeServiceResultSet}.
     *
     * @param pageSize the maximum no. of results per page
     * @param sort     the sort criteria. May be {@code null}
     * @param executor the query executor
     */
    public AbstractArchetypeServiceResultSet(int pageSize,
                                             SortConstraint[] sort,
                                             QueryExecutor<T> executor) {
        this(null, pageSize, sort, executor);
    }

    /**
     * Construct a new {@code AbstractArchetypeServiceResultSet}.
     *
     * @param constraints query constraints. May be {@code null}
     * @param pageSize    the maximum no. of results per page
     * @param sort        the sort criteria. May be {@code null}
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
     * Sort the set. This resets the iterator.
     *
     * @param sort the sort criteria. May be {@code null}
     */
    public void sort(SortConstraint[] sort) {
        setSortConstraint(sort);
        reset();
    }

    /**
     * Determines if the node is sorted ascending or descending.
     *
     * @return {@code true} if the node is sorted ascending or no sort
     *         constraint was specified; {@code false} if it is sorted
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
     * @return {@code true} if duplicate results should be removed;
     *         otherwise {@code false}
     */
    public boolean isDistinct() {
        return distinct;
    }

    /**
     * Sets a reference to constrain the query on.
     *
     * @param reference the reference. May be {@code null}
     */
    public void setReferenceConstraint(IMObjectReference reference) {
        this.reference = reference;
    }

    /**
     * Returns the reference to constrain the query on.
     *
     * @return the reference. May be {@code null}
     */
    public IMObjectReference getReferenceConstraint() {
        return reference;
    }

    /**
     * Sets the sort criteria.
     *
     * @param sort the sort criteria. May be {@code null}
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
     * @param maxResults  the maximum no of results in the page
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
        query.add(new ObjectRefConstraint(reference));
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
     * Performs a query.
     *
     * @param firstResult the first result of the page to retrieve
     * @param maxResults  the maximum no. of results in the page
     * @return the page, or {@code null}
     * @throws ArchetypeServiceException for any archetype service error
     */
    @Override
    protected IPage<T> query(int firstResult, int maxResults) {
        ArchetypeQuery query = createQuery(firstResult, maxResults);
        String[] nodes = getNodes();
        return executor.query(query, nodes);
    }

    /**
     * Counts the no. of results matching the query criteria.
     *
     * @return the total number of results
     */
    @Override
    protected int countResults() {
        ArchetypeQuery query = createQuery(0, 0);
        query.setCountResults(true);
        IArchetypeService service = ArchetypeServiceHelper.getArchetypeService();
        IPage<IMObject> results = service.get(query);
        return results.getTotalResults();
    }
}
