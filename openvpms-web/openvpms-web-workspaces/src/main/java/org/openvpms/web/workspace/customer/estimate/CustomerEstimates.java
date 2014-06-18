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
package org.openvpms.web.workspace.customer.estimate;

import org.openvpms.archetype.rules.act.EstimateActStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.web.system.ServiceHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static org.openvpms.archetype.rules.finance.estimate.EstimateArchetypes.ESTIMATE;
import static org.openvpms.component.system.common.query.Constraints.eq;
import static org.openvpms.component.system.common.query.Constraints.gt;
import static org.openvpms.component.system.common.query.Constraints.isNull;
import static org.openvpms.component.system.common.query.Constraints.join;
import static org.openvpms.component.system.common.query.Constraints.ne;
import static org.openvpms.component.system.common.query.Constraints.or;

/**
 * Queries a customer's estimates.
 *
 * @author benjamincharlton
 */
public class CustomerEstimates {

    /**
     * Returns active estimates for a customer and optionally a patient.
     * <p/>
     * If a patient is specified, returned estimates may also have items for other patients.
     *
     * @param customer the customer
     * @param patient  the patient. If {@code null}, returns all estimates for the customer
     * @return the estimates
     */
    public List<Act> getEstimates(Party customer, Party patient) {
        ArchetypeQuery query = createQuery(customer, patient);
        Iterator<Act> iterator = new IMObjectQueryIterator<Act>(query);
        List<Act> list = new ArrayList<Act>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        return list;
    }

    /**
     * Determines if a customer and optionally a patient, have estimates.
     * <p/>
     * Note that the estimates may not be for a single patient.
     *
     * @param customer the customer
     * @param patient  the patient. If {@code null}, returns if there are any estimates for the customer
     * @return {@code true} if estimates exist
     */
    public boolean hasEstimates(Party customer, Party patient) {
        ArchetypeQuery query = createQuery(customer, patient);
        query.setCountResults(true);
        return ServiceHelper.getArchetypeService().get(query).getTotalResults() != 0;
    }

    /**
     * Creates a new query.
     *
     * @param customer the customer
     * @param patient  the patient. May be {@code null}
     * @return a new query
     */
    private ArchetypeQuery createQuery(Party customer, Party patient) {
        ArchetypeQuery query = new ArchetypeQuery(ESTIMATE);
        query.add(join("customer").add(eq("entity", customer.getObjectReference())));
        query.add(ne("status", EstimateActStatus.CANCELLED));
        query.add(ne("status", EstimateActStatus.INVOICED));
        query.add(or(isNull("endTime"), gt("endTime", new Date())));
        if (patient != null) {
            query.add(join("items").add(join("target").add(
                    join("patient").add(eq("entity", patient.getObjectReference())))));
        }
        return query;
    }

}
