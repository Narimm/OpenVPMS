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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.customer.communication;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.SplitPane;
import org.openvpms.archetype.rules.customer.CustomerArchetypes;
import org.openvpms.archetype.rules.prefs.Preferences;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.ActQuery;
import org.openvpms.web.component.im.query.BrowserFactory;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.TabbedBrowser;
import org.openvpms.web.component.im.query.TabbedBrowserListener;
import org.openvpms.web.component.workspace.CRUDWindow;
import org.openvpms.web.component.workspace.DefaultCRUDWindow;
import org.openvpms.web.echo.factory.SplitPaneFactory;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.workspace.customer.CustomerActWorkspace;


/**
 * Customer communications workspace.
 *
 * @author Tim Anderson
 */
public class CommunicationWorkspace extends CustomerActWorkspace<Act> {

    /**
     * The alert archetypes.
     */
    private Archetypes<Act> alertArchetypes;


    /**
     * Short names supported by the workspace.
     */
    private static final String[] SHORT_NAMES = {CustomerArchetypes.ALERT, CommunicationArchetypes.ACTS};


    /**
     * Constructs a {@link CommunicationWorkspace}.
     *
     * @param context     the context
     * @param preferences user preferences
     */
    public CommunicationWorkspace(Context context, Preferences preferences) {
        super("customer.communication", context, preferences);
        setChildArchetypes(Act.class, SHORT_NAMES);
        alertArchetypes = Archetypes.create(CustomerArchetypes.ALERT, Act.class);
    }

    /**
     * Determines if the workspace can be updated with instances of the specified archetype.
     *
     * @param shortName the archetype's short name
     * @return {@code true} if {@code shortName} is one of those in {@link #getArchetypes()}
     */
    @Override
    public boolean canUpdate(String shortName) {
        return super.canUpdate(shortName) || TypeHelper.matches(shortName, CommunicationArchetypes.ACTS);
    }

    /**
     * Invoked when the workspace is displayed.
     */
    @Override
    public void show() {
        super.show();
        if (getBrowser() != null) {
            getBrowser().query();
        }
    }

    /**
     * Creates a new CRUD window for viewing and editing acts.
     *
     * @return a {@link DefaultCRUDWindow}, as this is the first view.
     */
    protected CRUDWindow<Act> createCRUDWindow() {
        return new CommunicationCRUDWindow(getContext(), getHelpContext());
    }

    /**
     * Creates a new query.
     *
     * @return a {@link CommunicationQuery}, as this is the first view.
     */
    protected ActQuery<Act> createQuery() {
        return new CommunicationQuery(getObject(), new DefaultLayoutContext(getContext(), getHelpContext()));
    }

    /**
     * Creates a new browser to query and display acts.
     *
     * @param query the query
     * @return a new browser
     */
    @Override
    protected Browser createBrowser(Query<Act> query) {
        Query<Act> alertsQuery = new CustomerAlertQuery(getObject());
        DefaultLayoutContext context = new DefaultLayoutContext(getContext(), getHelpContext());
        Browser browser = new Browser(query, alertsQuery, context);
        browser.setListener(new TabbedBrowserListener() {
            public void onBrowserChanged() {
                changeCRUDWindow();
            }
        });
        return browser;
    }

    /**
     * Creates the workspace component.
     *
     * @return a new workspace
     */
    @Override
    protected Component createWorkspace() {
        return SplitPaneFactory.create(SplitPane.ORIENTATION_VERTICAL,
                                       "BrowserCRUDWorkspace.Layout", getBrowser().getComponent(),
                                       getCRUDWindow().getComponent());
    }

    /**
     * Changes the CRUD window depending on the current browser view.
     */
    private void changeCRUDWindow() {
        Browser browser = (Browser) getBrowser();
        CRUDWindow<Act> window;
        if (browser.isAlertsBrowser()) {
            window = new DefaultCRUDWindow<>(alertArchetypes, getContext(), getHelpContext());
        } else {
            window = new CommunicationCRUDWindow(getContext(), getHelpContext());
        }

        Act selected = browser.getSelected();
        if (selected != null) {
            window.setObject(selected);
        }
        setCRUDWindow(window);
        setWorkspace(createWorkspace());
    }

    private static class Browser extends TabbedBrowser<Act> {

        /**
         * The alerts browser index.
         */
        private int alertsIndex;

        /**
         * Constructs an {@link Browser} that queries acts using the specified queries.
         *
         * @param communications query for communications
         * @param alerts         query for alerts
         * @param context        the layout context
         */
        public Browser(Query<Act> communications, Query<Act> alerts, LayoutContext context) {
            addBrowser(Messages.get("customer.communication.communications"),
                       BrowserFactory.create(communications, context));
            alertsIndex = addBrowser(Messages.get("customer.communication.alerts"),
                                     BrowserFactory.create(alerts, context));
        }

        /**
         * Determines if the current browser is the alerts browser.
         *
         * @return {@code true} if the current browser is the alerts browser; {@code false} if it is the communication
         * browser
         */
        public boolean isAlertsBrowser() {
            return getSelectedBrowserIndex() == alertsIndex;
        }

    }
}

