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

import java.math.BigDecimal;

import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.edit.Property;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.act.ActEditor;
import org.openvpms.web.component.im.edit.act.ActHelper;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectHelper;


/**
 * An editor for {@link Act}s which have an archetype of
 * <em>act.customerAccountPayment</em>, <em>act.customerAccountRefund</em>,
 * <em>act.supplierAccountPayment</em> or <em>act.supplierAccountRefund</em>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate:2006-02-21 03:48:29Z $
 */
public class PaymentEditor extends ActEditor {

    /**
     * Construct a new <code>PaymentEditor</code>.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be <code>null</code>
     * @param context the layout context
     */
    protected PaymentEditor(Act act, IMObject parent,
                            LayoutContext context) {
        super(act, parent, context);
    }

    /**
     * Create a new editor for an object, if it can be edited by this class.
     *
     * @param object  the object to edit
     * @param parent  the parent object. May be <code>null</code>
     * @param context the layout context
     * @return a new editor for <code>object</code>, or <code>null</code> if it
     *         cannot be edited by this
     */
    public static IMObjectEditor create(IMObject object, IMObject parent,
                                        LayoutContext context) {
        IMObjectEditor result = null;
        if (IMObjectHelper.isA(object,
                               "act.customerAccountPayment",
                               "act.customerAccountRefund",
                               "act.supplierAccountPayment",
                               "act.supplierAccountRefund")) {
            result = new PaymentEditor((Act) object, parent, context);
        }
        return result;
    }

    /**
     * Update totals when an act item changes.
     *
     * @todo - workaround for OVPMS-211
     */
    protected void updateTotals() {
        Property amount = getProperty("amount");
        BigDecimal value = ActHelper.sum(getEditor().getActs(), "amount");
        amount.setValue(value);
    }

}
