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
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.service.archetype.ValidationError;
import org.openvpms.component.business.service.archetype.ValidationException;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.help.HelpContext;
import org.openvpms.web.resource.util.Messages;

import java.util.List;


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
     * @param title       the title. Maty be {@code null}
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
        String message = getError(error, displayName);
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
        String message = getError(error);
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
        String message = getError(error);
        log.error(message, error);
        ErrorHandler.getInstance().error(null, message, error, listener);
    }

    /**
     * Helper to format a validation error.
     *
     * @param error the validation error
     * @return the formatted validation error message
     */
    public static String getError(ValidationError error) {
        String archetypeName = null;
        String nodeName = null;
        ArchetypeDescriptor archetype
            = DescriptorHelper.getArchetypeDescriptor(error.getArchetype());
        if (archetype != null) {
            archetypeName = archetype.getDisplayName();
            NodeDescriptor descriptor
                = archetype.getNodeDescriptor(error.getNode());
            if (descriptor != null) {
                nodeName = descriptor.getDisplayName();
            }
        }
        String key = error.getClass().getName() + ".formatted";
        return Messages.get(key, archetypeName, nodeName, error.getMessage());
    }

    /**
     * Returns the preferred exception message from an exception heirarchy.
     *
     * @param exception the exception
     * @return the exception message
     */
    public static String getError(Throwable exception) {
        return getError(exception, null);
    }

    /**
     * Returns the root cause of an exception.
     *
     * @param exception the exception
     * @return the root cause of the exception, or {@code exception} if it
     *         is the root
     */
    public static Throwable getRootCause(Throwable exception) {
        if (exception.getCause() != null) {
            return getRootCause(exception.getCause());
        }
        return exception;
    }

    /**
     * Returns the preferred exception message from an exception hierarchy.
     *
     * @param exception   the exception
     * @param displayName the display name to include in the message.
     *                    May be {@code null}
     * @return the exception message
     */
    public static String getError(Throwable exception, String displayName) {
        Throwable cause = getRootCause(exception);
        String result = getFormattedMessage(cause, displayName);
        if (result == null && exception != cause) {
            result = getFormattedMessage(exception, displayName);
        }
        if (result == null) {
            result = exception.getLocalizedMessage();
        }
        return result;
    }

    /**
     * Returns a formatted message for an exception, if one is configured.
     *
     * @param exception   the exception
     * @param displayName the display name
     * @return the formatted message, or {@code null} if none is available
     */
    private static String getFormattedMessage(Throwable exception, String displayName) {
        String result = null;
        if (displayName != null) {
            String key = exception.getClass().getName() + ".formatted";
            result = Messages.format(key, true, displayName);
        }
        if (result == null) {
            result = Messages.get(exception.getClass().getName(), true);
        }
        if (result == null) {
            if (exception instanceof ValidationException) {
                result = getError((ValidationException) exception);
            }
        }
        return result;
    }

    /**
     * Helper to extract the first error from a validation exception.
     *
     * @param exception the exception
     * @return the first error from the exception, or the localised message
     *         if there is no error
     */
    private static String getError(ValidationException exception) {
        List<ValidationError> errors = exception.getErrors();
        if (!errors.isEmpty()) {
            ValidationError error = errors.get(0);
            return getError(error);
        }
        return exception.getLocalizedMessage();
    }


}
