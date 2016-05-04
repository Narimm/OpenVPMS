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

package org.openvpms.web.workspace.patient.visit;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.workspace.patient.history.AbstractPatientHistoryBrowser;
import org.openvpms.web.workspace.patient.history.AbstractPatientHistoryCRUDWindow;

/**
 * A patient history browser that provides CRUD operations.
 *
 * @author Tim Anderson
 */
public class AbstractVisitHistoryBrowserCRUDWindow extends VisitBrowserCRUDWindow<Act> {

    /**
     * Default constructor.
     */
    protected AbstractVisitHistoryBrowserCRUDWindow() {
        super();
    }

    /**
     * Constructs a {@link AbstractVisitHistoryBrowserCRUDWindow}.
     *
     * @param browser the browser
     * @param window  the window
     */
    public AbstractVisitHistoryBrowserCRUDWindow(AbstractPatientHistoryBrowser browser,
                                                 AbstractPatientHistoryCRUDWindow window) {
        super(browser, window);
    }

    /**
     * Returns the browser.
     *
     * @return the browser
     */
    @Override
    public AbstractPatientHistoryBrowser getBrowser() {
        return (AbstractPatientHistoryBrowser) super.getBrowser();
    }

    /**
     * Returns the CRUD window.
     *
     * @return the window
     */
    @Override
    public AbstractPatientHistoryCRUDWindow getWindow() {
        return (AbstractPatientHistoryCRUDWindow) super.getWindow();
    }

    /**
     * Invoked when an object is double clicked.
     */
    protected void onDoubleClick() {
        AbstractPatientHistoryCRUDWindow window = getWindow();
        if (window.canEdit()) {
            window.edit();
        } else {
            window.view();
        }
    }

}
