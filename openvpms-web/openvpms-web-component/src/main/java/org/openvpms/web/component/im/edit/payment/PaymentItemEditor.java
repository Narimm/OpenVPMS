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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.edit.payment;

import org.openvpms.archetype.rules.math.Currency;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.app.ContextHelper;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.util.ErrorHelper;

import java.math.BigDecimal;


/**
 * An editor for {@link Act}s which have an archetype in
 * <em>act.customerAccountPayment*</em>, <em>act.customerAccountRefund*</em>
 * <em>act.supplierAccountPayment*</em> and <em>act.supplierAccountRefund*</em>
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate:2006-02-21 03:48:29Z $
 */
public class PaymentItemEditor extends AbstractIMObjectEditor {

    /**
     * Creates a new <tt>PaymentItemEditor</tt>.
     *
     * @param act     the act to edit
     * @param parent  the parent act
     * @param context the layout context
     */
    public PaymentItemEditor(FinancialAct act, FinancialAct parent,
                             LayoutContext context) {
        super(act, parent, context);
        if (act.isNew() && parent != null) {
            // default the act start time to that of the parent
            act.setActivityStartTime(parent.getActivityStartTime());
        }
        if (getProperty("roundedAmount") != null) {
            // need to derive the rounded and tendered amounts from the amount
            Property amount = getProperty("amount");
            amount.addModifiableListener(new ModifiableListener() {
                public void modified(Modifiable modifiable) {
                    onAmountChanged();
                }
            });
            if (act.isNew()) {
                onAmountChanged();
            }
        }
    }

    /**
     * Invoked when the amount changes, to update the rounded and tendered
     * amount.
     * Only applies to cash payments.
     */
    private void onAmountChanged() {
        try {
            BigDecimal amount = (BigDecimal) getProperty("amount").getValue();
            BigDecimal rounded = amount;
            Property roundedAmount = getProperty("roundedAmount");
            Currency currency = ContextHelper.getPracticeCurrency(
                getLayoutContext().getContext());
            if (currency != null) {
                rounded = currency.roundCash(amount);
            }
            roundedAmount.setValue(rounded);
            Property tenderedAmount = getProperty("tendered");
            if (tenderedAmount != null) {
                tenderedAmount.setValue(rounded);
            }
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

}
