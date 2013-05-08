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
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;

import java.util.List;

/**
 * A patient medical record browser that CRUD operations.
 *
 * @author Tim Anderson
 */
public class VisitBrowserCRUDWindow extends BrowserCRUDWindow<Act> {

    /**
     * The help context.
     */
    private final HelpContext help;

    /**
     * Constructs a {@code VisitBrowserCRUDWindow}.
     *
     * @param query   the patient medical record query
     * @param context the context
     * @param help    the help context
     */
    public VisitBrowserCRUDWindow(PatientHistoryQuery query, Context context, HelpContext help) {
        this.help = help;
        DefaultLayoutContext layout = new DefaultLayoutContext(context, help);
        PatientHistoryBrowser browser = new PatientHistoryBrowser(query, layout);
        if (browser.getSelected() == null) {
            browser.query();
            List<Act> objects = browser.getObjects();
            if (!objects.isEmpty()) {
                browser.setSelected(objects.get(0));
            }
        }
        setBrowser(browser);
        VisitCRUDWindow window = createWindow(context);
        window.setMailContext(new CustomerMailContext(context, help));
        window.setQuery(query);
        window.setEvent(browser.getEvent());
        setWindow(window);
    }

    /**
     * Sets the selected object.
     *
     * @param object the selected object
     */
    @Override
    public void setSelected(Act object) {
        super.setSelected(object);
        getWindow().setEvent(getBrowser().getEvent());
    }

    /**
     * Returns the CRUD window.
     *
     * @return the window
     */
    @Override
    public VisitCRUDWindow getWindow() {
        return (VisitCRUDWindow) super.getWindow();
    }

    /**
     * Returns the browser.
     *
     * @return the browser
     */
    @Override
    public PatientHistoryBrowser getBrowser() {
        return (PatientHistoryBrowser) super.getBrowser();
    }

    /**
     * Creates a new window.
     *
     * @param context the context
     * @return a new window
     */
    protected VisitCRUDWindow createWindow(Context context) {
        return new VisitCRUDWindow(context, help);
    }

    /**
     * Selects the current object. If the object is "double clicked", edits it.
     *
     * @param object the selected object
     */
    @Override
    protected void onSelected(Act object) {
        PatientHistoryBrowser browser = getBrowser();
        VisitCRUDWindow window = getWindow();
        window.setEvent(browser.getEvent(object));
        super.onSelected(object);
    }
}
