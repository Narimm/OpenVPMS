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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.component.system.common.query.IConstraint;
import org.openvpms.component.system.common.query.ShortNameConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.util.IMObjectHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Abstract implementation of the {@link Query} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractQuery<T> implements Query<T> {

    /**
     * The archetypes to query.
     */
    private final ShortNameConstraint archetypes;

    /**
     * The type that this query returns.
     */
    private final Class type;

    /**
     * Archetype short names to match on.
     */
    private final String[] shortNames;

    /**
     * The name being queried on.
     */
    private String name;

    /**
     * Determines if the query should be run automatically.
     */
    private boolean auto;

    /**
     * The minimum length of the name field, before queries can be performed.
     */
    private int nameMinLength;

    /**
     * Determines if duplicate rows should be filtered.
     */
    private boolean distinct;

    /**
     * The maxmimum no. of results to return per page.
     */
    private int maxResults = 20;

    /**
     * The default sort constraints. May be <tt>null</tt>
     */
    private SortConstraint[] sort;

    /**
     * The event listener list.
     */
    private List<QueryListener> listeners = new ArrayList<QueryListener>();

    /**
     * Additional constraints to associate with the query. May be <tt>null</tt>.
     */
    private IConstraint constraints;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(AbstractQuery.class);


    /**
     * Construct a new <tt>AbstractQuery</tt> that queries objects with
     * the specified primary short names.
     *
     * @param shortNames the archetype short names
     * @param type       the type that this query returns
     * @throws ArchetypeQueryException if the short names don't match any
     *                                 archetypes
     */
    public AbstractQuery(String[] shortNames, Class type) {
        this(shortNames, true, type);
    }

    /**
     * Construct a new <tt>AbstractQuery</tt> that queries objects with
     * the specified short names.
     *
     * @param shortNames  the archetype short names
     * @param primaryOnly if <tt>true</tt> only include primary archetypes
     * @param type        the type that this query returns
     * @throws ArchetypeQueryException if the short names don't match any
     *                                 archetypes
     */
    @SuppressWarnings("unchecked")
    public AbstractQuery(String[] shortNames, boolean primaryOnly, Class type) {
        this.shortNames = DescriptorHelper.getShortNames(shortNames,
                                                         primaryOnly);
        this.type = type;
        if (IMObject.class.isAssignableFrom(type)) {
            // verify that the specified type matches what the query actually
            // returns
            Class actual = IMObjectHelper.getType(this.shortNames);
            if (!type.isAssignableFrom(actual)) {
                throw new QueryException(QueryException.ErrorCode.InvalidType,
                                         type, actual);

            }
        }
        archetypes = new ShortNameConstraint(shortNames, primaryOnly, true);
    }

    /**
     * Construct a new <tt>AbstractQuery</tt> that queries objects with
     * the specified short names.
     *
     * @param shortNames  the archetype short names
     * @param primaryOnly if <tt>true</tt> only include primary archetypes
     * @throws ArchetypeQueryException if the short names don't match any
     *                                 archetypes
     */
    public AbstractQuery(String[] shortNames, boolean primaryOnly) {
        this.shortNames = DescriptorHelper.getShortNames(shortNames,
                                                         primaryOnly);
        type = IMObjectHelper.getType(this.shortNames);
        archetypes = new ShortNameConstraint(shortNames, primaryOnly, true);
    }

    /**
     * Returns the type that this query returns.
     *
     * @return the type
     */
    public Class getType() {
        return type;
    }

    /**
     * Sets the maximum no. of results to return per page.
     *
     * @param maxResults the maxiomum no. of rows per page
     */
    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    /**
     * Returns the maximum no. of results to return per page.
     *
     * @return the maximum no. of results to return per page
     */
    public int getMaxResults() {
        return maxResults;
    }

    /**
     * Sets the default sort constraint.
     *
     * @param sort the default sort cosntraint. May be <tt>null</tt>
     */
    public void setDefaultSortConstraint(SortConstraint[] sort) {
        this.sort = sort;
    }

    /**
     * Returns the default sort constraint
     *
     * @return the default sort constraint. May be <tt>null</tt>
     */
    public SortConstraint[] getDefaultSortConstraint() {
        return sort;
    }

    /**
     * Performs the query using the default sort constraint (if any).
     *
     * @return the query result set. May be <tt>null</tt>
     * @throws ArchetypeServiceException for any error
     */
    public ResultSet<T> query() {
        return query(sort);
    }

    /**
     * Determines if the query selects a particular object.
     * <p/>
     * This implementation performs a linear search.
     *
     * @param object the object to check
     * @return <tt>true</tt> if the object is selected by the query
     */
    public boolean selects(T object) {
        long start = System.currentTimeMillis();
        Iterator<T> iterator = iterator();
        while (iterator.hasNext()) {
            T next = iterator.next();
            if (next.equals(object)) {
                return true;
            }
        }
        long end = System.currentTimeMillis();
        if ((end - start) > 1000) {
            // If it takes more than a second then optimization is required.
            // Could argue that a second is too long.
            log.warn("Slow query: " + getClass().getName() + " performing linear search");
        }
        return false;
    }

    /**
     * Performs the query using the default sort constraint, and adapts the
     * results to an iterator.
     *
     * @param sort the sort constraint. May be <tt>null</tt>
     * @return an iterator over the results.
     * @throws ArchetypeServiceException if the query fails
     */
    public Iterator<T> iterator(SortConstraint[] sort) {
        return new ResultSetIterator<T>(query(sort));
    }

    /**
     * Performs the query using the default sort constraint, and adapts the
     * results to an iterator.
     *
     * @return an iterator over the results.
     * @throws ArchetypeServiceException if the query fails
     */
    public Iterator<T> iterator() {
        return iterator(sort);
    }

    /**
     * The archetype short names being queried.
     * Any wildcards are expanded.
     *
     * @return the short names being queried
     */
    public String[] getShortNames() {
        return shortNames;
    }

    /**
     * Sets the name to query on.
     *
     * @param name the name. May contain wildcards, or be <tt>null</tt>
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the name being queried on.
     *
     * @return the name. May contain wildcards, or be <tt>null</tt>
     */
    public String getName() {
        return name;
    }


    /**
     * Sets the minimum length of a name before queries can be performed.
     *
     * @param length the minimum name length
     */
    public void setNameMinLength(int length) {
        nameMinLength = length;
    }

    /**
     * Returns the minimum length of a name before queries can be performed
     *
     * @return the minimum length
     */
    public int getNameMinLength() {
        return nameMinLength;
    }

    /**
     * Determines if the query should be run automatically.
     *
     * @param auto if <tt>true</tt> the query should be run automatically
     */
    public void setAuto(boolean auto) {
        this.auto = auto;
    }

    /**
     * Determines if the query should be run automatically.
     *
     * @return <tt>true</tt> if the query should be run automatically;
     *         otherwise <tt>false</tt>
     */
    public boolean isAuto() {
        return auto;
    }

    /**
     * Determines if duplicate rows should be filtered.
     *
     * @param distinct if true, remove duplicate rows
     */
    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    /**
     * Determines if duplicate rows should be filtered.
     *
     * @return <tt>true</tt> if duplicate rows should be removed;
     *         otherwise <tt>false</tt>
     */
    public boolean isDistinct() {
        return distinct;
    }

    /**
     * Add a listener for query events.
     *
     * @param listener the listener to add
     */
    public void addQueryListener(QueryListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove a listener.
     *
     * @param listener the listener to remove
     */
    public void removeQueryListener(QueryListener listener) {
        listeners.remove(listener);
    }


    /**
     * Set query constraints.
     *
     * @param constraints the constraints. May be <tt>null</tt>
     */
    public void setConstraints(IConstraint constraints) {
        this.constraints = constraints;
    }

    /**
     * Returns query contraints.
     *
     * @return the constraints. May be <tt>null</tt>
     */
    public IConstraint getConstraints() {
        return constraints;
    }

    /**
     * Returns the archetypes to select from.
     *
     * @return the archetypes to select from
     */
    public ShortNameConstraint getArchetypes() {
        return archetypes;
    }

    /**
     * Notify listeners to perform a query.
     */
    protected void onQuery() {
        QueryListener[] listeners = this.listeners.toArray(new QueryListener[this.listeners.size()]);
        for (QueryListener listener : listeners) {
            listener.query();
        }
    }
}
