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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.workspace.customer.account;

import org.openvpms.archetype.rules.finance.tax.CustomerTaxRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.account.AccountActEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;

import java.math.BigDecimal;


/**
 * An editor of acts of type <em>act.customerAccountBadDebt</em>,
 * <em>act.customerAccountCreditAdjust</em>, or
 * <em>act.customerAccountDebitAdjust</em>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class AdjustmentActEditor extends AccountActEditor {

    /**
     * Construct a new <tt>AdjustmentActEditor</tt>.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be <tt>null</tt>
     * @param context the layout context. May be <tt>null</tt>
     */
    public AdjustmentActEditor(Act act, IMObject parent,
                               LayoutContext context) {
        super(act, parent, context);

        recalculateTax();  // recalculate tax, as per OVPMS-334

        Property amount = getProperty("amount");
        amount.addModifiableListener(new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                recalculateTax();
            }
        });
    }

    /**
     * Recalculates the tax amounts.
     */
    private void recalculateTax() {
        FinancialAct act = (FinancialAct) getObject();
        BigDecimal previousTax = act.getTaxAmount();
        Context context = getLayoutContext().getContext();
        Party practice = context.getPractice();
        if (practice != null) {
            CustomerTaxRules rules = new CustomerTaxRules(practice);
            BigDecimal tax = rules.calculateTax(act);
            if (tax.compareTo(previousTax) != 0) {
                Property property = getProperty("tax");
                property.refresh();
            }
        }
    }
}
