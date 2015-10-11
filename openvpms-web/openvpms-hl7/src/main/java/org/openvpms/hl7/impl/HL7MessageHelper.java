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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.hl7.impl;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Structure;
import ca.uhn.hl7v2.model.v25.datatype.CE;
import ca.uhn.hl7v2.model.v25.datatype.CWE;
import ca.uhn.hl7v2.model.v25.datatype.CX;
import ca.uhn.hl7v2.model.v25.datatype.EI;
import ca.uhn.hl7v2.model.v25.datatype.MSG;
import ca.uhn.hl7v2.model.v25.segment.ERR;
import ca.uhn.hl7v2.model.v25.segment.MSA;
import ca.uhn.hl7v2.model.v25.segment.MSH;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
     * Returns the MSH (message header) segment from a message, if it has one.
     *
     * @param message the message
     * @return the segment, or {@code null} if none is found
     */
    public static MSH getMSH(Message message) {
        return get(message, "MSH");
    }

    /**
     * Returns the MSA (message acknowledgement) segment from a message, if it has one.
     *
     * @param message the message
     * @return the segment, or {@code null} if none is found
     */
    public static MSA getMSA(Message message) {
        return get(message, "MSA");
    }

    /**
     * Generates an error message from an acknowledgement.
     *
     * @param ack the acknowledgement
     * @return the error message
     */
    public static String getErrorMessage(Message ack) {
        MSA msa = getMSA(ack);
        List<ERR> errors = getAll(ack, "ERR");

        StringBuilder buffer = new StringBuilder();
        String text = msa.getTextMessage().getValue(); // deprecated in HL7 2.4
        if (!StringUtils.isEmpty(text)) {
            buffer.append(text);
        }

        for (ERR err : errors) {
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

        String condition = formatCE(msa.getErrorCondition());
        if (!StringUtils.isEmpty(condition)) {
            append(buffer, "Error Condition: ", condition);
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
     * Returns the named structure from a message.
     *
     * @param message the message
     * @param name    the structure name
     * @return the structure, or {@code null} if none exists
     */
    @SuppressWarnings("unchecked")
    public static <T extends Structure> T get(Message message, String name) {
        try {
            for (String n : message.getNames()) {
                if (name.equals(n)) {
                    return (T) message.get(name);
                }
            }
        } catch (HL7Exception exception) {
            log.error("Failed to access " + name, exception);
        }
        return null;
    }

    /**
     * Returns all instances of the named structure from a message.
     *
     * @param message the message
     * @param name    the structure name
     * @return all instances the structure
     */
    @SuppressWarnings("unchecked")
    public static <T extends Structure> List<T> getAll(Message message, String name) {
        List<T> result = Collections.emptyList();
        try {
            for (String n : message.getNames()) {
                if (name.equals(n)) {
                    Structure[] list = message.getAll(name);
                    if (list.length != 0) {
                        result = new ArrayList<T>();
                        for (Structure s : list) {
                            result.add((T) s);
                        }
                    }
                }
            }
        } catch (HL7Exception exception) {
            log.error("Failed to access " + name, exception);
        }
        return result;
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
     * Formats a message header for logging.
     *
     * @param header the header
     * @return the formatted header, or {@code null} if the header cannot be formatted
     * @throws HL7Exception if the message cannot be encoded
     */
    public static String toString(MSH header) throws HL7Exception {
        return header.encode().replaceAll("\r", "\n");
    }

    /**
     * Helper to parse an id from a coded element.
     *
     * @param value the value
     * @return the id, or {@code -1} if one doesn't exist or can't be parsed
     */
    public static long getId(CE value) {
        return getId(value.getIdentifier().getValue());
    }

    /**
     * Helper to parse an id from an extended composite id.
     *
     * @param value the value
     * @return the id, or {@code -1} if one doesn't exist or can't be parsed
     */
    public static long getId(CX value) {
        return getId(value.getIDNumber().getValue());
    }

    /**
     * Helper to parse an id from an entity identifier.
     *
     * @param value the value
     * @return the id, or {@code -1} if one doesn't exist or can't be parsed
     */
    public static long getId(EI value) {
        return getId(value.getEntityIdentifier().getValue());
    }

    /**
     * Helper to parse an id from a string.
     *
     * @param value the value to parse
     * @return the id, or {@code -1} if one doesn't exist or can't be parsed
     */
    public static long getId(String value) {
        long id = -1;
        if (!StringUtils.isEmpty(value)) {
            try {
                id = Long.valueOf(value);
            } catch (NumberFormatException ignore) {
                // do nothing
            }
        }
        return id;
    }

    /**
     * Formats a Coded with Exceptions message field.
     *
     * @param field the field to format
     * @return the formatted field, or {@code null} if there is nothing to format
     */
    private static String formatCWE(CWE field) {
        String result = formatIdText(field.getIdentifier().getValue(), field.getText().getValue());
        if (result != null) {
            String originalText = field.getOriginalText().getValue();
            if (!StringUtils.isEmpty(originalText)) {
                result += "\nOriginal Text: ";
                result += originalText;
            }
        }
        return result;
    }

    /**
     * Formats a Coded Element field.
     *
     * @param field the field to format
     * @return the formatted field, or {@code null} if there is nothing to format
     */
    private static String formatCE(CE field) {
        return formatIdText(field.getIdentifier().getValue(), field.getText().getValue());
    }

    /**
     * Formats an identifier and text.
     *
     * @param id   the identifier. May be {@code null}
     * @param text the text. May be {@code null}
     * @return the formatted text. May be {@code null}
     */
    private static String formatIdText(String id, String text) {
        String result = null;
        if (!StringUtils.isEmpty(id) || !StringUtils.isEmpty(text)) {
            if (!StringUtils.isEmpty(id) && !StringUtils.isEmpty(text)) {
                result = id + " - " + text;
            } else if (!StringUtils.isEmpty(id)) {
                result = id;
            } else {
                result = text;
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
