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

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserFactory;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.QueryFactory;
import org.openvpms.web.component.im.select.IMObjectSelector;

/**
 * A CRUD workspace that provides a {@link IMObjectSelector selector} to
 * select the parent object, a {@link Query} and {@link Browser} to display related child objects,
 * and a {@link CRUDWindow} to view/edit the child objects.
 * <p>
 * The selector is optional.
 *
 * @author Tim Anderson
 */
public abstract class QueryBrowserCRUDWorkspace<Parent extends IMObject, Child extends IMObject>
        extends BrowserCRUDWorkspace<Parent, Child> {

    /**
     * The query.
     */
    private Query<Child> query;


    /**
     * Constructs a {@link QueryBrowserCRUDWorkspace}, with a selector to select the parent object.
     * <p>
     * The {@link #setArchetypes} and {@link #setChildArchetypes} methods must
     * be invoked to set archetypes that the workspace supports, before
     * performing any operations.
     *
     * @param id      the workspace identifier
     * @param context the context
     */
    public QueryBrowserCRUDWorkspace(String id, Context context) {
        this(id, context, true);
    }

    /**
     * Constructs a {@link QueryBrowserCRUDWorkspace}.
     * <p>
     * The {@link #setArchetypes} and {@link #setChildArchetypes} methods must * be invoked to set archetypes that the
     * workspace supports, before performing any operations.
     *
     * @param id           the workspace identifier
     * @param context      the context
     * @param showSelector if {@code true}, show the selector
     */
    public QueryBrowserCRUDWorkspace(String id, Context context, boolean showSelector) {
        super(id, context, showSelector);
    }

    /**
     * Constructs a {@link QueryBrowserCRUDWorkspace}, with a selector for the parent object.
     * <p>
     * The {@link #setChildArchetypes} method must be invoked to set archetypes
     * that the workspace supports, before performing any operations.
     *
     * @param id         the workspace identifier
     * @param archetypes the archetypes that this operates on. If {@code null}, the {@link #setArchetypes}
     *                   method must be invoked to set a non-null value before performing any operation
     * @param context    the context
     */
    public QueryBrowserCRUDWorkspace(String id, Archetypes<Parent> archetypes, Context context) {
        this(id, archetypes, null, context);
    }

    /**
     * Constructs a {@link QueryBrowserCRUDWorkspace}, with a selector for the parent object.
     *
     * @param id              the workspace identifier
     * @param archetypes      the archetypes that this operates on. If {@code null}, the {@link #setArchetypes}
     *                        method must be invoked to set a non-null value before performing any operation
     * @param childArchetypes the child archetypes that this operates on. If {@code null}, the
     *                        {@link #setChildArchetypes} method must be invoked to set a non-null value before
     *                        performing any operation
     * @param context         the context
     */
    public QueryBrowserCRUDWorkspace(String id, Archetypes<Parent> archetypes, Archetypes<Child> childArchetypes,
                                     Context context) {
        this(id, archetypes, childArchetypes, context, true);
    }

    /**
     * Constructs a {@link QueryBrowserCRUDWorkspace}.
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
    public QueryBrowserCRUDWorkspace(String id, Archetypes<Parent> archetypes, Archetypes<Child> childArchetypes,
                                     Context context, boolean showSelector) {
        super(id, archetypes, childArchetypes, context, showSelector);
    }

    /**
     * Creates a new browser.
     * <p/>
     * This delegates to {@link #createBrowser(Query)}.
     *
     * @return the browser
     */
    @Override
    protected Browser<Child> createBrowser() {
        query = createQuery();
        return createBrowser(query);
    }

    /**
     * /**
     * Creates a new browser.
     *
     * @param query the query
     * @return a new browser
     */
    protected Browser<Child> createBrowser(Query<Child> query) {
        return BrowserFactory.create(query, new DefaultLayoutContext(getContext(), getHelpContext()));
    }

    /**
     * Creates a new query to populate the browser.
     *
     * @return a new query
     */
    protected Query<Child> createQuery() {
        Archetypes shortNames = getChildArchetypes();
        return QueryFactory.create(shortNames.getShortNames(), getContext(), shortNames.getType());
    }

    /**
     * Recreates the workspace.
     */
    @Override
    protected void recreateWorkspace() {
        super.recreateWorkspace();
        if (query.isAuto()) {
            onBrowserQuery();
        }
    }
}
