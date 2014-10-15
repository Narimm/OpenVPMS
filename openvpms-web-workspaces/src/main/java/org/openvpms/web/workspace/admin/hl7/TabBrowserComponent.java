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

/**
 * Tab component that displays a {@link Browser}.
 *
 * @author Tim Anderson
 */
public class TabBrowserComponent<T extends IMObject> implements TabComponent {

    /**
     * The browser.
     */
    private final Browser<T> browser;

    /**
     * Constructs a {@link TabBrowserComponent}.
     *
     * @param browser the browser
     */
    public TabBrowserComponent(Browser<T> browser) {
        this.browser = browser;
    }

    /**
     * Invoked when the tab is displayed.
     */
    @Override
    public void show() {
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
        return browser.getComponent();
    }

    /**
     * Returns the button component.
     *
     * @return {@code null} - this doesn't support buttons
     */
    @Override
    public Component getButtons() {
        return null;
    }
}
