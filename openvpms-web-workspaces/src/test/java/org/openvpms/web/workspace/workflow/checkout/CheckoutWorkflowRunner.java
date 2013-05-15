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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.checkout;

import org.openvpms.archetype.rules.act.ActCalculator;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.payment.CustomerPaymentEditor;
import org.openvpms.web.component.print.BatchPrintDialog;
import org.openvpms.web.component.workflow.EditIMObjectTask;
import org.openvpms.web.component.workflow.Task;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.workflow.FinancialWorkflowRunner;

import java.math.BigDecimal;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.openvpms.web.test.EchoTestHelper.fireDialogButton;


/**
 * Runs the {@link CheckOutWorkflow}.
 *
 * @author Tim Anderson
 */
class CheckoutWorkflowRunner extends FinancialWorkflowRunner<CheckoutWorkflowRunner.TestWorkflow> {

    /**
     * The appointment/task.
     */
    private Act act;

    /**
     * The act end time, prior to running the workflow.
     */
    private Date endTime;

    /**
     * The act status, prior to running the workflow.
     */
    private String status;


    /**
     * Constructs a {@code CheckoutWorkflowRunner}.
     *
     * @param act      the act
     * @param practice the practice, used to determine tax rates
     * @param context  the context
     */
    public CheckoutWorkflowRunner(Act act, Party practice, Context context) {
        super(practice);
        this.act = act;
        endTime = act.getActivityEndTime();
        status = act.getStatus();
        setWorkflow(new TestWorkflow(act, context));
    }

    /**
     * Returns the payment.
     *
     * @return the payment. May be {@code null}
     */
    public FinancialAct getPayment() {
        return (FinancialAct) getContext().getObject(CustomerAccountArchetypes.PAYMENT);
    }

    /**
     * Verifies that the current task is a {@link PaymentEditTask}, adds a payment item, and closes the dialog.
     *
     * @param till the till to use
     */
    public void addPayment(Party till) {
        EditDialog dialog = addPaymentItem(till);
        fireDialogButton(dialog, PopupDialog.OK_ID);  // save the payment
    }

    /**
     * Verifies that the current task is a PaymentEditTask, and adds a payment item
     *
     * @param till the till to use
     * @return the edit dialog
     */
    public EditDialog addPaymentItem(Party till) {
        EditDialog dialog = getPaymentEditDialog();
        CustomerPaymentEditor paymentEditor = (CustomerPaymentEditor) dialog.getEditor();
        paymentEditor.setTill(till);
        paymentEditor.addItem();
        return dialog;
    }

    /**
     * Returns the payment edit dialog.
     *
     * @return the payment edit dialog
     */
    private EditDialog getPaymentEditDialog() {
        Task task = getTask();
        assertTrue(task instanceof PaymentEditTask);
        PaymentEditTask paymentTask = (PaymentEditTask) task;
        return paymentTask.getEditDialog();
    }

    /**
     * Verifies that the current task is an {@link PrintDocumentsTask}, and skips the dialog.
     */
    public void print() {
        Task task = getTask();
        assertTrue(task instanceof PrintDocumentsTask);
        BatchPrintDialog print = ((PrintDocumentsTask) task).getPrintDialog();
        fireDialogButton(print, PopupDialog.SKIP_ID);
    }

    /**
     * Verifies that the items in the context match that expected.
     *
     * @param context   the context to check
     * @param customer  the expected customer. May be {@code null}
     * @param patient   the expected patient. May be {@code null}
     * @param till      the expected till. May be {@code null}
     * @param clinician the expected clinician. May be {@code null}
     */
    public void checkContext(Context context, Party customer, Party patient, Party till, User clinician) {
        assertEquals(patient, context.getPatient());
        assertEquals(customer, context.getCustomer());
        assertEquals(till, context.getTill());
        assertEquals(clinician, context.getClinician());
    }

    /**
     * Verifies that the workflow is complete.
     *
     * @param statusUpdated if {@code true} expect the appointment/task status to be COMPLETE
     */
    public void checkComplete(boolean statusUpdated) {
        assertNull(getTask());
        boolean isTask = TypeHelper.isA(act, ScheduleArchetypes.TASK);
        if (isTask) {
            assertNull(endTime);
        }
        act = get(act);
        if (statusUpdated) {
            assertEquals(ActStatus.COMPLETED, act.getStatus());
            if (isTask) {
                assertNotNull(act.getActivityEndTime());
            }
        } else {
            assertEquals(status, act.getStatus());
            if (isTask) {
                assertNull(act.getActivityEndTime());
            }
        }
    }

    /**
     * Verifies that the payment matches the specified details.
     *
     * @param status the expected status
     * @param amount the expected amount
     */
    public void checkPayment(String status, BigDecimal amount) {
        FinancialAct act = get(getPayment());
        assertEquals(act.getStatus(), status);
        assertTrue(amount.compareTo(act.getTotal()) == 0);
        ActCalculator calc = new ActCalculator(ServiceHelper.getArchetypeService());
        BigDecimal itemTotal = calc.sum(act, "amount");
        assertTrue(amount.compareTo(itemTotal) == 0);
    }

    protected static class TestWorkflow extends CheckOutWorkflow {

        /**
         * Constructs a new {@code TestWorkflow} from an <em>act.customerAppointment</em> or <em>act.customerTask</em>.
         *
         * @param act     the act
         * @param context the external context to access and update
         */
        public TestWorkflow(Act act, Context context) {
            super(act, context, new HelpContext("foo", null));
        }

        /**
         * Creates a new task to edit the invoice.
         *
         * @return a new task
         */
        @Override
        protected EditIMObjectTask createEditInvoiceTask() {
            return new EditInvoiceTask();
        }
    }
}
