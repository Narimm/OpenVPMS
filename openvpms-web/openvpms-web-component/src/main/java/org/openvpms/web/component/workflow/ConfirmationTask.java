/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.workflow;

import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.help.HelpContext;


/**
 * An {@link EvalTask} task that pops up a Yes/No/Cancel or OK/Cancel
 * confirmation dialog.
 * <p/>
 * It evaluates {@code true} if Yes/OK is selected, or {@code false}
 * if No is selected. Selecting Cancel cancels the task.
 *
 * @author Tim Anderson
 */
public class ConfirmationTask extends AbstractConfirmationTask {

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
     * Constructs a {@link ConfirmationTask}.
     *
     * @param title   the dialog title
     * @param message the dialog message
     * @param help    the help context
     */
    public ConfirmationTask(String title, String message, HelpContext help) {
        this(title, message, true, help);
    }

    /**
     * Constructs a {@code ConfirmationTask}.
     *
     * @param title     the dialog title
     * @param message   the dialog message
     * @param displayNo determines if the 'No' button should be displayed
     * @param help      the help context
     */
    public ConfirmationTask(String title, String message, boolean displayNo, HelpContext help) {
        super(help);
        this.title = title;
        this.message = message;
        this.displayNo = displayNo;
    }

    /**
     * Creates a new confirmation dialog.
     *
     * @param help the help context
     * @return a new confirmation dialog
     */
    @Override
    protected ConfirmationDialog createConfirmationDialog(HelpContext help) {
        String[] buttons = (displayNo) ? PopupDialog.YES_NO_CANCEL : PopupDialog.OK_CANCEL;
        return new ConfirmationDialog(title, message, buttons, help);
    }
}
