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
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.query.IConstraint;
import org.openvpms.component.system.common.query.SortConstraint;


/**
 * Query facility for {@link IMObject} instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public interface Query<T extends IMObject> {

    /**
     * Returns the query component.
     *
     * @return the query component
     */
    Component getComponent();

    /**
     * Sets the maximum no. of rows to return per page.
     *
     * @param rows the maxiomum no. of rows per page
     */
    void setMaxRows(int rows);

    /**
     * Returns the maximum no. of rows to return per page.
     *
     * @return the maximum no. of rows to return per page
     */
    int getMaxRows();

    /**
     * Performs the query.
     *
     * @param sort the sort constraint. May be <code>null</code>
     * @return the query result set. May be <code>null</code>
     */
    ResultSet<T> query(SortConstraint[] sort);

    /**
     * The archetype short names being queried.
     *
     * @return the short names being queried
     */
    String[] getShortNames();

    /**
     * Determines if the query should be run automatically.
     *
     * @return <code>true</code> if the query should be run automaticaly;
     *         otherwie <code>false</code>
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

}
