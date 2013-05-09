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
 */

package org.openvpms.web.app.customer.note;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.BrowserFactory;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.TabbedBrowser;
import org.openvpms.web.resource.i18n.Messages;


/**
 * Customer alert/note browser.
 *
 * @author Tim Anderson
 */
public class NoteAlertBrowser extends TabbedBrowser<Act> {

    /**
     * The alerts browser index.
     */
    private int alertsIndex;


    /**
     * Constructs an {@code NoteAlertBrowser} that queries acts using the specified queries.
     *
     * @param notes   query for notes
     * @param alerts  query for alerts
     * @param context the layout context
     */
    public NoteAlertBrowser(Query<Act> notes, Query<Act> alerts, LayoutContext context) {
        addBrowser(Messages.get("customer.note.notes"), BrowserFactory.create(notes, context));
        alertsIndex = addBrowser(Messages.get("customer.note.alerts"), BrowserFactory.create(alerts, context));
    }

    /**
     * Determines if the current browser is the alerts browser.
     *
     * @return {@code true} if the current browser is the alerts browser; {@code false} if it is the notes browser
     */
    public boolean isAlertsBrowser() {
        return getSelectedBrowserIndex() == alertsIndex;
    }

}