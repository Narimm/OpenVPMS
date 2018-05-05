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


package org.openvpms.component.system.common.exception;

import org.openvpms.component.i18n.Message;
import org.openvpms.component.i18n.Messages;

/**
 * This is the base exception for all OpenVPMS exceptions.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 */
public abstract class OpenVPMSException extends org.openvpms.component.exception.OpenVPMSException {

    /**
     * The base name of the resource file, which holds the error messages.
     */
    public static final String ERRMESSAGES_FILE = "errmessages";

    /**
     * Default SUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * Delegate to the super class
     *
     * @param msg the message
     */
    public OpenVPMSException(String msg) {
        super(Messages.create(msg));
    }

    /**
     * Delegate to the super class
     *
     * @param msg       the message
     * @param exception the exception
     */
    public OpenVPMSException(String msg, Throwable exception) {
        super(Messages.create(msg), exception);
    }

    /**
     * Delegate to the super class
     *
     * @param exception the exception
     */
    public OpenVPMSException(Throwable exception) {
        super(Messages.create(exception.getMessage()), exception);
    }

    /**
     * Constructs a {@link OpenVPMSException}.
     *
     * @param message the exception message
     */
    public OpenVPMSException(Message message) {
        super(message);
    }

    /**
     * Constructs a {@link OpenVPMSException}.
     *
     * @param message the exception message
     * @param cause   the cause
     */
    public OpenVPMSException(Message message, Throwable cause) {
        super(message, cause);
    }
}
