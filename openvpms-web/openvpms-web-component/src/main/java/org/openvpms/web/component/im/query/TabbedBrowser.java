/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.query;

import echopointng.TabbedPane;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.SplitPane;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.SplitPaneFactory;
import org.openvpms.web.echo.factory.TabbedPaneFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.echo.tabpane.ObjectTabPaneModel;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Browser that contains other browsers, with each browser selected by a tab.
 * <p/>
 * The tabs and content are rendered in a split pane so that tabs don't scroll if the content is too
 * large to fit on screen.
 *
 * @author Tim Anderson
 */
public class TabbedBrowser<T> implements Browser<T> {

    /**
     * The browsers.
     */
    private List<Browser<T>> browsers = new ArrayList<>();

    /**
     * The tab container.
     */
    private Column tabContainer;

    /**
     * The container.
     */
    private SplitPane container;

    /**
     * The tab pane model.
     */
    private ObjectTabPaneModel<Browser<T>> model;

    /**
     * The tabbed pane.
     */
    private TabbedPane tab;

    /**
     * The set of registered listeners.
     */
    private List<BrowserListener<T>> listeners = new ArrayList<>();

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
     * Constructs a {@code TabbedBrowser}.
     */
    public TabbedBrowser() {
        tabContainer = ColumnFactory.create(Styles.INSET_Y);
        container = SplitPaneFactory.create(SplitPane.ORIENTATION_VERTICAL_TOP_BOTTOM, "TabbedBrowser");
        container.add(tabContainer);
        model = new ObjectTabPaneModel<>(tabContainer);
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
                onBrowserSelected(selected);
            }
            tab.setSelectedIndex(selected);

            tab.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    int index = tab.getSelectedIndex();
                    if (index != selected) {
                        selected = index;
                        onBrowserSelected(selected);
                    }
                }
            });
            tabContainer.add(tab);
            focusGroup.add(tab);
        }
        return container;
    }

    /**
     * Returns the selected object.
     *
     * @return the selected object, or {@code null} if none has been selected.
     */
    public T getSelected() {
        Browser<T> browser = getSelectedBrowser();
        return (browser != null) ? browser.getSelected() : null;
    }

    /**
     * Select an object.
     *
     * @param object the object to select. May be {@code null} to deselect the current selection
     * @return {@code true} if the object was selected, {@code false} if it doesn't exist in the current view
     */
    public boolean setSelected(T object) {
        boolean result = false;
        Browser<T> browser = getSelectedBrowser();
        if (browser != null) {
            result = browser.setSelected(object);
        }
        return result;
    }

    /**
     * Returns the objects matching the query.
     *
     * @return the objects matching the query.
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
     * @return the selected browser, or {@code null} if no browser is selected
     */
    public Browser<T> getSelectedBrowser() {
        return (selected != -1) ? browsers.get(selected) : null;
    }

    /**
     * Selects a browser.
     *
     * @param index the browser index
     */
    public void setSelectedBrowser(int index) {
        selected = index;
        tab.setSelectedIndex(selected);
        onBrowserSelected(selected);
    }

    /**
     * Returns the selected browser index.
     *
     * @return the selected browser index, or {@code -1} if no browser is selected
     */
    public int getSelectedBrowserIndex() {
        return selected;
    }

    /**
     * Sets the browser listener.
     *
     * @param listener the listener. May be {@code null}
     */
    public void setListener(TabbedBrowserListener listener) {
        this.listener = listener;
    }

    /**
     * Returns the browser state.
     * <p/>
     * This implementation always returns {@code null}.
     *
     * @return {@code null}
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
     * Invoked when a browser is selected.
     * <p/>
     * This notifies any registered listener.
     *
     * @param selected the selected index
     */
    protected void onBrowserSelected(@SuppressWarnings("unused") int selected) {
        if (container.getComponentCount() == 2) {
            container.remove(1);
        }
        Browser<T> browser = model.getObject(selected);
        if (browser != null) {
            container.add(browser.getComponent());
        }
        if (listener != null) {
            listener.onBrowserChanged();
        }
        if (browser != null) {
            // select the first available act, if any
            if (browser.getSelected() == null) {
                List<T> objects = browser.getObjects();
                if (!objects.isEmpty()) {
                    T current = objects.get(0);
                    browser.setSelected(current);
                }
            }
        }
    }

    /**
     * Queries a browser, preserving the selected object if possible.
     * <p/>
     * Note that this suppresses events for all but the current browser, to avoid events from one browser triggering
     * behaviour in another.
     * <p/>
     * TODO - ideally each tab would be treated independently, and refreshed when displayed.
     *
     * @param browser the browser
     */
    protected void query(Browser<T> browser) {
        boolean suppressEvents = getSelectedBrowser() != browser;
        if (suppressEvents) {
            for (BrowserListener<T> l : listeners) {
                browser.removeBrowserListener(l);
            }
        }
        try {
            T selected = browser.getSelected();
            browser.query();
            browser.setSelected(selected);
        } finally {
            if (suppressEvents) {
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
        String text;
        int result = model.size();
        int shortcut = result + 1;
        if (shortcut <= 10) {
            if (shortcut == 10) {
                shortcut = 0;
            }
            text = "&" + shortcut + " " + displayName;
        } else {
            text = displayName;
        }
        model.addTab(browser, text, new Label());
        return result;
    }
}
