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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.workflow.scheduling;

import echopointng.layout.TableLayoutDataEx;
import nextapp.echo2.app.Color;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Font;
import nextapp.echo2.app.Table;
import org.openvpms.archetype.rules.workflow.Appointment;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.ObjectSet;
import static org.openvpms.web.app.workflow.scheduling.AppointmentTableModel.Availability.FREE;
import static org.openvpms.web.app.workflow.scheduling.AppointmentTableModel.Highlight.STATUS;
import org.openvpms.web.component.table.AbstractTableCellRenderer;
import org.openvpms.web.component.util.ColourHelper;
import org.openvpms.web.component.util.LabelFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * TableCellRender that assigns blocks of appointments in different hours a
 * different style.
 * Note that for this renderer will not work for partial table renders as
 * it maintains state for the style of the previous row.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-09-06 07:52:23Z $
 */
public class AppointmentTableCellRenderer extends AbstractTableCellRenderer {

    /**
     * Cache of appointment colours.
     */
    private Map<IMObjectReference, String> appointmentColours;

    /**
     * Cache of clinician colours.
     */
    private Map<IMObjectReference, String> clinicianColours;


    /**
     * Default constructor.
     */
    public AppointmentTableCellRenderer() {
        appointmentColours = getColours("entity.appointmentType");
        clinicianColours = getColours("security.user");
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
        Component component = super.getTableCellRendererComponent(
                table, value, column, row);
        if (column != AppointmentTableModel.START_TIME_INDEX) {
            AppointmentTableModel model
                    = (AppointmentTableModel) table.getModel();
            if (!model.isSingleScheduleView()
                    && model.isSelectedCell(column, row)
                    && model.getAvailability(column, row)
                    != AppointmentTableModel.Availability.UNAVAILABLE) {
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
        }
        return component;
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
        AppointmentTableModel model = (AppointmentTableModel) table.getModel();
        Component component;
        if (value instanceof Component) {
            // pre-rendered component
            component = (Component) value;
        } else if (value == null
                && !model.isSingleScheduleView()
                && model.isSelectedCell(column, row)
                && model.getAvailability(column, row) == FREE) {
            // free slot in multiple schedule view
            component = LabelFactory.create("workflow.scheduling.table.new");
        } else {
            component = super.getComponent(table, value, column, row);
        }

        if (column != AppointmentTableModel.START_TIME_INDEX) {
            // highlight the cell
            ObjectSet appointment = model.getAppointment(column, row);
            AppointmentTableModel.Highlight highlight = model.getHighlight();
            Color colour = getAppointmentColour(appointment, highlight);
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
        return component;
    }

    /**
     * Returns the style name for a column and row.
     *
     * @param table  the <code>Table</code> for which the rendering is
     *               occurring
     * @param value  the value retrieved from the <tt>TableModel</tt> for
     *               the specified coordinate
     * @param column the column
     * @param row    the row
     * @return a style name for the given column and row.
     */
    protected String getStyle(Table table, Object value, int column, int row) {
        String result = null;
        AppointmentTableModel model = (AppointmentTableModel) table.getModel();
        if (column == AppointmentTableModel.START_TIME_INDEX) {
            result = getFreeStyle(model, row);
        } else {
            if (model.getHighlight() == STATUS) {
                result = getStatusStyle(model, column, row);
            }
            if (result == null) {
                AppointmentTableModel.Availability avail
                        = model.getAvailability(column, row);

                switch (avail) {
                    case BUSY:
                        result = "Appointment.Busy";
                        break;
                    case FREE:
                        result = getFreeStyle(model, row);
                        break;
                    default:
                        result = "Appointment.Unavailable";
                        break;
                }
            }
        }
        return result;
    }

    private String getStatusStyle(AppointmentTableModel model, int column,
                                  int row) {
        String result = null;
        ObjectSet set = model.getAppointment(column, row);
        if (set != null) {
            result = "TaskTable." + set.getString(Appointment.ACT_STATUS);
        }
        return result;
    }

    private String getFreeStyle(AppointmentTableModel model, int row) {
        int hour = model.getHour(row);
        return (hour % 2 == 0) ? "Appointment.Even" : "Appointment.Odd";
    }

    private Color getAppointmentColour(
            ObjectSet set, AppointmentTableModel.Highlight highlight) {
        Color result = null;
        if (set != null) {
            switch (highlight) {
                case APPOINTMENT:
                    result = getColour(set,
                                       Appointment.APPOINTMENT_TYPE_REFERENCE,
                                       appointmentColours);
                    break;
                case CLINICIAN:
                    result = getColour(set,
                                       Appointment.CLINICIAN_REFERENCE,
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
