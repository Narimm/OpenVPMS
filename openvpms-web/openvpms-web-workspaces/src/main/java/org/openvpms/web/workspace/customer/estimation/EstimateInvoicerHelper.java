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

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.workspace.customer.charge.AbstractCustomerChargeActEditor;
import org.openvpms.web.workspace.customer.charge.CustomerChargeActItemEditor;

/**
 * Helper to invoice estimates.
 *
 * @author Tim Anderson
 */
public class EstimateInvoicerHelper {

    /**
     * Invoices an estimate.
     *
     * @param estimate the estimate to invoice
     * @param editor   the editor to add invoice items to
     */
    public static void invoice(Act estimate, AbstractCustomerChargeActEditor editor) {
        ActBean bean = new ActBean(estimate);
        ActRelationshipCollectionEditor items = editor.getItems();
        for (Act estimationItem : bean.getNodeActs("items")) {
            ActBean itemBean = new ActBean(estimationItem);
            Act act = (Act) items.create();
            if (act == null) {
                throw new IllegalStateException("Failed to create charge item");
            }
            CustomerChargeActItemEditor itemEditor = (CustomerChargeActItemEditor) items.getEditor(act);
            itemEditor.getComponent();
            items.addEdited(itemEditor);
            itemEditor.setPatientRef(itemBean.getNodeParticipantRef("patient"));
            itemEditor.setQuantity(itemBean.getBigDecimal("highQty"));

            // NOTE: setting the product can trigger popups - want the popups to get the correct
            // property values from above
            itemEditor.setProductRef(itemBean.getNodeParticipantRef("product"));

            itemEditor.setFixedPrice(itemBean.getBigDecimal("fixedPrice"));
            itemEditor.setUnitPrice(itemBean.getBigDecimal("highUnitPrice"));
            itemEditor.setDiscount(itemBean.getBigDecimal("discount"));
        }
        items.refresh();
    }
}
