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
    private static final Log _log = LogFactory.getLog(ErrorHelper.class);


    /**
     * Display and log an error.
     *
     * @param error the error
     */
    public static void show(String error) {
        _log.error(error);
        ErrorDialog.show(error);
    }

    /**
     * Display and log an error.
     *
     * @param title the title
     * @param error the error
     */
    public static void show(String title, String error) {
        _log.error(error);
        ErrorDialog.show(title, error);
    }

    /**
     * Display and log an error.
     *
     * @param title the title
     * @param error the error
     */
    public static void show(String title, Throwable error) {
        _log.error(error.getLocalizedMessage(), error);
        ErrorDialog.show(title, error.getLocalizedMessage());
    }

    /**
     * Display and log an error.
     *
     * @param error the error
     */
    public static void show(Throwable error) {
        _log.error(error.getLocalizedMessage(), error);
        ErrorDialog.show(error.getLocalizedMessage());
    }

    /**
     * Display and log an error, notifying when the user closes the dialog.
     *
     * @param error    the error
     * @param listener the listener to notify
     */
    public static void show(Throwable error, WindowPaneListener listener) {
        _log.error(error.getLocalizedMessage(), error);
        ErrorDialog dialog = new ErrorDialog(error.getLocalizedMessage());
        dialog.addWindowPaneListener(listener);
        dialog.show();
    }
}
