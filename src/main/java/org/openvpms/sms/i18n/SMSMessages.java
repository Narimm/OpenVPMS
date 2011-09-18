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

package org.openvpms.sms.i18n;

import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.i18n.Message;
import org.openvpms.component.system.common.i18n.Messages;

/**
 * Messages reported by the SMS interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class SMSMessages {

    /**
     * The messages.
     */
    private static Messages messages = new Messages("SMS", SMSMessages.class.getName());

    public static Message SMSNotConfigured(Party practice) {
        return messages.getMessage(1, practice.getName());
    }

    public static Message failedToCreateEmail(String reason) {
        return messages.getMessage(2, reason);
    }

    public static Message practiceNotFound() {
        return messages.getMessage(3);
    }

    public static Message failedToEvaluateExpression(String expression) {
        return messages.getMessage(4, expression);
    }

    public static Message noEmailAddress(Party practice) {
        return messages.getMessage(5, practice.getName());
    }
}
