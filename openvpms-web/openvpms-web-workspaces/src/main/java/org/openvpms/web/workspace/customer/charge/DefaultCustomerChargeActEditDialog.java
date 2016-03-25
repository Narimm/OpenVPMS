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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.customer.charge;

import org.openvpms.web.component.app.Context;
import org.openvpms.web.workspace.customer.order.OrderCharger;


/**
 * An edit dialog for {@link CustomerChargeActEditor} editors.
 * <p/>
 * This performs printing of unprinted documents that have their <em>interactive</em> flag set to {@code true}
 * when <em>Apply</em> or <em>OK</em> is pressed.
 *
 * @author Tim Anderson
 */
public class DefaultCustomerChargeActEditDialog extends CustomerChargeActEditDialog {

    /**
     * Constructs a {@link DefaultCustomerChargeActEditDialog}.
     *
     * @param editor  the editor
     * @param context the context
     */
    public DefaultCustomerChargeActEditDialog(CustomerChargeActEditor editor, Context context) {
        this(editor, null, context, true);
    }

    /**
     * Constructs a {@link DefaultCustomerChargeActEditDialog}.
     *
     * @param editor           the editor
     * @param charger          the order charger. May be {@code null}
     * @param context          the context
     * @param autoChargeOrders if {@code true}, automatically charge customer orders if they are complete
     */
    public DefaultCustomerChargeActEditDialog(CustomerChargeActEditor editor, OrderCharger charger, Context context,
                                              boolean autoChargeOrders) {
        super(editor, charger, context, autoChargeOrders);
    }

}