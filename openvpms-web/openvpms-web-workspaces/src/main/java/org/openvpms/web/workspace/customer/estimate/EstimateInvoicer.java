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

package org.openvpms.web.workspace.customer.estimate;

import org.openvpms.archetype.rules.act.EstimateActStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.workspace.customer.charge.AbstractCustomerChargeActEditor;
import org.openvpms.web.workspace.customer.charge.AbstractInvoicer;
import org.openvpms.web.workspace.customer.charge.CustomerChargeActEditDialog;
import org.openvpms.web.workspace.customer.charge.CustomerChargeActEditor;
import org.openvpms.web.workspace.customer.charge.CustomerChargeActItemEditor;


/**
 * Helper to create an invoice from an estimate.
 * <p/>
 * The invoice is returned in a dialog to:
 * <ul>
 * <li>enable the user to make changes
 * <li>edit labels and reminders
 * </ul>
 *
 * @author Tim Anderson
 */
public class EstimateInvoicer extends AbstractInvoicer {

    /**
     * Creates an invoice for an estimate.
     * <p/>
     * The editor is displayed in a visible dialog, in order for medication and reminder popups to be displayed
     * correctly.
     *
     * @param estimate the estimate to invoice
     * @param invoice  the invoice to add to, or {@code null} to create a new invoice
     * @param context  the layout context
     * @return an editor for the invoice, or {@code null} if the editor cannot be created
     * @throws OpenVPMSException for any error
     */
    public CustomerChargeActEditDialog invoice(Act estimate, FinancialAct invoice, LayoutContext context) {
        estimate.setStatus(EstimateActStatus.INVOICED);
        ActBean estimateBean = new ActBean(estimate);

        if (invoice == null) {
            invoice = createInvoice(estimateBean.getNodeParticipantRef("customer"));
        }

        CustomerChargeActEditor editor = createChargeEditor(invoice, context);

        // NOTE: need to display the dialog as the process of populating medications and reminders can display
        // popups which would parent themselves on the wrong window otherwise.
        ChargeDialog dialog = new ChargeDialog(editor, estimate, context.getContext());
        dialog.show();
        invoice(estimate, editor);
        return dialog;
    }

    /**
     * Invoices an estimate.
     *
     * @param estimate the estimate to invoice
     * @param editor   the editor to add invoice items to
     */
    public void invoice(Act estimate, AbstractCustomerChargeActEditor editor) {
        ActBean bean = new ActBean(estimate);
        ActRelationshipCollectionEditor items = editor.getItems();

        for (Act estimationItem : bean.getNodeActs("items")) {
            ActBean itemBean = new ActBean(estimationItem);
            CustomerChargeActItemEditor itemEditor = getItemEditor(editor);
            itemEditor.setPatientRef(itemBean.getNodeParticipantRef("patient"));
            itemEditor.setQuantity(itemBean.getBigDecimal("highQty"));

            // NOTE: setting the product can trigger popups - want the popups to get the correct
            // property values from above
            itemEditor.setProductRef(itemBean.getNodeParticipantRef("product"));

            itemEditor.setFixedPrice(itemBean.getBigDecimal("fixedPrice"));
            itemEditor.setUnitPrice(itemBean.getBigDecimal("highUnitPrice"));
            itemEditor.setDiscount(itemBean.getBigDecimal("highDiscount"));
        }
        items.refresh();
        ActRelationshipCollectionEditor customerNotes = editor.getCustomerNotes();
        if (customerNotes != null) {
            for (Act note : bean.getNodeActs("customerNotes")) {
                IMObjectEditor noteEditor = customerNotes.getEditor(note);
                noteEditor.getComponent();
                customerNotes.addEdited(noteEditor);
            }
        }
        ActRelationshipCollectionEditor documents = editor.getDocuments();
        if (documents != null) {
            for (Act document : bean.getNodeActs("documents")) {
                IMObjectEditor documentsEditor = documents.getEditor(document);
                documentsEditor.getComponent();
                documents.addEdited(documentsEditor);
            }
        }
    }

}
