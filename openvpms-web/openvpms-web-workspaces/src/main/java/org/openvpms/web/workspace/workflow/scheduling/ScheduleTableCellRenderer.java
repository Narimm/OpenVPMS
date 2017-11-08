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

package org.openvpms.web.workspace.workflow.scheduling;

import echopointng.BalloonHelp;
import echopointng.layout.TableLayoutDataEx;
import echopointng.table.TableCellRendererEx;
import nextapp.echo2.app.Color;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Font;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.Table;
import nextapp.echo2.app.layout.TableLayoutData;
import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.workflow.ScheduleEvent;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.web.echo.colour.ColourHelper;
import org.openvpms.web.echo.factory.BalloonHelpFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.echo.table.TableHelper;
import org.openvpms.web.workspace.workflow.scheduling.ScheduleEventGrid.Availability;

import static org.openvpms.web.workspace.workflow.scheduling.ScheduleEventGrid.Availability.FREE;
import static org.openvpms.web.workspace.workflow.scheduling.ScheduleEventGrid.Availability.UNAVAILABLE;
import static org.openvpms.web.workspace.workflow.scheduling.ScheduleTableModel.Highlight;


/**
 * TableCellRender for schedule events.
 *
 * @author Tim Anderson
 */
public abstract class ScheduleTableCellRenderer implements TableCellRendererEx {

    /**
     * Even row style.
     */
    public static final String EVEN_ROW_STYLE = "ScheduleTable.Even";

    /**
     * Odd row style.
     */
    public static final String ODD_ROW_STYLE = "ScheduleTable.Odd";

    /**
     * The table model.
     */
    private final ScheduleTableModel model;

    /**
     * The colour cache.
     */
    private final ScheduleColours colours;

    /**
     * The previous rendered row.
     */
    private int previousRow = -1;

    /**
     * Determines if the 'New' indicator has been rendered.
     */
    private boolean newPrompt = false;

    /**
     * Constructs a {@link ScheduleTableCellRenderer}.
     *
     * @param model the table model
     */
    public ScheduleTableCellRenderer(ScheduleTableModel model) {
        this.model = model;
        this.colours = model.getColours();
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
            component = getEvent(table, (PropertySet) value, column, row);
        } else {
            component = getComponent(table, value, column, row);
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
        return model.getAvailability(column, row) != UNAVAILABLE;
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
        return model.getAvailability(column, row) != UNAVAILABLE;
    }

    /**
     * Returns the table model.
     *
     * @return the table model
     */
    protected ScheduleTableModel getModel() {
        return model;
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
        return new Label();
    }

    /**
     * Returns a component for a value.
     *
     * @param table  the {@code Table} for which the rendering is occurring
     * @param value  the value retrieved from the {@code TableModel} for the
     *               specified coordinate
     * @param column the column
     * @param row    the row
     * @return a component representation of the value. May be {@code null}
     */
    protected Component getComponent(Table table, Object value, int column, int row) {
        if (previousRow != row) {
            newPrompt = false;
        }

        Component component = null;
        if (value instanceof Component) {
            // pre-rendered component
            component = (Component) value;
        } else {
            Availability availability = model.getAvailability(column, row);
            if (availability != UNAVAILABLE) {
                if (value == null && availability == FREE) {
                    // free slot.
                    Cell cell = model.getSelected();
                    if (cell != null && row == cell.getRow() && !newPrompt) {
                        // render a 'New' prompt if required
                        if (renderNewPrompt(model, column, row)) {
                            component = LabelFactory.create("workflow.scheduling.table.new");
                            highlightCell(component);
                            newPrompt = true;
                        }
                    }
                } else {
                    Label label = LabelFactory.create();
                    if (value != null) {
                        label.setText(value.toString());
                    }
                    component = label;
                }
            }
        }

        if (component != null) {
            PropertySet event = model.getEvent(column, row);
            if (event != null) {
                styleEvent(event, component, table);
            } else {
                colourCell(component, column, row, model);
            }
        }
        previousRow = row;
        return component;
    }

