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
 *  Copyright 2009 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.echo.button;

import echopointng.KeyStrokeListener;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;

import java.util.HashMap;
import java.util.Map;


/**
 * Abstract implementation of the {@link KeyStrokeHandler} interface
 * that provides support for registering listeners for specific key codes.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractKeystrokeHandler implements KeyStrokeHandler {

    /**
     * The container.
     */
    private final Component container;

    /**
     * The keystroke listener. May be <tt>null</tt>.
     */
    private KeyStrokeListener listener;

    /**
     * The handlers.
     */
    private Map<String, ActionListener> handlers = new HashMap<String, ActionListener>();


    /**
     * Constructs a <tt>AbstractKeystrokeHandler</tt>.
     *
     * @param container the container to register the keystroke listener in
     */
    public AbstractKeystrokeHandler(Component container) {
        this.container = container;
    }

    /**
     * Returns the keystroke listener.
     *
     * @return the keystroke listener, or <tt>null</tt> if none is required
     */
    public KeyStrokeListener getKeyStrokeListener() {
        return listener;
    }

    /**
     * Adds a listener for the specified key code.
     *
     * @param keyCode  the key code
     * @param listener the listener to add
     */
    public void addListener(int keyCode, ActionListener listener) {
        String key = Integer.toString(keyCode);
        addKey(keyCode, key);
        handlers.put(key, listener);
    }

    /**
     * Removes the listener for the specified key code.
     *
     * @param keyCode the key code
     */
    public void removeListener(int keyCode) {
        removeKey(keyCode);
        handlers.remove(Integer.toString(keyCode));
    }

    /**
     * Invoked when a keystroke is pressed.
     *
     * @param event the action event
     */
    protected abstract void onKeyStroke(ActionEvent event);

    /**
     * Add a listener for the specified key.
     *
     * @param keyCode       the key code
     * @param actionCommand the command to be used with the <tt>ActionEvent</tt>
     */
    protected void addKey(int keyCode, String actionCommand) {
        getListener().addKeyCombination(keyCode, actionCommand);
    }

    /**
     * Removes a listener for the specified key.
     *
     * @param keyCode the key code
     */
    protected void removeKey(int keyCode) {
        if (listener != null) {
            listener.removeKeyCombination(keyCode);
        }
    }

    private void handleKey(ActionEvent event) {
        ActionListener listener = handlers.get(event.getActionCommand());
        if (listener != null) {
            listener.actionPerformed(event);
        } else {
            onKeyStroke(event);
        }
    }

    /**
     * Returns the keystroke listener, creating it if it doesn't exist.
     *
     * @return the keystroke listener
     */
    private KeyStrokeListener getListener() {
        if (listener == null) {
            listener = new KeyStrokeListener();
            listener.setCancelMode(true);
            listener.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    handleKey(event);
                }
            });
            container.add(listener);
        } else if (container.indexOf(listener) == -1) {
            // someone has done a removeAll() or similar on the container.
            // Need to re-register the listener
            container.add(listener);
        }
        return listener;
    }

}
