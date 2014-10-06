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

package org.openvpms.archetype.rules.finance.order;

import org.apache.commons.collections4.CollectionUtils;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.NodeSelectConstraint;
import org.openvpms.component.system.common.query.ObjectSetQueryIterator;

import java.util.ArrayList;
import java.util.List;

import static org.openvpms.component.system.common.query.Constraints.join;

/**
 * Customer order rules.
 *
 * @author Tim Anderson
 */
public class OrderRules {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Constructs an {@link OrderRules}.
     *
     * @param service the archetype service
     */
    public OrderRules(IArchetypeService service) {
        this.service = service;
    }


    /**
     * Determines if a customer has orders or returns for a particular patient that have not been finalised.
     *
     * @param customer the customer
     * @param patient  the patient
     * @return {@code true} if there are orders for the customer and patient
     */
    public boolean hasOrders(Party customer, Party patient) {
        ArchetypeQuery query = createQuery(customer, patient);
        query.add(new NodeSelectConstraint("id"));
        query.setMaxResults(1);
        ObjectSetQueryIterator iterator = new ObjectSetQueryIterator(service, query);
        return iterator.hasNext();
    }

    /**
     * Returns all IN_PROGRESS orders or returns for a customer and patient.
     *
     * @param customer the customer
     * @param patient  the patient
     * @return the orders and returns
     */
    public List<Act> getOrders(Party customer, Party patient) {
        ArchetypeQuery query = createQuery(customer, patient);
        query.setDistinct(true);
        query.add(Constraints.sort("startTime"));
        query.add(Constraints.sort("id"));
        IMObjectQueryIterator<Act> iterator = new IMObjectQueryIterator<Act>(service, query);
        List<Act> result = new ArrayList<Act>();
        CollectionUtils.addAll(result, iterator);
        return result;
    }

    /**
     * Creates an order and returns query for a customer and patient.
     *
     * @param customer the customer
     * @param patient  the patient
     * @return the query
     */
    private ArchetypeQuery createQuery(Party customer, Party patient) {
        String[] shortNames = {OrderArchetypes.ORDERS, OrderArchetypes.RETURNS};
        ArchetypeQuery query = new ArchetypeQuery(shortNames, true, true);
        query.add(join("customer").add(Constraints.eq("entity", customer)));
        query.add(join("items").add(join("target").add(join("patient").add(Constraints.eq("entity", patient)))));
        query.add(Constraints.eq("status", ActStatus.IN_PROGRESS));
        return query;
    }

}
