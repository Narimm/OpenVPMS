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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.workspace;

import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.WindowPaneEvent;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserDialog;
import org.openvpms.web.component.im.query.BrowserFactory;
import org.openvpms.web.component.im.query.BrowserStates;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.QueryFactory;
import org.openvpms.web.component.im.select.RepeatSelector;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.event.WindowPaneListener;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.factory.SplitPaneFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;


/**
 * Workspace that provides an optional selector to select the object for viewing.
 *
 * @author Tim Anderson
 */
public abstract class AbstractViewWorkspace<T extends IMObject> extends AbstractWorkspace<T> {

    /**
     * The archetypes that this may process.
     */
    private Archetypes<T> archetypes;

    /**
     * The selector.
     */
    private RepeatSelector<T> selector;

    /**
     * The root component.
     */
    private SplitPane root;


    /**
     * Constructs an {@code AbstractViewWorkspace}.
     * <p/>
     * The {@link #setArchetypes} method must be invoked to set archetypes that the workspace supports, before
     * performing any operations.
     *
     * @param workspacesId the workspace group localisation identifier
     * @param workspaceId the workspace localisation identifier
     * @param context     the context
     */
    public AbstractViewWorkspace(String workspacesId, String workspaceId, Context context) {
        this(workspacesId, workspaceId, null, context);
    }

    /**
     * Constructs a new {@code AbstractViewWorkspace}.
     * <p/>
     * If no archetypes are supplied, the {@link #setArchetypes} method must
     * before performing any operations.
     *
     * @param workspacesId the workspace group localisation identifier
     * @param workspaceId the workspace localisation identifier
     * @param archetypes  the archetype that this operates on. May be {@code null}
     * @param context     the context
     */
    public AbstractViewWorkspace(String workspacesId, String workspaceId, Archetypes<T> archetypes, Context context) {
        this(workspacesId, workspaceId, archetypes, context, true);
    }

    /**
     * Constructs an {@code AbstractViewWorkspace}.
     * <p/>
     * If no archetypes are supplied, the {@link #setArchetypes} method must before performing any operations.
     *
     * @param workspacesId  the workspace group localisation identifier
     * @param workspaceId  the workspace localisation identifier
     * @param archetypes   the archetype that this operates on. May be {@code null}
     * @param context      the context
     * @param showSelector if {@code true}, show the selector
     */
    public AbstractViewWorkspace(String workspacesId, String workspaceId, Archetypes<T> archetypes,
                                 Context context, boolean showSelector) {
        super(workspacesId, workspaceId, context);
        this.archetypes = archetypes;
        if (showSelector) {
            selector = createSelector();
        }
    }

    /**
     * Sets the current object, updating the selector if present.
     *
     * @param object the object. May be {@code null}
     */
    public void setObject(T object) {
        super.setObject(object);
        if (selector != null) {
            selector.setObject(object);
            updateSelector();
        }
    }

    /**
     * Determines if the workspace can be updated with instances of the specified archetype.
     *
     * @param shortName the archetype's short name
     * @return {@code true} if {@code shortName} is one of those in {@link #getArchetypes()}
     */
    public boolean canUpdate(String shortName) {
        return archetypes.contains(shortName);
    }

    /**
     * Invoked when the workspace is displayed.
     */
    @Override
    public void show() {
        super.show();
        updateSelector();
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
     * <em>&lt;workspacesId&gt;.&lt;workspaceId&gt;.type</em>
     *
     * @param type       the type that the short names represent
     * @param shortNames the archetype short names
     */
    protected void setArchetypes(Class<T> type, String... shortNames) {
        String key = getWorkspacesId() + "." + getWorkspaceId() + ".type";
        setArchetypes(Archetypes.create(shortNames, type, Messages.get(key)));
    }

    /**
     * Returns the archetype this operates on.
     *
     * @return the archetypes, or {@code null} if none has been set
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
            Row wrapper = RowFactory.create("AbstractViewWorkspace.Selector", select);
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
     * Invoked when the 'select' button is pressed. This pops up an {@link Browser} to select an object.
     */
    protected void onSelect() {
        HelpContext select = getHelpContext().subtopic("select");
        Browser<T> browser = createSelectBrowser(select);
        onSelect(browser, select);
    }

    /**
     * Invoked when the 'select again' button is pressed. This pops up an {@link Browser} to select an object.
     */
    protected void onSelectAgain() {
        HelpContext select = getHelpContext().subtopic("select");
        Browser<T> browser = createSelectBrowser(select);
        BrowserStates states = BrowserStates.getInstance();
        states.setBrowserState(browser);
        onSelect(browser, select);
    }

    /**
     * Invoked when the selection browser is closed.
     *
     * @param dialog the browser dialog
     */
    protected void onSelectClosed(BrowserDialog<T> dialog) {
        T object = dialog.getSelected();
        if (object != null) {
            onSelected(object);
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
     * Creates a new dialog to select an object.
     *
     * @param browser the browser
     * @param help    the help context
     * @return a new dialog
     */
    protected BrowserDialog<T> createBrowserDialog(Browser<T> browser, HelpContext help) {
        String title = Messages.get("imobject.select.title", getArchetypes().getDisplayName());
        return new BrowserDialog<T>(title, browser, help);
    }

    /**
     * Creates a new browser to select an object.
     *
     * @param help the help context
     * @return a new browser
     * @throws ArchetypeQueryException if the short names don't match any archetypes
     */
    protected Browser<T> createSelectBrowser(HelpContext help) {
        return BrowserFactory.create(createSelectQuery(), new DefaultLayoutContext(getContext(), help));
    }

    /**
     * Creates a new query to select an object.
     *
     * @return a new query
     * @throws ArchetypeQueryException if the short names don't match any archetypes
     */
    protected Query<T> createSelectQuery() {
        return QueryFactory.create(getArchetypes().getShortNames(), getContext(), getType());
    }

    /**
     * Creates a new selector that delegates to {@link #onSelect} and {@link #onSelectAgain()} depending on which
     * button is pressed.
     *
     * @return a new selector
     */
    private RepeatSelector<T> createSelector() {
        RepeatSelector<T> selector = new RepeatSelector<T>();
        selector.getSelect().addActionListener(new ActionListener() {
            public void onAction(ActionEvent actionEvent) {
                onSelect();
            }
        });
        selector.getSelectAgain().addActionListener(new ActionListener() {
            public void onAction(ActionEvent event) {
                onSelectAgain();
            }
        });
        return selector;
    }

    /**
     * Updates the selector.
     */
    private void updateSelector() {
        if (selector != null && !selector.isShowSelectAgain()) {
            BrowserStates states = BrowserStates.getInstance();
            if (states.exists(getArchetypes().getType(), getArchetypes().getShortNames())) {
                selector.setShowSelectAgain(true);
            }
        }
    }

    /**
     * Creates a dialog to display the browser.
     *
     * @param browser the browser
     * @param help    the help context
     */
    private void onSelect(Browser<T> browser, HelpContext help) {
        final BrowserDialog<T> popup = createBrowserDialog(browser, help);

        popup.addWindowPaneListener(new WindowPaneListener() {
            public void onClose(WindowPaneEvent event) {
                onSelectClosed(popup);
            }
        });

        popup.show();
    }

}
