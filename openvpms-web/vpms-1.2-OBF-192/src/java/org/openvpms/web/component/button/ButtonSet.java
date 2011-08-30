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

package org.openvpms.web.component.button;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import org.openvpms.web.component.focus.FocusGroup;
import org.openvpms.web.component.util.ButtonFactory;


/**
 * A set of buttons, rendered in a component.
 * If a {@link ShortcutButton}s with a valid keycode is added to this, it
 * will receive keystroke notification.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-12-12 03:25:07Z $
 */
public class ButtonSet implements KeyStrokeHandler {

    /**
     * The button container.
     */
    private final Component container;

    /**
     * The focus group. May be <tt>null</tt>
     */
    private final FocusGroup focusGroup;

    /**
     * The button style.
     */
    private final String style;

    /**
     * The shortcut buttons.
     */
    private ShortcutButtons buttons;

    /**
     * The default button style.
     */
    private static final String BUTTON_STYLE = "default";


    /**
     * Constructs a new <tt>ButtonSet</tt>.
     *
     * @param container the button container
     */
    public ButtonSet(Component container) {
        this(container, null);
    }

    /**
     * Constructs a new <tt>ButtonSet</tt>.
     *
     * @param container the button container
     * @param focus     the focus group. May be <tt>null</tt>
     */
    public ButtonSet(Component container, FocusGroup focus) {
        this(container, focus, BUTTON_STYLE);
    }

    /**
     * Construct a new <tt>ButtonSet</tt>.
     *
     * @param container the button container
     * @param focus     the focus group. May be <tt>null</tt>
     * @param style     the button style. May be <tt>null</tt>
     */
    public ButtonSet(Component container, FocusGroup focus, String style) {
        this.container = container;
        if (focus != null) {
            this.focusGroup = new FocusGroup("ButtonSet");
            focus.add(this.focusGroup);
        } else {
            this.focusGroup = null;
        }
        buttons = new ShortcutButtons(container);
        this.style = (style != null) ? style : BUTTON_STYLE;
    }

    /**
     * Adds a button.
     *
     * @return a new button
     */
    public Button add() {
        return add(null, false);
    }

    /**
     * Adds a button. The id is used to get localised text for the
     * button, and is returned by {@link ActionEvent#getActionCommand} when
     * triggered.
     *
     * @param id the button id, used to identify the button and as a resource
     *           bundle key. May be <tt>null</tt>
     * @return a new button
     */
    public Button add(String id) {
        return add(id, false);
    }

    /**
     * Adds a button. The id is used to get localised text for the
     * button, and is returned by {@link ActionEvent#getActionCommand} when
     * triggered.
     *
     * @param id              the button id, used to identify the button and as
     *                        a resource bundle key. May be <tt>null</tt>
     * @param disableShortcut if <tt>true</tt> disable any keyboard shortcut
     * @return a new button
     */
    public Button add(String id, boolean disableShortcut) {
        Button button = ButtonFactory.create(id, style, !disableShortcut);
        return add(button);
    }

    /**
     * Adds a button.
     * Note that for {@link ShortcutButton} instances, the
     * {@link ShortcutButton#getActionCommand()} must return non-null in order
     * for keystroke events to be triggered on the appropriate button.
     *
     * @param button the button to add
     * @return the button
     */
    public Button add(Button button) {
        if (button instanceof ShortcutButton) {
            buttons.add((ShortcutButton) button);
        }
        if (focusGroup != null) {
            focusGroup.add(button);
        }
        Component listener = buttons.getKeyStrokeListener();
        if (listener != null) {
            // add the button before the keystroke listener to avoid
            // cell spacing issues
            container.add(button, container.indexOf(listener));
        } else {
            container.add(button);
        }
        return button;
    }

    /**
     * Adds a button, and registers an event listener.
     *
     * @param id       the button id, used to identify the button and as a
     *                 resource bundle key. May be <tt>null</tt>
     * @param listener the listener to add
     * @return a new button
     */
    public Button add(String id, ActionListener listener) {
        return add(id, listener, false);
    }

    /**
     * Adds a button, and registers an event listener.
     *
     * @param id              the button id, used to identify the button and as
     *                        a resource bundle key. May be <tt>null</tt>
     * @param listener        the listener to add
     * @param disableShortcut if <tt>true</tt> disable any keyboard shortcut
     * @return a new button
     */
    public Button add(String id, ActionListener listener,
                      boolean disableShortcut) {
        Button button = add(id, disableShortcut);
        button.addActionListener(listener);
        return button;
    }

    /**
     * Removes a button.
     *
     * @param button the button to remove
     */
    public void remove(Button button) {
        if (button.getParent() != null
                && button.getParent().equals(container)) {
            container.remove(button);
        }
        if (button instanceof ShortcutButton) {
            buttons.remove((ShortcutButton) button);
        }
    }

    /**
     * Removes all buttons from the parent container.
     * Any other components will remain.
     */
    public void removeAll() {
        for (Component component : container.getComponents()) {
            if (component instanceof Button) {
                remove((Button) component);
            }
        }
    }

    /**
     * Determines if the set contains a button.
     *
     * @param button the button
     * @return <tt>true</tt> if the set contains the button, otherwise
     *         <tt>false</tt>
     */
    public boolean contains(Button button) {
        return (container.indexOf(button) != -1);
    }

    /**
     * Returns a button given its identifier.
     *
     * @param id the button identifier
     * @return the button with the corresponding id, or <tt>null</tt> if
     *         none is found
     */
    public Button getButton(String id) {
        return (Button) container.getComponent(id);
    }

    /**
     * Returns the underlying container.
     *
     * @return the container
     */
    public Component getContainer() {
        return container;
    }

    /**
     * Re-registers keystroke listeners.
     * This is a workaround for Firefox which appears to deregister listeners
     * on the parent component when a child contains listeners.
     */
    public void reregisterKeyStrokeListeners() {
        buttons.reregisterKeyStrokeListeners();
    }

}
