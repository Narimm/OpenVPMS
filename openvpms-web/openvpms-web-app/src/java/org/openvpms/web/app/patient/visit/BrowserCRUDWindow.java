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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.app.patient.visit;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.SplitPane;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.button.ButtonSet;
import org.openvpms.web.component.help.HelpContext;
import org.openvpms.web.component.im.query.AbstractBrowserListener;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.subsystem.AbstractCRUDWindow;
import org.openvpms.web.component.subsystem.AbstractViewCRUDWindow;
import org.openvpms.web.component.subsystem.CRUDWindow;
import org.openvpms.web.component.subsystem.CRUDWindowListener;
import org.openvpms.web.component.util.DoubleClickMonitor;
import org.openvpms.web.component.util.SplitPaneFactory;


/**
 * Links a {@link Browser} to a {@link CRUDWindow}.
 *
 * @author Tim Anderson
 */
public class BrowserCRUDWindow<T extends IMObject> {

    /**
     * The browser.
     */
    private Browser<T> browser;

    /**
     * The CRUD window.
     */
    private AbstractCRUDWindow<T> window;

    /**
     * Helper to monitor double clicks. When an act is double clicked, an edit dialog is displayed
     */
    private DoubleClickMonitor click = new DoubleClickMonitor();


    /**
     * Constructs a {@code BrowserCRUDWindow}.
     */
    protected BrowserCRUDWindow() {

    }

    /**
     * Constructs a {@code BrowserCRUDWindow}.
     *
     * @param browser the browser
     * @param window  the window
     */
    public BrowserCRUDWindow(Browser<T> browser, AbstractCRUDWindow<T> window) {
        setBrowser(browser);
        setWindow(window);
    }

    /**
     * Sets the buttons.
     *
     * @param buttons the buttons
     */
    public void setButtons(ButtonSet buttons) {
        window.setButtons(buttons);
    }

    /**
     * Returns the help context.
     *
     * @return the help context
     */
    public HelpContext getHelpContext() {
        return window.getHelpContext();
    }

    /**
     * Returns the component.
     *
     * @return the component
     */
    public Component getComponent() {
        Component result;
        if (window instanceof AbstractViewCRUDWindow) {
            result = SplitPaneFactory.create(SplitPane.ORIENTATION_VERTICAL,
                                             "PatientRecordWorkspace.Layout",
                                             browser.getComponent(),
                                             window.getComponent());
        } else {
            result = browser.getComponent();
        }
        return result;
    }

    /**
     * Returns the browser.
     *
     * @return the browser
     */
    public Browser<T> getBrowser() {
        return browser;
    }

    /**
     * Returns the CRUD window.
     *
     * @return the window
     */
    public CRUDWindow<T> getWindow() {
        return window;
    }

    /**
     * Sets the selected object.
     *
     * @param object the selected object
     */
    public void setSelected(T object) {
        browser.setSelected(object);
        if (window != null) {
            window.setObject(browser.getSelected());
        }
    }

    /**
     * Registers the browser.
     *
     * @param browser the browser
     */
    protected void setBrowser(Browser<T> browser) {
        this.browser = browser;
        browser.addBrowserListener(new AbstractBrowserListener<T>() {
            public void selected(T object) {
                onSelected(object);
            }
        });
        if (window != null) {
            window.setObject(browser.getSelected());
        }
    }

    /**
     * Registers the window.
     *
     * @param window the window
     */
    protected void setWindow(AbstractCRUDWindow<T> window) {
        this.window = window;
        window.setListener(new CRUDWindowListener<T>() {
            public void saved(T object, boolean isNew) {
                refreshBrowser(object);
            }

            public void deleted(T object) {
                refreshBrowser(null);
            }

            public void refresh(T object) {
                if (object.isNew()) {
                    // object not persistent, so don't attempt to reselect after refresh
                    refreshBrowser(null);
                } else {
                    refreshBrowser(object);
                }
            }
        });
        if (browser != null) {
            window.setObject(browser.getSelected());
        }
    }

    /**
     * Selects the current object. If the object is "double clicked", edits it.
     *
     * @param object the selected object
     */
    protected void onSelected(T object) {
        window.setObject(object);
        if (click.isDoubleClick(object.getId())) {
            window.edit();
        }
    }

    /**
     * Refresh the browser.
     *
     * @param object the object to select. May be <tt>null</tt>
     */
    private void refreshBrowser(T object) {
        browser.query();
        browser.setSelected(object);
    }

}
