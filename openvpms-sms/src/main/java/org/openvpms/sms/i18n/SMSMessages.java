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

    /**
     * Creates a message for the situation where there is no <em>entity.SMSConfig*</em> associated with the practice.
     *
     * @param practice the practice
     * @return a new messsage
     */
    public static Message SMSNotConfigured(Party practice) {
        return messages.getMessage(100, practice.getName());
    }

    /**
     * Creates a message for failure to find the <em>party.organisationPractice</em>.
     *
     * @return a new message
     */
    public static Message practiceNotFound() {
        return messages.getMessage(101);
    }

    /**
     * Creates a message for the situation where an email cannot be created.
     *
     * @param reason the reason
     * @return a new message
     */
    public static Message failedToCreateEmail(String reason) {
        return messages.getMessage(200, reason);
    }

    /**
     * Creates a message for a mail server authentication failure.
     *
     * @param reason the reason
     * @return a new message
     */
    public static Message mailAuthenticationFailed(String reason) {
        return messages.getMessage(201, reason);
    }

    /**
     * Creates a message for a mail connection failure.
     *
     * @param reason the reason
     * @return a new message
     */
    public static Message mailConnectionFailed(String reason) {
        return messages.getMessage(202, reason);
    }

    /**
     * Creates a message for a mail send failure.
     *
     * @param reason the reason
     * @return a new message
     */
    public static Message mailSendFailed(String reason) {
        return messages.getMessage(203, reason);
    }

    /**
     * Creates a message for failure to evaluate an expression.
     *
     * @param expression the expression
     * @return a new message
     */
    public static Message failedToEvaluateExpression(String expression) {
        return messages.getMessage(300, expression);
    }

    /**
     * Creates a message for an invalid 'From' address.
     *
     * @param address the address
     * @return a new message
     */
    public static Message invalidFromAddress(String address) {
        return messages.getMessage(301, address);
    }

    /**
     * Creates a message for an invalid 'To' address.
     *
     * @param address the address
     * @return a new message
     */
    public static Message invalidToAddress(String address) {
        return messages.getMessage(302, address);
    }

    /**
     * Creates a message for an invalid 'Reply-To' address.
     *
     * @param address the address
     * @return a new message
     */
    public static Message invalidReplyToAddress(String address) {
        return messages.getMessage(303, address);
    }

    /**
     * Creates a message for no message text.
     *
     * @return a new message
     */
    public static Message noMessageText() {
        return messages.getMessage(304);
    }
}
