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
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
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

    /**
     * Creates an invoice for an estimation.
     * <p/>
     * The editor is displayed in a visible dialog, in order for medication and reminder popups to be displayed
     * correctly.
     *
     * @param estimation the estimation to invoice
     * @param invoice    the invoice to add to, or {@code null} to create a new invoice
     * @param context    the layout context
     * @return an editor for the invoice, or <tt>null</tt> if the editor cannot be created
     * @throws OpenVPMSException for any error
     */
    public CustomerChargeActEditDialog invoice(Act estimation, FinancialAct invoice, LayoutContext context) {
        estimation.setStatus(EstimationActStatus.INVOICED);
        ActBean estimationBean = new ActBean(estimation);

        if (invoice == null) {
            invoice = (FinancialAct) IMObjectCreator.create(CustomerAccountArchetypes.INVOICE);
            if (invoice == null) {
                throw new IllegalStateException("Failed to create invoice");
            }
            ActBean invoiceBean = new ActBean(invoice);
            invoiceBean.addNodeParticipation("customer", estimationBean.getNodeParticipantRef("customer"));
        }

        ChargeEditor editor = createChargeEditor(invoice, context);

        // NOTE: need to display the dialog as the process of populating medications and reminders can display
        // popups which would parent themselves on the wrong window otherwise.
        EditDialog dialog = new EditDialog(editor, estimation);
        dialog.show();
        for (Act estimationItem : estimationBean.getNodeActs("items")) {
            ActBean itemBean = new ActBean(estimationItem);
            CustomerChargeActItemEditor itemEditor = editor.add();
            itemEditor.setPatientRef(itemBean.getNodeParticipantRef("patient"));
            itemEditor.setQuantity(itemBean.getBigDecimal("highQty"));

            // NOTE: setting the product can trigger popups - want the popups to get the correct
            // property values from above
            itemEditor.setProductRef(itemBean.getNodeParticipantRef("product"));

            itemEditor.setFixedPrice(itemBean.getBigDecimal("fixedPrice"));
            itemEditor.setUnitPrice(itemBean.getBigDecimal("highUnitPrice"));
            itemEditor.setDiscount(itemBean.getBigDecimal("discount"));
        }
        editor.refresh();
        return dialog;
    }

    /**
     * Creates a new <tt>ChargeEditor</tt>.
     *
     * @param invoice the invoice
     * @param context the layout context
     * @return a new charge editor
     */
    protected ChargeEditor createChargeEditor(FinancialAct invoice, LayoutContext context) {
        return new ChargeEditor(invoice, context);
    }

    protected static class ChargeEditor extends CustomerChargeActEditor {

        /**
         * Constructs a <tt>ChargeEditor</tt>.
         *
         * @param act     the act to edit
         * @param context the layout context
         */
        public ChargeEditor(FinancialAct act, LayoutContext context) {
            super(act, null, context, false);
        }

        /**
         * Adds a new invoice item.
         *
         * @return the invoice item editor
         */
        public CustomerChargeActItemEditor add() {
            CustomerChargeActItemEditor result;
            ActRelationshipCollectionEditor items = getItems();
            Act item = (Act) items.create();
            if (item == null) {
                throw new IllegalStateException("Failed to create invoice item");
            }
            result = (CustomerChargeActItemEditor) items.createEditor(item, getLayoutContext());
            result.getComponent();
            items.addEdited(result);

            // need to flag as modified as the editor isn't hooked in to onCurrentEditorModified() 
            // to avoid redundant component creation
            items.setModified(item, true);
            return result;
        }

        /**
         * Refreshes the collection display.
         */
        public void refresh() {
            getItems().refresh();
        }

        /**
         * Returns the items collection editor.
         *
         * @return the items collection editor. May be <tt>null</tt>
         */
        @Override
        public ActRelationshipCollectionEditor getItems() {
            return super.getItems();
        }
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
                estimationSaved = result;
            } else {
                result = super.save(editor);
            }
            return result;
        }
    }

}
