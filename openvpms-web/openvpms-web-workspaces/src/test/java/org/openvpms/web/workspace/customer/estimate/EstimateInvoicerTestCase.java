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

package org.openvpms.web.workspace.customer.estimate;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.workspace.customer.charge.ChargeItemRelationshipCollectionEditor;
import org.openvpms.web.workspace.customer.charge.DefaultCustomerChargeActEditor;
import org.openvpms.web.workspace.customer.charge.EditorQueue;


/**
 * Tests the {@link EstimateInvoicer} class.
 *
 * @author Tim Anderson
 */
public class EstimateInvoicerTestCase extends AbstractEstimateInvoicerTestCase {

    /**
     * Creates a new {@link EstimateInvoicer}.
     *
     * @return a new estimate invoicer
     */
    @Override
    protected EstimateInvoicer createEstimateInvoicer() {
        return new TestEstimateInvoicer();
    }

    private class TestEstimateInvoicer extends EstimateInvoicer {

        /**
         * Constructs a {@code TestEstimateInvoicer}.
         *
         * @param invoice the invoice
         * @param context the layout context
         * @return a new charge editor
         */
        @Override
        protected DefaultCustomerChargeActEditor createChargeEditor(FinancialAct invoice, LayoutContext context) {
            final EditorQueue manager = createEditorQueue(context.getContext());
            return new DefaultCustomerChargeActEditor(invoice, null, context) {
                @Override
                protected ActRelationshipCollectionEditor createItemsEditor(Act act, CollectionProperty items) {
                    ActRelationshipCollectionEditor editor = super.createItemsEditor(act, items);
                    if (editor instanceof ChargeItemRelationshipCollectionEditor) {
                        // register a handler for act popups
                        ((ChargeItemRelationshipCollectionEditor) editor).setEditorQueue(manager);
                    }
                    return editor;
                }

            };
        }

    }

}
