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

package org.openvpms.web.component.im.util;

import nextapp.echo2.app.event.WindowPaneListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.web.component.dialog.ErrorDialog;
import org.openvpms.web.resource.util.Messages;


/**
 * Helper for displaying and logging errors.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ErrorHelper {

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(ErrorHelper.class);


    /**
     * Display and log an error.
     *
     * @param error the error
     */
    public static void show(String error) {
        log.error(error);
        ErrorDialog.show(error);
    }

    /**
     * Display and log an error.
     *
     * @param title the title
     * @param error the error
     */
    public static void show(String title, String error) {
        log.error(error);
        ErrorDialog.show(title, error);
    }

    /**
     * Display and log an error.
     *
     * @param title the title
     * @param error the error
     */
    public static void show(String title, Throwable error) {
        show(title, null, error);
    }

    /**
     * Display and log an error.
     *
     * @param title       the title
     * @param displayName the display name to include in the error message.
     *                    May be <tt>null</tt>
     * @param error       the error
     */
    public static void show(String title, String displayName, Throwable error) {
        String message = getError(error, displayName);
        log.error(message, error);
        ErrorDialog.show(title, message);
    }

    /**
     * Display and log an error.
     *
     * @param error the error
     */
    public static void show(Throwable error) {
        String message = getError(error);
        log.error(message, error);
        ErrorDialog.show(message);
    }

    /**
     * Display and log an error, notifying when the user closes the dialog.
     *
     * @param error    the error
     * @param listener the listener to notify
     */
    public static void show(Throwable error, WindowPaneListener listener) {
        String message = getError(error);
        log.error(message, error);
        ErrorDialog dialog = new ErrorDialog(message);
        dialog.addWindowPaneListener(listener);
        dialog.show();
    }

    /**
     * Returns the preferred exception message from an exception heirarchy.
     *
     * @param exception the exception
     * @return the exception message
     */
    private static String getError(Throwable exception) {
        return getError(exception, null);
    }

    /**
     * Returns the preferred exception message from an exception heirarchy.
     *
     * @param exception   the exception
     * @param displayName the display name to include in the message.
     *                    May be <tt>null</tt>
     * @return the exception message
     */
    private static String getError(Throwable exception, String displayName) {
        exception = getRootCause(exception);
        String result = null;
        if (displayName != null) {
            String key = exception.getClass().getName() + ".formatted";
            result = Messages.get(key, true, displayName);
        }
        if (result == null) {
            result = Messages.get(exception.getClass().getName(), true);
        }
        if (result == null) {
            result = exception.getLocalizedMessage();
        }
        return result;
    }

    /**
     * Returns the root cause of an exception.
     *
     * @param exception the exception
     * @return the root cause of the exception, or <tt>exception</tt> if it
     *         is the root
     */
    private static Throwable getRootCause(Throwable exception) {
        if (exception.getCause() != null) {
            return getRootCause(exception.getCause());
        }
        return exception;
    }

}
