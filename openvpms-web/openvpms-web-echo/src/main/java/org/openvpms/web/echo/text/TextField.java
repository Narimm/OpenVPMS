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
 * A single-line text input field.
 * Workaround for a bug in the echo2 TextComponent javascript implementation.
 * <p/>
 * This should be used instead of the echo2 class.
 * <p/>
 * This exists to enable {@link TextFieldPeer} to be used to specify a corrected javascript file,
 * <em>org/openvpms/web/resource/js/TextComponent.js</em>.
 * The binding is specified in <em>META-INF\nextapp\echo2\SynchronizePeerBindings.properties</em>
 * <p/>
 * See http://jira.openvpms.org/jira/browse/OVPMS-1017 for more details.
 *
 * @author Tim Anderson
 */
public class TextField extends TextComponent {

    /**
     * Constructs a {@link TextField} with an empty {@code StringDocument} as its model, and default width
     * setting.
     */
    public TextField() {
        super(new StringDocument());
    }

    /**
     * Constructs a {@link TextField}  with the specified {@code Document} model.
     *
     * @param document the document
     */
    public TextField(Document document) {
        super(document);
    }

    /**
     * Constructs a {@link TextField}  with the specified {@code Document} model, initial text, and column width.
     *
     * @param document the document
     * @param text     the initial text (may be null)
     * @param columns  the number of columns to display
     */
    public TextField(Document document, String text, int columns) {
        super(document);
        if (text != null) {
            document.setText(text);
        }
        setWidth(new Extent(columns, Extent.EX));
    }

}
                                                           