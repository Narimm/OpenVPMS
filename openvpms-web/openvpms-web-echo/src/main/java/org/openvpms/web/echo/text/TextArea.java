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

import nextapp.echo2.app.Extent;
import nextapp.echo2.app.text.Document;
import nextapp.echo2.app.text.StringDocument;

/**
 * A multiple-line text input field.
 * <p/>
 * This replaces the echo2 implementation.
 *
 * @author Tim Anderson
 */
public class TextArea extends TextComponent {

    /**
     * Creates a new {@code TextArea} with an empty {@code StringDocument} as its model, and default width and
     * height settings.
     */
    public TextArea() {
        super(new StringDocument());
    }

    /**
     * Creates a new {@code TextArea} with the specified {@code Document} model.
     *
     * @param document the document
     */
    public TextArea(Document document) {
        super(document);
    }

    /**
     * Creates a new {@code TextArea} with the specified {@code Document} model, initial text, width
     * and height settings.
     *
     * @param document the document
     * @param text     the initial text (may be null)
     * @param columns  the number of columns to display
     * @param rows     the number of rows to display
     */
    public TextArea(Document document, String text, int columns, int rows) {
        super(document);
        if (text != null) {
            document.setText(text);
        }
        setWidth(new Extent(columns, Extent.EM));
        setHeight(new Extent(rows, Extent.EM));
    }

}
