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

package org.openvpms.web.component.im.edit.invoice;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.edit.act.ActEditor;
import org.openvpms.web.component.im.edit.act.ActHelper;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.Property;

import java.math.BigDecimal;
import java.util.List;


/**
 * An editor for {@link Act}s which have an archetype of
 * <em>act.customerAccountChargesInvoice</em>, <em>act.customerAccountChargesCredit</em>
 * <em>act.customerAccountChargesCounter</em>, <em>act.supplierAccountChargesInvoice</em>
 * or <em>act.supplierAccountChargesCredit</em>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate:2006-02-21 03:48:29Z $
 */
public abstract class InvoiceEditor extends ActEditor {

    /**
     * Construct a new <code>InvoiceEditor</code>.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be <code>null</code>
     * @param context the layout context
     */
    public InvoiceEditor(Act act, IMObject parent, LayoutContext context) {
        super(act, parent, context);
        recalculateTax();
    }

    /**
     * Update the tax amounts for the act.
     */
    public void calculateTax() {
        Property taxAmount = getProperty("tax");
        if (taxAmount != null) {
            List<Act> acts = getEditor().getActs();
            BigDecimal tax = ActHelper.sum((Act) getObject(), acts, "tax");
            taxAmount.setValue(tax);
        }
    }

    /**
     * Update totals when an act item changes.
     */
    protected void updateTotals() {
        // @todo - workaround for OVPMS-211
        Property amount = getProperty("amount");
        BigDecimal value = ActHelper.sum((Act) getObject(),
                                         getEditor().getActs(), "total");
        amount.setValue(value);
        calculateTax();
    }

    /**
     * Recalculates all tax amounts. See OVPMS-334.
     */
    private void recalculateTax() {
        Property taxAmount = getProperty("tax");
        if (taxAmount != null) {
            ActRelationshipCollectionEditor items = getEditor();
            List<Act> acts = items.getActs();
            for (Act act : acts) {
                // get the item editor. For CustomerInvoiceItemEditors, this
                // will recalculate the tax amount
                items.getEditor(act);
            }
            BigDecimal previousTax = (BigDecimal) taxAmount.getValue();
            BigDecimal tax = ActHelper.sum((Act) getObject(), acts, "tax");
            if (tax.compareTo(previousTax) != 0) {
                taxAmount.setValue(tax);
            }
        }
    }

}
