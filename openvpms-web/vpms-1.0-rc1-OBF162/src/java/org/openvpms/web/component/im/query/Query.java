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


/**
 * Queries objects for display by an {@link Browser}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public interface Query<T> {

    /**
     * Returns the query component.
     *
     * @return the query component
     */
    Component getComponent();

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
     * Performs the query.
     *
     * @param sort the sort constraint. May be <code>null</code>
     * @return the query result set. May be <code>null</code>
     * @throws ArchetypeServiceException if the query fails
     */
    ResultSet<T> query(SortConstraint[] sort);

    /**
     * The archetype short names being queried.
     *
     * @return the short names being queried
     */
    String[] getShortNames();

    /**
     * Sets the name to query on.
     *
     * @param name the name. May contain wildcards, or be <code>null</code>
     */
    void setName(String name);

    /**
     * Returns the name being queried on.
     *
     * @return the name. May contain wildcards, or be <code>null</code>
     */
    String getName();

    /**
     * Sets the minimum length of a name before queries can be performed.
     *
     * @param length
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
     * @param auto if <code>true</code> run the query automatically
     */
    void setAuto(boolean auto);

    /**
     * Determines if the query should be run automatically.
     *
     * @return <code>true</code> if the query should be run automaticaly;
     *         otherwise <code>false</code>
     */
    boolean isAuto();

    /**
     * Determines if duplicate rows should be filtered.
     *
     * @param distinct if true, remove duplicate rows
     */
    void setDistinct(boolean distinct);

    /**
     * Determines if dusplicate rows should be filtered.
     *
     * @return <code>true</code> if duplicate rows should be removed;
     *         otherwise <code>false</code>
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
