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

package org.openvpms.web.component.dialog;

import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;


/**
 * Generic popup dialog, providing OK and Cancel buttons.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class PopupDialog extends PopupWindow {

    /**
     * OK button identifier.
     */
    public static final String OK_ID = "ok";

    /**
     * Cancel button identifier.
     */
    public static final String CANCEL_ID = "cancel";

    /**
     * Yes button identifier.
     */
    public static final String YES_ID = "yes";

    /**
     * No button identifier.
     */
    public static final String NO_ID = "no";

    /**
     * Skip button identifier.
     */
    public static final String SKIP_ID = "skip";

    /**
     * Used to indicate which buttons to display.
     */
    public enum Button {
        OK, YES, NO, CANCEL, SKIP}

    /**
     *
     */
    public static final String[] OK = {OK_ID};

    public static final String[] CANCEL = {CANCEL_ID};

    public static final String[] OK_CANCEL = {OK_ID, CANCEL_ID};

    public static final String[] OK_SKIP_CANCEL = {OK_ID, SKIP_ID, CANCEL_ID};

    public static final String[] SKIP_CANCEL = {SKIP_ID, CANCEL_ID};

    public static final String[] YES_NO_CANCEL = {YES_ID, NO_ID, CANCEL_ID};

    /**
     * The dialog action.
     */
    private String _action;


    /**
     * Construct a new <code>PopupDialog</code>.
     *
     * @param title   the window title
     * @param buttons the buttons to display
     */
    public PopupDialog(String title, String[] buttons) {
        this(title, null, buttons);
    }

    /**
     * Construct a new <code>PopupDialog</code>.
     *
     * @param title   the window title
     * @param style   the window style
     * @param buttons the buttons to display
     */
    public PopupDialog(String title, String style, String[] buttons) {
        super(title, style, null);

        for (final String button : buttons) {
            addButton(button, new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    onButton(button);
                }
            });
        }
    }

    /**
     * Returns the dialog action.
     *
     * @return the dialog action
     */
    public String getAction() {
        return _action;
    }

    /**
     * Sets the dialog action.
     *
     * @param action the action
     */
    protected void setAction(String action) {
        _action = action;
    }

    /**
     * Invoked when a button is pressed. This delegates to the appropriate
     * on*() method for the button if it is known, else sets the action to
     * the button identifier and closes the window.
     *
     * @param button the button identifier
     */
    protected void onButton(String button) {
        if (OK_ID.equals(button)) {
            onOK();
        } else if (CANCEL_ID.equals(button)) {
            onCancel();
        } else if (YES_ID.equals(button)) {
            onYes();
        } else if (NO_ID.equals(button)) {
            onNo();
        } else if (SKIP_ID.equals(button)) {
            onSkip();
        } else {
            setAction(button);
            close();
        }
    }

    /**
     * Invoked when the 'OK' button is pressed. This sets the action and closes
     * the window.
     */
    protected void onOK() {
        setAction(OK_ID);
        close();
    }

    /**
     * Invoked when the 'cancel' button is pressed. This sets the action and
     * closes the window.
     */
    protected void onCancel() {
        setAction(CANCEL_ID);
        close();
    }

    /**
     * Invoked when the 'yes' button is pressed. This sets the action and closes
     * the window.
     */
    protected void onYes() {
        setAction(YES_ID);
        close();
    }

    /**
     * Invoked when the 'no' button is pressed. This sets the action and closes
     * the window.
     */
    protected void onNo() {
        setAction(NO_ID);
        close();
    }

    /**
     * Invoked when the 'skip' button is pressed. This sets the action and
     * closes the window.
     */
    protected void onSkip() {
        setAction(SKIP_ID);
        close();
    }
}
