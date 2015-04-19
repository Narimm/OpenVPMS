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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow;

import org.openvpms.archetype.rules.finance.account.CustomerAccountRules;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.app.ContextException;
import org.openvpms.web.component.workflow.SynchronousTask;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.system.ServiceHelper;


/**
 * Task to query the most recent <em>act.customerAccountChargesInvoice</em>
 * with IN_PROGRESS or COMPLETED status for the context customer.
 * If one is present, adds it to the context.
 *
 * @author Tim Anderson
 */
public class GetInvoiceTask extends SynchronousTask {

    /**
     * The rules.
     */
    private final CustomerAccountRules rules;

    /**
     * Constructs a {@link GetInvoiceTask}.
     */
    public GetInvoiceTask() {
        rules = ServiceHelper.getBean(CustomerAccountRules.class);
    }

    /**
     * Executes the task.
     *
     * @throws OpenVPMSException for any error
     */
    public void execute(TaskContext context) {
        Party customer = context.getCustomer();
        if (customer == null) {
            throw new ContextException(ContextException.ErrorCode.NoCustomer);
        }
        FinancialAct invoice = rules.getInvoice(customer);
        if (invoice != null) {
            context.addObject(invoice);
        }
    }
}
