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
     * Used to indicate which buttons to display.
     */
    public static enum Buttons {
        OK,
        CANCEL,
        OK_CANCEL
    }

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
    public PopupDialog(String title, Buttons buttons) {
        this(title, null, buttons);
    }

    /**
     * Construct a new <code>PopupDialog</code>.
     *
     * @param title   the window title
     * @param style   the window style
     * @param buttons the buttons to display
     */
    public PopupDialog(String title, String style, Buttons buttons) {
        super(title, style, null);

        if (buttons == Buttons.OK || buttons == Buttons.OK_CANCEL) {
            addButton(OK_ID, new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    onOK();
                }
            });
        }
        if (buttons == Buttons.CANCEL || buttons == Buttons.OK_CANCEL) {
            addButton(CANCEL_ID, new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    onCancel();
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
     * Invoked when the OK button is pressed. This sets the action and closes
     * the window.
     */
    protected void onOK() {
        setAction(OK_ID);
        close();
    }

    /**
     * Invoked when the cancel button is pressed. This sets the action and
     * closes the window.
     */
    protected void onCancel() {
        setAction(CANCEL_ID);
        close();
    }

}
