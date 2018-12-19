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

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.web.component.app.ContextException;
import org.openvpms.web.component.workflow.SynchronousTask;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.echo.dialog.InformationDialog;
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
     * The event reason. May be {@code null}
     */
    private final String reason;

    /**
     * The appointment. May be {@code null}
     */
    private final Act appointment;

    /**
     * Determines if a new event is required.
     */
    private final boolean newEvent;

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
     * @param reason      the event reason. May be {@code null}
     * @param appointment the appointment. If specified, this will be linked to new events. May be {@code null}
     * @param newEvent    if {@code true}, require a new event, unless the appointment links to one already.
     *                    If not, and there is an In Progress event, terminate the task
     */
    public GetClinicalEventTask(Date date, String reason, Act appointment, boolean newEvent) {
        this.date = date;
        this.reason = reason;
        this.appointment = appointment;
        this.newEvent = newEvent;
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
        Party location = context.getLocation();
        if (location == null) {
            throw new ContextException(ContextException.ErrorCode.NoLocation);
        }

        User clinician = context.getClinician();
        Act event;
        ClinicalEventFactory factory = new ClinicalEventFactory();
        try {
            event = factory.getEvent(date, patient, clinician, appointment, reason, context.getLocation(), newEvent);

            if (event.isNew()) {
                List<Act> toSave = new ArrayList<>();
                toSave.add(event);
                IArchetypeService service = ServiceHelper.getArchetypeService();
                if (appointment != null) {
                    IMObjectBean appointmentBean = service.getBean(appointment);
                    appointmentBean.addTarget("event", event, "appointment");
                    toSave.add(appointment);
                }
                service.save(toSave);
            }

            // TODO - need to check if a non-boarding appointment would re-use an existing boarding visit.
            context.addObject(event);
        } catch (IllegalStateException exception) {
            InformationDialog.show(exception.getMessage());
            notifyCancelled();
        }
    }

}
