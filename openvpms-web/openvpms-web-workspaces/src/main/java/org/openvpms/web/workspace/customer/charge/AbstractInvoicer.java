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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */


package org.openvpms.web.workspace.customer.charge;

import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.component.model.object.Reference;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.IMObjectEditorFactory;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectCreator;
import org.openvpms.web.system.ServiceHelper;

/**
 * Base class for classes invoicing an act.
 *
 * @author Tim Anderson
 */
public abstract class AbstractInvoicer {

    /**
     * Creates a new {@link CustomerChargeActEditor}.
     *
     * @param invoice the invoice
     * @param context the layout context
     * @return a new charge editor
     */
    protected CustomerChargeActEditor createChargeEditor(FinancialAct invoice, LayoutContext context) {
        IMObjectEditorFactory factory = ServiceHelper.getBean(IMObjectEditorFactory.class);
        CustomerChargeActEditor editor = (CustomerChargeActEditor) factory.create(invoice, context);
        editor.setAddDefaultItem(false);
        return editor;
    }

    /**
     * Creates a new invoice for a customer.
     *
     * @param customer the customer. May be {@code null}
     * @return a new invoice
     */
    protected FinancialAct createInvoice(Reference customer) {
        return createCharge(CustomerAccountArchetypes.INVOICE, customer);
    }

    /**
     * Creates a new charge for a customer.
     *
     * @param shortName the charge archetype short name
     * @param customer  the customer. May be {@code null}
     * @return a new charge
     */
    protected FinancialAct createCharge(String shortName, Reference customer) {
        FinancialAct result = (FinancialAct) IMObjectCreator.create(shortName);
        if (result == null) {
            throw new IllegalStateException("Failed to create " + shortName);
        }
        IMObjectBean invoiceBean = new IMObjectBean(result);
        if (customer != null) {
            invoiceBean.setTarget("customer", customer);
        }
        return result;
    }

    /**
     * Returns the next item editor for population.
     * <p>
     * This returns the current editor, if it has no product, else it creates a new one.
     *
     * @param editor the charge editor
     * @return the next item editor
     */
    protected CustomerChargeActItemEditor getItemEditor(CustomerChargeActEditor editor) {
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
            result = getItemEditor(act, editor);
        }

        return result;
    }

    /**
     * Returns a charge item editor for a charge item.
     *
     * @param act    the charge item act
     * @param editor the parent charge editor
     * @return an editor for the charge item
     */
    protected CustomerChargeActItemEditor getItemEditor(Act act, CustomerChargeActEditor editor) {
        ActRelationshipCollectionEditor items = editor.getItems();
        CustomerChargeActItemEditor result;
        result = (CustomerChargeActItemEditor) items.getEditor(act);
        result.getComponent();
        items.addEdited(result);
        items.setModified(act, true);
        // need to explicitly flag the  item as modified, or it can be excluded as a default value object
        return result;
    }


    /**
     * Dialog that ensures the act is saved when the charge is saved.
     */
    protected static class ChargeDialog extends CustomerChargeActEditDialog {

        /**
         * The act being charged.
         */
        private final Act act;

        /**
         * Determines if the act has been saved.
         */
        private boolean saved = false;

        /**
         * Constructs a {@link ChargeDialog}.
         *
         * @param editor  the charge editor
         * @param act     the order/return
         * @param context the context
         */
        public ChargeDialog(CustomerChargeActEditor editor, Act act, Context context) {
            super(editor, null, context, false); // suppress automatic charging of customer orders
            this.act = act;
        }

        /**
         * Saves the current object.
         *
         * @param editor the editor
         * @throws OpenVPMSException if the save fails
         */
        @Override
        protected void doSave(IMObjectEditor editor) {
            super.doSave(editor);
            if (!saved) {
                ServiceHelper.getArchetypeService().save(act);
                saved = true;
            }
        }
    }

}
