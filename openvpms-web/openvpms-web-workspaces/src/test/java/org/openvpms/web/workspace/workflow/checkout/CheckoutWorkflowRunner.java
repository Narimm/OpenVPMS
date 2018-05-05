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

package org.openvpms.web.workspace.workflow.checkout;

import org.openvpms.archetype.rules.act.ActCalculator;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.payment.CustomerPaymentEditor;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.print.BatchPrintDialog;
import org.openvpms.web.component.workflow.Task;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.workflow.FinancialWorkflowRunner;

import java.math.BigDecimal;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.openvpms.web.test.EchoTestHelper.fireDialogButton;


/**
 * Runs the {@link CheckOutWorkflow}.
 *
 * @author Tim Anderson
 */
class CheckoutWorkflowRunner extends FinancialWorkflowRunner<CheckOutWorkflow> {

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
        context.setPractice(practice);
        this.act = act;
        endTime = act.getActivityEndTime();
        status = act.getStatus();
        setWorkflow(new TestWorkflow(act, context, new HelpContext("foo", null)));
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
            Visits visits = getWorkflow().getVisits();
            assertFalse(visits.isEmpty());
            for (Visit event : visits) {
                Act act = event.getEvent();
                assertNotNull(act);
                assertEquals(ActStatus.COMPLETED, act.getStatus());
                assertNotNull(act.getActivityEndTime());
            }

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

    private static class TestWorkflow extends CheckOutWorkflow {
        /**
         * Constructs a {@link TestWorkflow} from an <em>act.customerAppointment</em> or <em>act.customerTask</em>.
         *
         * @param act     the act
         * @param context the external context to access and update
         * @param help    the help context
         */
        public TestWorkflow(Act act, Context context, HelpContext help) {
            super(act, context, help);
        }

        /**
         * Creates a new task to performing charging.
         *
         * @param visits the events
         * @return a new task
         */
        @Override
        protected CheckoutEditInvoiceTask createChargeTask(Visits visits) {
            return new CheckoutEditInvoiceTask(visits) {
                /**
                 * Creates a new editor for an object.
                 * <p/>
                 * This implementation suppresses a default item from being added.
                 *
                 * @param object  the object to edit
                 * @param context the task context
                 * @return a new editor
                 */
                @Override
                protected IMObjectEditor createEditor(IMObject object, TaskContext context) {
                    LayoutContext layout = new DefaultLayoutContext(true, context, new HelpContext("foo", null));
                    return new CheckoutChargeEditor((FinancialAct) object, getVisits(), layout, false);
                }
            };
        }
    }
}
