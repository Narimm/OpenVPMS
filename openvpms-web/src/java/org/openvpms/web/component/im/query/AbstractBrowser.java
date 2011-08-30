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
    private List<BrowserListener<T>> listeners = new ArrayList<BrowserListener<T>>();

    /**
     * The focus group.
     */
    private FocusGroup focusGroup = new FocusGroup(getClass().getName());


    /**
     * Adds a listener to receive notification of selection and query actions.
     *
     * @param listener the listener to add
     */
    public void addBrowserListener(BrowserListener<T> listener) {
        listeners.add(listener);
    }

    /**
     * Removes a listener to stop receive notification of selection and query actions.
     *
     * @param listener the listener to remove
     */
    public void removeBrowserListener(BrowserListener<T> listener) {
        listeners.remove(listener);
    }

    /**
     * Returns the browser state.
     * <p/>
     * This implementation always returns <tt>null</tt>.
     *
     * @return <tt>null</tt>
     */
    public BrowserState getBrowserState() {
        return null;
    }

    /**
     * Sets the browser state.
     * <p/>
     * This implementation is a bo-op.
     *
     * @param state the state
     */
    public void setBrowserState(BrowserState state) {
        // do nothing
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
    protected void notifyBrowserListeners() {
        for (BrowserListener<T> listener : getBrowserListeners()) {
            listener.query();
        }
    }

    /**
     * Notifies listeners when an object is selected.
     *
     * @param selected the selected object
     */
    protected void notifySelected(T selected) {
        for (BrowserListener<T> listener : getBrowserListeners()) {
            listener.selected(selected);
        }
    }

    /**
     * Notifies listeners when an object is browsed.
     *
     * @param browsed the browsed object
     */
    protected void notifyBrowsed(T browsed) {
        for (BrowserListener<T> listener : getBrowserListeners()) {
            listener.browsed(browsed);
        }
    }

    /**
     * Returns the listeners.
     *
     * @return the listeners
     */
    @SuppressWarnings("unchecked")
    protected BrowserListener<T>[] getBrowserListeners() {
        return listeners.toArray(new BrowserListener[listeners.size()]);
    }

}
