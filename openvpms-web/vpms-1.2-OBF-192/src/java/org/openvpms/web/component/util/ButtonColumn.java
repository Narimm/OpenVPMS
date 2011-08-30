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
import org.openvpms.web.component.button.ButtonSet;
import org.openvpms.web.component.button.KeyStrokeHandler;
import org.openvpms.web.component.focus.FocusGroup;


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
     * Constructs a new <code>Buttoncolumn</code>.
     */
    public ButtonColumn() {
        this(STYLE, null);
    }

    /**
     * Constructs a new <code>Buttoncolumn</code>.
     *
     * @param columnStyle the column style. May be <code>null</code>
     * @param buttonStyle the button style. May be <code>null</code>
     */
    public ButtonColumn(String columnStyle, String buttonStyle) {
        this(null, columnStyle, buttonStyle);
    }

    /**
     * Constructs a new <code>Buttoncolumn</code>.
     *
     * @param focus       the focus set. May be <code>null</code>
     * @param columnStyle the column style. May be <code>null</code>
     * @param buttonStyle the button style. May be <code>null</code>
     */
    public ButtonColumn(FocusGroup focus, String columnStyle,
                        String buttonStyle) {
        setStyleName(columnStyle);
        set = new ButtonSet(this, focus, buttonStyle);
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
     * @param key the resource bundle key. May be <code>null</code>
     * @return a new button
     */
    public Button addButton(String key) {
        return set.add(key);
    }

    /**
     * Adds a button, and registers an event listener.
     *
     * @param key      the resource bundle key. May be <code>null</code>
     * @param listener the listener to add
     * @return the button
     */
    public Button addButton(String key, ActionListener listener) {
        return set.add(key, listener);
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
     * Removes the specified child <code>Component</code> from this
     * <code>Component</code>.
     *
     * @param component the child <code>Component</code> to remove
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
