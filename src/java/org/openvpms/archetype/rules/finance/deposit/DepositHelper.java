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

package org.openvpms.archetype.rules.finance.deposit;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.CollectionNodeConstraint;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.ObjectRefNodeConstraint;
import org.openvpms.component.system.common.query.RelationalOp;

import java.util.List;


/**
 * Bank deposit helper.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DepositHelper {

    /**
     * Returns the undeposited bank deposit for an account, if it exists.
     *
     * @param account the account
     * @return an <em>act.bankDeposit</code> or <code>null</code> if none exists
     */
    public static FinancialAct getUndepositedDeposit(Entity account) {
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        ArchetypeQuery query = new ArchetypeQuery(DepositArchetypes.BANK_DEPOSIT,
                                                  false,
                                                  true);
        query.setFirstResult(0);
        query.setMaxResults(ArchetypeQuery.ALL_RESULTS);
        query.add(new NodeConstraint("status", RelationalOp.EQ,
                                     DepositStatus.UNDEPOSITED));
        CollectionNodeConstraint participations
                = new CollectionNodeConstraint(
                "depositAccount", DepositArchetypes.DEPOSIT_PARTICIPATION,
                false, true);
        participations.add(new ObjectRefNodeConstraint(
                "entity", account.getObjectReference()));
        query.add(participations);
        List<IMObject> matches = service.get(query).getResults();
        return (!matches.isEmpty()) ? (FinancialAct) matches.get(0) : null;
    }

    /**
     * Creates a new bank deposit.
     *
     * @param account the account to deposit to
     * @return a new bank deposit
     */
    public static Act createBankDeposit(Entity account) {
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        Act act = (Act) service.create(DepositArchetypes.BANK_DEPOSIT);
        ActBean bean = new ActBean(act);
        bean.addParticipation(DepositArchetypes.DEPOSIT_PARTICIPATION, account);
        return act;
    }
}
