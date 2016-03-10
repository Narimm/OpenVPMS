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
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.echo.table.TableHelper;
import org.openvpms.web.workspace.workflow.appointment.AbstractMultiDayTableCellRenderer;
import org.openvpms.web.workspace.workflow.scheduling.Schedule;
import org.openvpms.web.workspace.workflow.scheduling.ScheduleEventGrid;

/**
 * A table cell renderer for {@link CageTableModel}.
 *
 * @author Tim Anderson
 */
class CageTableCellRenderer extends AbstractMultiDayTableCellRenderer {

    /**
     * Constructs a {@link CageTableCellRenderer}.
     *
     * @param model the table model
     */
    public CageTableCellRenderer(CageTableModel model) {
        super(model);
    }

    /**
     * Returns the model.
     *
     * @return the model
     */
    @Override
    protected CageTableModel getModel() {
        return (CageTableModel) super.getModel();
    }

    /**
     * Returns a component for a value.
     *
     * @param table  the {@code Table} for which the rendering is occurring
     * @param value  the value retrieved from the {@code TableModel} for the specified coordinate
     * @param column the column
     * @param row    the row
     * @return a component representation of the value. May be {@code null}
     */
    @Override
    protected Component getComponent(Table table, Object value, int column, int row) {
        Component result;
        if (value instanceof CageRow) {
            result = getFreeSlots((CageRow) value, column);
        } else {
            result = super.getComponent(table, value, column, row);
        }
        return result;
    }

    /**
     * Returns a component displaying the free slots for a cage type, on a given day.
     *
     * @param cageRow the cage type
     * @param column  the column, representing the day
     * @return a component displaying the free slots
     */
    protected Component getFreeSlots(CageRow cageRow, int column) {
        int free = 0;
        CageTableModel model = getModel();
        int slot = model.getSlot(column, 0);
        if (cageRow.isTotal()) {
            for (CageScheduleGroup group : model.getGrid().getGroups()) {
                free += getFreeSlots(group, model, slot);
            }
        } else {
            free = getFreeSlots(cageRow.getGroup(), model, slot);
        }
        Label label = TableHelper.rightAlign(Integer.toString(free));
        label.setStyleName(Styles.BOLD);
        TableHelper.mergeStyle(label, "ScheduleTable.Even");
        return label;
    }

    private int getFreeSlots(CageScheduleGroup group, CageTableModel model, int slot) {
        int free = 0;
        for (Schedule schedule : group.getSchedules()) {
            if (model.getAvailability(schedule, slot) == ScheduleEventGrid.Availability.FREE) {
                ++free;
            }
        }
        return free;
    }
}
