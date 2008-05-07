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

package org.openvpms.web.app.supplier.delivery;

import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.web.app.subsystem.BrowserCRUDWorkspace;
import org.openvpms.web.app.subsystem.CRUDWindow;


/**
 * Supplier delivery workspace.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2008-03-19 10:19:58 +1100 (Wed, 19 Mar 2008) $
 */
public class DeliveryWorkspace
        extends BrowserCRUDWorkspace<FinancialAct, FinancialAct> {

    /**
     * Constructs a new <tt>DeliveryWorkspace</tt>.
     */
    public DeliveryWorkspace() {
        super("supplier", "delivery", false);
        setArchetypes(FinancialAct.class, "act.supplierDelivery",
                      "act.supplierReturn");
        setChildArchetypes(getArchetypes());
    }

    /**
     * Creates a new CRUD window for viewing and editing acts.
     *
     * @return a new CRUD window
     */
    protected CRUDWindow<FinancialAct> createCRUDWindow() {
        return new DeliveryCRUDWindow(getArchetypes());
    }

    /**
     * Determines if the parent object is optional (i.e may be <tt>null</tt>,
     * when laying out the workspace.
     *
     * @return <tt>true</tt>
     */
    @Override
    protected boolean isParentOptional() {
        return true;
    }

}
