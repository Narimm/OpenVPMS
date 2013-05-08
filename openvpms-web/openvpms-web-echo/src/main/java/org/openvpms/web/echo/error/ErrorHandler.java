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
 *
 *  $Id: $
 */

package org.openvpms.web.echo.error;

import nextapp.echo2.app.event.WindowPaneListener;


/**
 * Error handler.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public abstract class ErrorHandler {

    /**
     * The singleton instance.
     */
    private static ErrorHandler instance = new DialogErrorHandler();

    /**
     * Registers an instance to handle errors.
     *
     * @param handler the handler
     */
    public static void setInstance(ErrorHandler handler) {
        if (handler == null) {
            throw new IllegalArgumentException("Argument 'handler' is null");
        }
        instance = handler;
    }

    /**
     * Returns the singleton instance.
     *
     * @return the singleton instance
     */
    public static ErrorHandler getInstance() {
        return instance;
    }


    /**
     * Handles an error.
     *
     * @param cause the cause of the error
     */
    public abstract void error(Throwable cause);

    /**
     * Handles an error.
     *
     * @param title    the error title. May be <tt>null</tt>
     * @param message  the error message
     * @param cause    the cause. May be <tt>null</tt>
     * @param listener the listener. May be <tt>null</tt>
     */
    public abstract void error(String title, String message, Throwable cause, WindowPaneListener listener);

}
