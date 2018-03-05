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

package org.openvpms.web.workspace.workflow.appointment;

import org.openvpms.archetype.rules.workflow.ScheduleEvent;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.workspace.workflow.scheduling.Schedule;
import org.openvpms.web.workspace.workflow.scheduling.ScheduleColours;
import org.openvpms.web.workspace.workflow.scheduling.ScheduleEventGrid;
import org.openvpms.web.workspace.workflow.scheduling.ScheduleTableModel;

import java.util.Date;

/**
 * Appointment table model for appointments that span multiple days.
 *
 * @author Tim Anderson
 */
public abstract class AbstractMultiDayTableModel extends ScheduleTableModel {

    /**
     * The schedule column index.
     */
    public static final int SCHEDULE_INDEX = 0;

    /**
     * Constructs an {@link AbstractMultiDayTableModel}.
     *
     * @param grid    the appointment grid
     * @param context the context
     * @param colours the colour cache
     */
    public AbstractMultiDayTableModel(ScheduleEventGrid grid, Context context, ScheduleColours colours) {
        super(grid, context, false, colours);
    }

    /**
     * Returns the slot of the specified event.
     *
     * @param schedule the schedule
     * @param eventRef the event reference
     * @return the slot, or {@code -1} if the event is not found
     */
    @Override
    public int getSlot(Schedule schedule, IMObjectReference eventRef) {
        PropertySet event = schedule.getEvent(eventRef);
        if (event != null) {
            return getGrid().getSlot(event.getDate(ScheduleEvent.ACT_START_TIME));
        }
        return -1;
    }

    /**
     * Returns the grid.
     *
     * @return the grid
     */
    @Override
    public AbstractMultiDayScheduleGrid getGrid() {
        return (AbstractMultiDayScheduleGrid) super.getGrid();
    }

    /**
     * Returns the number of rows spanned by a cell.
     *
     * @param column the cell column
     * @param row    the cell row
     * @return the number of spanned rows
     */
    public abstract int getRows(int column, int row);

    /**
     * Returns the slot of a cell.
     *
     * @param column the column
     * @param row    the row
     * @return the slot
     */
    @Override
    public int getSlot(int column, int row) {
        return column - 1;
    }

    /**
     * Returns the cell column corresponding to a slot.
     *
     * @param slot the slot
     * @return the column
     */
    @Override
    protected int getCellColumn(int slot) {
        return slot + 1;
    }

    /**
     * Date column.
     */
    protected static class DateColumn extends Column {

        public DateColumn(int modelIndex, Date startTime) {
            super(modelIndex, null);
            setHeaderValue(startTime);
            setHeaderRenderer(MultiDayTableHeaderRenderer.INSTANCE);
        }
    }

}
