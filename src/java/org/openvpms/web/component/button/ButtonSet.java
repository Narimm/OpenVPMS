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

import echopointng.KeyStrokeListener;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
     * The focus group. May be <code>null</code>
     */
    private final FocusGroup focusGroup;

    /**
     * The keystroke listener. May be <code>null</code>.
     */
    private KeyStrokeListener keyStrokeListener;

    /**
     * The button style.
     */
    private final String style;

    /**
     * The default button style.
     */
    private static final String BUTTON_STYLE = "default";

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(ButtonSet.class);


    /**
     * Constructs a new <code>ButtonSet</code>.
     *
     * @param container the button container
     */
    public ButtonSet(Component container) {
        this(container, null);
    }

    /**
     * Constructs a new <code>ButtonSet</code>.
     *
     * @param container the button container
     * @param focus     the focus group. May be <code>null</code>
     */
    public ButtonSet(Component container, FocusGroup focus) {
        this(container, focus, BUTTON_STYLE);
    }

    /**
     * Construct a new <code>ButtonSet</code>.
     *
     * @param container the button container
     * @param focus     the focus group. May be <code>null</code>
     * @param style     the button style. May be <code>null</code>
     */
    public ButtonSet(Component container, FocusGroup focus, String style) {
        this.container = container;
        if (focus != null) {
            this.focusGroup = new FocusGroup("ButtonSet");
            focus.add(this.focusGroup);
        } else {
            this.focusGroup = null;
        }
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
     *           bundle key. May be <code>null</code>
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
     * @param id              the button id, used to identify the button and as a resource
     *                        bundle key. May be <code>null</code>
     * @param disableShortcut if <code>true</code> disable any keyboard shortcut
     * @return a new button
     */
    public Button add(String id, boolean disableShortcut) {
        Button button;
        if (disableShortcut) {
            button = ButtonFactory.create(null, style);
            if (id != null) {
                String text = ShortcutHelper.getText(
                        ButtonFactory.getString(id));
                button.setText(text);
            }
        } else {
            button = ButtonFactory.create(id, style);
        }
        button.setId(id);
        button.setActionCommand(id);
        return add(button);
    }

    /**
     * Adds a button.
     * Note that for {@link ShortcutButton} instances, the
     * {@link ShortcutButton#getId} must return non-null in order for keystroke
     * events to be triggered on the appropriate button.
     *
     * @param button the button to add
     * @return the button
     */
    public Button add(Button button) {
        if (button instanceof ShortcutButton) {
            ShortcutButton shortcut = (ShortcutButton) button;
            addKeyStrokeListener(shortcut);
        }
        if (focusGroup != null) {
            focusGroup.add(button);
        }
        if (keyStrokeListener != null) {
            // add the button before the keystroke listener to avoid
            // cell spacing issues
            container.add(button, container.getComponentCount() - 1);
        } else {
            container.add(button);
        }
        return button;
    }

    /**
     * Adds a button, and registers an event listener.
     *
     * @param id       the button id, used to identify the button and as a resource
     *                 bundle key. May be <code>null</code>
     * @param listener the listener to add
     * @return a new button
     */
    public Button add(String id, ActionListener listener) {
        return add(id, listener, false);
    }

    /**
     * Adds a button, and registers an event listener.
     *
     * @param id              the button id, used to identify the button and as a resource
     *                        bundle key. May be <code>null</code>
     * @param listener        the listener to add
     * @param disableShortcut if <code>true</code> disable any keyboard shortcut
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
            removeKeystrokeListener((ShortcutButton) button);
        }
    }

    /**
     * Removes all buttons from the parent container.
     * Any other components will remain.
     */
    public void removeAll() {
        for (Component component : container.getComponents()) {
            if (component instanceof Button) {
                container.remove(component);
            }
        }
    }

    /**
     * Determines if the set contains a button.
     *
     * @param button the button
     * @return <code>true</code> if the set contains the button, otherwise
     *         <code>false</code>
     */
    public boolean contains(Button button) {
        return (container.indexOf(button) != -1);
    }

    /**
     * Returns a button given its identifier.
     *
     * @param id the button identifier
     * @return the button with the corresponding id, or <code>null</code> if
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
        for (Component component : container.getComponents()) {
            if (component instanceof ShortcutButton) {
                ShortcutButton button = (ShortcutButton) component;
                removeKeystrokeListener(button);
                addKeyStrokeListener(button);
            }
        }
    }

    /**
     * Adds a keystroke listener for a button.
     *
     * @param button the shortcut button to add the listener for
     */
    private void addKeyStrokeListener(ShortcutButton button) {
        int code = button.getKeyCode();
        if (code != -1) {
            getListener().addKeyCombination(code, button.getId());
        }
    }

    /**
     * Removes a keystroke listener.
     *
     * @param button the shortcut button to remove the listener for
     */
    private void removeKeystrokeListener(ShortcutButton button) {
        int code = button.getKeyCode();
        if (code != -1) {
            if (keyStrokeListener != null) {
                keyStrokeListener.removeKeyCombination(code);
            }
        }
    }

    /**
     * Invoked when a keystroke is pressed.
     *
     * @param event the action event
     */
    private void onKeyStroke(ActionEvent event) {
        String command = event.getActionCommand();
        if (command != null) {
            Button button = (Button) container.getComponent(command);
            if (button != null) {
                button.fireActionPerformed(event);
            } else {
                log.warn("Keystroke received but not handled, actionCommand="
                        + command);
            }
        } else {
            log.warn("Keystroke received but not handled");
        }
    }

    /**
     * Returns the keystroke listener, creating it if it doesn't exist.
     *
     * @return the keystroke listener
     */
    private KeyStrokeListener getListener() {
        if (keyStrokeListener == null) {
            keyStrokeListener = new KeyStrokeListener();
            keyStrokeListener.setCancelMode(true);
            keyStrokeListener.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    onKeyStroke(event);
                }
            });
            container.add(keyStrokeListener);
        }
        return keyStrokeListener;
    }

}
