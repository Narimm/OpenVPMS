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
 */

package org.openvpms.web.component.subsystem;

import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.select.IMObjectSelector;
import org.openvpms.web.component.im.util.Archetypes;
import org.openvpms.web.resource.util.Messages;


/**
 * Abstract implementation of a CRUD workspace.
 * <p/>
 * Provides a {@link IMObjectSelector selector} and {@link CRUDWindow CRUD window}. The selector is optional.
 * <p/>
 * The workspace has two parameters, <em>Parent</em> and <em>Child</em>.
 * The range of archetypes supported by each are specified via {@link #setArchetypes}
 * and {@link #setChildArchetypes} respectively.
 * <p/>
 * This is to support workspaces where a parent object is used to select
 * child objects for editing. For workspaces where there is no child,
 * specify the same values for each.
 *
 * @author Tim Anderson
 */
public abstract class AbstractCRUDWorkspace<Parent extends IMObject, Child extends IMObject>
    extends AbstractViewWorkspace<Parent> {

    /**
     * The child archetypes.
     */
    private Archetypes<Child> childArchetypes;

    /**
     * The CRUD window.
     */
    private CRUDWindow<Child> window;


    /**
     * Constructs an {@code AbstractCRUDWorkspace}.
     * <p/>
     * The {@link #setArchetypes} and {@link #setChildArchetypes} methods must
     * be invoked to set archetypes that the workspace supports, before
     * performing any operations.
     *
     * @param subsystemId  the subsystem localisation identifier
     * @param workspaceId  the workspace localisation identifier
     * @param context      the context
     * @param showSelector if {@code true}, show the selector
     */
    public AbstractCRUDWorkspace(String subsystemId, String workspaceId, Context context, boolean showSelector) {
        this(subsystemId, workspaceId, null, null, context, showSelector);
    }

    /**
     * Constructs an {@code AbstractCRUDWorkspace}, with a selector for the parent object.
     *
     * @param subsystemId     the subsystem localisation identifier
     * @param workspaceId     the workspace localisation identifier
     * @param archetypes      the archetypes that this operates on. If {@code null}, the {@link #setArchetypes}
     *                        method must be invoked to set a non-null value before performing any operation
     * @param childArchetypes the child archetypes that this operates on. If {@code null}, the
     *                        {@link #setChildArchetypes} method must be invoked to set a non-null value before
     *                        performing any operation
     * @param context         the context
     */
    public AbstractCRUDWorkspace(String subsystemId, String workspaceId, Archetypes<Parent> archetypes,
                                 Archetypes<Child> childArchetypes, Context context) {
        this(subsystemId, workspaceId, archetypes, childArchetypes, context, true);
    }

    /**
     * Constructs an {@code AbstractCRUDWorkspace}.
     *
     * @param subsystemId     the subsystem localisation identifier
     * @param workspaceId     the workspace localisation identifier
     * @param archetypes      the archetypes that this operates on. If {@code null}, the {@link #setArchetypes} method
     *                        must be invoked to set a non-null value before performing any operation
     * @param childArchetypes the child archetypes that this operates on. If {@code null}, the
     *                        {@link #setChildArchetypes} method must be invoked to set a non-null value before
     *                        performing any operation
     * @param context         the context
     * @param showSelector    if {@code true}, show a selector to select the parent object
     */
    public AbstractCRUDWorkspace(String subsystemId, String workspaceId, Archetypes<Parent> archetypes,
                                 Archetypes<Child> childArchetypes, Context context, boolean showSelector) {
        super(subsystemId, workspaceId, archetypes, context, showSelector);
        this.childArchetypes = childArchetypes;
    }

    /**
     * Returns the CRUD window, creating it if it doesn't exist.
     *
     * @return the CRUD window
     */
    protected CRUDWindow<Child> getCRUDWindow() {
        if (window == null) {
            CRUDWindow<Child> window = createCRUDWindow();
            setCRUDWindow(window);
        }
        return window;
    }

    /**
     * Registers a new CRUD window.
     *
     * @param newWindow the new window. If {@code null}, deregisters any existing window
     */
    protected void setCRUDWindow(CRUDWindow<Child> newWindow) {
        if (newWindow != null) {
            newWindow.setMailContext(getMailContext());
        }
        if (window != null) {
            Component current = window.getComponent();
            Component parent = current.getParent();
            if (parent != null) {
                int index = parent.indexOf(current);
                if (index != -1) {
                    parent.remove(index);
                    if (newWindow != null) {
                        parent.add(newWindow.getComponent(), index);
                    }
                }
            }
        }
        window = newWindow;
        if (window != null) {
            window.setListener(new CRUDWindowListener<Child>() {
                public void saved(Child object, boolean isNew) {
                    onSaved(object, isNew);
                }

                public void deleted(Child object) {
                    onDeleted(object);
                }

                public void refresh(Child object) {
                    onRefresh(object);
                }
            });
        }
    }

    /**
     * Creates a new CRUD window.
     *
     * @return a new CRUD window
     */
    protected CRUDWindow<Child> createCRUDWindow() {
        return new DefaultCRUDWindow<Child>(getChildArchetypes(), getContext(), getHelpContext());
    }

    /**
     * Returns the child archetypes that this operates on.
     *
     * @return the child archetypes, or {@code null} if none are set
     */
    protected Archetypes<Child> getChildArchetypes() {
        return childArchetypes;
    }

    /**
     * Sets the child archetypes that this operates on.
     *
     * @param archetypes the child archetypes. May be {@code null}
     */
    protected void setChildArchetypes(Archetypes<Child> archetypes) {
        childArchetypes = archetypes;
    }

    /**
     * Sets the archetypes that this operates on.
     * <p/>
     * The archetypes are assigned a localised display name using the
     * resource bundle key:
     * <em>&lt;subsystemId&gt;.&lt;workspaceId&gt;.createtype</em>
     *
     * @param type       the type that the short names represent
     * @param shortNames the archetype short names
     */
    protected void setChildArchetypes(Class<Child> type, String... shortNames) {
        String key = getSubsystemId() + "." + getWorkspaceId() + ".createtype";
        Archetypes<Child> archetypes = Archetypes.create(shortNames, type,
                                                         Messages.get(key));
        setChildArchetypes(archetypes);
    }

    /**
     * Lays out the component.
     * <p/>
     * This implementation adds the {@link #getCRUDWindow() CRUD window}
     * to the container.
     *
     * @param container the container
     */
    protected void doLayout(Component container) {
        CRUDWindow<Child> window = getCRUDWindow();
        container.add(window.getComponent());
    }

    /**
     * Invoked when an object has been saved.
     * <p/>
     * This implementation is a no-op.
     *
     * @param object the object
     * @param isNew  determines if the object is a new instance
     */
    protected void onSaved(Child object, boolean isNew) {
    }

    /**
     * Invoked when an object has been deleted.
     * <p/>
     * This implementation is a no-op.
     *
     * @param object the object
     */
    protected void onDeleted(Child object) {
    }

    /**
     * Invoked when an object needs to be refreshed.
     * <p/>
     * This implementation is a no-op.
     *
     * @param object the object
     */
    protected void onRefresh(Child object) {
    }

}
