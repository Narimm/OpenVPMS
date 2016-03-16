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

import echopointng.LabelEx;
import echopointng.table.TableCellRendererEx;
import echopointng.xhtml.XhtmlFragment;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.Table;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.factory.TableFactory;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.echo.table.TableHelper;
import org.openvpms.web.workspace.workflow.scheduling.Schedule;
import org.openvpms.web.workspace.workflow.scheduling.ScheduleTableCellRenderer;

/**
 * Renderer for the cage/schedule column of {@link DefaultCageTableModel}.
 *
 * @author Tim Anderson
 */
class CageScheduleCellRenderer implements TableCellRendererEx {

    /**
     * Returns a {@code XhtmlFragment} that will be displayed as the content at the specified co-ordinate in the table.
     *
     * @param table  the {@code Table} for which the rendering is occurring
     * @param value  the value retrieved from the {@code TableModel} for the specified coordinate
     * @param column the column index to render
     * @param row    the row index to render
     * @return a {@code XhtmlFragment} representation of the value
     */
    @Override
    public XhtmlFragment getTableCellRendererContent(Table table, Object value, int column, int row) {
        return null;
    }

    /**
     * This method allows you to "restrict" the cells (within a row) that will
     * cause selection of the row to occur. By default any cell will cause
     * selection of a row. If this methods returns false then only certain cells
     * within the row will cause selection when clicked on.
     *
     * @param table  the table
     * @param column the column
     * @param row    the row
     * @return {@code true} if the cell causes selection
     */
    @Override
    public boolean isSelectionCausingCell(Table table, int column, int row) {
        return false;
    }

    /**
     * This method is called to determine which cells within a row can cause an
     * action to be raised on the server when clicked.
     * <p>
     * By default if a Table has attached actionListeners then any click on any
     * cell within a row will cause the action to fire.
     * <p>
     * This method allows this to be overridden and only certain cells within a
     * row can cause an action event to be raise.
     *
     * @param table  the Table in question
     * @param column the column in question
     * @param row    the row in question
     * @return true means that the cell can cause actions while false means the cells can not cause action events.
     */
    @Override
    public boolean isActionCausingCell(Table table, int column, int row) {
        CageTableModel model = (CageTableModel) table.getModel();
        CageRow cageRow = model.getCageRow(row);
        return cageRow.isSummary() && !cageRow.isTotal();
    }

    /**
     * Returns a component that will be displayed at the specified coordinate in
     * the table.
     *
     * @param table  the {@code Table} for which the rendering is occurring
     * @param value  the value retrieved from the {@code TableModel} for
     *               the specified coordinate
     * @param column the column index to render
     * @param row    the row index to render
     * @return a component representation  of the value.
     */
    @Override
    public Component getTableCellRendererComponent(Table table, Object value, int column, int row) {
        Component result;
        CageRow cageRow = (CageRow) value;
        Label name = LabelFactory.create(null, Styles.BOLD);
        name.setText(cageRow.getName());
        if (cageRow.isTotal()) {
            result = getTotal(table, name);
        } else if (cageRow.isSummary()) {
            result = getCageType(cageRow, name);
        } else {
            result = getSchedule(table, cageRow, name, row);
        }
        return result;
    }

    /**
     * Returns a component representing the total no. of schedules.
     *
     * @param table the table
     * @param name  the total name
     * @return the component
     */
    private Component getTotal(Table table, Label name) {
        Component result;
        CageTableModel model = (CageTableModel) table.getModel();
        Label count = getCount(model.getGrid().getScheduleCount());
        LabelEx spacer = new LabelEx(new XhtmlFragment(TableHelper.SPACER));
        spacer.setStyleName("CageTable.spacer"); // to align the totals

        Row right = RowFactory.create(Styles.CELL_SPACING, RowFactory.rightAlign(), count, spacer);
        result = RowFactory.create(Styles.CELL_SPACING, name, right);
        TableHelper.mergeStyle(result, ScheduleTableCellRenderer.EVEN_ROW_STYLE);
        return result;
    }

    /**
     * Returns a component representing the cage type.
     *
     * @param cageRow the row
     * @param name    the cage type name
     * @return the component
     */
    private Component getCageType(CageRow cageRow, Label name) {
        Component result;
        CageScheduleGroup group = cageRow.getGroup();
        Label count = getCount(group.getScheduleCount());
        LabelEx action = new LabelEx();
        if (group.isExpanded()) {
            action.setStyleName("CageTable.minus");
        } else {
            action.setStyleName("CageTable.plus");
        }
        Row right = RowFactory.create(Styles.CELL_SPACING, RowFactory.rightAlign(), count, action);
        result = RowFactory.create(Styles.CELL_SPACING, name, right);
        TableHelper.mergeStyle(result, ScheduleTableCellRenderer.EVEN_ROW_STYLE);
        return result;
    }

    /**
     * Returns a component representing a schedule.
     *
     * @param table   the table
     * @param cageRow the row
     * @param name    the schedule name
     * @param row     the table row
     * @return the component
     */
    private Component getSchedule(Table table, CageRow cageRow, Label name, int row) {
        String styleName = cageRow.renderEven() ? "ScheduleTable.Even-InsetX" : "ScheduleTable.Odd-InsetX";
        Entity schedule = cageRow.getSchedule().getSchedule();
        row++;
        int span = 1;
        CageTableModel model = (CageTableModel) table.getModel();
        while (row < model.getRowCount()) {
            Schedule next = model.getSchedule(0, row);
            if (next != null && next.getSchedule().equals(schedule)) {
                span++;
                row++;
            } else {
                break;
            }
        }
        if (span > 1) {
            name.setLayoutData(TableFactory.rowSpan(span));
        }
        TableHelper.mergeStyle(name, styleName);
        return name;
    }

    /**
     * Renders a count as a bold label.
     *
     * @param count the count
     * @return the label
     */
    private Label getCount(int count) {
        Label label = LabelFactory.create(null, Styles.BOLD);
        label.setText(Integer.toString(count));
        return label;
    }
}