    /**
     * Styles an event.
     *
     * @param event     the event
     * @param component the component representing the event to style
     * @param table     the table, used to determine the font style
     */
    protected void styleEvent(PropertySet event, Component component, Table table) {
        TableLayoutData layout = getEventLayoutData(event, model);
        if (layout != null) {
            Color background = layout.getBackground();
            if (background != null) {
                setForegroundFromBackground(component, background);
            }
            TableHelper.mergeStyle(component, layout, true);
        }
        if (model.useStrikeThrough()) {
            String status = event.getString(ScheduleEvent.ACT_STATUS);
            if (ActStatus.COMPLETED.equals(status) || ActStatus.CANCELLED.equals(status)) {
                setStrikethroughFont(component, table);
            }
        }
    }

    /**
     * Evaluates the view's displayExpression expression against the supplied
     * event. If no displayExpression is present, {@code null} is returned.
     * <p>
     * If the event has an {@link ScheduleEvent#ARRIVAL_TIME} property,
     * a formatted string named <em>waiting</em> will be added to the set prior
     * to evaluation of the expression. This indicates the waiting time, and
     * is the difference between the arrival time and the current time.
     *
     * @param event the event
     * @return the evaluate result. May be {@code null}
     */
    protected String evaluate(PropertySet event) {
        return model.evaluate(event);
    }

