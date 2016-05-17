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

package org.openvpms.web.workspace.admin.template;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectCreator;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.workflow.appointment.reminder.AppointmentReminderEvaluator;

import java.util.Date;

import static org.openvpms.archetype.rules.workflow.ScheduleArchetypes.APPOINTMENT;

/**
 * A component to test the expression evaluation of an <em>entity.documentTemplateSMSAppointment</em>.
 *
 * @author Tim Anderson
 */
public class SMSAppointmentTemplateSampler extends SMSTemplateSampler {

    /**
     * The template evaluator.
     */
    private AppointmentReminderEvaluator evaluator;

    /**
     * Constructs an {@link SMSAppointmentTemplateSampler}.
     *
     * @param template      the template
     * @param layoutContext the layout context
     */
    public SMSAppointmentTemplateSampler(Entity template, LayoutContext layoutContext) {
        super(template, layoutContext);
        evaluator = ServiceHelper.getBean(AppointmentReminderEvaluator.class);
    }

    /**
     * Evaluates the template.
     *
     * @param template the template
     * @param context  the context
     * @return the result of the evaluation. May be {@code null}
     */
    @Override
    protected String evaluate(Entity template, Context context) {
        Act act = (Act) IMObjectCreator.create(APPOINTMENT);
        ActBean bean = new ActBean(act);
        bean.setNodeParticipant("customer", getCustomer());
        bean.setNodeParticipant("patient", getPatient());
        act.setActivityStartTime(new Date());
        act.setActivityEndTime(new Date());
        return evaluator.evaluate(template, act, context.getLocation(), context.getPractice());
    }

}
