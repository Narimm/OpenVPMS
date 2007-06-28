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

import org.openvpms.archetype.component.processor.AbstractAsynchronousProcessor;
import org.openvpms.archetype.rules.finance.account.CustomerAccountRules;
import static org.openvpms.archetype.rules.finance.statement.StatementEvent.Action.EMAIL;
import static org.openvpms.archetype.rules.finance.statement.StatementEvent.Action.PRINT;
import static org.openvpms.archetype.rules.finance.statement.StatementProcessorException.ErrorCode.NoContact;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


/**
 * Default implementation of the {@link StatementProcessor} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DefaultStatementProcessor
        extends AbstractAsynchronousProcessor<StatementEvent.Action, Party,
        StatementEvent> implements StatementProcessor {


    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The customer account business rules.
     */
    private final CustomerAccountRules accountRules;

    /**
     * The customer statement business rules.
     */
    private final StatementRules statementRules;

    /**
     * The processing date.
     */
    private final Date date;

    /**
     * The current customer.
     */
    private Party current;


    /**
     * Creates a new <tt>DefaultStatementProcessor</tt>.
     */
    public DefaultStatementProcessor() {
        this(ArchetypeServiceHelper.getArchetypeService());
    }

    /**
     * Creates a new <tt>DefaultStatementProcessor</tt>.
     *
     * @param service the archetype service
     */
    public DefaultStatementProcessor(IArchetypeService service) {
        this.service = service;
        accountRules = new CustomerAccountRules(service);
        statementRules = new StatementRules(service);
        date = new Date();
    }

    /**
     * Starts processing a customer.
     * If the customer has an overdue amount that incurs an accounting fee,
     * an <em>act.customerAccountDebitAdjust</em> will be applied containing
     * the fee. Any registered listener will then be notified.
     * One completion of processing, the listeners should invoke
     * {@link #end(Party)}.
     *
     * @param customer the customer to process
     * @throws ArchetypeServiceException for any archetype service error
     */
    public void start(Party customer) {
        current = customer;
        BigDecimal fee = statementRules.getAccountFee(customer, date);
        if (fee.compareTo(BigDecimal.ZERO) != 0) {
            statementRules.applyAccountingFee(customer, fee);
        }
        Contact contact = getContact(customer);
        StatementEvent event;
        if (TypeHelper.isA(contact, "contact.email")) {
            event = new StatementEvent(this, EMAIL, customer, contact, date);
        } else {
            event = new StatementEvent(this, PRINT, customer, contact, date);
        }
        notifyListeners(event.getAction(), event);
    }

    /**
     * Finish processing a customer.
     * The first invocation for a customer will generate account period end
     * acts, i.e an <em>act.customerAccountClosingBalance</em>
     * and <em>act.customerAccountOpeningBalance</em>.
     *
     * @param customer the customer
     * @throws ArchetypeServiceException for any archetype service error
     */
    public void end(Party customer) {
        if (current != null && current.equals(customer)) {
            current = null;
            accountRules.createPeriodEnd(customer);
        }
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
