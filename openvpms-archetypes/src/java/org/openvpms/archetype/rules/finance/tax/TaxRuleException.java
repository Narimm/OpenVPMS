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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.finance.tax;

import org.apache.commons.resources.Messages;
import org.openvpms.component.system.common.exception.OpenVPMSException;


/**
 * Exception class for exceptions raised by tax rules.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class TaxRuleException extends OpenVPMSException {

    /**
     * An enumeration of error codes
     */
    public enum ErrorCode {
        InvalidActForTax
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
            = Messages.getMessages(
            "org.openvpms.archetype.rules.finance.tax."
            + OpenVPMSException.ERRMESSAGES_FILE);

    /**
     * Constructs a new <code>TaxRuleException</code>.
     *
     * @param errorCode the error code
     */
    public TaxRuleException(ErrorCode errorCode, Object... args) {
        super(MESSAGES.getMessage(errorCode.toString(), args));
        _errorCode = errorCode;
    }

    /**
     * Returns the error code.
     *
     * @return the error code
     */
    public TaxRuleException.ErrorCode getErrorCode() {
        return _errorCode;
    }
}
