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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.admin.calendar;

import echopointng.BalloonHelp;
import echopointng.layout.TableLayoutDataEx;
import echopointng.table.TableCellRendererEx;
import echopointng.xhtml.XhtmlFragment;
import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Color;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Font;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.Table;
import nextapp.echo2.app.layout.RowLayoutData;
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.workflow.ScheduleEvent;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.web.echo.colour.ColourHelper;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.factory.TableFactory;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.echo.table.Cell;
import org.openvpms.web.echo.table.TableHelper;

import java.util.Date;


/**
 * Renders events in a {@link CalendarTableModel}.
 *
 * @author Tim Anderson
 */
public class CalendarTableCellRenderer implements TableCellRendererEx {

    /**
     * The table model.
     */
    private final CalendarTableModel model;

    /**
     * The previous rendered row.
     */
    private int previousRow = -1;

    /**
     * Determines if the 'New' indicator has been rendered.
     */
    private boolean newPrompt = false;

    /**
     * Constructs a {@link CalendarTableCellRenderer}.
     *
     * @param model the table model
     */
    public CalendarTableCellRenderer(CalendarTableModel model) {
        this.model = model;
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
            component = getEvent((PropertySet) value, column, row);
        } else {
            component = getComponent(column, row);
        }
        if (component != null) {
            if (isCut(column, row)) {
                cutCell(table, component);
            } else if (canHighlightCell(column, row, value)) {
                // highlight the selected cell.
                highlightCell(component);
            }
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
        return true;
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
        return true;
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
     * Returns the table model.
     *
     * @return the table model
     */
    protected CalendarTableModel getModel() {
        return model;
    }

    /**
     * Returns a component for a value.
     *
     * @param column the column
     * @param row    the row
     * @return a component representation of the value. May be {@code null}
     */
    protected Component getComponent(int column, int row) {
        if (previousRow != row) {
            newPrompt = false;
        }

        Component component = null;
        // free slot.
        Cell cell = model.getSelected();
        if (cell != null && row == cell.getRow() && !newPrompt) {
            // render a 'New' prompt if required
            if (renderNewPrompt(model, column)) {
                component = LabelFactory.create("workflow.scheduling.table.new");
                int rows = model.getFreeRows(column, row);
                component.setLayoutData(TableFactory.rowSpan(rows));
                highlightCell(component);
                newPrompt = true;
            }
        }

        if (component == null) {
            component = getFreeSlot(column, row);
        }
        previousRow = row;
        return component;
    }

    /**
     * Styles an event.
     *
     * @param component the component representing the event to style
     */
    protected void styleEvent(Component component) {
        TableLayoutDataEx layout = TableHelper.getTableLayoutDataEx("Calendar.Event");
        Color background = layout.getBackground();
        if (background != null) {
            setForegroundFromBackground(component, background);
        }
        TableHelper.mergeStyle(component, layout, true);
    }

    /**
     * Determines if the cell can be highlighted.
     *
     * @param column the column
     * @param row    the row
     * @param value  the value at the cell
     * @return {@code true} if the cell can be highlighted
     */
    protected boolean canHighlightCell(int column, int row, Object value) {
        boolean highlight = false;
        if (getModel().isSelected(column, row) && value instanceof PropertySet) {
            highlight = true;
        }
        return highlight;
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
     * Returns a component representing an event.
     *
     * @param event  the event
     * @param column the column
     * @param row    the row
     * @return the component
     */
    protected Component getEvent(PropertySet event, int column, int row) {
        Component result = getEvent(event);
        Label next = null;
        Label previous = null;
        Date startTime = event.getDate(ScheduleEvent.ACT_START_TIME);
        int slot = column - 1; // first column is the time
        if (DateRules.compareDates(startTime, model.getStartDate()) < 0) {
            previous = LabelFactory.create(null, "navigation.previous");
        }
        int colSpan = model.getColumns(event, slot);
        int rowSpan = model.getRows(event, column, row);
        if (colSpan > 1) {
            if (column + colSpan > model.getColumnCount()) {
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
        if (colSpan > 1 || rowSpan > 1) {
            result.setLayoutData(TableFactory.span(colSpan, rowSpan));
        }
        styleEvent(result);
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
        CalendarTableModel model = getModel();
        result = TableHelper.createSpacer();
        TableLayoutDataEx layout = new TableLayoutDataEx();
        int rows = model.getFreeRows(column, row);
        layout.setRowSpan(rows);
        result.setLayoutData(layout);
        return result;
    }

    /**
     * Formats an event.
     *
     * @param event the event
     * @return the event component
     */
    private Component getEvent(PropertySet event) {
        String text = event.getString(ScheduleEvent.ACT_DESCRIPTION);
        if (text == null) {
            text = event.getString(ScheduleEvent.ACT_NAME);
        }
        Label label;
        if (!StringUtils.isEmpty(text)) {
            label = LabelFactory.create(true);
            label.setText(text);
        } else {
            label = TableHelper.createSpacer();
        }
        return label;
    }

    /**
     * Formats a block.
     *
     * @param event the blocking event
     * @return the block component
     */
    private Component getBlock(PropertySet event) {
        String text = event.getString(ScheduleEvent.ACT_DESCRIPTION);
        if (text == null) {
            text = event.getString(ScheduleEvent.ACT_NAME);
            if (text == null) {
                text = event.getString(ScheduleEvent.SCHEDULE_TYPE_NAME);
            }
        }
        Label label = LabelFactory.create(true);
        label.setText(text);
        return label;
    }

    /**
     * Invoked to determine if the 'New' prompt should be rendered for a cell.
     * <p>
     * Only invoked when a new prompt hasn't already been rendered for the
     * selected row, and the specified cell is empty.
     *
     * @param model  the table model
     * @param column the column
     * @return {@code true} if the 'New' prompt should be rendered for the cell
     */
    private boolean renderNewPrompt(CalendarTableModel model, int column) {
        boolean result = false;
        Cell cell = model.getSelected();
        if (cell != null) {
            int selected = cell.getColumn();
            if (selected == column) {
                result = true;
            }
        }
        return result;
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

    /**
     * Sets the foreground colour of a component based on a background colour.
     * <p>
     * If the component is a {@code Row}, the request will be propagated to the child components.
     * <p>
     * If the component is a BalloonHelp, or a child of a row is a BalloonHelp, the foreground colour change will
     * be ignored.
     * <p>
     * NOTE: this is a workaround to ensure that rows containing {@code BalloonHelp} components are rendered correctly
     * when the background is black.
     * <p>
     * TODO - don't render components within the model - move all of this out to the renderer(s)
     *
     * @param component  the component
     * @param background the background colour of the row
     */
    private void setForegroundFromBackground(Component component, Color background) {
        setForeground(component, ColourHelper.getTextColour(background));
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
