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

package org.openvpms.web.component.subsystem;

import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.component.event.WindowPaneListener;
import nextapp.echo2.app.event.WindowPaneEvent;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserDialog;
import org.openvpms.web.component.im.query.BrowserFactory;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.QueryFactory;
import org.openvpms.web.component.im.select.BasicSelector;
import org.openvpms.web.component.im.select.Selector;
import org.openvpms.web.component.im.util.Archetypes;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.component.util.SplitPaneFactory;
import org.openvpms.web.resource.util.Messages;


/**
 * Workspace that provides an optional selector to select the object for
 * viewing.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class AbstractViewWorkspace<T extends IMObject>
        extends AbstractWorkspace<T> {

    /**
     * The archetypes that this may process.
     */
    private Archetypes<T> archetypes;

    /**
     * The selector.
     */
    private Selector<T> selector;

    /**
     * The root component.
     */
    private SplitPane root;


    /**
     * Constructs a new <tt>AbstractViewWorkspace</tt>.
     * <p/>
     * The {@link #setArchetypes} method must be invoked to set archetypes
     * that the workspace supports, before performing any operations.
     *
     * @param subsystemId the subsystem localisation identifier
     * @param workspaceId the workspace localisation identfifier
     */
    public AbstractViewWorkspace(String subsystemId, String workspaceId) {
        this(subsystemId, workspaceId, null);
    }

    /**
     * Constructs a new <tt>AbstractViewWorkspace</tt>.
     * <p/>
     * If no archetypes are supplied, the {@link #setArchetypes} method must
     * before performing any operations.
     *
     * @param subsystemId the subsystem localisation identifier
     * @param workspaceId the workspace localisation identfifier
     * @param archetypes  the archetype that this operates on.
     *                    May be <tt>null</tt>
     */
    public AbstractViewWorkspace(String subsystemId, String workspaceId,
                                 Archetypes<T> archetypes) {
        this(subsystemId, workspaceId, archetypes, true);
    }

    /**
     * Constructs a new <tt>AbstractViewWorkspace</tt>.
     * <p/>
     * If no archetypes are supplied, the {@link #setArchetypes} method must
     * before performing any operations.
     *
     * @param subsystemId  the subsystem localisation identifier
     * @param workspaceId  the workspace localisation identfifier
     * @param archetypes   the archetype that this operates on.
     *                     May be <tt>null</tt>
     * @param showSelector if <tt>true</tt>, show the selector
     */
    public AbstractViewWorkspace(String subsystemId, String workspaceId,
                                 Archetypes<T> archetypes,
                                 boolean showSelector) {
        super(subsystemId, workspaceId);
        this.archetypes = archetypes;
        if (showSelector) {
            selector = new BasicSelector<T>();
            selector.getSelect().addActionListener(new ActionListener() {
                public void onAction(ActionEvent actionEvent) {
                    onSelect();
                }
            });
        }
    }

    /**
     * Sets the current object, updating the selector if present.
     *
     * @param object the object. May be <tt>null</tt>
     */
    public void setObject(T object) {
        super.setObject(object);
        if (selector != null) {
            selector.setObject(object);
        }
    }

    /**
     * Determines if the workspace supports an archetype.
     *
     * @param shortName the archetype's short name
     * @return <tt>true</tt> if the workspace can handle the archetype;
     *         otherwise <tt>false</tt>
     */
    public boolean canHandle(String shortName) {
        return archetypes.contains(shortName);
    }

    /**
     * Returns the class type that this operates on.
     *
     * @return the class type that this operates on
     */
    protected Class<T> getType() {
        return archetypes.getType();
    }

    /**
     * Sets the archetypes that this operates on.
     *
     * @param archetypes the archetypes
     */
    protected void setArchetypes(Archetypes<T> archetypes) {
        this.archetypes = archetypes;
    }

    /**
     * Sets the archetypes that this operates on.
     * <p/>
     * The archetypes are assigned a localised display name using the
     * resource bundle key:
     * <em>&lt;subsystemId&gt;.&lt;workspaceId&gt;.type</em>
     *
     * @param type       the type that the short names represent
     * @param shortNames the archetype short names
     */
    protected void setArchetypes(Class<T> type, String ... shortNames) {
        String key = getSubsystemId() + "." + getWorkspaceId() + ".type";
        setArchetypes(Archetypes.create(shortNames, type, Messages.get(key)));
    }

    /**
     * Returns the archetype this operates on.
     *
     * @return the archetypes, or <tt>null</tt> if none has been set
     */
    protected Archetypes<T> getArchetypes() {
        return archetypes;
    }

    /**
     * Lays out the component.
     *
     * @return the component
     */
    @Override
    protected Component doLayout() {
        root = createRootComponent();
        Component heading = super.doLayout();
        Column top = ColumnFactory.create(heading);
        if (selector != null) {
            Component select = selector.getComponent();
            Row wrapper = RowFactory.create("AbstractViewWorkspace.Selector",
                                            select);
            top.add(wrapper);
        }

        root.add(top);
        doLayout(root);
        return root;
    }

    /**
     * Returns the root split pane.
     *
     * @return the root split pane
     */
    protected SplitPane getRootComponent() {
        if (root == null) {
            root = createRootComponent();
        }
        return root;
    }

    /**
     * Creates a root split pane.
     *
     * @return a root split pane
     */
    protected SplitPane createRootComponent() {
        int orientation = SplitPane.ORIENTATION_VERTICAL;
        String style = (selector != null)
                ? "AbstractViewWorkspace.Layout"
                : "AbstractViewWorkspace.LayoutNoSelector";
        return SplitPaneFactory.create(orientation, style);
    }

    /**
     * Lays out the component.
     *
     * @param container the container
     */
    protected abstract void doLayout(Component container);

    /**
     * Invoked when the 'select' button is pressed. This pops up an {@link
     * Browser} to select an object.
     */
    protected void onSelect() {
        try {
            final Browser<T> browser = createSelectBrowser();

            String title = Messages.get("imobject.select.title",
                                        getArchetypes().getDisplayName());
            final BrowserDialog<T> popup = new BrowserDialog<T>(
                    title, browser);

            popup.addWindowPaneListener(new WindowPaneListener() {
                public void onClose(WindowPaneEvent event) {
                    T object = popup.getSelected();
                    if (object != null) {
                        onSelected(object);
                    }
                }
            });

            popup.show();
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Invoked when an object is selected.
     *
     * @param object the selected object
     */
    protected void onSelected(T object) {
        setObject(object);
    }

    /**
     * Creates a new browser to select an object.
     *
     * @return a new browser
     * @throws ArchetypeQueryException if the short names don't match any
     *                                 archetypes
     */
    protected Browser<T> createSelectBrowser() {
        return BrowserFactory.create(createSelectQuery());
    }

    /**
     * Creates a new query to select an object.
     *
     * @return a new query
     * @throws ArchetypeQueryException if the short names don't match any
     *                                 archetypes
     */
    protected Query<T> createSelectQuery() {
        return QueryFactory.create(getArchetypes().getShortNames(),
                                   GlobalContext.getInstance(), getType());
    }

}
