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
import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.act.ActCalculator;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.FinancialTestHelper;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.archetype.rules.workflow.ScheduleTestHelper;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.app.customer.charge.AbstractCustomerChargeActEditorTest;
import org.openvpms.web.app.customer.charge.ChargeItemRelationshipCollectionEditor;
import org.openvpms.web.app.customer.charge.CustomerChargeActEditor;
import static org.openvpms.web.app.workflow.GetInvoiceTask.INVOICE_SHORTNAME;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.dialog.ConfirmationDialog;
import org.openvpms.web.component.dialog.PopupDialog;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.edit.payment.CustomerPaymentEditor;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.print.BatchPrintDialog;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.workflow.ConfirmationTask;
import org.openvpms.web.component.workflow.EditIMObjectTask;
import org.openvpms.web.component.workflow.Task;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.component.workflow.TaskEvent;
import org.openvpms.web.component.workflow.TaskListener;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Tests the {@link CheckOutWorkflow}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class CheckoutWorkflowTestCase extends AbstractCustomerChargeActEditorTest {

    /**
     * The context.
     */
    private Context context;

    /**
     * The patient.
     */
    private Party patient;

    /**
     * The customer.
     */
    private Party customer;

    /**
     * The till.
     */
    private Party till;

    /**
     * The clinician.
     */
    private User clinician;


    /**
     * Tests the check-out workflow when started with an appointment.
     */
    @Test
    public void testCheckOutWorkflowForAppointment() {
        Act appointment = createAppointment();
        checkWorkflow(appointment);
    }

    /**
     * Tests the check-out workflow when started with a task.
     */
    @Test
    public void testCheckoutWorkflowForTask() {
        Date startTime = new Date();
        Date endTime = null; // updated at end of workflow
        Party workList = ScheduleTestHelper.createWorkList();

        Act task = ScheduleTestHelper.createTask(startTime, endTime, workList, customer, patient, clinician, null);
        save(task);
        checkWorkflow(task);
    }

    /**
     * Verifies that closing the invoice edit dialog by the 'x' button cancels the workflow.
     */
    @Test
    public void testCancelInvoiceByUserCloseNoSave() {
        checkCancelInvoice(false, true);
    }

    /**
     * Verifies that closing the invoice edit dialog by the 'x' button cancels the workflow.
     */
    @Test
    public void testCancelInvoiceByUserCloseAfterSave() {
        checkCancelInvoice(true, true);
    }

    /**
     * Verifies that cancelling the invoice edit dialog by the 'Cancel' button cancels the workflow.
     */
    @Test
    public void testCancelInvoiceByCancelButtonNoSave() {
        checkCancelInvoice(false, false);
    }

    /**
     * Verifies that cancelling the invoice edit dialog by the 'Cancel' button cancels the workflow.
     */
    @Test
    public void testCancelInvoiceByCancelButtonAfterSave() {
        checkCancelInvoice(true, false);
    }

    /**
     * Tests the behaviour of clicking the 'no' button on the finalise invoice confirmation dialog.
     */
    @Test
    public void testNoFinaliseInvoice() {
        WorkflowRunner workflow = new WorkflowRunner();
        workflow.start();
        BigDecimal amount = workflow.checkInvoice(false);
        workflow.checkConfirmation(PopupDialog.NO_ID);        // skip posting the invoice. Payment is skipped
        workflow.checkPrint();
        workflow.checkComplete(true, false, true);
        workflow.checkInvoice(ActStatus.IN_PROGRESS, amount);
        assertNull(workflow.getPayment());
    }

    /**
     * Tests the behaviour of clicking the 'cancel' button on the finalise invoice confirmation dialog.
     */
    @Test
    public void testCancelFinaliseInvoice() {
        WorkflowRunner workflow = new WorkflowRunner();
        workflow.start();
        BigDecimal amount = workflow.checkInvoice(false);
        workflow.checkConfirmation(PopupDialog.CANCEL_ID);
        workflow.checkComplete(false, false, false);
        workflow.checkInvoice(ActStatus.IN_PROGRESS, amount);
        assertNull(workflow.getPayment());
    }

    /**
     * Tests the behaviour of clicking the 'user close' button on the finalise invoice confirmation dialog.
     */
    @Test
    public void testUserCloseFinaliseInvoice() {
        WorkflowRunner workflow = new WorkflowRunner();
        workflow.start();
        BigDecimal amount = workflow.checkInvoice(false);
        workflow.checkConfirmation(null);
        workflow.checkComplete(false, false, false);
        workflow.checkInvoice(ActStatus.IN_PROGRESS, amount);
        assertNull(workflow.getPayment());
    }

    /**
     * Verifies that the payment can be skipped.
     */
    @Test
    public void testSkipPayment() {
        WorkflowRunner workflow = new WorkflowRunner();
        workflow.start();
        BigDecimal amount = workflow.checkInvoice(true);

        workflow.checkConfirmation(PopupDialog.NO_ID); // skip payment

        workflow.checkPrint();
        workflow.checkComplete(true, false, true);
        workflow.checkInvoice(ActStatus.POSTED, amount);
        assertNull(workflow.getPayment());
    }

    /**
     * Verifies that the workflow cancels if the 'Cancel' button is pressed at the payment confirmation.
     */
    @Test
    public void testCancelPaymentConfirmation() {
        WorkflowRunner workflow = new WorkflowRunner();
        workflow.start();
        BigDecimal amount = workflow.checkInvoice(true);

        workflow.checkConfirmation(PopupDialog.CANCEL_ID); // cancel payment

        workflow.checkComplete(false, false, false);
        workflow.checkInvoice(ActStatus.POSTED, amount);
        assertNull(workflow.getPayment());
    }

    /**
     * Verifies that the workflow cancels if the 'user close' button is pressed at the payment confirmation.
     */
    @Test
    public void testUserClosePaymentConfirmation() {
        WorkflowRunner workflow = new WorkflowRunner();
        workflow.start();
        BigDecimal amount = workflow.checkInvoice(true);

        workflow.checkConfirmation(null); // cancel payment

        workflow.checkComplete(false, false, false);
        workflow.checkInvoice(ActStatus.POSTED, amount);
        assertNull(workflow.getPayment());
    }

    /**
     * Verifies that the workflow completes after payment is cancelled.
     */
    @Test
    public void testCancelPayment() {
        WorkflowRunner workflow = new WorkflowRunner();
        workflow.start();
        BigDecimal amount = workflow.checkInvoice(true);

        workflow.checkConfirmation(PopupDialog.YES_ID);
        EditDialog dialog = workflow.addPaymentItem();
        fireDialogButton(dialog, PopupDialog.CANCEL_ID);

        workflow.checkComplete(false, false, false);
        workflow.checkInvoice(ActStatus.POSTED, amount);
        FinancialAct payment = workflow.getPayment();
        assertNotNull(payment);
        assertTrue(payment.isNew()); // unsaved
    }

    /**
     * Verifies that the workflow completes after payment is cancelled by pressing the 'user close' button.
     */
    @Test
    public void testUserClosePayment() {
        WorkflowRunner workflow = new WorkflowRunner();
        workflow.start();
        BigDecimal amount = workflow.checkInvoice(true);

        workflow.checkConfirmation(PopupDialog.YES_ID);
        EditDialog dialog = workflow.addPaymentItem();
        dialog.userClose();

        workflow.checkComplete(false, false, false);
        workflow.checkInvoice(ActStatus.POSTED, amount);
        FinancialAct payment = workflow.getPayment();
        assertNotNull(payment);
        assertTrue(payment.isNew()); // unsaved
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        super.setUp();
        Party location = TestHelper.createLocation();
        User user = TestHelper.createUser();
        context = new LocalContext();
        context.setLocation(location);
        context.setUser(user);

        customer = TestHelper.createCustomer();
        patient = TestHelper.createPatient(customer);
        till = FinancialTestHelper.createTill();
        clinician = TestHelper.createClinician();
    }

    /**
     * Runs the workflow for the specified act.
     *
     * @param act the act
     */
    private void checkWorkflow(Act act) {
        WorkflowRunner workflow = new WorkflowRunner(act);
        workflow.start();

        // first task to pause should by the invoice edit task.
        BigDecimal amount = workflow.checkInvoice(false);

        // second task to pause should be a confirmation, prompting to post the invoice
        workflow.checkConfirmation(PopupDialog.YES_ID);

        // verify the invoice has been posted
        workflow.checkInvoice(ActStatus.POSTED, amount);

        // third task to pause should be a confirmation prompting to pay the invoice
        workflow.checkConfirmation(PopupDialog.YES_ID);

        // 4th task to pause should be payment editor
        workflow.checkPayment();
        workflow.checkPayment(ActStatus.POSTED, amount);

        // 5th task to pause should be print dialog
        workflow.checkPrint();

        workflow.checkComplete(true, true, true);
    }

    /**
     * Verifies that cancelling the invoice cancels the workflow.
     *
     * @param save      if <tt>true</tt> save the invoice before cancelling
     * @param userClose if <tt>true</tt> cancel by clicking the 'x' button, otherwise cancel via the 'Cancel' button
     */
    private void checkCancelInvoice(boolean save, boolean userClose) {
        WorkflowRunner workflow = new WorkflowRunner();
        workflow.start();

        // first task to pause should by the invoice edit task.
        BigDecimal amount = BigDecimal.valueOf(20);
        EditDialog dialog = workflow.checkAddInvoiceItem(amount);
        if (save) {
            fireDialogButton(dialog, PopupDialog.APPLY_ID);          // save the invoice
        }
        workflow.checkAddInvoiceItem(amount);                    // add another item. Won't be saved

        // close the dialog
        if (userClose) {
            dialog.userClose();
        } else {
            fireDialogButton(dialog, PopupDialog.CANCEL_ID);
        }

        if (save) {
            workflow.checkInvoice(ActStatus.IN_PROGRESS, amount);
        } else {
            FinancialAct invoice = workflow.getInvoice();
            assertNotNull(invoice);
            assertTrue(invoice.isNew()); // unsaved
        }

        workflow.checkComplete(false, false, false);
        assertNull(workflow.getPayment());
    }

    /**
     * Helper to create an appointment.
     *
     * @return a new appoingtment
     */
    private Act createAppointment() {
        Date startTime = new Date();
        Date endTime = new Date();
        Party schedule = ScheduleTestHelper.createSchedule();

        Act act = ScheduleTestHelper.createAppointment(startTime, endTime, schedule, customer, patient, clinician, null);
        save(act);
        return act;
    }

    private class WorkflowRunner {

        /**
         * The appointment/task.
         */
        private Act act;

        /**
         * The task tracker.
         */
        private TaskTracker tracker;

        /**
         * The workflow.
         */
        private CheckOutWorkflow workflow;

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
         * Constructs a <tt>WorkflowRunner</tt> with an appointment.
         */
        public WorkflowRunner() {
            this(createAppointment());
        }

        /**
         * Constructs a <tt>WorkflowRunner</tt>.
         *
         * @param act the act
         */
        public WorkflowRunner(Act act) {
            this.act = act;
            endTime = act.getActivityEndTime();
            status = act.getStatus();
            tracker = new TaskTracker();
            workflow = new TestWorkflow(act, context);
            workflow.addTaskListener(tracker);
        }

        /**
         * Starts the workflow.
         */
        public void start() {
            workflow.start();
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
         * Verifies that the current task is an EditInvoiceTask, and adds invoice item for the specified amount.
         *
         * @param amount the amount
         * @return the edit dialog
         */
        public EditDialog checkAddInvoiceItem(BigDecimal amount) {
            // first task to pause should by the invoice edit task.
            Task current = tracker.getCurrent();
            assertTrue(current instanceof EditInvoiceTask);
            EditInvoiceTask editInvoiceTask = (EditInvoiceTask) current;
            EditDialog dialog = editInvoiceTask.getEditDialog();

            // get the editor and add an item
            CustomerChargeActEditor editor = (CustomerChargeActEditor) dialog.getEditor();
            editor.setClinician(clinician);
            invoice = (FinancialAct) editor.getObject();
            Product product = createProduct(ProductArchetypes.SERVICE, amount);
            addItem(editor, patient, product, BigDecimal.ONE, editInvoiceTask.getEditorManager());
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
            EditDialog dialog = checkAddInvoiceItem(amount);
            if (post) {
                CustomerChargeActEditor editor = (CustomerChargeActEditor) dialog.getEditor();
                editor.setStatus(ActStatus.POSTED);
            }
            fireDialogButton(dialog, PopupDialog.OK_ID);  // save the invoice
            return invoice.getTotal();
        }

        /**
         * Verifies that the current task is a confirmation, and selects the specified button.
         *
         * @param button the button identifier. If <tt>null</tt>, use the <tt>userClose</tt> method.
         */
        public void checkConfirmation(String button) {
            Task current = tracker.getCurrent();
            assertTrue(current instanceof ConfirmationTask);
            ConfirmationTask post = (ConfirmationTask) current;
            ConfirmationDialog dialog = post.getConfirmationDialog();
            if (button != null) {
                fireDialogButton(dialog, button);
            } else {
                dialog.userClose();
            }
        }

        /**
         * Verifies that the current task is a {@link PaymentEditTask}, adds a payment item, and closes the dialog.
         */
        public void checkPayment() {
            EditDialog dialog = addPaymentItem();
            fireDialogButton(dialog, PopupDialog.OK_ID);  // save the payment
        }

        /**
         * Verifies that the current task is a PaymentEditTask, and adds a payment item
         *
         * @return the edit dialog
         */
        public EditDialog addPaymentItem() {
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
            Task current = tracker.getCurrent();
            assertTrue(current instanceof PaymentEditTask);
            PaymentEditTask paymentTask = (PaymentEditTask) current;
            return paymentTask.getEditDialog();
        }

        /**
         * Verifies that the current task is an {@link PrintDocumentsTask}, and skips the dialog.
         */
        public void checkPrint() {
            Task current = tracker.getCurrent();
            assertTrue(current instanceof PrintDocumentsTask);
            BatchPrintDialog print = ((PrintDocumentsTask) current).getPrintDialog();
            fireDialogButton(print, PopupDialog.SKIP_ID);
        }

        /**
         * Verifies that the workflow is complete.
         *
         * @param invoiceContextUpdated if <tt>true</tt> expected the context attributes relating to invoicing to have
         *                              been updated
         * @param paymentContextUpdated if <tt>true</tt> expected the context attributes relating to payment to have
         *                              been updated
         * @param statusUpdated         if <tt>true</tt> expect the appointment/task status to be COMPLETE
         */
        public void checkComplete(boolean invoiceContextUpdated, boolean paymentContextUpdated,
                                  boolean statusUpdated) {
            assertNull(tracker.getCurrent());
            if (invoiceContextUpdated) {
                assertEquals(customer, context.getCustomer());
                assertEquals(patient, context.getPatient());
                assertEquals(clinician, context.getClinician());
            } else {
                assertNull(context.getCustomer());
                assertNull(context.getPatient());
                assertNull(context.getClinician());
            }
            if (paymentContextUpdated) {
                assertEquals(till, context.getTill());
            } else {
                assertNull(context.getTill());
            }
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
         * Verifies that the invoice matches the specified details.
         *
         * @param status the expected status
         * @param amount the expected amount
         */
        public void checkInvoice(String status, BigDecimal amount) {
            FinancialAct act = get(invoice);
            assertEquals(act.getStatus(), status);
            assertTrue(amount.compareTo(act.getTotal()) == 0);
            ActCalculator calc = new ActCalculator(getArchetypeService());
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
            ActCalculator calc = new ActCalculator(getArchetypeService());
            BigDecimal itemTotal = calc.sum(act, "amount");
            assertTrue(amount.compareTo(itemTotal) == 0);
        }
    }

    /**
     * Helper to edit invoices.
     * This is required to automatically close popup dialogs.
     */
    private static class EditInvoiceTask extends EditIMObjectTask {

        /**
         * The popup dialog manager.
         */
        private EditorManager manager = new EditorManager();

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
        public EditorManager getEditorManager() {
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

    private static class TestWorkflow extends CheckOutWorkflow {

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

    /**
     * Helper to track the currently executing task.
     */
    private static class TaskTracker implements TaskListener {

        /**
         * The current tasks.
         */
        private List<Task> current = new ArrayList<Task>();

        public Task getCurrent() {
            return current.size() > 0 ? current.get(current.size() - 1) : null;
        }

        /**
         * Invoked prior to a task starting.
         *
         * @param task the task
         */
        public void starting(Task task) {
            task.addTaskListener(this);
            current.add(task);
        }

        /**
         * Invoked when a task event occurs.
         *
         * @param event the event
         */
        public void taskEvent(TaskEvent event) {
            current.remove(event.getTask());
        }
    }
}
