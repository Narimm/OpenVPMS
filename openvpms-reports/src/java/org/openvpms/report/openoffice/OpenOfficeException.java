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

package org.openvpms.report.openoffice;

import org.apache.commons.resources.Messages;
import org.openvpms.component.system.common.exception.OpenVPMSException;


/**
 * Exception class for exceptions raised by OpenOffice services.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class OpenOfficeException extends OpenVPMSException {

    /**
     * Default SUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * An enumeration of error codes
     */
    public enum ErrorCode {
        InvalidURL,
        FailedToConnect,
        FailedToStartService,
        FailedToGetService,
        ServiceNotInit,
        FailedToCreateDoc,
        FailedToPrint,
        FailedToGetField,
        FailedToSetField,
        FailedToExportDoc,
        FailedToGetUserFields,
        FailedToGetInputFields
    }

    /**
     * The error code.
     */
    private final ErrorCode _errorCode;

    /**
     * The appropriate resource file is loaded cached into memory when this
     * class is loaded.
     */
    private static Messages MESSAGES
            = Messages.getMessages("org.openvpms.report.openoffice."
            + OpenVPMSException.ERRMESSAGES_FILE);


    /**
     * Constructs a new <code>OpenOfficeException</code>.
     *
     * @param errorCode the error code
     * @param args      a list of arguments to format the error message with
     */
    public OpenOfficeException(ErrorCode errorCode, Object ... args) {
        super(MESSAGES.getMessage(errorCode.toString(), args));
        _errorCode = errorCode;
    }

    /**
     * Constructs a new <code>OpenOfficeException</code>.
     *
     * @param cause     the cause of the exception
     * @param errorCode the error code
     * @param args      a list of arguments to format the error message with
     */
    public OpenOfficeException(Throwable cause, ErrorCode errorCode,
                               Object ... args) {
        super(MESSAGES.getMessage(errorCode.toString(), args), cause);
        _errorCode = errorCode;
    }

    /**
     * Returns the error code.
     *
     * @return the error code
     */
    public ErrorCode getErrorCode() {
        return _errorCode;
    }

}
