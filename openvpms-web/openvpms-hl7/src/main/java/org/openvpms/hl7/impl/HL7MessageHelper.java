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

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v25.datatype.CWE;
import ca.uhn.hl7v2.model.v25.datatype.MSG;
import ca.uhn.hl7v2.model.v25.message.ACK;
import ca.uhn.hl7v2.model.v25.segment.ERR;
import ca.uhn.hl7v2.model.v25.segment.MSH;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * HL7 Message helper methods.
 *
 * @author Tim Anderson
 */
public class HL7MessageHelper {

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(HL7MessageHelper.class);

    /**
     * Returns a formatted name for a message.
     *
     * @param header the message header
     * @return the formatted type
     */
    public static String getMessageName(MSH header) {
        MSG type = header.getMessageType();
        return type.getMessageCode() + "^" + type.getTriggerEvent() + "^" + type.getMessageStructure();
    }

    /**
     * Generates an error message from an acknowledgement.
     *
     * @param ack the acknowledgement
     * @return the error message
     */
    public static String getErrorMessage(ACK ack) {
        StringBuilder buffer = new StringBuilder();
        String text = ack.getMSA().getTextMessage().getValue(); // deprecated in HL7 2.4
        if (!StringUtils.isEmpty(text)) {
            buffer.append(text);
        }
        try {
            for (ERR err : ack.getERRAll()) {
                String hl7ErrorCode = formatCWE(err.getHL7ErrorCode());
                if (hl7ErrorCode != null) {
                    append(buffer, "HL7 Error Code: ", hl7ErrorCode);
                }
                String errorCode = formatCWE(err.getApplicationErrorCode());
                if (!StringUtils.isEmpty(errorCode)) {
                    append(buffer, "Application Error Code: ", errorCode);
                }
                String diagnostic = err.getDiagnosticInformation().getValue();
                if (!StringUtils.isEmpty(diagnostic)) {
                    append(buffer, "Diagnostic Information: ", diagnostic);
                }
                String userMessage = err.getUserMessage().getValue();
                if (!StringUtils.isEmpty(userMessage)) {
                    append(buffer, "User Message: ", userMessage);
                }
            }
        } catch (HL7Exception exception) {
            log.error("Failed to access ERR segments", exception);
        }
        if (buffer.length() == 0) {
            buffer.append("Message body: ");
            try {
                buffer.append(toString(ack));
            } catch (HL7Exception exception) {
                buffer.append("unknown");
                log.error("Failed to encode message", exception);
            }
        }
        return buffer.toString();
    }

    /**
     * Formats a message for logging.
     *
     * @param message the message
     * @return the formatted message
     * @throws HL7Exception if the message cannot be encoded
     */
    public static String toString(Message message) throws HL7Exception {
        return message.encode().replaceAll("\r", "\n");
    }

    /**
     * Formats a Coded with Exceptions message field.
     *
     * @param field the field to format
     * @return the formatted field, or {@code null} if there is nothing to format
     */
    private static String formatCWE(CWE field) {
        String result = null;
        String id = field.getIdentifier().getValue();
        String text = field.getText().getValue();
        if (!StringUtils.isEmpty(id) || !StringUtils.isEmpty(text)) {
            if (!StringUtils.isEmpty(id) && !StringUtils.isEmpty(text)) {
                result = id + " - " + text;
            } else if (!StringUtils.isEmpty(id)) {
                result = id;
            } else {
                result = text;
            }

            String originalText = field.getOriginalText().getValue();
            if (!StringUtils.isEmpty(originalText)) {
                result += "\nOriginal Text: ";
                result += originalText;
            }
        }
        return result;
    }

    /**
     * Appends values to a buffer, prepended by a new-line if the buffer is not empty.
     *
     * @param buffer the buffer
     */
    private static void append(StringBuilder buffer, String... values) {
        if (buffer.length() != 0) {
            buffer.append("\n");
        }
        for (String value : values) {
            buffer.append(value);
        }
    }

}
