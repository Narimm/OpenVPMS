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

package org.openvpms.web.echo.error;

import nextapp.echo2.app.event.WindowPaneListener;


/**
 * Error handler.
 *
 * @author Tim Anderson
 */
public abstract class ErrorHandler {

    /**
     * The singleton instance.
     */
    private static ErrorHandler instance = LoggingErrorHandler.INSTANCE;

    /**
     * Handles an error.
     *
     * @param cause the cause of the error
     */
    public abstract void error(Throwable cause);

    /**
     * Handles an error.
     *
     * @param message the error message
     * @param cause   the cause. May be {@code null}
     */
    public void error(String message, Throwable cause) {
        error(null, message, cause, null);
    }

    /**
     * Handles an error.
     *
     * @param message  the error message
     * @param cause    the cause. May be {@code null}
     * @param listener the listener. May be {@code null}
     */
    public void error(String message, Throwable cause, WindowPaneListener listener) {
        error(null, message, cause, listener);
    }

    /**
     * Handles an error.
     *
     * @param title    the error title. May be {@code null}
     * @param message  the error message
     * @param cause    the cause. May be {@code null}
     * @param listener the listener. May be {@code null}
     */
    public abstract void error(String title, String message, Throwable cause, WindowPaneListener listener);

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

}
