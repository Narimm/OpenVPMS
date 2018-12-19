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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow;

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.patient.MedicalRecordRules;
import org.openvpms.archetype.rules.workflow.AppointmentRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.object.Reference;
import org.openvpms.web.resource.i18n.Messages;

import java.util.Date;

/**
 * Factory for <em>act.patientClinicalEvent</em>s.
 *
 * @author Tim Anderson
 */
public class ClinicalEventFactory {

    /**
     * The medical record rules.
     */
    private final MedicalRecordRules recordRules;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The appointment rules.
     */
    private AppointmentRules appointmentRules;


    /**
     * Constructs a {@link ClinicalEventFactory}.
     *
     * @param service          the archetype service
     * @param recordRules      the medical record rules
     * @param appointmentRules the appointment rules
     */
    public ClinicalEventFactory(IArchetypeService service, MedicalRecordRules recordRules,
                                AppointmentRules appointmentRules) {
        this.service = service;
        this.recordRules = recordRules;
        this.appointmentRules = appointmentRules;
    }

    /**
     * Returns the event for a patient based on the specified criteria.
     *
     * @param date        the date to use to locate the event
     * @param patient     the patient
     * @param clinician   the clinician. May be {@code null}
     * @param appointment the appointment. If specified, this will be linked to new events. May be {@code null}
     * @param reason      a reason for the event
     * @param location    the practice location
     * @param newEvent    if {@code true}, require a new event, unless the appointment links to one already.
     *                    If not, and there is an In Progress event, throw an {@code IllegalStateException}
     * @throws IllegalStateException if a new event cannot be created
     * @throws OpenVPMSException     for any other error
     */
    public Act getEvent(Date date, Party patient, User clinician, Act appointment, String reason, Party location,
                        boolean newEvent) {
        Act event;
        if (!newEvent && appointment != null) {
            // an event should already exist for the appointment.
            if (appointmentRules.isBoardingAppointment(appointment)) {
                // for boarding appointments, there must be an event associated with the appointment
                IMObjectBean bean = service.getBean(appointment);
                event = bean.getTarget("event", Act.class);
                if (event == null) {
                    throw new IllegalStateException(Messages.format("workflow.checkin.visit.novisit", patient.getName(),
                                                                    appointment.getActivityStartTime()));
                }
            } else {
                event = getEvent(date, patient, clinician);
            }
        } else {
            event = getEvent(date, patient, clinician);
        }
        if (event != null) {
            if (event.isNew()) {
                populate(event, reason, location);
            } else if (newEvent) {
                // a new event is required, unless the appointment links to an incomplete event
                if (!ActStatus.COMPLETED.equals(event.getStatus())) {
                    if (appointment == null || !hasEvent(appointment, event)) {
                        throw new IllegalStateException(Messages.format("workflow.checkin.visit.exists",
                                                                        patient.getName(),
                                                                        event.getActivityStartTime()));
                    }
                } else {
                    event = recordRules.createEvent(patient, date, clinician);
                    populate(event, reason, location);
                }
            }
            // TODO - need to check if a non-boarding appointment would re-use an existing boarding visit.
        }
        return event;
    }

    /**
     * Returns an event that may have acts added.
     *
     * @param date      the event start time
     * @param patient   the patient
     * @param clinician the clinician. May be {@code null}
     * @return an event
     */
    protected Act getEvent(Date date, Party patient, Entity clinician) {
        return recordRules.getEventForAddition(patient, date, clinician);
    }

    /**
     * Populates an event.
     *
     * @param event    the event to populate
     * @param reason   a reason for the event
     * @param location the practice location
     */
    protected void populate(Act event, String reason, Party location) {
        event.setStatus(ActStatus.IN_PROGRESS);
        event.setReason(reason);
        IMObjectBean bean = service.getBean(event);
        bean.setTarget("location", location);
    }

    /**
     * Determines if an appointment is linked to an event.
     *
     * @param appointment the appointment
     * @param event       the event
     * @return {@code true} if the appointment is linked to the event
     */
    private boolean hasEvent(Act appointment, Act event) {
        IMObjectBean bean = service.getBean(appointment);
        Reference eventRef = bean.getTargetRef("event");
        return ObjectUtils.equals(eventRef, event.getObjectReference());
    }

}

