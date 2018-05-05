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

package org.openvpms.web.workspace.workflow.appointment;

import echopointng.layout.TableLayoutDataEx;
import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Color;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.Table;
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.workflow.ScheduleEvent;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.workspace.workflow.scheduling.Schedule;
import org.openvpms.web.workspace.workflow.scheduling.ScheduleTableCellRenderer;
import org.openvpms.web.workspace.workflow.scheduling.ScheduleTableModel;

import java.util.Date;

/**
 * Cell renderer for appointments.
 *
 * @author Tim Anderson
 */
public abstract class AbstractAppointmentTableCellRender extends ScheduleTableCellRenderer {

    /**
     * Constructs a {@link AbstractAppointmentTableCellRender}.
     *
     * @param model the table model
     */
    public AbstractAppointmentTableCellRender(ScheduleTableModel model) {
        super(model);
    }

    /**
     * Helper to create a label indicating the reminder status of an appointment.
     *
     * @param reminderSent  the date a reminder was sent. May be {@code null}
     * @param reminderError the reminder error, if the reminder couldn't be sent. May be {@code null}
     * @return a new label
     */
    public static Label createReminderIcon(Date reminderSent, String reminderError) {
        String style;
        if (!StringUtils.isEmpty(reminderError)) {
            style = "AppointmentReminder.error";
        } else if (reminderSent != null) {
            style = "AppointmentReminder.sent";
        } else {
            style = "AppointmentReminder.unsent";
        }
        return LabelFactory.create(null, style);
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
        Component result;
        if (Schedule.isBlockingEvent(event)) {
            result = getBlock(event);
        } else {
            result = getAppointment(event);
        }
        return result;
    }

    /**
     * Helper to create a multiline label with optional notes popup,
     * if the supplied notes are non-null and {@code displayNotes} is
     * {@code true}.
     *
     * @param text          the label text
     * @param notes         the notes. May be {@code null}
     * @param sendReminder  if {@code true}, a reminder should be sent for this appointment
     * @param reminderSent  the date a reminder was sent. May be {@code null}
     * @param reminderError the reminder error, if the reminder couldn't be sent. May be {@code null}
     * @param onlineBooking determines if the appointment is from an online booking
     * @return a component representing the label with optional popup
     */
    protected Component createLabelWithNotes(String text, String notes, boolean sendReminder, Date reminderSent,
                                             String reminderError, boolean onlineBooking) {
        Component result = createLabelWithNotes(text, notes);
        boolean hasReminder = sendReminder || reminderSent != null || reminderError != null;
        if (hasReminder || onlineBooking) {
            if (!(result instanceof Row)) {
                result = RowFactory.create(Styles.CELL_SPACING, result);
            }
            if (hasReminder) {
                Label reminder = createReminderIcon(reminderSent, reminderError);
                reminder.setLayoutData(RowFactory.layout(new Alignment(Alignment.RIGHT, Alignment.TOP), Styles.FULL_WIDTH));
                result.add(reminder);
            }
            if (onlineBooking) {
                Label booking = LabelFactory.create(null, "Appointment.OnlineBooking");
                booking.setLayoutData(RowFactory.layout(new Alignment(Alignment.RIGHT, Alignment.TOP), Styles.FULL_WIDTH));
                result.add(booking);
            }
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
    @Override
    protected TableLayoutDataEx getEventLayoutData(PropertySet event, ScheduleTableModel.Highlight highlight) {
        TableLayoutDataEx result = null;
        if (Schedule.isBlockingEvent(event)) {
            IMObjectReference reference = event.getReference(ScheduleEvent.SCHEDULE_TYPE_REFERENCE);
            Color colour = getColour(reference);
            if (colour != null) {
                result = new TableLayoutDataEx();
                result.setBackground(colour);
            }
        } else {
            result = super.getEventLayoutData(event, highlight);
        }
        return result;
    }

    /**
     * Formats an appointment.
     *
     * @param event the event
     * @return the appointment component
     */
    private Component getAppointment(PropertySet event) {
        Component result;
        String text = evaluate(event);
        if (text == null) {
            String customer = event.getString(ScheduleEvent.CUSTOMER_NAME);
            String patient = event.getString(ScheduleEvent.PATIENT_NAME);
            String status = getModel().getStatus(event);
            String reason = event.getString(ScheduleEvent.ACT_REASON_NAME);
            if (reason == null) {
                // fall back to the code
                reason = event.getString(ScheduleEvent.ACT_REASON);
            }

            if (patient == null) {
                if (customer == null) {
                    customer = Messages.get("workflow.scheduling.appointment.table.nocustomer");
                }
                text = Messages.format("workflow.scheduling.appointment.table.customer",
                                       customer, reason, status);
            } else {
                text = Messages.format("workflow.scheduling.appointment.table.customerpatient",
                                       customer, patient, reason, status);
            }
        }
        String notes = event.getString(ScheduleEvent.ACT_DESCRIPTION);
        boolean onlineBooking = event.exists(ScheduleEvent.ONLINE_BOOKING)
                                && event.getBoolean(ScheduleEvent.ONLINE_BOOKING);
        result = createLabelWithNotes(text, notes, event.getBoolean(ScheduleEvent.SEND_REMINDER),
                                      event.getDate(ScheduleEvent.REMINDER_SENT),
                                      event.getString(ScheduleEvent.REMINDER_ERROR), onlineBooking);
        return result;
    }

    /**
     * Formats a block.
     *
     * @param event the blocking event
     * @return the block component
     */
    private Component getBlock(PropertySet event) {
        String text = event.getString(ScheduleEvent.ACT_NAME);
        if (text == null) {
            text = event.getString(ScheduleEvent.SCHEDULE_TYPE_NAME);
        }
        String notes = event.getString(ScheduleEvent.ACT_DESCRIPTION);
        return createLabelWithNotes(text, notes);
    }

}
