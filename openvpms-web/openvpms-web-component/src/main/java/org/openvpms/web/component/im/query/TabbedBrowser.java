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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.component.im.query;

import echopointng.TabbedPane;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.TabPaneModel;
import org.openvpms.web.component.util.TabbedPaneFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Browser that contains other browsers, rendered in a tab pane.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class TabbedBrowser<T> implements Browser<T> {

    /**
     * The browsers.
     */
    private List<Browser<T>> browsers = new ArrayList<Browser<T>>();

    /**
     * The container.
     */
    private Column container;

    /**
     * The tab pane model.
     */
    private TabPaneModel model;

    /**
     * The tabbed pane.
     */
    private TabbedPane tab;

    /**
     * The set of registered listeners.
     */
    private List<BrowserListener<T>> listeners = new ArrayList<BrowserListener<T>>();

    /**
     * The event listener.
     */
    private TabbedBrowserListener listener;

    /**
     * The selected tab.
     */
    private int selected = -1;

    /**
     * The focus group.
     */
    private FocusGroup focusGroup = new FocusGroup(getClass().getName());


    /**
     * Constructs a <tt>TabbedBrowser</tt>.
     */
    public TabbedBrowser() {
        container = ColumnFactory.create("InsetY");
        model = new TabPaneModel(container);
    }

    /**
     * Adds a browser.
     *
     * @param displayName the display name
     * @param browser     the browser to add
     * @return the browser tab position
     */
    public int addBrowser(String displayName, Browser<T> browser) {
        browsers.add(browser);
        for (BrowserListener<T> listener : listeners) {
            browser.addBrowserListener(listener);
        }
        return addTab(displayName, browser);
    }

    /**
     * Returns the query component.
     *
     * @return the query component
     */
    public Component getComponent() {
        if (tab == null) {
            tab = TabbedPaneFactory.create(model);
            if (model.size() > 0) {
                selected = 0;
            }
            tab.setSelectedIndex(selected);

            tab.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    int index = tab.getSelectedIndex();
                    if (index != selected) {
                        selected = index;
                        if (listener != null) {
                            listener.onBrowserChanged();
                        }
                    }
                }
            });
            container.add(tab);
            focusGroup.add(tab);
        }
        return container;
    }

    /**
     * Returns the selected object.
     *
     * @return the selected object, or <tt>null</tt> if none has been selected.
     */
    public T getSelected() {
        Browser<T> browser = getSelectedBrowser();
        return (browser != null) ? browser.getSelected() : null;
    }

    /**
     * Select an object.
     *
     * @param object the object to select
     */
    public void setSelected(T object) {
        Browser<T> browser = getSelectedBrowser();
        if (browser != null) {
            browser.setSelected(object);
        }
    }

    /**
     * Returns the objects matching the query.
     *
     * @return the objects matcing the query.
     */
    public List<T> getObjects() {
        Browser<T> browser = getSelectedBrowser();
        return (browser != null) ? browser.getObjects() : Collections.<T>emptyList();
    }

    /**
     * Adds a listener to receive notification of selection and query actions.
     *
     * @param listener the listener to add
     */
    public void addBrowserListener(BrowserListener<T> listener) {
        listeners.add(listener);
        for (Browser<T> browser : browsers) {
            browser.addBrowserListener(listener);
        }
    }

    /**
     * Removes a listener to stop receive notification of selection and query actions.
     *
     * @param listener the listener to remove
     */
    public void removeBrowserListener(BrowserListener<T> listener) {
        listeners.remove(listener);
        for (Browser<T> browser : browsers) {
            browser.removeBrowserListener(listener);
        }
    }

    /**
     * Query using the specified criteria, and populate the table with matches.
     */
    public void query() {
        // TODO - should query lazily
        for (Browser<T> browser : browsers) {
            query(browser);
        }
    }

    /**
     * Returns the browsers.
     *
     * @return the browsers
     */
    public List<Browser<T>> getBrowsers() {
        return browsers;
    }

    /**
     * Returns the selected browser.
     *
     * @return the selected browser, or <tt>null</tt> if no browser is selected
     */
    public Browser<T> getSelectedBrowser() {
        return (selected != -1) ? browsers.get(selected) : null;
    }

    /**
     * Returns the selected browser index.
     *
     * @return the selected browser index, or <tt>-1</tt> if no browser is selected
     */
    public int getSelectedBrowserIndex() {
        return selected;
    }

    /**
     * Sets the browser listener.
     *
     * @param listener the listener. May be <code>null</code>
     */
    public void setListener(TabbedBrowserListener listener) {
        this.listener = listener;
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
     * Sets focus on the results.
     */
    public void setFocusOnResults() {
        getSelectedBrowser().setFocusOnResults();
    }

    /**
     * Queries a browser, preserving the selected object if possible.
     * <p/>
     * Note that this supresses events for all but the current browser, to avoid events from one browser triggering
     * behaviour in another.
     * <p/>
     * TODO - ideally each tab would be treated independently, and refreshed when displayed.
     *
     * @param browser the browser
     */
    private void query(Browser<T> browser) {
        boolean supressEvents = getSelectedBrowser() != browser;
        if (supressEvents) {
            for (BrowserListener<T> l : listeners) {
                browser.removeBrowserListener(l);
            }
        }
        try {
            T selected = browser.getSelected();
            browser.query();
            browser.setSelected(selected);
        } finally {
            if (supressEvents) {
                for (BrowserListener<T> l : listeners) {
                    browser.addBrowserListener(l);
                }
            }
        }
    }

    /**
     * Adds a browser tab.
     *
     * @param displayName the tab name
     * @param browser     the browser
     * @return the tab index
     */
    protected int addTab(String displayName, Browser<T> browser) {
        int result = model.size();
        int shortcut = result + 1;
        String text = "&" + shortcut + " " + displayName;
        Component component = browser.getComponent();
        component = ColumnFactory.create("Inset", component);
        model.addTab(text, component);

        // select the first available act, if any
        if (browser.getSelected() == null) {
            List<T> objects = browser.getObjects();
            if (!objects.isEmpty()) {
                T current = objects.get(0);
                browser.setSelected(current);
            }
        }
        return result;
    }
}
