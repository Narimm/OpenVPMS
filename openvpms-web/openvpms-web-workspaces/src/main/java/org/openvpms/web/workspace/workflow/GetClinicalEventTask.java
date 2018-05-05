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
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.web.component.app.ContextException;
import org.openvpms.web.component.workflow.SynchronousTask;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.component.workflow.TaskProperties;
import org.openvpms.web.echo.dialog.InformationDialog;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Queries the most recent <em>act.patientClinicalEvent</em> for the context patient,
 * creating one if it doesn't exist.
 *
 * @author Tim Anderson
 */
public class GetClinicalEventTask extends SynchronousTask {

    /**
     * The date to use to locate the event.
     */
    private final Date date;

    /**
     * Properties to populate the created object with. May be {@code null}
     */
    private final TaskProperties properties;

    /**
     * The appointment. May be {@code null}
     */
    private final Act appointment;

    /**
     * Determines if a new event is required.
     */
    private final boolean newEvent;

    /**
     * The medical record rules.
     */
    private final MedicalRecordRules rules;

    /**
     * Constructs a {@link GetClinicalEventTask}.
     *
     * @param date the date to use to locate the event
     */
    public GetClinicalEventTask(Date date) {
        this(date, null);
    }

    /**
     * Constructs a {@link GetClinicalEventTask}.
     *
     * @param date        the date to use to locate the event
     * @param appointment the appointment used to locate the event. May be {@code null}
     */
    public GetClinicalEventTask(Date date, Act appointment) {
        this(date, null, appointment, false);
    }

    /**
     * Constructs a {@link GetClinicalEventTask}.
     *
     * @param date        the date to use to locate the event
     * @param properties  properties to populate any created event. May be {@code null}
     * @param appointment the appointment. If specified, this will be linked to new events. May be {@code null}
     * @param newEvent    if {@code true}, require a new event, unless the appointment links to one already.
     *                    If not, and there is an In Progress event, terminate the task
     */
    public GetClinicalEventTask(Date date, TaskProperties properties, Act appointment, boolean newEvent) {
        this.date = date;
        this.properties = properties;
        this.appointment = appointment;
        this.newEvent = newEvent;
        rules = ServiceHelper.getBean(MedicalRecordRules.class);
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
     * Executes the task.
     *
     * @throws OpenVPMSException for any error
     */
    public void execute(TaskContext context) {
        Party patient = context.getPatient();
        if (patient == null) {
            throw new ContextException(ContextException.ErrorCode.NoPatient);
        }
        Entity clinician = context.getClinician();
        Act event;
        if (!newEvent && appointment != null) {
            // an event should already exist for the appointment.
            AppointmentRules appointmentRules = ServiceHelper.getBean(AppointmentRules.class);
            if (appointmentRules.isBoardingAppointment(appointment)) {
                // for boarding appointments, there must be an event associated with the appointment
                ActBean bean = new ActBean(appointment);
                event = (Act) bean.getNodeTargetObject("event");
                if (event == null) {
                    InformationDialog.show(Messages.format("workflow.checkin.visit.novisit", patient.getName(),
                                                           appointment.getActivityStartTime()));
                    notifyCancelled();
                }
            } else {
                event = getEvent(patient, clinician);
            }
        } else {
            event = getEvent(patient, clinician);
        }
        if (event != null) {
            if (event.isNew()) {
                populate(event, context);
            } else if (newEvent) {
                // a new event is required, unless the appointment links to an incomplete event
                if (!ActStatus.COMPLETED.equals(event.getStatus())) {
                    if (appointment == null || !hasEvent(appointment, event)) {
                        InformationDialog.show(Messages.format("workflow.checkin.visit.exists", patient.getName(),
                                                               event.getActivityStartTime()));
                        notifyCancelled();
                    }
                } else {
                    event = rules.createEvent(patient, date, clinician);
                    populate(event, context);
                }
            }
            // TODO - need to check if a non-boarding appointment would re-use an existing boarding visit.
            context.addObject(event);
        }
    }

    /**
     * Returns an event that may have acts added.
     *
     * @param patient   the patient
     * @param clinician the clinician. May be {@code null}
     * @return an event
     */
    protected Act getEvent(Party patient, Entity clinician) {
        return rules.getEventForAddition(patient, date, clinician);
    }

    /**
     * Populates an event.
     *
     * @param event   the event to populate
     * @param context the task context
     */
    protected void populate(Act event, TaskContext context) {
        event.setStatus(ActStatus.IN_PROGRESS);
        if (properties != null) {
            populate(event, properties, context);
        }
        ActBean bean = new ActBean(event, ServiceHelper.getArchetypeService());
        bean.addNodeParticipation("location", context.getLocation());

        List<Act> toSave = new ArrayList<>();
        toSave.add(event);
        if (appointment != null) {
            ActBean appointmentBean = new ActBean(appointment);
            appointmentBean.addNodeRelationship("event", event);
            toSave.add(appointment);
        }
        ServiceHelper.getArchetypeService().save(toSave);
    }

    /**
     * Determines if an appointment is linked to an event.
     *
     * @param appointment the appointment
     * @param event the event
     * @return {@code true} if the appointment is linked to the event
     */
    private boolean hasEvent(Act appointment, Act event) {
        ActBean bean = new ActBean(appointment);
        IMObjectReference eventRef = bean.getNodeTargetObjectRef("event");
        return ObjectUtils.equals(eventRef, event.getObjectReference());
    }

}
