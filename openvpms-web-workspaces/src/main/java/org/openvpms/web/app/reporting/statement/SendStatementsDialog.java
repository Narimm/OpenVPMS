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

package org.openvpms.web.app.reporting.statement;

import nextapp.echo2.app.CheckBox;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.factory.CheckBoxFactory;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.help.HelpContext;


/**
 * Send statements confirmation dialog.
 *
 * @author Tim Anderson
 */
class SendStatementsDialog extends ConfirmationDialog {

    /**
     * Determines if printed statements should be processed.
     */
    private final CheckBox reprint;


    /**
     * Constructs a {@code SendStatementsDialog}.
     *
     * @param title   the window title
     * @param message the message
     * @param help    the help context
     */
    public SendStatementsDialog(String title, String message, HelpContext help) {
        super(title, message, OK_CANCEL, help);
        reprint = CheckBoxFactory.create("reporting.statements.run.reprint", false);
    }

    /**
     * Determines if printed statements should be processed.
     *
     * @return {@code true} if printed statements should be processed
     */
    public boolean reprint() {
        return reprint.isSelected();
    }

    /**
     * Lays out the component prior to display.
     */
    @Override
    protected void doLayout() {
        Label message = LabelFactory.create();
        message.setText(getMessage());
        Column column = ColumnFactory.create("WideCellSpacing", message, reprint);
        Row row = RowFactory.create("Inset", column);
        getLayout().add(row);
    }
}
