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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.party;

import org.openvpms.archetype.rules.patient.reminder.ReminderQuery;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.util.Date;
import java.util.List;


/**
 * Rules for <em>party.customer*</em> objects.
 *
 * @author Tim Anderson
 */
public class CustomerRules extends PartyRules {

    /**
     * /**
     * Constructs a {@code CustomerRules}.
     *
     * @param service the archetype service
     */
    public CustomerRules(IArchetypeService service) {
        super(service);
    }

    /**
     * Returns the <em>lookup.customerAccountType</em> for a customer.
     *
     * @param party the party
     * @return the account type, or {@code null} if one doesn't exist
     */
    public Lookup getAccountType(Party party) {
        Lookup result = null;
        IMObjectBean bean = new IMObjectBean(party, getArchetypeService());
        if (bean.hasNode("type")) {
            List<Lookup> types = bean.getValues("type", Lookup.class);
            result = (types.isEmpty()) ? null : types.get(0);
        }
        return result;
    }

    /**
     * Merges two customers.
     *
     * @param from the customer to merge
     * @param to   the customer to merge to
     * @throws MergeException            if the customers cannot be merged
     * @throws ArchetypeServiceException for any archetype service error
     */
    public void mergeCustomers(Party from, Party to) {
        CustomerMerger merger = new CustomerMerger(getArchetypeService());
        merger.merge(from, to);
    }

    /**
     * Returns reminders for the specified customer's patients.
     *
     * @param customer       the customer
     * @param dueInterval    the due interval, relative to the current date
     * @param dueUnits       the due interval units
     * @param includeOverdue if {@code true}, include reminders that are overdue (i.e. those with a due date prior to
     *                       today's date)
     * @return the reminders for the customer's patients
     */
    public List<Act> getReminders(Party customer, int dueInterval, DateUnits dueUnits, boolean includeOverdue) {
        ReminderQuery query = new ReminderQuery(getArchetypeService());
        query.setCustomer(customer);
        Date from = new Date();
        Date to = DateRules.getDate(from, dueInterval, dueUnits);
        if (!includeOverdue) {
            query.setFrom(from);
        }
        query.setTo(to);
        query.setCustomer(customer);
        return query.execute();
    }

}
