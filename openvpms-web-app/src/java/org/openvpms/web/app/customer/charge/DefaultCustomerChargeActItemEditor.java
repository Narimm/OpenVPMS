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
 *  $Id:CustomerInvoiceItemEditor.java 2287 2007-08-13 07:56:33Z tanderson $
 */

package org.openvpms.web.app.customer.charge;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.im.layout.LayoutContext;


/**
 * An editor for {@link Act}s which have an archetype of
 * <em>act.customerAccountInvoiceItem</em>,
 * <em>act.customerAccountCreditItem</em>
 * or <em>act.customerAccountCounterItem</em>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate:2006-02-21 03:48:29Z $
 */
public class DefaultCustomerChargeActItemEditor extends CustomerChargeActItemEditor {

    /**
     * Constructs a <tt>CustomerChargeActItemEditor</tt>.
     * <p/>
     * This recalculates the tax amount.
     *
     * @param act     the act to edit
     * @param parent  the parent act
     * @param context the layout context
     */
    public DefaultCustomerChargeActItemEditor(Act act, Act parent, LayoutContext context) {
        super(act, parent, context);
    }

}
