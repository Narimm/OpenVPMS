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
import org.openvpms.web.component.focus.FocusGroup;

import java.util.List;


/**
 * Browser for objects matching query criteria.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public interface Browser<T> {

    /**
     * Returns the browser component.
     *
     * @return the browser component
     */
    Component getComponent();

    /**
     * Returns the selected object.
     *
     * @return the selected object, or <code>null</code> if none has been
     *         selected.
     */
    T getSelected();

    /**
     * Select an object.
     *
     * @param object the object to select
     */
    void setSelected(T object);

    /**
     * Returns the objects matching the query.
     *
     * @return the objects matcing the query.
     */
    List<T> getObjects();

    /**
     * Adds a listener to receive notification of selection and query actions.
     *
     * @param listener the listener to add
     */
    void addQueryListener(QueryBrowserListener<T> listener);

    /**
     * Query using the specified criteria, and populate the browser with
     * matches.
     */
    void query();

    /**
     * Returns the focus group.
     *
     * @return the focus group
     */
    FocusGroup getFocusGroup();

    /**
     * Sets focus on the results.
     */
    void setFocusOnResults();
}
