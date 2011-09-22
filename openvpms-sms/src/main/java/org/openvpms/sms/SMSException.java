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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package org.openvpms.sms;

import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.i18n.Message;


/**
 * SMS interface exception.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class SMSException extends OpenVPMSException {

    private final Message message;

    /**
     * Constructs an <tt>SMSException</tt>.
     *
     * @param message the message
     */
    public SMSException(Message message) {
        super(message.toString());
        this.message = message;
    }

    /**
     * Constructs an <tt>SMSException</tt>.
     *
     * @param message the message
     * @param cause   the root cause
     */
    public SMSException(Message message, Throwable cause) {
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
