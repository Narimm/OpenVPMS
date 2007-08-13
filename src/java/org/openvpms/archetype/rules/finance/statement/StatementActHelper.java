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

import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.CustomerAccountActTypes;
import org.openvpms.archetype.rules.finance.account.CustomerAccountRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.component.system.common.query.CollectionNodeConstraint;
import org.openvpms.component.system.common.query.IterableIMObjectQuery;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.ObjectRefNodeConstraint;
import org.openvpms.component.system.common.query.OrConstraint;
import org.openvpms.component.system.common.query.RelationalOp;
import org.openvpms.component.system.common.query.ShortNameConstraint;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


/**
 * Helper for performing statement act queries.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class StatementActHelper {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Customer account rules.
     */
    private final CustomerAccountRules account;

    /**
     * Statement rules
     */
    private final StatementRules statement;

    /**
     * The short names to query. This contains all debit/credit parent acts,
     * and opening and closing balances.
     */
    private static final String[] SHORT_NAMES;

    /**
     * Charge short names.
     */
    private static final String[] CHARGE_SHORT_NAMES = {
            CustomerAccountActTypes.CHARGES_INVOICE,
            CustomerAccountActTypes.CHARGES_COUNTER,
            CustomerAccountActTypes.CHARGES_CREDIT
    };

    static {
        List<String> shortNames = new ArrayList<String>();
        shortNames.addAll(Arrays.asList(
                CustomerAccountActTypes.DEBIT_CREDIT_SHORT_NAMES));
        shortNames.add(CustomerAccountActTypes.OPENING_BALANCE);
        shortNames.add(CustomerAccountActTypes.CLOSING_BALANCE);
        SHORT_NAMES = shortNames.toArray(new String[0]);
    }


    /**
     * Creates a new <tt>StatementActHelper</tt>.
     *
     * @param service the archetype service
     */
    public StatementActHelper(IArchetypeService service) {
        this.service = service;
        account = new CustomerAccountRules(service);
        statement = new StatementRules(service);
    }

    /**
     * Returns all statement acts for a customer between the opening balance
     * prior to the specified date, and the corresponding closing balance.
     * Acts may have any status.
     *
     * @param customer      the customer
     * @param statementDate the statement date
     * @return the statement acts
     * @throws ArchetypeServiceException for any archetype service error
     * @throws ArchetypeQueryException   for any archetype query error
     */
    public Iterable<Act> getActs(Party customer, Date statementDate) {
        Date open = getOpeningBalanceTimestamp(customer, statementDate);
        Date close = getClosingBalanceTimestamp(customer, statementDate, open);
        ArchetypeQuery query = createQuery(customer, open, close, null);
        return new IterableIMObjectQuery<Act>(service, query);
    }

    /**
     * Returns all POSTED statement acts between the opening and closing balance
     * timestamps.
     *
     * @param customer                the customer
     * @param openingBalanceTimestamp the opening balance timestamp. May be
     *                                <tt>null</tt>
     * @param closingBalanceTimestamp the closing balance timestamp. May be
     *                                <tt>null</tt>
     * @return the posted acts
     * @throws ArchetypeServiceException for any archetype service error
     * @throws ArchetypeQueryException   for any archetype query error
     */
    public Iterable<Act> getPostedActs(Party customer,
                                       Date openingBalanceTimestamp,
                                       Date closingBalanceTimestamp) {
        ArchetypeQuery query = createQuery(customer, openingBalanceTimestamp,
                                           closingBalanceTimestamp,
                                           ActStatus.POSTED);
        return new IterableIMObjectQuery<Act>(service, query);
    }

    /**
     * Returns all COMPLETED charge act for a customer, between the
     * opening balance prior to the specified date, and the corresponding
     * closing balance. If there is no closing balance, returns up to the
     * statement date timestamp.
     *
     * @param customer      the customer
     * @param statementDate the statement date
     * @return any charge acts with COMPLETED status
     * @throws ArchetypeServiceException for any archetype service error
     * @throws ArchetypeQueryException   for any archetype query error
     */
    public Iterable<Act> getCompletedCharges(Party customer,
                                             Date statementDate) {
        Date openTimestamp = getOpeningBalanceTimestamp(customer,
                                                        statementDate);
        Date closeTimestamp = getClosingBalanceTimestamp(customer,
                                                         openTimestamp,
                                                         statementDate);
        if (closeTimestamp == null) {
            closeTimestamp = statement.getStatementTimestamp(statementDate);
        }
        ArchetypeQuery query = createQuery(customer, CHARGE_SHORT_NAMES,
                                           openTimestamp, closeTimestamp,
                                           ActStatus.COMPLETED);
        return new IterableIMObjectQuery<Act>(service, query);
    }

    /**
     * Returns all POSTED statement acts and COMPLETED charge acts for a
     * customer from the opening balance timestamp to the end of the statement
     * date. <p/>
     * This adds (but does not save) an accounting fee act if an accounting fee
     * is required.
     * This is intended to be used to preview acts prior to end-of-period.
     *
     * @param customer                the customer
     * @param statementDate           the date
     * @param openingBalanceTimestamp the opening balance timestamp. May be
     *                                <tt>null</tt>
     * @return the statement acts
     * @throws ArchetypeServiceException for any archetype service error
     * @throws ArchetypeQueryException   for any archetype query error
     */
    public Iterable<Act> getPreviewActs(Party customer,
                                        Date statementDate,
                                        Date openingBalanceTimestamp) {
        Date close = statement.getStatementTimestamp(statementDate);
        ArchetypeQuery query = createQuery(customer, openingBalanceTimestamp,
                                           close, null);
        query.add(new OrConstraint()
                .add(new NodeConstraint("status", ActStatus.POSTED))
                .add(new NodeConstraint("status", ActStatus.COMPLETED)));
        Iterable<Act> result = new IterableIMObjectQuery<Act>(service, query);

        // no closing balance, so calculate any account fees
        BigDecimal fee = statement.getAccountFee(customer, statementDate);
        if (fee.compareTo(BigDecimal.ZERO) != 0) {
            Act feeAct = statement.createAccountingFeeAdjustment(
                    customer, fee, statementDate);
            List<Act> toAdd = new ArrayList<Act>();
            toAdd.add(feeAct);
            result = new IterableChain<Act>(result, toAdd);
        }
        return result;
    }

    /**
     * Returns the opening balance timestamp for a customer and statement date.
     *
     * @param customer      the customer
     * @param statementDate the statement date
     * @return the opening balance, or <tt>null</tt> if none is found
     */
    public Date getOpeningBalanceTimestamp(Party customer, Date statementDate) {
        return account.getOpeningBalanceTimestampBefore(customer,
                                                        statementDate);
    }

    /**
     * Returns the closing balance timestamp for a customer relative to a
     * statement date and opening balance timestamp.
     *
     * @param customer                the customer
     * @param statementDate           the statement date
     * @param openingBalanceTimestamp the opening balance timestamp. May be
     *                                <tt>null</tt>
     * @return the closing balance timestamp, or <tt>null</tt> if none is found
     */
    public Date getClosingBalanceTimestamp(Party customer, Date statementDate,
                                           Date openingBalanceTimestamp) {
        Date result;
        if (openingBalanceTimestamp == null) {
            result = account.getClosingBalanceTimestampBefore(customer,
                                                              statementDate);
            if (result == null) {
                result = account.getClosingBalanceDateAfter(customer,
                                                            statementDate);
            }
        } else {
            result = account.getClosingBalanceDateAfter(
                    customer, openingBalanceTimestamp);
        }
        return result;
    }

    /**
     * Helper to creates a query for all account act types between
     * the opening and closing balance timetamps, inclusive.
     *
     * @param customer                the customer
     * @param openingBalanceTimestamp the opening balance timestamp.
     *                                May be <tt>null</tt>
     * @param closingBalanceTimestamp the closing balance timestamp.
     *                                May be <tt>null</tt>
     * @param status                  the status. May be <tt>null</tt>
     * @return a new query
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected ArchetypeQuery createQuery(Party customer,
                                         Date openingBalanceTimestamp,
                                         Date closingBalanceTimestamp,
                                         String status) {
        return createQuery(customer, SHORT_NAMES, openingBalanceTimestamp,
                           closingBalanceTimestamp, status);
    }

    /**
     * Helper to creates a query for all account act types between
     * the opening and closing balance timetamps, inclusive.
     *
     * @param customer                the customer
     * @param openingBalanceTimestamp the opening balance timestamp.
     *                                May be <tt>null</tt>
     * @param closingBalanceTimestamp the closing balance timestamp.
     *                                May be <tt>null</tt>
     * @param status                  the status. May be <tt>null</tt>
     * @return a new query
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected ArchetypeQuery createQuery(Party customer,
                                         String[] shortNames,
                                         Date openingBalanceTimestamp,
                                         Date closingBalanceTimestamp,
                                         String status) {
        ShortNameConstraint archetypes = new ShortNameConstraint(
                "a", SHORT_NAMES, false, false);
        ArchetypeQuery query = new ArchetypeQuery(archetypes);
        CollectionNodeConstraint constraint = new CollectionNodeConstraint(
                "customer", "participation.customer", false, false);
        constraint.add(new ObjectRefNodeConstraint(
                "entity", customer.getObjectReference()));
        if (openingBalanceTimestamp != null) {
            query.add(new NodeConstraint("startTime", RelationalOp.GTE,
                                         openingBalanceTimestamp));
        }
        if (closingBalanceTimestamp != null) {
            query.add(new NodeConstraint("startTime", RelationalOp.LTE,
                                         closingBalanceTimestamp));
        }
        if (status != null) {
            query.add(new NodeConstraint("status", status));
        }
        query.add(constraint);
        query.add(new NodeSortConstraint("startTime"));
        return query;
    }

}
