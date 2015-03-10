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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.visit;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.SplitPane;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.workspace.AbstractCRUDWindow;
import org.openvpms.web.component.workspace.AbstractViewCRUDWindow;
import org.openvpms.web.component.workspace.BrowserCRUDWindow;
import org.openvpms.web.component.workspace.CRUDWindow;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.factory.SplitPaneFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.pane.ContentPane;


/**
 * Links a {@link Browser} to a {@link CRUDWindow}.
 *
 * @author Tim Anderson
 */
public class VisitBrowserCRUDWindow<T extends Act> extends BrowserCRUDWindow<T> implements VisitEditorTab {

    /**
     * The tab identifier.
     */
    private int id;

    /**
     * Constructs a {@code VisitBrowserCRUDWindow}.
     */
    protected VisitBrowserCRUDWindow() {
        super();
    }

    /**
     * Constructs a {@link VisitBrowserCRUDWindow}.
     *
     * @param browser the browser
     * @param window  the window
     */
    public VisitBrowserCRUDWindow(Browser<T> browser, AbstractCRUDWindow<T> window) {
        super(browser, window);
    }


    /**
     * Sets the buttons.
     *
     * @param buttons the buttons
     */
    public void setButtons(ButtonSet buttons) {
        ((AbstractCRUDWindow<T>) getWindow()).setButtons(buttons);
    }

    /**
     * Returns the help context.
     *
     * @return the help context
     */
    public HelpContext getHelpContext() {
        return getWindow().getHelpContext();
    }

    /**
     * Returns the identifier of this tab.
     *
     * @return the tab identifier
     */
    @Override
    public int getId() {
        return id;
    }

    /**
     * Sets the identifier of this tab.
     *
     * @param id the tab identifier
     */
    @Override
    public void setId(int id) {
        this.id = id;
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
     * Invoked prior to switching to another tab, in order to save state.
     * <p/>
     * If the save fails, then switching is cancelled.
     *
     * @return {@code true} if the save was successful, otherwise {@code false}
     */
    @Override
    public boolean save() {
        return true;
    }

    /**
     * Returns the component.
     *
     * @return the component
     */
    public Component getComponent() {
        Component result;
        CRUDWindow<T> window = getWindow();
        if (window instanceof AbstractViewCRUDWindow) {
            result = SplitPaneFactory.create(SplitPane.ORIENTATION_VERTICAL,
                                             "PatientRecordWorkspace.Layout",
                                             getBrowser().getComponent(),
                                             window.getComponent());
        } else {
            ContentPane pane = new ContentPane(); // add the browser to a pane to get scroll bars
            pane.add(getBrowser().getComponent());
            result = pane;
        }
        return result;
    }

}
