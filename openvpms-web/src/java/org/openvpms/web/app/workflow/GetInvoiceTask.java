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

package org.openvpms.web.app.workflow;

import org.openvpms.archetype.rules.act.FinancialActStatus;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.CollectionNodeConstraint;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.ObjectRefNodeConstraint;
import org.openvpms.web.component.app.ContextException;
import org.openvpms.web.component.workflow.QueryIMObjectTask;
import org.openvpms.web.component.workflow.TaskContext;


/**
 * Task to query the most recent <em>act.customerAccountChargesInvoice</em>
 * with IN_PROGRESS or COMPLETED status for the context customer.
 * If one is present, adds it to the context.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class GetInvoiceTask extends QueryIMObjectTask {

    /**
     * The invoice short name.
     */
    public static final String INVOICE_SHORTNAME
            = "act.customerAccountChargesInvoice";


    /**
     * Returns the queries to execute.
     *
     * @param context the task context
     * @return the queries
     */
    protected ArchetypeQuery[] getQueries(TaskContext context) {
        Party customer = context.getCustomer();
        if (customer == null) {
            throw new ContextException(ContextException.ErrorCode.NoCustomer);
        }
        ArchetypeQuery inProgress = createQuery(customer,
                                                FinancialActStatus.IN_PROGRESS);
        ArchetypeQuery completed = createQuery(customer,
                                               FinancialActStatus.COMPLETED);
        return new ArchetypeQuery[]{inProgress, completed};
    }

    /**
     * Creates a query to return the latest invoice for a customer with the
     * given status.
     *
     * @param customer the customer
     * @param status   the act status
     * @return a new query
     */
    private ArchetypeQuery createQuery(Party customer, String status) {
        ArchetypeQuery query = new ArchetypeQuery(INVOICE_SHORTNAME, false,
                                                  true);
        query.setMaxResults(1);

        CollectionNodeConstraint participations
                = new CollectionNodeConstraint("customer",
                                               "participation.customer",
                                               false, true);
        participations.add(new ObjectRefNodeConstraint(
                "entity", customer.getObjectReference()));

        query.add(participations);
        query.add(new NodeConstraint("status", status));
        query.add(new NodeSortConstraint("startTime", false));
        return query;
    }
}
