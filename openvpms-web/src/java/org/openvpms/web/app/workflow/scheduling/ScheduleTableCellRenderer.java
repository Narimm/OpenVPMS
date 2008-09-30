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
import nextapp.echo2.app.Color;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Font;
import nextapp.echo2.app.Table;
import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.workflow.ScheduleEvent;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.ObjectSet;
import static org.openvpms.web.app.workflow.scheduling.ScheduleEventGrid.Availability.FREE;
import static org.openvpms.web.app.workflow.scheduling.ScheduleEventGrid.Availability.UNAVAILABLE;
import static org.openvpms.web.app.workflow.scheduling.ScheduleTableModel.Highlight;
import org.openvpms.web.component.table.AbstractTableCellRenderer;
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
public class ScheduleTableCellRenderer extends AbstractTableCellRenderer {

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
     * @param table  the <code>Table</code> for which the rendering is
     *               occurring
     * @param value  the value retrieved from the <code>TableModel</code> for
     *               the specified coordinate
     * @param column the column index to render
     * @param row    the row index to render
     * @return a component representation  of the value.
     */
    @Override
    public Component getTableCellRendererComponent(Table table, Object value,
                                                   int column, int row) {
        Component component = getComponent(table, value, column, row);
        boolean highlight = canHighlightCell(table, column, row);
        if (highlight) {
            // highlight the selected cell.
            // Ideally, this would be done by the table, however none of
            // the tables support cell selection.
            // Also, it would be best if highlighting was done by changing
            // the cell background, but due to a bug in TableEx, this
            // results in all similar cells being updated with the highlight
            // colour.
            Font font = getFont(table);
            if (font != null) {
                int style = Font.BOLD | Font.ITALIC;
                font = new Font(font.getTypeface(), style, font.getSize());
                component.setFont(font);
            }
        }
        return component;
    }

    /**
     * Refreshes the cached data.
     */
    public void refresh() {
        eventColours = getColours(scheduleTypeShortName);
        clinicianColours = getColours("security.user");
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
     * @return a component representation of the value
     */
    @Override
    protected Component getComponent(Table table, Object value, int column,
                                     int row) {
        ScheduleTableModel model = (ScheduleTableModel) table.getModel();
        boolean singleScheduleView = model.isSingleScheduleView();
        Component component;
        if (value instanceof Component) {
            // pre-rendered component
            component = (Component) value;
        } else {
            if (value == null && !singleScheduleView
                    && model.isSelectedCell(column, row)
                    && model.getAvailability(column, row) == FREE) {
                // free slot in multiple schedule view
                component = LabelFactory.create(
                        "workflow.scheduling.table.new");
            } else {
                component = super.getComponent(table, value, column, row);
            }
        }

        ObjectSet event = model.getEvent(column, row);
        if (event != null) {
            highlightEvent(component, event, model);
        } else {
            colourCell(component, column, row, model);
        }
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
                && model.getAvailability(column, row) != UNAVAILABLE) {
            highlight = true;
        }
        return highlight;
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
     * @param model     the the event model
     * @param row       the cell row
     */
    protected void colourCell(Component component,
                              ScheduleEventGrid.Availability avail,
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
        mergeStyle(component, style);
    }

    /**
     * Highlights an event.
     *
     * @param component the component representing the schedule event
     * @param event     the event
     * @param model     the schedule table model
     */
    private void highlightEvent(Component component,
                                ObjectSet event,
                                ScheduleTableModel model) {
        if (!isSelectedClinician(event, model)) {
            mergeStyle(component, "ScheduleTable.Busy");
        } else {
            Highlight highlight = model.getHighlight();

            if (highlight == Highlight.STATUS) {
                String style = getStatusStyle(event);
                mergeStyle(component, style);
            } else {
                Color colour = getEventColour(event, highlight);
                if (colour != null) {
                    TableLayoutDataEx layout
                            = (TableLayoutDataEx) component.getLayoutData();
                    if (layout == null) {
                        layout = new TableLayoutDataEx();
                        component.setLayoutData(layout);
                    }
                    layout.setBackground(colour);
                }
            }
        }
    }

    /**
     * Returns the stye of an event based on its status.
     *
     * @param event the event
     * @return the style
     */
    private String getStatusStyle(ObjectSet event) {
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
    private boolean isSelectedClinician(ObjectSet event,
                                        ScheduleTableModel model) {
        IMObjectReference clinician = model.getClinician();
        if (clinician == null) {
            return true;
        }
        return ObjectUtils.equals(clinician, event.getReference(
                ScheduleEvent.CLINICIAN_REFERENCE));
    }

    /**
     * Returns a colour for an event, for the given highlight style.
     *
     * @param event     the event. May be <tt>null</tt>
     * @param highlight the highlight style
     * @return the colour, or <tt>null</tt> if none is found
     */
    private Color getEventColour(ObjectSet event, Highlight highlight) {
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
    private Color getColour(ObjectSet set, String key,
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
