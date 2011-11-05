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
 *
 *  $Id: $
 */

package org.openvpms.web.app.workflow.checkout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.openvpms.archetype.rules.act.ActCalculator;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.app.customer.charge.ChargeItemRelationshipCollectionEditor;
import org.openvpms.web.app.customer.charge.ChargePopupEditorManager;
import org.openvpms.web.app.customer.charge.CustomerChargeActEditor;
import static org.openvpms.web.app.customer.charge.CustomerChargeTestHelper.addItem;
import static org.openvpms.web.app.customer.charge.CustomerChargeTestHelper.createProduct;
import static org.openvpms.web.app.workflow.GetInvoiceTask.INVOICE_SHORTNAME;
import org.openvpms.web.app.workflow.WorkflowRunner;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.dialog.PopupDialog;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.edit.payment.CustomerPaymentEditor;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.print.BatchPrintDialog;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.workflow.EditIMObjectTask;
import org.openvpms.web.component.workflow.Task;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.system.ServiceHelper;
import static org.openvpms.web.test.EchoTestHelper.fireDialogButton;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Enter descroption.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
class CheckoutWorkflowRunner extends WorkflowRunner<CheckoutWorkflowRunner.TestWorkflow> {

    /**
     * The appointment/task.
     */
    private Act act;

    /**
     * The practice, used to determine tax rates.
     */
    private final Party practice;

    /**
     * The patient.
     */
    private Party patient;

    /**
     * The clinician.
     */
    private User clinician;

    /**
     * The invoice.
     */
    private FinancialAct invoice;

    /**
     * The payment.
     */
    private FinancialAct payment;

    /**
     * The act end time, prior to running the workflow.
     */
    private Date endTime;

    /**
     * The act status, prior to running the workflow.
     */
    private String status;


    /**
     * Constructs a <tt>CheckoutWorkflowRunner</tt>.
     *
     * @param act      the act
     * @param practice the practice, used to determine tax rates
     * @param context  the context
     */
    public CheckoutWorkflowRunner(Act act, Party practice, Context context) {
        this.act = act;
        this.practice = practice;
        endTime = act.getActivityEndTime();
        status = act.getStatus();
        setWorkflow(new TestWorkflow(act, context));
    }

    /**
     * Returns the invoice.
     *
     * @return the invoice. May be <tt>null</tt>
     */
    public FinancialAct getInvoice() {
        return invoice;
    }

    /**
     * Returns the payment.
     *
     * @return the payment. May be <tt>null</tt>
     */
    public FinancialAct getPayment() {
        return payment;
    }

    /**
     * Sets the patient to add to invoice items.
     *
     * @param patient the patient
     */
    public void setPatient(Party patient) {
        this.patient = patient;
    }

    /**
     * Sets the clinician to add to invoices.
     *
     * @param clinician the clinician
     */
    public void setClinician(User clinician) {
        this.clinician = clinician;
    }

    /**
     * Verifies that the current task is an EditInvoiceTask, and adds invoice item for the specified amount.
     *
     * @param amount the amount
     * @return the edit dialog
     */
    public EditDialog addInvoiceItem(BigDecimal amount) {
        EditInvoiceTask task = (EditInvoiceTask) getEditTask();
        EditDialog dialog = task.getEditDialog();

        // get the editor and add an item
        CustomerChargeActEditor editor = (CustomerChargeActEditor) dialog.getEditor();
        editor.setClinician(clinician);
        invoice = (FinancialAct) editor.getObject();
        Product product = createProduct(ProductArchetypes.SERVICE, amount, practice);
        addItem(editor, patient, product, BigDecimal.ONE, task.getEditorManager());
        return dialog;
    }

