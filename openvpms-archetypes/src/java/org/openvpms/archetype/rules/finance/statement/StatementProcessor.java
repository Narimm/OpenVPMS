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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.finance.statement;

import org.openvpms.archetype.component.processor.AbstractProcessor;
import org.openvpms.archetype.rules.finance.account.CustomerAccountRules;
import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.exception.OpenVPMSException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.openvpms.archetype.rules.finance.statement.StatementProcessorException.ErrorCode.InvalidStatementDate;


/**
 * Processor for customer statements.
 *
 * @author Tim Anderson
 */
public class StatementProcessor extends AbstractProcessor<Party, Statement> {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The statement date.
     */
    private final Date statementDate;

    /**
     * Statement rules.
     */
    private final StatementRules rules;

    /**
     * Statement act helper.
     */
    private final StatementActHelper actHelper;

    /**
     * Determines if statements that have been printed should be processed.
     */
    private boolean reprint;


    /**
     * Creates a {@link StatementProcessor}.
     *
     * @param statementDate the statement date. Must be a date prior to today.
     * @param practice      the practice
     * @param service       the archetype service
     * @param accountRules  the customer account rules
     * @throws StatementProcessorException if the statement date is invalid
     */
    public StatementProcessor(Date statementDate, Party practice, IArchetypeService service,
                              CustomerAccountRules accountRules) {
        this.service = service;
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        if (calendar.getTime().compareTo(statementDate) < 0) {
            throw new StatementProcessorException(InvalidStatementDate, statementDate);
        }
        rules = new StatementRules(practice, service, accountRules);
        actHelper = new StatementActHelper(service);
        this.statementDate = actHelper.getStatementTimestamp(statementDate);
    }

    /**
     * Determines if statements that have been printed should be reprinted.
     * A statement is printed if the printed flag of its
     * <em>act.customerClosingOpeningBalance</em> is {@code true}.
     * Defaults to {@code false}.
     *
     * @param reprint if {@code true}, process statements that have been printed.
     */
    public void setReprint(boolean reprint) {
        this.reprint = reprint;
    }

    /**
     * Processes a customer.
     *
     * @param customer the customer to process
     * @throws OpenVPMSException for any error
     */
    public void process(Party customer) {
        StatementPeriod period = new StatementPeriod(customer, statementDate, actHelper);
        if (!period.isPrinted() || reprint) {
            Iterable<FinancialAct> acts;
            Date open = period.getOpeningBalanceTimestamp();
            Date close = period.getClosingBalanceTimestamp();
            if (!period.hasStatement()) {
                acts = rules.getStatementPreview(customer, open, statementDate, true, true);
            } else {
                acts = rules.getStatement(customer, open, close);
            }
            List<Contact> contacts = getContacts(customer);
            Statement statement = new Statement(customer, contacts, statementDate, open, close, acts,
                                                period.isPrinted());
            notifyListeners(statement);
        }
    }

    /**
     * Returns the preferred statement contacts for the customer.
     *
     * @param customer the customer
     * @return the preferred contacts for {@code customer}, or an empty list if the customer has no contacts
     * @throws ArchetypeServiceException for any archetype service error
     */
    private List<Contact> getContacts(Party customer) {
        List<Contact> result = new ArrayList<>();
        addBillingContacts(result, customer, ContactArchetypes.EMAIL);
        addBillingContacts(result, customer, ContactArchetypes.LOCATION);
        if (result.isEmpty()) {
            for (org.openvpms.component.model.party.Contact contact : customer.getContacts()) {
                if (contact.isA(ContactArchetypes.LOCATION)) {
                    result.add((Contact) contact);
                }
            }
        }
        return result;
    }

    /**
     * Adds contacts with the specified archetype short name and
     * <em>BILLING</em> purpose.
     *
     * @param list      the contact list to add to
     * @param customer  the customer
     * @param shortName the contact archetype short name
     * @throws ArchetypeServiceException for any archetype service error
     */
    private void addBillingContacts(List<Contact> list, Party customer,
                                    String shortName) {
        for (org.openvpms.component.model.party.Contact contact : customer.getContacts()) {
            if (contact.isA(shortName)) {
                IMObjectBean bean = new IMObjectBean(contact, service);
                List<Lookup> purposes = bean.getValues("purposes", Lookup.class);
                for (Lookup purpose : purposes) {
                    if ("BILLING".equals(purpose.getCode())) {
                        list.add((Contact) contact);
                        break;
                    }
                }
            }
        }
    }

}
