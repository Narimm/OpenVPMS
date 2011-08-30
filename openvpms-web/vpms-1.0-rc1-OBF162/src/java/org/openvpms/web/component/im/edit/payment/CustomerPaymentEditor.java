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
import org.openvpms.web.component.im.layout.LayoutContext;


/**
 * An editor for {@link Act}s which have an archetype of
 * <em>act.customerAccountPayment</em> or <em>act.customerAccountRefund</em>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate:2006-02-21 03:48:29Z $
 */
public class CustomerPaymentEditor extends PaymentEditor {

    /**
     * Constructs a new <code>CustomerPaymentEditor</code>.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be <code>null</code>
     * @param context the layout context
     */
    public CustomerPaymentEditor(Act act, IMObject parent,
                                 LayoutContext context) {
        super(act, parent, context);
        initParticipant("customer", context.getContext().getCustomer());
    }

}
