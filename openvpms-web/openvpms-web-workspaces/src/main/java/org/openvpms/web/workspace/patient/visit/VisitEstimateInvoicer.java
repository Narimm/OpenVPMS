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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.visit;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.web.workspace.customer.charge.CustomerChargeActEditor;
import org.openvpms.web.workspace.customer.estimate.EstimateInvoicer;
import org.openvpms.web.workspace.patient.charge.TemplateChargeItems;
import org.openvpms.web.workspace.patient.charge.VisitChargeItemRelationshipCollectionEditor;

import java.util.List;
import java.util.Map;

/**
 * Helper to create an invoice from an estimate in the Visit editor.
 * <p/>
 * This collects template items in order to add visit notes to patient history.
 *
 * @author Tim Anderson
 */
public class VisitEstimateInvoicer extends EstimateInvoicer {

    /**
     * Creates a strategy for invoicing an estimate.
     *
     * @param estimate the estimate to invoice
     * @param editor   the invoice editor
     * @return a new strategy
     */
    @Override
    protected InvoicingStrategy createStrategy(Act estimate, CustomerChargeActEditor editor) {
        return new VisitInvoicingStrategy(estimate, editor);
    }

    protected class VisitInvoicingStrategy extends InvoicingStrategy {

        /**
         * Constructs a {@link VisitInvoicingStrategy}.
         *
         * @param estimate the estimate to invoice
         * @param editor   the editor to add invoice items to
         */
        public VisitInvoicingStrategy(Act estimate, CustomerChargeActEditor editor) {
            super(estimate, editor);
        }

        /**
         * Notifies that one or more expanded templates were encountered when invoicing the estimate.
         * <p/>
         * This implementation registers them on the charge items editor so that any visit notes are created on commit.
         *
         * @param items the map of items to their corresponding template products
         */
        @Override
        protected void notifyTemplateExpansion(Map<Product, List<Act>> items) {
            List<TemplateChargeItems> templates = getItems().getTemplates();
            for (Map.Entry<Product, List<Act>> entry : items.entrySet()) {
                Product template = entry.getKey();
                List<Act> acts = entry.getValue();
                templates.add(new TemplateChargeItems(template, acts));
            }
            super.notifyTemplateExpansion(items);
        }

        /**
         * Returns the items collection editor.
         *
         * @return the items collection editor
         */
        @Override
        protected VisitChargeItemRelationshipCollectionEditor getItems() {
            return (VisitChargeItemRelationshipCollectionEditor) super.getItems();
        }
    }
}
