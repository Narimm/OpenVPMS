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

package org.openvpms.web.workspace.admin.hl7;

import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.workspace.AbstractCRUDWindow;
import org.openvpms.web.component.workspace.BrowserCRUDWindow;
import org.openvpms.web.component.workspace.CRUDWindow;

/**
 * Tab component that links a {@link Browser} to a {@link CRUDWindow}.
 *
 * @author Tim Anderson
 */
public class HL7BrowserCRUDWindow<T extends IMObject> extends BrowserCRUDWindow<T> implements TabComponent {

    /**
     * Constructs a {@link HL7BrowserCRUDWindow}.
     *
     * @param browser the browser
     * @param window  the window
     */
    public HL7BrowserCRUDWindow(Browser<T> browser, AbstractCRUDWindow<T> window) {
        super(browser, window);
    }

    /**
     * Invoked when the tab is displayed.
     */
    @Override
    public void show() {
        Browser<T> browser = getBrowser();
        T selected = browser.getSelected();
        browser.query();
        if (selected != null) {
            browser.setSelected(selected);
        }
        browser.setFocusOnResults();
    }

    /**
     * Returns the tab component.
     *
     * @return the tab component
     */
    @Override
    public Component getComponent() {
        return getBrowser().getComponent();
    }

    /**
     * Returns the button component.
     *
     * @return the button component
     */
    @Override
    public Component getButtons() {
        return getWindow().getComponent();
    }
}
