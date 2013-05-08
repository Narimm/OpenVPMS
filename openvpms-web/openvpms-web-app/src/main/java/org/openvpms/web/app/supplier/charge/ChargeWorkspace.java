/*
 * Version: 1.0
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
 *  Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.app.supplier.charge;

import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.web.app.supplier.SupplierActWorkspace;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.query.ActQuery;
import org.openvpms.web.component.im.query.ActStatuses;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserFactory;
import org.openvpms.web.component.im.query.DefaultActQuery;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.table.IMObjectTableModel;
import org.openvpms.web.component.im.table.act.ActAmountTableModel;
import org.openvpms.web.component.subsystem.CRUDWindow;

import static org.openvpms.archetype.rules.act.ActStatus.POSTED;


/**
 * Supplier invoice workspace.
 *
 * @author Tim Anderson
 */
public class ChargeWorkspace extends SupplierActWorkspace<FinancialAct> {

    /**
     * The act statuses to query, excluding POSTED.
     */
    private static final ActStatuses STATUSES = new ActStatuses("act.supplierAccountChargesInvoice", POSTED);


    /**
     * Constructs a {@code ChargeWorkspace}.
     *
     * @param context the context
     */
    public ChargeWorkspace(Context context) {
        super("supplier", "charge", context);
        setChildArchetypes(FinancialAct.class, "act.supplierAccountCharges*");
    }

    /**
     * Creates a new CRUD window for viewing and editing acts.
     *
     * @return a new CRUD window
     */
    protected CRUDWindow<FinancialAct> createCRUDWindow() {
        return new ChargeCRUDWindow(getChildArchetypes(), getContext(), getHelpContext());
    }

    /**
     * Creates a new query.
     *
     * @return a new query
     */
    protected ActQuery<FinancialAct> createQuery() {
        return new DefaultActQuery<FinancialAct>(
            getObject(), "supplier", "participation.supplier",
            getChildArchetypes().getShortNames(), STATUSES);
    }

    /**
     * Invoked when the object has been saved.
     *
     * @param object the object
     * @param isNew  determines if the object is a new instance
     */
    @Override
    protected void onSaved(FinancialAct object, boolean isNew) {
        super.onSaved(object, isNew);
        if (POSTED.equals(object.getStatus())) {
            onBrowserSelected(null);
        }
    }

    /**
     * Creates a new browser to query and display acts.
     *
     * @param query the query
     * @return a new browser
     */
    @Override
    protected Browser<FinancialAct> createBrowser(Query<FinancialAct> query) {
        IMObjectTableModel<FinancialAct> model = new ActAmountTableModel<FinancialAct>(true, true);
        return BrowserFactory.create(query, null, model, new DefaultLayoutContext(getContext(), getHelpContext()));
    }

}
