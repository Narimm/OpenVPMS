/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.workflow.scheduling;

import echopointng.layout.TableLayoutDataEx;
import echopointng.table.TableCellRendererEx;
import nextapp.echo2.app.Color;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Font;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Table;
import nextapp.echo2.app.layout.TableLayoutData;
import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.workflow.ScheduleEvent;
import org.openvpms.archetype.rules.user.UserArchetypes;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.web.app.workflow.scheduling.ScheduleEventGrid.Availability;
import static org.openvpms.web.app.workflow.scheduling.ScheduleEventGrid.Availability.FREE;
import static org.openvpms.web.app.workflow.scheduling.ScheduleEventGrid.Availability.UNAVAILABLE;
import static org.openvpms.web.app.workflow.scheduling.ScheduleTableModel.Highlight;
import org.openvpms.web.component.table.TableHelper;
import org.openvpms.web.component.util.ColourHelper;
import org.openvpms.web.component.util.LabelFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * TableCellRender for schedule events.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-09-06 07:52:23Z $
 */
public abstract class ScheduleTableCellRenderer implements TableCellRendererEx {

    /**
     * The schedule type archetype short name.
     */
    private final String scheduleTypeShortName;

    /**
     * Cache of event colours.
     */
    private Map<IMObjectReference, String> eventColours;

    /**
     * Cache of clinician colours.
     */
    private Map<IMObjectReference, String> clinicianColours;

    /**
     * The previous rendered row.
     */
    private int previousRow = -1;

    /**
     * Determines if the 'New' indicator has been rendered.
     */
    private boolean newPrompt = false;

    /**
     * Creates a new <tt>ScheduleTableCellRenderer</tt>.
     *
     * @param scheduleTypeShortName the schedule type archetype short name
     */
    public ScheduleTableCellRenderer(String scheduleTypeShortName) {
        this.scheduleTypeShortName = scheduleTypeShortName;
        refresh();
    }

    /**
     * Returns a component that will be displayed at the specified coordinate in
     * the table.
     *
     * @param table  the <tt>Table</tt> for which the rendering is
     *               occurring
     * @param value  the value retrieved from the <tt>TableModel</tt> for
     *               the specified coordinate
     * @param column the column index to render
     * @param row    the row index to render
     * @return a component representation  of the value.
     */
    public Component getTableCellRendererComponent(Table table, Object value,
                                                   int column, int row) {
        Component component = getComponent(table, value, column, row);
        if (component != null) {
            if (isCut(table, column, row)) {
                cutCell(table, component);
            } else if (canHighlightCell(table, column, row)) {
                // highlight the selected cell.
                highlightCell(table, component);
            }
        }
        return component;
    }

