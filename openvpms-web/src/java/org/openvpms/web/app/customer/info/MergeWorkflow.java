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

package org.openvpms.web.app.customer.info;

import org.openvpms.archetype.rules.party.CustomerRules;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.workflow.ConfirmationTask;
import org.openvpms.web.component.workflow.DefaultTaskContext;
import org.openvpms.web.component.workflow.SelectIMObjectTask;
import org.openvpms.web.component.workflow.SynchronousTask;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.component.workflow.WorkflowImpl;
import org.openvpms.web.resource.util.Messages;


/**
 * Merge workflow.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class MergeWorkflow extends WorkflowImpl {

    /**
     * The customer to merge to.
     */
    private Party customer;

    /**
     * The initial context.
     */
    private final TaskContext initial;


    /**
     * Constructs a new <tt>MergeWorkflow</tt>.
     *
     * @param customer the customer to merge to
     */
    public MergeWorkflow(Party customer) {
        this.customer = customer;
        initial = new DefaultTaskContext(false);

        String mergeTitle = Messages.get("customer.merge.title");
        String mergeMsg = Messages.get("customer.merge.message");

        addTask(new ConfirmationTask(mergeTitle, mergeMsg, false));
        SelectIMObjectTask selectCustomer = new SelectIMObjectTask(
                "party.customerperson", initial);
        selectCustomer.setTitle(Messages.get("customer.merge.select.title",
                                             customer.getName()));
        selectCustomer.setMessage(
                Messages.get("customer.merge.select.message"));
        addTask(selectCustomer);
        addTask(new MergeTask());
    }

    /**
     * Starts the workflow.
     */
    @Override
    public void start() {
        super.start(initial);
    }

    private class MergeTask extends SynchronousTask {

        /**
         * Executes the task.
         *
         * @throws OpenVPMSException for any error
         */
        public void execute(TaskContext context) {
            Party from = context.getCustomer();
            CustomerRules rules = new CustomerRules();
            rules.mergeCustomers(from, customer);
        }
    }
}
