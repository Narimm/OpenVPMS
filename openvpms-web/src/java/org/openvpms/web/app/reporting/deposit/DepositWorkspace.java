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

package org.openvpms.web.app.reporting.deposit;

import org.openvpms.archetype.rules.finance.deposit.DepositStatus;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.app.subsystem.BrowserCRUDWorkspace;
import org.openvpms.web.component.subsystem.CRUDWindow;
import org.openvpms.web.component.im.query.ActQuery;
import org.openvpms.web.component.im.query.ActStatuses;
import org.openvpms.web.component.im.query.DefaultActQuery;


/**
 * Bank Deposit workspace.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-19 07:20:38Z $
 */
public class DepositWorkspace
        extends BrowserCRUDWorkspace<Party, FinancialAct> {

    /**
     * The act statuses to query.
     */
    private static final ActStatuses STATUSES
            = new ActStatuses("act.bankDeposit");


    /**
     * Construct a new <tt>DepositWorkspace</tt>.
     */
    public DepositWorkspace() {
        super("reporting", "deposit");
        setArchetypes(Party.class, "party.organisationDeposit");
        setChildArchetypes(FinancialAct.class, "act.bankDeposit");
    }

    /**
     * Creates a new CRUD window for viewing and editing acts.
     *
     * @return a new CRUD window
     */
    protected CRUDWindow<FinancialAct> createCRUDWindow() {
        return new DepositCRUDWindow(getChildArchetypes());
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

}
