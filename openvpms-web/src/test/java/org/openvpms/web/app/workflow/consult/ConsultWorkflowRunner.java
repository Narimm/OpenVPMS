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

package org.openvpms.web.app.workflow.consult;

import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.app.customer.charge.ChargePopupEditorManager;
import org.openvpms.web.app.patient.charge.VisitChargeEditor;
import org.openvpms.web.app.patient.charge.VisitChargeItemRelationshipCollectionEditor;
import org.openvpms.web.app.patient.history.PatientHistoryQuery;
import org.openvpms.web.app.patient.visit.VisitBrowserCRUDWindow;
import org.openvpms.web.app.patient.visit.VisitCRUDWindow;
import org.openvpms.web.app.patient.visit.VisitChargeCRUDWindow;
import org.openvpms.web.app.patient.visit.VisitEditor;
import org.openvpms.web.app.patient.visit.VisitEditorDialog;
import org.openvpms.web.app.workflow.EditVisitTask;
import org.openvpms.web.app.workflow.FinancialWorkflowRunner;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.dialog.PopupDialog;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectCreator;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.workflow.TaskContext;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.openvpms.web.app.customer.charge.CustomerChargeTestHelper.addItem;
import static org.openvpms.web.app.customer.charge.CustomerChargeTestHelper.createProduct;
import static org.openvpms.web.test.EchoTestHelper.fireDialogButton;


