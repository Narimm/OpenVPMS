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

package org.openvpms.web.component.error;

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Window;
import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.web.echo.dialog.ErrorDialog;
import org.openvpms.web.echo.error.ErrorHandler;


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
     * The logger.
     */
    private static final Log log = LogFactory.getLog(DialogErrorHandler.class);

    /**
     * Handles an error.
     *
     * @param cause the cause of the error
     */
    @Override
    public void error(Throwable cause) {
        Throwable rootCause = ExceptionHelper.getRootCause(cause);
        String message = ErrorFormatter.format(rootCause);
        error(null, message, cause, null);
    }

    /**
     * Handles an error.
     *
     * @param title    the error title. May be {@code null}
     * @param message  the error message
     * @param cause    the cause. May be {@code null}
     * @param listener the listener. May be {@code null}
     */
    public void error(String title, String message, Throwable cause, WindowPaneListener listener) {
        log.error(message, cause);
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
        } else if (listener != null) {
            // notify of immediate closure
            listener.windowPaneClosing(new WindowPaneEvent(this));
        }
    }

    /**
     * Determines if the error can be displayed in the browser.
     *
     * @return {@code true} if the error can be displayed in the browser
     */
    private static boolean canDisplay() {
        ApplicationInstance instance = ApplicationInstance.getActive();
        return instance != null && instance.getDefaultWindow() != null;
    }

    /**
     * Determines if an error dialog is already being displayed.
     *
     * @return {@code true} if an error dialog is already being displayed
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
