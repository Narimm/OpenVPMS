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

import echopointng.ButtonEx;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.MutableStyle;
import nextapp.echo2.app.event.ActionListener;
import org.openvpms.web.component.button.ShortcutButton;


/**
 * Factory for {@link Button}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public final class ButtonFactory extends ComponentFactory {

    /**
     * Component type.
     */
    private static final String TYPE = "button";


    /**
     * Create a new button with the default style.
     *
     * @return a new button
     */
    public static Button create() {
        Button button = new Button();
        setDefaultStyle(button);
        return button;
    }

    /**
     * Create a new button with a localised text message, and default style.
     * The button will be parsed for shortcuts.
     * If non-null, the key is also used as the button identifier and action
     * command ({@link Button#getId} and {@link Button#getActionCommand()}).
     *
     * @param key the resource bundle key. May be <code>null</code>.
     * @return a new button
     */
    public static Button create(String key) {
        Button button;
        if (key != null) {
            button = new ShortcutButton(getString(TYPE, key, false));
            button.setId(key);
            button.setActionCommand(key);
        } else {
            button = new ShortcutButton();
        }
        setDefaultStyle(button);
        return button;
    }

    /**
     * Create a new button with default style and listener.
     *
     * @param listener the listener
     * @return a new button
     */
    public static Button create(ActionListener listener) {
        Button button = create();
        button.addActionListener(listener);
        return button;
    }

    /**
     * Create a new button with a localised text message, default style, and
     * listener. The button will be parsed for shortcuts.
     *
     * @param key      the resource bundle key. May be <code>null</code>
     * @param listener the listener
     * @return a new button
     */
    public static Button create(String key, ActionListener listener) {
        Button button = create(key);
        button.addActionListener(listener);
        return button;
    }

    /**
     * Create a new button with a localised text message and style.
     * The button will be parsed for shortcuts.
     *
     * @param key   the resource bundle key. May be <code>null</code>
     * @param style the style name
     * @return a new button
     */
    public static Button create(String key, String style) {
        Button button = create(key);
        setStyle(button, style);
        return button;
    }

    /**
     * Create a new button with a localised text message, specific style, and
     * listener.
     *
     * @param key      the resource bundle key. May be <code>null</code>
     * @param style    the style name
     * @param listener the listener
     * @return a new button
     */
    public static Button create(String key, String style,
                                ActionListener listener) {
        Button button = create(key, style);
        button.addActionListener(listener);
        return button;
    }

    /**
     * Helper to return localised text for a button.
     *
     * @param key the resource bundle key
     * @return the localised string corresponding to <code>id</code>
     */
    public static String getString(String key) {
        return getString(TYPE, key, false);
    }

    /**
     * Hack to ensure that EPNG fallback styles aren't used. These prevent
     * inheritance.
     */
    static {
        MutableStyle defaultStyle = (MutableStyle) ButtonEx.DEFAULT_STYLE;
        defaultStyle.removeProperty(ButtonEx.PROPERTY_BACKGROUND);
        defaultStyle.removeProperty(ButtonEx.PROPERTY_ROLLOVER_BACKGROUND);
    }

}
