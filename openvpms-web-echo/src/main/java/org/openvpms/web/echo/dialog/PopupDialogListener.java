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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.echo.dialog;

import nextapp.echo2.app.event.WindowPaneEvent;
import org.openvpms.web.component.event.WindowPaneListener;
import org.openvpms.web.component.util.ErrorHelper;


/**
 * Listener for {@link PopupDialog} window close events.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class PopupDialogListener extends WindowPaneListener {

    /**
     * Invoked when a user attempts to close a <tt>WindowPane</tt>.
     *
     * @param event the <tt>WindowPaneEvent</tt> describing the change
     */
    public void onClose(WindowPaneEvent event) {
        if (event.getSource() instanceof PopupDialog) {
            PopupDialog dialog = (PopupDialog) event.getSource();
            try {
                onAction(dialog);
            } catch (Throwable exception) {
                ErrorHelper.show(exception);
            }
        }
    }

    /**
     * Invoked when the 'OK' button is pressed.
     */
    public void onOK() {
    }

    /**
     * Invoked when the 'cancel' button is pressed.
     */
    public void onCancel() {
    }

    /**
     * Invoked when the 'yes' button is pressed.
     */
    public void onYes() {
    }

    /**
     * Invoked when the 'no' button is pressed.
     */
    public void onNo() {
    }

    /**
     * Invoked when the 'skip' button is pressed.
     */
    public void onSkip() {
    }

    /**
     * Invoked when the 'apply' button is pressed.
     */
    public void onApply() {
    }

    /**
     * Invoked when the 'retry' button is pressed.
     */
    public void onRetry() {
    }

    /**
     * Invoked when an unknown button is selected.
     *
     * @param action the dialog action
     */
    public void onAction(String action) {
    }

    /**
     * Invoked when a dialog closes.
     * <p/>
     * Examines the {@link PopupDialog#getAction() dialog action} and
     * invokes the appropriate <em>on&lt;Action&gt;</em> method above.
     *
     * @param dialog the dialog
     */
    protected void onAction(PopupDialog dialog) {
        String action = dialog.getAction();
        if (PopupDialog.OK_ID.equals(action)) {
            onOK();
        } else if (PopupDialog.CANCEL_ID.equals(action)) {
            onCancel();
        } else if (PopupDialog.YES_ID.equals(action)) {
            onYes();
        } else if (PopupDialog.NO_ID.equals(action)) {
            onNo();
        } else if (PopupDialog.SKIP_ID.equals(action)) {
            onSkip();
        } else if (PopupDialog.APPLY_ID.equals(action)) {
            onApply();
        } else if (PopupDialog.RETRY_ID.equals(action)) {
            onRetry();
        } else {
            onAction(action);
        }
    }

}
