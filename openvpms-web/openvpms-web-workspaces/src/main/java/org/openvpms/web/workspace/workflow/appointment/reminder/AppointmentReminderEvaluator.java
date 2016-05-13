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

package org.openvpms.web.workspace.workflow.appointment.reminder;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.sms.SMSTemplateEvaluator;
import org.openvpms.web.resource.i18n.Messages;

/**
 * Evaluates appointment reminder expressions from an <em>entity.documentTemplateSMSAppointment</em>
 *
 * @author Tim Anderson
 */
public class AppointmentReminderEvaluator {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The template evaluator.
     */
    private final SMSTemplateEvaluator evaluator;

    /**
     * Constructs an {@link AppointmentReminderEvaluator}.
     *
     * @param service   the service
     * @param evaluator the lookups
     */
    public AppointmentReminderEvaluator(IArchetypeService service, SMSTemplateEvaluator evaluator) {
        this.service = service;
        this.evaluator = evaluator;
    }

    /**
     * Evaluates an SMS appointment reminder template against an appointment.
     * <p/>
     * The customer, patient and appointment are available as variables.
     *
     * @param template    the template
     * @param appointment the appointment
     * @param location    the practice location
     * @param practice    the practice
     * @return the result of the expression
     * @throws AppointmentReminderException if the expression cannot be evaluated
     */
    public String evaluate(Entity template, Act appointment, Party location, Party practice) {
        String result;
        Context context = new LocalContext();
        ActBean bean = new ActBean(appointment, service);
        context.setCustomer((Party) bean.getNodeParticipant("customer"));
        context.setPatient((Party) bean.getNodeParticipant("patient"));
        context.setLocation(location);
        context.setPractice(practice);
        context.addObject(appointment);
        try {
            result = evaluator.evaluate(template, appointment, context);
        } catch (Throwable exception) {
            throw new AppointmentReminderException(Messages.format("sms.appointment.evaluatefailed",
                                                                   template.getName()), exception);
        }
        return result;
    }
}
