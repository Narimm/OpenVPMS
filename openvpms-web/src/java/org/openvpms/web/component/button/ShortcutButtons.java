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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.button;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;


/**
 * Manages a set of {@link ShortcutButtons}, enabling them to receive
 * keystroke notification.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ShortcutButtons extends AbstractKeystrokeHandler {

    /**
     * The buttons.
     */
    private Map<String, ShortcutButton> buttons
            = new HashMap<String, ShortcutButton>();

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(ShortcutButtons.class);


    /**
     * Creates a new <tt>ShortcutButtons</tt>.
     *
     * @param container the container to register the keystroke listener in
     */
    public ShortcutButtons(Component container) {
        super(container);
    }

    /**
     * Adds a new button.
     *
     * @param button the button to add
     */
    public void add(ShortcutButton button) {
        buttons.put(button.getActionCommand(), button);
        addKeyStrokeListener(button);
    }

    /**
     * Removes a button.
     *
     * @param button the button to remove
     */
    public void remove(ShortcutButton button) {
        buttons.remove(button.getActionCommand());
        removeKeystrokeListener(button);
    }

    /**
     * Re-registers keystroke listeners.
     */
    public void reregisterKeyStrokeListeners() {
        for (ShortcutButton button : buttons.values()) {
            removeKeystrokeListener(button);
            addKeyStrokeListener(button);
        }
    }

    /**
     * Invoked when a keystroke is pressed.
     *
     * @param event the action event
     */
    protected void onKeyStroke(ActionEvent event) {
        String command = event.getActionCommand();
        if (command != null) {
            Button button = buttons.get(command);
            if (button != null && button.isEnabled()) {
                ActionEvent buttonEvent = new ActionEvent(button, command);
                button.fireActionPerformed(buttonEvent);
            } else {
                log.warn("Keystroke received but not handled, actionCommand="
                         + command);
            }
        } else {
            log.warn("Keystroke received but not handled");
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
            addKey(code, button.getActionCommand());
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
            removeKey(code);
        }
    }

}
