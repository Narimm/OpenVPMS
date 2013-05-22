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
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.keyboard.KeyStrokeHandler;


/**
 * A set of buttons, rendered in a component.
 * If a {@link ShortcutButton} with a valid keycode is added to this, it
 * will receive keystroke notification.
 *
 * @author Tim Anderson
 */
public class ButtonSet implements KeyStrokeHandler {

    /**
     * The button container.
     */
    private final Component container;

    /**
     * The focus group. May be {@code null}
     */
    private final FocusGroup focusGroup;

    /**
     * The button style.
     */
    private final String style;

    /**
     * Determines if disabled buttons should be hidden.
     */
    private boolean hideDisabled;

    /**
     * The shortcut buttons.
     */
    private ShortcutButtons buttons;

    /**
     * The default button style.
     */
    private static final String BUTTON_STYLE = "default";


    /**
     * Constructs a {@link ButtonSet}.
     *
     * @param container the button container
     */
    public ButtonSet(Component container) {
        this(container, null);
    }

    /**
     * Constructs a {@link ButtonSet}.
     *
     * @param container the button container
     * @param focus     the focus group. May be {@code null}
     */
    public ButtonSet(Component container, FocusGroup focus) {
        this(container, focus, BUTTON_STYLE);
    }

    /**
     * Constructs a {@link ButtonSet}.
     *
     * @param container the button container
     * @param focus     the focus group. May be {@code null}
     * @param style     the button style. May be {@code null}
     */
    public ButtonSet(Component container, FocusGroup focus, String style) {
        this(container, container, focus, style);
    }

    /**
     * Constructs a {@link ButtonSet}.
     *
     * @param container         the button container
     * @param shortcutContainer the container to add the keystroke listener to. Specify this to avoid cell spacing
     *                          issues
     * @param focus             the focus group. May be {@code null}
     * @param style             the button style. May be {@code null}
     */
    public ButtonSet(Component container, Component shortcutContainer, FocusGroup focus, String style) {
        this.container = container;
        if (focus != null) {
            this.focusGroup = new FocusGroup("ButtonSet");
            focus.add(this.focusGroup);
        } else {
            this.focusGroup = null;
        }
        buttons = new ShortcutButtons(shortcutContainer);
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
     * Adds a button. The id is used to get localised text for the button, and is returned by
     * {@link ActionEvent#getActionCommand} when triggered.
     *
     * @param id the button id, used to identify the button and as a resource bundle key. May be {@code null}
     * @return a new button
     */
    public Button add(String id) {
        return add(id, false);
    }

    /**
     * Adds a button. The id is used to get localised text for the button, and is returned by
     * {@link ActionEvent#getActionCommand} when triggered.
     *
     * @param id              the button id, used to identify the button and as
     *                        a resource bundle key. May be {@code null}
     * @param disableShortcut if {@code true} disable any keyboard shortcut
     * @return a new button
     */
    public Button add(String id, boolean disableShortcut) {
        Button button = create(id, disableShortcut);
        return add(button);
    }

    /**
     * Creates, but does not add a button.
     *
     * @param id the button id, used to identify the button and as a resource bundle key. May be {@code null}
     * @return the new button
     */
    public Button create(String id) {
        return create(id, false);
    }

    /**
     * Creates, but does not add a button.
     *
     * @param id              the button id, used to identify the button and as a resource bundle key.
     *                        May be {@code null}
     * @param disableShortcut if {@code true} disable any keyboard shortcut
     * @return the new button
     */
    public Button create(String id, boolean disableShortcut) {
        return ButtonFactory.create(id, style, !disableShortcut);
    }

    /**
     * Creates, but does not add a button.
     *
     * @param id       the button id, used to identify the button and as a resource bundle key. May be {@code null}
     * @param listener the listener to add
     * @return the new button
     */
    public Button create(String id, ActionListener listener) {
        return create(id, false, listener);
    }

    /**
     * Creates, but does not add a button.
     *
     * @param id              the button id, used to identify the button and as a resource bundle key.
     *                        May be {@code null}
     * @param disableShortcut if {@code true} disable any keyboard shortcut
     * @param listener        the listener to add
     * @return the new button
     */
    public Button create(String id, boolean disableShortcut, ActionListener listener) {
        Button button = create(id, disableShortcut);
        button.addActionListener(listener);
        return button;
    }

    /**
     * Adds a button.
     * <p/>
     * Note that for {@link ShortcutButton} instances, the {@link ShortcutButton#getActionCommand()} must
     * return non-null in order for keystroke events to be triggered on the appropriate button.
     *
     * @param button the button to add
     * @return the button
     */
    public Button add(Button button) {
        Component listener = buttons.getKeyStrokeListener();
        int index;
        if (listener != null) {
            // add the button before the keystroke listener to avoid cell spacing issues
            index = container.indexOf(listener);
        } else {
            index = container.getComponentCount();
        }
        return add(button, index);
    }

    /**
     * Adds a button at the specified position.
     * <p/>
     * Note that for {@link ShortcutButton} instances, the {@link ShortcutButton#getActionCommand()} must
     * return non-null in order for keystroke events to be triggered on the appropriate button.
     *
     * @param button the button to add
     * @param index  the index at which to add the button
     * @return the button
     */
    public Button add(Button button, int index) {
        if (button instanceof ShortcutButton) {
            buttons.add((ShortcutButton) button);
        }
        if (focusGroup != null) {
            focusGroup.add(button);
        }
        container.add(button, index);
        return button;
    }

    /**
     * Adds a button, and registers an event listener.
     *
     * @param id       the button id, used to identify the button and as a
     *                 resource bundle key. May be {@code null}
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
     *                        a resource bundle key. May be {@code null}
     * @param listener        the listener to add
     * @param disableShortcut if {@code true} disable any keyboard shortcut
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
     * Returns a button given its identifier.
     *
     * @param id the button identifier
     * @return the button with the corresponding id, or {@code null} if none is found
     */
    public Button getButton(String id) {
        return (Button) container.getComponent(id);
    }

    /**
     * Returns the index of a button given its identifier.
     *
     * @param id the button identifier
     * @return the index of the button or {@code -1} if none is found
     */
    public int indexOf(String id) {
        Button button = getButton(id);
        return (button == null) ? -1 : container.indexOf(button);
    }

    /**
     * Determines if disabled components should be visible.
     *
     * @param hide if {@code true}, hide disabled components
     */
    public void setHideDisabled(boolean hide) {
        this.hideDisabled = hide;
    }

    /**
     * Enables or disables a button.
     *
     * @param id      the button identifer
     * @param enabled if {@code true} enable the button, otherwise disable it
     */
    public void setEnabled(String id, boolean enabled) {
        Button button = getButton(id);
        if (button != null) {
            button.setEnabled(enabled);
            if (hideDisabled) {
                button.setVisible(enabled);
            }
        }
    }

    /**
     * Adds a listener for a specific key code.
     *
     * @param keyCode  the key code
     * @param listener the listener to add
     */
    public void addKeyListener(int keyCode, ActionListener listener) {
        buttons.addListener(keyCode, listener);
    }

    /**
     * Removes a listener for a key code.
     *
     * @param keyCode the key code
     */
    public void removeKeyListener(int keyCode) {
        buttons.removeListener(keyCode);
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
