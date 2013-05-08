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

import org.apache.commons.lang.exception.ExceptionUtils;
import org.openvpms.web.component.help.HelpContext;
import org.openvpms.web.echo.i18n.Messages;


/**
 * Modal error dialog box.
 *
 * @author Tim Anderson
 */
public class ErrorDialog extends MessageDialog {

    /**
     * Constructs an {@code ErrorDialog}.
     *
     * @param message the message to display
     */
    public ErrorDialog(String message) {
        this(Messages.get("errordialog.title"), message);
    }

    /**
     * Constructs an {@code ErrorDialog}.
     *
     * @param message the message to display
     * @param help    the help context
     */
    public ErrorDialog(String message, HelpContext help) {
        this(Messages.get("errordialog.title"), message, help);
    }

    /**
     * Constructs an {@code ErrorDialog}.
     *
     * @param exception the exception to display
     */
    public ErrorDialog(Throwable exception) {
        this(ExceptionUtils.getStackTrace(exception));
    }

    /**
     * Constructs an {@code ErrorDialog}.
     *
     * @param title   the dialog title
     * @param message the message to display
     */
    public ErrorDialog(String title, String message) {
        this(title, message, (HelpContext) null);
    }

    /**
     * Constructs an {@code ErrorDialog}.
     *
     * @param title   the dialog title
     * @param message the message to display
     * @param help    the help context. May be {@code null}
     */
    public ErrorDialog(String title, String message, HelpContext help) {
        super(title, message, "ErrorDialog", OK, help);
        setDefaultButton(OK_ID);
    }

    /**
     * Constructs an {@code ErrorDialog}.
     *
     * @param title   the dialog title
     * @param message the message to display
     * @param buttons the buttons to display
     */
    public ErrorDialog(String title, String message, String[] buttons) {
        super(title, message, "ErrorDialog", buttons);
    }

    /**
     * Constructs an {@code ErrorDialog}.
     *
     * @param title   the dialog title
     * @param message the message to display
     * @param buttons the buttons to display
     * @param help    the help context
     */
    public ErrorDialog(String title, String message, String[] buttons, HelpContext help) {
        super(title, message, "ErrorDialog", buttons, help);
    }

    /**
     * Helper to show a new error dialog.
     *
     * @param exception the exception to display
     */
    public static void show(Throwable exception) {
        ErrorDialog dialog = new ErrorDialog(exception);
        dialog.show();
    }

    /**
     * Helper to show a new error dialog.
     *
     * @param message dialog message
     */
    public static void show(String message) {
        ErrorDialog dialog = new ErrorDialog(message);
        dialog.show();
    }

    /**
     * Helper to show a new error dialog.
     *
     * @param message dialog message
     * @param help    the help context
     */
    public static void show(String message, HelpContext help) {
        ErrorDialog dialog = new ErrorDialog(message, help);
        dialog.show();
    }

    /**
     * Helper to show a new error dialog.
     *
     * @param title   the dialog title
     * @param message dialog message
     */
    public static void show(String title, String message) {
        ErrorDialog dialog = new ErrorDialog(title, message);
        dialog.show();
    }

}
