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

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.service.archetype.ValidationError;
import org.openvpms.component.business.service.archetype.ValidationException;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.resource.i18n.Messages;

import java.util.List;

/**
 * Formats error messages.
 *
 * @author Tim Anderson
 */
public class ErrorFormatter {

    /**
     * Formats a validation error.
     *
     * @param error the validation error
     * @return the formatted validation error message
     */
    public static String format(ValidationError error) {
        String archetypeName = null;
        String nodeName = null;
        ArchetypeDescriptor archetype = DescriptorHelper.getArchetypeDescriptor(error.getArchetype());
        if (archetype != null) {
            archetypeName = archetype.getDisplayName();
            NodeDescriptor descriptor = archetype.getNodeDescriptor(error.getNode());
            if (descriptor != null) {
                nodeName = descriptor.getDisplayName();
            }
        }
        String key = error.getClass().getName() + ".formatted";
        return Messages.get(key, archetypeName, nodeName, error.getMessage());
    }

    /**
     * Returns the preferred exception message from an exception hierarchy.
     *
     * @param exception the exception
     * @return the exception message
     */
    public static String format(Throwable exception) {
        return format(exception, null);
    }

    /**
     * Returns the preferred exception message from an exception hierarchy.
     *
     * @param exception   the exception
     * @param displayName the display name to include in the message. May be {@code null}
     * @return the exception message
     */
    public static String format(Throwable exception, String displayName) {
        Throwable cause = ExceptionHelper.getRootCause(exception);
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
            return format(error);
        }
        return exception.getLocalizedMessage();
    }

}
