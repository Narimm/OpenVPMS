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
 *  $Id: QueryBuilderException.java 2183 2007-07-10 05:32:07Z tanderson $
 */


package org.openvpms.component.business.dao.hibernate.im.query;

// commons-resources

import org.apache.commons.resources.Messages;
import org.openvpms.component.system.common.exception.OpenVPMSException;

/**
 * This exception is thrown by the query builders.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2007-07-10 15:32:07 +1000 (Tue, 10 Jul 2007) $
 */
public class QueryBuilderException extends OpenVPMSException {

    /**
     * Default SUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * An enumeration of error codes.
     */
    public enum ErrorCode {
        NullQuery,
        NoShortNamesSpecified,
        MustSpecifyNodeName,
        InvalidQualifiedName,
        NoNodeDescriptorForName,
        OperatorNotSupported,
        CanOnlySortOnTopLevelNodes,
        CannotQueryAcrossTypes,
        NoNodeDescWithName,
        NodeDescriptorsDoNotMatch,
        ConstraintTypeNotSupported,
        NoArchetypesForId,
        InvalidObjectReferenceConstraint,
        NoMatchingArchetypesForId,
        NoMatchingArchetypesForShortName,
        NoMatchingArchetypesForLongName,
        NoArchetypeRangeAssertion,
        DuplicateAlias,
        CannotJoinDuplicateAlias
    }

    /**
     * Cache the werror code.
     */
    private ErrorCode errorCode;

    /**
     * The appropriate resource file is loaded cached into memory when this
     * class is loaded.
     */
    private static Messages messages = Messages
            .getMessages(
                    "org.openvpms.component.business.dao.hibernate.im.query."
                            + OpenVPMSException.ERRMESSAGES_FILE);


    /**
     * Instantiate an exception given an error code. The error code corresponds
     * to a message that does not require any parameters to redner
     *
     * @param errorCode the error code
     */
    public QueryBuilderException(ErrorCode errorCode) {
        super(messages.getMessage(errorCode.toString()));
        this.errorCode = errorCode;
    }

    /**
     * Instantiate an exception given an error code and a set of associated
     * object parameters. The params are required to render the message
     *
     * @param errorCode the error code
     * @param params    the parameters used to render the message associated with the
     *                  error code
     */
    public QueryBuilderException(ErrorCode errorCode, Object ... params) {
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
    public QueryBuilderException(ErrorCode errorCode, Throwable cause) {
        super(messages.getMessage(errorCode.toString()), cause);
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
