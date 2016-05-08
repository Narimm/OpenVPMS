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
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;

/**
 * Appointment series.
 *
 * @author Tim Anderson
 */
public class AppointmentSeries extends ScheduleEventSeries {

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
     * Populates an event from state.
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
                        .isEquals();
            }
            return result;
        }

    }

}
