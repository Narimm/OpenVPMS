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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.app.workflow.scheduling;

import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.help.HelpContext;


/**
 * Dialog to edit the expression of a schedule view.
 *
 * @author Tim Anderson
 */
public class ScheduleViewExpressionDialog extends PopupDialog {

    /**
     * The expression editor.
     */
    private final ScheduleViewExpressionEditor editor;


    /**
     * Constructs a {@code ScheduleViewExpressionDialog}.
     *
     * @param title  the window title
     * @param editor the expression editor
     * @param help   the help context
     */
    public ScheduleViewExpressionDialog(String title, ScheduleViewExpressionEditor editor, HelpContext help) {
        super(title, "EditDialog", new String[]{"test", OK_ID}, help);
        getLayout().add(editor.getComponent());
        setModal(true);
        this.editor = editor;
    }

    /**
     * Invoked when a button is pressed.
     *
     * @param button the button identifier
     */
    @Override
    protected void onButton(String button) {
        if (button.equals("test")) {
            editor.test();
        } else {
            super.onButton(button);
        }
    }

}
