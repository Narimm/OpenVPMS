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
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.app.subsystem.ShortNames;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserDialog;
import org.openvpms.web.component.im.query.IMObjectTableBrowserFactory;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.QueryFactory;
import org.openvpms.web.component.im.select.BasicSelector;
import org.openvpms.web.component.im.select.Selector;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.component.util.SplitPaneFactory;
import org.openvpms.web.resource.util.Messages;


/**
 * Workspace that provides a selector to select an object for viewing.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class AbstractViewWorkspace<T extends IMObject>
        extends AbstractWorkspace<T> {

    /**
     * The archetype short names that this may process.
     */
    private final ShortNames shortNames;

    /**
     * The current object. May be <code>null</code>.
     */
    private T object;

    /**
     * The selector.
     */
    private Selector<T> selector;

    /**
     * Localised type display name (e.g, Customer, Product).
     */
    private final String type;

    /**
     * The root component.
     */
    private SplitPane root;


    /**
     * Constructs a new <tt>AbstractViewWorkspace</tt>.
     *
     * @param subsystemId the subsystem localisation identifier
     * @param workspaceId the workspace localisation identfifier
     * @param shortNames  the archetype short names that this operates on
     */
    public AbstractViewWorkspace(String subsystemId, String workspaceId,
                                 ShortNames shortNames) {
        super(subsystemId, workspaceId);
        this.shortNames = shortNames;
        selector = new BasicSelector<T>();

        String id = getSubsystemId() + "." + getWorkspaceId();
        type = Messages.get(id + ".type");

        selector.getSelect().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                onSelect();
            }
        });
    }

    /**
     * Determines if the workspace supports an archetype.
     *
     * @param shortName the archetype's short name
     * @return <code>true</code> if the workspace can handle the archetype;
     *         otherwise <code>false</code>
     */
    public boolean canHandle(String shortName) {
        boolean result = false;
        try {
            result = shortNames.contains(shortName);
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
        return result;
    }

    /**
     * Sets the current object.
     *
     * @param object the object. May be <code>null</code>
     */
    public void setObject(T object) {
        this.object = object;
        selector.setObject(object);
    }

    /**
     * Returns the current object.
     *
     * @return the current object. May be <code>null</code>
     */
    public T getObject() {
        return object;
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
        Component select = selector.getComponent();
        Row wrapper = RowFactory.create("AbstractViewWorkspace.Selector",
                                        select);

        Column top = ColumnFactory.create(heading, wrapper);
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
        return SplitPaneFactory.create(SplitPane.ORIENTATION_VERTICAL,
                                       "AbstractViewWorkspace.Layout");
    }

    /**
     * Lays out the component.
     *
     * @param container the container
     */
    protected abstract void doLayout(Component container);

    /**
     * Returns the archetype short names that this operates on.
     *
     * @return the archetype short names
     */
    protected ShortNames getShortNames() {
        return shortNames;
    }

    /**
     * Returns a localised type display name.
     *
     * @return a localised type display name
     */
    protected String getType() {
        return type;
    }

    /**
     * Returns the selector.
     *
     * @return the selector
     */
    protected Selector getSelector() {
        return selector;
    }

    /**
     * Create a new browser.
     *
     * @return a new browser
     * @throws ArchetypeQueryException if the short names don't match any
     *                                 archetypes
     */
    protected Browser<T> createBrowser() {
        Query<T> query = createQuery();
        SortConstraint[] sort = {new NodeSortConstraint("name", true)};
        return IMObjectTableBrowserFactory.create(query, sort);
    }

    /**
     * Create a new query.
     *
     * @return a new query
     * @throws ArchetypeQueryException if the short names don't match any
     *                                 archetypes
     */
    protected Query<T> createQuery() {
        return QueryFactory.create(shortNames.getShortNames(),
                                   GlobalContext.getInstance());
    }

    /**
     * Invoked when the 'select' button is pressed. This pops up an {@link
     * Browser} to select an object.
     */
    protected void onSelect() {
        try {
            final Browser<T> browser = createBrowser();

            String title = Messages.get("imobject.select.title", type);
            final BrowserDialog<T> popup = new BrowserDialog<T>(
                    title, browser);

            popup.addWindowPaneListener(new WindowPaneListener() {
                public void windowPaneClosing(WindowPaneEvent event) {
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

}
