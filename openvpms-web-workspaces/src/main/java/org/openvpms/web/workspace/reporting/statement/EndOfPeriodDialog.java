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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.workspace.reporting.statement;

import nextapp.echo2.app.CheckBox;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.factory.CheckBoxFactory;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;


/**
 * End of period confirmation dialog.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class EndOfPeriodDialog extends ConfirmationDialog {

    /**
     * Determines if completed invoices should be posted.
     */
    private final CheckBox postCompleted;


    /**
     * Constructs a new <tt>EndOfPeriodDialog</tt>.
     *
     * @param title   the window title
     * @param message the message
     */
    public EndOfPeriodDialog(String title, String message) {
        super(title, message, OK_CANCEL);
        postCompleted = CheckBoxFactory.create(
            "reporting.statements.eop.postCompleted", true);
    }

    /**
     * Determines if completed invoices should be posted.
     *
     * @return <tt>true</tt> if completed invoices should be posted
     */
    public boolean postCompletedInvoices() {
        return postCompleted.isSelected();
    }

    /**
     * Lays out the component prior to display.
     */
    @Override
    protected void doLayout() {
        Label message = LabelFactory.create();
        message.setText(getMessage());
        Column column = ColumnFactory.create("WideCellSpacing",
                                             message, postCompleted);
        Row row = RowFactory.create("Inset", column);
        getLayout().add(row);
    }
}
