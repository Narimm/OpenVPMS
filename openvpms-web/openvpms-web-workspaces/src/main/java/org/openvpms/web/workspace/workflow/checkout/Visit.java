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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.checkout;

import org.openvpms.archetype.rules.math.Weight;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.workflow.AppointmentRules;
import org.openvpms.archetype.rules.workflow.CageType;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.bean.Policies;
import org.openvpms.component.model.object.Reference;

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
     * Appointment bean. May be {@code null}
     */
    private final IMObjectBean appointment;

    /**
     * The appointment rules.
     */
    private final AppointmentRules appointmentRules;

    /**
     * The patient rules.
     */
    private final PatientRules patientRules;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The event. An <em>act.patientClinicalEvent</em>
     */
    private Act event;

    /**
     * The patient.
     */
    private Party patient;

    /**
     * The cage type.
     */
    private CageType cageType;

    /**
     * The patient weight.
     */
    private Weight weight;

    /**
     * Determines if the appointment has been changed.
     */
    private boolean changed;

    /**
     * The 'boarding charged' node name.
     */
    static final String BOARDING_CHARGED = "boardingCharged";

    /**
     * The 'first pet rate' node name.
     */
    static final String FIRST_PET_RATE = "firstPetRate";

    /**
     * Constructs an {@link Visit}.
     *
     * @param event            the event. An <em>act.patientClinicalEvent</em>
     * @param appointment      the appointment. May be {@code null}
     * @param appointmentRules the appointment rules
     * @param patientRules     the patient rules
     * @param service          the archetype service
     */
    public Visit(Act event, Act appointment, AppointmentRules appointmentRules, PatientRules patientRules,
                 IArchetypeService service) {
        this.event = event;
        this.appointment = appointment != null ? service.getBean(appointment) : null;
        this.appointmentRules = appointmentRules;
        this.patientRules = patientRules;
        this.service = service;
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
     * Reloads and returns the latest instance of the event.
     *
     * @return the event, or {@code null} if it has been removed
     */
    public Act reloadEvent() {
        Act act = (Act) service.get(event.getObjectReference());
        if (act != null) {
            event = act;
        }
        return act;
    }

    /**
     * Returns the appointment.
     *
     * @return the appointment. May be {@code null}
     */
    public Act getAppointment() {
        return (appointment != null) ? (Act) appointment.getObject() : null;
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
            Entity schedule = appointment.getTarget("schedule", Entity.class);
            if (schedule != null) {
                IMObjectBean scheduleBean = service.getBean(schedule);
                Entity type = scheduleBean.getTarget("cageType", Entity.class, Policies.active());
                if (type != null) {
                    cageType = new CageType(type, service);
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
     * @return the number of days to charge for
     */
    public int getDays() {
        return getDays(new Date());
    }

    /**
     * Returns the no. of days to charge for, relative to the specified time.
     * <p/>
     * This is determined by the number of nights stayed.
     *
     * @param endTime the end time to use, if the event hasn't ended
     * @return the number of days to charge for
     */
    public int getDays(Date endTime) {
        return appointmentRules.getBoardingNights(event.getActivityStartTime(), getEndTime(endTime));
    }

    /**
     * Determines if the pet is staying overnight.
     *
     * @param endTime the end time to use, if the event hasn't ended
     * @return {@code true} if the pet is staying overnight
     */
    public boolean isOvernight(Date endTime) {
        return !DateRules.dateEquals(event.getActivityStartTime(), getEndTime(endTime));
    }

    /**
     * Determines if the patient is charged the first pet boarding rate.
     *
     * @return {@code true} if the patient is charged the first pet boarding rate, {@code false} if they are charged
     * the second pet rate
     */
    public boolean isFirstPet() {
        return appointment != null && appointment.getBoolean(FIRST_PET_RATE, false);
    }

    /**
     * Determines if the patient is charged the first pet boarding rate.
     *
     * @param firstPet if {@code true}, patient is charged the first pet boarding rate, otherwise they are charged
     *                 the second pet rate
     */
    public void setFirstPet(boolean firstPet) {
        if (appointment != null && firstPet != isFirstPet()) {
            appointment.setValue(FIRST_PET_RATE, firstPet);
            changed = true;
        }
    }

    /**
     * Returns the event start time.
     *
     * @return the event start
     */
    public Date getStartTime() {
        return event.getActivityStartTime();
    }

    /**
     * The event end time.
     *
     * @return the event end time
     */
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

    /**
     * Returns a reference to the appointment schedule.
     *
     * @return a reference to the appointment schedule, or {@code null} if it is not known
     */
    public Reference getScheduleRef() {
        return appointment != null ? appointment.getTargetRef("schedule") : null;
    }

    /**
     * Returns the patient weight.
     *
     * @return the patient weight
     */
    public Weight getWeight() {
        if (weight == null) {
            weight = patientRules.getWeight(getPatient());
        }
        return weight;
    }

    /**
     * Determines if the visit has been charged.
     *
     * @return {@code true} if the appointment has its boardingCharged flag set.
     */
    public boolean isCharged() {
        return appointment != null && appointment.getBoolean(BOARDING_CHARGED, false);
    }

    /**
     * Determines if the visit needs to be charged.
     *
     * @return {@code true} if the visit hasn't been charged and is linked to a cage type with a product
     */
    public boolean needsCharge() {
        boolean result = false;
        if (!isCharged()) {
            result = getProduct(new Date()) != null;
        }
        return result;
    }

    /**
     * Returns the product to charge if the visit was to end at the specified time.
     * <p/>
     * If the visit has already completed, its end time will be used.
     *
     * @param endTime the end time
     * @return the product, or {@code null} if the visit doesn't attract a boarding fee
     */
    public Product getProduct(Date endTime) {
        Product result = null;
        CageType cageType = getCageType();
        if (cageType != null) {
            Date visitEndTime = getEndTime(endTime);
            int days = getDays(visitEndTime);
            boolean overnight = days > 1 || isOvernight(visitEndTime);
            result = cageType.getProduct(days, overnight, isFirstPet());
        }
        return result;
    }

    /**
     * Determines if the visit has been charged.
     *
     * @param charged if {@code true}, indicates that boarding charges have been applied
     */
    public void setCharged(boolean charged) {
        if (appointment != null && charged != isCharged()) {
            appointment.setValue(BOARDING_CHARGED, charged);
            changed = true;
        }
    }

    /**
     * Saves the appointment if it has changed.
     */
    public void save() {
        if (changed && appointment != null) {
            appointment.save();
        }
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

}
