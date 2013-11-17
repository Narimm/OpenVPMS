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

package org.openvpms.web.echo.button;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.keyboard.KeyStrokeHandler;


/**
 * A row of buttons.
 *
 * @author Tim Anderson
 */
public class ButtonRow extends Row implements KeyStrokeHandler {

    /**
     * The button set.
     */
    private final ButtonSet set;

    /**
     * The default row style.
     */
    public static final String STYLE = "ButtonRow";

    /**
     * The default button style.
     */
    public static final String BUTTON_STYLE = "default";


    /**
     * Constructs a {@link ButtonRow}.
     */
    public ButtonRow() {
        this(STYLE, BUTTON_STYLE);
    }

    /**
     * Constructs a {@link ButtonRow}.
     *
     * @param rowStyle the row style. May be {@code null}
     */
    public ButtonRow(String rowStyle) {
        this(rowStyle, BUTTON_STYLE);
    }

    /**
     * Constructs a {@link ButtonRow}.
     *
     * @param focus the focus group
     */
    public ButtonRow(FocusGroup focus) {
        this(focus, STYLE, BUTTON_STYLE);
    }

    /**
     * Constructs a {@link ButtonRow}.
     *
     * @param rowStyle    the row style. May be {@code null}
     * @param buttonStyle the button style. May be {@code null}
     */
    public ButtonRow(String rowStyle, String buttonStyle) {
        this(null, rowStyle, buttonStyle);
    }

    /**
     * Constructs a {@link ButtonRow}.
     *
     * @param focus       the focus group. May be {@code null}
     * @param rowStyle    the row style. May be {@code null}
     * @param buttonStyle the button style. May be {@code null}
     */
    public ButtonRow(FocusGroup focus, String rowStyle, String buttonStyle) {
        setStyleName(rowStyle);

        set = new ButtonSet(this, focus, buttonStyle);
    }

    /**
     * Constructs a {@link ButtonRow}.
     *
     * @param shortcutContainer the container to add the keystroke listener to. Specify this to avoid cell spacing
     *                          issues
     * @param focus             the focus group. May be {@code null}
     */
    public ButtonRow(Component shortcutContainer, FocusGroup focus) {
        this(shortcutContainer, focus, STYLE, BUTTON_STYLE);
    }

    /**
     * Constructs a {@link ButtonRow}.
     *
     * @param shortcutContainer the container to add the keystroke listener to. Specify this to avoid cell spacing
     *                          issues
     * @param focus             the focus group. May be {@code null}
     * @param rowStyle          the row style. May be {@code null}
     * @param buttonStyle       the button style. May be {@code null}
     */
    public ButtonRow(Component shortcutContainer, FocusGroup focus, String rowStyle, String buttonStyle) {
        setStyleName(rowStyle);
        set = new ButtonSet(this, shortcutContainer, focus, buttonStyle);
    }

    /**
     * Returns the buttons.
     *
     * @return the buttons
     */
    public ButtonSet getButtons() {
        return set;
    }

    /**
     * Adds a button.
     *
     * @return a new button
     */
    public Button addButton() {
        return set.add();
    }

    /**
     * Adds a button.
     *
     * @param button the button to add
     */
    public void addButton(Button button) {
        set.add(button);
    }

    /**
     * Adds a button. The key is used to get localised text for the
     * button, and is returned by {@link ActionEvent#getActionCommand} when
     * triggered.
     *
     * @param key the resource bundle key. May be {@code null}
     * @return a new button
     */
    public Button addButton(String key) {
        return set.add(key);
    }

    /**
     * Adds a button, and registers an event listener.
     *
     * @param key      the resource bundle key. May be {@code null}
     * @param listener the listener to add
     * @return the button
     */
    public Button addButton(String key, ActionListener listener) {
        return addButton(key, false, listener);
    }

    /**
     * Adds a button, and registers an event listener.
     *
     * @param key             the resource bundle key. May be {@code null}
     * @param disableShortcut if {@code true} disable any keyboard shortcut
     * @param listener        the listener to add
     * @return the button
     */
    public Button addButton(String key, boolean disableShortcut, ActionListener listener) {
        return set.add(key, disableShortcut, listener);
    }

    /**
     * Removes a button.
     *
     * @param button the button to remove
     */
    public void removeButton(Button button) {
        set.remove(button);
    }

    /**
     * Removes the specified child {@code Component} from this
     * {@code Component}.
     *
     * @param component the child {@code Component} to remove
     */
    @Override
    public void remove(Component component) {
        super.remove(component);
        if (component instanceof Button) {
            removeButton((Button) component);
        }
    }

    /**
     * Re-registers keystroke listeners.
     * This is a workaround for Firefox which appears to deregister listeners
     * on the parent component when a child contains listeners.
     */
    public void reregisterKeyStrokeListeners() {
        set.reregisterKeyStrokeListeners();
    }

}