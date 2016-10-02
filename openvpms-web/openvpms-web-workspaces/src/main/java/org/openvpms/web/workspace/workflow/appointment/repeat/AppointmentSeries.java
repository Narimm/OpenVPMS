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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.appointment.repeat;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.joda.time.Period;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.workflow.Times;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;

import java.util.Date;

/**
 * Appointment series.
 *
 * @author Tim Anderson
 */
public class AppointmentSeries extends CalendarEventSeries {

    /**
     * The period for new reminders during which reminders should not be scheduled, or {@code null} if
     * appointment reminders aren't configured.
     */
    private Period noReminderPeriod;

    /**
     * Constructs an {@link AppointmentSeries}.
     *
     * @param appointment the appointment
     * @param service     the archetype service
     */
    public AppointmentSeries(Act appointment, IArchetypeService service) {
        super(appointment, service);
    }

    /**
     * Sets the period before which there may be no appointment reminders.
     *
     * @param noReminderPeriod the no-reminder period, or {@code null} if appointment reminders are disabled
     */
    public void setNoReminderPeriod(Period noReminderPeriod) {
        this.noReminderPeriod = noReminderPeriod;
    }

    /**
     * Copies state.
     *
     * @param state the state to copy
     * @return a copy of {@code state}
     */
    @Override
    protected State copy(State state) {
        return new AppointmentState((AppointmentState) state);
    }

    /**
     * Creates state from an act.
     *
     * @param bean the act bean
     * @return a new state
     */
    @Override
    protected State createState(ActBean bean) {
        return new AppointmentState(bean);
    }

    /**
     * Determines if the series can be calculated.
     *
     * @param state the current event state
     * @return {@code true} if the series can be calculated
     */
    @Override
    protected boolean canCalculateSeries(State state) {
        return super.canCalculateSeries(state) && ((AppointmentState) state).getAppointmentType() != null;
    }

    /**
     * Updates an event.
     * <p/>
     * This sets the sendReminder flag for future appointments.
     *
     * @param act   the event
     * @param times the event times
     * @param state the state to populate the event from
     * @return the event
     */
    @Override
    protected ActBean populate(Act act, Times times, State state) {
        ActBean bean = super.populate(act, times, state);
        AppointmentState appointment = (AppointmentState) state;
        if (noReminderPeriod != null) {
            boolean sendReminder = false;
            Date now = new Date();
            Date startTime = act.getActivityStartTime();
            if (startTime.after(now)) {
                if (appointment.getSendReminder()) {
                    if (act.isNew()) {
                        // for new appointments only send reminders if the start time is after the no reminder period
                        Date to = DateRules.plus(now, noReminderPeriod);
                        if (startTime.after(to)) {
                            sendReminder = true;
                        }
                    } else {
                        sendReminder = true;
                    }
                }
                bean.setValue("sendReminder", sendReminder);
            }
        }
        return bean;
    }

    /**
     * Populates an event from state. This is invoked after the event times and schedule have been set.
     *
     * @param bean  the event bean
     * @param state the state
     */
    protected void populate(ActBean bean, State state) {
        super.populate(bean, state);
        Act act = bean.getAct();
        AppointmentState appointment = (AppointmentState) state;
        bean.setNodeParticipant("appointmentType", appointment.getAppointmentType());
        act.setStatus(appointment.getStatus());
        bean.setNodeParticipant("customer", appointment.getCustomer());
        bean.setNodeParticipant("patient", appointment.getPatient());
        bean.setNodeParticipant("clinician", appointment.getClinician());
        act.setReason(appointment.getReason());
        act.setDescription(appointment.getNotes());
    }

    /**
     * Appointment series state.
     */
    static class AppointmentState extends State {

        /**
         * The appointment type.
         */
        private IMObjectReference appointmentType;

        /**
         * The status.
         */
        private String status;

        /**
         * The customer.
         */
        private IMObjectReference customer;

        /**
         * The patient.
         */
        private IMObjectReference patient;

        /**
         * The clinician.
         */
        private IMObjectReference clinician;

        /**
         * The appointment reason.
         */
        private String reason;

        /**
         * The appointment notes.
         */
        private String notes;

        /**
         * Determines if reminders should be sent.
         */
        private boolean sendReminder;

        /**
         * Initialises the state from an appointment.
         *
         * @param appointment the appointment
         */
        public AppointmentState(ActBean appointment) {
            super(appointment);
        }

        /**
         * Updates the state from an appointment.
         *
         * @param appointment the appointment
         */
        @Override
        public void update(ActBean appointment) {
            super.update(appointment);
            Act act = appointment.getAct();
            appointmentType = appointment.getNodeParticipantRef("appointmentType");
            status = act.getStatus();
            customer = appointment.getNodeParticipantRef("customer");
            patient = appointment.getNodeParticipantRef("patient");
            clinician = appointment.getNodeParticipantRef("clinician");
            reason = appointment.getString("reason");
            notes = appointment.getString("description");
            sendReminder = appointment.getBoolean("sendReminder");
        }

        /**
         * Copy constructor.
         *
         * @param state the state to copy
         */
        public AppointmentState(AppointmentState state) {
            super(state);
            this.appointmentType = state.appointmentType;
            this.status = state.status;
            this.customer = state.customer;
            this.patient = state.patient;
            this.clinician = state.clinician;
            this.reason = state.reason;
            this.notes = state.notes;
            this.sendReminder = state.sendReminder;
        }

        public IMObjectReference getAppointmentType() {
            return appointmentType;
        }

        public String getStatus() {
            return status;
        }

        public IMObjectReference getCustomer() {
            return customer;
        }

        public IMObjectReference getPatient() {
            return patient;
        }

        public IMObjectReference getClinician() {
            return clinician;
        }

        public String getReason() {
            return reason;
        }

        public String getNotes() {
            return notes;
        }

        public boolean getSendReminder() {
            return sendReminder;
        }

        /**
         * Indicates whether some other object is "equal to" this one.
         *
         * @param obj the reference object with which to compare.
         * @return {@code true} if this object is the same as the obj
         */
        @Override
        public boolean equals(Object obj) {
            boolean result = false;
            if (obj instanceof AppointmentState && super.equals(obj)) {
                AppointmentState other = (AppointmentState) obj;
                result = new EqualsBuilder()
                        .append(appointmentType, other.appointmentType)
                        .append(status, other.status)
                        .append(customer, other.customer)
                        .append(patient, other.patient)
                        .append(clinician, other.clinician)
                        .append(reason, other.reason)
                        .append(notes, other.notes)
                        .append(sendReminder, other.sendReminder)
                        .isEquals();
            }
            return result;
        }

    }

}
