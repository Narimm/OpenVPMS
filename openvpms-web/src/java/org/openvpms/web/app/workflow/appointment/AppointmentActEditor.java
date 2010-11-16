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

package org.openvpms.web.app.workflow.appointment;

import org.openvpms.archetype.rules.workflow.AppointmentRules;
import org.openvpms.archetype.rules.workflow.AppointmentStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.app.workflow.scheduling.AbstractScheduleActEditor;
import org.openvpms.web.component.focus.FocusHelper;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.util.ErrorHelper;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;


/**
 * An editor for <em>act.customerAppointment</em>s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class AppointmentActEditor extends AbstractScheduleActEditor {

    /**
     * Constructs an <tt>AppointmentActEditor</tt>.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be <tt>null</tt>
     * @param context the layout context
     */
    public AppointmentActEditor(Act act, IMObject parent, LayoutContext context) {
        super(act, parent, context);
        initParticipant("schedule", context.getContext().getSchedule());

        Entity appointmentType = (Entity) getParticipant("appointmentType");
        if (appointmentType == null) {
            // set the appointment type to the default for the schedule
            Party schedule = (Party) getParticipant("schedule");
            if (schedule != null) {
                appointmentType = getDefaultAppointmentType(schedule);
                setParticipant("appointmentType", appointmentType);
            }
        }

        Date startTime = getStartTime();
        if (startTime == null) {
            Date scheduleDate = context.getContext().getScheduleDate();
            if (scheduleDate != null) {
                startTime = getDefaultStartTime(scheduleDate);

                setStartTime(startTime, true);
            }
        }

        getProperty("status").addModifiableListener(new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                onStatusChanged();
            }
        });
        addStartEndTimeListeners();
    }

    /**
     * Sets the schedule.
     *
     * @param schedule the schedule
     */
    public void setSchedule(Entity schedule) {
        setParticipant("schedule", schedule);
        AppointmentTypeParticipationEditor editor = getAppointmentTypeEditor();
        editor.setSchedule(schedule);
        calculateEndTime();
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return new AppointmentLayoutStrategy();
    }

    /**
     * Invoked when layout has completed. All editors have been created.
     */
    @Override
    protected void onLayoutCompleted() {
        super.onLayoutCompleted();

        Party schedule = (Party) getParticipant("schedule");
        AppointmentTypeParticipationEditor editor = getAppointmentTypeEditor();
        editor.setSchedule(schedule);
        editor.addModifiableListener(new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                onAppointmentTypeChanged();
            }
        });

        if (getEndTime() == null) {
            calculateEndTime();
        }
    }

    /**
     * Invoked when the start time changes. Calculates the end time.
     */
    @Override
    protected void onStartTimeChanged() {
        try {
            calculateEndTime();
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Invoked when the end time changes. Recalculates the end time if it
     * is less than the start time.
     */
    @Override
    protected void onEndTimeChanged() {
        Date start = getStartTime();
        Date end = getEndTime();
        if (start != null && end != null) {
            if (end.compareTo(start) < 0) {
                calculateEndTime();
            }
        }
    }

    /**
     * Invoked when the appointment type changes. Calculates the end time
     * if the start time is set.
     */
    private void onAppointmentTypeChanged() {
        try {
            calculateEndTime();
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Invoked when the status changes. Sets the arrivalTime to now if
     * the status is CHECKED_IN.
     */
    private void onStatusChanged() {
        String status = (String) getProperty("status").getValue();
        if (AppointmentStatus.CHECKED_IN.equals(status)) {
            getProperty("arrivalTime").setValue(new Date());
        }
    }

    /**
     * Calculates the default start time of an appointment, using the supplied
     * date and current time.
     * The start time is rounded to the next nearest 'slot-size' interval.
     *
     * @param date the start date
     * @return the start time
     */
    private Date getDefaultStartTime(Date date) {
        int slotSize = 0;
        Party schedule = (Party) getParticipant("schedule");
        if (schedule != null) {
            AppointmentRules rules = new AppointmentRules();
            slotSize = rules.getSlotSize(schedule);
        }

        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        Calendar timeCal = new GregorianCalendar();
        timeCal.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        if (slotSize != 0) {
            int mins = calendar.get(Calendar.MINUTE);
            mins = ((mins / slotSize) * slotSize) + slotSize;
            calendar.set(Calendar.MINUTE, mins);
        }
        return calendar.getTime();
    }

    /**
     * Calculates the end time if the start time and appointment type are set.
     *
     * @throws OpenVPMSException for any error
     */
    private void calculateEndTime() {
        Date start = getStartTime();
        Party schedule = (Party) getParticipant("schedule");
        AppointmentTypeParticipationEditor editor
                = getAppointmentTypeEditor();
        Entity appointmentType = editor.getEntity();
        if (start != null && schedule != null && appointmentType != null) {
            AppointmentRules rules = new AppointmentRules();
            Date end = rules.calculateEndTime(start, schedule, appointmentType);
            setEndTime(end);
        }
    }

    /**
     * Returns the appointment type editor.
     *
     * @return the appointment type editor
     */
    private AppointmentTypeParticipationEditor getAppointmentTypeEditor() {
        return (AppointmentTypeParticipationEditor) getEditor(
                "appointmentType");
    }

    /**
     * Returns the default appointment type associated with a schedule.
     *
     * @param schedule the schedule
     * @return the default appointment type, or the the first appointment type
     *         if there is no default, or <tt>null</tt> if none is found
     */
    private Entity getDefaultAppointmentType(Party schedule) {
        return new AppointmentRules().getDefaultAppointmentType(schedule);
    }

    private class AppointmentLayoutStrategy extends LayoutStrategy {

        /**
         * Sets focus on the customer component.
         *
         * @param components the components
         */
        @Override
        protected void setFocus(List<ComponentState> components) {
            for (ComponentState state : components) {
                Property property = state.getProperty();
                if (property != null && "customer".equals(property.getName())) {
                    FocusHelper.setFocus(state.getFocusable());
                    break;
                }
            }
        }
    }
}
