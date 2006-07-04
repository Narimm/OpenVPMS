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

package org.openvpms.archetype.rules.deposit;

import static org.openvpms.archetype.rules.deposit.DepositRuleException.ErrorCode.MissingDeposit;
import static org.openvpms.archetype.rules.deposit.DepositRuleException.ErrorCode.InvalidDepositArchetype;
import static org.openvpms.archetype.rules.deposit.DepositRuleException.ErrorCode.SavingClearedDeposit;
import static org.openvpms.archetype.rules.deposit.DepositRuleException.ErrorCode.UnclearedDepositExists;

import java.util.List;

import org.openvpms.archetype.rules.till.TillRuleException;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.CollectionNodeConstraint;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.ObjectRefNodeConstraint;
import org.openvpms.component.system.common.query.RelationalOp;

/**
 * Deposit rules.
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */

public class DepositRules {

    /**
     * Rule that determines if an <em>act.depositBalance</em> can be saved.
     * One can be saved if:
     * <ul>
     * <li>it has status 'Cleared' and was previously 'Uncleared'</li>
     * <li>it has status 'Uncleared' and there are no other uncleared
     * act.tillBalances for  the till</li>
     * </ul>
     *
     * @param service the archetype service
     * @param act     the deposit balance act
     * @throws DepositRuleException if the act can't be saved
     */
    public static void checkCanSaveDepositBalance(IArchetypeService service,
                                                 FinancialAct act)
            throws DepositRuleException {
        if (!TypeHelper.isA(act, "act.bankDeposit")) {
            throw new DepositRuleException(InvalidDepositArchetype,
                                        act.getArchetypeId().getShortName());
        }
        
        // Get existing original Act
        ArchetypeQuery existsquery = new ArchetypeQuery("act.bankDeposit", false,true);
        existsquery.setFirstRow(0);
        existsquery.setNumOfRows(ArchetypeQuery.ALL_ROWS);
        existsquery.add(new NodeConstraint("uid", RelationalOp.EQ, act.getUid()));
        List<IMObject> existing = service.get(existsquery).getRows();
        if (!existing.isEmpty()) {
            // If have match then we have an original till balance being saved.
            FinancialAct oldAct = (FinancialAct)existing.get(0);
            // If old till balance cleared can't do
            if ("Cleared".equals(oldAct.getStatus())) {
                throw new DepositRuleException(SavingClearedDeposit, act.getArchetypeId());               
            }
        }
        else {
            // Else we have a completely new till balance so if status is cleared check no other uncleared for Till.                
            if ("Uncleared".equals(act.getStatus())) {
                IMObjectBean balance = new IMObjectBean(act);
                List<IMObject> tills = balance.getValues("depositAccount");
                if (tills.size() != 1) {
                    throw new DepositRuleException(MissingDeposit, act.getArchetypeId());
                }
                Participation participation = (Participation) tills.get(0);
    
                ArchetypeQuery query = new ArchetypeQuery("act.bankDeposit", false,
                                                          true);
                query.setFirstRow(0);
                query.setNumOfRows(ArchetypeQuery.ALL_ROWS);
                query.add(
                        new NodeConstraint("status", RelationalOp.EQ, "Uncleared"));
                CollectionNodeConstraint participations
                        = new CollectionNodeConstraint("depositAccount",
                                                       "participation.deposit",
                                                       false, true);
                participations.add(
                        new ObjectRefNodeConstraint("entity",
                                                    participation.getEntity()));
                query.add(participations);
                List<IMObject> matches = service.get(query).getRows();
                if (!matches.isEmpty()) {
                    IMObject match = matches.get(0);
                    if (match.getUid() != act.getUid()) {
                        Object desc = participation.getEntity();
                        IMObject deposit = ArchetypeQueryHelper.getByObjectReference(
                                service, participation.getEntity());
                        if (deposit != null) {
                            desc = deposit.getName();
                        }
                        throw new DepositRuleException(UnclearedDepositExists, desc);
                    }
                }
            }
        }
    }

}
