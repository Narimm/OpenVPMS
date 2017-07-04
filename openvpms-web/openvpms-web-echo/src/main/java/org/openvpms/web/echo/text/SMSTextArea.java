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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.echo.text;

import echopointng.text.StringDocumentEx;
import nextapp.echo2.app.text.Document;


/**
 * SMS text area. This displays the available remaining characters.
 *
 * @author Tim Anderson
 */
public class SMSTextArea extends TextArea {

    /**
     * The maximum number of message parts.
     */
    public static final String PROPERTY_MAX_PARTS = "maxParts";

    /**
     * Constructs an {@code CountedTextArea} with an empty {@code StringDocument} as its model, and default width and
     * height settings.
     */
    public SMSTextArea() {
        this(new StringDocumentEx());
    }

    /**
     * Constructs an {@code CountedTextArea} with the specified {@code Document} model.
     *
     * @param document the document
     */
    public SMSTextArea(Document document) {
        super(document);
        setMaxParts(1);
    }

    /**
     * Constructs an {@code CountedTextArea} with the specified initial columns and rows.
     *
     * @param columns the number of columns to display
     * @param rows    the number of rows to display
     */
    public SMSTextArea(int columns, int rows) {
        this(new StringDocumentEx(), null, columns, rows);
    }

    /**
     * Constructs an {@code CountedTextArea} with the specified {@code Document} model, initial text, column
     * and row settings.
     *
     * @param document the document
     * @param text     the initial text (may be null)
     * @param columns  the number of columns to display
     * @param rows     the number of rows to display
     */
    public SMSTextArea(Document document, String text, int columns, int rows) {
        super(document, text, columns, rows);
        setMaxParts(1);
    }

    /**
     * Sets the maximum number of parts that may be sent in an SMS message.
     *
     * @param maxParts the maximum number of parts
     */
    public void setMaxParts(int maxParts) {
        setProperty(PROPERTY_MAX_PARTS, maxParts);
    }

    /**
     * Returns the maximum number of parts that may be sent in an SMS message.
     *
     * @return the maximum number of parts
     */
    public int getMaxParts() {
        Integer maxParts = (Integer) getProperty(PROPERTY_MAX_PARTS);
        return maxParts != null ? maxParts : 1;
    }

}
