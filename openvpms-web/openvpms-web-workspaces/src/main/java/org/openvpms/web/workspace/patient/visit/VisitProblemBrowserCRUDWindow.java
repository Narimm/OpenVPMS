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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.visit;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.workspace.patient.problem.ProblemBrowser;
import org.openvpms.web.workspace.patient.problem.ProblemRecordCRUDWindow;

/**
 * A patient problem browser that provides CRUD operations.
 *
 * @author Tim Anderson
 */
public class VisitProblemBrowserCRUDWindow extends BrowserCRUDWindow<Act> {

    /**
     * Constructs a {@link VisitProblemBrowserCRUDWindow}.
     *
     * @param browser the browser
     * @param window  the window
     */
    public VisitProblemBrowserCRUDWindow(ProblemBrowser browser, ProblemRecordCRUDWindow window) {
        super(browser, window);
    }

    /**
     * Returns the browser.
     *
     * @return the browser
     */
    @Override
    public ProblemBrowser getBrowser() {
        return (ProblemBrowser) super.getBrowser();
    }

    /**
     * Returns the CRUD window.
     *
     * @return the window
     */
    @Override
    public ProblemRecordCRUDWindow getWindow() {
        return (ProblemRecordCRUDWindow) super.getWindow();
    }

    /**
     * Selects the current object.
     *
     * @param object the selected object
     */
    @Override
    protected void select(Act object) {
        super.select(object);
        ProblemBrowser browser = getBrowser();
        ProblemRecordCRUDWindow window = getWindow();
        Act selectedParent = browser.getSelectedParent();
        window.setProblem(selectedParent);
        window.setEvent(browser.getEvent(object));
    }

}