/**
 * Runs the {@link ConsultWorkflow}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
class ConsultWorkflowRunner extends FinancialWorkflowRunner<ConsultWorkflowRunner.TestWorkflow> {

    /**
     * The appointment/task.
     */
    private Act act;

    /**
     * Constructs a <tt>ConsultWorkflowRunner</tt> with an appointment/task.
     *
     * @param act      the appointment/task
     * @param practice the practice, used to determine tax rates
     * @param context  the context
     */
    public ConsultWorkflowRunner(Act act, Party practice, Context context) {
        super(practice);
        this.act = act;
        setWorkflow(new TestWorkflow(act, context));
    }

    public void addNote() {
        TestEditVisitTask task = (TestEditVisitTask) getTask();
        VisitEditorDialog dialog = task.getVisitDialog();

        // get the editor and add an item
        VisitEditor visitEditor = dialog.getEditor();
        TestVisitCRUDWindow window = (TestVisitCRUDWindow) visitEditor.getHistory();
        assertNotNull(window);
        window.addNote();
    }

    /**
     * Performs the "Add Visit & Note" operation.
     *
     * @return the added note
     */
    public Act addVisitAndNote() {
        TestEditVisitTask task = (TestEditVisitTask) getTask();
        VisitEditorDialog dialog = task.getVisitDialog();

        // get the editor
        VisitEditor visitEditor = dialog.getEditor();
        TestVisitCRUDWindow window = (TestVisitCRUDWindow) visitEditor.getHistory();
        assertNotNull(window);
        Act note = window.addVisitAndNote();
        return get(note);  // need to reload as the Retryer loads it before linking it to the event
    }

    /**
     * Verifies that the current task is an EditVisitTask, and adds an invoice item.
     *
     * @param patient   the patient
     * @param clinician the clinician. May be <tt>null</tt>
     * @return the invoice total
     */
    public BigDecimal addVisitInvoiceItem(Party patient, User clinician) {
        BigDecimal amount = BigDecimal.valueOf(20);
        addVisitInvoiceItem(patient, amount, clinician);
        return getInvoice().getTotal();
    }

    /**
     * Verifies that the current task is an EditInvoiceTask, and adds invoice item for the specified amount.
     *
     * @param patient   the patient
     * @param amount    the amount
     * @param clinician the clinician. May be <tt>null</tt>
     * @return the edit dialog
     */
    public VisitEditorDialog addVisitInvoiceItem(Party patient, BigDecimal amount, User clinician) {
        TestEditVisitTask task = (TestEditVisitTask) getTask();
        VisitEditorDialog dialog = task.getVisitDialog();

        // get the editor and add an item
        VisitEditor visitEditor = dialog.getEditor();
        VisitChargeEditor editor = visitEditor.getChargeEditor();
        assertNotNull(editor);
        editor.setClinician(clinician);
        Product product = createProduct(ProductArchetypes.SERVICE, amount, getPractice());
        addItem(editor, patient, product, BigDecimal.ONE, task.getEditorManager());
        return dialog;
    }

    /**
     * Verifies that the workflow is complete, and the appointment/task status matches that expected.
     *
     * @param status the expected status
     */
    public void checkComplete(String status) {
        assertNull(getTask());
        act = get(act);
        assertNotNull(act);
        assertEquals(status, act.getStatus());
    }

    /**
     * Verifies the context matches that expected
     *
     * @param context   the context to check
     * @param customer  the expected context customer. May be <tt>null</tt>
     * @param patient   the expected context patient. May be <tt>null</tt>
     * @param clinician the expected clinician. May be <tt>null</tt>
     */
    public void checkContext(Context context, Party customer, Party patient, User clinician) {
        assertEquals(customer, context.getCustomer());
        assertEquals(patient, context.getPatient());
        assertEquals(clinician, context.getClinician());
    }

    protected static class TestWorkflow extends ConsultWorkflow {

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
         * Creates a new {@link EditVisitTask}.
         *
         * @return a new task to edit the visit
         */
        @Override
        protected EditVisitTask createEditVisitTask() {
            return new TestEditVisitTask();
        }
    }

    private static class TestVisitCRUDWindow extends VisitCRUDWindow {

        private final List<Act> saved = new ArrayList<Act>();

        public TestVisitCRUDWindow(Context context) {
            super(context);
        }

        /**
         * Performs the "Add Visit & Note" operation.
         *
         * @return the added note
         */
        public Act addVisitAndNote() {
            saved.clear();
            onAddNote();
            assertEquals(1, saved.size());
            assertTrue(TypeHelper.isA(saved.get(0), PatientArchetypes.CLINICAL_NOTE));
            return saved.get(0);
        }

        public void addNote() {
            Act act = (Act) IMObjectCreator.create(PatientArchetypes.CLINICAL_NOTE);
            assertNotNull(act);
            LayoutContext context = createLayoutContext();
            IMObjectEditor editor = createEditor(act, context);
            edit(editor);
        }

        /**
         * Edits an object.
         *
         * @param editor the object editor
         * @return the edit dialog
         */
        @Override
        protected EditDialog edit(IMObjectEditor editor) {
            EditDialog dialog = super.edit(editor);
            fireDialogButton(dialog, PopupDialog.OK_ID);
            return dialog;
        }

        /**
         * Invoked when the object has been saved.
         *
         * @param act   the object
         * @param isNew determines if the object is a new instance
         */
        @Override
        protected void onSaved(Act act, boolean isNew) {
            super.onSaved(act, isNew);
            if (!saved.contains(act)) {
                saved.add(act);
            }
        }
    }

    /**
     * Helper to edit invoices.
     * This is required to automatically close popup dialogs.
     */
    protected static class TestEditVisitTask extends EditVisitTask {

        /**
         * The popup dialog manager.
         */
        private ChargePopupEditorManager manager = new ChargePopupEditorManager();

        /**
         * Returns the popup dialog manager.
         *
         * @return the popup dialog manager
         */
        public ChargePopupEditorManager getEditorManager() {
            return manager;
        }

        /**
         * Creates a new visit editor.
         *
         * @param event   the event
         * @param invoice the invoice
         * @param context the task context
         * @param patient the patient
         * @return a new editor
         */
        @Override
        protected VisitEditor createVisitEditor(Act event, FinancialAct invoice, TaskContext context, Party patient) {
            return new VisitEditor(patient, event, invoice, context) {
                @Override
                protected VisitBrowserCRUDWindow createVisitBrowserCRUDWindow(Context context) {
                    return new TestVisitBrowserCRUDWindow(getQuery(), context);
                }

                @Override
                protected VisitChargeCRUDWindow createVisitChargeCRUDWindow(Act event, Context context) {
                    return new VisitChargeCRUDWindow(event, context) {
                        @Override
                        protected VisitChargeEditor createVisitChargeEditor(FinancialAct charge, Act event,
                                                                            LayoutContext context) {
                            return new TestVisitChargeEditor(charge, event, context);
                        }
                    };
                }
            };
        }

        private class TestVisitBrowserCRUDWindow extends VisitBrowserCRUDWindow {
            /**
             * Constructs a {@code VisitBrowserCRUDWindow}.
             *
             * @param query   the patient medical record query
             * @param context the context
             */
            public TestVisitBrowserCRUDWindow(PatientHistoryQuery query, Context context) {
                super(query, context);
            }

            protected VisitCRUDWindow createWindow(Context context) {
                return new TestVisitCRUDWindow(context);
            }

        }

        private class TestVisitChargeEditor extends VisitChargeEditor {
            public TestVisitChargeEditor(FinancialAct charge, Act event, LayoutContext context) {
                super(charge, event, context, false); // don't add a default item...
            }

            @Override
            protected ActRelationshipCollectionEditor createItemsEditor(Act act,
                                                                        CollectionProperty items) {
                VisitChargeItemRelationshipCollectionEditor result
                        = new VisitChargeItemRelationshipCollectionEditor(items, act, getLayoutContext());
                result.setPopupEditorManager(manager);
                return result;
            }
        }
    }


}
