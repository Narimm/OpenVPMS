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

import org.openvpms.web.app.workflow.checkout.PaymentEditTask;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.workflow.ConditionalTask;
import org.openvpms.web.component.workflow.ConfirmationTask;
import org.openvpms.web.component.workflow.DefaultTaskContext;
import org.openvpms.web.component.workflow.SynchronousTask;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.component.workflow.TaskListener;
import org.openvpms.web.component.workflow.Tasks;
import org.openvpms.web.component.workflow.WorkflowImpl;
import org.openvpms.web.resource.util.Messages;

import java.math.BigDecimal;


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
     * The charge amount that triggered the payment workflow.
     */
    private BigDecimal chargeAmount;


    /**
     * Constructs a <tt>PaymentWorkflow</tt>.
     *
     * @param chargeAmount the charge amount that triggered the payment workflow. If <tt>0</tt>, the context will be
     *                     examined for an invoice to determine the amount
     * @param context      the context
     */
    public PaymentWorkflow(BigDecimal chargeAmount, Context context) {
        this(new DefaultTaskContext(false), chargeAmount, context);
    }

    /**
     * Constructs a <tt>PaymentWorkflow</tt>.
     *
     * @param context         the task context
     * @param fallbackContext the context to fall back on if an object isn't in the task context
     */
    public PaymentWorkflow(TaskContext context, Context fallbackContext) {
        this(context, BigDecimal.ZERO, fallbackContext);
    }

    /**
     * Constructs a <tt>PaymentWorkflow</tt>.
     *
     * @param context         the task context
     * @param chargeAmount    the charge amount that triggered the payment workflow
     * @param fallbackContext the context to fall back on if an object isn't in the task context
     */
    public PaymentWorkflow(TaskContext context, BigDecimal chargeAmount, Context fallbackContext) {
        initial = context;
        this.chargeAmount = chargeAmount;

        if (initial.getCustomer() == null) {
            initial.setCustomer(fallbackContext.getCustomer());
        }
        if (initial.getPatient() == null) {
            initial.setPatient(fallbackContext.getPatient());
        }
        if (initial.getClinician() == null) {
            initial.setClinician(fallbackContext.getClinician());
        }
        if (initial.getUser() == null) {
            initial.setUser(fallbackContext.getUser());
        }
        if (initial.getTill() == null) {
            initial.setTill(fallbackContext.getTill());
        }

        if (initial.getPractice() == null) {
            initial.setPractice(fallbackContext.getPractice());
        }
        if (initial.getLocation() == null) {
            // need to set location for cash rounding purposes during payments
            initial.setLocation(fallbackContext.getLocation());
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

        Tasks tasks = new Tasks();
        tasks.addTask(new PaymentEditTask(chargeAmount));

        // add a task to update the global context at the end of the workflow
        tasks.addTask(new SynchronousTask() {
            public void execute(TaskContext context) {
                Context global = GlobalContext.getInstance();
                global.setCustomer(context.getCustomer());
                global.setPatient(context.getPatient());
                global.setTill(context.getTill());
                global.setClinician(context.getClinician());
            }
        });

        boolean displayNo = !isRequired();
        addTask(new ConditionalTask(
                new ConfirmationTask(payTitle, payMsg, displayNo),
                tasks));
        super.start(context);
    }

}
