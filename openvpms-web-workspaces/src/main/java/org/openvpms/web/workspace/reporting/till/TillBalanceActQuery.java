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

package org.openvpms.web.workspace.reporting.till;

import org.openvpms.archetype.rules.finance.till.TillArchetypes;
import org.openvpms.archetype.rules.finance.till.TillBalanceStatus;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.web.component.im.query.ActStatuses;
import org.openvpms.web.component.im.query.DateRangeActQuery;

/**
 * Query for <em>act.tillBalance</em>. acts.
 *
 * @author Tim Anderson
 */
public class TillBalanceActQuery extends DateRangeActQuery<FinancialAct> {

    /**
     * The short names to query.
     */
    private static final String[] SHORT_NAMES = {TillArchetypes.TILL_BALANCE};

    /**
     * The default statuses to query.
     */
    private static final String[] DEFAULT_STATUSES = {TillBalanceStatus.UNCLEARED, TillBalanceStatus.IN_PROGRESS};

    /**
     * The act statuses. Exclude the <em>IN_PROGRESS</em> status, as it will be handled explicitly whenever
     * <em>UNCLEARED</em> is selected.
     */
    private static final ActStatuses STATUSES = new ActStatuses(TillArchetypes.TILL_BALANCE,
                                                                TillBalanceStatus.IN_PROGRESS);

    /**
     * Constructs a {@link TillBalanceActQuery}.
     *
     * @param till the till to query balance acts for
     */
    public TillBalanceActQuery(Entity till) {
        super(till, "till", TillArchetypes.TILL_PARTICIPATION, SHORT_NAMES, STATUSES, FinancialAct.class);
        setAuto(true);
    }

    /**
     * Returns the act statuses to query.
     * <p/>
     * If the status is <em>UNCLEARED</em>, this also includes <em>IN_PROGRESS</em> acts.
     *
     * @return the act statuses to query
     */
    @Override
    protected String[] getStatuses() {
        String[] statuses = super.getStatuses();
        if (statuses.length == 1 && statuses[0].equals(TillBalanceStatus.UNCLEARED)) {
            statuses = DEFAULT_STATUSES;
        }
        return statuses;
    }
}
