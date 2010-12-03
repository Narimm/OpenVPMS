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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.customer.note;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.SplitPane;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.app.customer.CustomerActWorkspace;
import org.openvpms.web.app.subsystem.CRUDWindow;
import org.openvpms.web.app.subsystem.DefaultCRUDWindow;
import org.openvpms.web.component.im.query.ActQuery;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.TabbedBrowserListener;
import org.openvpms.web.component.im.util.Archetypes;
import org.openvpms.web.component.util.SplitPaneFactory;


/**
 * Customer note workspace.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
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
    private static final String[] SHORT_NAMES = {CustomerAlertQuery.CUSTOMER_ALERT, NoteQuery.CUSTOMER_NOTE};


    /**
     * Constructs a new <tt>NoteAlertWorkspace</tt>.
     */
    public NoteAlertWorkspace() {
        super("customer", "note");
        setChildArchetypes(Act.class, SHORT_NAMES);
        alertArchetypes = Archetypes.create(CustomerAlertQuery.CUSTOMER_ALERT, Act.class);
        noteArchetypes = Archetypes.create(NoteQuery.CUSTOMER_NOTE, Act.class);
    }

    /**
     * Creates a new CRUD window for viewing and editing acts.
     *
     * @return a {@link NoteCRUDWindow}, as this is the first view.
     */
    protected CRUDWindow<Act> createCRUDWindow() {
        return new NoteCRUDWindow(noteArchetypes);
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
        NoteAlertBrowser browser = new NoteAlertBrowser(query, alertsQuery);
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
            window = new DefaultCRUDWindow<Act>(alertArchetypes);
        } else {
            window = new DefaultCRUDWindow<Act>(noteArchetypes);
        }

        Act selected = browser.getSelected();
        if (selected != null) {
            window.setObject(selected);
        }
        setCRUDWindow(window);
        setWorkspace(createWorkspace());
    }

}
