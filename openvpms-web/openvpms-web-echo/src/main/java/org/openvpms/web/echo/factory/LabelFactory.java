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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.echo.factory;

import echopointng.LabelEx;
import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.ImageReference;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.layout.CellLayoutData;
import org.openvpms.web.echo.text.TextHelper;
import org.openvpms.web.resource.i18n.format.NumberFormatter;


/**
 * Factory for {@link Label}s. Labels returned by this factory filter
 * invalid characters, using {@link TextHelper}.
 *
 * @author Tim Anderson
 */
public final class LabelFactory extends ComponentFactory {

    /**
     * Component type.
     */
    private static final String TYPE = "label";


    /**
     * Create a new label, with the default style.
     *
     * @return a new label
     */
    public static Label create() {
        return create(false);
    }

    /**
     * Creates a new label with the specified text and default style.
     *
     * @param text the label text
     * @return a new label
     */
    public static Label text(String text) {
        Label label = create();
        label.setText(text);
        return label;
    }

    /**
     * Creates a new label with the specified text.
     *
     * @param text the label text
     * @param style the style name
     * @return a new label
     */
    public static Label text(String text, String style) {
        Label label = create(null, style);
        label.setText(text);
        return label;
    }

    /**
     * Creates a new label that may support multiple lines.
     *
     * @param multiline if {@code true}, interprets new lines in the text
     * @return a new label
     */
    public static Label create(boolean multiline) {
        return create(multiline, false);
    }

    /**
     * Creates a new label that may support multiple lines, and wrap long lines.
     *
     * @param multiline if {@code true}, interprets new lines in the text
     * @param wrap      if {@code true}, long lines will be wrapped
     * @return a new label
     */
    public static Label create(boolean multiline, boolean wrap) {
        Label result;
        if (multiline) {
            LabelEx label = new LabelEx() {
                @Override
                public void setText(String newValue) {
                    if (TextHelper.hasControlChars(newValue)) {
                        // replace any control chars with spaces.
                        newValue = TextHelper.replaceControlChars(newValue, " ");
                    }
                    super.setText(newValue);
                }
            };
            label.setIntepretNewlines(true);
            result = label;
        } else {
            result = new Label() {
                @Override
                public void setText(String newValue) {
                    if (TextHelper.hasControlChars(newValue)) {
                        // replace any control chars with spaces.
                        newValue = TextHelper.replaceControlChars(newValue, " ");
                    }
                    super.setText(newValue);
                }
            };
        }
        result.setLineWrap(wrap);
        setDefaultStyle(result);
        return result;
    }

    /**
     * Create a new label with an image.
     *
     * @param image the image
     * @return a new label.
     */
    public static Label create(ImageReference image) {
        Label label = create();
        label.setIcon(image);
        return label;
    }

    /**
     * Create a new label with localised text, and default style.
     *
     * @param key the resource bundle key. May be {@code null}
     * @return a new label
     */
    public static Label create(String key) {
        return create(key, false);
    }

    /**
     * Create a new label with localised text, and default style.
     *
     * @param key       the resource bundle key. May be {@code null}
     * @param multiline if {@code true}, iterprets new lines in the text
     * @return a new label
     */
    public static Label create(String key, boolean multiline) {
        return create(key, multiline, false);
    }

    /**
     * Create a new label with localised text, and default style.
     *
     * @param key       the resource bundle key. May be {@code null}
     * @param multiline if {@code true}, interprets new lines in the text
     * @param wrap      if {@code true}, long lines will be wrapped
     * @return a new label
     */
    public static Label create(String key, boolean multiline, boolean wrap) {
        Label label = create(multiline, wrap);
        if (key != null) {
            label.setText(getString(TYPE, key, false));
        }
        return label;
    }

    /**
     * Create a new label with localised text, and specific style.
     *
     * @param key   the resource bundle key. May be {@code null}
     * @param style the label style
     * @return a new label
     */
    public static Label create(String key, String style) {
        Label label = create(key);
        setStyle(label, style);
        return label;
    }

    /**
     * Creates a new label for a numeric value, to be right aligned in a cell.
     *
     * @param value  the value
     * @param layout the layout to assign the label
     * @return a new label
     */
    public static Label create(Number value, CellLayoutData layout) {
        Label label = create();
        label.setText(NumberFormatter.format(value));
        return rightAlign(label, layout);
    }

    /**
     * Creates a new label for a numeric value, to be right aligned in a cell.
     *
     * @param value  the value
     * @param layout the layout to assign the label
     * @return a new label
     */
    public static Label create(long value, CellLayoutData layout) {
        Label label = create();
        label.setText(NumberFormatter.format(value));
        return rightAlign(label, layout);
    }

    /**
     * Helper to right-align a label.
     *
     * @param label  the label to align
     * @param layout the layout to assign the label
     * @return the label
     */
    private static Label rightAlign(Label label, CellLayoutData layout) {
        layout.setAlignment(new Alignment(Alignment.RIGHT, Alignment.DEFAULT));
        label.setLayoutData(layout);
        return label;
    }

}
