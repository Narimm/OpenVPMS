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

package org.openvpms.web.app.subsystem;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.SplitPane;
import org.apache.commons.lang.ObjectUtils;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserFactory;
import org.openvpms.web.component.im.query.BrowserListener;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.QueryFactory;
import org.openvpms.web.component.im.select.IMObjectSelector;
import org.openvpms.web.component.im.util.Archetypes;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.SplitPaneFactory;
import org.openvpms.web.component.subsystem.AbstractCRUDWorkspace;
import org.openvpms.web.component.subsystem.CRUDWindow;

import java.util.List;


/**
 * A CRUD workspace that provides a {@link IMObjectSelector selector} to
 * select the parent object, a {@link Browser} to display related child objects,
 * and a {@link CRUDWindow} to view/edit the child objects.
 * <p/>
 * The selector is optional.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class BrowserCRUDWorkspace<Parent extends IMObject,
        Child extends IMObject>
        extends AbstractCRUDWorkspace<Parent, Child> {

    /**
     * The query.
     */
    private Query<Child> query;

    /**
     * The browser.
     */
    private Browser<Child> browser;

    /**
     * The workspace.
     */
    private Component workspace;


    /**
     * Constructs a new <tt>BrowserCRUDWorkspace</tt>, with a selector to
     * select the parent object.
     * <p/>
     * The {@link #setArchetypes} and {@link #setChildArchetypes} methods must
     * be invoked to set archetypes that the workspace supports, before
     * performing any operations.
     *
     * @param subsystemId the subsystem localisation identifier
     * @param workspaceId the workspace localisation identfifier
     */
    public BrowserCRUDWorkspace(String subsystemId, String workspaceId) {
        this(subsystemId, workspaceId, true);
    }

    /**
     * Constructs a <tt>BrowserCRUDWorkspace</tt>.
     * <p/>
     * The {@link #setArchetypes} and {@link #setChildArchetypes} methods must
     * be invoked to set archetypes that the workspace supports, before
     * performing any operations.
     *
     * @param subsystemId  the subsystem localisation identifier
     * @param workspaceId  the workspace localisation identfifier
     * @param showSelector if <tt>true</tt>, show the selector
     */
    public BrowserCRUDWorkspace(String subsystemId, String workspaceId,
                                boolean showSelector) {
        super(subsystemId, workspaceId, showSelector);
    }

    /**
     * Constructs a new <tt>BrowserCRUDWorkspace</tt>, with a selector for
     * the parent object.
     * <p/>
     * The {@link #setChildArchetypes} method must be invoked to set archetypes
     * that the workspace supports, before performing any operations.
     *
     * @param subsystemId the subsystem localisation identifier
     * @param workspaceId the workspace localisation identfifier
     * @param archetypes  the archetypes that this operates on.
     *                    If <tt>null</tt>, the {@link #setArchetypes}
     *                    method must be invoked to set a non-null value
     *                    before performing any operation
     */
    public BrowserCRUDWorkspace(String subsystemId, String workspaceId,
                                Archetypes<Parent> archetypes) {
        this(subsystemId, workspaceId, archetypes, null);
    }

    /**
     * Constructs a new <tt>BrowserCRUDWorkspace</tt>, with a selector for
     * the parent object.
     *
     * @param subsystemId     the subsystem localisation identifier
     * @param workspaceId     the workspace localisation identfifier
     * @param archetypes      the archetypes that this operates on.
     *                        If <tt>null</tt>, the {@link #setArchetypes}
     *                        method must be invoked to set a non-null value
     *                        before performing any operation
     * @param childArchetypes the child archetypes that this operates on.
     *                        If <tt>null</tt>, the {@link #setChildArchetypes}
     *                        method must be invoked to set a non-null value
     *                        before performing any operation
     */
    public BrowserCRUDWorkspace(String subsystemId, String workspaceId,
                                Archetypes<Parent> archetypes,
                                Archetypes<Child> childArchetypes) {
        this(subsystemId, workspaceId, archetypes, childArchetypes, true);
    }

    /**
     * Constructs a new <tt>BrowserCRUDWorkspace</tt>.
     *
     * @param subsystemId     the subsystem localisation identifier
     * @param workspaceId     the workspace localisation identfifier
     * @param archetypes      the archetypes that this operates on.
     *                        If <tt>null</tt>, the {@link #setArchetypes}
     *                        method must be invoked to set a non-null value
     *                        before performing any operation
     * @param childArchetypes the child archetypes that this operates on.
     *                        If <tt>null</tt>, the {@link #setChildArchetypes}
     *                        method must be invoked to set a non-null value
     *                        before performing any operation
     * @param showSelector    if <tt>true</tt>, show a selector to select the
     *                        parent object
     */
    public BrowserCRUDWorkspace(String subsystemId, String workspaceId,
                                Archetypes<Parent> archetypes,
                                Archetypes<Child> childArchetypes,
                                boolean showSelector) {
        super(subsystemId, workspaceId, archetypes, childArchetypes,
              showSelector);
    }

    /**
     * Sets the current object.
     *
     * @param object the object. May be <tt>null</tt>
     */
    @Override
    public void setObject(Parent object) {
        super.setObject(object);
        layoutWorkspace(false);
    }

    /**
     * Returns the browser.
     *
     * @return the browser, or <tt>null</tt> if none has been registered
     */
    protected Browser<Child> getBrowser() {
        return browser;
    }

    /**
     * Registers a browser.
     *
     * @param browser the browser. If <tt>null</tt>, deregisters any existing
     *                browser
     */
    protected void setBrowser(Browser<Child> browser) {
        if (browser != null) {
            browser.addBrowserListener(new BrowserListener<Child>() {
                public void query() {
                    onBrowserQuery();
                }

                public void selected(Child object) {
                    onBrowserSelected(object);
                }

                public void browsed(Child object) {
                    onBrowserViewed(object);
                }
            });
        }
        this.browser = browser;
    }

    /**
     * Creates a new browser.
     *
     * @param query the query
     * @return a new browser
     */
    protected Browser<Child> createBrowser(Query<Child> query) {
        return BrowserFactory.create(query);
    }

    /**
     * Returns the query used to populate the browser.
     *
     * @return the query, or <tt>null</tt> if none is registered
     */
    protected Query<Child> getQuery() {
        return query;
    }

    /**
     * Registers a browser query.
     *
     * @param query the browser query. May be <tt>null</tt>
     */
    protected void setQuery(Query<Child> query) {
        this.query = query;
    }

    /**
     * Creates a new query to populate the browser.
     *
     * @return a new query
     */
    protected Query<Child> createQuery() {
        Archetypes shortNames = getChildArchetypes();
        return QueryFactory.create(shortNames.getShortNames(),
                                   GlobalContext.getInstance(),
                                   shortNames.getType());
    }

    /**
     * Invoked when a browser object is selected.
     * <p/>
     * This implementation sets the object in the CRUD window.
     *
     * @param object the selected object
     */
    protected void onBrowserSelected(Child object) {
        getCRUDWindow().setObject(object);
    }

    /**
     * Invoked when a browser object is viewed (aka 'browsed').
     * <p/>
     * This implementation sets the object in the CRUD window.
     *
     * @param object the selected object
     */
    protected void onBrowserViewed(Child object) {
        getCRUDWindow().setObject(object);
    }

    /**
     * Invoked when the object has been saved.
     *
     * @param object the object
     * @param isNew  determines if the object is a new instance
     */
    @Override
    protected void onSaved(Child object, boolean isNew) {
        browser.query();
        CRUDWindow<Child> window = getCRUDWindow();
        if (!browser.getObjects().isEmpty()) {
            // there are objects to display. Not necessarily that just saved,
            // but attempt to select it anyway.
            browser.setSelected(object);
            window.setObject(object);
        } else {
            // the query doesn't select the saved object
            window.setObject(null);
        }
        if (updateSummaryOnChildUpdate()) {
            firePropertyChange(SUMMARY_PROPERTY, null, null);
        }
        browser.setFocusOnResults();
    }

    /**
     * Invoked when the object has been deleted.
     *
     * @param object the object
     */
    @Override
    protected void onDeleted(Child object) {
        browser.query();
        if (updateSummaryOnChildUpdate()) {
            firePropertyChange(SUMMARY_PROPERTY, null, null);
        }
        browser.setFocusOnResults();
    }

    /**
     * Invoked when the object needs to be refreshed.
     *
     * @param object the object
     */
    @Override
    protected void onRefresh(Child object) {
        browser.query();
        browser.setSelected(object);
        if (updateSummaryOnChildUpdate()) {
            firePropertyChange(SUMMARY_PROPERTY, null, null);
        }

        // if the browser selects the object (i.e is visible), set the object back in the CRUD window,
        // otherwise clear the CRUD window
        CRUDWindow<Child> window = getCRUDWindow();
        if (ObjectUtils.equals(browser.getSelected(), object)) {
            window.setObject(object);
        } else {
            window.setObject(null);
        }

        browser.setFocusOnResults();
    }

    /**
     * Lays out the component.
     *
     * @param container the container
     */
    protected void doLayout(Component container) {
        Parent latest = getLatest();
        Parent object = getObject();
        if (latest != object) {
            setObject(latest);
        } else {
            layoutWorkspace(true);
        }
    }

    /**
     * Lays out the workspace.
     *
     * @param refresh if <tt>true</tt> and the workspace exists, refresh the workspace, otherwise recreate it
     */
    protected void layoutWorkspace(boolean refresh) {
        Parent parent = getObject();
        if (parent != null || isParentOptional()) {
            Browser<Child> browser = getBrowser();
            if (refresh && browser != null) {
                // need to reregister as doLayout() has recreated the root component
                setCRUDWindow(getCRUDWindow());
                setWorkspace(getWorkspace());
                browser.query();
            } else {
                Query<Child> query = createQuery();
                setQuery(query);
                setBrowser(createBrowser(query));
                setCRUDWindow(createCRUDWindow());
                setWorkspace(createWorkspace());
                if (query.isAuto()) {
                    onBrowserQuery();
                }
            }
        } else {
            setQuery(null);
            setBrowser(null);
            setCRUDWindow(null);
            if (workspace != null) {
                getRootComponent().remove(workspace);
                workspace = null;
            }
        }
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
     * Returns the workspace.
     *
     * @return the workspace. May be <tt>null</tt>
     */
    protected Component getWorkspace() {
        return workspace;
    }

    /**
     * Creates the workspace component.
     *
     * @return a new workspace
     */
    protected Component createWorkspace() {
        Component browser = ColumnFactory.create("Inset", getBrowser().getComponent());
        return SplitPaneFactory.create(SplitPane.ORIENTATION_VERTICAL,
                                       "BrowserCRUDWorkspace.Layout", browser,
                                       getCRUDWindow().getComponent());
    }

    /**
     * Determines if the a property change notification containing
     * {@link #SUMMARY_PROPERTY} should be made when a child updates.
     * <p/>
     * This implementation always returns <tt>true</tt>.
     *
     * @return <tt>true</tt> if a notification should be made, otherwise
     *         <tt>false</tt>
     */
    protected boolean updateSummaryOnChildUpdate() {
        return true;
    }

    /**
     * Determines if the parent object is optional (i.e may be <tt>null</tt>,
     * when laying out the workspace.
     * <p/>
     * If the parent object is optional, the browser and CRUD window will be
     * displayed if there is no parent object. If it is mandatory, the
     * browser and CRUD window will only be displayed if it is present.
     * <p/>
     * This implementation always returns <tt>false</tt>.
     *
     * @return <tt>true</tt> if the parent object is optional, otherwise
     *         <tt>false</tt>
     */
    protected boolean isParentOptional() {
        return false;
    }

    /**
     * Invoked when the browser is queried.
     * This implementation selects the first available object.
     */
    protected void onBrowserQuery() {
        List<Child> objects = browser.getObjects();
        CRUDWindow<Child> window = getCRUDWindow();
        if (!objects.isEmpty()) {
            Child current = objects.get(0);
            browser.setSelected(current);
            window.setObject(current);
        } else {
            window.setObject(null);
        }
    }

}
