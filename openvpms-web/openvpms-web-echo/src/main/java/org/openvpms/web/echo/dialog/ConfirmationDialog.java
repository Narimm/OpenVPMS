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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.echo.dialog;


import org.openvpms.web.component.help.HelpContext;

/**
 * A modal dialog that prompts the user to select an OK or Cancel button.
 *
 * @author Tim Anderson
 */
public class ConfirmationDialog extends MessageDialog {

    /**
     * Dialog style name.
     */
    private static final String STYLE = "ConfirmationDialog";


    /**
     * Constructs a {@code ConfirmationDialog}.
     *
     * @param title   the window title
     * @param message the message
     */
    public ConfirmationDialog(String title, String message) {
        this(title, message, OK_CANCEL);
    }

    /**
     * Constructs a {@code ConfirmationDialog}.
     *
     * @param title   the window title
     * @param message the message
     * @param help    the help context. May be {@code null}
     */
    public ConfirmationDialog(String title, String message, HelpContext help) {
        this(title, message, OK_CANCEL, help);
    }

    /**
     * Constructs a {@code ConfirmationDialog}.
     *
     * @param title   the window title
     * @param message the message
     * @param buttons the buttons to display
     */
    public ConfirmationDialog(String title, String message, String[] buttons) {
        super(title, message, STYLE, buttons);
    }

    /**
     * Constructs a {@code ConfirmationDialog}.
     *
     * @param title   the window title
     * @param message the message
     * @param buttons the buttons to display
     * @param help    the help context. May be {@code null}
     */
    public ConfirmationDialog(String title, String message, String[] buttons, HelpContext help) {
        super(title, message, STYLE, buttons, help);
    }

    /**
     * Constructs a {@code ConfirmationDialog}.
     *
     * @param title             the window title
     * @param message           the message
     * @param disableOKShortcut if {@code true} disable any shortcut on the OK button
     */
    public ConfirmationDialog(String title, String message, boolean disableOKShortcut) {
        super(title, message, new String[0]);
        addButton(OK_ID, disableOKShortcut);
        addButton(CANCEL_ID, false);
        setDefaultButton(CANCEL_ID);
    }

    /**
     * Constructs a {@code ConfirmationDialog}.
     *
     * @param title             the window title
     * @param message           the message
     * @param disableOKShortcut if {@code true} disable any shortcut on the OK button
     * @param help              the help context. May be {@code null}
     */
    public ConfirmationDialog(String title, String message, boolean disableOKShortcut, HelpContext help) {
        super(title, message, new String[0], help);
        addButton(OK_ID, disableOKShortcut);
        addButton(CANCEL_ID, false);
        setDefaultButton(CANCEL_ID);
    }

}
