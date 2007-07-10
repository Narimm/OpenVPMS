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

package org.openvpms.web.app.financial.till;

import nextapp.echo2.app.Component;
import org.openvpms.archetype.rules.finance.till.TillBalanceStatus;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.app.subsystem.ActWorkspace;
import org.openvpms.web.app.subsystem.CRUDWindow;
import org.openvpms.web.app.subsystem.ShortNameList;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.im.query.ActQuery;
import org.openvpms.web.component.im.query.DefaultActQuery;
import org.openvpms.web.component.im.table.IMObjectTableModel;
import org.openvpms.web.component.im.table.act.ActAmountTableModel;
import org.openvpms.web.component.im.util.FastLookupHelper;
import org.openvpms.web.resource.util.Messages;

import java.util.List;


/**
 * Till workspace.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-19 07:20:38Z $
 */
public class TillWorkspace extends ActWorkspace<Party, FinancialAct> {

    /**
     * Constructs a new <tt>TillWorkspace</tt>.
     */
    public TillWorkspace() {
        super("financial", "till", new ShortNameList("party.organisationTill"));
    }

    /**
     * Sets the current object.
     *
     * @param object the object. May be <tt>null</tt>
     */
    @Override
    public void setObject(Party object) {
        super.setObject(object);
        GlobalContext.getInstance().setTill(object);
        layoutWorkspace(object);
        initQuery(object);
    }

    /**
     * Sets the current object.
     * This is analagous to  {@link #setObject} but performs a safe cast
     * to the required type.
     *
     * @param object the current object. May be <code>null</code>
     */
    public void setIMObject(IMObject object) {
        if (object == null || object instanceof Party) {
            setObject((Party) object);
        } else {
            throw new IllegalArgumentException(
                    "Argument 'object' must be an instance of "
                            + Party.class.getName());
        }
    }

    /**
     * Creates a new CRUD window for viewing and editing acts.
     *
     * @return a new CRUD window
     */
    protected CRUDWindow<FinancialAct> createCRUDWindow() {
        String type = Messages.get("financial.till.createtype");
        return new TillCRUDWindow(type, "act.tillBalanceAdjustment");
    }

    /**
     * Creates a new query.
     *
     * @param till the till to query acts for
     * @return a new query
     */
    protected ActQuery<FinancialAct> createQuery(Party till) {
        List<Lookup> lookups = FastLookupHelper.getLookups("act.tillBalance",
                                                           "status");
        ActQuery<FinancialAct> query = new DefaultActQuery<FinancialAct>(
                till, "till", "participation.till", "act.tillBalance", lookups);
        query.setStatus(TillBalanceStatus.UNCLEARED);
        return query;
    }

    /**
     * Lays out the component.
     *
     * @param container the container
     */
    protected void doLayout(Component container) {
        Party latest = getLatest();
        if (latest != getObject()) {
            setObject(latest);
        } else {
            // need to add the existing workspace to the container
            Component workspace = getWorkspace();
            if (workspace != null) {
                container.add(workspace);
            }
        }
    }

    /**
     * Returns the latest version of the current till context object.
     *
     * @return the latest version of the till context object, or
     *         {@link #getObject()} if they are the same
     */
    @Override
    protected Party getLatest() {
        return getLatest(GlobalContext.getInstance().getTill());
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
