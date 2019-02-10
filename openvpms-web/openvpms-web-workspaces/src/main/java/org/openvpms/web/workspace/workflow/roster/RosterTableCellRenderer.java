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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.roster;

import echopointng.BalloonHelp;
import echopointng.table.TableCellRendererEx;
import echopointng.xhtml.XhtmlFragment;
import nextapp.echo2.app.Color;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Font;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.Table;
import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.workflow.ScheduleEvent;
import org.openvpms.archetype.rules.workflow.roster.RosterEvent;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.table.Cell;
import org.openvpms.web.echo.table.TableHelper;
import org.openvpms.web.resource.i18n.format.DateFormatter;

import java.util.Date;

/**
 * Cell renderer for {@link UserRosterTableModel}.
 *
 * @author Tim Anderson
 */
class RosterTableCellRenderer implements TableCellRendererEx {

    /**
     * The key to extract the entity name from events.
     */
    private final String key;

    /**
     * The table model.
     */
    private final RosterTableModel model;

    /**
     * The context.
     */
    private final Context context;

    /**
     * Constructs a {@link RosterTableCellRenderer}.
     *
     * @param key     the key to extract entity names from sets
     * @param model   the table model
     * @param context the context
     */
    public RosterTableCellRenderer(String key, RosterTableModel model, Context context) {
        this.key = key;
        this.model = model;
        this.context = context;
    }

