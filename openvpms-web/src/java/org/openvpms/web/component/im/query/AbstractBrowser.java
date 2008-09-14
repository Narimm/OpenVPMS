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

import org.openvpms.web.component.focus.FocusGroup;

import java.util.ArrayList;
import java.util.List;


/**
 * Abstract implementation of the {@link Browser} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractBrowser<T> implements Browser<T> {

    /**
     * The event listener list.
     */
    private List<QueryBrowserListener<T>> listeners
            = new ArrayList<QueryBrowserListener<T>>();

    /**
     * The focus group.
     */
    private FocusGroup focusGroup = new FocusGroup(getClass().getName());


    /**
     * Adds a listener to receive notification of selection and query actions.
     *
     * @param listener the listener to add
     */
    public void addQueryListener(QueryBrowserListener<T> listener) {
        listeners.add(listener);
    }

    /**
     * Returns the focus group.
     *
     * @return the focus group
     */
    public FocusGroup getFocusGroup() {
        return focusGroup;
    }

    /**
     * Notifies any registered query listeners.
     */
    protected void notifyQueryListeners() {
        QueryBrowserListener[] listeners
                = this.listeners.toArray(new QueryBrowserListener[0]);
        for (QueryBrowserListener listener : listeners) {
            listener.query();
        }
    }

    /**
     * Notifies listeners when an object is selected.
     *
     * @param selected the selected object
     */
    @SuppressWarnings("unchecked")
    protected void notifySelected(T selected) {
        QueryBrowserListener<T>[] listeners
                = (QueryBrowserListener<T>[]) this.listeners.toArray(
                new QueryBrowserListener[0]);
        for (QueryBrowserListener<T> listener : listeners) {
            listener.selected(selected);
        }
    }

}
