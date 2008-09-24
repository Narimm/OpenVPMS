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
import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.workflow.Appointment;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.ObjectSet;
import static org.openvpms.web.app.workflow.scheduling.AppointmentGrid.Availability.FREE;
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
        if (column != AppointmentTableModel.START_TIME_INDEX) {
            AppointmentTableModel model
                    = (AppointmentTableModel) table.getModel();
            if (!model.isSingleScheduleView()
                    && model.isSelectedCell(column, row)
                    && model.getAvailability(column, row)
                    != AppointmentGrid.Availability.UNAVAILABLE) {
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

        if (column == AppointmentTableModel.START_TIME_INDEX) {
            mergeStyle(component, getFreeStyle(model, row));
        } else {
            ObjectSet appointment = model.getAppointment(column, row);
            if (appointment != null) {
                highlightAppointment(component, appointment, model);
            } else {
                highlightCell(component, column, row, model);
            }
        }
        return component;
    }

    /**
     * Refreshes the cached data.
     */
    public void refresh() {
        appointmentColours = getColours("entity.appointmentType");
        clinicianColours = getColours("security.user");
    }

    /**
     * Highlights a cell based on its availability.
     *
     * @param component a component representing the cell
     * @param column    the cell column
     * @param row       the cell row
     * @param model     the appointment model
     */
    private void highlightCell(Component component, int column, int row,
                               AppointmentTableModel model) {
        AppointmentGrid.Availability avail
                = model.getAvailability(column, row);
        String style;

        switch (avail) {
            case BUSY:
                style = "Appointment.Busy";
                break;
            case FREE:
                style = getFreeStyle(model, row);
                break;
            default:
                style = "Appointment.Unavailable";
                break;
        }
        mergeStyle(component, style);
    }

    /**
     * Highlights an appointment.
     *
     * @param component   the component representing the appointment
     * @param appointment the appointment
     * @param model       the appointment table model
     */
    private void highlightAppointment(Component component,
                                      ObjectSet appointment,
                                      AppointmentTableModel model) {
        if (!isSelectedClinician(appointment, model)) {
            mergeStyle(component, "Appointment.Busy");
        } else {
            AppointmentTableModel.Highlight highlight = model.getHighlight();

            if (model.getHighlight() == STATUS) {
                String style = getStatusStyle(appointment);
                mergeStyle(component, style);
            } else {
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
        }
    }

    /**
     * Returns the stye of an appointment based on its status.
     *
     * @param appointment the appointment
     * @return the style
     */
    private String getStatusStyle(ObjectSet appointment) {
        return "TaskTable." + appointment.getString(Appointment.ACT_STATUS);
    }

    /**
     * Determines if an appointment has the same clinician as that specified
     * by the table model.
     *
     * @param appointment the appointment
     * @param model       the appointment table model
     * @return <tt>true</tt> if they have the same clinician, or the model
     *         indicates to display all clincians
     */
    private boolean isSelectedClinician(ObjectSet appointment,
                                        AppointmentTableModel model) {
        IMObjectReference clinician = model.getClinician();
        if (clinician == null) {
            return true;
        }
        return ObjectUtils.equals(clinician, appointment.getReference(
                Appointment.CLINICIAN_REFERENCE));
    }

    /**
     * Returns the style for a free row.
     *
     * @param model the appointment table model
     * @param row   the row
     * @return a style for the row
     */
    private String getFreeStyle(AppointmentTableModel model, int row) {
        int hour = model.getHour(row);
        return (hour % 2 == 0) ? "Appointment.Even" : "Appointment.Odd";
    }

    /**
     * Returns a colour for an appointment, for the given highlight style.
     *
     * @param set       the appointment. May be <tt>null</tt>
     * @param highlight the highlight style
     * @return the colour, or <tt>null</tt> if none is found
     */
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
