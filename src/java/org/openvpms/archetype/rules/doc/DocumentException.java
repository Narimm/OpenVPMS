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

package org.openvpms.archetype.rules.doc;

import org.apache.commons.resources.Messages;
import org.openvpms.component.system.common.exception.OpenVPMSException;


/**
 * Document exceptions.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DocumentException extends OpenVPMSException {

    /**
     * Serial version SUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * An enumeration of error codes.
     */
    public enum ErrorCode {
        UnsupportedDoc,
        ReadError,
        WriteError,
        NotFound,
        InvalidUnits,
        InvalidOrientation,
        InvalidMediaTray,
        InvalidPaperSize
    }

    /**
     * The error code.
     */
    private final ErrorCode errorCode;

    /**
     * The appropriate resource file is loaded cached into memory when this
     * class is loaded.
     */
    private static Messages MESSAGES = Messages.getMessages("org.openvpms.archetype.rules.doc."
                                                            + OpenVPMSException.ERRMESSAGES_FILE);

    /**
     * Constructs a {@code DocumentException}.
     *
     * @param errorCode the error code
     * @param args      message arguments
     */
    public DocumentException(ErrorCode errorCode, Object... args) {
        super(MESSAGES.getMessage(errorCode.toString(), args));
        this.errorCode = errorCode;
    }

    /**
     * Constructs a {@code DocumentException}.
     *
     * @param errorCode the error code
     * @param exception the cause
     * @param args      message arguments
     */
    public DocumentException(ErrorCode errorCode, Throwable exception, Object... args) {
        super(MESSAGES.getMessage(errorCode.toString(), args), exception);
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
