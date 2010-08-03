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
import org.openvpms.web.component.focus.FocusGroup;

import java.util.ArrayList;
import java.util.List;


/**
 * Adapts the results of one browser to another.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class BrowserAdapter<A, T> implements Browser<T> {

    /**
     * The browser to adapt from.
     */
    private Browser<A> browser;


    /**
     * Creates a new <tt>BrowserAdapter</tt>.
     * <p/>
     * The browser to adapt from must be set using {@link #setBrowser}.
     */
    public BrowserAdapter() {
    }

    /**
     * Creates a new <tt>BrowserAdapter</tt>.
     *
     * @param browser the browser to adapt from
     */
    public BrowserAdapter(Browser<A> browser) {
        this.browser = browser;
    }

    /**
     * Returns the browser component.
     *
     * @return the browser component
     */
    public Component getComponent() {
        return browser.getComponent();
    }

    /**
     * Returns the selected object.
     *
     * @return the selected object, or <code>null</code> if none has been
     *         selected.
     */
    public T getSelected() {
        A selected = browser.getSelected();
        return (selected != null) ? convert(selected) : null;
    }

    /**
     * Select an object.
     *
     * @param object the object to select
     */
    public void setSelected(T object) {
        if (object == null) {
            browser.setSelected(null);
        } else {
            for (A target : browser.getObjects()) {
                T converted = convert(target);
                if (converted != null && converted.equals(object)) {
                    browser.setSelected(target);
                    break;
                }
            }
        }
    }

    /**
     * Returns the objects matching the query.
     *
     * @return the objects matcing the query.
     */
    public List<T> getObjects() {
        List<T> result = new ArrayList<T>();
        for (A object : browser.getObjects()) {
            result.add(convert(object));
        }
        return result;
    }

    /**
     * Adds a listener to receive notification of selection and query actions.
     *
     * @param listener the listener to add
     */
    public void addQueryListener(final QueryBrowserListener<T> listener) {
        browser.addQueryListener(new QueryBrowserListener<A>() {
            public void query() {
                listener.query();
            }

            public void selected(A object) {
                listener.selected(convert(object));
            }

            public void browsed(A object) {
                listener.browsed(convert(object));
            }
        });
    }

    /**
     * Query using the specified criteria, and populate the table with matches.
     */
    public void query() {
        browser.query();
    }

    /**
     * Returns the focus group.
     *
     * @return the focus group
     */
    public FocusGroup getFocusGroup() {
        return browser.getFocusGroup();
    }

    /**
     * Sets focus on the results.
     */
    public void setFocusOnResults() {
        getBrowser().setFocusOnResults();
    }

    /**
     * Returns the underlying browser.
     *
     * @return the underlying browser
     */
    public Browser<A> getBrowser() {
        return browser;
    }

    /**
     * Sets the underlying browser.
     *
     * @param browser the browser
     */
    protected void setBrowser(Browser<A> browser) {
        this.browser = browser;
    }

    /**
     * Converts an object.
     *
     * @param object the object to convert
     * @return the converted object
     */
    protected abstract T convert(A object);
}
