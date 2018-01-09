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

package org.openvpms.web.workspace.customer.estimate;

import org.openvpms.archetype.rules.act.EstimateActStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.web.component.edit.AlertListener;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.edit.act.TemplateProductListener;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.workspace.customer.charge.AbstractInvoicer;
import org.openvpms.web.workspace.customer.charge.ChargeItemRelationshipCollectionEditor;
import org.openvpms.web.workspace.customer.charge.CustomerChargeActEditDialog;
import org.openvpms.web.workspace.customer.charge.CustomerChargeActEditor;
import org.openvpms.web.workspace.customer.charge.CustomerChargeActItemEditor;
import org.openvpms.web.workspace.patient.charge.TemplateChargeItems;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
    public void invoice(Act estimate, CustomerChargeActEditor editor) {
        InvoicingStrategy strategy = createStrategy(estimate, editor);
        strategy.invoice();
    }

    /**
     * Creates a strategy for invoicing an estimate.
     *
     * @param estimate the estimate to invoice
     * @param editor   the invoice editor
     * @return a new strategy
     */
    protected InvoicingStrategy createStrategy(Act estimate, CustomerChargeActEditor editor) {
        return new InvoicingStrategy(estimate, editor);
    }

    /**
     * Strategy for invoicing estimates.
     */
    protected class InvoicingStrategy {

        /**
         * The estimate to invoice.
         */
        private final Act estimate;

        /**
         * The invoice editor.
         */
        private final CustomerChargeActEditor editor;

        /**
         * Collects template products to their corresponding charge items.
         */
        private Map<Product, List<Act>> templateItems = new HashMap<>();

        /**
         * Constructs an {@link InvoicingStrategy}.
         *
         * @param estimate the estimate to invoice
         * @param editor   the editor to add invoice items to
         */
        public InvoicingStrategy(Act estimate, CustomerChargeActEditor editor) {
            this.estimate = estimate;
            this.editor = editor;
        }

        /**
         * Invoices the estimate.
         */
        public void invoice() {
            ActBean bean = new ActBean(estimate);
            ActRelationshipCollectionEditor items = getItems();

            int outOfStock = 0;
            for (Act estimationItem : bean.getNodeActs("items")) {
                CustomerChargeActItemEditor itemEditor = invoice(estimationItem);
                if (itemEditor != null) {
                    BigDecimal stock = itemEditor.getStock();
                    if (stock != null && stock.compareTo(BigDecimal.ZERO) <= 0) {
                        outOfStock++;
                    }
                }
            }
            items.refresh();

            if (!templateItems.isEmpty()) {
                notifyTemplateExpansion(templateItems);
            }

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
            if (outOfStock != 0) {
                AlertListener listener = editor.getAlertListener();
                if (listener != null) {
                    listener.onAlert(Messages.format("customer.charge.outofstock", outOfStock));
                }
            }
        }

        /**
         * Invoices an estimate item.
         *
         * @param item the estimate item
         * @return the item editor, or {@code null} if the item wasn't invoiced
         */
        protected CustomerChargeActItemEditor invoice(Act item) {
            ActBean itemBean = new ActBean(item);
            CustomerChargeActItemEditor itemEditor = getItemEditor(editor);
            itemEditor.setPatientRef(itemBean.getNodeParticipantRef("patient"));
            itemEditor.setPrint(itemBean.getBoolean("print", true));

            // NOTE: setting the product can trigger popups - want the popups to get the correct
            // property values from above
            itemEditor.setMinimumQuantity(itemBean.getBigDecimal("lowQty"));
            IMObjectReference templateRef = itemBean.getNodeParticipantRef("template");
            IMObjectReference product = itemBean.getNodeParticipantRef("product");
            setProduct(itemEditor, product, templateRef);
            itemEditor.setQuantity(itemBean.getBigDecimal("highQty")); // replaces any doses

            itemEditor.setFixedPrice(itemBean.getBigDecimal("fixedPrice"));
            itemEditor.setUnitPrice(itemBean.getBigDecimal("highUnitPrice"));
            itemEditor.setDiscount(itemBean.getBigDecimal("highDiscount"));
            return itemEditor;
        }

        /**
         * Sets the product.
         *
         * @param itemEditor  the item editor
         * @param productRef  the product reference
         * @param templateRef the template reference. May be {@code null}
         */
        protected void setProduct(CustomerChargeActItemEditor itemEditor, IMObjectReference productRef,
                                  IMObjectReference templateRef) {
            itemEditor.setProduct(productRef, templateRef);
            Product template = itemEditor.getTemplate();
            if (template != null) {
                List<Act> items = templateItems.get(template);
                if (items == null) {
                    items = new ArrayList<>();
                    templateItems.put(template, items);
                }
                items.add(itemEditor.getObject());
            }
        }

        /**
         * Notifies that a one or more expanded templates were encountered when invoicing the estimate.
         *
         * @param items the map of items to their corresponding template products
         */
        protected void notifyTemplateExpansion(Map<Product, List<Act>> items) {
            List<TemplateChargeItems> templates = getItems().getTemplates();
            for (Map.Entry<Product, List<Act>> entry : items.entrySet()) {
                Product template = entry.getKey();
                List<Act> acts = entry.getValue();
                templates.add(new TemplateChargeItems(template, acts));
            }
            TemplateProductListener listener = getItems().getTemplateProductListener();
            if (listener != null) {
                for (Map.Entry<Product, List<Act>> entry : items.entrySet()) {
                    listener.expanded(entry.getKey());
                }
            }
        }

        /**
         * Returns the items collection editor.
         *
         * @return the items collection editor
         */
        protected ChargeItemRelationshipCollectionEditor getItems() {
            return editor.getItems();
        }

    }
}
