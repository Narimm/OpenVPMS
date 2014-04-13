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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.party;

import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.finance.account.CustomerAccountQueryFactory;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.NodeSelectConstraint;
import org.openvpms.component.system.common.query.ObjectRefNodeConstraint;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.ObjectSetQueryIterator;
import org.openvpms.component.system.common.query.RelationalOp;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.CLOSING_BALANCE;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.OPENING_BALANCE;


/**
 * Merges two <em>party.customerperson</em> instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class CustomerMerger extends PartyMerger {

    /**
     * Customer rules.
     */
    private final CustomerRules rules;


    /**
     * Creates a new <tt>CustomerMerger</tt>.
     */
    public CustomerMerger() {
        this(ArchetypeServiceHelper.getArchetypeService());
    }

    /**
     * Creates a new <tt>CustomerMerger</tt>.
     *
     * @param service the archetype service
     */
    public CustomerMerger(IArchetypeService service) {
        super("party.customerperson", service);
        rules = new CustomerRules(service);
    }

    /**
     * Moves act participations from one party to another.
     * Prior to the move, this implementation removes all
     * <em>act.customerAccountOpeningBalance</em>
     * and <em>act.customerAccountClosingBalance</em> in the from customer,
     * and any <em>act.customerAccountOpeningBalance</em>
     * and <em>act.customerAccountClosingBalance</em> from the to customer
     * that have a date on or after the first transaction date in the from
     * customer.
     *
     * @param from the party to move from
     * @param to   the party to move to
     * @return the moved participations
     */
    @Override
    protected List<IMObject> moveParticipations(Party from, Party to) {
        List<IMObject> result = new ArrayList<IMObject>();

        // remove the opening and closing balance acts from the from customer
        ArchetypeQuery fromQuery = createOpeningClosingBalanceQuery(from);
        if (remove(fromQuery)) {
            Date startTime = getFirstTransactionStartTime(from);
            if (startTime != null) {
                // remove opening and closing balance acts from the to customer
                // that are timestamped on or after the first transaction date
                ArchetypeQuery toQuery = createOpeningClosingBalanceQuery(to);
                toQuery.add(new NodeConstraint("startTime", RelationalOp.GTE,
                                               startTime));
                toQuery.setMaxResults(ArchetypeQuery.ALL_RESULTS);
                remove(toQuery);
            }
        }
        IMObjectReference fromRef = from.getObjectReference();
        IMObjectReference toRef = to.getObjectReference();

        // assign any participations over to the to customer,
        // excluding any linked to opening and closing balance acts that
        // have been deleted (but due to transaction, are still returned)
        ArchetypeQuery query
                = new ArchetypeQuery("participation.*", true, false);
        query.add(new ObjectRefNodeConstraint("entity", fromRef));
        query.setMaxResults(ArchetypeQuery.ALL_RESULTS);
        List<IMObject> participations = getArchetypeService().get(
                query).getResults();
        for (IMObject object : participations) {
            Participation participation = (Participation) object;
            if (!TypeHelper.isA(participation.getAct(),
                                OPENING_BALANCE, CLOSING_BALANCE)) {
                participation.setEntity(toRef);
                result.add(participation);
            }
        }
        return result;
    }

    /**
     * Copies classifications from one party to another.
     * This ensures that only one <em>lookup.customerAccountType</em> appears in
     * the 'to' party, to avoid cardinality violations. If both parties have a
     * <em>lookup.customerAccountType</em>, the 'to' party's type takes
     * precedence.
     *
     * @param from the party to copy from
     * @param to   the party to copy to
     */
    @Override
    protected void copyClassifications(Party from, Party to) {
        for (Lookup lookup : from.getClassifications()) {
            if (!TypeHelper.isA(lookup, "lookup.customerAccountType")) {
                to.addClassification(lookup);
            }
        }
        Lookup accountType = rules.getAccountType(to);
        if (accountType == null) {
            accountType = rules.getAccountType(from);
            if (accountType != null) {
                to.addClassification(accountType);
            }
        }
    }

    /**
     * Removes all objects matching a query.
     *
     * @param query the query
     * @return <tt>true</tt> if any objects were removed
     * @throws ArchetypeServiceException for any archetype service error
     */
    private boolean remove(ArchetypeQuery query) {
        boolean removed = false;
        IArchetypeService service = getArchetypeService();
        Iterator<IMObject> iter
                = new IMObjectQueryIterator<IMObject>(service, query);
        while (iter.hasNext()) {
            service.remove(iter.next());
            removed = true;
        }
        return removed;
    }

    /**
     * Returns the start time of the first account transaction for a customer.
     *
     * @param party the customer
     * @return the first start time, or <tt>null</tt> if the customer has no
     *         transactions
     * @throws ArchetypeServiceException for any archetype service error
     */
    private Date getFirstTransactionStartTime(Party party) {
        ArchetypeQuery query = CustomerAccountQueryFactory.createObjectSetQuery(
                party, CustomerAccountArchetypes.DEBITS_CREDITS, true);
        query.add(new NodeSelectConstraint("act.startTime"));
        query.setMaxResults(1);
        Date startTime = null;
        ObjectSetQueryIterator iter = new ObjectSetQueryIterator(
                getArchetypeService(), query);
        if (iter.hasNext()) {
            ObjectSet set = iter.next();
            startTime = (Date) set.get("act.startTime");
        }
        return startTime;
    }

    /**
     * Creates a query for opening and closing balances, for a customer.
     *
     * @param party the customer
     * @return a new query
     */
    private ArchetypeQuery createOpeningClosingBalanceQuery(Party party) {
        String[] shortNames = {OPENING_BALANCE,
                               CLOSING_BALANCE};
        ArchetypeQuery query = CustomerAccountQueryFactory.createQuery(
                party, shortNames);
        query.setMaxResults(ArchetypeQuery.ALL_RESULTS);
        return query;
    }

}
