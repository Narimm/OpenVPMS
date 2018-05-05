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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.admin.hl7;

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
import org.openvpms.web.component.workspace.BrowserCRUDWindowTab;
import org.openvpms.web.component.workspace.DefaultCRUDWindow;
import org.openvpms.web.component.workspace.TabComponent;
import org.openvpms.web.component.workspace.TabbedWorkspace;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.tabpane.ObjectTabPaneModel;
import org.openvpms.web.resource.i18n.Messages;


/**
 * HL7 workspace.
 *
 * @author Tim Anderson
 */
public class HL7Workspace extends TabbedWorkspace<IMObject> {

    /**
     * Constructs an {@link HL7Workspace}.
     */
    public HL7Workspace(Context context) {
        super("admin.hl7", context);
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
     * Adds tabs to the tabbed pane.
     *
     * @param model the tabbed pane model
     */
    @Override
    protected void addTabs(ObjectTabPaneModel<TabComponent> model) {
        addServiceBrowser(model);
        addConnectorBrowser(model);
        addMappingBrowser(model);
    }

    /**
     * Adds a service browser to the tab pane.
     *
     * @param model the tab pane model
     */
    private void addServiceBrowser(ObjectTabPaneModel<TabComponent> model) {
        Context context = getContext();
        HelpContext help = subtopic("service");
        Query<IMObject> query = QueryFactory.create(HL7Archetypes.SERVICES, context);
        Browser<IMObject> browser = BrowserFactory.create(query, new DefaultLayoutContext(context, help));
        Archetypes<IMObject> archetypes = Archetypes.create(HL7Archetypes.SERVICES, IMObject.class,
                                                            Messages.get("admin.hl7.service.type"));
        DefaultCRUDWindow<IMObject> window = new DefaultCRUDWindow<>(archetypes, context, help);
        addTab("admin.hl7.services", model, new BrowserCRUDWindowTab<>(browser, window));
    }

    /**
     * Adds a connector browser to the tab pane.
     *
     * @param model the tab pane model
     */
    private void addConnectorBrowser(ObjectTabPaneModel<TabComponent> model) {
        Context context = getContext();
        HelpContext help = subtopic("connector");
        Query<Entity> query = QueryFactory.create(HL7Archetypes.CONNECTORS, context);
        Browser<Entity> browser = new HL7ConnectorBrowser(query, new DefaultLayoutContext(context, help));
        Archetypes<Entity> archetypes = Archetypes.create(HL7Archetypes.CONNECTORS, Entity.class,
                                                          Messages.get("admin.hl7.connector.type"));
        HL7ConnectorCRUDWindow window = new HL7ConnectorCRUDWindow(archetypes, getContext(), help);
        addTab("admin.hl7.connectors", model, new BrowserCRUDWindowTab<>(browser, window));
    }

    /**
     * Adds a mapping browser to the tab pane.
     *
     * @param model the tab pane model
     */
    private void addMappingBrowser(ObjectTabPaneModel<TabComponent> model) {
        Context context = getContext();
        HelpContext help = subtopic("mapping");
        Query<Entity> query = QueryFactory.create(HL7Archetypes.MAPPINGS, context);
        Browser<Entity> browser = BrowserFactory.create(query, new DefaultLayoutContext(context, help));
        Archetypes<Entity> archetypes = Archetypes.create(HL7Archetypes.MAPPINGS, Entity.class,
                                                          Messages.get("admin.hl7.mapping.type"));
        HL7MappingCRUDWindow window = new HL7MappingCRUDWindow(archetypes, context, help);
        addTab("admin.hl7.mappings", model, new BrowserCRUDWindowTab<>(browser, window));
    }

}
