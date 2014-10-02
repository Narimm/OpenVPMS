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

package org.openvpms.web.workspace.customer.charge;

import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectCreator;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

/**
 * Base class for classes invoicing an act.
 *
 * @author Tim Anderson
 */
public class AbstractInvoicer {

    /**
     * Creates a new {@link CustomerChargeActEditor}.
     *
     * @param invoice the invoice
     * @param context the layout context
     * @return a new charge editor
     */
    protected CustomerChargeActEditor createChargeEditor(FinancialAct invoice, LayoutContext context) {
        return new CustomerChargeActEditor(invoice, null, context, false);
    }

    /**
     * Creates a new invoice for a customer.
     *
     * @param customer the customer. May be {@code null}
     * @return a new invoice
     */
    protected FinancialAct createInvoice(IMObjectReference customer) {
        FinancialAct result = (FinancialAct) IMObjectCreator.create(CustomerAccountArchetypes.INVOICE);
        if (result == null) {
            throw new IllegalStateException("Failed to create invoice");
        }
        ActBean invoiceBean = new ActBean(result);
        if (customer != null) {
            invoiceBean.addNodeParticipation("customer", customer);
        }
        return result;
    }

    /**
     * Returns the next item editor for population.
     * <p/>
     * This returns the current editor, if it has no product, else it creates a new one.
     *
     * @param editor the charge editor
     * @return the next item editor
     */
    protected CustomerChargeActItemEditor getItemEditor(AbstractCustomerChargeActEditor editor) {
        CustomerChargeActItemEditor result;
        // if there is an existing empty editor, populate it first
        ActRelationshipCollectionEditor items = editor.getItems();
        IMObjectEditor currentEditor = items.getCurrentEditor();
        if (currentEditor instanceof CustomerChargeActItemEditor &&
            ((CustomerChargeActItemEditor) currentEditor).getProductRef() == null) {
            result = (CustomerChargeActItemEditor) currentEditor;
        } else {
            Act act = (Act) items.create();
            if (act == null) {
                throw new IllegalStateException("Failed to create charge item");
            }
            result = (CustomerChargeActItemEditor) items.getEditor(act);
            result.getComponent();
            items.addEdited(result);
        }
        return result;
    }


    /**
     * Dialog that ensures the invoiced act is saved when the invoice is saved.
     */
    protected static class InvoicerDialog extends CustomerChargeActEditDialog {

        /**
         * The act being invoiced.
         */
        private final Act act;

        /**
         * Determines if the act has been saved.
         */
        private boolean saved = false;

        /**
         * Constructs an {@link InvoicerDialog}.
         *
         * @param editor  the invoice editor
         * @param act     the estimate
         * @param context the context
         */
        public InvoicerDialog(CustomerChargeActEditor editor, Act act, Context context) {
            super(editor, context);
            this.act = act;
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

            if (!saved) {
                TransactionCallback<Boolean> callback = new TransactionCallback<Boolean>() {
                    public Boolean doInTransaction(TransactionStatus status) {
                        return SaveHelper.save(act) && SaveHelper.save(editor);
                    }
                };
                result = SaveHelper.save(editor.getDisplayName(), callback);
                saved = result;
            } else {
                result = super.save(editor);
            }
            return result;
        }
    }

}
