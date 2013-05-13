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

package org.openvpms.web.workspace.customer.note;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.SplitPane;
import org.openvpms.archetype.rules.customer.CustomerArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.query.ActQuery;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.TabbedBrowserListener;
import org.openvpms.web.component.subsystem.CRUDWindow;
import org.openvpms.web.component.subsystem.DefaultCRUDWindow;
import org.openvpms.web.echo.factory.SplitPaneFactory;
import org.openvpms.web.workspace.customer.CustomerActWorkspace;


/**
 * Customer note workspace.
 *
 * @author Tim Anderson
 */
public class NoteAlertWorkspace extends CustomerActWorkspace<Act> {

    /**
     * The alert archetypes.
     */
    private Archetypes<Act> alertArchetypes;

    /**
     * The note archetypes.
     */
    private Archetypes<Act> noteArchetypes;

    /**
     * Short names supported by the workspace.
     */
    private static final String[] SHORT_NAMES = {CustomerArchetypes.ALERT, NoteQuery.CUSTOMER_NOTE};


    /**
     * Constructs a new {@code NoteAlertWorkspace}.
     */
    public NoteAlertWorkspace(Context context) {
        super("customer", "note", context);
        setChildArchetypes(Act.class, SHORT_NAMES);
        alertArchetypes = Archetypes.create(CustomerArchetypes.ALERT, Act.class);
        noteArchetypes = Archetypes.create(NoteQuery.CUSTOMER_NOTE, Act.class);
    }

    /**
     * Creates a new CRUD window for viewing and editing acts.
     *
     * @return a {@link NoteCRUDWindow}, as this is the first view.
     */
    protected CRUDWindow<Act> createCRUDWindow() {
        return new NoteCRUDWindow(noteArchetypes, getContext(), getHelpContext());
    }

    /**
     * Creates a new query.
     *
     * @return a {@link NoteQuery}, as this is the first view.
     */
    protected ActQuery<Act> createQuery() {
        return new NoteQuery(getObject());
    }

    /**
     * Creates a new browser to query and display acts.
     *
     * @param query the query
     * @return a new browser
     */
    @Override
    protected Browser<Act> createBrowser(Query<Act> query) {
        Query<Act> alertsQuery = new CustomerAlertQuery(getObject());
        DefaultLayoutContext context = new DefaultLayoutContext(getContext(), getHelpContext());
        NoteAlertBrowser browser = new NoteAlertBrowser(query, alertsQuery, context);
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
        NoteAlertBrowser browser = (NoteAlertBrowser) getBrowser();
        CRUDWindow<Act> window;
        if (browser.isAlertsBrowser()) {
            window = new DefaultCRUDWindow<Act>(alertArchetypes, getContext(), getHelpContext());
        } else {
            window = new DefaultCRUDWindow<Act>(noteArchetypes, getContext(), getHelpContext());
        }

        Act selected = browser.getSelected();
        if (selected != null) {
            window.setObject(selected);
        }
        setCRUDWindow(window);
        setWorkspace(createWorkspace());
    }

}
