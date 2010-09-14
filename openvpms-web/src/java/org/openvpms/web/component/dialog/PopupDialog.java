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

import nextapp.echo2.app.Button;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.component.focus.FocusGroup;
import org.openvpms.web.component.util.VetoListener;
import org.openvpms.web.component.util.Vetoable;
import org.openvpms.web.component.util.ErrorHelper;


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
     * Close button identifier.
     */
    public static final String CLOSE_ID = "close";

    /**
     * Apply button identifier.
     */
    public static final String APPLY_ID = "apply";

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
     * Retry button identifier.
     */
    public static final String RETRY_ID = "retry";

    /**
     * Helper to create a button row containing the OK button.
     */
    public static final String[] OK = {OK_ID};

    /**
     * Helper to create a button row containing the CLOSE button.
     */
    public static final String[] CLOSE = {CLOSE_ID};

    /**
     * Helper to create a button row containing the CANCEL button.
     */
    public static final String[] CANCEL = {CANCEL_ID};

    /**
     * Helper to create a button row containing the OK and CANCEL buttons.
     */
    public static final String[] OK_CANCEL = {OK_ID, CANCEL_ID};

    /**
     * Helper to create a button row containing the OK, SKIP and CANCEL buttons.
     */
    public static final String[] OK_SKIP_CANCEL = {OK_ID, SKIP_ID, CANCEL_ID};

    /**
     * Helper to create a button row containing the SKIP and CANCEL buttons.
     */
    public static final String[] SKIP_CANCEL = {SKIP_ID, CANCEL_ID};

    /**
     * Helper to create a button row containing the YES, NO and CANCEL buttons.
     */
    public static final String[] YES_NO_CANCEL = {YES_ID, NO_ID, CANCEL_ID};

    /**
     * Helper to create a button row containing the APPLY, OK, and CANCEL
     * buttons.
     */
    public static final String[] APPLY_OK_CANCEL
            = {APPLY_ID, OK_ID, CANCEL_ID};

    /**
     * Helper to craete a button row containing the RETRY and CANCEL buttons.
     */
    public static final String[] RETRY_CANCEL = {RETRY_ID, CANCEL_ID};

    /**
     * The dialog action. May be <tt>null</tt>
     */
    private String action;

    /**
     * The listener to veto cancel events. May be <tt>null</tt>
     */
    private VetoListener cancelListener;


    /**
     * Construct a new <tt>PopupDialog</tt>.
     *
     * @param title   the window title
     * @param buttons the buttons to display
     */
    public PopupDialog(String title, String[] buttons) {
        this(title, null, buttons);
    }

    /**
     * Construct a new <tt>PopupDialog</tt>.
     *
     * @param title   the window title
     * @param style   the window style. May be <tt>null</tt>
     * @param buttons the buttons to display
     */
    public PopupDialog(String title, String style, String[] buttons) {
        this(title, style, buttons, null);
    }

    /**
     * Construct a new <tt>PopupDialog</tt>.
     *
     * @param title   the window title. May be <tt>null</tt>
     * @param style   the window style. May be <tt>null</tt>
     * @param buttons the buttons to display
     * @param focus   the focus group. May be <tt>null</tt>
     */
    public PopupDialog(String title, String style, String[] buttons,
                       FocusGroup focus) {
        super(title, style, focus);

        for (String button : buttons) {
            addButton(button, false);
        }
    }

    /**
     * Returns the dialog action.
     *
     * @return the dialog action
     */
    public String getAction() {
        return action;
    }

    /**
     * Sets a listener to veto cancel events.
     *
     * @param listener the listener. May be <tt>null</tt>
     */
    public void setCancelListener(VetoListener listener) {
        cancelListener = listener;
    }

    /**
     * Sets the dialog action.
     *
     * @param action the action
     */
    protected void setAction(String action) {
        this.action = action;
    }

    /**
     * Invoked when a button is pressed. This delegates to the appropriate
     * on*() method for the button if it is known, else sets the action to
     * the button identifier and closes the window.
     *
     * @param button the button identifier
     */
    protected void onButton(String button) {
        try {
            if (OK_ID.equals(button)) {
                onOK();
            } else if (CLOSE_ID.equals(button)) {
                onClose();
            } else if (CANCEL_ID.equals(button)) {
                onCancel();
            } else if (YES_ID.equals(button)) {
                onYes();
            } else if (NO_ID.equals(button)) {
                onNo();
            } else if (SKIP_ID.equals(button)) {
                onSkip();
            } else if (APPLY_ID.equals(button)) {
                onApply();
            } else if (RETRY_ID.equals(button)) {
                onRetry();
            } else {
                setAction(button);
                close();
            }
        } catch (Throwable exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Invoked when the 'OK' button is pressed. This sets the action and closes
     * the window.
     */
    protected void onOK() {
        close(OK_ID);
    }

    /**
     * Invoked when the 'close' button is pressed. This sets the action to CLOSE and closes the window.
     */
    protected void onClose() {
        close(CLOSE_ID);
    }

    /**
     * Invoked when the 'cancel' button is pressed. If a {@link VetoListener}
     * has been registered, this will be notified, otherwise the action will
     * be set and the window closed.
     */
    protected void onCancel() {
        if (cancelListener != null) {
            cancelListener.onVeto(new Vetoable() {
                public void veto(boolean veto) {
                    if (!veto) {
                        close(CANCEL_ID);
                    }
                }
            });
        } else {
            close(CANCEL_ID);
        }
    }

    /**
     * Invoked when the 'yes' button is pressed. This sets the action and closes
     * the window.
     */
    protected void onYes() {
        close(YES_ID);
    }

    /**
     * Invoked when the 'no' button is pressed. This sets the action and closes
     * the window.
     */
    protected void onNo() {
        close(NO_ID);
    }

    /**
     * Invoked when the 'skip' button is pressed. This sets the action and
     * closes the window.
     */
    protected void onSkip() {
        close(SKIP_ID);
    }

    /**
     * Invoked when the 'apply' button is pressed. This sets the action and
     * closes the window.
     */
    protected void onApply() {
        close(APPLY_ID);
    }

    /**
     * Invoked when the 'retry' button is pressed. This sets the action and
     * closes the window.
     */
    protected void onRetry() {
        close(RETRY_ID);
    }

    /**
     * Sets the action and closes the window.
     *
     * @param action the action
     */
    protected void close(String action) {
        setAction(action);
        close();
    }

    /**
     * Adds a new button.
     *
     * @param id              the button identifier
     * @param disableShortcut if <tt>true</tt> disable any keyboard shortcut
     * @return the new button
     */
    protected Button addButton(final String id, boolean disableShortcut) {
        ActionListener listener = new ActionListener() {
            public void onAction(ActionEvent event) {
                onButton(id);
            }
        };
        return addButton(id, listener, disableShortcut);
    }

}
