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
 */

package org.openvpms.web.component.bound;

import echopointng.RichTextArea;
import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.PasswordField;
import nextapp.echo2.app.TextArea;
import nextapp.echo2.app.text.TextComponent;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.util.NumericPropertyFormatter;
import org.openvpms.web.echo.factory.TextComponentFactory;
import org.openvpms.web.echo.text.TextField;

import java.text.Format;


/**
 * Factory for {@link TextComponent}s bound to {@link Property} instances.
 *
 * @author Tim Anderson
 */
public class BoundTextComponentFactory extends TextComponentFactory {

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
    public static TextField create(Property property, int columns, Format format) {
        TextField text = new BoundFormattedField(property, columns, format);
        setDefaultStyle(text);
        return text;
    }

    /**
     * Creates a new bound text field, restricted to numeric input.
     *
     * @param property the property to bind
     * @param columns  the no. of columnns to display
     * @return a new bound text field
     */
    public static TextField createNumeric(Property property, int columns) {
        boolean edit = !property.isReadOnly() && !property.isDerived();
        Format format = NumericPropertyFormatter.getFormat(property, edit);
        TextField text = create(property, columns, format);
        if (!edit) {
            Alignment align = new Alignment(Alignment.RIGHT, Alignment.DEFAULT);
            text.setAlignment(align);
        }
        return text;
    }

    /**
     * Creates a new bound text area.
     *
     * @param property the property to bind
     * @return a new bound text field
     */
    public static TextArea createTextArea(Property property) {
        TextArea text = new BoundTextArea(property);
        setDefaultStyle(text);
        return text;
    }

    /**
     * Create a new bound text area.
     *
     * @param property the property to bind
     * @param columns  the no. of columns to display
     * @param rows     the no. of rows to display
     * @return a new bound text field
     */
    public static TextArea createTextArea(Property property, int columns, int rows) {
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
    public static RichTextArea createRichTextArea(Property property, int columns) {
        RichTextArea text = new BoundRichTextArea(property, columns);
        setDefaultStyle(text);
        return text;
    }

    /**
     * Create a new password field bound to a property.
     *
     * @param property the property to bind
     * @return a new password field
     */
    public static PasswordField createPassword(Property property) {
        PasswordField password = new BoundPasswordField(property);
        setDefaultStyle(password);
        return password;
    }

}
