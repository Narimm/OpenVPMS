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

import nextapp.echo2.app.Component;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.system.common.query.IConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.focus.FocusGroup;

import java.util.Iterator;


/**
 * Queries objects for display by an {@link Browser}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public interface Query<T> extends Iterable<T> {

    /**
     * Returns the query component.
     *
     * @return the query component
     */
    Component getComponent();

    /**
     * Returns the type that this query returns.
     *
     * @return the type
     */
    Class getType();

    /**
     * Sets the maximum no. of results to return per page.
     *
     * @param maxResults the maximum no. of results per page
     */
    void setMaxResults(int maxResults);

    /**
     * Returns the maximum no. of results to return per page.
     *
     * @return the maximum no. of results to return per page
     */
    int getMaxResults();

    /**
     * Sets the default sort constraint.
     *
     * @param sort the default sort cosntraint. May be <tt>null</tt>
     */
    void setDefaultSortConstraint(SortConstraint[] sort);

    /**
     * Returns the default sort constraint
     *
     * @return the default sort constraint. May be <tt>null</tt>
     */
    SortConstraint[] getDefaultSortConstraint();

    /**
     * Performs the query using the default sort constraint (if any).
     *
     * @return the query result set. May be <tt>null</tt>
     * @throws ArchetypeServiceException for any error
     */
    ResultSet<T> query();

    /**
     * Performs the query.
     *
     * @param sort the sort constraint. May be <tt>null</tt>
     * @return the query result set. May be <tt>null</tt>
     * @throws ArchetypeServiceException if the query fails
     */
    ResultSet<T> query(SortConstraint[] sort);

    /**
     * Determines if the query selects a particular object.
     *
     * @param object the object to check
     * @return <tt>true</tt> if the object is selected by the query
     */
    boolean selects(T object);

    /**
     * Performs the query using the default sort constraint (if any), and
     * adapts the results to an iterator.
     *
     * @return an iterator over the results.
     * @throws ArchetypeServiceException if the query fails
     */
    Iterator<T> iterator();

    /**
     * Performs the query using the default sort constraint, and adapts the
     * results to an iterator.
     *
     * @param sort the sort constraint. May be <tt>null</tt>
     * @return an iterator over the results.
     * @throws ArchetypeServiceException if the query fails
     */
    Iterator<T> iterator(SortConstraint[] sort);

    /**
     * The archetype short names being queried.
     * Any wildcards are expanded.
     *
     * @return the short names being queried
     */
    String[] getShortNames();

    /**
     * Sets the name to query on.
     *
     * @param name the name. May contain wildcards, or be <tt>null</tt>
     */
    void setName(String name);

    /**
     * Returns the name being queried on.
     *
     * @return the name. May contain wildcards, or be <tt>null</tt>
     */
    String getName();

    /**
     * Sets the minimum length of a name before queries can be performed.
     *
     * @param length the minimum length
     */
    void setNameMinLength(int length);

    /**
     * Returns the minimum length of a name before queries can be performed
     *
     * @return the minimum length
     */
    int getNameMinLength();

    /**
     * Determines if the query should be run automatically.
     *
     * @param auto if <tt>true</tt> run the query automatically
     */
    void setAuto(boolean auto);

    /**
     * Determines if the query should be run automatically.
     *
     * @return <tt>true</tt> if the query should be run automaticaly;
     *         otherwise <tt>false</tt>
     */
    boolean isAuto();

    /**
     * Determines if duplicate rows should be filtered.
     *
     * @param distinct if true, remove duplicate rows
     */
    void setDistinct(boolean distinct);

    /**
     * Determines if duplicate rows should be filtered.
     *
     * @return <tt>true</tt> if duplicate rows should be removed;
     *         otherwise <tt>false</tt>
     */
    boolean isDistinct();

    /**
     * Add a listener for query events.
     *
     * @param listener the listener to add
     */
    void addQueryListener(QueryListener listener);

    /**
     * Remove a listener.
     *
     * @param listener the listener to remove
     */
    void removeQueryListener(QueryListener listener);

    /**
     * Set query constraints.
     *
     * @param constraints the constraints
     */
    void setConstraints(IConstraint constraints);

    /**
     * Returns the focus group for the component.
     *
     * @return the focus group
     */
    FocusGroup getFocusGroup();

}
