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

package org.openvpms.archetype.rules.finance.account;

import org.openvpms.archetype.rules.act.ActCalculator;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountActTypes.*;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.ObjectSetQueryIterator;
import org.openvpms.component.system.common.query.RelationalOp;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;


/**
 * Customer balance calculator.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class BalanceCalculator {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;


    /**
     * Creates a new <tt>BalanceCalculator</tt>.
     *
     * @param service the archetype service
     */
    public BalanceCalculator(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Calculates the outstanding balance for a customer.
     *
     * @param customer the customer
     * @throws ArchetypeServiceException for any archetype service error
     */
    public BigDecimal getBalance(Party customer) {
        ArchetypeQuery query = QueryFactory.createUnallocatedObjectSetQuery(
                customer, DEBIT_CREDIT_SHORT_NAMES);
        Iterator<ObjectSet> iterator
                = new ObjectSetQueryIterator(service, query);
        return calculateBalance(iterator);
    }

    /**
     * Calculates the overdue balance for a customer.
     * This is the sum of unallocated amounts in associated debits that have a
     * date less than the specified overdue date.
     *
     * @param customer the customer
     * @param date     the overdue date
     * @return the overdue balance
     * @throws ArchetypeServiceException for any archetype service error
     */
    public BigDecimal getOverdueBalance(Party customer, Date date) {
        // query all overdue debit acts
        ArchetypeQuery query = QueryFactory.createUnallocatedObjectSetQuery(
                customer, DEBIT_SHORT_NAMES);
        query.add(new NodeConstraint("startTime", RelationalOp.LT, date));
        Iterator<ObjectSet> iterator
                = new ObjectSetQueryIterator(service, query);

        BigDecimal amount = calculateBalance(iterator);
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            amount = BigDecimal.ZERO;
        }
        return amount;
    }

    /**
     * Calculates the sum of all unallocated credits for a customer.
     *
     * @param customer the customer
     * @return the credit amount
     * @throws ArchetypeServiceException for any archetype service error
     */
    public BigDecimal getCreditBalance(Party customer) {
        ArchetypeQuery query = QueryFactory.createUnallocatedObjectSetQuery(
                customer, CREDIT_SHORT_NAMES);
        Iterator<ObjectSet> iterator = new ObjectSetQueryIterator(service,
                                                                  query);
        return calculateBalance(iterator);
    }

    /**
     * Returns the unbilled amount for a customer.
     *
     * @param customer the customer
     * @return the unbilled amount
     * @throws ArchetypeServiceException for any archetype service error
     */
    public BigDecimal getUnbilledAmount(Party customer) {
        String[] shortNames = {CHARGES_INVOICE, CHARGES_COUNTER,
                               CHARGES_CREDIT};
        ArchetypeQuery query = QueryFactory.createUnbilledObjectSetQuery(customer,
                                                                         shortNames);
        Iterator<ObjectSet> iterator = new ObjectSetQueryIterator(service,
                                                                  query);
        return calculateBalance(iterator);
    }

    /**
     * Helper to return the amount that may be allocated from a total.
     * If either value is <tt>null</tt> they are treated as being <tt>0.0</tt>.
     *
     * @param amount    the total amount. May be <tt>null</tt>
     * @param allocated the current amount allocated. May be <tt>null<tt>
     * @return <tt>amount - allocated</tt>
     */
    public BigDecimal getAllocatable(BigDecimal amount, BigDecimal allocated) {
        if (amount == null) {
            amount = BigDecimal.ZERO;
        }
        if (allocated == null) {
            allocated = BigDecimal.ZERO;
        }
        return amount.subtract(allocated);
    }

    /**
     * Calculates the oustanding balance.
     *
     * @param iterator an iterator over the collection
     * @return the outstanding balance
     */
    protected BigDecimal calculateBalance(Iterator<ObjectSet> iterator) {
        BigDecimal total = BigDecimal.ZERO;
        ActCalculator calculator = new ActCalculator(service);
        while (iterator.hasNext()) {
            ObjectSet set = iterator.next();
            BigDecimal amount = (BigDecimal) set.get("a.amount");
            BigDecimal allocated = (BigDecimal) set.get("a.allocatedAmount");
            boolean credit = (Boolean) set.get("a.credit");
            BigDecimal unallocated = getAllocatable(amount, allocated);
            total = calculator.addAmount(total, unallocated, credit);
        }
        return total;
    }

}
