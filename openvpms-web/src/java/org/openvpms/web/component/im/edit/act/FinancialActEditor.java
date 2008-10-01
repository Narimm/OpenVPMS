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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.edit.act;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.act.ActHelper;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.Property;

import java.math.BigDecimal;
import java.util.List;


/**
 * An editor for parent {@link FinancialAct}s.
 * <p/>
 * This is responsible for calculating the amount and tax when a child item
 * changes.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class FinancialActEditor extends ActEditor {

    /**
     * Creates a new <tt>FinancialActEditor</tt>.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be <tt>null</tt>
     * @param context the layout context. May be <tt>null</tt>
     */
    protected FinancialActEditor(FinancialAct act, IMObject parent,
                                 LayoutContext context) {
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
     * Updates the amount and tax when an act item changes.
     */
    @Override
    protected void onItemsChanged() {
        Property amount = getProperty("amount");
        BigDecimal value = ActHelper.sum((Act) getObject(),
                                         getEditor().getCurrentActs(), "total");
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
