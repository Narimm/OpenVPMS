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

package org.openvpms.web.workspace.customer.account;

import org.openvpms.archetype.rules.act.FinancialActStatus;
import org.openvpms.archetype.rules.prefs.Preferences;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.query.DefaultActQuery;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.workspace.CRUDWindow;
import org.openvpms.web.workspace.customer.CustomerFinancialWorkspace;


/**
 * Customer account workspace.
 *
 * @author Tim Anderson
 */
public class AccountWorkspace extends CustomerFinancialWorkspace<FinancialAct> {

    /**
     * The customer archetype short names.
     */
    private static final String[] CUSTOMER_SHORT_NAMES = {
            "party.customer*", "party.organisationOTC"
    };


    /**
     * Constructs an {@link AccountWorkspace}.
     *
     * @param context     the context
     * @param preferences the user preferences
     */
    public AccountWorkspace(Context context, Preferences preferences) {
        super("customer.account", context, preferences);
        setArchetypes(Party.class, CUSTOMER_SHORT_NAMES);
        setChildArchetypes(FinancialAct.class, "act.customerAccount*");
    }

    /**
     * Creates a new CRUD window for viewing and editing acts.
     *
     * @return a new CRUD window
     */
    protected CRUDWindow<FinancialAct> createCRUDWindow() {
        return new AccountCRUDWindow(getChildArchetypes(), getContext(), getHelpContext());
    }

    /**
     * Creates a new query.
     *
     * @return a new query
     */
    protected Query<FinancialAct> createQuery() {
        String[] shortNames = {"act.customerAccountCharges*",
                               "act.customerAccountPayment",
                               "act.customerAccountRefund",
                               "act.customerAccountClosingBalance",
                               "act.customerAccountOpeningBalance",
                               "act.customerAccountDebitAdjust",
                               "act.customerAccountCreditAdjust",
                               "act.customerAccountInitialBalance",
                               "act.customerAccountBadDebt"};
        String[] statuses = {FinancialActStatus.POSTED};

        Party customer = getObject();
        return new DefaultActQuery<>(customer, "customer", "participation.customer", shortNames, statuses);
    }

}
