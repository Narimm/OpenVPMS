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

import java.util.EventListener;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.event.EventListenerList;

import org.openvpms.web.component.focus.FocusSet;
import org.openvpms.web.component.focus.FocusTree;


/**
 * A row of buttons.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ButtonRow extends Row {

    /**
     * The button event listener.
     */
    private final ActionListener _listener;

    /**
     * The focus set. May be <code>null</code>
     */
    private final FocusSet _focus;

    /**
     * The row style.
     */
    private static final String STYLE = "ButtonRow";

    /**
     * The button style.
     */
    private static final String BUTTON_STYLE = "ButtonRow.Button";


    /**
     * Construct a new <code>ButtonRow</code>.
     */
    public ButtonRow() {
        this(null);
    }

    /**
     * Construct a new <code>ButtonRow</code>.
     *
     * @param focus the focus tree. May be <code>null</code>
     */
    public ButtonRow(FocusTree focus) {
        setStyleName(STYLE);

        if (focus != null) {
            _focus = new FocusSet("ButtonRow");
            focus.add(_focus);
        } else {
            _focus = null;
        }

        _listener = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                doAction(event);
            }
        };
    }

    /**
     * Add a button. The identifier is used to get localised text for the
     * button, and is returned by {@link ActionEvent#getActionCommand} when
     * triggered.
     *
     * @param id the button identifier
     */
    public void addButton(String id) {
        add(id);
    }

    /**
     * Add a button, and register an event listener.
     *
     * @param id       the button identifier
     * @param listener the listener to add
     * @return the button
     */
    public Button addButton(String id, ActionListener listener) {
        Button button = add(id);
        button.addActionListener(listener);
        return button;
    }

    /**
     * Adds a listener to receive notification when the user presses a button.
     * The listener receives events from all buttons.
     *
     * @param listener the listener to add
     */
    public void addActionListener(ActionListener listener) {
        getEventListenerList().addListener(ActionListener.class, listener);
    }

    /**
     * Adds a listener to receive notification when the user presses a specific
     * button.
     *
     * @param id       the button identifier
     * @param listener the listener to add
     */
    public void addActionListener(String id, ActionListener listener) {
        Button button = (Button) getComponent(id);
        button.addActionListener(listener);
    }

    /**
     * Removes an <code>ActionListener</code> from receiving notification when
     * the user presses a button.
     *
     * @param listener the listener to remove
     */
    public void removeActionListener(ActionListener listener) {
        getEventListenerList().removeListener(ActionListener.class, listener);
    }

    /**
     * Removes an <code>ActionListener</code> from receiving notification when
     * the user presses a specific button.
     *
     * @param id       the button identifier
     * @param listener the listener to remove
     */
    public void removeActionListener(String id, ActionListener listener) {
        Button button = (Button) getComponent(id);
        button.removeActionListener(listener);
    }

    /**
     * Invoked when a button is pressed. Forwards the event to any registered
     * listener.
     *
     * @param event the button event
     */
    protected void doAction(ActionEvent event) {
        EventListenerList list = getEventListenerList();
        EventListener[] listeners = list.getListeners(ActionListener.class);
        ActionEvent forward = new ActionEvent(this, event.getActionCommand());
        for (EventListener listener : listeners) {
            ((ActionListener) listener).actionPerformed(forward);
        }
    }

    /**
     * Add a button.
     *
     * @param id the button identifier
     * @return the button.
     */
    protected Button add(String id) {
        Button button = ButtonFactory.create(id, BUTTON_STYLE, _listener);
        button.setId(id);
        button.setActionCommand(id);
        if (_focus != null) {
            _focus.add(button);
        }
        add(button);
        return button;
    }

}
