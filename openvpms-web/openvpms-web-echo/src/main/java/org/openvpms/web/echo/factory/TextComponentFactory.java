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

package org.openvpms.web.echo.factory;

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Extent;
import org.openvpms.web.echo.text.PasswordField;
import org.openvpms.web.echo.text.TextArea;
import org.openvpms.web.echo.text.TextComponent;
import org.openvpms.web.echo.text.TextDocument;
import org.openvpms.web.echo.text.TextField;
import org.openvpms.web.resource.i18n.format.NumberFormatter;

import java.math.BigDecimal;


/**
 * Factory for {@link TextComponent}s.
 *
 * @author Tim Anderson
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
     * Create a new text area.
     *
     * @param columns the columns
     * @param rows    the rows
     * @return a new text area
     */
    public static TextArea createTextArea(int columns, int rows) {
        TextArea text = new TextArea(new TextDocument());
        text.setWidth(new Extent(columns, Extent.EX));
        text.setHeight(new Extent(rows, Extent.EM));
        setDefaultStyle(text);
        return text;
    }

    /**
     * Create a new password field.
     *
     * @return a new password field
     */
    public static PasswordField createPassword() {
        PasswordField password = new PasswordField();
        password.setDocument(new TextDocument());
        setDefaultStyle(password);
        return password;
    }

    /**
     * Creates a new text field that has a width based on the number of characters present.
     * <p>
     * This selects a width that displays the text in field slightly bigger than the text, up to maxLength characters.
     * This is not perfect, as it is dependent on the font and characters used, but works in most cases.
     * The alternative would be to calculate the field width in pixels on the browser side based on the characters.
     * See OVPMS-1440.
     *
     * @param value     the text value. May be {@code null}
     * @param minLength the minimum display length
     * @param maxLength the maximum display length
     * @return a new text field
     */
    public static TextComponent create(String value, int minLength, int maxLength) {
        int columns = (value != null) ? value.length() : minLength;
        if (columns < minLength) {
            columns = minLength;
        }
        if (columns > maxLength) {
            columns = maxLength;
        }
        TextComponent result = create();

        int width;
        int units = Extent.EX;
        if (columns < 7) {
            width = columns;
            units = Extent.EM;
        } else if (columns < 25) {
            width = columns + 3 + (columns / 5);
        } else {
            width = columns + (columns / 8);
        }
        if (width > maxLength) {
            width = maxLength;
        }
        result.setWidth(new Extent(width, units));
        result.setText(value);
        return result;
    }

    /**
     * Create a new password field.
     *
     * @param columns the no. of columns to display
     * @return a new password field
     */
    public static PasswordField createPassword(int columns) {
        PasswordField text = createPassword();
        if (columns <= 10) {
            text.setWidth(new Extent(columns, Extent.EM));
        } else {
            text.setWidth(new Extent(columns, Extent.EX));
        }
        return text;
    }

    /**
     * Creates a right aligned text field displaying a currency amount.
     *
     * @param amount   the amount
     * @param columns  the columns
     * @param readOnly if {@code true} disable the field
     * @return a new text field
     */
    public static TextComponent createAmount(BigDecimal amount, int columns, boolean readOnly) {
        TextField field = create(columns);
        Alignment align = new Alignment(Alignment.RIGHT, Alignment.DEFAULT);
        field.setAlignment(align);
        field.setText(NumberFormatter.getCurrencyFormat().format(amount));
        if (readOnly) {
            field.setEnabled(false);
        }
        return field;
    }


}
