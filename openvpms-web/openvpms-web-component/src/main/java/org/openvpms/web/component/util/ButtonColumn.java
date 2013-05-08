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

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.keyboard.KeyStrokeHandler;
import org.openvpms.web.echo.focus.FocusGroup;


/**
 * A column of buttons.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-12-12 03:25:07Z $
 */
public class ButtonColumn extends Column implements KeyStrokeHandler {

    /**
     * The button set.
     */
    private final ButtonSet set;

    /**
     * The column style.
     */
    private static final String STYLE = "ControlColumn";


    /**
     * Constructs a new <tt>ButtonColumn</tt>.
     */
    public ButtonColumn() {
        this(STYLE, null);
    }

    /**
     * Constructs a new <tt>ButtonColumn</tt>.
     *
     * @param columnStyle the column style. May be <tt>null</tt>
     * @param buttonStyle the button style. May be <tt>null</tt>
     */
    public ButtonColumn(String columnStyle, String buttonStyle) {
        this(null, columnStyle, buttonStyle);
    }

    /**
     * Constructs a new <tt>ButtonColumn</tt>.
     *
     * @param focus the focus group. May be <tt>null</tt>
     */
    public ButtonColumn(FocusGroup focus) {
        this(focus, STYLE, null);
    }

    /**
     * Constructs a new <tt>ButtonColumn</tt>.
     *
     * @param focus       the focus set. May be <tt>null</tt>
     * @param columnStyle the column style. May be <tt>null</tt>
     * @param buttonStyle the button style. May be <tt>null</tt>
     */
    public ButtonColumn(FocusGroup focus, String columnStyle, String buttonStyle) {
        setStyleName(columnStyle);
        set = new ButtonSet(this, focus, buttonStyle);
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
     * Adds a button. The key is used to get localised text for the
     * button, and is returned by {@link ActionEvent#getActionCommand} when
     * triggered.
     *
     * @param key the resource bundle key. May be <tt>null</tt>
     * @return a new button
     */
    public Button addButton(String key) {
        return set.add(key);
    }

    /**
     * Adds a button, and registers an event listener.
     *
     * @param key      the resource bundle key. May be <tt>null</tt>
     * @param listener the listener to add
     * @return the button
     */
    public Button addButton(String key, ActionListener listener) {
        return set.add(key, listener);
    }

    /**
     * Adds a button, and registers an event listener.
     *
     * @param key             the resource bundle key. May be <tt>null</tt>
     * @param listener        the listener to add
     * @param disableShortcut if <tt>true</tt> disable any keyboard shortcut
     * @return the button
     */
    public Button addButton(String key, ActionListener listener, boolean disableShortcut) {
        return set.add(key, listener, disableShortcut);
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
     * Removes the specified child <tt>Component</tt> from this
     * <tt>Component</tt>.
     *
     * @param component the child <tt>Component</tt> to remove
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
