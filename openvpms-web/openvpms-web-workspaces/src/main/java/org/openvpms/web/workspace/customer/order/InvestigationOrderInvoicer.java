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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.customer.order;

import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.order.OrderRules;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.workspace.customer.charge.CustomerChargeActEditor;
import org.openvpms.web.workspace.customer.charge.CustomerChargeActItemEditor;
import org.openvpms.web.workspace.patient.mr.PatientInvestigationActEditor;

/**
 * This class is responsible for cancelling investigations associated with from
 * <em>act.customerReturnInvestigation</em> acts.
 * <p/>
 * Unlike other {@link OrderInvoicer} implementations, this does not support the charging of new investigations,
 * or crediting returns. This is due to the fact that there is not a 1:1 relationship between a charge item and
 * investigations.
 *
 * @author Tim Anderson
 */
public class InvestigationOrderInvoicer extends OrderInvoicer {

    /**
     * Constructs a {@link InvestigationOrderInvoicer}.
     *
     * @param act   the order/return act
     * @param rules the order rules
     */
    public InvestigationOrderInvoicer(FinancialAct act, OrderRules rules) {
        super(act, rules);
    }

    /**
     * Determines if an order/return must charged via an editor.
     *
     * @return {@code true} if an editor is required
     */
    @Override
    public boolean requiresEdit() {
        return true;
    }

    @Override
    protected Item createItem(FinancialAct item, FinancialAct invoiceItem, FinancialAct invoice) {
        return new InvestigationItem(item, invoiceItem, invoice);
    }

    private class InvestigationItem extends Item {
        private final IMObjectReference investigation;

        public InvestigationItem(FinancialAct orderItem, FinancialAct invoiceItem, FinancialAct invoice) {
            super(orderItem, invoiceItem, invoice);
            ActBean bean = new ActBean(orderItem);
            this.investigation = bean.getReference("sourceInvestigation");
        }

        @Override
        public boolean canInvoice() {
            return !isOrder() && !isPosted() && getInvoiceItem() != null;
        }

        /**
         * Determines if an order or return can be credited.
         *
         * @return {@code false}
         */
        @Override
        public boolean canCredit() {
            return false;
        }

        @Override
        public boolean validate(Validator validator) {
            return super.validate(validator) && validateRequired(validator, "sourceInvestigation", investigation);
        }

        public void charge(CustomerChargeActEditor editor, CustomerChargeActItemEditor itemEditor) {
            if (!isOrder()) {
                PatientInvestigationActEditor investigation = itemEditor.getInvestigation(this.investigation);
                if (investigation != null) {
                    investigation.setStatus(ActStatus.CANCELLED);
                }
            }
        }

    }
}
