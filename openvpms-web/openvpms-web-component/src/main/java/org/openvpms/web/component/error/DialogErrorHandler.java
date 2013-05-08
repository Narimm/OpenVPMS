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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.error;

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Window;
import nextapp.echo2.app.event.WindowPaneListener;
import org.openvpms.web.echo.dialog.ErrorDialog;
import org.openvpms.web.echo.error.ErrorHandler;
import org.openvpms.web.echo.error.ErrorReportingDialog;


/**
 * Displays errors in a dialog.
 * <p/>
 * This implementation will only display a single error dialog at a time; if an error dialog is displayed,
 * no subsequent errors will be displayed.
 * <p/>
 * This is to avoid popping up multiple dialogs relating to the same error.
 *
 * @author Tim Anderson
 */
public class DialogErrorHandler extends ErrorHandler {

    /**
     * Handles an error.
     *
     * @param title    the error title. May be <tt>null</tt>
     * @param message  the error message
     * @param cause    the cause. May be <tt>null</tt>
     * @param listener the listener. May be <tt>null</tt>
     */
    public void error(String title, String message, Throwable cause, WindowPaneListener listener) {
        if (canDisplay() && !inError()) {
            ErrorDialog dialog;
            if (cause != null) {
                if (title != null) {
                    dialog = new ErrorReportingDialog(title, message, cause);
                } else {
                    dialog = new ErrorReportingDialog(message, cause);
                }
            } else {
                if (title != null) {
                    dialog = new ErrorDialog(title, message);
                } else {
                    dialog = new ErrorDialog(message);
                }
            }
            if (listener != null) {
                dialog.addWindowPaneListener(listener);
            }
            dialog.show();
        }
    }

    /**
     * Determines if the error can be displayed in the browser.
     *
     * @return <tt>true</tt> if the error can be displayed in the browser
     */
    private static boolean canDisplay() {
        ApplicationInstance instance = ApplicationInstance.getActive();
        return instance != null && instance.getDefaultWindow() != null;
    }

    /**
     * Determines if an error dialog is already being displayed.
     *
     * @return <tt>true</tt> if an error dialog is already being displayed
     */
    private static boolean inError() {
        Window root = ApplicationInstance.getActive().getDefaultWindow();
        for (Component component : root.getContent().getComponents()) {
            if (component instanceof ErrorDialog) {
                return true;
            }
        }
        return false;
    }
}
