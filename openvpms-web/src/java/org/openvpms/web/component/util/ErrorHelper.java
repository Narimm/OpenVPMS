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
 *  $Id: ErrorHelper.java 3317 2009-04-16 04:23:11Z tanderson $
 */

package org.openvpms.web.component.util;

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Window;
import nextapp.echo2.app.event.WindowPaneListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.service.archetype.ValidationError;
import org.openvpms.component.business.service.archetype.ValidationException;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.dialog.ErrorDialog;
import org.openvpms.web.component.error.ErrorReportingDialog;
import org.openvpms.web.resource.util.Messages;

import java.util.List;


/**
 * Helper for displaying and logging errors.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2009-04-16 14:23:11 +1000 (Thu, 16 Apr 2009) $
 */
public class ErrorHelper {

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(ErrorHelper.class);


    /**
     * Display and log an error. If an error dialog is already displayed,
     * this method will not pop up a new one, to avoid multiple dialogs
     * related to the same error.
     *
     * @param error the error
     */
    public static void show(String error) {
        show(error, true);
    }

    /**
     * Display and optionally log an error. If an error dialog is already
     * displayed, this method will not pop up a new one, to avoid
     * multiple dialogs related to the same error.
     *
     * @param error the error
     * @param log   if <tt>true</tt> log the error
     */
    public static void show(String error, boolean log) {
        if (log) {
            ErrorHelper.log.error(error);
        }
        if (!inError()) {
            ErrorDialog.show(error);
        }
    }

    /**
     * Display and log an error. If an error dialog is already displayed,
     * this method will not pop up a new one, to avoid multiple dialogs
     * related to the same error.
     *
     * @param title the title
     * @param error the error
     */
    public static void show(String title, String error) {
        log.error(error);
        if (!inError()) {
            ErrorDialog.show(title, error);
        }
    }

    /**
     * Display and log an error. If an error dialog is already displayed,
     * this method will not pop up a new one, to avoid multiple dialogs
     * related to the same error.
     *
     * @param title the title
     * @param error the error
     */
    public static void show(String title, Throwable error) {
        show(title, null, error);
    }

    /**
     * Display and log an error. If an error dialog is already displayed,
     * this method will not pop up a new one, to avoid multiple dialogs
     * related to the same error.
     *
     * @param title       the title
     * @param displayName the display name to include in the error message.
     *                    May be <tt>null</tt>
     * @param error       the error
     */
    public static void show(String title, String displayName, Throwable error) {
        show(title, displayName, null, error);
    }

    /**
     * Display and log an error. If an error dialog is already displayed,
     * this method will not pop up a new one, to avoid multiple dialogs
     * related to the same error.
     *
     * @param title       the title
     * @param displayName the display name to include in the error message.
     *                    May be <tt>null</tt>
     * @param context     a context message, for logging purposes.
     *                    May be <tt>null</tt>
     * @param error       the error
     */
    public static void show(String title, String displayName, String context,
                            Throwable error) {
        String message = getError(error, displayName);
        String logerror = message;
        if (context != null) {
            logerror = Messages.get("logging.error.messageandcontext", message,
                                    context);
        }
        log.error(logerror, error);
        if (!inError()) {
            ErrorDialog.show(title, message);
        }
    }

    /**
     * Display and log an error. If an error dialog is already displayed,
     * this method will not pop up a new one, to avoid multiple dialogs
     * related to the same error.
     *
     * @param error the error
     */
    public static void show(Throwable error) {
        String message = getError(error);
        log.error(message, error);
        if (!inError()) {
            ErrorReportingDialog.show(message, error);
        }
    }

    /**
     * Display and optionally log an error. If an error dialog is already
     * displayed, this method will not pop up a new one, to avoid multiple
     * dialogs related to the same error.
     *
     * @param error the error
     * @param log   if <tt>true</tt> log the error
     */
    public static void show(Throwable error, boolean log) {
        String message = getError(error);
        if (log) {
            ErrorHelper.log.error(message, error);
        }
        if (!inError()) {
            ErrorDialog.show(message);
        }
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
        ErrorDialog dialog = new ErrorReportingDialog(message, error);
        dialog.addWindowPaneListener(listener);
        dialog.show();
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
     * Returns the preferred exception message from an exception heirarchy.
     *
     * @param exception   the exception
     * @param displayName the display name to include in the message.
     *                    May be <tt>null</tt>
     * @return the exception message
     */
    public static String getError(Throwable exception, String displayName) {
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
            if (exception instanceof ValidationException) {
                result = getError((ValidationException) exception);
            } else {
                result = exception.getLocalizedMessage();
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
