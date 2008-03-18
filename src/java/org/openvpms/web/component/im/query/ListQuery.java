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
import nextapp.echo2.app.Label;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.query.IConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.focus.FocusGroup;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.im.util.IMObjectSorter;
import org.openvpms.web.component.util.LabelFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Query where the results are pre-loaded from a list.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ListQuery<T> implements Query<T> {

    /**
     * The objects to return.
     */
    private final List<T> objects;

    /**
     * The type that this query returns.
     */
    private final Class type;

    /**
     * The archetype short names.
     */
    private final String[] shortNames;

    /**
     * The default sort constraints. May be <tt>null</tt>
     */
    private SortConstraint[] sort;

    /**
     * The maxmimum no. of results to return per page.
     */
    private int maxResults = 20;

    /**
     * The query component.
     */
    private Label component;

    /**
     * The focus group.
     */
    private FocusGroup group;


    /**
     * Creates a new <tt>ListQuery</tt>.
     *
     * @param objects   the objects that the query returns
     * @param shortName the archetype short name(s) of the objects. May contain
     *                  wildcards
     * @param type      the type that this query returns
     */
    public ListQuery(List<T> objects, String shortName, Class<T> type) {
        this(objects, new String[]{shortName}, type);
    }

    /**
     * Creates a new <tt>ListQuery</tt>.
     *
     * @param objects    the objects that the query returns
     * @param shortNames the archetype short names of the objects. May contain
     *                   wildcards
     * @param type       the type that this query returns
     */
    public ListQuery(List<T> objects, String[] shortNames, Class<T> type) {
        this.objects = objects;
        this.shortNames = DescriptorHelper.getShortNames(shortNames, true);
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
        group = new FocusGroup("ListQuery");
    }

    /**
     * Returns the query component.
     *
     * @return the query component
     */
    public Component getComponent() {
        if (component == null) {
            component = LabelFactory.create();
        }
        return component;
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
     * @return the query result set
     * @throws ArchetypeServiceException for any error
     */
    public ResultSet<T> query() {
        return query(sort);
    }

    /**
     * Performs the query.
     *
     * @param sort the sort constraint. May be <tt>null</tt>
     * @return the query result set
     * @throws ArchetypeServiceException if the query fails
     */
    @SuppressWarnings("unchecked")
    public ResultSet<T> query(SortConstraint[] sort) {
        if (sort != null && IMObject.class.isAssignableFrom(type)) {
            List sorted = new ArrayList(objects);
            IMObjectSorter.sort(sorted, sort);
            return new ListResultSet<T>(sorted, getMaxResults());
        }
        return new ListResultSet<T>(objects, getMaxResults());
    }

    /**
     * Performs the query using the default sort constraint (if any), and
     * adapts the results to an iterator.
     *
     * @return an iterator over the results.
     * @throws ArchetypeServiceException if the query fails
     */
    public Iterator<T> iterator() {
        return iterator(sort);
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
        return shortNames;
    }

    /**
     * Sets the name to query on.
     *
     * @param name the name. May contain wildcards, or be <tt>null</tt>
     */
    public void setName(String name) {
    }

    /**
     * Returns the name being queried on.
     *
     * @return the name. May contain wildcards, or be <tt>null</tt>
     */
    public String getName() {
        return null;
    }

    /**
     * Sets the minimum length of a name before queries can be performed.
     *
     * @param length
     */
    public void setNameMinLength(int length) {
    }

    /**
     * Returns the minimum length of a name before queries can be performed
     *
     * @return the minimum length
     */
    public int getNameMinLength() {
        return 0;
    }

    /**
     * Determines if the query should be run automatically.
     *
     * @param auto if <tt>true</tt> run the query automatically
     */
    public void setAuto(boolean auto) {
    }

    /**
     * Determines if the query should be run automatically.
     *
     * @return <tt>true</tt> if the query should be run automaticaly;
     *         otherwise <tt>false</tt>
     */
    public boolean isAuto() {
        return true;
    }

    /**
     * Determines if duplicate rows should be filtered.
     *
     * @param distinct if true, remove duplicate rows
     */
    public void setDistinct(boolean distinct) {
    }

    /**
     * Determines if dusplicate rows should be filtered.
     *
     * @return <tt>true</tt> if duplicate rows should be removed;
     *         otherwise <tt>false</tt>
     */
    public boolean isDistinct() {
        return false;
    }

    /**
     * Add a listener for query events.
     *
     * @param listener the listener to add
     */
    public void addQueryListener(QueryListener listener) {
    }

    /**
     * Remove a listener.
     *
     * @param listener the listener to remove
     */
    public void removeQueryListener(QueryListener listener) {
    }

    /**
     * Set query constraints.
     *
     * @param constraints the constraints
     */
    public void setConstraints(IConstraint constraints) {
    }

    /**
     * Returns the focus group for the component.
     *
     * @return the focus group
     */
    public FocusGroup getFocusGroup() {
        return group;
    }
}
