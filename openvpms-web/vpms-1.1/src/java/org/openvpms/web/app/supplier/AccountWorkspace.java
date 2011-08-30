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
 *
 *  $Id$
 */

package org.openvpms.web.app.supplier;

import org.openvpms.archetype.rules.act.FinancialActStatus;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.app.subsystem.CRUDWindow;
import org.openvpms.web.component.im.query.ActQuery;
import org.openvpms.web.component.im.query.DefaultActQuery;
import org.openvpms.web.component.im.table.IMObjectTableModel;
import org.openvpms.web.component.im.table.act.ActAmountTableModel;
import org.openvpms.web.resource.util.Messages;


/**
 * Supplier account workspace.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class AccountWorkspace extends SupplierFinancialActWorkspace {

    /**
     * Constructs a new <tt>AccountWorkspace</tt>.
     */
    public AccountWorkspace() {
        super("supplier", "account");
    }

    /**
     * Creates a new CRUD window for viewing and editing acts.
     *
     * @return a new CRUD window
     */
    protected CRUDWindow<FinancialAct> createCRUDWindow() {
        String type = Messages.get("supplier.account.createtype");
        return new AccountCRUDWindow(type, "act.supplierAccount*");
    }

    /**
     * Creates a new query.
     *
     * @param party the party to query acts for
     * @return a new query
     */
    protected ActQuery<FinancialAct> createQuery(Party party) {
        String[] shortNames = {"act.supplierAccountCharges*",
                               "act.supplierAccountPayment",
                               "act.supplierAccountRefund"};
        String[] statuses = {FinancialActStatus.POSTED};
        return new DefaultActQuery<FinancialAct>(party, "supplier",
                                                 "participation.supplier",
                                                 shortNames, statuses);
    }

    /**
     * Creates a new table model to display acts.
     *
     * @return a new table model.
     */
    @Override
    protected IMObjectTableModel<FinancialAct> createTableModel() {
        return new ActAmountTableModel<FinancialAct>(false, true);
    }

}
