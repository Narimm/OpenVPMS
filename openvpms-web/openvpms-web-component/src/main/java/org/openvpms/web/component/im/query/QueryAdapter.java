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

import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.system.common.query.IConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.focus.FocusGroup;

import java.util.Iterator;


/**
 * Adapts the results of one query to another.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class QueryAdapter<A, T> implements Query<T> {

    /**
     * The query to adapt from.
     */
    private final Query<A> query;

    /**
     * The type that this query returns.
     */
    private final Class type;


    /**
     * Creates a new <tt>QueryAdapter</tt>.
     *
     * @param query the query to adapt from
     * @param type  the type that this query returns
     */
    public QueryAdapter(Query<A> query, Class type) {
        this.query = query;
        this.type = type;
    }

    /**
     * Returns the query component.
     *
     * @return the query component
     */
    public Component getComponent() {
        return query.getComponent();
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
     * @param maxResults the maximum no. of results per page
     */
    public void setMaxResults(int maxResults) {
        query.setMaxResults(maxResults);
    }

    /**
     * Returns the maximum no. of results to return per page.
     *
     * @return the maximum no. of results to return per page
     */
    public int getMaxResults() {
        return query.getMaxResults();
    }

    /**
     * Sets the default sort constraint.
     *
     * @param sort the default sort cosntraint. May be <tt>null</tt>
     */
    public void setDefaultSortConstraint(SortConstraint[] sort) {
        query.setDefaultSortConstraint(sort);
    }

    /**
     * Returns the default sort constraint
     *
     * @return the default sort constraint. May be <tt>null</tt>
     */
    public SortConstraint[] getDefaultSortConstraint() {
        return query.getDefaultSortConstraint();
    }

    /**
     * Performs the query using the default sort constraint (if any).
     *
     * @return the query result set
     * @throws ArchetypeServiceException for any error
     */
    public ResultSet<T> query() {
        ResultSet<A> set = query.query();
        return (set != null) ? convert(set) : null;
    }

    /**
     * Performs the query.
     *
     * @param sort the sort constraint. May be <tt>null</tt>
     * @return the query result set
     * @throws ArchetypeServiceException if the query fails
     */
    public ResultSet<T> query(SortConstraint[] sort) {
        ResultSet<A> set = query.query(sort);
        return (set != null) ? convert(set) : null;
    }

    /**
     * Determines if the query selects a particular object.
     *
     * @param object the object to check
     * @return <tt>true</tt> if the object is selected by the query
     */
    public boolean selects(T object) {
        Iterator<T> iterator = iterator();
        while (iterator.hasNext()) {
            T next = iterator.next();
            if (next.equals(object)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if the query selects a particular object reference.
     *
     * @param reference the object reference to check
     * @return <tt>true</tt> if the object reference is selected by the query
     */
    public boolean selects(IMObjectReference reference) {
        return query.selects(reference);
    }

    /**
     * Performs the query using the default sort constraint (if any), and
     * adapts the results to an iterator.
     *
     * @return an iterator over the results.
     * @throws ArchetypeServiceException if the query fails
     */
    public Iterator<T> iterator() {
        return iterator(getDefaultSortConstraint());
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
     * The archetype short names being queried.
     * Any wildcards are expanded.
     *
     * @return the short names being queried
     */
    public String[] getShortNames() {
        return query.getShortNames();
    }

    /**
     * Sets the name to query on.
     *
     * @param name the name. May contain wildcards, or be <tt>null</tt>
     */
    public void setValue(String name) {
        query.setValue(name);
    }

    /**
     * Returns the name being queried on.
     *
     * @return the name. May contain wildcards, or be <tt>null</tt>
     */
    public String getValue() {
        return query.getValue();
    }

    /**
     * Sets the minimum length of a name before queries can be performed.
     *
     * @param length the length
     */
    public void setValueMinLength(int length) {
        query.setValueMinLength(length);
    }

    /**
     * Returns the minimum length of a name before queries can be performed
     *
     * @return the minimum length
     */
    public int getValueMinLength() {
        return query.getValueMinLength();
    }

    /**
     * Determines if the query should be run automatically.
     *
     * @param auto if <tt>true</tt> run the query automatically
     */
    public void setAuto(boolean auto) {
        query.setAuto(auto);
    }

    /**
     * Determines if the query should be run automatically.
     *
     * @return <tt>true</tt> if the query should be run automaticaly;
     *         otherwise <tt>false</tt>
     */
    public boolean isAuto() {
        return query.isAuto();
    }

    /**
     * Determines if duplicate rows should be filtered.
     *
     * @param distinct if true, remove duplicate rows
     */
    public void setDistinct(boolean distinct) {
        query.setDistinct(distinct);
    }

    /**
     * Determines if dusplicate rows should be filtered.
     *
     * @return <tt>true</tt> if duplicate rows should be removed;
     *         otherwise <tt>false</tt>
     */
    public boolean isDistinct() {
        return query.isDistinct();
    }

    /**
     * Add a listener for query events.
     *
     * @param listener the listener to add
     */
    public void addQueryListener(QueryListener listener) {
        query.addQueryListener(listener);
    }

    /**
     * Remove a listener.
     *
     * @param listener the listener to remove
     */
    public void removeQueryListener(QueryListener listener) {
        query.removeQueryListener(listener);
    }

    /**
     * Set query constraints.
     *
     * @param constraints the constraints
     */
    public void setConstraints(IConstraint constraints) {
        query.setConstraints(constraints);
    }

    /**
     * Returns the query state.
     *
     * @return the query state
     */
    public QueryState getQueryState() {
        return query.getQueryState();
    }

    /**
     * Sets the query state.
     *
     * @param state the query state
     */
    public void setQueryState(QueryState state) {
        query.setQueryState(state);
    }

    /**
     * Returns the focus group for the component.
     *
     * @return the focus group
     */
    public FocusGroup getFocusGroup() {
        return query.getFocusGroup();
    }

    /**
     * Returns the underlying query.
     *
     * @return the underlying query
     */
    public Query<A> getQuery() {
        return query;
    }

    /**
     * Converts a result set.
     *
     * @param set the set to convert
     * @return the converted set
     */
    protected abstract ResultSet<T> convert(ResultSet<A> set);
}
