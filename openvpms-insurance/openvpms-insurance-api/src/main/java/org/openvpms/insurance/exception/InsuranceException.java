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

package org.openvpms.insurance.exception;

import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.component.i18n.Message;

/**
 * Insurance exception.
 *
 * @author Tim Anderson
 */
public class InsuranceException extends OpenVPMSException {

    /**
     * Constructs an {@link InsuranceException}.
     *
     * @param message the message
     */
    public InsuranceException(Message message) {
        super(message);
    }

    /**
     * Constructs an {@link InsuranceException}.
     *
     * @param message the message
     * @param cause   the root cause
     */
    public InsuranceException(Message message, Throwable cause) {
        super(message, cause);
    }
}
