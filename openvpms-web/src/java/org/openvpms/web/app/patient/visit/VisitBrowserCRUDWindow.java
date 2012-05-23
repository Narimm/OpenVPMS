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
 *  Copyright 2012 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.app.patient.visit;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.app.customer.CustomerMailContext;
import org.openvpms.web.app.patient.history.PatientHistoryBrowser;
import org.openvpms.web.app.patient.history.PatientHistoryQuery;
import org.openvpms.web.component.app.Context;

/**
 * A patient medical record browser that CRUD operations.
 *
 * @author Tim Anderson
 */
public class VisitBrowserCRUDWindow extends BrowserCRUDWindow<Act> {

    /**
     * Constructs a {@code VisitBrowserCRUDWindow}.
     *
     * @param query the patient medical record query
     * @param context the context
     */
    public VisitBrowserCRUDWindow(PatientHistoryQuery query, Context context) {
        PatientHistoryBrowser browser = new PatientHistoryBrowser(query);
        setBrowser(browser);
        VisitCRUDWindow window = new VisitCRUDWindow(context);
        window.setMailContext(new CustomerMailContext(context));
        window.setQuery(query);
        window.setEvent(browser.getEvent());
        setWindow(window);
    }

    /**
     * Selects the current object. If the object is "double clicked", edits it.
     *
     * @param object the selected object
     */
    @Override
    protected void onSelected(Act object) {
        PatientHistoryBrowser browser = (PatientHistoryBrowser) getBrowser();
        VisitCRUDWindow window = (VisitCRUDWindow) getWindow();
        window.setEvent(browser.getEvent(object));
        super.onSelected(object);
    }
}
