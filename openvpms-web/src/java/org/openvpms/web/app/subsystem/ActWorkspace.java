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

package org.openvpms.web.app.subsystem;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.SplitPane;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.im.query.ActQuery;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.IMObjectTableBrowserFactory;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.QueryBrowserListener;
import org.openvpms.web.component.im.table.IMObjectTableModel;
import org.openvpms.web.component.im.table.act.ActAmountTableModel;
import org.openvpms.web.component.subsystem.AbstractViewWorkspace;
import org.openvpms.web.component.util.GroupBoxFactory;
import org.openvpms.web.component.util.SplitPaneFactory;

import java.util.List;


/**
 * Act workspace.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class ActWorkspace<T extends IMObject, A extends Act>
        extends AbstractViewWorkspace<T> {

    /**
     * The workspace.
     */
    private Component workspace;

    /**
     * The query.
     */
    private ActQuery<A> query;

    /**
     * The act browser.
     */
    private Browser<A> browser;

    /**
     * The CRUD window.
     */
    private CRUDWindow<A> window;


    /**
     * Constructs a new <tt>ActWorkspace</tt>.
     *
     * @param subsystemId the subsystem localisation identifier
     * @param workspaceId the workspace localisation identfifier
     * @param shortNames  the archetype short names that this operates on
     */
    public ActWorkspace(String subsystemId, String workspaceId,
                        ShortNames shortNames) {
        super(subsystemId, workspaceId, shortNames);
    }

    /**
     * Invoked when the object has been saved.
     *
     * @param object the object
     * @param isNew  determines if the object is a new instance
     */
    protected void onSaved(A object, boolean isNew) {
        browser.query();
        browser.setSelected(object);
        firePropertyChange(SUMMARY_PROPERTY, null, null);
    }

    /**
     * Invoked when the object has been deleted.
     *
     * @param object the object
     */
    protected void onDeleted(A object) {
        browser.query();
        firePropertyChange(SUMMARY_PROPERTY, null, null);
    }

    /**
     * Invoked when the object needs to be refreshed.
     *
     * @param object the object
     */
    protected void onRefresh(A object) {
        browser.query();
        browser.setSelected(object);
        firePropertyChange(SUMMARY_PROPERTY, null, null);
    }

    /**
     * Invoked when an act is selected.
     *
     * @param act the act,. May be <tt>null</tt>
     */
    protected void actSelected(A act) {
        window.setObject(act);
    }

    /**
     * Lays out the workspace.
     *
     * @param party the party. May be <code>null</code>
     */
    protected void layoutWorkspace(Party party) {
        if (party != null) {
            setQuery(createQuery(party));
            setBrowser(createBrowser(query));
            setCRUDWindow(createCRUDWindow());
            setWorkspace(createWorkspace());
        } else {
            query = null;
            browser = null;
            window = null;
            if (workspace != null) {
                getRootComponent().remove(workspace);
                workspace = null;
            }
        }
    }

    /**
     * Returns a component representing the acts.
     * This implementation returns the acts displayed in a group box.
     *
     * @param acts the act browser
     * @return a component representing the acts
     */
    protected Component getActs(Browser acts) {
        return GroupBoxFactory.create(browser.getComponent());
    }

    /**
     * Creates the workspace component.
     *
     * @return a new workspace
     */
    protected Component createWorkspace() {
        Component acts = getActs(getBrowser());
        return SplitPaneFactory.create(SplitPane.ORIENTATION_VERTICAL,
                                       "ActWorkspace.Layout", acts,
                                       window.getComponent());
    }

    /**
     * Creates a new CRUD window for viewing and editing acts.
     *
     * @return a new CRUD window
     */
    protected abstract CRUDWindow<A> createCRUDWindow();

    /**
     * Creates a new query.
     *
     * @param party the party to query acts for
     * @return a new query
     */
    protected abstract ActQuery<A> createQuery(Party party);

    /**
     * Creates a new browser to query and display acts.
     *
     * @param query the query
     * @return a new browser
     */
    protected Browser<A> createBrowser(ActQuery<A> query) {
        return IMObjectTableBrowserFactory.create(query, null,
                                                  createTableModel());
    }

    /**
     * Creates a new table model to display acts.
     *
     * @return a new table model.
     */
    protected IMObjectTableModel<A> createTableModel() {
        return new ActAmountTableModel<A>();
    }

    /**
     * Registers a new query.
     *
     * @param query the new query
     */
    protected void setQuery(ActQuery<A> query) {
        this.query = query;
    }

    /**
     * Returns the query.
     *
     * @return the query. May be <code>null</code>
     */
    protected Query<A> getQuery() {
        return query;
    }

    /**
     * Registers a new browser.
     *
     * @param browser the new browser
     */
    protected void setBrowser(Browser<A> browser) {
        this.browser = browser;
        this.browser.addQueryListener(new QueryBrowserListener<A>() {
            public void query() {
                onQuery();
            }

            public void selected(A object) {
                actSelected(object);
            }
        });
    }

    /**
     * Returns the browser.
     *
     * @return the browser. May be <code>null</code>
     */
    protected Browser<A> getBrowser() {
        return browser;
    }

    /**
     * Registers a new workspace.
     *
     * @param workspace the workspace
     */
    protected void setWorkspace(Component workspace) {
        SplitPane root = getRootComponent();
        if (this.workspace != null) {
            root.remove(this.workspace);
        }
        this.workspace = workspace;
        root.add(this.workspace);
    }

    /**
     * Registers a new CRUD window.
     *
     * @param window the window
     */
    protected void setCRUDWindow(CRUDWindow<A> window) {
        this.window = window;
        this.window.setListener(new CRUDWindowListener<A>() {
            public void saved(A object, boolean isNew) {
                onSaved(object, isNew);
            }

            public void deleted(A object) {
                onDeleted(object);
            }

            public void refresh(A object) {
                onRefresh(object);
            }
        });
    }

    /**
     * Returns the CRUD window.
     *
     * @return the CRUD window. May be <code>null</code>
     */
    protected CRUDWindow<A> getCRUDWindow() {
        return window;
    }

    /**
     * Perform an initial query, selecting the first available act.
     *
     * @param party the party
     */
    protected void initQuery(Party party) {
        if (query != null) {
            query.setEntity(party);
            browser.query();
            onQuery();
        }
    }

    /**
     * Returns the workspace.
     *
     * @return the workspace. May be <code>null</code>
     */
    protected Component getWorkspace() {
        return workspace;
    }

    /**
     * Invoked when acts are queried. Selects the first available act, if any.
     */
    protected void onQuery() {
        if (query != null) {
            selectFirst();
        }
    }

    /**
     * Selects the first available act, if any.
     */
    private void selectFirst() {
        List<A> objects = browser.getObjects();
        if (!objects.isEmpty()) {
            A current = objects.get(0);
            browser.setSelected(current);
            window.setObject(current);
        } else {
            window.setObject(null);
        }
    }
}
