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

import echopointng.ButtonEx;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.MutableStyle;
import nextapp.echo2.app.RadioButton;
import nextapp.echo2.app.button.ButtonGroup;
import nextapp.echo2.app.event.ActionListener;
import org.openvpms.web.echo.button.ShortcutButton;
import org.openvpms.web.echo.button.ShortcutHelper;
import org.openvpms.web.resource.i18n.Messages;


/**
 * Factory for {@link Button}s.
 *
 * @author Tim Anderson
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
        Button button = new ButtonEx();
        setDefaultStyle(button);
        return button;
    }

    /**
     * Create a new button with a localised text message, and default style.
     * <p/>
     * The returned button supports shortcuts.
     * <p/>
     * If non-null, the key is also used as the button identifier and action
     * command ({@link Button#getId} and {@link Button#getActionCommand()}).
     *
     * @param key the resource bundle key. May be {@code null}.
     * @return a new button
     */
    public static Button create(String key) {
        return create(key, true);
    }

    /**
     * Create a new button with a localised text message, and default style.
     * <p/>
     * If non-null, the key is also used as the button identifier and action
     * command ({@link Button#getId} and {@link Button#getActionCommand()}).
     *
     * @param key             the resource bundle key. May be {@code null}.
     * @param enableShortcuts if {@code true}, enable shortcuts
     * @return a new button
     */
    public static Button create(String key, boolean enableShortcuts) {
        Button button = (enableShortcuts) ? new ShortcutButton() : new ButtonEx();
        if (key != null) {
            String text = getString(TYPE, key, false);
            if (!enableShortcuts) {
                text = ShortcutHelper.getText(text);
            }
            button.setText(text);
            button.setId(key);
            button.setActionCommand(key);
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
     * listener.
     * <p/>
     * The returned button supports shortcuts.
     * <p/>
     *
     * @param key      the resource bundle key. May be {@code null}
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
     * <p/>
     * The returned button supports shortcuts.
     * <p/>
     *
     * @param key   the resource bundle key. May be {@code null}
     * @param style the style name
     * @return a new button
     */
    public static Button create(String key, String style) {
        return create(key, style, true);
    }

    /**
     * Create a new button with a localised text message and style.
     *
     * @param key             the resource bundle key. May be {@code null}
     * @param style           the style name
     * @param enableShortcuts if {@code true}, enable shortcuts
     * @return a new button
     */
    public static Button create(String key, String style, boolean enableShortcuts) {
        Button button = create(key, enableShortcuts);
        setStyle(button, style);
        return button;
    }

    /**
     * Create a new button with a localised text message, specific style, and
     * listener.
     * <p/>
     * The returned button supports shortcuts.
     *
     * @param key      the resource bundle key. May be {@code null}
     * @param style    the style name
     * @param listener the listener
     * @return a new button
     */
    public static Button create(String key, String style, ActionListener listener) {
        return create(key, style, true, listener);
    }

    /**
     * Create a new button with a localised text message, specific style, and listener.
     *
     * @param key             the resource bundle key. May be {@code null}
     * @param style           the style name
     * @param enableShortcuts if {@code true}, enable shortcuts
     * @param listener        the listener
     * @return a new button
     */
    public static Button create(String key, String style, boolean enableShortcuts, ActionListener listener) {
        Button button = create(key, style, enableShortcuts);
        button.addActionListener(listener);
        return button;
    }

    /**
     * Creates a new radio button.
     *
     * @param key   the resource bundle key. May be {@code null}
     * @param group the button group
     * @return a new button
     */
    public static RadioButton create(String key, ButtonGroup group) {
        String label = (key != null) ? Messages.get(key) : null;
        RadioButton button = new RadioButton(label);
        button.setGroup(group);
        setDefaultStyle(button);
        return button;
    }

    /**
     * Creates a new radio button.
     *
     * @param key   the resource bundle key. May be {@code null}
     * @param group the button group
     * @return a new button
     */
    public static RadioButton create(String key, ButtonGroup group, ActionListener listener) {
        RadioButton button = create(key, group);
        button.addActionListener(listener);
        return button;
    }

    /**
     * Hack to ensure that EPNG fallback styles aren't used. These prevent
     * inheritance.
     */
    static {
        MutableStyle defaultStyle = (MutableStyle) ButtonEx.DEFAULT_STYLE;
        defaultStyle.removeProperty(ButtonEx.PROPERTY_BACKGROUND);
        defaultStyle.removeProperty(ButtonEx.PROPERTY_ROLLOVER_BACKGROUND);
        defaultStyle.removeProperty(ButtonEx.PROPERTY_DISABLED_FOREGROUND);
    }

}
