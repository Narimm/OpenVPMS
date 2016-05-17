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

package org.openvpms.web.workspace.reporting.reminder;

import org.openvpms.archetype.rules.patient.reminder.ReminderEvent;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.sms.SMSTemplateEvaluator;
import org.openvpms.web.workspace.reporting.ReportingException;

/**
 * Evaluates <em>entity.documentTemplateSMSReminder</em> templates.
 *
 * @author Tim Anderson
 */
public class ReminderSMSEvaluator {

    /**
     * The template evaluator.
     */
    private final SMSTemplateEvaluator evaluator;

    /**
     * Constructs an {@link ReminderSMSEvaluator}.
     *
     * @param evaluator the lookups
     */
    public ReminderSMSEvaluator(SMSTemplateEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    /**
     * Evaluates a template against a reminder event.
     *
     * @param template the template
     * @param event    the reminder event
     * @param location the practice location
     * @param practice the practice
     * @return the result of the expression. May be {@code null}, or too long for an SMS
     */
    public String evaluate(Entity template, ReminderEvent event, Party location, Party practice) {
        String result;
        Context local = new LocalContext();
        local.setCustomer(event.getCustomer());
        local.setPatient(event.getPatient());
        local.setLocation(location);
        local.setPractice(practice);
        try {
            result = evaluator.evaluate(template, event.getReminder(), local);
        } catch (Throwable exception) {
            throw new ReportingException(ReportingException.ErrorCode.SMSEvaluationFailed, exception,
                                         template.getName());
        }
        return result;
    }
}
