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
import org.openvpms.web.component.help.HelpContext;
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
 * @author Tim Anderson
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
     * The context to update at the end of the workflow.
     */
    private final Context parent;


    /**
     * Constructs a {@code PaymentWorkflow}.
     *
     * @param chargeAmount the charge amount that triggered the payment workflow. If {@code 0}, the context will be
     *                     examined for an invoice to determine the amount
     * @param context      the context
     * @param help         the help context
     */
    public PaymentWorkflow(BigDecimal chargeAmount, Context context, HelpContext help) {
        this(new DefaultTaskContext(null, help), chargeAmount, context);
    }

    /**
     * Constructs a {@code PaymentWorkflow}.
     *
     * @param context the task context
     * @param parent  the context to fall back on if an object isn't in the task context
     */
    public PaymentWorkflow(TaskContext context, Context parent) {
        this(context, BigDecimal.ZERO, parent);
    }

    /**
     * Constructs a {@code PaymentWorkflow}.
     *
     * @param initial      the initial task context
     * @param chargeAmount the charge amount that triggered the payment workflow
     * @param parent       the parent context to fall back on if an object isn't in the task context
     */
    public PaymentWorkflow(TaskContext initial, BigDecimal chargeAmount, Context parent) {
        super(initial.getHelpContext());
        this.initial = initial;
        this.chargeAmount = chargeAmount;
        this.parent = parent;

        if (this.initial.getCustomer() == null) {
            this.initial.setCustomer(parent.getCustomer());
        }
        if (this.initial.getPatient() == null) {
            this.initial.setPatient(parent.getPatient());
        }
        if (this.initial.getClinician() == null) {
            this.initial.setClinician(parent.getClinician());
        }
        if (this.initial.getUser() == null) {
            this.initial.setUser(parent.getUser());
        }
        if (this.initial.getTill() == null) {
            this.initial.setTill(parent.getTill());
        }

        if (this.initial.getPractice() == null) {
            this.initial.setPractice(parent.getPractice());
        }
        if (this.initial.getLocation() == null) {
            // need to set location for cash rounding purposes during payments
            this.initial.setLocation(parent.getLocation());
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

        Tasks tasks = new Tasks(context.getHelpContext());
        tasks.addTask(new PaymentEditTask(chargeAmount));

        // add a task to update the parent context at the end of the workflow
        tasks.addTask(new SynchronousTask() {
            public void execute(TaskContext context) {
                parent.setCustomer(context.getCustomer());
                parent.setPatient(context.getPatient());
                parent.setTill(context.getTill());
                parent.setClinician(context.getClinician());
            }
        });

        boolean displayNo = !isRequired();
        addTask(new ConditionalTask(
                new ConfirmationTask(payTitle, payMsg, displayNo),
                tasks));
        super.start(context);
    }

}
