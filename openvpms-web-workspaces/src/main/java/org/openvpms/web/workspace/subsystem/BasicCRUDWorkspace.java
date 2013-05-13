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
 */

package org.openvpms.web.workspace.subsystem;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserDialog;
import org.openvpms.web.component.im.select.IMObjectSelector;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.subsystem.AbstractCRUDWorkspace;
import org.openvpms.web.component.subsystem.CRUDWindow;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;


/**
 * Basic CRUD workspace.
 * <p/>
 * Provides an {@link IMObjectSelector selector} to select objects and
 * {@link CRUDWindow CRUD window}. The selector is optional.
 *
 * @author Tim Anderson
 */
public abstract class BasicCRUDWorkspace<T extends IMObject>
    extends AbstractCRUDWorkspace<T, T> {

    /**
     * Constructs a{@code BasicCRUDWorkspace}, with a selector for  the object.
     * <p/>
     * The {@link #setArchetypes} method must be invoked to set the archetypes
     * that the workspace supports, before performing any operations.
     *
     * @param subsystemId the subsystem localisation identifier
     * @param workspaceId the workspace localisation identifier
     * @param context     the context
     */
    public BasicCRUDWorkspace(String subsystemId, String workspaceId, Context context) {
        this(subsystemId, workspaceId, null, context);
    }

    /**
     * Constructs a new {@code BasicCRUDWorkspace}, with a selector for
     * the object.
     *
     * @param subsystemId the subsystem localisation identifier
     * @param workspaceId the workspace localisation identifier
     * @param archetypes  the archetypes that this operates on. If {@code null}, the {@link #setArchetypes}
     *                    method must be invoked to set a non-null value before performing any operation
     * @param context     the context
     */
    public BasicCRUDWorkspace(String subsystemId, String workspaceId, Archetypes<T> archetypes, Context context) {
        super(subsystemId, workspaceId, archetypes, archetypes, context);
    }

    /**
     * Constructs a {@code BasicCRUDWorkspace}.
     *
     * @param subsystemId  the subsystem localisation identifier
     * @param workspaceId  the workspace localisation identifier
     * @param archetypes   the archetypes that this operates on. If {@code null}, the {@link #setArchetypes}
     *                     method must be invoked to set a non-null value before performing any operation
     * @param context      the context
     * @param showSelector if {@code true}, show a selector to select the object
     */
    public BasicCRUDWorkspace(String subsystemId, String workspaceId, Archetypes<T> archetypes, Context context,
                              boolean showSelector) {
        super(subsystemId, workspaceId, archetypes, archetypes, context, showSelector);
    }

    /**
     * Sets the current object.
     *
     * @param object the object. May be {@code null}
     */
    @Override
    public void setObject(T object) {
        super.setObject(object);
        getCRUDWindow().setObject(object);
    }

    /**
     * Creates a new dialog to select an object.
     * <p/>
     * This implementation adds a 'New' button.
     *
     * @param browser the browser
     * @param help    the help context
     * @return a new dialog
     */
    @Override
    protected BrowserDialog<T> createBrowserDialog(Browser<T> browser, HelpContext help) {
        String title = Messages.get("imobject.select.title", getArchetypes().getDisplayName());
        return new BrowserDialog<T>(title, browser, true, help);
    }

    /**
     * Invoked when the selection browser is closed.
     *
     * @param dialog the browser dialog
     */
    @Override
    protected void onSelectClosed(BrowserDialog<T> dialog) {
        if (dialog.createNew()) {
            getCRUDWindow().create();
        } else {
            T object = dialog.getSelected();
            if (object != null) {
                onSelected(object);
            }
        }
    }

    /**
     * Invoked when the object has been saved.
     *
     * @param object the object
     * @param isNew  determines if the object is a new instance
     */
    protected void onSaved(T object, boolean isNew) {
        setObject(object);
    }

    /**
     * Invoked when the object has been deleted.
     *
     * @param object the object
     */
    protected void onDeleted(T object) {
        setObject(null);
    }

    /**
     * Invoked when the object needs to be refreshed.
     *
     * @param object the object
     */
    protected void onRefresh(T object) {
        object = IMObjectHelper.reload(object);
        setObject(object);
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

}
