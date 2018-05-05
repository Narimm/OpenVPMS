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

import echopointng.layout.TableLayoutDataEx;
import echopointng.xhtml.XhtmlFragment;
import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.Table;
import nextapp.echo2.app.layout.RowLayoutData;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.workflow.ScheduleEvent;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.factory.TableFactory;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.echo.table.TableHelper;
import org.openvpms.web.workspace.workflow.scheduling.Schedule;
import org.openvpms.web.workspace.workflow.scheduling.ScheduleEventGrid;
import org.openvpms.web.workspace.workflow.scheduling.ScheduleTableModel;

import java.util.Date;

import static org.openvpms.web.workspace.workflow.scheduling.ScheduleEventGrid.Availability.UNAVAILABLE;

/**
 * TableCellRender for {@link AbstractMultiDayTableModel}.
 *
 * @author Tim Anderson
 */
public abstract class AbstractMultiDayTableCellRenderer extends AbstractAppointmentTableCellRender {

    /**
     * Constructs an {@link AbstractMultiDayTableCellRenderer}.
     *
     * @param model the table model
     */
    public AbstractMultiDayTableCellRenderer(AbstractMultiDayTableModel model) {
        super(model);
    }

    /**
     * Returns the model.
     *
     * @return the model
     */
    @Override
    protected AbstractMultiDayTableModel getModel() {
        return (AbstractMultiDayTableModel) super.getModel();
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
        Component result = super.getComponent(table, value, column, row);
        if (result == null) {
            result = getFreeSlot(column, row);
        }
        return result;
    }

    /**
     * Returns a {@code XhtmlFragment} that will be displayed as the content at the specified co-ordinate in the table.
     *
     * @param table  the {@code Table} for which the rendering is occurring
     * @param value  the value retrieved from the {@code TableModel} for the specified coordinate
     * @param column the column index to render
     * @param row    the row index to render
     * @return a {@code XhtmlFragment} representation of the value
     */
    public XhtmlFragment getTableCellRendererContent(Table table, Object value, int column, int row) {
        XhtmlFragment result = TableHelper.createFragment(value);
        ScheduleTableModel model = (ScheduleTableModel) table.getModel();

        ScheduleEventGrid.Availability avail = model.getAvailability(column, row);
        String style = getStyle(avail, model, row);
        TableLayoutDataEx layout = TableHelper.getTableLayoutDataEx(style);

        if (layout != null && avail == UNAVAILABLE) {
            Schedule schedule = model.getSchedule(column, row);
            int span = model.getGrid().getUnavailableSlots(schedule, row);
            layout.setRowSpan(span);
        }
        result.setLayoutData(layout);
        return result;
    }

    /**
     * Returns the style for a free row.
     *
     * @param model the schedule table model
     * @param row   the row
     * @return a style for the row
     */
    @Override
    protected String getFreeStyle(ScheduleTableModel model, int row) {
        Schedule schedule = model.getSchedule(0, row);
        return (schedule.getRenderEven()) ? EVEN_ROW_STYLE : ODD_ROW_STYLE;
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
        Component result = super.getEvent(table, event, column, row);
        Label next = null;
        Label previous = null;
        Date startTime = event.getDate(ScheduleEvent.ACT_START_TIME);
        int slot = column - 1; // first column is the schedule
        AbstractMultiDayTableModel model = getModel();
        AbstractMultiDayScheduleGrid grid = model.getGrid();
        if (DateRules.compareDates(startTime, grid.getDate(slot)) < 0) {
            previous = LabelFactory.create(null, "navigation.previous");
        }
        int span = grid.getSlots(event, column - 1);
        if (span > 1) {
            if (column + span > model.getColumnCount()) {
                next = LabelFactory.create(null, "navigation.next");
                RowLayoutData newValue = new RowLayoutData();
                newValue.setAlignment(Alignment.ALIGN_RIGHT);
                newValue.setWidth(Styles.FULL_WIDTH);
                next.setLayoutData(newValue);
            }
        }
        if (previous != null || next != null) {
            Row container = RowFactory.create();
            if (previous != null) {
                container.add(previous);
            }
            container.add(result);
            if (next != null) {
                container.add(next);
            }
            result = container;
        }
        if (span > 1) {
            result.setLayoutData(TableFactory.columnSpan(span));
        }
        styleEvent(event, result, table);
        return result;
    }

    /**
     * Renders a free slot.
     *
     * @param column the column
     * @param row    the row
     * @return a component representing the free slot
     */
    protected Component getFreeSlot(int column, int row) {
        Component result;
        AbstractMultiDayTableModel model = getModel();
        result = LabelFactory.create();
        String style = getFreeStyle(model, row);
        TableLayoutDataEx layout = TableHelper.getTableLayoutDataEx(style);
        layout.setRowSpan(model.getRows(column, row));
        result.setLayoutData(layout);
        return result;
    }

    /**
     * Determines if the cell can be highlighted.
     *
     * @param column the column
     * @param row    the row
     * @param value  the value at the cell
     * @return {@code true} if the cell can be highlighted
     */
    @Override
    protected boolean canHighlightCell(int column, int row, Object value) {
        boolean highlight = false;
        if (getModel().isSelected(column, row) && value instanceof PropertySet) {
            highlight = true;
        }
        return highlight;
    }
}
