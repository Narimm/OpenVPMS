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
 */

package org.openvpms.web.component.echo;

import echopointng.text.StringDocumentEx;
import nextapp.echo2.app.TextArea;
import nextapp.echo2.app.text.Document;


/**
 * Counted text area. This displays the available remaining characters.
 *
 * @author Tim Anderson
 */
public class CountedTextArea extends TextArea {

    /**
     * Default maximum length.
     */
    private static final int MAX_LENGTH = 255;


    /**
     * Constructs an {@code CountedTextArea} with an empty <tt>StringDocument</tt> as its model, and default width and
     * height settings.
     */
    public CountedTextArea() {
        this(new StringDocumentEx());
    }

    /**
     * Constructs an {@code CountedTextArea} with the specified <tt>Document</tt> model.
     *
     * @param document the document
     */
    public CountedTextArea(Document document) {
        super(document);
        setMaximumLength(MAX_LENGTH);
    }

    /**
     * Constructs an {@code CountedTextArea} with the specified initial columns and rows.
     *
     * @param columns the number of columns to display
     * @param rows    the number of rows to display
     */
    public CountedTextArea(int columns, int rows) {
        this(new StringDocumentEx(), null, columns, rows);
    }

    /**
     * Constructs an {@code CountedTextArea} with the specified <tt>Document</tt> model, initial text, column
     * and row settings.
     *
     * @param document the document
     * @param text     the initial text (may be null)
     * @param columns  the number of columns to display
     * @param rows     the number of rows to display
     */
    public CountedTextArea(Document document, String text, int columns, int rows) {
        super(document, text, columns, rows);
        setMaximumLength(MAX_LENGTH);
    }
}
