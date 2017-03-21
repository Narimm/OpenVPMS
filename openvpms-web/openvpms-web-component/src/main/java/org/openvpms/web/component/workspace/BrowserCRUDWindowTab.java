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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.workspace;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.SplitPane;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.echo.factory.SplitPaneFactory;
import org.openvpms.web.echo.help.HelpContext;

/**
 * Tab component that links a {@link Browser} to a {@link CRUDWindow}.
 *
 * @author Tim Anderson
 */
public class BrowserCRUDWindowTab<T extends IMObject> extends BrowserCRUDWindow<T> implements TabComponent {

    /**
     * Determines if the browser should be refreshed when the tab is displayed.
     */
    private final boolean refreshOnShow;

    /**
     * Constructs a {@link BrowserCRUDWindowTab}.
     *
     * @param browser the browser
     * @param window  the window
     */
    public BrowserCRUDWindowTab(Browser<T> browser, AbstractCRUDWindow<T> window) {
        this(browser, window, true);
    }

    /**
     * Constructs a {@link BrowserCRUDWindowTab}.
     *
     * @param browser       the browser
     * @param window        the window
     * @param refreshOnShow determines if the browser should be refreshed when the tab is displayed.
     */
    public BrowserCRUDWindowTab(Browser<T> browser, AbstractCRUDWindow<T> window, boolean refreshOnShow) {
        super(browser, window);
        this.refreshOnShow = refreshOnShow;
    }

    /**
     * Invoked when the tab is displayed.
     */
    @Override
    public void show() {
        if (refreshOnShow) {
            Browser<T> browser = getBrowser();
            T selected = browser.getSelected();
            browser.query();
            if (selected != null) {
                browser.setSelected(selected);
            }
            browser.setFocusOnResults();
        }
    }

    /**
     * Invoked when a query is performed.
     */
    @Override
    protected void onQuery() {
        T selected = getBrowser().getSelected();
        setSelected(selected);
    }

    /**
     * Returns the tab component.
     *
     * @return the tab component
     */
    @Override
    public Component getComponent() {
        return SplitPaneFactory.create(SplitPane.ORIENTATION_VERTICAL, "BrowserCRUDWorkspace.Layout",
                                       getBrowser().getComponent(), getWindow().getComponent());
    }

    /**
     * Returns the help context for the tab.
     *
     * @return the help context
     */
    @Override
    public HelpContext getHelpContext() {
        return getWindow().getHelpContext();
    }
}
