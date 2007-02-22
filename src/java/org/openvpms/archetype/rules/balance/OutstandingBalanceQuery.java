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

package org.openvpms.archetype.rules.balance;

import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.CollectionNodeConstraint;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.IdConstraint;
import org.openvpms.component.system.common.query.ObjectSelectConstraint;
import org.openvpms.component.system.common.query.ShortNameConstraint;

import java.util.Iterator;


/**
 * Query for customers with outstanding balances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class OutstandingBalanceQuery {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Creates a new <code>OutstandingBalanceQuery</code>.
     */
    public OutstandingBalanceQuery() {
        this(ArchetypeServiceHelper.getArchetypeService());
    }

    /**
     * Creates a new <code>OutstandingBalanceQuery</code>.
     *
     * @param service the archetype service
     */
    public OutstandingBalanceQuery(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Returns all customers that have outstanding balances.
     *
     * @return an iterator over the customers
     * @throws ArchetypeServiceException for any error
     */
    public Iterator<Party> query() {
        ShortNameConstraint acts = new ShortNameConstraint(
                "act", CustomerBalanceRules.DEBIT_SHORT_NAMES, true, true);
        ShortNameConstraint balance = new ShortNameConstraint(
                "balance", CustomerBalanceRules.ACCOUNT_BALANCE_SHORTNAME,
                true, true);
        ShortNameConstraint customer = new ShortNameConstraint(
                "customer", "party.customer*", true, true);
        ArchetypeQuery query = new ArchetypeQuery(acts);
        query.add(new ObjectSelectConstraint("customer"));
        query.add(new CollectionNodeConstraint("accountBalance", balance));
        query.add(customer);
        query.add(new IdConstraint("balance.entity", "customer"));
        query.add(new IdConstraint("balance.act", "act"));
        query.setDistinct(true);
        return new IMObjectQueryIterator<Party>(service, query);
    }

}
