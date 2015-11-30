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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.appointment.reminder;

import org.apache.commons.jxpath.JXPathContext;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.system.common.jxpath.JXPathHelper;
import org.openvpms.macro.Macros;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.macro.MacroVariables;
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
     * The lookup service.
     */
    private final ILookupService lookups;

    /**
     * The macros.
     */
    private final Macros macros;

    /**
     * Constructs an {@link AppointmentReminderEvaluator}.
     *
     * @param service the service
     * @param lookups the lookups
     * @param macros  the macros
     */
    public AppointmentReminderEvaluator(IArchetypeService service, ILookupService lookups, Macros macros) {
        this.service = service;
        this.lookups = lookups;
        this.macros = macros;
    }

    /**
     * Evaluates an SMS appointment reminder.
     *
     * @param template    the template
     * @param appointment the appointment
     * @param location    the practice location
     * @param practice    the practice
     * @return the result of the expression
     * @throws AppointmentReminderException if the expression cannot be evaluated
     */
    public String evaluate(Entity template, Act appointment, Party location, Party practice) {
        Context context = new LocalContext();
        MacroVariables variables = new MacroVariables(context, service, lookups);
        ActBean bean = new ActBean(appointment, service);
        context.setCustomer((Party) bean.getNodeParticipant("customer"));
        context.setPatient((Party) bean.getNodeParticipant("patient"));
        context.setLocation(location);
        context.setPractice(practice);
        context.addObject(appointment);

        IMObjectBean templateBean = new IMObjectBean(template, service);
        String type = templateBean.getString("expressionType");
        String expression = templateBean.getString("expression");
        String result;
        try {
            if ("XPATH".equals(type)) {
                variables.declareVariable("nl", "\n");     // to make expressions with newlines simpler
                JXPathContext jxPathContext = JXPathHelper.newContext(appointment);
                jxPathContext.setVariables(variables);
                result = (String) jxPathContext.getValue(expression, String.class);
            } else {
                result = macros.runAll(expression, appointment, variables, null, true);
            }
        } catch (Throwable exception) {
            throw new AppointmentReminderException(Messages.format("sms.appointment.evaluatefailed",
                                                                   template.getName()), exception);
        }
        return result;
    }
}
