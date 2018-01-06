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

package org.openvpms.component.exception;

import org.openvpms.component.i18n.Message;

/**
 * This is the base exception for all OpenVPMS exceptions.
 *
 * @author Tim Anderson
 */
public abstract class OpenVPMSException extends RuntimeException {

    /**
     * The exception message.
     */
    private final Message message;

    /**
     * Constructs a {@link OpenVPMSException}.
     *
     * @param message the exception message
     */
    public OpenVPMSException(Message message) {
        super(message.toString());
        this.message = message;
    }

    /**
     * Constructs a {@link OpenVPMSException}.
     *
     * @param message the exception message
     * @param cause   the cause
     */
    public OpenVPMSException(Message message, Throwable cause) {
        super(message.toString(), cause);
        this.message = message;
    }

    /**
     * Returns the internalisation message.
     *
     * @return the message
     */
    public Message getI18nMessage() {
        return message;
    }
}
