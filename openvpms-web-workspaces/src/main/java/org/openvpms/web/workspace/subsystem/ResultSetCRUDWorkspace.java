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
 */
package org.openvpms.web.workspace.subsystem;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.SplitPane;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.query.QueryBrowser;
import org.openvpms.web.component.workspace.CRUDWindow;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.SplitPaneFactory;
import org.openvpms.web.echo.util.DoubleClickMonitor;


/**
 * A CRUD workspace that provides a {@link QueryBrowser} to display objects, and a {@link ResultSetCRUDWindow}
 * to view/edit the objects.
 *
 * @author Tim Anderson
 */
public abstract class ResultSetCRUDWorkspace<T extends IMObject> extends BrowserCRUDWorkspace<T, T> {

    /**
     * The double click monitor.
     */
    private DoubleClickMonitor click = new DoubleClickMonitor();


    /**
     * Constructs a {@code ResultSetCRUDWorkspace}.
     * <p/>
     * The {@link #setArchetypes} method must be invoked to set archetypes that the workspace supports, before
     * performing any operations.
     *
     * @param workspacesId the workspaces localisation identifier
     * @param workspaceId  the workspace localisation identifier
     * @param context      the context
     */
    public ResultSetCRUDWorkspace(String workspacesId, String workspaceId, Context context) {
        super(workspacesId, workspaceId, context, false);
    }

    /**
     * Sets the current object.
     * <p/>
     * This is analagous to {@link #setObject} but performs a safe cast to the required type.
     * <p/>
     * If the current object is the same instance as that supplied, no changes will be made.
     *
     * @param object the current object. May be {@code null}
     */
    @Override
    public void setIMObject(IMObject object) {
        boolean select = object != null && object == getObject();
        super.setIMObject(object);
        if (select) {
            // object is already in the workspace, so setObject() not invoked. Select it instead.
            select(getType().cast(object));
        }
    }

    /**
     * Sets the current object.
     *
     * @param object the object. May be {@code null}
     */
    @Override
    public void setObject(T object) {
        super.setObject(object);
        select(object);
    }

    /**
     * Selects an object.
     *
     * @param object the object to select. May be {@code null}
     */
    protected void select(T object) {
        ResultSetCRUDWindow<T> window = getCRUDWindow();
        window.setObject(object);
        if (object != null) {
            QueryBrowser<T> browser = getBrowser();
            browser.getQuery().setValue(object.getName());
            browser.query();
            if (!browser.getObjects().isEmpty()) {
                // there are objects to display. Not necessarily that just set, but attempt to select it anyway.
                browser.setSelected(object);
                window.view();
            }
        }
    }

    /**
     * Creates the workspace component.
     *
     * @return a new workspace
     */
    @Override
    protected Component createWorkspace() {
        Component window = getCRUDWindow().getComponent();
        Component browser = ColumnFactory.create("Inset", getBrowser().getComponent());
        return SplitPaneFactory.create(SplitPane.ORIENTATION_VERTICAL_BOTTOM_TOP, "SplitPaneWithButtonRow",
                                       window, browser);
    }

    /**
     * Creates a new CRUD window.
     *
     * @return a new CRUD window
     */
    @Override
    protected CRUDWindow<T> createCRUDWindow() {
        QueryBrowser<T> browser = getBrowser();
        return new ResultSetCRUDWindow<T>(getArchetypes(), browser.getQuery(), browser.getResultSet(),
                                          getContext(), getHelpContext());
    }

    /**
     * Returns the CRUD window, creating it if it doesn't exist.
     *
     * @return the CRUD window
     */
    @Override
    protected ResultSetCRUDWindow<T> getCRUDWindow() {
        return (ResultSetCRUDWindow<T>) super.getCRUDWindow();
    }

    /**
     * Returns the browser.
     *
     * @return the browser, or {@code null} if none has been registered
     */
    @Override
    protected QueryBrowser<T> getBrowser() {
        return (QueryBrowser<T>) super.getBrowser();
    }

    /**
     * Invoked when a browser object is selected.
     * <p/>
     * This implementation sets the object in the CRUD window and if it has been double clicked:
     * <ul>
     * <li>pops up an editor, if editing is supported; otherwise
     * <li>pops up a viewer
     * </li>
     *
     * @param object the selected object
     */
    @Override
    protected void onBrowserSelected(T object) {
        updateResultSet();
        super.onBrowserSelected(object);
        ResultSetCRUDWindow<T> window = getCRUDWindow();
        if (click.isDoubleClick(object.getId())) {
            if (window.canEdit()) {
                window.edit();
            } else {
                window.view();
            }
        }
    }

    /**
     * Invoked when a browser object is viewed (aka 'browsed').
     * <p/>
     * This implementation sets the object in the CRUD window.
     *
     * @param object the selected object
     */
    @Override
    protected void onBrowserViewed(T object) {
        updateResultSet();
        super.onBrowserViewed(object);
    }

    /**
     * Invoked when the object has been saved.
     *
     * @param object the object
     * @param isNew  determines if the object is a new instance
     */
    @Override
    protected void onSaved(T object, boolean isNew) {
        super.onSaved(object, isNew);
        updateResultSet();
    }

    /**
     * Invoked when the object has been deleted.
     *
     * @param object the object
     */
    @Override
    protected void onDeleted(T object) {
        super.onDeleted(object);
        updateResultSet();
    }

    /**
     * Invoked when the object needs to be refreshed.
     *
     * @param object the object
     */
    @Override
    protected void onRefresh(T object) {
        super.onRefresh(object);
        updateResultSet();
    }

    /**
     * Invoked when the browser is queried.
     */
    @Override
    protected void onBrowserQuery() {
        super.onBrowserQuery();
        updateResultSet();
    }

    /**
     * Determines if the parent object is optional (i.e may be {@code null},
     * when laying out the workspace.
     * <p/>
     * This implementation always returns {@code true}.
     *
     * @return {@code true}
     */
    @Override
    protected boolean isParentOptional() {
        return true;
    }

    /**
     * Sets the archetypes that this operates on.
     *
     * @param archetypes the archetypes
     */
    @Override
    protected void setArchetypes(Archetypes<T> archetypes) {
        super.setArchetypes(archetypes);
        setChildArchetypes(archetypes);
    }

    /**
     * Updates the CRUD window with the current result set.
     */
    private void updateResultSet() {
        QueryBrowser<T> browser = getBrowser();
        ResultSetCRUDWindow<T> window = getCRUDWindow();
        window.setQuery(browser.getQuery());
        window.setResultSet(browser.getResultSet());
    }

}
