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
     *
     * @param customer the customer
     * @param date     the date
     * @return the statement acts
     * @throws ArchetypeServiceException for any archetype service error
     * @throws ArchetypeQueryException   for any archetype query error
     */
    public Iterable<Act> getActs(Party customer, Date date) {
        return new IterableIMObjectQuery<Act>(service,
                                              createQuery(customer, date));
    }

    /**
     * Returns all COMPLETED statements act for a customer, between the
     * opening balance prior to the specified date, and the corresponding
     * closing balance.
     *
     * @param customer the customer
     * @param date     the date
     * @return any acts with COMPLETED statu
     * @throws ArchetypeServiceException for any archetype service error
     * @throws ArchetypeQueryException   for any archetype query error
     */
    public Iterable<Act> getCompletedActs(Party customer, Date date) {
        ArchetypeQuery query = createQuery(customer, date);
        query.add(new NodeConstraint("status", ActStatus.COMPLETED));
        return new IterableIMObjectQuery<Act>(service, query);
    }

    /**
     * Returns all statement acts for a customer between the opening balance
     * prior to the specified date, and the corresponding closing balance.
     * This adds (but does not save) an accounting fee act if required.
     *
     * @param customer the customer
     * @param date     the date
     * @return the statement acts
     * @throws ArchetypeServiceException for any archetype service error
     * @throws ArchetypeQueryException   for any archetype query error
     */
    public Iterable<Act> getActsWithAccountFees(Party customer, Date date) {
        BigDecimal fee = statement.getAccountFee(customer, date);
        if (fee.compareTo(BigDecimal.ZERO) != 0) {
            Act act = statement.createAccountingFeeAdjustment(customer,
                                                              fee, date);
            List<Act> acts = Arrays.asList(act);
            return new IterableChain<Act>(getActs(customer, date), acts);
        }
        return getActs(customer, date);
    }

    /**
     * Helper to creates a query for all account act types between
     * the opening balance prior to the specified date, and the corresponding
     * closing balance.
     *
     * @param customer the customer
     * @param date     the date
     * @return a new query
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected ArchetypeQuery createQuery(Party customer, Date date) {
        Date open = account.getOpeningBalanceDateBefore(customer, date);
        Date close;
        if (open == null) {
            close = account.getClosingBalanceDateBefore(customer, date);
            if (close == null) {
                close = account.getClosingBalanceDateAfter(customer, date);
            }
        } else {
            close = account.getClosingBalanceDateAfter(customer, date);
        }
        ShortNameConstraint archetypes = new ShortNameConstraint(
                "a", SHORT_NAMES, false, false);
        ArchetypeQuery query = new ArchetypeQuery(archetypes);
        CollectionNodeConstraint constraint = new CollectionNodeConstraint(
                "customer", "participation.customer", false, false);
        constraint.add(new ObjectRefNodeConstraint(
                "entity", customer.getObjectReference()));
        if (open != null) {
            query.add(new NodeConstraint("startTime", RelationalOp.GTE, open));
        }
        if (close != null) {
            query.add(new NodeConstraint("startTime", RelationalOp.LTE, close));
        }
        query.add(constraint);
        query.add(new NodeSortConstraint("startTime"));
        return query;
    }

}
