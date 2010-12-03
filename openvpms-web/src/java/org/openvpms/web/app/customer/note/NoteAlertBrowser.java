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

package org.openvpms.web.app.customer.note;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.im.query.BrowserFactory;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.TabbedBrowser;
import org.openvpms.web.resource.util.Messages;


/**
 * Customer alert/note browser.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class NoteAlertBrowser extends TabbedBrowser<Act> {

    /**
     * The alerts browser index.
     */
    private int alertsIndex;


    /**
     * Constructs an <code>NoteAlertBrowser</code> that queries acts using the specified queries.
     *
     * @param notes  query for notes
     * @param alerts query for alerts
     */
    public NoteAlertBrowser(Query<Act> notes, Query<Act> alerts) {
        addBrowser(Messages.get("customer.note.notes"), BrowserFactory.create(notes));
        alertsIndex = addBrowser(Messages.get("customer.note.alerts"), BrowserFactory.create(alerts));
    }

    /**
     * Determines if the current browser is the alerts browser.
     *
     * @return <tt>true</tt> if the current browser is the alerts browser; <tt>false</tt> if it is the notes browser
     */
    public boolean isAlertsBrowser() {
        return getSelectedBrowserIndex() == alertsIndex;
    }

}