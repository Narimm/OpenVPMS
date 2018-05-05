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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.appointment.boarding;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Table;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.workflow.ScheduleEvent;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.workspace.workflow.appointment.AbstractMultiDayScheduleGrid;
import org.openvpms.web.workspace.workflow.appointment.AbstractMultiDayTableModel;
import org.openvpms.web.workspace.workflow.scheduling.Schedule;

import java.util.Date;

/**
 * A table cell renderer for {@link CageSummaryTableModel}.
 *
 * @author Tim Anderson
 */
class CageSummaryTableCellRenderer extends AbstractCageTableCellRenderer {

    /**
     * Indicates a pet staying for the day.
     */
    private final String day;

    /**
     * Indicates a pet staying overnight.
     */
    private final String overnight;

    /**
     * Indicates a pet that has stayed overnight and is checking out.
     */
    private final String checkout;


    /**
     * Constructs a {@link CageSummaryTableCellRenderer}.
     *
     * @param model the table model
     */
    public CageSummaryTableCellRenderer(CageSummaryTableModel model) {
        super(model);
        day = Messages.get("workflow.scheduling.appointment.summary.day");
        overnight = Messages.get("workflow.scheduling.appointment.summary.overnight");
        checkout = Messages.get("workflow.scheduling.appointment.summary.checkout");
    }

    /**
     * Returns a component representing an event.
     *
     * @param table  the table
     * @param event  the event
     * @param column the column
     * @param row    the row
     * @return the component
     */
    @Override
    protected Component getEvent(Table table, PropertySet event, int column, int row) {
        Date startTime = event.getDate(ScheduleEvent.ACT_START_TIME);
        Date endTime = event.getDate(ScheduleEvent.ACT_END_TIME);
        int slot = column - 1; // first column is the schedule
        AbstractMultiDayTableModel model = getModel();
        AbstractMultiDayScheduleGrid grid = model.getGrid();
        Label label = LabelFactory.create();
        if (!Schedule.isBlockingEvent(event)) {
            Date startDate = DateRules.getDate(startTime);
            Date endDate = DateRules.getDate(endTime);
            if (startDate.equals(endDate) || DateRules.compareTo(endTime, DateRules.getNextDate(startDate)) == 0) {
                // if the appointment is on the same day, or ends a midnight, it is a day board
                label.setText(day);
            } else {
                Date date = grid.getDate(slot);
                if (date.compareTo(endDate) < 0) {
                    label.setText(overnight);
                } else {
                    label.setText(checkout);
                }
            }
        }
        styleEvent(event, label, table);
        return label;
    }

}
