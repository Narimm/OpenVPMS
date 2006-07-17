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

package org.openvpms.web.app.customer.account;

import org.openvpms.web.component.edit.Modifiable;
import org.openvpms.web.component.edit.ModifiableListener;
import org.openvpms.web.component.edit.Property;
import org.openvpms.web.component.im.edit.act.ActEditor;
import org.openvpms.web.component.im.layout.LayoutContext;

import org.openvpms.archetype.rules.tax.TaxRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;


/**
 * An editor of acts of type <em>act.customerAccountBadDebt</em>,
 * <em>act.customerAccountCreditAdjust</em>, or
 * <em>act.customerAccountDebitAdjust</em>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class AdjustmentActEditor extends ActEditor {

    /**
     * Construct a new <code>AdjustmentActEditor</code>.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be <code>null</code>
     * @param context the layout context. May be <code>null</code>
     */
    public AdjustmentActEditor(Act act, IMObject parent,
                               LayoutContext context) {
        super(act, parent, context);
        Property amount = getProperty("amount");
        amount.addModifiableListener(new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                updateTotals();
            }
        });
    }

    /**
     * Update the tax amount when the amount changes.
     */
    @Override
    protected void updateTotals() {
        TaxRules.calculateTax((FinancialAct) getObject(),
                              ArchetypeServiceHelper.getArchetypeService());
        Property property = getProperty("tax");
        property.refresh();

    }
}
