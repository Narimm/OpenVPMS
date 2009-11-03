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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.dao.im.common;

import org.apache.commons.resources.Messages;
import org.openvpms.component.system.common.exception.OpenVPMSException;

/**
 * The exception and error codes for {@link IMObjectDAO}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class IMObjectDAOException extends OpenVPMSException {

    /**
     * The serialization version identifier.
     */
    private static final long serialVersionUID = 1;

    /**
     * An enumeration of error codes.
     */
    public enum ErrorCode {
        FailedToSaveIMObject,
        FailedToSaveCollectionOfObjects,
        FailedToDeleteIMObject,
        CannotDeleteInUseLookup,
        FailedToFindIMObject,
        FailedToFindIMObjects,
        ClassNameMustBeSpecified,
        FailedToExecuteNamedQuery,
        ObjectNotFound,
        InvalidQueryString,
        FailedToExecuteQuery,
        CannotCollectObject
    }

    /**
     * The error code.
     */
    private ErrorCode errorCode;

    /**
     * The appropriate resource file is loaded cached into memory when this
     * class is loaded.
     */
    private static Messages messages = Messages
            .getMessages("org.openvpms.component.business.dao.im.common."
                    + OpenVPMSException.ERRMESSAGES_FILE);

    /**
     * Instantiate an exception given an error code. The error code corresponds
     * to a message that does not require any parameters to render
     *
     * @param errorCode the error code
     */
    public IMObjectDAOException(ErrorCode errorCode) {
        super(messages.getMessage(errorCode.toString()));
        this.errorCode = errorCode;
    }

    /**
     * Instantiate an exception given an error code and a set of associated
     * object parameters. The params are required to render the message
     *
     * @param errorCode the error code
     * @param params    the parameters used to render the message associated
     *                  with the error code
     */
    public IMObjectDAOException(ErrorCode errorCode, Object ... params) {
        super(messages.getMessage(errorCode.toString(), params));
        this.errorCode = errorCode;
    }

    /**
     * Create an exception with the following error code and the root exception.
     * The error code is used to render a local specific message.
     *
     * @param errorCode the error code
     * @param cause     the root exception
     */
    public IMObjectDAOException(ErrorCode errorCode, Throwable cause) {
        super(messages.getMessage(errorCode.toString()), cause);
        this.errorCode = errorCode;
    }

    /**
     * Create an exception with the following error code and the root exception.
     * The params is used to render the messsgae that is associated with the
     * error code
     *
     * @param errorCode the error code
     * @param cause     the root exception
     * @param params    additional information required to render the message
     */
    public IMObjectDAOException(ErrorCode errorCode, Throwable cause,
                                Object... params) {
        super(messages.getMessage(errorCode.toString(), params), cause);
        this.errorCode = errorCode;
    }

    /**
     * Returns the error code.
     *
     * @return the error code
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
