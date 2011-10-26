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

package org.openvpms.web.app.customer.estimation;

import org.openvpms.archetype.rules.act.EstimationActStatus;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.app.customer.charge.CustomerChargeActEditDialog;
import org.openvpms.web.app.customer.charge.CustomerChargeActEditor;
import org.openvpms.web.app.customer.charge.CustomerChargeActItemEditor;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.util.IMObjectCreator;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;


/**
 * Helper to create an invoice from an estimation.
 * <p/>
 * The invoice is returned in a dialog to:
 * <ul>
 * <li>enable the user to make changes
 * <li>edit labels and reminders
 * </ul>
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
class EstimationInvoicer {

    public EstimationInvoicer() {

    }

    /**
     * Creates an invoice for an estimation.
     * <p/>
     * The editor is displayed in a visible dialog, in order for medication and reminder popups to be displayed
     * correctly.
     * <p/>
     * TODO - this is not ideal, but is due to the current requirement that all dialogs be parented by the root window.
     *
     * @param estimation the estimation
     * @return an editor for the invoice, or <tt>null</tt> if the editor cannot be created
     * @throws OpenVPMSException for any error
     */
    public CustomerChargeActEditDialog invoice(Act estimation) {
        FinancialAct invoice = (FinancialAct) IMObjectCreator.create(CustomerAccountArchetypes.INVOICE);
        if (invoice != null) {
            estimation.setStatus(EstimationActStatus.INVOICED);
            ActBean estimationBean = new ActBean(estimation);
            ActBean invoiceBean = new ActBean(invoice);
            invoiceBean.addNodeParticipation("customer", estimationBean.getNodeParticipantRef("customer"));
            CustomerChargeActEditor editor = new CustomerChargeActEditor(invoice, null, new DefaultLayoutContext(true),
                                                                         false);
            EditDialog dialog = new EditDialog(editor, estimation);
            dialog.show();
            // NOTE: need to display the dialog as the process of populating medications and reminders can display
            // popups which would parent themselves on the wrong window otherwise.
            for (Act item : estimationBean.getNodeActs("items")) {
                ActBean itemBean = new ActBean(item);
                CustomerChargeActItemEditor itemEditor = editor.addItem();
                if (itemEditor != null) {
                    itemEditor.setPatientRef(itemBean.getNodeParticipantRef("patient"));
                    itemEditor.setQuantity(itemBean.getBigDecimal("highQty"));
                    itemEditor.setFixedPrice(itemBean.getBigDecimal("fixedPrice"));
                    itemEditor.setUnitPrice(itemBean.getBigDecimal("highUnitPrice"));
                    itemEditor.setDiscount(itemBean.getBigDecimal("discount"));

                    // NOTE: set the product last as it can trigger popups - want the popups to get the correct
                    // property values from above
                    itemEditor.setProductRef(itemBean.getNodeParticipantRef("product"));
                }
            }
            return dialog;
        }
        return null;
    }

    private static class EditDialog extends CustomerChargeActEditDialog {

        /**
         * The estimation.
         */
        private final Act estimation;

        /**
         * Determines if the estimation has been saved.
         */
        private boolean estimationSaved = false;

        /**
         * Constructs an <tt>EditDialog</tt>.
         *
         * @param editor     the invoice editor
         * @param estimation the estimation
         */
        public EditDialog(CustomerChargeActEditor editor, Act estimation) {
            super(editor);
            this.estimation = estimation;
        }

        /**
         * Saves the editor in a transaction.
         *
         * @param editor the editor
         * @return <tt>true</tt> if the save was successful
         */
        @Override
        protected boolean save(final IMObjectEditor editor) {
            boolean result;

            if (!estimationSaved) {
                TransactionCallback<Boolean> callback = new TransactionCallback<Boolean>() {
                    public Boolean doInTransaction(TransactionStatus status) {
                        return SaveHelper.save(estimation) && SaveHelper.save(editor);
                    }
                };
                result = SaveHelper.save(editor.getDisplayName(), callback);
            } else {
                result = super.save(editor);
            }
            return result;
        }
    }
}
