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

import org.apache.commons.lang.time.DateUtils;
import org.openvpms.archetype.component.processor.AbstractProcessor;
import static org.openvpms.archetype.rules.finance.statement.StatementProcessorException.ErrorCode.InvalidStatementDate;
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
import org.openvpms.component.system.common.query.ArchetypeQueryException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


/**
 * Processor for customer statements.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
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
     * Determines if statements that have been printed should be procesed.
     */
    private boolean reprint;


    /**
     * Creates a new <tt>StatementProcessor</tt>.
     *
     * @param statementDate the statement date. Must be a date prior to today.
     * @throws StatementProcessorException if the statement date is invalid
     */
    public StatementProcessor(Date statementDate) {
        this(statementDate, ArchetypeServiceHelper.getArchetypeService());
    }

    /**
     * Creates a new <tt>StatementProcessor</tt>.
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
        rules = new StatementRules(service);
        actHelper = new StatementActHelper(service);
        this.statementDate = actHelper.getStatementTimestamp(statementDate);
    }

    /**
     * Determines if statements that have been printed should be reprinted.
     * A statement is printed if the printed flag of its
     * <em>act.customerAccountOpeningBalance</em> is <tt>true</tt>.
     * Defaults to <tt>false</tt>.
     *
     * @param reprint if <tt>true</tt>, process statements that have been
     *                printed.
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
        Date open = actHelper.getOpeningBalanceTimestamp(
                customer, statementDate);
        StatementActHelper.ActState closeState
                = actHelper.getClosingBalanceState(customer, statementDate, 
                                                   open);
        Date close = null;
        boolean printed = false;
        if (closeState != null) {
            close = closeState.getStartTime();
            printed = closeState.isPrinted();

            // only include those acts up to the statement date to support
            // back-dated statements
            if (getDate(close).compareTo(getDate(statementDate)) > 0) {
                close = statementDate;
            }
        }
        if (!printed || reprint) {
            Iterable<Act> acts;
            if (close == null) {
                acts = getPreviewActs(customer, open);
            } else {
                acts = actHelper.getPostedActs(customer, open, close, false);
            }
            List<Contact> contacts = getContacts(customer);
            Date date = (close == null) ? statementDate : close;
            Statement statement = new Statement(customer, contacts, date,
                                                open, close, acts, printed);
            notifyListeners(statement);
        }
    }

    /**
     * Returns all POSTED statement acts and COMPLETED charge acts for a
     * customer from the opening balance timestamp to the end of the statement
     * date. <p/>
     * This adds (but does not save) an accounting fee act if an accounting fee
     * is required.
     *
     * @param customer                the customer
     * @param openingBalanceTimestamp the opening balance timestamp. May be
     *                                <tt>null</tt>
     * @return the statement acts
     * @throws ArchetypeServiceException for any archetype service error
     * @throws ArchetypeQueryException   for any archetype query error
     */
    private Iterable<Act> getPreviewActs(Party customer,
                                         Date openingBalanceTimestamp) {
        Iterable<Act> result = actHelper.getPostedAndCompletedActs(
                customer, statementDate, openingBalanceTimestamp);

        BigDecimal fee = rules.getAccountFee(customer, statementDate);
        if (fee.compareTo(BigDecimal.ZERO) != 0) {
            Act feeAct = rules.createAccountingFeeAdjustment(
                    customer, fee, statementDate);
            List<Act> toAdd = new ArrayList<Act>();
            toAdd.add(feeAct);
            result = new IterableChain<Act>(result, toAdd);
        }
        return result;
    }

    /**
     * Returns the preferred statement contacts for the customer.
     *
     * @param customer the customer
     * @return the preferred contacts for <tt>customer</tt>, or an empty list
     *         if the customer has no contacts
     * @throws ArchetypeServiceException for any archetype service error
     */
    private List<Contact> getContacts(Party customer) {
        List<Contact> result = new ArrayList<Contact>();
        addBillingContacts(result, customer, "contact.email");
        addBillingContacts(result, customer, "contact.location");
        if (result.isEmpty()) {
            for (Contact contact : customer.getContacts()) {
                if (TypeHelper.isA(contact, "contact.location")) {
                    result.add(contact);
                }
            }
        }
        return result;
    }

    /**
     * Adds contacts with the specified archetype short name and
     * <em>BILLING</em> purpose.
     *
     * @param customer  the customer
     * @param shortName the contact archetype short name
     * @throws ArchetypeServiceException for any archetype service error
     */
    private void addBillingContacts(List<Contact> list, Party customer,
                                    String shortName) {
        for (Contact contact : customer.getContacts()) {
            if (TypeHelper.isA(contact, shortName)) {
                IMObjectBean bean = new IMObjectBean(contact, service);
                List<Lookup> purposes
                        = bean.getValues("purposes", Lookup.class);
                for (Lookup purpose : purposes) {
                    if ("BILLING".equals(purpose.getCode())) {
                        list.add(contact);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Returns the day/month/year part of a date-time.
     *
     * @param datetime the date/time
     * @return the day/month/year part of the date
     */
    private Date getDate(Date datetime) {
        return DateUtils.truncate(datetime, Calendar.DAY_OF_MONTH);
    }


}
