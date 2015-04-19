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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.etl.load;

import org.apache.commons.resources.Messages;
import org.openvpms.component.system.common.exception.OpenVPMSException;


/**
 * Exception class for exceptions raised by {@link Loader}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class LoaderException extends OpenVPMSException {

    /**
     * Default serialization identifier.
     */
    private static final long serialVersionUID = 1L;


    /**
     * An enumeration of error codes.
     */
    public enum ErrorCode {
        ArchetypeNotFound,
        IMObjectNotFound,
        InvalidMapping,
        InvalidNode,
        InvalidReference,
        NullReference,
        LookupNotFound,
        LookupSourceNodeNotFound,
        LookupRelationshipNotFound,
        LookupRelationshipTargetNotFound,
        MissingRowValue,
        RefResolvesMultipleObjects,
        ReferencedObjectNotMapped
    }

    /**
     * The error code.
     */
    private final LoaderException.ErrorCode errorCode;

    /**
     * The appropriate resource file is loaded cached into memory when this
     * class is loaded.
     */
    private static Messages MESSAGES
            = Messages.getMessages("org.openvpms.etl.load." // NON-NLS
            + OpenVPMSException.ERRMESSAGES_FILE);

    /**
     * Constructs a new <code>IMObjectBeanException</code>.
     *
     * @param errorCode the error code
     */
    public LoaderException(LoaderException.ErrorCode errorCode,
                           Object ... args) {
        super(LoaderException.MESSAGES.getMessage(errorCode.toString(), args));
        this.errorCode = errorCode;
    }

    /**
     * Returns the error code.
     *
     * @return the error code
     */
    public LoaderException.ErrorCode getErrorCode() {
        return errorCode;
    }

}
