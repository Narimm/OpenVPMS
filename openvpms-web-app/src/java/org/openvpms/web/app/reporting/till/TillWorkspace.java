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

package org.openvpms.web.app.reporting.till;

import org.openvpms.archetype.rules.finance.till.TillBalanceStatus;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.app.subsystem.BrowserCRUDWorkspace;
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
import org.openvpms.web.component.mail.MailContext;
import org.openvpms.web.component.subsystem.CRUDWindow;


/**
 * Till workspace.
 *
 * @author Tim Anderson
 */
public class TillWorkspace extends BrowserCRUDWorkspace<Party, FinancialAct> {

    /**
     * The act statuses to query.
     */
    private static final ActStatuses STATUSES = new ActStatuses("act.tillBalance");


    /**
     * Constructs a {@code TillWorkspace}.
     *
     * @param context     the context
     * @param mailContext the mail context
     */
    public TillWorkspace(Context context, MailContext mailContext) {
        super("reporting", "till", context);
        setArchetypes(Party.class, "party.organisationTill");
        setChildArchetypes(FinancialAct.class, "act.tillBalance");
        setMailContext(mailContext);
    }

    /**
     * Sets the current object.
     *
     * @param object the object. May be {@code null}
     */
    @Override
    public void setObject(Party object) {
        super.setObject(object);
        getContext().setTill(object);
    }

    /**
     * Creates a new query to select a till.
     * <p/>
     * This constrains tills to those associated with the current location.
     *
     * @return a new query
     */
    @Override
    protected Query<Party> createSelectQuery() {
        return new TillQuery(getContext().getLocation());
    }

    /**
     * Creates a new CRUD window for viewing and editing acts.
     *
     * @return a new CRUD window
     */
    protected CRUDWindow<FinancialAct> createCRUDWindow() {
        return new TillCRUDWindow(getContext(), getHelpContext());
    }

    /**
     * Creates a new query.
     *
     * @return a new query
     */
    protected ActQuery<FinancialAct> createQuery() {
        ActQuery<FinancialAct> query = new DefaultActQuery<FinancialAct>(
            getObject(), "till", "participation.till",
            getChildArchetypes().getShortNames(), STATUSES);
        query.setStatus(TillBalanceStatus.UNCLEARED);
        return query;
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

    /**
     * Returns the latest version of the current till context object.
     *
     * @return the latest version of the till context object, or {@link #getObject()} if they are the same
     */
    @Override
    protected Party getLatest() {
        return getLatest(getContext().getTill());
    }

}
