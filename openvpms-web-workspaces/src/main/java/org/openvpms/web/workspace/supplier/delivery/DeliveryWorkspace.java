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
 */

package org.openvpms.web.workspace.supplier.delivery;

import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.workspace.CRUDWindow;
import org.openvpms.web.workspace.subsystem.BrowserCRUDWorkspace;
import org.openvpms.web.workspace.supplier.SupplierMailContext;


/**
 * Supplier delivery workspace.
 *
 * @author Tim Anderson
 */
public class DeliveryWorkspace
    extends BrowserCRUDWorkspace<FinancialAct, FinancialAct> {

    /**
     * Constructs a {@code DeliveryWorkspace}.
     *
     * @param context the context
     */
    public DeliveryWorkspace(Context context) {
        super("supplier", "delivery", context, false);
        setArchetypes(FinancialAct.class, "act.supplierDelivery", "act.supplierReturn");
        setChildArchetypes(getArchetypes());
        setMailContext(new SupplierMailContext(context, getHelpContext()));
    }

    /**
     * Creates a new CRUD window for viewing and editing acts.
     *
     * @return a new CRUD window
     */
    protected CRUDWindow<FinancialAct> createCRUDWindow() {
        return new DeliveryCRUDWindow(getArchetypes(), getContext(), getHelpContext());
    }

    /**
     * Determines if the parent object is optional (i.e may be {@code null},
     * when laying out the workspace.
     *
     * @return {@code true}
     */
    @Override
    protected boolean isParentOptional() {
        return true;
    }

    /**
     * Creates a new query to populate the browser.
     *
     * @return a new query
     */
    @Override
    protected Query<FinancialAct> createQuery() {
        Archetypes shortNames = getChildArchetypes();
        return new DeliveryQuery(shortNames.getShortNames(), new DefaultLayoutContext(getContext(), getHelpContext()));
    }
}
