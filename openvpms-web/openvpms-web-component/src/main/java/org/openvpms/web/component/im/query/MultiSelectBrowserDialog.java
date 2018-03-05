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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.query;

import org.openvpms.web.echo.help.HelpContext;

import java.util.Collection;

/**
 * Displays a {@link MultiSelectBrowser} in a popup dialog.
 *
 * @author Tim Anderson
 */
public class MultiSelectBrowserDialog<T> extends BrowserDialog<T> {

    /**
     * Constructs a {@link MultiSelectBrowserDialog}.
     *
     * @param title   the dialog title
     * @param browser the browser
     * @param help    the help context
     */
    public MultiSelectBrowserDialog(String title, MultiSelectBrowser<T> browser, HelpContext help) {
        super(title, null, OK_CANCEL, browser, false, help);
        enableOK();
    }

    /**
     * Returns the browser.
     *
     * @return the browser
     */
    @Override
    public MultiSelectBrowser<T> getBrowser() {
        return (MultiSelectBrowser<T>) super.getBrowser();
    }

    /**
     * Determines if an object has been selected.
     *
     * @return {@code true} if an object has been selected, otherwise {@code false}
     */
    @Override
    public boolean isSelected() {
        return !getBrowser().getSelections().isEmpty();
    }

    /**
     * Returns the selections.
     *
     * @return the selections
     */
    public Collection<T> getSelections() {
        return getBrowser().getSelections();
    }

    /**
     * Updates the current selection using {@link #setSelected} , but doesn't close the browser.
     *
     * @param object the selected object
     */
    @Override
    protected void onBrowsed(T object) {
        super.onBrowsed(object);
        enableOK();
    }

    /**
     * Enables the OK button if something is selected, otherwise disables it.
     */
    private void enableOK() {
        getButtons().setEnabled(OK_ID, isSelected());
    }
}
