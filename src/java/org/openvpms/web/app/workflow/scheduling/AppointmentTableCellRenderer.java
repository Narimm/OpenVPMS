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
import nextapp.echo2.app.Table;
import org.openvpms.archetype.rules.workflow.Appointment;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.component.table.AbstractTableCellRenderer;
import org.openvpms.web.component.util.ColourHelper;

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


    private Map<IMObjectReference, String> appointmentColours;

    /**
     * Default constructor.
     */
    public AppointmentTableCellRenderer() {
        loadAppointmentColours();
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
        Component component = super.getComponent(table, value, column, row);
        if (column != AppointmentTableModel.START_TIME_INDEX) {
            AppointmentTableModel model = (AppointmentTableModel) table.getModel();
            ObjectSet appointment = model.getAppointment(column, row);
            Color colour = getAppointmentColour(appointment);
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
     * @param value  the value retrieved from the <code>TableModel</code> for
     *               the specified coordinate
     * @param column the column
     * @param row    the row
     * @return a style name for the given column and row.
     */
    protected String getStyle(Table table, Object value, int column, int row) {
        String result;
        AppointmentTableModel model = (AppointmentTableModel) table.getModel();
        if (column == AppointmentTableModel.START_TIME_INDEX) {
            result = getFreeStyle(model, row);
        } else {
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
        return result;
    }

    private String getFreeStyle(AppointmentTableModel model, int row) {
        int hour = model.getHour(row);
        return (hour % 2 == 0) ? "Appointment.Even" : "Appointment.Odd";
    }

    private Color getAppointmentColour(ObjectSet set) {
        if (set != null) {
            String colour = appointmentColours.get(
                    set.getReference(Appointment.APPOINTMENT_TYPE_REFERENCE));
            return ColourHelper.getColor(colour);
        }
        return null;
    }

    private void loadAppointmentColours() {
        appointmentColours = new HashMap<IMObjectReference, String>();
        ArchetypeQuery query = new ArchetypeQuery("entity.appointmentType",
                                                  true, true);
        query.setMaxResults(ArchetypeQuery.ALL_RESULTS);
        Iterator<Entity> iter = new IMObjectQueryIterator<Entity>(query);
        while (iter.hasNext()) {
            Entity type = iter.next();
            IMObjectBean bean = new IMObjectBean(type);
            appointmentColours.put(type.getObjectReference(),
                                   bean.getString("colour"));
        }
    }

}
