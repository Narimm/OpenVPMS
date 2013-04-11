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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.app.supplier;

import org.openvpms.archetype.rules.act.FinancialActStatus;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.query.ActQuery;
import org.openvpms.web.component.im.query.DefaultActQuery;
import org.openvpms.web.component.subsystem.CRUDWindow;


/**
 * Supplier account workspace.
 *
 * @author Tim Anderson
 */
public class AccountWorkspace extends SupplierActWorkspace<FinancialAct> {

    /**
     * Constructs an {@code AccountWorkspace}.
     *
     * @param context the context
     */
    public AccountWorkspace(Context context) {
        super("supplier", "account", context);
        setChildArchetypes(FinancialAct.class, "act.supplierAccount*");
    }

    /**
     * Creates a new CRUD window for viewing and editing acts.
     *
     * @return a new CRUD window
     */
    protected CRUDWindow<FinancialAct> createCRUDWindow() {
        return new AccountCRUDWindow(getChildArchetypes(), getHelpContext());
    }

    /**
     * Creates a new query.
     *
     * @return a new query
     */
    protected ActQuery<FinancialAct> createQuery() {
        String[] shortNames = {"act.supplierAccountCharges*",
                "act.supplierAccountPayment",
                "act.supplierAccountRefund"};
        String[] statuses = {FinancialActStatus.POSTED};
        return new DefaultActQuery<FinancialAct>(getObject(), "supplier",
                                                 "participation.supplier",
                                                 shortNames, statuses);
    }

}
