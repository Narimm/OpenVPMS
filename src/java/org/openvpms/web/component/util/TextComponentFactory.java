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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.util;

import echopointng.RichTextArea;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.PasswordField;
import nextapp.echo2.app.TextArea;
import nextapp.echo2.app.TextField;
import nextapp.echo2.app.text.TextComponent;
import org.openvpms.web.component.bound.BoundFormattedField;
import org.openvpms.web.component.bound.BoundRichTextArea;
import org.openvpms.web.component.bound.BoundTextArea;
import org.openvpms.web.component.bound.BoundTextField;
import org.openvpms.web.component.property.Property;

import java.text.Format;


/**
 * Factory for {@link TextComponent}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class TextComponentFactory extends ComponentFactory {

    /**
     * Create a new text field.
     *
     * @return a new text field
     */
    public static TextField create() {
        TextField text = new TextField(new TextDocument());
        setDefaultStyle(text);
        return text;
    }

    /**
     * Create a new text field.
     *
     * @param columns the no. of columns to display
     * @return a new text field
     */
    public static TextField create(int columns) {
        TextField text = create();
        if (columns <= 10) {
            text.setWidth(new Extent(columns, Extent.EM));
        } else {
            text.setWidth(new Extent(columns, Extent.EX));
        }
        setDefaultStyle(text);
        return text;
    }

    /**
     * Create a new bound text field.
     *
     * @param property the property to bind
     * @param columns  the no. of columns to display
     * @return a new bound text field
     */
    public static TextField create(Property property, int columns) {
        TextField text = new BoundTextField(property, columns);
        setDefaultStyle(text);
        return text;
    }

    /**
     * Create a new bound formatted text field.
     *
     * @param property the property to bind
     * @param columns  the no. of columns to display
     * @param format   the field format
     * @return a new bound text field
     */
    public static TextField create(Property property, int columns,
                                   Format format) {
        TextField text = new BoundFormattedField(property, columns, format);
        setDefaultStyle(text);
        return text;
    }

    /**
     * Create a new text area.
     *
     * @return a new text area
     */
    public static TextArea createTextArea() {
        TextArea text = new TextArea(new TextDocument());
        setDefaultStyle(text);
        return text;
    }

    /**
     * Create a new bound text area.
     *
     * @param property the property to bind
     * @param columns  the no. of columns to display
     * @param rows     TODO
     * @return a new bound text field
     */
    public static TextArea createTextArea(Property property, int columns,
                                          int rows) {
        TextArea text = new BoundTextArea(property, columns, rows);
        setDefaultStyle(text);
        return text;
    }

    /**
     * Create a new bound text area.
     *
     * @param property the property to bind
     * @param columns  the no. of columns to display
     * @return a new bound text field
     */
    public static RichTextArea createRichTextArea(Property property,
                                                  int columns) {
        RichTextArea text = new BoundRichTextArea(property, columns);
        setDefaultStyle(text);
        return text;
    }

    /**
     * Create a new password field.
     *
     * @return a new password field
     */
    public static TextField createPassword() {
        TextField password = new PasswordField();
        password.setDocument(new TextDocument());
        setDefaultStyle(password);
        return password;
    }

    /**
     * Create a new password field.
     *
     * @param columns the no. of columns to display
     * @return a new password field
     */
    public static TextField createPassword(int columns) {
        TextField text = createPassword();
        if (columns <= 10) {
            text.setWidth(new Extent(columns, Extent.EM));
        } else {
            text.setWidth(new Extent(columns, Extent.EX));
        }
        return text;
    }
}
