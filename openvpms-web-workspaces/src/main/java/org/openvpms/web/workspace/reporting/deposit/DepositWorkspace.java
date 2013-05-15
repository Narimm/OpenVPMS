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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.reporting.deposit;

import org.openvpms.archetype.rules.finance.deposit.DepositStatus;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.query.ActQuery;
import org.openvpms.web.component.im.query.ActStatuses;
import org.openvpms.web.component.im.query.DefaultActQuery;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.mail.MailContext;
import org.openvpms.web.component.workspace.BrowserCRUDWorkspace;
import org.openvpms.web.component.workspace.CRUDWindow;


/**
 * Bank Deposit workspace.
 *
 * @author Tim Anderson
 */
public class DepositWorkspace extends BrowserCRUDWorkspace<Party, FinancialAct> {

    /**
     * The act statuses to query.
     */
    private static final ActStatuses STATUSES = new ActStatuses("act.bankDeposit");


    /**
     * Constructs a {@code DepositWorkspace}.
     *
     * @param context     the context
     * @param mailContext the mail context
     */
    public DepositWorkspace(Context context, MailContext mailContext) {
        super("reporting", "deposit", context);
        setArchetypes(Party.class, "party.organisationDeposit");
        setChildArchetypes(FinancialAct.class, "act.bankDeposit");
        setMailContext(mailContext);
    }

    /**
     * Creates a new CRUD window for viewing and editing acts.
     *
     * @return a new CRUD window
     */
    protected CRUDWindow<FinancialAct> createCRUDWindow() {
        return new DepositCRUDWindow(getChildArchetypes(), getContext(), getHelpContext());
    }

    /**
     * Creates a new query to select a deposit account.
     * <p/>
     * This constrains accounts to those associated with the current location.
     *
     * @return a new query
     */
    @Override
    protected Query<Party> createSelectQuery() {
        return new DepositQuery(getContext().getLocation());
    }

    /**
     * Creates a new query.
     *
     * @return a new query
     */
    protected ActQuery<FinancialAct> createQuery() {
        ActQuery<FinancialAct> query = new DefaultActQuery<FinancialAct>(
            getObject(), "depositAccount", "participation.deposit",
            "act.bankDeposit", STATUSES);
        query.setStatus(DepositStatus.UNDEPOSITED);
        return query;
    }

    /**
     * Returns the latest version of the current deposit context object.
     *
     * @return the latest version of the deposit context object, or {@link #getObject()} if they are the same
     */
    @Override
    protected Party getLatest() {
        return getLatest(getContext().getDeposit());
    }

}
