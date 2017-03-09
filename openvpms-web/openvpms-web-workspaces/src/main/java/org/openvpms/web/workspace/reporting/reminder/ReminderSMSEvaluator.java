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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.reporting.reminder;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.sms.SMSTemplateEvaluator;
import org.openvpms.web.workspace.reporting.ReportingException;

import java.util.List;

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
     * Evaluates a template against a patient reminder.
     *
     * @param template the template
     * @param reminder the reminder
     * @param customer the customer
     * @param patient  the patient
     * @param location the practice location
     * @param practice the practice
     * @return the result of the expression. May be {@code null}, or too long for an SMS
     */
    public String evaluate(Entity template, Act reminder, Party customer, Party patient, Party location,
                           Party practice) {
        String result;
        Context local = new LocalContext();
        local.setCustomer(customer);
        local.setPatient(patient);
        local.setLocation(location);
        local.setPractice(practice);
        try {
            result = evaluator.evaluate(template, reminder, local);
        } catch (Throwable exception) {
            throw new ReportingException(ReportingException.ErrorCode.SMSEvaluationFailed, exception,
                                         template.getName());
        }
        return result;
    }

    /**
     * Evaluates a template against a collection of reminders.
     *
     * @param template  the template
     * @param reminders the reminders
     * @param customer  the customer
     * @param patient   the patient
     * @param location  the practice location
     * @param practice  the practice
     * @return the result of the expression. May be {@code null}, or too long for an SMS
     */
    public String evaluate(Entity template, List<ObjectSet> reminders, Party customer, Party patient,
                           Party location, Party practice) {
        String result;
        Context local = new LocalContext();
        local.setCustomer(customer);
        local.setPatient(patient);
        local.setLocation(location);
        local.setPractice(practice);
        try {
            result = evaluator.evaluate(template, reminders, local);
        } catch (Throwable exception) {
            throw new ReportingException(ReportingException.ErrorCode.SMSEvaluationFailed, exception,
                                         template.getName());
        }
        return result;
    }
}
