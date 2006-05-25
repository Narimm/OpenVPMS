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

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
package org.openvpms.web.app.patient;

import nextapp.echo2.app.Component;
import nextapp.echo2.extras.app.TabPane;
import nextapp.echo2.extras.app.layout.TabPaneLayoutData;
import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.DefaultTreeBrowser;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.QueryBrowserListener;
import org.openvpms.web.component.im.query.TreeBrowser;
import org.openvpms.web.component.im.tree.ActTreeBuilder;
import org.openvpms.web.component.im.tree.BottomUpActTreeBuilder;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.resource.util.Messages;

import java.util.List;


/**
 * Patient record browser.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class RecordBrowser implements Browser<Act> {

    /**
     * The tabbed pane.
     */
    private TabPane _tab;

    /**
     * The visits browser.
     */
    private TreeBrowser<Act> _visits;

    /**
     * The problems browser.
     */
    private TreeBrowser<Act> _problems;


    /**
     * Construct a new <code>RecordBrowser</code> that queries IMObjects using
     * the specified query.
     *
     * @param visits   query for visists
     * @param problems query for problems
     * @param sort     the sort criteria. May be <code>null</code>
     */
    public RecordBrowser(Query<Act> visits, Query<Act> problems,
                         SortConstraint[] sort) {
        _visits = new DefaultTreeBrowser<Act>(visits, sort,
                new BottomUpActTreeBuilder());
        _problems = new DefaultTreeBrowser<Act>(problems, sort,
                new ActTreeBuilder());
    }

    /**
     * Returns the query component.
     *
     * @return the query component
     */
    public Component getComponent() {
        if (_tab == null) {
            _tab = new TabPane();
            addTab("button.visit", _visits);
            addTab("button.problem", _problems);
        }
        return _tab;
    }

    /**
     * Returns the selected object.
     *
     * @return the selected object, or <code>null</code> if none has been
     *         selected.
     */
    public Act getSelected() {
        return getCurrent().getSelected();
    }

    /**
     * Select an object.
     *
     * @param object the object to select
     */
    public void setSelected(Act object) {
        getCurrent().setSelected(object);
    }

    /**
     * Returns the objects matching the query.
     *
     * @return the objects matcing the query.
     */
    public List<Act> getObjects() {
        return getCurrent().getObjects();
    }

    /**
     * Adds a listener to receive notification of selection and query actions.
     *
     * @param listener the listener to add
     */
    public void addQueryListener(QueryBrowserListener listener) {
        _visits.addQueryListener(listener);
        _problems.addQueryListener(listener);
    }

    /**
     * Query using the specified criteria, and populate the table with matches.
     */
    public void query() {
        _visits.query();
        _problems.query();
    }

    /**
     * Returns the parent of an object.
     *
     * @param object the object
     * @return the parent of object, or <code>null</code> if the object has
     *         no parent
     */
    public Act getParent(Act object) {
        return getCurrent().getParent(object);
    }

    /**
     * Returns the selected browser.
     *
     * @return the selected browser
     */
    private TreeBrowser<Act> getCurrent() {
        int index = _tab.getActiveTabIndex();  // default == -1
        return (index <= 0) ? _visits : _problems;
    }

    /**
     * Helper to add a browser to the tab pane.
     *
     * @param button  the button key
     * @param browser the browser to add
     */
    private void addTab(String button, Browser browser) {
        Component component = browser.getComponent();
        component = ColumnFactory.create("Inset", component);
        TabPaneLayoutData layout = new TabPaneLayoutData();
        layout.setTitle(Messages.get(button));
        component.setLayoutData(layout);
        _tab.add(component);
    }
}
