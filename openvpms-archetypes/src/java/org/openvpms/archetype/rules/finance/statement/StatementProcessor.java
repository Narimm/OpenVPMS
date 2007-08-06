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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.finance.statement;

import org.openvpms.archetype.component.processor.AbstractActionProcessor;
import static org.openvpms.archetype.rules.finance.statement.StatementEvent.Action.*;
import static org.openvpms.archetype.rules.finance.statement.StatementProcessorException.ErrorCode.InvalidStatementDate;
import static org.openvpms.archetype.rules.finance.statement.StatementProcessorException.ErrorCode.NoContact;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;

import java.util.Calendar;
import java.util.Date;
import java.util.List;


/**
 * Processor for customer statements.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class StatementProcessor
        extends AbstractActionProcessor<StatementEvent.Action, Party,
        StatementEvent> {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The statement rules.
     */
    private final StatementRules statementRules;

    /**
     * The statement date.
     */
    private final Date statementDate;

    /**
     * Statement act helper.
     */
    private final StatementActHelper actHelper;


    /**
     * Creates a new <tt>DefaultStatementProcessor</tt>.
     *
     * @param statementDate the statement date. Must be a date prior to today.
     * @throws StatementProcessorException if the statement date is invalid
     */
    public StatementProcessor(Date statementDate) {
        this(statementDate, ArchetypeServiceHelper.getArchetypeService());
    }

    /**
     * Creates a new <tt>DefaultStatementProcessor</tt>.
     *
     * @param statementDate the statement date. Must be a date prior to today.
     * @param service       the archetype service
     * @throws StatementProcessorException if the statement date is invalid
     */
    public StatementProcessor(Date statementDate, IArchetypeService service) {
        this.service = service;
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        if (calendar.getTime().compareTo(statementDate) < 0) {
            throw new StatementProcessorException(InvalidStatementDate,
                                                  statementDate);
        }
        this.statementDate = statementDate;
        statementRules = new StatementRules(service);
        actHelper = new StatementActHelper(service);
    }

    /**
     * Processes a customer.
     *
     * @param customer the customer to process
     * @throws OpenVPMSException for any error
     */
    public void process(Party customer) {
        StatementEvent event;
        if (!statementRules.hasStatement(customer, statementDate)) {
            Contact contact = getContact(customer);
            Iterable<Act> acts = actHelper.getActsWithAccountFees(
                    customer, statementDate);
            if (TypeHelper.isA(contact, "contact.email")) {
                event = new StatementEvent(EMAIL, customer, contact,
                                           statementDate, acts);
            } else {
                event = new StatementEvent(PRINT, customer, contact,
                                           statementDate, acts);
            }
        } else {
            // customer already has had a statement generated for the statement
            // period.
            event = new StatementEvent(SKIP, customer, statementDate);
        }
        notifyListeners(event.getAction(), event);
    }

    /**
     * Returns a contact for the specified customer.
     *
     * @param customer the customer
     * @return the contact for <tt>customer</tt>
     * @throws StatementProcessorException if there is no contact for the
     *                                     customer
     * @throws ArchetypeServiceException   for any archetype service error
     */
    private Contact getContact(Party customer) {
        Contact result = getContact(customer, "contact.email");
        if (result == null) {
            result = getContact(customer, "contact.location");
        }
        if (result == null) {
            for (Contact contact : customer.getContacts()) {
                if (TypeHelper.isA(contact, "contact.location")) {
                    result = contact;
                }
            }
        }
        if (result == null) {
            throw new StatementProcessorException(NoContact, customer.getName(),
                                                  customer.getDescription());
        }
        return result;
    }

    /**
     * Returns the first contact for the customer with the specified archetype
     * short name and <em>BILLING</em> purpose.
     *
     * @param customer  the customer
     * @param shortName the congtact archetype short name
     * @return the matching contact or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    private Contact getContact(Party customer, String shortName) {
        Contact result = null;
        for (Contact contact : customer.getContacts()) {
            if (TypeHelper.isA(contact, shortName)) {
                IMObjectBean bean = new IMObjectBean(contact, service);
                List<Lookup> purposes
                        = bean.getValues("purposes", Lookup.class);
                for (Lookup purpose : purposes) {
                    if ("BILLING".equals(purpose.getCode())) {
                        result = contact;
                        break;
                    }
                }
            }
        }
        return result;
    }

}
