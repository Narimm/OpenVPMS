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

package org.openvpms.web.component.im.edit.payment;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.edit.account.AccountActEditor;
import org.openvpms.web.component.im.edit.act.ActHelper;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.Property;

import java.math.BigDecimal;


/**
 * An editor for {@link Act}s which have an archetype of
 * <em>act.customerAccountPayment</em>, <em>act.customerAccountRefund</em>,
 * <em>act.supplierAccountPayment</em> or <em>act.supplierAccountRefund</em>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate:2006-02-21 03:48:29Z $
 */
public abstract class PaymentEditor extends AccountActEditor {

    /**
     * Construct a new <code>PaymentEditor</code>.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be <code>null</code>
     * @param context the layout context
     */
    public PaymentEditor(Act act, IMObject parent,
                         LayoutContext context) {
        super(act, parent, context);
    }

    /**
     * Update totals when an act item changes.
     */
    protected void updateTotals() {
        // @todo - workaround for OVPMS-211
        Property amount = getProperty("amount");
        BigDecimal value = ActHelper.sum((Act) getObject(),
                                         getEditor().getActs(), "amount");
        amount.setValue(value);
    }

}
