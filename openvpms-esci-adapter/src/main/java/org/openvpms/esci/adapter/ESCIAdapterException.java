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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.esci.adapter;

import org.apache.commons.resources.Messages;
import org.openvpms.component.system.common.exception.OpenVPMSException;


/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ESCIAdapterException extends OpenVPMSException {

    /**
     * An enumeration of error codes.
     */
    public enum ErrorCode {

        ESCINotConfigured,
        InvalidServiceURL
    }

    /**
     * The error code.
     */
    private final ErrorCode errorCode;

    /**
     * The appropriate resource file is loaded cached into memory when this
     * class is loaded.
     */
    private static Messages MESSAGES = Messages.getMessages("org.openvpms.esci.adapter."
                                                            + OpenVPMSException.ERRMESSAGES_FILE);

    /**
     * Constructs an <tt>ESCIAdapterException</tt>.
     *
     * @param errorCode the error code
     * @param args      arguments to format the message with
     */
    public ESCIAdapterException(ErrorCode errorCode, Object... args) {
        super(MESSAGES.getMessage(errorCode.toString(), args));
        this.errorCode = errorCode;
    }

    public ESCIAdapterException(ErrorCode errorCode, Throwable cause, Object... args) {
        super(MESSAGES.getMessage(errorCode.toString(), args), cause);
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
