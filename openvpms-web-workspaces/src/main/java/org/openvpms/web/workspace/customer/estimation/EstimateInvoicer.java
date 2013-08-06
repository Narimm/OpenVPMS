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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.customer.estimation;

import org.openvpms.archetype.rules.act.EstimateActStatus;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectCreator;
import org.openvpms.web.workspace.customer.charge.CustomerChargeActEditDialog;
import org.openvpms.web.workspace.customer.charge.CustomerChargeActEditor;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;


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
class EstimateInvoicer {

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
            invoice = (FinancialAct) IMObjectCreator.create(CustomerAccountArchetypes.INVOICE);
            if (invoice == null) {
                throw new IllegalStateException("Failed to create invoice");
            }
            ActBean invoiceBean = new ActBean(invoice);
            invoiceBean.addNodeParticipation("customer", estimateBean.getNodeParticipantRef("customer"));
        }

        CustomerChargeActEditor editor = createChargeEditor(invoice, context);

        // NOTE: need to display the dialog as the process of populating medications and reminders can display
        // popups which would parent themselves on the wrong window otherwise.
        EditDialog dialog = new EditDialog(editor, estimate, context.getContext());
        dialog.show();
        EstimateInvoicerHelper.invoice(estimate, editor);
        return dialog;
    }

    /**
     * Creates a new {@code ChargeEditor}.
     *
     * @param invoice the invoice
     * @param context the layout context
     * @return a new charge editor
     */
    protected CustomerChargeActEditor createChargeEditor(FinancialAct invoice, LayoutContext context) {
        return new CustomerChargeActEditor(invoice, null, context, false);
    }

    private static class EditDialog extends CustomerChargeActEditDialog {

        /**
         * The estimate.
         */
        private final Act estimate;

        /**
         * Determines if the estimation has been saved.
         */
        private boolean estimateSaved = false;

        /**
         * Constructs an {@code EditDialog}.
         *
         * @param editor   the invoice editor
         * @param estimate the estimate
         * @param context  the context
         */
        public EditDialog(CustomerChargeActEditor editor, Act estimate, Context context) {
            super(editor, context);
            this.estimate = estimate;
        }

        /**
         * Saves the editor in a transaction.
         *
         * @param editor the editor
         * @return {@code true} if the save was successful
         */
        @Override
        protected boolean save(final IMObjectEditor editor) {
            boolean result;

            if (!estimateSaved) {
                TransactionCallback<Boolean> callback = new TransactionCallback<Boolean>() {
                    public Boolean doInTransaction(TransactionStatus status) {
                        return SaveHelper.save(estimate) && SaveHelper.save(editor);
                    }
                };
                result = SaveHelper.save(editor.getDisplayName(), callback);
                estimateSaved = result;
            } else {
                result = super.save(editor);
            }
            return result;
        }
    }

}
