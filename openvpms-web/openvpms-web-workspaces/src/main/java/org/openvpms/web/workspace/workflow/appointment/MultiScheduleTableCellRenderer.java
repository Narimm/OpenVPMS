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

package org.openvpms.web.workspace.workflow.appointment;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Table;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.web.echo.factory.TableFactory;
import org.openvpms.web.workspace.workflow.scheduling.Schedule;

/**
 * TableCellRenderer for {@link MultiScheduleTableModel}.
 *
 * @author Tim Anderson
 */
public class MultiScheduleTableCellRenderer extends AppointmentTableCellRenderer {

    /**
     * Constructs a {@link MultiScheduleTableCellRenderer}.
     *
     * @param model the table model
     */
    public MultiScheduleTableCellRenderer(MultiScheduleTableModel model) {
        super(model);
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
    protected Component getEvent(Table table, PropertySet event, int column, int row) {
        Schedule schedule = getModel().getSchedule(column, row);
        Component component = super.getEvent(table, event, column, row);
        AppointmentGrid grid = getModel().getGrid();
        int rowSpan = grid.getSlots(event, schedule, row);
        if (rowSpan > 1) {
            component.setLayoutData(TableFactory.rowSpan(rowSpan));
        }
        styleEvent(event, component, table);
        return component;
    }


}
