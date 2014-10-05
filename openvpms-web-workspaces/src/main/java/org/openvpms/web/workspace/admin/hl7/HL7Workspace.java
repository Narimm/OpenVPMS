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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.admin.hl7;

import echopointng.TabbedPane;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.event.ChangeEvent;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.hl7.util.HL7Archetypes;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserFactory;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.QueryFactory;
import org.openvpms.web.component.workspace.AbstractWorkspace;
import org.openvpms.web.component.workspace.BrowserCRUDWindow;
import org.openvpms.web.component.workspace.DefaultCRUDWindow;
import org.openvpms.web.echo.event.ChangeListener;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.SplitPaneFactory;
import org.openvpms.web.echo.factory.TabbedPaneFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.echo.tabpane.ObjectTabPaneModel;
import org.openvpms.web.resource.i18n.Messages;


/**
 * HL7 workspace.
 *
 * @author Tim Anderson
 */
public class HL7Workspace extends AbstractWorkspace<IMObject> {

    /**
     * Manages services and connections tabs.
     */
    private TabbedPane pane;

    /**
     * The container for the tabbed pane and the buttons.
     */
    private SplitPane container;

    /**
     * The split pane style.
     */
    private static final String STYLE = "SplitPaneWithButtonRow";

    /**
     * Constructs an {@code UserWorkspace}.
     */
    public HL7Workspace(Context context) {
        super("admin", "hl7", context);
    }

    /**
     * Returns the class type that this operates on.
     *
     * @return the class type that this operates on
     */
    @Override
    protected Class<IMObject> getType() {
        return IMObject.class;
    }

    /**
     * Determines if the workspace can be updated with instances of the specified archetype.
     *
     * @param shortName the archetype's short name
     * @return {@code false}
     */
    @Override
    public boolean canUpdate(String shortName) {
        return super.canUpdate(shortName);
    }

    /**
     * Lays out the component.
     * <p/>
     * This renders a heading using {@link #createHeading}.
     *
     * @return the component
     */
    @Override
    protected Component doLayout() {
        SplitPane root = SplitPaneFactory.create(SplitPane.ORIENTATION_VERTICAL, STYLE);
        container = SplitPaneFactory.create(SplitPane.ORIENTATION_VERTICAL_BOTTOM_TOP, STYLE);
        Component heading = super.doLayout();
        Column container = ColumnFactory.create(Styles.INSET_Y);
        final ObjectTabPaneModel<BrowserCRUDWindow<IMObject>> model
                = new ObjectTabPaneModel<BrowserCRUDWindow<IMObject>>(container);
        addTabs(model);
        pane = TabbedPaneFactory.create(model);
        pane.getSelectionModel().addChangeListener(new ChangeListener() {
            @Override
            public void onChange(ChangeEvent event) {
                onTabSelected(model.getObject(pane.getSelectedIndex()));
            }
        });
        onTabSelected(model.getObject(0));

        root.add(heading);
        root.add(this.container);
        return root;
    }

    /**
     * Invoked when a tab is selected.
     *
     * @param tab the tab
     */
    private void onTabSelected(BrowserCRUDWindow<IMObject> tab) {
        if (tab != null) {
            container.removeAll();
            container.add(tab.getWindow().getComponent());
            container.add(pane);
        }
    }

    private void addTabs(ObjectTabPaneModel<BrowserCRUDWindow<IMObject>> model) {
        addServiceBrowser(model);
        addConnectionBrowser(model);
    }

    private void addServiceBrowser(ObjectTabPaneModel<BrowserCRUDWindow<IMObject>> model) {
        Context context = getContext();
        HelpContext help = getHelpContext();
        Query<IMObject> query = QueryFactory.create(HL7Archetypes.SERVICES, context);
        Browser<IMObject> browser = BrowserFactory.create(query, new DefaultLayoutContext(context, help));
        Archetypes<IMObject> archetypes = Archetypes.create(HL7Archetypes.SERVICES, IMObject.class,
                                                            Messages.get("admin.hl7.service.type"));
        DefaultCRUDWindow<IMObject> window = new DefaultCRUDWindow<IMObject>(archetypes, context, help);
        BrowserCRUDWindow<IMObject> tab = new BrowserCRUDWindow<IMObject>(browser, window);
        addTab("admin.hl7.services", model, tab);
    }

    private void addConnectionBrowser(ObjectTabPaneModel<BrowserCRUDWindow<IMObject>> model) {
        Context context = getContext();
        HelpContext help = getHelpContext();
        Query<IMObject> query = QueryFactory.create(HL7Archetypes.CONNECTIONS, context);
        Browser<IMObject> browser = BrowserFactory.create(query, new DefaultLayoutContext(context, help));
        Archetypes<IMObject> archetypes = Archetypes.create(HL7Archetypes.CONNECTIONS, IMObject.class,
                                                            Messages.get("admin.hl7.connection.type"));
        DefaultCRUDWindow<IMObject> window = new DefaultCRUDWindow<IMObject>(archetypes, context, help);
        addTab("admin.hl7.connections", model, new BrowserCRUDWindow<IMObject>(browser, window));
    }

    /**
     * Helper to add a tab to the tab pane.
     *
     * @param name  the tab name
     * @param model the tab model
     * @param tab   the component
     */
    protected void addTab(String name, ObjectTabPaneModel<BrowserCRUDWindow<IMObject>> model,
                          BrowserCRUDWindow<IMObject> tab) {
        int index = model.size();
        int shortcut = index + 1;
        String text = "&" + shortcut + " " + Messages.get(name);
        model.addTab(tab, text, tab.getBrowser().getComponent());
    }
}
