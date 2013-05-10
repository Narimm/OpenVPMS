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

package org.openvpms.web.echo.factory;

import nextapp.echo2.app.Extent;
import nextapp.echo2.app.PasswordField;
import nextapp.echo2.app.TextArea;
import nextapp.echo2.app.text.TextComponent;
import org.openvpms.web.echo.text.TextDocument;
import org.openvpms.web.echo.text.TextField;


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

}
