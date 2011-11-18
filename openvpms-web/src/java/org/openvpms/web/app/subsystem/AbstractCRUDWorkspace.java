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
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.select.IMObjectSelector;
import org.openvpms.web.component.im.util.Archetypes;
import org.openvpms.web.component.mail.MailContext;
import org.openvpms.web.component.subsystem.AbstractViewWorkspace;
import org.openvpms.web.resource.util.Messages;


/**
 * Abstract implementation of a CRUD workspace.
 * <p/>
 * Provides a {@link IMObjectSelector selector} and
 * {@link CRUDWindow CRUD window}. The selector is optional.
 * <p/>
 * The workspace has two parameters, <em>Parent</em> and <em>Child</em>.
 * The range of archetypes supported by each are specified via {@link #setArchetypes}
 * and {@link #setChildArchetypes} respectively.
 * <p/>
 * This is to support workspaces where a parent object is used to select
 * child objects for editing. For workspaces where there is no child,
 * specify the same values for each.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class AbstractCRUDWorkspace<Parent extends IMObject,
        Child extends IMObject> extends AbstractViewWorkspace<Parent> {

    /**
     * The child archetypes.
     */
    private Archetypes<Child> childArchetypes;

    /**
     * The CRUD window.
     */
    private CRUDWindow<Child> window;

    /**
     * The email context.
     */
    private MailContext context;


    /**
     * Constructs an <tt>AbstractCRUDWorkspace</tt>.
     * <p/>
     * The {@link #setArchetypes} and {@link #setChildArchetypes} methods must
     * be invoked to set archetypes that the workspace supports, before
     * performing any operations.
     *
     * @param subsystemId  the subsystem localisation identifier
     * @param workspaceId  the workspace localisation identfifier
     * @param showSelector if <tt>true</tt>, show the selector
     */
    public AbstractCRUDWorkspace(String subsystemId, String workspaceId,
                                 boolean showSelector) {
        this(subsystemId, workspaceId, null, null, showSelector);
    }

    /**
     * Constructs an <tt>AbstractCRUDWorkspace</tt>, with a selector for the parent object.
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
    public AbstractCRUDWorkspace(String subsystemId, String workspaceId,
                                 Archetypes<Parent> archetypes,
                                 Archetypes<Child> childArchetypes) {
        this(subsystemId, workspaceId, archetypes, childArchetypes, true);
    }

    /**
     * Constructs an <tt>AbstractCRUDWorkspace</tt>.
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
    public AbstractCRUDWorkspace(String subsystemId, String workspaceId,
                                 Archetypes<Parent> archetypes,
                                 Archetypes<Child> childArchetypes,
                                 boolean showSelector) {
        super(subsystemId, workspaceId, archetypes, showSelector);
        this.childArchetypes = childArchetypes;
    }

    /**
     * Sets the mail context.
     *
     * @param context the mail context. May be <tt>null</tt>
     */
    public void setMailContext(MailContext context) {
        this.context = context;
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
     * @param newWindow the new window. If <tt>null<tt>, deregisters any
     *                  existing window
     */
    protected void setCRUDWindow(CRUDWindow<Child> newWindow) {
        if (newWindow != null) {
            newWindow.setMailContext(context);
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
        return new DefaultCRUDWindow<Child>(getChildArchetypes());
    }

    /**
     * Returns the child archetypes that this operates on.
     *
     * @return the child archetypes, or <tt>null</tt> if none are set
     */
    protected Archetypes<Child> getChildArchetypes() {
        return childArchetypes;
    }

    /**
     * Sets the child archetypes that this operates on.
     *
     * @param archetypes the child archetypes. May be <tt>null</tt>
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
