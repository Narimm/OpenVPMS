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
 *
 *  $Id$
 */

package org.openvpms.web.component.dialog;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.openvpms.web.resource.util.Messages;


/**
 * Modal error dialog box.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ErrorDialog extends MessageDialog {

    /**
     * Construct a new <code>ErrorDialog</code>.
     *
     * @param message the message to display
     */
    public ErrorDialog(String message) {
        this(Messages.get("errordialog.title"), message);
    }

    /**
     * Construct a new <code>ErrorDialog</code>.
     *
     * @param exception the exception to display
     */
    public ErrorDialog(Throwable exception) {
        this(ExceptionUtils.getStackTrace(exception));
    }

    /**
     * Construct a new <code>ErrorDialog</code>.
     *
     * @param title   the dialog title
     * @param message the message to display
     */
    public ErrorDialog(String title, String message) {
        super(title, message, "ErrorDialog", OK);
        setDefaultButton(OK_ID);
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
     * @param title   the dialog title
     * @param message dialog message
     */
    public static void show(String title, String message) {
        ErrorDialog dialog = new ErrorDialog(title, message);
        dialog.show();
    }

}
