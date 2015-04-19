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

package org.openvpms.web.workspace.customer.order;

import org.openvpms.archetype.rules.finance.order.OrderArchetypes;
import org.openvpms.archetype.rules.finance.order.OrderRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.system.common.query.BaseArchetypeConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.query.ListResultSet;
import org.openvpms.web.component.im.query.NonRenderingQuery;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.system.ServiceHelper;

import java.util.List;

/**
 * Pending order query.
 *
 * @author Tim Anderson
 */
public class PendingOrderQuery extends NonRenderingQuery<Act> {

    /**
     * The orders and returns to query.
     */
    public static final String[] SHORT_NAMES = {OrderArchetypes.ORDERS, OrderArchetypes.RETURNS};

    /**
     * The customer.
     */
    private final Party customer;

    /**
     * The patient. May be {@code null}
     */
    private final Party patient;

    /**
     * Orders to exclude from the results.
     */
    private List<Act> exclude;

    /**
     * The order rules.
     */
    private final OrderRules rules;

    /**
     * Constructs an {@link PendingOrderQuery}.
     *
     * @param customer the customer
     * @param patient  the patient. May be {@code null}
     * @param exclude  orders to exclude from the results
     */
    public PendingOrderQuery(Party customer, Party patient, List<Act> exclude) {
        super(SHORT_NAMES, Act.class);
        this.customer = customer;
        this.patient = patient;
        this.exclude = exclude;
        rules = ServiceHelper.getBean(OrderRules.class);
    }

    /**
     * Performs the query.
     *
     * @param sort the sort constraint. May be {@code null}
     * @return the query result set. May be {@code null}
     * @throws ArchetypeServiceException if the query fails
     */
    @Override
    public ResultSet<Act> query(SortConstraint[] sort) {
        List<Act> acts = getOrders();
        return new ListResultSet<Act>(acts, getMaxResults());
    }

    /**
     * Determines if the query selects a particular object reference.
     *
     * @param reference the object reference to check
     * @return {@code true} if the object reference is selected by the query
     */
    @Override
    public boolean selects(IMObjectReference reference) {
        for (Act act : getOrders()) {
            if (act.getObjectReference().equals(reference)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if active and/or inactive instances should be returned.
     *
     * @return the active state
     */
    @Override
    public BaseArchetypeConstraint.State getActive() {
        return BaseArchetypeConstraint.State.ACTIVE;
    }

    /**
     * Returns the orders.
     *
     * @return the orders
     */
    private List<Act> getOrders() {
        List<Act> acts = rules.getOrders(customer, patient);
        acts.removeAll(exclude);
        return acts;
    }

}
