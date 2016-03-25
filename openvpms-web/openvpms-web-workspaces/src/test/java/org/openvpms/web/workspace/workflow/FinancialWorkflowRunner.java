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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow;

import org.openvpms.archetype.rules.act.ActCalculator;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.workflow.EditIMObjectTask;
import org.openvpms.web.component.workflow.WorkflowImpl;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.customer.charge.ChargeItemRelationshipCollectionEditor;
import org.openvpms.web.workspace.customer.charge.CustomerChargeActEditor;
import org.openvpms.web.workspace.customer.charge.CustomerChargeActItemEditor;
import org.openvpms.web.workspace.patient.charge.VisitChargeEditor;
import org.openvpms.web.workspace.patient.charge.VisitChargeItemEditor;
import org.openvpms.web.workspace.patient.visit.VisitEditor;
import org.openvpms.web.workspace.patient.visit.VisitEditorDialog;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.openvpms.web.test.EchoTestHelper.fireDialogButton;
import static org.openvpms.web.workspace.customer.charge.CustomerChargeTestHelper.addItem;
import static org.openvpms.web.workspace.customer.charge.CustomerChargeTestHelper.createProduct;


/**
 * Helper to run financial workflows.
 *
 * @author Tim Anderson
 */
public abstract class FinancialWorkflowRunner<T extends WorkflowImpl> extends WorkflowRunner<T> {

    /**
     * The practice, used to determine tax rates.
     */
    private final Party practice;

    /**
     * Constructs a <tt>WorkflowRunner</tt>.
     *
     * @param practice the practice, used to determine tax rates
     */
    public FinancialWorkflowRunner(Party practice) {
        this.practice = practice;
    }

    /**
     * Returns the invoice.
     *
     * @return the invoice. May be {@code null}
     */
    public FinancialAct getInvoice() {
        return (FinancialAct) getContext().getObject(CustomerAccountArchetypes.INVOICE);
    }

    /**
     * Verifies that the current task is an EditInvoiceTask, and adds invoice item for the specified amount.
     *
     * @param patient   the patient
     * @param amount    the amount
     * @param clinician the clinician. May be {@code null}
     * @return the edit dialog
     */
    public EditDialog addInvoiceItem(Party patient, BigDecimal amount, User clinician) {
        EditIMObjectTask task = getEditTask();
        EditDialog dialog = task.getEditDialog();

        // get the editor and add an item
        CustomerChargeActEditor editor = (CustomerChargeActEditor) dialog.getEditor();
        editor.setClinician(clinician);
        Product product = createProduct(ProductArchetypes.SERVICE, amount, practice);
        ChargeItemRelationshipCollectionEditor items = (ChargeItemRelationshipCollectionEditor) editor.getItems();
        addItem(editor, patient, product, BigDecimal.ONE, items.getEditorQueue());
        return dialog;
    }

    /**
     * Verifies that the current task is an EditInvoiceTask, and adds invoice item, closing the dialog.
     *
     * @param patient   the patient
     * @param clinician the clinician. May be {@code null}
     * @param post      if <tt>true</tt> post the invoice
     * @return the invoice total
     */
    public BigDecimal addInvoice(Party patient, User clinician, boolean post) {
        BigDecimal amount = BigDecimal.valueOf(20);
        EditDialog dialog = addInvoiceItem(patient, amount, clinician);
        if (post) {
            CustomerChargeActEditor editor = (CustomerChargeActEditor) dialog.getEditor();
            editor.setStatus(ActStatus.POSTED);
        }
        fireDialogButton(dialog, PopupDialog.OK_ID);  // save the invoice
        return getInvoice().getTotal();
    }

    /**
     * Verifies that the invoice matches the specified details.
     *
     * @param status the expected status
     * @param amount the expected amount
     */
    public void checkInvoice(String status, BigDecimal amount) {
        FinancialAct act = get(getInvoice());
        assertEquals(act.getStatus(), status);
        assertTrue(amount.compareTo(act.getTotal()) == 0);
        ActCalculator calc = new ActCalculator(ServiceHelper.getArchetypeService());
        BigDecimal itemTotal = calc.sum(act, "total");
        assertTrue(amount.compareTo(itemTotal) == 0);
    }

    /**
     * Verifies that the current task is an EditVisitTask, and adds invoice item for the specified amount.
     *
     * @param patient   the patient
     * @param amount    the amount
     * @param clinician the clinician. May be {@code null}
     * @return the item editor
     */
    public CustomerChargeActItemEditor addVisitInvoiceItem(Party patient, BigDecimal amount, User clinician) {
        Product product = createProduct(ProductArchetypes.SERVICE, amount, getPractice());
        return addVisitInvoiceItem(patient, clinician, product);
    }

    /**
     * Verifies that the current task is an {@link EditVisitTask} and returns the corresponding dialog.
     */
    public VisitEditorDialog getVisitEditorDialog() {
        TestEditVisitTask task = (TestEditVisitTask) getTask();
        return task.getVisitDialog();
    }

    /**
     * Verifies that the current task is an {@link EditVisitTask}, and adds invoice item for the specified product.
     *
     * @param patient   the patient
     * @param clinician the clinician. May be {@code null}
     * @param product   the product
     * @return the item editor
     */
    public VisitChargeItemEditor addVisitInvoiceItem(Party patient, User clinician, Product product) {
        TestEditVisitTask task = (TestEditVisitTask) getTask();
        VisitEditorDialog dialog = task.getVisitDialog();

        // get the editor and add an item
        VisitEditor visitEditor = dialog.getEditor();
        VisitChargeEditor editor = visitEditor.getChargeEditor();
        assertNotNull(editor);
        editor.setClinician(clinician);
        return (VisitChargeItemEditor) addItem(editor, patient, product, BigDecimal.ONE, task.getQueue());
    }

    public VisitChargeItemEditor getVisitItemEditor() {
        TestEditVisitTask task = (TestEditVisitTask) getTask();
        VisitEditorDialog dialog = task.getVisitDialog();

        // get the editor and add an item
        VisitEditor visitEditor = dialog.getEditor();
        TestVisitChargeEditor editor = (TestVisitChargeEditor) visitEditor.getChargeEditor();
        assertNotNull(editor);
        return (VisitChargeItemEditor) editor.getItems().getCurrentEditor();
    }

    /**
     * Returns the practice.
     *
     * @return the practice
     */
    protected Party getPractice() {
        return practice;
    }

}
