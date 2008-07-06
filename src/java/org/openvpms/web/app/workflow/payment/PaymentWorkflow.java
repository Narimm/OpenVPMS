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

package org.openvpms.web.app.workflow.payment;

import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.workflow.ConditionalTask;
import org.openvpms.web.component.workflow.ConfirmationTask;
import org.openvpms.web.component.workflow.DefaultTaskContext;
import org.openvpms.web.component.workflow.EditAccountActTask;
import org.openvpms.web.component.workflow.Task;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.component.workflow.TaskListener;
import org.openvpms.web.component.workflow.WorkflowImpl;
import org.openvpms.web.resource.util.Messages;


/**
 * A workflow that prompts the user to pay the account. If selected, it
 * displays an editor for an <em>act.customerAccountPayment</em>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class PaymentWorkflow extends WorkflowImpl {

    /**
     * The initial context.
     */
    private final TaskContext initial;


    /**
     * Creates a new <tt>PaymentWorkflow</tt>.
     */
    public PaymentWorkflow() {
        this(new DefaultTaskContext(false));
    }

    /**
     * Creates a new <tt>PaymentWorkflow</tt>.
     * <p/>
     *
     * @param context the task context
     */
    public PaymentWorkflow(TaskContext context) {
        GlobalContext global = GlobalContext.getInstance();
        initial = context;
        if (initial.getCustomer() == null) {
            initial.setCustomer(global.getCustomer());
        }
        if (initial.getPatient() == null) {
            initial.setPatient(global.getPatient());
        }
        if (initial.getClinician() == null) {
            initial.setClinician(global.getClinician());
        }
        if (initial.getUser() == null) {
            initial.setUser(global.getUser());
        }
        if (initial.getTill() == null) {
            initial.setTill(global.getTill());
        }

        if (initial.getPractice() == null) {
            initial.setPractice(global.getPractice());
        }
        if (initial.getLocation() == null) {
            // need to set location for cash rounding purposes during payments
            initial.setLocation(global.getLocation());
        }
    }

    /**
     * Starts the workflow.
     */
    @Override
    public void start() {
        start(initial);
    }

    /**
     * Starts the task.
     * <p/>
     * The registered {@link TaskListener} will be notified on completion or
     * failure.
     *
     * @param context the task context
     */
    @Override
    public void start(TaskContext context) {
        String payTitle = Messages.get("workflow.payment.payaccount.title");
        String payMsg = Messages.get("workflow.payment.payaccount.message");
        Task edit = new EditAccountActTask(CustomerAccountArchetypes.PAYMENT,
                                           true);
        boolean displayNo = !isRequired();
        addTask(new ConditionalTask(
                new ConfirmationTask(payTitle, payMsg, displayNo),
                edit));
        super.start(context);
    }

}
