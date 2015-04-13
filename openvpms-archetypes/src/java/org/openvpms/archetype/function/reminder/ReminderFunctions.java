/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package org.openvpms.archetype.function.reminder;

import org.openvpms.archetype.rules.customer.CustomerArchetypes;
import org.openvpms.archetype.rules.party.CustomerRules;
import org.openvpms.archetype.rules.patient.reminder.ReminderRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;

import java.util.Collections;
import java.util.List;

/**
 * JXPath extension functions for reminders.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class ReminderFunctions {

    /**
     * The reminder rules.
     */
    private final ReminderRules rules;

    /**
     * The customer rules.
     */
    private final CustomerRules customerRules;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Constructs a <tt>ReminderFunctions</tt>.
     *
     * @param archetypeService the archetype service
     * @param rules            the reminder rules
     * @param customerRules    the customer rules
     */
    public ReminderFunctions(IArchetypeService archetypeService, ReminderRules rules, CustomerRules customerRules) {
        this.service = archetypeService;
        this.rules = rules;
        this.customerRules = customerRules;
    }

    /**
     * Returns reminders for a customer's patients for the customer associated with the supplied act.
     *
     * @param act         the act
     * @param dueInterval the due interval, relative to the current date
     * @param dueUnits    the due interval units
     * @return the reminders for the customer's patients
     */
    public List<Act> getReminders(Act act, int dueInterval, String dueUnits) {
        return getReminders(act, dueInterval, dueUnits, false);
    }

    /**
     * Returns reminders for a customer's patients for the customer associated with the supplied act.
     *
     * @param act            the act
     * @param dueInterval    the due interval, relative to the current date
     * @param dueUnits       the due interval units
     * @param includeOverdue if <tt>true</tt>, include reminders that are overdue
     * @return the reminders for the customer's patients
     */
    public List<Act> getReminders(Act act, int dueInterval, String dueUnits, boolean includeOverdue) {
        List<Act> result;
        ActBean bean = new ActBean(act, service);
        Party customer = (Party) bean.getParticipant(CustomerArchetypes.CUSTOMER_PARTICIPATION);
        if (customer != null) {
            result = getReminders(customer, dueInterval, dueUnits, includeOverdue);
        } else {
            result = Collections.emptyList();
        }
        return result;
    }

    /**
     * Returns reminders for the specified customer's patients.
     *
     * @param customer    the customer
     * @param dueInterval the due interval, relative to the current date
     * @param dueUnits    the due interval units
     * @return the reminders for the customer's patients
     */
    public List<Act> getReminders(Party customer, int dueInterval, String dueUnits) {
        return getReminders(customer, dueInterval, dueUnits, false);
    }

    /**
     * Returns reminders for the specified customer's patients.
     *
     * @param customer       the customer
     * @param dueInterval    the due interval, relative to the current date
     * @param dueUnits       the due interval units
     * @param includeOverdue if <tt>true</tt>, include reminders that are overdue
     * @return the reminders for the customer's patients
     */
    public List<Act> getReminders(Party customer, int dueInterval, String dueUnits, boolean includeOverdue) {
        if (customer != null) {
            DateUnits units = DateUnits.valueOf(dueUnits);
            return customerRules.getReminders(customer, dueInterval, units, includeOverdue);
        }
        return Collections.emptyList();
    }

    /**
     * Returns a reminder associated with an <em>act.patientDocumentForm</em>.
     *
     * @param form the form
     * @return the reminder, or <tt>null</tt> if there are no associated reminders
     */
    public Act getDocumentFormReminder(DocumentAct form) {
        return rules.getDocumentFormReminder(form);
    }

}
