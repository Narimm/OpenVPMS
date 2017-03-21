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

import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.archetype.rules.patient.reminder.ReminderEvent;
import org.openvpms.archetype.rules.patient.reminder.ReminderType;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.component.im.sms.SMSHelper;
import org.openvpms.web.workspace.reporting.ReportingException;

import java.util.List;

/**
 * Reminders to be SMS'ed to a customer.
 *
 * @author Tim Anderson
 */
public class SMSReminders extends GroupedReminders {

    /**
     * The SMS template. May be {@code null}
     */
    private final Entity smsTemplate;

    /**
     * The SMS template evaluator.
     */
    private final ReminderSMSEvaluator evaluator;

    /**
     * The reminder text.
     */
    private String text;

    /**
     * Constructs a {@link GroupedReminders}.
     *
     * @param reminders   the reminders to send
     * @param groupBy     the reminder grouping policy. This is used to determine which document template, if any, is
     *                    selected to process reminders.
     * @param cancelled   reminders that have been cancelled
     * @param errors      reminders that are in error
     * @param updated     reminders/reminder items that have been updated
     * @param resend      determines if reminders are being resent
     * @param customer    the customer the reminders are for. May be {@code null} if there are no reminders to send
     * @param contact     the contact to use. May be {@code null} if there are no reminders to send
     * @param location    the practice location. May be {@code null} if there are no reminders to send
     * @param template    the document template to use. May be {@code null} if there are no reminders to send
     * @param smsTemplate the SMS template. May be {@code null} if there are no reminders to send
     * @param evaluator   the SMS evaluator
     */
    public SMSReminders(List<ReminderEvent> reminders, ReminderType.GroupBy groupBy, List<ReminderEvent> cancelled,
                        List<ReminderEvent> errors, List<Act> updated, boolean resend, Party customer, Contact contact,
                        Party location, DocumentTemplate template, Entity smsTemplate, ReminderSMSEvaluator evaluator) {
        super(reminders, groupBy, cancelled, errors, updated, resend, customer, contact, location, template);
        this.smsTemplate = smsTemplate;
        this.evaluator = evaluator;
    }

    /**
     * Returns the contact phone number.
     *
     * @return the contact phone number
     */
    public String getPhoneNumber() {
        return SMSHelper.getPhone(getContact());
    }

    /**
     * Returns the SMS template.
     *
     * @return the template. May be {@code null} if there are no reminders to send
     */
    public Entity getSMSTemplate() {
        return smsTemplate;
    }

    /**
     * Returns the reminder SMS text.
     *
     * @param practice the practice
     * @return the text
     * @throws ReportingException if evaluation fails
     */
    public String getText(Party practice) {
        String result;
        List<ReminderEvent> reminders = getReminders();
        Party customer = getCustomer();
        Party location = getLocation();
        try {
            if (reminders.size() == 1) {
                ReminderEvent event = reminders.get(0);
                Act reminder = event.getReminder();
                Party patient = (Party) event.get("patient");
                result = evaluator.evaluate(smsTemplate, reminder, customer, patient, location, practice);
            } else {
                ReminderEvent event = reminders.get(0);
                Party patient = event.getPatient(); // pass the first patient.
                List<ObjectSet> sets = getObjectSets(reminders);
                result = evaluator.evaluate(smsTemplate, sets, customer, patient, location, practice);
            }
        } catch (Throwable exception) {
            throw new ReportingException(ReportingException.ErrorCode.SMSEvaluationFailed, exception,
                                         smsTemplate.getName());
        }
        text = result;
        return result;
    }

    /**
     * Returns the reminder SMS text.
     * <p>
     * The {@link #getText(Party)} must have been invoked.
     *
     * @return the reminder SMS text. May be {@code null}
     */
    public String getText() {
        return text;
    }

}
