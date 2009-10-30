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
 *  Copyright 2009 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.component.business.service.lookup;

import org.apache.commons.resources.Messages;
import org.openvpms.component.system.common.exception.OpenVPMSException;


/**
 * Exceptions raised by the {@link ILookupService}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class LookupServiceException extends OpenVPMSException {

    /**
     * Serial version identifier.
     */
    private static final long serialVersionUID = 1L;

    /**
     * An enumeration of error codes.
     */
    public enum ErrorCode {
        LookupServiceNotSet
    }

    /**
     * The error code.
     */
    private ErrorCode errorCode;

    /**
     * The errormessages.
     */
    private static Messages messages
            = Messages.getMessages("org.openvpms.component.business.service.lookup."
                                   + OpenVPMSException.ERRMESSAGES_FILE);

    /**
     * Constructs a <tt>LookupServiceException</tt>.
     *
     * @param errorCode the error code
     */
    public LookupServiceException(ErrorCode errorCode) {
        super(messages.getMessage(errorCode.toString()));
        this.errorCode = errorCode;
    }

    /**
     * Constructs a <tt>LookupServiceException</tt>.
     *
     * @param errorCode the error code
     * @param params    the parameters used to render the message associated with the error code
     */
    public LookupServiceException(ErrorCode errorCode, Object... params) {
        super(messages.getMessage(errorCode.toString(), params));
        this.errorCode = errorCode;
    }

    /**
     * Constructs a <tt>LookupServiceException</tt>.
     *
     * @param errorCode the error code
     * @param cause     the root exception
     */
    public LookupServiceException(ErrorCode errorCode, Throwable cause) {
        super(messages.getMessage(errorCode.toString()), cause);
        this.errorCode = errorCode;
    }

    /**
     * Constructs a <tt>LookupServiceException</tt>.
     *
     * @param errorCode the error code
     * @param cause     the root exception
     * @param params    additional information required to render the message
     */
    public LookupServiceException(ErrorCode errorCode, Throwable cause, Object... params) {
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
