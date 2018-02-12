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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.workspace;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.SplitPane;
import org.apache.commons.lang.ObjectUtils;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserListener;
import org.openvpms.web.component.im.select.IMObjectSelector;
import org.openvpms.web.echo.factory.SplitPaneFactory;

import java.util.List;


/**
 * A CRUD workspace that provides a {@link IMObjectSelector selector} to
 * select the parent object, a {@link Browser} to display related child objects,
 * and a {@link CRUDWindow} to view/edit the child objects.
 * <p>
 * The selector is optional.
 *
 * @author Tim Anderson
 */
public abstract class BrowserCRUDWorkspace<Parent extends IMObject, Child extends IMObject>
        extends AbstractCRUDWorkspace<Parent, Child> {

    /**
     * The browser.
     */
    private Browser<Child> browser;

    /**
     * The workspace.
     */
    private Component workspace;


    /**
     * Constructs a {@link BrowserCRUDWorkspace}, with a selector to select the parent object.
     * <p>
     * The {@link #setArchetypes} and {@link #setChildArchetypes} methods must
     * be invoked to set archetypes that the workspace supports, before
     * performing any operations.
     *
     * @param id      the workspace identifier
     * @param context the context
     */
    public BrowserCRUDWorkspace(String id, Context context) {
        this(id, context, true);
    }

    /**
     * Constructs a {@link BrowserCRUDWorkspace}.
     * <p>
     * The {@link #setArchetypes} and {@link #setChildArchetypes} methods must * be invoked to set archetypes that the
     * workspace supports, before performing any operations.
     *
     * @param id           the workspace identifier
     * @param context      the context
     * @param showSelector if {@code true}, show the selector
     */
    public BrowserCRUDWorkspace(String id, Context context, boolean showSelector) {
        super(id, context, showSelector);
    }

    /**
     * Constructs a {@link BrowserCRUDWorkspace}, with a selector for the parent object.
     * <p>
     * The {@link #setChildArchetypes} method must be invoked to set archetypes
     * that the workspace supports, before performing any operations.
     *
     * @param id         the workspace identifier
     * @param archetypes the archetypes that this operates on. If {@code null}, the {@link #setArchetypes}
     *                   method must be invoked to set a non-null value before performing any operation
     * @param context    the context
     */
    public BrowserCRUDWorkspace(String id, Archetypes<Parent> archetypes, Context context) {
        this(id, archetypes, null, context);
    }

    /**
     * Constructs a {@link BrowserCRUDWorkspace}, with a selector for the parent object.
     *
     * @param id              the workspace identifier
     * @param archetypes      the archetypes that this operates on. If {@code null}, the {@link #setArchetypes}
     *                        method must be invoked to set a non-null value before performing any operation
     * @param childArchetypes the child archetypes that this operates on. If {@code null}, the
     *                        {@link #setChildArchetypes} method must be invoked to set a non-null value before
     *                        performing any operation
     * @param context         the context
     */
    public BrowserCRUDWorkspace(String id, Archetypes<Parent> archetypes, Archetypes<Child> childArchetypes,
                                Context context) {
        this(id, archetypes, childArchetypes, context, true);
    }

    /**
     * Constructs a {@link BrowserCRUDWorkspace}.
     *
     * @param id              the workspace identifier
     * @param archetypes      the archetypes that this operates on. If {@code null}, the {@link #setArchetypes}
     *                        method must be invoked to set a non-null value before performing any operation
     * @param childArchetypes the child archetypes that this operates on. If {@code null}, the
     *                        {@link #setChildArchetypes} method must be invoked to set a non-null value before
     *                        performing any operation
     * @param context         the context
     * @param showSelector    if {@code true}, show a selector to select the
     */
    public BrowserCRUDWorkspace(String id, Archetypes<Parent> archetypes, Archetypes<Child> childArchetypes,
                                Context context, boolean showSelector) {
        super(id, archetypes, childArchetypes, context, showSelector);
    }

    /**
     * Sets the current object.
     *
     * @param object the object. May be {@code null}
     */
    @Override
    public void setObject(Parent object) {
        super.setObject(object);
        layoutWorkspace(false);
    }

    /**
     * Creates a new browser.
     *
     * @return the browser
     */
    protected abstract Browser<Child> createBrowser();

    /**
     * Returns the browser.
     *
     * @return the browser, or {@code null} if none has been registered
     */
    protected Browser<Child> getBrowser() {
        return browser;
    }

    /**
     * Registers a browser.
     *
     * @param browser the browser. If {@code null}, deregisters any existing
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
     * Invoked when a browser object is selected.
     * <p>
     * This implementation sets the object in the CRUD window.
     *
     * @param object the selected object
     */
    protected void onBrowserSelected(Child object) {
        getCRUDWindow().setObject(object);
    }

    /**
     * Invoked when a browser object is viewed (aka 'browsed').
     * <p>
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
            window.setObject(browser.getSelected());
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
     * @param refresh if {@code true} and the workspace exists, refresh the workspace, otherwise recreate it
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
                recreateWorkspace();
            }
        } else {
            setBrowser(null);
            setCRUDWindow(null);
            if (workspace != null) {
                getRootComponent().remove(workspace);
                workspace = null;
            }
        }
    }

    /**
     * Recreates the workspace.
     */
    protected void recreateWorkspace() {
        setBrowser(createBrowser());
        setCRUDWindow(createCRUDWindow());
        setWorkspace(createWorkspace());
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
     * @return the workspace. May be {@code null}
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
        return SplitPaneFactory.create(SplitPane.ORIENTATION_VERTICAL, "BrowserCRUDWorkspace.Layout",
                                       getBrowser().getComponent(), getCRUDWindow().getComponent());
    }

    /**
     * Determines if the a property change notification containing
     * {@link #SUMMARY_PROPERTY} should be made when a child updates.
     * <p>
     * This implementation always returns {@code true}.
     *
     * @return {@code true} if a notification should be made, otherwise
     * {@code false}
     */
    protected boolean updateSummaryOnChildUpdate() {
        return true;
    }

    /**
     * Determines if the parent object is optional (i.e may be {@code null},
     * when laying out the workspace.
     * <p>
     * If the parent object is optional, the browser and CRUD window will be
     * displayed if there is no parent object. If it is mandatory, the
     * browser and CRUD window will only be displayed if it is present.
     * <p>
     * This implementation always returns {@code false}.
     *
     * @return {@code true} if the parent object is optional, otherwise
     * {@code false}
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
