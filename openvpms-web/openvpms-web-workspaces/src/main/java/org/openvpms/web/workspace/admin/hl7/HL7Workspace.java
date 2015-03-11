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
import org.openvpms.component.business.domain.im.common.Entity;
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
import org.openvpms.web.component.workspace.DefaultCRUDWindow;
import org.openvpms.web.echo.event.ChangeListener;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
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
     * The tab pane model.
     */
    private ObjectTabPaneModel<TabComponent> model;

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
     * Invoked when the workspace is displayed.
     */
    @Override
    public void show() {
        TabComponent tab = model.getObject(pane.getSelectedIndex());
        if (tab != null) {
            tab.show();
        }
    }

    /**
     * Returns the help context.
     *
     * @return the help context
     */
    @Override
    public HelpContext getHelpContext() {
        HelpContext result = null;
        if (model != null && pane != null) {
            TabComponent tab = model.getObject(pane.getSelectedIndex());
            if (tab != null) {
                result = tab.getHelpContext();
            }
        }
        return (result == null) ? super.getHelpContext() : result;
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
        Column wrapper = ColumnFactory.create(Styles.INSET_Y);
        model = new ObjectTabPaneModel<TabComponent>(wrapper);
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
    private void onTabSelected(TabComponent tab) {
        if (tab != null) {
            container.removeAll();
            Component buttons = tab.getButtons();
            if (buttons != null) {
                container.add(buttons);
            } else {
                container.add(LabelFactory.create());
            }
            container.add(pane);
            tab.show();
        }
    }

    private void addTabs(ObjectTabPaneModel<TabComponent> model) {
        addServiceBrowser(model);
        addConnectorBrowser(model);
    }

    private void addServiceBrowser(ObjectTabPaneModel<TabComponent> model) {
        Context context = getContext();
        HelpContext help = subtopic("service");
        Query<IMObject> query = QueryFactory.create(HL7Archetypes.SERVICES, context);
        Browser<IMObject> browser = BrowserFactory.create(query, new DefaultLayoutContext(context, help));
        Archetypes<IMObject> archetypes = Archetypes.create(HL7Archetypes.SERVICES, IMObject.class,
                                                            Messages.get("admin.hl7.service.type"));
        DefaultCRUDWindow<IMObject> window = new DefaultCRUDWindow<IMObject>(archetypes, context, help);
        addTab("admin.hl7.services", model, new HL7BrowserCRUDWindow<IMObject>(browser, window));
    }

    private void addConnectorBrowser(ObjectTabPaneModel<TabComponent> model) {
        Context context = getContext();
        HelpContext help = subtopic("connector");
        Query<Entity> query = QueryFactory.create(HL7Archetypes.CONNECTORS, context);
        Browser<Entity> browser = new HL7ConnectorBrowser(query, new DefaultLayoutContext(context, help));
        Archetypes<Entity> archetypes = Archetypes.create(HL7Archetypes.CONNECTORS, Entity.class,
                                                          Messages.get("admin.hl7.connector.type"));
        HL7ConnectorCRUDWindow window = new HL7ConnectorCRUDWindow(archetypes, getContext(), help);
        addTab("admin.hl7.connectors", model, new HL7BrowserCRUDWindow<Entity>(browser, window));
    }

    /**
     * Creates a help sub-topic.
     *
     * @param topic the sub-topic
     * @return a new help context
     */
    private HelpContext subtopic(String topic) {
        return super.getHelpContext().subtopic(topic);
    }

    /**
     * Helper to add a tab to the tab pane.
     *
     * @param name  the tab name
     * @param model the tab model
     * @param tab   the component
     */
    private void addTab(String name, ObjectTabPaneModel<TabComponent> model, TabComponent tab) {
        int index = model.size();
        int shortcut = index + 1;
        String text = "&" + shortcut + " " + Messages.get(name);
        model.addTab(tab, text, tab.getComponent());
    }
}
