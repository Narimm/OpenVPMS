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


package org.openvpms.component.business.service.archetype;

import org.apache.commons.resources.Messages;
import org.openvpms.component.system.common.exception.OpenVPMSException;

import java.util.ArrayList;
import java.util.List;

/**
 * This exception is raised when we are validating objects
 *
 * @author Jim Alateras
 */
public class ValidationException extends OpenVPMSException {

    /**
     * An enumeration of error codes
     */
    public enum ErrorCode {
        FailedToValidObjectAgainstArchetype
    }

    /**
     * Cache the werror code
     */
    private ErrorCode errorCode;

    /**
     * Holds a list of errors
     */
    private List<org.openvpms.component.service.archetype.ValidationError> errors = new ArrayList<>();

    /**
     * The appropriate resource file is loaded cached into memory when this
     * class is loaded.
     */
    private static Messages messages = Messages
            .getMessages("org.openvpms.component.business.service.archetype."
                         + OpenVPMSException.ERRMESSAGES_FILE);

    /**
     * Generated SUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiate an exception given an error code. The error code corresponds
     * to a message that does not require any parameters to redner
     *
     * @param errors    the list of validation errors
     * @param errorCode the error code
     */
    public ValidationException(List<org.openvpms.component.service.archetype.ValidationError> errors,
                               ErrorCode errorCode) {
        super(messages.getMessage(errorCode.toString()));
        this.errors = errors;
        this.errorCode = errorCode;
    }

    /**
     * Instantiate an exception given an error code and a set of associated
     * object parameters. The params are required to render the message
     *
     * @param errors    the list of validation errors
     * @param errorCode the error code
     * @param params    the parameters used to render the message associated with the error code
     */
    public ValidationException(List<org.openvpms.component.service.archetype.ValidationError> errors,
                               ErrorCode errorCode, Object[] params) {
        super(messages.getMessage(errorCode.toString(), params));
        this.errors = errors;
        this.errorCode = errorCode;
    }

    /**
     * Create an exception with the following error code and the root exception.
     * The error code is used to render a local specific message.
     *
     * @param errors    the list of validation errors
     * @param errorCode the error code
     * @param cause     the root exception
     */
    public ValidationException(List<org.openvpms.component.service.archetype.ValidationError> errors,
                               ErrorCode errorCode, Throwable cause) {
        super(messages.getMessage(errorCode.toString()), cause);
        this.errors = errors;
        this.errorCode = errorCode;
    }

    /**
     * Create an exception with the following error code and the root exception.
     * The params is used to render the messsgae that is associated with the
     * error code
     *
     * @param errors    the list of validation errors
     * @param errorCode the error code
     * @param params    additional information required to render the message
     * @param cause     the root exception
     */
    public ValidationException(List<org.openvpms.component.service.archetype.ValidationError> errors,
                               ErrorCode errorCode, Object[] params, Throwable cause) {
        super(messages.getMessage(errorCode.toString(), params), cause);
        this.errors = errors;
        this.errorCode = errorCode;
    }

    /**
     * @return Returns the errorCode.
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    /**
     * @return Returns the errors.
     */
    public List<org.openvpms.component.service.archetype.ValidationError> getErrors() {
        return errors;
    }

    /* (non-Javadoc)
     * @see java.lang.Throwable#toString()
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(getMessage());
        buf.append(" ")
                .append(getErrorCode())
                .append("\n");
        for (org.openvpms.component.service.archetype.ValidationError error : errors) {
            buf.append("\tnode:")
                    .append(error.getNode())
                    .append(" msg:")
                    .append(error.getMessage())
                    .append("\n");
        }

        return buf.toString();
    }

}
