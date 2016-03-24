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

package org.openvpms.web.workspace.workflow.checkout;

import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.math.Weight;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.workflow.AppointmentRules;
import org.openvpms.archetype.rules.workflow.CageType;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.system.ServiceHelper;

import java.util.Date;
import java.util.HashMap;

/**
 * Associates a patient clinical event (aka a 'visit') with an optional appointment.
 * <p/>
 * This is used to determine boarding charges for a patient.
 *
 * @author Tim Anderson
 */
class Visit {

    /**
     * The event. An <em>act.patientClinicalEvent</em>
     */
    private final Act event;

    /**
     * The appointment. May be {@code null}
     */
    private final Act appointment;

    /**
     * The patient.
     */
    private Party patient;

    /**
     * The cage type.
     */
    private CageType cageType;

    /**
     * Determines if the patient is charged the first or second pet rate.
     */
    private boolean firstPet = true;

    /**
     * The appointment rules.
     */
    private final AppointmentRules appointmentRules;

    /**
     * The patient rules.
     */
    private final PatientRules patientRules;

    /**
     * The patient weight.
     */
    private Weight weight;

    /**
     * Constructs an {@link Visit}.
     *
     * @param event            the event. An <em>act.patientClinicalEvent</em>
     * @param appointment      the appointment. May be {@code null}
     * @param appointmentRules the appointment rules
     * @param patientRules     the patient rules
     */
    public Visit(Act event, Act appointment, AppointmentRules appointmentRules, PatientRules patientRules) {
        this.event = event;
        this.appointment = appointment;
        this.appointmentRules = appointmentRules;
        this.patientRules = patientRules;
    }

    /**
     * Returns the event.
     *
     * @return the event
     */
    public Act getEvent() {
        return event;
    }

    /**
     * Returns the appointment.
     *
     * @return the appointment. May be {@code null}
     */
    public Act getAppointment() {
        return appointment;
    }

    /**
     * Returns the patient associated with the event.
     *
     * @return the patient
     */
    public Party getPatient() {
        if (patient == null) {
            ActBean bean = new ActBean(event);
            patient = (Party) bean.getNodeParticipant("patient");
            if (patient == null) {
                throw new IllegalStateException("The patient for visit=" + event.getId() + " cannot be null");
            }
        }
        return patient;
    }

    /**
     * Returns the cage type for the appointment.
     *
     * @return the cage type, or {@code null} if the cage type is unspecified
     */
    public CageType getCageType() {
        if (appointment != null && cageType == null) {
            ActBean bean = new ActBean(appointment);
            Entity schedule = bean.getNodeParticipant("schedule");
            if (schedule != null) {
                IMObjectBean scheduleBean = new IMObjectBean(schedule);
                Entity type = (Entity) scheduleBean.getNodeTargetObject("cageType");
                if (type != null) {
                    cageType = new CageType(type, ServiceHelper.getArchetypeService());
                }
            }
        }
        return cageType;
    }

    /**
     * Determines if the visit is a late checkout, relative to the current time.
     *
     * @return {@code true} if the visit is a late checkout
     */
    public boolean isLateCheckout() {
        return isLateCheckout(new Date());
    }

    /**
     * Determines if the visit is a late checkout, relative to the specified time.
     *
     * @param endTime the end time to use, if the event hasn't ended
     * @return {@code true} if the visit is a late checkout
     */
    public boolean isLateCheckout(Date endTime) {
        boolean result = false;
        CageType type = getCageType();
        if (type != null) {
            result = type.isLateCheckout(getEndTime(endTime));
        }
        return result;
    }

    /**
     * Returns the no. of days to charge for, relative to the current time.
     *
     * @return the number of days
     */
    public int getDays() {
        return getDays(new Date());
    }

    /**
     * Returns the no. of days to charge for, relative to the specified time.
     *
     * @param endTime the end time to use, if the event hasn't ended
     * @return the number of days
     */
    public int getDays(Date endTime) {
        return appointmentRules.getBoardingDays(event.getActivityStartTime(), getEndTime(endTime));
    }

    /**
     * Determines if the patient is charged the first pet boarding rate.
     *
     * @return {@code true} if the patient is charged the first pet boarding rate, {@code false} if they are charged
     * the second pet rate
     */
    public boolean isFirstPet() {
        return firstPet;
    }

    /**
     * Determines if the patient is charged the first pet boarding rate.
     *
     * @param firstPet if {@code true}, patient is charged the first pet boarding rate, otherwise they are charged
     *                 the second pet rate
     */
    public void setFirstPet(boolean firstPet) {
        this.firstPet = firstPet;
    }

    public Date getStartTime() {
        return event.getActivityStartTime();
    }

    public Date getEndTime() {
        return event.getActivityEndTime();
    }

    /**
     * Returns the visit end time.
     *
     * @param endTime the end time to use, if the event hasn't ended. May be {@code null}
     * @return the end time
     */
    public Date getEndTime(Date endTime) {
        if (event.getActivityEndTime() != null) {
            endTime = event.getActivityEndTime();
        }
        return endTime;
    }

    public IMObjectReference getScheduleRef() {
        return appointment != null ? new ActBean(appointment).getNodeParticipantRef("schedule") : null;
    }

    public Weight getWeight() {
        if (weight == null) {
            weight = patientRules.getWeight(getPatient());
        }
        return weight;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param obj the reference object with which to compare.
     * @return {@code true} if this object is the same as the obj argument; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof Visit) {
            return event.equals(((Visit) obj).event);
        }
        return false;
    }

    /**
     * Returns a hash code value for the object. This method is
     * supported for the benefit of hash tables such as those provided by
     * {@link HashMap}.
     *
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        return event.hashCode();
    }

    /**
     * Determines if the visit has been charged.
     *
     * @return {@code true} if the appointment status is {@link ActStatus#COMPLETED}.
     */
    public boolean isCharged() {
        return appointment != null && ActStatus.COMPLETED.equals(appointment.getStatus());
    }
}