    /**
     * Verifies that the current task is an EditInvoiceTask, and adds invoice item, closing the dialog.
     *
     * @param post if <tt>true</tt> post the invoice
     * @return the invoice total
     */
    public BigDecimal checkInvoice(boolean post) {
        BigDecimal amount = BigDecimal.valueOf(20);
        EditDialog dialog = addInvoiceItem(amount);
        if (post) {
            CustomerChargeActEditor editor = (CustomerChargeActEditor) dialog.getEditor();
            editor.setStatus(ActStatus.POSTED);
        }
        fireDialogButton(dialog, PopupDialog.OK_ID);  // save the invoice
        return invoice.getTotal();
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
        payment = (FinancialAct) paymentEditor.getObject();
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
     * @param customer  the expected customer. May be <tt>null</tt>
     * @param patient   the expected patient. May be <tt>null</tt>
     * @param till      the expected till. May be <tt>null</tt>
     * @param clinician the expected clinician. May be <tt>null</tt>
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
     * @param statusUpdated if <tt>true</tt> expect the appointment/task status to be COMPLETE
     */
    public void checkComplete(boolean statusUpdated) {
        assertNull(getTask());
        boolean isTask = TypeHelper.isA(act, ScheduleArchetypes.TASK);
        if (isTask) {
            assertNull(endTime);
        }
        act = IMObjectHelper.reload(act);
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
     * Verifies that the invoice matches the specified details.
     *
     * @param status the expected status
     * @param amount the expected amount
     */
    public void checkInvoice(String status, BigDecimal amount) {
        FinancialAct act = get(invoice);
        assertEquals(act.getStatus(), status);
        assertTrue(amount.compareTo(act.getTotal()) == 0);
        ActCalculator calc = new ActCalculator(ServiceHelper.getArchetypeService());
        BigDecimal itemTotal = calc.sum(act, "total");
        assertTrue(amount.compareTo(itemTotal) == 0);
    }

    /**
     * Verifies that the payment matches the specified details.
     *
     * @param status the expected status
     * @param amount the expected amount
     */
    public void checkPayment(String status, BigDecimal amount) {
        FinancialAct act = get(payment);
        assertEquals(act.getStatus(), status);
        assertTrue(amount.compareTo(act.getTotal()) == 0);
        ActCalculator calc = new ActCalculator(ServiceHelper.getArchetypeService());
        BigDecimal itemTotal = calc.sum(act, "amount");
        assertTrue(amount.compareTo(itemTotal) == 0);
    }

    /**
     * Helper to edit invoices.
     * This is required to automatically close popup dialogs.
     */
    private static class EditInvoiceTask extends EditIMObjectTask {

        /**
         * The popup dialog manager.
         */
        private ChargePopupEditorManager manager = new ChargePopupEditorManager();

        /**
         * Constructs an <tt>EditInvoice</tt> to edit an object in the {@link TaskContext}.
         */
        public EditInvoiceTask() {
            super(INVOICE_SHORTNAME);
        }

        /**
         * Returns the popup dialog manager.
         *
         * @return the popup dialog manager
         */
        public ChargePopupEditorManager getEditorManager() {
            return manager;
        }

        /**
         * Creates a new editor for an object.
         *
         * @param object  the object to edit
         * @param context the task context
         * @return a new editor
         */
        @Override
        protected IMObjectEditor createEditor(IMObject object, TaskContext context) {
            LayoutContext layout = new DefaultLayoutContext(true);
            layout.setContext(context);
            return new CustomerChargeActEditor((FinancialAct) object, null, layout, false) {
                @Override
                protected ActRelationshipCollectionEditor createItemsEditor(Act act, CollectionProperty items) {
                    ActRelationshipCollectionEditor editor = super.createItemsEditor(act, items);
                    if (editor instanceof ChargeItemRelationshipCollectionEditor) {
                        // register a handler for act popups
                        ((ChargeItemRelationshipCollectionEditor) editor).setPopupEditorManager(manager);
                    }
                    return editor;
                }
            };
        }
    }

    protected static class TestWorkflow extends CheckOutWorkflow {

        /**
         * Constructs a new <tt>TestWorkflow</tt> from an <em>act.customerAppointment</em> or <em>act.customerTask</em>.
         *
         * @param act     the act
         * @param context the external context to access and update
         */
        public TestWorkflow(Act act, Context context) {
            super(act, context);
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
