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

package org.openvpms.web.component.util;

import nextapp.echo2.app.event.WindowPaneListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.web.component.error.ErrorFormatter;
import org.openvpms.web.echo.error.ErrorHandler;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;


/**
 * Helper for displaying and logging errors.
 *
 * @author Tim Anderson
 */
public class ErrorHelper {

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(ErrorHelper.class);


    /**
     * Display an error.
     *
     * @param error the error
     */
    public static void show(String error) {
        show(null, error);
    }

    /**
     * Display an error.
     *
     * @param title the title. May be {@code null}
     * @param error the error
     */
    public static void show(String title, String error) {
        log.error(error);
        ErrorHandler.getInstance().error(title, error, null, null);
    }

    /**
     * Display an error.
     *
     * @param title the title. May be {@code null}
     * @param error the error
     */
    public static void show(String title, Throwable error) {
        show(title, null, error);
    }

    /**
     * Display an error.
     *
     * @param title       the title. May be {@code null}
     * @param displayName the display name to include in the error message. May be {@code null}
     * @param error       the error
     */
    public static void show(String title, String displayName, Throwable error) {
        show(title, displayName, null, error);
    }

    /**
     * Display an error.
     *
     * @param title       the title. May be {@code null}
     * @param displayName the display name to include in the error message. May be {@code null}
     * @param context     a context message, for logging purposes. May be {@code null}
     * @param error       the error
     */
    public static void show(String title, String displayName, String context, Throwable error) {
        String message = ErrorFormatter.format(error, displayName);
        String logerror = message;
        if (context != null) {
            logerror = Messages.get("logging.error.messageandcontext", message, context);
        }
        log.error(logerror, error);
        ErrorHandler.getInstance().error(title, message, error, null);
    }

    /**
     * Display and log an error. If an error dialog is already displayed,
     * this method will not pop up a new one, to avoid multiple dialogs
     * related to the same error.
     *
     * @param error the error
     */
    public static void show(Throwable error) {
        show(error, true);
    }

    /**
     * Display and log an error. If an error dialog is already displayed,
     * this method will not pop up a new one, to avoid multiple dialogs
     * related to the same error.
     *
     * @param error the error
     * @param help  the help context, used to provide context information for the error. May be {@code null}
     */
    public static void show(Throwable error, HelpContext help) {
        show(error, true, help);
    }

    /**
     * Display and optionally log an error. If an error dialog is already
     * displayed, this method will not pop up a new one, to avoid multiple
     * dialogs related to the same error.
     *
     * @param error the error
     * @param log   if {@code true} log the error
     */
    public static void show(Throwable error, boolean log) {
        show(error, log, null);
    }

    /**
     * Display and optionally log an error. If an error dialog is already
     * displayed, this method will not pop up a new one, to avoid multiple
     * dialogs related to the same error.
     *
     * @param error the error
     * @param log   if {@code true} log the error
     * @param help  the help context, used to provide context information for the error. May be {@code null}
     */
    public static void show(Throwable error, boolean log, HelpContext help) {
        String message = ErrorFormatter.format(error);
        if (log) {
            ErrorHelper.log.error(message, error);
            if (help != null) {
                ErrorHelper.log.error("Called from: " + help);
            }
        }
        ErrorHandler.getInstance().error(null, message, error, null);
    }

    /**
     * Display and log an error, notifying when the user closes the dialog.
     *
     * @param error    the error
     * @param listener the listener to notify
     */
    public static void show(Throwable error, WindowPaneListener listener) {
        String message = ErrorFormatter.format(error);
        log.error(message, error);
        ErrorHandler.getInstance().error(null, message, error, listener);
    }

}