    /**
     * Refreshes the cached data.
     */
    public void refresh() {
        eventColours = getColours(scheduleTypeShortName);
        clinicianColours = getColours(UserArchetypes.USER);
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
     * @return <tt>true<t/tt> if the cell causes selection
     */
    public boolean isSelectionCausingCell(Table table, int column, int row) {
        ScheduleTableModel model = (ScheduleTableModel) table.getModel();
        return model.getAvailability(column, row) != UNAVAILABLE;
    }

    /**
     * This method is called to determine which cells within a row can cause an
     * action to be raised on the server when clicked.
     * <p/>
     * By default if a Table has attached actionListeners then any click on any
     * cell within a row will cause the action to fire.
     * <p/>
     * This method allows this to be overrriden and only certain cells within a
     * row can cause an action event to be raise.
     *
     * @param table  the Table in question
     * @param column the column in question
     * @param row    the row in quesiton
     * @return - Return true means that the cell can cause actions while false
     *         means the cells can not cause action events.
     */
    public boolean isActionCausingCell(Table table, int column, int row) {
        ScheduleTableModel model = (ScheduleTableModel) table.getModel();
        return model.getAvailability(column, row) != UNAVAILABLE;
    }

    /**
     * Returns a component for a value.
     *
     * @param table  the <tt>Table</tt> for which the rendering is
     *               occurring
     * @param value  the value retrieved from the <tt>TableModel</tt> for the
     *               specified coordinate
     * @param column the column
     * @param row    the row
     * @return a component representation of the value. May be <tt>null</tt>
     */
    protected Component getComponent(Table table, Object value, int column,
                                     int row) {
        ScheduleTableModel model = (ScheduleTableModel) table.getModel();

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
                    if (row == model.getSelectedRow() && !newPrompt) {
                        // render a 'New' prompt if required
                        if (renderNewPrompt(model, column, row)) {
                            component = LabelFactory.create(
                                    "workflow.scheduling.table.new");
                            highlightCell(table, component);
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
                TableLayoutData layout = getEventLayoutData(event, model);
                if (layout != null) {
                    TableHelper.mergeStyle(component, layout, true);
                }
            } else {
                colourCell(component, column, row, model);
            }
        }
        previousRow = row;
        return component;
    }

    /**
     * Determines if the cell can be higlighted.
     *
     * @param table  the table
     * @param column the column
     * @param row    the row
     * @return <tt>true</tt> if the cell can be highlighted
     */
    protected boolean canHighlightCell(Table table, int column, int row) {
        ScheduleTableModel model = (ScheduleTableModel) table.getModel();
        boolean highlight = false;
        if (!model.isSingleScheduleView() && model.isSelectedCell(column, row)
            && model.getAvailability(column, row) == Availability.BUSY) {
            highlight = true;
        }
        return highlight;
    }

    /**
     * Determines if a cell has been 'cut'.
     *
     * @param table  the table
     * @param column the column
     * @param row    the row
     * @return <tt>true</tt> if the cell has been 'cut'
     */
    protected boolean isCut(Table table, int column, int row) {
        ScheduleTableModel model = (ScheduleTableModel) table.getModel();
        return model.isCutCell(column, row);
    }

    /**
     * Highlights a cell component, used to highlight the selected cell.
     * <p/>
     * Ideally, this would be done by the table, however none of
     * the tables support cell selection.
     * Also, it would be best if highlighting was done by changing
     * the cell background, but due to a bug in TableEx, this
     * results in all similar cells being updated with the highlight colour.
     *
     * @param table     the table
     * @param component the cell component
     */
    protected void highlightCell(Table table, Component component) {
        Font font = getFont(table);
        if (font != null) {
            int style = Font.BOLD | Font.ITALIC;
            font = new Font(font.getTypeface(), style, font.getSize());
            component.setFont(font);
        }
    }

    /**
     * Marks a cell as being cut.
     *
     * @param table     the table
     * @param component the cell component
     */
    protected void cutCell(Table table, Component component) {
        Font font = getFont(table);
        if (font != null) {
            int style = Font.BOLD | Font.LINE_THROUGH;
            font = new Font(font.getTypeface(), style, font.getSize());
            component.setFont(font);
        }
    }

    /**
     * Returns the style for a free row.
     *
     * @param model the schedule table model
     * @param row   the row
     * @return a style for the row
     */
    protected String getFreeStyle(ScheduleTableModel model, int row) {
        return (row % 2 == 0) ? "ScheduleTable.Even" : "ScheduleTable.Odd";
    }

    /**
     * Colours a cell based on its availability.
     *
     * @param component a component representing the cell
     * @param column    the cell column
     * @param row       the cell row
     * @param model     the event model
     */
    protected void colourCell(Component component, int column, int row,
                              ScheduleTableModel model) {
        ScheduleEventGrid.Availability avail
                = model.getAvailability(column, row);
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
    protected void colourCell(Component component,
                              ScheduleEventGrid.Availability avail,
                              ScheduleTableModel model, int row) {
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
    protected String getStyle(Availability avail,
                              ScheduleTableModel model, int row) {
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
     * @return layout data for the event, or <tt>null</tt> if no style
     *         information exists
     */
    protected TableLayoutDataEx getEventLayoutData(PropertySet event,
                                                   ScheduleTableModel model) {
        TableLayoutDataEx result = null;
        if (!isSelectedClinician(event, model)) {
            result = TableHelper.getTableLayoutDataEx("ScheduleTable.Busy");
        } else {
            Highlight highlight = model.getHighlight();

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
        }
        return result;
    }

    /**
     * Returns the stye of an event based on its status.
     *
     * @param event the event
     * @return the style
     */
    private String getStatusStyle(PropertySet event) {
        return "ScheduleTable." + event.getString(ScheduleEvent.ACT_STATUS);
    }

    /**
     * Determines if a schedule has the same clinician as that specified
     * by the table model.
     *
     * @param event the schedule event
     * @param model the schedule table model
     * @return <tt>true</tt> if they have the same clinician, or the model
     *         indicates to display all clincians
     */
    private boolean isSelectedClinician(PropertySet event,
                                        ScheduleTableModel model) {
        IMObjectReference clinician = model.getClinician();
        return clinician == null
               || ObjectUtils.equals(clinician, event.getReference(ScheduleEvent.CLINICIAN_REFERENCE));
    }

    /**
     * Invoked to determine if the 'New' prompt should be rendered for a cell.
     * <p/>
     * Only invoked when a new prompt hasn't already been rendered for the
     * selected row, and the specified cell is empty.
     *
     * @param model  the table model
     * @param column the column
     * @param row    the row
     * @return <tt>true</tt> if the 'New' prompt shouldbe rendered for the cell
     */
    private boolean renderNewPrompt(ScheduleTableModel model, int column,
                                    int row) {
        boolean result = false;
        int selected = model.getSelectedColumn();
        if (selected == column) {
            result = true;
        } else if (model.isSingleScheduleView()
                   && (column == selected - 1 || column == selected + 1)) {
            // if the column is adjacent to the selected column
            if (model.getValueAt(selected, row) != null) {
                // render the prompt in the current column if the selected
                // column isn't empty
                result = true;
            }
        }
        return result;
    }

    /**
     * Returns a colour for an event, for the given highlight style.
     *
     * @param event     the event. May be <tt>null</tt>
     * @param highlight the highlight style
     * @return the colour, or <tt>null</tt> if none is found
     */
    private Color getEventColour(PropertySet event, Highlight highlight) {
        Color result = null;
        if (event != null) {
            switch (highlight) {
                case EVENT:
                    result = getColour(event,
                                       ScheduleEvent.SCHEDULE_TYPE_REFERENCE,
                                       eventColours);
                    break;
                case CLINICIAN:
                    result = getColour(event,
                                       ScheduleEvent.CLINICIAN_REFERENCE,
                                       clinicianColours);
            }
        }
        return result;
    }

    /**
     * Helper to get a colour for an object identified by its reference.
     *
     * @param set     the set to look up the reference in
     * @param key     the reference key
     * @param colours the colours, keyed on object reference
     * @return the colour, or <tt>null</tt> if none is found
     */
    private Color getColour(PropertySet set, String key,
                            Map<IMObjectReference, String> colours) {
        String colour = colours.get(set.getReference(key));
        return ColourHelper.getColor(colour);
    }

    /**
     * Returns a map of object references and their corresponding 'colour' node
     * values for the specified short name.
     *
     * @param shortName the archetype short name
     * @return a map of the matching objects and their 'colour' node  values
     */
    private Map<IMObjectReference, String> getColours(String shortName) {
        Map<IMObjectReference, String> result
                = new HashMap<IMObjectReference, String>();
        ArchetypeQuery query = new ArchetypeQuery(shortName, true, true);
        query.setMaxResults(ArchetypeQuery.ALL_RESULTS);
        Iterator<IMObject> iter = new IMObjectQueryIterator<IMObject>(query);
        while (iter.hasNext()) {
            IMObject object = iter.next();
            IMObjectBean bean = new IMObjectBean(object);
            result.put(object.getObjectReference(), bean.getString("colour"));
        }
        return result;
    }

    /**
     * Helper to return a font for a component, navigating up the component
     * heirarchy if one isn't found on the specified component.
     *
     * @param component the component
     * @return the font, or <tt>null</tt> if none is found
     */
    private Font getFont(Component component) {
        Font font = component.getFont();
        if (font == null) {
            font = (Font) component.getRenderProperty(Component.PROPERTY_FONT);
            if (font == null && component.getParent() != null) {
                font = getFont(component.getParent());
            }
        }
        return font;
    }

}
