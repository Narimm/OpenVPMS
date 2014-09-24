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

package org.openvpms.hl7.impl;

import org.openvpms.component.system.common.i18n.Message;
import org.openvpms.component.system.common.i18n.Messages;

/**
 * Enter description.
 *
 * @author Tim Anderson
 */
public class HL7Messages {

    /**
     * The messages.
     */
    private static Messages messages = new Messages("HL7", HL7Messages.class.getName());

    public static Message failedToEncodeMessage() {
        return messages.getMessage(1);
    }

    public static Message failedToParseMessage(String content) {
        return messages.getMessage(2);
    }

    public static Message failedToConnect(String host, int port) {
        return messages.getMessage(3);
    }

    public static Message failedToSend(String host, int port) {
        return messages.getMessage(4);
    }

}
