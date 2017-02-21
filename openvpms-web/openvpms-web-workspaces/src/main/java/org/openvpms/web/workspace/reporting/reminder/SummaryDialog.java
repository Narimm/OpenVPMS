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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.reporting.reminder;

import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Label;
import org.openvpms.archetype.rules.patient.reminder.ReminderArchetypes;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.GridFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;


/**
 * Displays reminder generation summary statistics in a popup window.
 *
 * @author Tim Anderson
 */
class SummaryDialog extends PopupDialog {

    /**
     * Constructs a {@link SummaryDialog}.
     *
     * @param stats the statistics to display
     */
    public SummaryDialog(Statistics stats) {
        super(Messages.get("reporting.reminder.summary.title"), OK);
        setModal(true);

        Grid grid = GridFactory.create(2);
        add(grid, Messages.get("reporting.reminder.summary.cancel"), stats.getCancelled());
        add(grid, Messages.get("reporting.reminder.summary.errors"), stats.getErrors());

        String[] shortNames = {ReminderArchetypes.EMAIL_REMINDER, ReminderArchetypes.SMS_REMINDER,
                               ReminderArchetypes.PRINT_REMINDER, ReminderArchetypes.LIST_REMINDER,
                               ReminderArchetypes.EXPORT_REMINDER};
        for (Entity reminderType : stats.getReminderTypes()) {
            String text = reminderType.getName();
            add(grid, text, stats.getCount(reminderType, shortNames));
        }

        add(grid, ReminderArchetypes.EMAIL_REMINDER, "email", stats);
        add(grid, ReminderArchetypes.PRINT_REMINDER, "print", stats);
        add(grid, ReminderArchetypes.SMS_REMINDER, "sms", stats);
        add(grid, ReminderArchetypes.LIST_REMINDER, "list", stats);
        add(grid, ReminderArchetypes.EXPORT_REMINDER, "export", stats);

        getLayout().add(ColumnFactory.create(Styles.INSET, grid));
    }

    /**
     * Adds a summary line item to a grid.
     *
     * @param grid      the grid
     * @param shortName the reminder item short name
     * @param key       the localisation key suffix
     * @param stats     the reminder statistics
     */
    private void add(Grid grid, String shortName, String key, Statistics stats) {
        String text = Messages.get("reporting.reminder.summary." + key);
        add(grid, text, stats.getCount(shortName));
    }

    /**
     * Adds a summary line item to a grid.
     *
     * @param grid  the grid
     * @param text  the item text
     * @param count the statistics count
     */
    private void add(Grid grid, String text, int count) {
        Label label = LabelFactory.create();
        label.setText(text);
        Label value = LabelFactory.create();
        value.setText(Integer.toString(count));
        grid.add(label);
        grid.add(value);
    }
}
