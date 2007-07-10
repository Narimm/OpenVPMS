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

import org.openvpms.archetype.component.processor.ProcessorListener;
import org.openvpms.archetype.rules.finance.account.CustomerAccountActTypes;
import org.openvpms.archetype.rules.finance.account.CustomerAccountRules;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.CollectionNodeConstraint;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.ObjectRefNodeConstraint;
import org.openvpms.component.system.common.query.RelationalOp;
import org.openvpms.component.system.common.query.ShortNameConstraint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


/**
 * Abstract implementation of the {@link ProcessorListener} interface for
 * statement processing.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractStatementProcessorListener
        implements ProcessorListener<StatementEvent> {

    /**
     * The customer account rules.
     */
    private final CustomerAccountRules rules;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

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
     * Creates a new <tt>AbstractStatementProcessorLister</tt>.
     *
     * @param service the archetype service
     */
    public AbstractStatementProcessorListener(IArchetypeService service) {
        this.service = service;
        rules = new CustomerAccountRules(service);
    }

    /**
     * Returns the archetype service.
     *
     * @return the archetype service
     */
    protected IArchetypeService getArchetypeService() {
        return service;
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
        Date open = rules.getOpeningBalanceDateBefore(customer, date);
        Date close;
        if (open == null) {
            close = rules.getClosingBalanceDateBefore(customer, date);
        } else {
            close = rules.getClosingBalanceDateAfter(customer, date);
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
        return query;
    }
}
