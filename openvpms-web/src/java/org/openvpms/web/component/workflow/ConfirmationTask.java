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

package org.openvpms.web.component.workflow;

import nextapp.echo2.app.event.WindowPaneEvent;
import org.openvpms.web.component.dialog.ConfirmationDialog;
import org.openvpms.web.component.dialog.PopupDialog;
import org.openvpms.web.component.event.WindowPaneListener;


/**
 * An {@link EvalTask} task that pops up a Yes/No/Cancel or OK/Cancel
 * confirmation dialog.
 * <p/>
 * It evaluates <tt>true</tt> if Yes/OK is selected, or <tt>false</tt>
 * if No is selected. Selecting Cancel cancels the task.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ConfirmationTask extends EvalTask<Boolean> {

    /**
     * The dialog title.
     */
    private final String title;

    /**
     * The dialog message.
     */
    private final String message;

    /**
     * Determines if the No button should be displayed.
     */
    private final boolean displayNo;

    /**
     * The confirmation dialog.
     */
    private ConfirmationDialog dialog;


    /**
     * Creates a new <tt>ConfirmationTask</tt>.
     *
     * @param title   the dialog title
     * @param message the dialog message
     */
    public ConfirmationTask(String title, String message) {
        this(title, message, true);
    }

    /**
     * Creates a new <tt>ConfirmationTask</tt>.
     *
     * @param title     the dialog title
     * @param message   the dialog message
     * @param displayNo determines if the 'No' button should be displayed
     */
    public ConfirmationTask(String title, String message, boolean displayNo) {
        this.title = title;
        this.message = message;
        this.displayNo = displayNo;
    }

    /**
     * Returns the dialog.
     *
     * @return the dialog, or <tt>null</tt> if the task isn't started
     */
    public ConfirmationDialog getConfirmationDialog() {
        return dialog;
    }

    /**
     * Starts the task.
     * <p/>
     * The registered {@link TaskListener} will be notified on completion or failure.
     *
     * @param context the task context
     */
    public void start(TaskContext context) {
        String[] buttons = (displayNo) ? PopupDialog.YES_NO_CANCEL : PopupDialog.OK_CANCEL;
        dialog = new ConfirmationDialog(title, message, buttons, context.getHelpContext());
        dialog.addWindowPaneListener(new WindowPaneListener() {
            public void onClose(WindowPaneEvent event) {
                String action = dialog.getAction();
                dialog = null;
                if (ConfirmationDialog.YES_ID.equals(action) || ConfirmationDialog.OK_ID.equals(action)) {
                    setValue(true);
                } else if (ConfirmationDialog.NO_ID.equals(action)) {
                    setValue(false);
                } else {
                    notifyCancelled();
                }
            }
        });
        dialog.show();
    }
}
