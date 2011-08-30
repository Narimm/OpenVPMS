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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.workflow;

import org.openvpms.archetype.rules.act.FinancialActStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.CollectionNodeConstraint;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.ObjectRefNodeConstraint;
import org.openvpms.web.component.app.ContextException;
import org.openvpms.web.component.workflow.CreateIMObjectTask;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.component.workflow.TaskListener;

import java.util.List;


/**
 * Task to create an invoice for a customer, if one doesn't already exist.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class InvoiceTask extends CreateIMObjectTask {

    /**
     * The invoice short name.
     */
    public static final String INVOICE_SHORTNAME
            = "act.customerAccountChargesInvoice";


    /**
     * Constructs a new <code>InvoiceTask</code>
     */
    public InvoiceTask() {
        super(INVOICE_SHORTNAME);
    }

    /**
     * Starts the task.
     * <p/>
     * The registered {@link TaskListener} will be notified on completion or
     * failure.
     *
     * @param context the task context
     * @throws ArchetypeServiceException for any archetype service error
     * @throws ContextException          if the context doesn't contain a
     *                                   customer
     */
    @Override
    public void start(final TaskContext context) {
        Act invoice = getInvoice(context, FinancialActStatus.IN_PROGRESS);
        if (invoice == null) {
            invoice = getInvoice(context, FinancialActStatus.COMPLETED);
        }
        if (invoice == null) {
            super.start(context);
        } else {
            context.addObject(invoice);
            notifyCompleted();
        }
    }

    /**
     * Returns the  most recent invoice with the specified status.
     *
     * @param context the task context
     * @param status  the invoice status
     * @return the most recent invoice with the specified status or
     *         <code>null</code> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     * @throws ContextException          if the context doesn't contain a
     *                                   customer
     */
    private Act getInvoice(TaskContext context, String status) {
        ArchetypeQuery query = new ArchetypeQuery(getShortNames(), false,
                                                  true);
        query.setFirstResult(0);
        query.setMaxResults(1);

        Party customer = context.getCustomer();
        if (customer == null) {
            throw new ContextException(ContextException.ErrorCode.NoCustomer);
        }
        CollectionNodeConstraint participations
                = new CollectionNodeConstraint("customer",
                                               "participation.customer",
                                               false, true);
        participations.add(new ObjectRefNodeConstraint(
                "entity", customer.getObjectReference()));

        query.add(participations);
        query.add(new NodeConstraint("status", status));
        query.add(new NodeSortConstraint("startTime", false));

        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        List<IMObject> invoices = service.get(query).getResults();

        Act result = null;
        if (!invoices.isEmpty()) {
            result = (Act) invoices.get(0);
        }
        return result;
    }
}
