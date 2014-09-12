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

    public static Message invalidPatientIdentifier(String value) {
        return messages.getMessage(5);
    }
}
