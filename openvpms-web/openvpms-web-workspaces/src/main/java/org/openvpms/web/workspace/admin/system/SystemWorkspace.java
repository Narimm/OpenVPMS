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

package org.openvpms.web.workspace.admin.system;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.workspace.TabComponent;
import org.openvpms.web.component.workspace.TabbedWorkspace;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.tabpane.ObjectTabPaneModel;


/**
 * System workspace.
 *
 * @author Tim Anderson
 */
public class SystemWorkspace extends TabbedWorkspace<IMObject> {

    /**
     * Constructs an {@link SystemWorkspace}.
     */
    public SystemWorkspace(Context context) {
        super("admin.system", context);
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
        addInfoBrowser(model);
        addPluginBrowser(model);
        addDocumentLockBrowser(model);
    }

    /**
     * Adds a system info browser to the tabbed pane.
     *
     * @param model the tab pane model
     */
    private void addInfoBrowser(ObjectTabPaneModel<TabComponent> model) {
        HelpContext help = subtopic("session");
        addTab("admin.system.session", model, new SessionBrowser(help));
    }

    /**
     * Adds a plugin browser to the tabbed pane.
     *
     * @param model the tab pane model
     */
    private void addPluginBrowser(ObjectTabPaneModel<TabComponent> model) {
        HelpContext help = subtopic("plugin");
        addTab("admin.system.plugin", model, new PluginBrowser(help));
    }

    /**
     * Adds a WebDAV document lock browser to the tabbed pane.
     *
     * @param model the tab pane model
     */
    private void addDocumentLockBrowser(ObjectTabPaneModel<TabComponent> model) {
        HelpContext help = subtopic("webdav");
        addTab("admin.system.webdav", model, new WebDAVLockBrowser(help));
    }

}
