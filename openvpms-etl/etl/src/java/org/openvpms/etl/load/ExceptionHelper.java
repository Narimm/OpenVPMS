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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.etl.load;

import org.apache.commons.resources.Messages;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.ValidationError;
import org.openvpms.component.business.service.archetype.ValidationException;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;

import java.util.List;


/**
 * Exception helper.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ExceptionHelper {

    /**
     * Logging messages.
     */
    private static final Messages messages
            = Messages.getMessages("org.openvpms.etl.load.messages"); // NON-NLS

    /**
     * The archetype service.
     */
    private final IArchetypeService service;


    /**
     * Creates a new <tt>ExceptionHelper</tt>.
     *
     * @param service the archetype service
     */
    public ExceptionHelper(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Returns the message from the root cause of an exception.
     *
     * @param exception the exception
     * @return the root cause message of the exception
     */
    public String getMessage(Throwable exception) {
        Throwable cause = getRootCause(exception);
        String message;
        if (cause instanceof ValidationException) {
            message = getMessage((ValidationException) cause);
        } else {
            message = cause.getLocalizedMessage();
        }
        if (message == null) {
            message = cause.toString();
        }
        return message;
    }

    /**
     * Returns the root cause of an exception.
     *
     * @param exception the exception
     * @return the root cause of the exception, or <tt>exception</tt> if it
     *         is the root
     */
    public Throwable getRootCause(Throwable exception) {
        if (exception.getCause() != null) {
            return getRootCause(exception.getCause());
        }
        return exception;
    }

    /**
     * Returns a formatted message for a validation excecption.
     *
     * @param exception the validation exception
     * @return a formatted message for the exception
     */
    private String getMessage(ValidationException exception) {
        List<ValidationError> errors = exception.getErrors();
        if (!errors.isEmpty()) {
            ValidationError error = errors.get(0);
            return getError(error);
        }
        return exception.getLocalizedMessage();
    }

    /**
     * Helper to format a validation error.
     *
     * @param error the validation error
     * @return the formatted validation error message
     */
    private String getError(ValidationError error) {
        String archetypeName = null;
        String nodeName = null;
        ArchetypeDescriptor archetype
                = DescriptorHelper.getArchetypeDescriptor(error.getArchetype(),
                                                          service);
        if (archetype != null) {
            archetypeName = archetype.getDisplayName();
            NodeDescriptor descriptor
                    = archetype.getNodeDescriptor(error.getNode());
            if (descriptor != null) {
                nodeName = descriptor.getDisplayName();
            }
        }
        return messages.getMessage(
                "ValidationError",
                new Object[]{archetypeName, nodeName, error.getMessage()});
    }

}