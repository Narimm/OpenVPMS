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
import nextapp.echo2.app.event.WindowPaneListener;
import org.openvpms.web.component.dialog.ConfirmationDialog;
import org.openvpms.web.component.dialog.PopupDialog;


/**
 * An {@link EvalTask} task that pops up a Yes/No/Cancel confirmation dialog,
 * evaluating to <code>true</code> if Yes is selected, or <code>false</code>
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
     * Creates a new <code>ConfirmationTask</code>.
     *
     * @param title   the dialog title
     * @param message the dialog message
     */
    public ConfirmationTask(String title, String message) {
        this.title = title;
        this.message = message;
    }

    /**
     * Starts the task.
     * <p/>
     * The registered {@link TaskListener} will be notified on completion or
     * failure.
     *
     * @param context the task context
     */
    public void start(TaskContext context) {
        final ConfirmationDialog dialog = new ConfirmationDialog(
                title, message, PopupDialog.YES_NO_CANCEL);
        dialog.addWindowPaneListener(new WindowPaneListener() {
            public void windowPaneClosing(WindowPaneEvent event) {
                String action = dialog.getAction();
                if (ConfirmationDialog.YES_ID.equals(action)) {
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
