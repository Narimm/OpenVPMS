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

package org.openvpms.web.workspace.supplier;

import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.workspace.AbstractWorkspaces;
import org.openvpms.web.workspace.supplier.charge.ChargeWorkspace;
import org.openvpms.web.workspace.supplier.delivery.DeliveryWorkspace;
import org.openvpms.web.workspace.supplier.document.SupplierDocumentWorkspace;
import org.openvpms.web.workspace.supplier.order.OrderWorkspace;


/**
 * Supplier workspaces.
 * `
 *
 * @author Tim Anderson
 */
public class SupplierWorkspaces extends AbstractWorkspaces {

    /**
     * Constructs a {@code SupplierWorkspaces|.
     *
     * @param context the context
     */
    public SupplierWorkspaces(Context context) {
        super("supplier");
        addWorkspace(new InformationWorkspace(context));
        addWorkspace(new SupplierDocumentWorkspace(context));
        addWorkspace(new OrderWorkspace(context));
        addWorkspace(new DeliveryWorkspace(context));
        addWorkspace(new ChargeWorkspace(context));
        addWorkspace(new PaymentWorkspace(context));
        addWorkspace(new AccountWorkspace(context));
    }

}