    /**
     * Returns a component that will be displayed at the specified coordinate in
     * the table.
     *
     * @param table  the {@code Table} for which the rendering is occurring
     * @param value  the value retrieved from the {@code TableModel} for the specified coordinate
     * @param column the column index to render
     * @param row    the row index to render
     * @return a component representation  of the value.
     */
    public Component getTableCellRendererComponent(Table table, Object value, int column, int row) {
        Component component;
        if (value instanceof PropertySet) {
            component = getEvent((PropertySet) value, column);
        } else {
            component = getComponent(value, column, row);
        }
        if (isCut(column, row)) {
            cutCell(table, component);
        }
        if (isSelected(column, row)) {
            highlightCell(component);
        }
        return component;
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
    public boolean isSelectionCausingCell(Table table, int column, int row) {
        return column != 0;
    }

    /**
     * This method is called to determine which cells within a row can cause an
     * action to be raised on the server when clicked.
     * <p>
     * By default if a Table has attached actionListeners then any click on any
     * cell within a row will cause the action to fire.
     * <p>
     * This method allows this to be overrriden and only certain cells within a
     * row can cause an action event to be raise.
     *
     * @param table  the Table in question
     * @param column the column in question
     * @param row    the row in quesiton
     * @return true means that the cell can cause actions while false means the cells can not cause action events.
     */
    public boolean isActionCausingCell(Table table, int column, int row) {
        return column != 0;
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
        return null;
    }

    /**
     * Returns a component representing an event.
     *
     * @param event  the event
     * @param column the column
     * @return the component
     */
    protected Component getEvent(PropertySet event, int column) {
        Component result;
        Label next = null;
        Label previous = null;
        Date from = event.getDate(ScheduleEvent.ACT_START_TIME);
        Date to = event.getDate(ScheduleEvent.ACT_END_TIME);
        Date date = model.getDate(column);
        if (from.compareTo(date) < 0) {
            previous = LabelFactory.create(null, "navigation.previous");
            from = date;
        }
        StringBuilder buffer = new StringBuilder();
        buffer.append(DateFormatter.formatTime(from, false));
        buffer.append(" - ");
        Date following = DateRules.getNextDate(date);
        if (to.compareTo(following) > 0) {
            next = LabelFactory.create(null, "navigation.next");
            buffer.append(DateFormatter.formatTime(following, false));
        } else {
            buffer.append(DateFormatter.formatTime(to, false));
        }
        Label times = LabelFactory.text(buffer.toString());
        Label entity = LabelFactory.text(event.getString(key));
        Column component = ColumnFactory.create(times, entity);
        if (!ObjectUtils.equals(context.getLocation().getObjectReference(),
                                event.getReference(RosterEvent.LOCATION_REFERENCE))) {
            component.add(LabelFactory.text(event.getString(RosterEvent.LOCATION_NAME)));
        }

        if (previous != null || next != null) {
            Row container = RowFactory.create();
            if (previous != null) {
                container.add(previous);
            }
            container.add(component);
            if (next != null) {
                container.add(next);
            }
            result = container;
        } else {
            result = component;
        }
        return result;
    }

    /**
     * Returns a component for a value.
     *
     * @param value  the value retrieved from the {@code TableModel} for the
     *               specified coordinate
     * @param column the column
     * @param row    the row
     * @return a component representation of the value
     */
    protected Component getComponent(Object value, int column, int row) {
        Component component;
        if (value == null) {
            // free slot.
            if (isSelected(column, row)) {
                // render a 'New' prompt
                component = LabelFactory.create("workflow.scheduling.table.new");
            } else {
                component = LabelFactory.create();
            }
        } else {
            component = LabelFactory.text(value.toString());
        }
        return component;
    }

    /**
     * Determines if a cell is selected.
     *
     * @param column the column
     * @param row    the row
     * @return {@code true} if the cell is selected 'cut'
     */
    protected boolean isSelected(int column, int row) {
        Cell cell = model.getSelected();
        return (cell != null && row == cell.getRow() && column == cell.getColumn());
    }

    /**
     * Determines if a cell has been 'cut'.
     *
     * @param column the column
     * @param row    the row
     * @return {@code true} if the cell has been 'cut'
     */
    protected boolean isCut(int column, int row) {
        return model.isCut() && model.isMarked(column, row);
    }

    /**
     * Highlights a cell component, used to highlight the selected cell.
     * <p>
     * Ideally this would be done by the table, however none of the tables support cell selection.
     *
     * @param component the cell component
     */
    protected void highlightCell(Component component) {
        TableHelper.mergeStyle(component, "ScheduleTable.Selected", true);
        Color colour = component.getForeground();
        if (colour != null && component.getComponentCount() != 0) {
            setForeground(component, colour);
        }
    }

    /**
     * Marks a cell as being cut.
     *
     * @param table     the table
     * @param component the cell component
     */
    protected void cutCell(Table table, Component component) {
        setStrikethroughFont(component, table);
    }


    /**
     * Helper to return a font for a component, navigating up the component hierarchy if one isn't found on the
     * specified component.
     *
     * @param component the component
     * @return the font, or {@code null} if none is found
     */
    protected Font getFont(Component component) {
        Font font = component.getFont();
        if (font == null) {
            font = (Font) component.getRenderProperty(Component.PROPERTY_FONT);
            if (font == null && component.getParent() != null) {
                font = getFont(component.getParent());
            }
        }
        return font;
    }


    /**
     * Sets the font on a component to use strike-through.
     * <p>
     * This sets the font on each nested component, to avoid font inheritance issues on Chrome.
     *
     * @param component the component
     * @param table     the parent table
     */
    private void setStrikethroughFont(Component component, Table table) {
        Font font = getFont(table);
        if (font != null) {
            int style = Font.BOLD | Font.LINE_THROUGH;
            font = new Font(font.getTypeface(), style, font.getSize());
            setFont(component, font);
        }
    }

    /**
     * Recursively sets the font on a component and its children.
     *
     * @param component the component
     * @param font      the font
     */
    private void setFont(Component component, Font font) {
        for (Component child : component.getComponents()) {
            setFont(child, font);
        }
        component.setFont(font);
    }

    private void setForeground(Component component, Color colour) {
        if (component instanceof Row) {
            for (Component child : component.getComponents()) {
                setForeground(child, colour);
            }
        } else if (!(component instanceof BalloonHelp)) {
            component.setForeground(colour);
        }
    }

}