    /**
     * Helper to create a multiline label with optional notes popup,
     * if the supplied notes are non-null and {@code displayNotes} is
     * {@code true}.
     *
     * @param text  the label text
     * @param notes the notes. May be {@code null}
     * @return a component representing the label with optional popup
     */
    protected Component createLabelWithNotes(String text, String notes) {
        Label label = LabelFactory.create(true);
        Component result;
        if (text != null) {
            label.setText(text);
        }
        if (model.getDisplayNotes() && notes != null) {
            BalloonHelp help = BalloonHelpFactory.create(notes);
            result = RowFactory.create(Styles.CELL_SPACING, label, help);
        } else {
            result = label;
        }
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
    protected boolean canHighlightCell(int column, int row, Object value) {
        boolean highlight = false;
        boolean single = model.isSingleScheduleView();
        Cell cell = model.getSelected();
        if ((single && cell != null && cell.getRow() == row)
            || (!single && model.isSelected(column, row)) && model.getAvailability(column, row) == Availability.BUSY) {
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
     * Returns the style for a free row.
     *
     * @param model the schedule table model
     * @param row   the row
     * @return a style for the row
     */
    protected String getFreeStyle(ScheduleTableModel model, int row) {
        return (row % 2 == 0) ? EVEN_ROW_STYLE : ODD_ROW_STYLE;
    }

    /**
     * Colours a cell based on its availability.
     *
     * @param component a component representing the cell
     * @param column    the cell column
     * @param row       the cell row
     * @param model     the event model
     */
    protected void colourCell(Component component, int column, int row, ScheduleTableModel model) {
        ScheduleEventGrid.Availability avail = model.getAvailability(column, row);
        colourCell(component, avail, model, row);
    }

    /**
     * Colours an event cell based on availability.
     *
     * @param component the component representing the cell
     * @param avail     the cell's availability
     * @param model     the event model
     * @param row       the cell row
     */
    protected void colourCell(Component component, ScheduleEventGrid.Availability avail, ScheduleTableModel model,
                              int row) {
        String style;
        style = getStyle(avail, model, row);
        TableHelper.mergeStyle(component, style);
    }

    /**
     * Returns the style of a cell based on availability.
     *
     * @param avail the cell's availability
     * @param model the event model
     * @param row   the cell row
     * @return the style name
     */
    protected String getStyle(Availability avail, ScheduleTableModel model, int row) {
        String style;
        switch (avail) {
            case BUSY:
                style = "ScheduleTable.Busy";
                break;
            case FREE:
                style = getFreeStyle(model, row);
                break;
            default:
                style = "ScheduleTable.Unavailable";
                break;
        }
        return style;
    }

    /**
     * Returns table layout data for an event.
     *
     * @param event the event
     * @param model the model
     * @return layout data for the event, or {@code null} if no style information exists
     */
    protected TableLayoutDataEx getEventLayoutData(PropertySet event, ScheduleTableModel model) {
        TableLayoutDataEx result;
        if (!isSelectedClinician(event, model)) {
            result = TableHelper.getTableLayoutDataEx("ScheduleTable.Busy");
        } else {
            Highlight highlight = model.getHighlight();
            result = getEventLayoutData(event, highlight);
        }
        return result;
    }

    /**
     * Returns the table layout data for an event .
     *
     * @param event     the event
     * @param highlight the highlight setting
     * @return layout data for the event, or {@code null} if no style information exists
     */
    protected TableLayoutDataEx getEventLayoutData(PropertySet event, Highlight highlight) {
        TableLayoutDataEx result = null;
        if (highlight == Highlight.STATUS) {
            String style = getStatusStyle(event);
            result = TableHelper.getTableLayoutDataEx(style);
        } else {
            Color colour = getEventColour(event, highlight);
            if (colour != null) {
                result = new TableLayoutDataEx();
                result.setBackground(colour);
            }
        }
        return result;
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
     * Returns a colour for an object identified by its reference.
     *
     * @param reference the reference
     * @return the colour, or {@code null} if no colour exists
     */
    protected Color getColour(IMObjectReference reference) {
        return colours.getColour(reference);
    }

    /**
     * Returns the style of an event based on its status.
     *
     * @param event the event
     * @return the style
     */
    private String getStatusStyle(PropertySet event) {
        return "ScheduleTable." + event.getString(ScheduleEvent.ACT_STATUS);
    }

    /**
     * Determines if a schedule has the same clinician as that specified by the table model.
     *
     * @param event the schedule event
     * @param model the schedule table model
     * @return {@code true} if they have the same clinician, or the model indicates to display all clinicians
     */
    private boolean isSelectedClinician(PropertySet event, ScheduleTableModel model) {
        IMObjectReference clinician = model.getClinician();
        return clinician == null
               || ObjectUtils.equals(clinician, event.getReference(ScheduleEvent.CLINICIAN_REFERENCE));
    }

    /**
     * Invoked to determine if the 'New' prompt should be rendered for a cell.
     * <p>
     * Only invoked when a new prompt hasn't already been rendered for the
     * selected row, and the specified cell is empty.
     *
     * @param model  the table model
     * @param column the column
     * @param row    the row
     * @return {@code true} if the 'New' prompt should be rendered for the cell
     */
    private boolean renderNewPrompt(ScheduleTableModel model, int column, int row) {
        boolean result = false;
        Cell cell = model.getSelected();
        if (cell != null) {
            int selected = cell.getColumn();
            if (selected == column) {
                result = true;
            } else if (model.isSingleScheduleView() && (column == selected - 1 || column == selected + 1)) {
                // if the column is adjacent to the selected column
                if (model.getValueAt(selected, row) != null) {
                    // render the prompt in the current column if the selected
                    // column isn't empty
                    result = true;
                }
            }
        }
        return result;
    }

    /**
     * Returns a colour for an event, for the given highlight style.
     *
     * @param event     the event. May be {@code null}
     * @param highlight the highlight style
     * @return the colour, or {@code null} if none is found
     */
    private Color getEventColour(PropertySet event, Highlight highlight) {
        Color result = null;
        if (event != null) {
            switch (highlight) {
                case EVENT_TYPE:
                    result = getColour(event, ScheduleEvent.SCHEDULE_TYPE_REFERENCE);
                    break;
                case CLINICIAN:
                    result = getColour(event, ScheduleEvent.CLINICIAN_REFERENCE);
            }
        }
        return result;
    }

    /**
     * Helper to get a colour for an object identified by its reference.
     *
     * @param set the set to look up the reference in
     * @param key the reference key
     * @return the colour, or {@code null} if none is found
     */
    private Color getColour(PropertySet set, String key) {
        IMObjectReference reference = set.getReference(key);
        return getColour(reference);
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
