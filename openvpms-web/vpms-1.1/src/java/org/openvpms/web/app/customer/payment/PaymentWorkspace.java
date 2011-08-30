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

package org.openvpms.web.app.customer.payment;

import org.openvpms.archetype.rules.act.FinancialActStatus;
import org.openvpms.archetype.rules.finance.account.CustomerAccountActTypes;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.app.customer.CustomerFinancialActWorkspace;
import org.openvpms.web.app.subsystem.CRUDWindow;
import org.openvpms.web.component.im.query.ActQuery;
import org.openvpms.web.component.im.query.DefaultActQuery;
import org.openvpms.web.component.im.table.IMObjectTableModel;
import org.openvpms.web.component.im.table.act.ActAmountTableModel;
import org.openvpms.web.resource.util.Messages;


/**
 * Payment workspace.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class PaymentWorkspace extends CustomerFinancialActWorkspace {

    /**
     * Payment and refund shortnames supported by the workspace.
     */
    private static final String[] SHORT_NAMES = {
            CustomerAccountActTypes.PAYMENT, CustomerAccountActTypes.REFUND};


    /**
     * Constructs a new <tt>PaymentWorkspace</tt>.
     */
    public PaymentWorkspace() {
        super("customer", "payment");
    }

    /**
     * Creates a new CRUD window for viewing and editing acts.
     *
     * @return a new CRUD window
     */
    protected CRUDWindow<FinancialAct> createCRUDWindow() {
        String type = Messages.get("customer.payment.createtype");
        return new PaymentCRUDWindow(type, SHORT_NAMES);
    }

    /**
     * Creates a new query.
     *
     * @param customer the customer to query acts for
     * @return a new query
     */
    protected ActQuery<FinancialAct> createQuery(Party customer) {
        String[] statuses = {FinancialActStatus.IN_PROGRESS,
                             FinancialActStatus.ON_HOLD};
        return new DefaultActQuery<FinancialAct>(customer, "customer",
                                                 "participation.customer",
                                                 SHORT_NAMES, statuses);
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
        if (FinancialActStatus.POSTED.equals(object.getStatus())) {
            actSelected(null);
        }
    }

    /**
     * Creates a new table model to display acts.
     *
     * @return a new table model.
     */
    protected IMObjectTableModel<FinancialAct> createTableModel() {
        return new ActAmountTableModel<FinancialAct>(true, true);
    }

}
