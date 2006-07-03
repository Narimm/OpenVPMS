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

package org.openvpms.archetype.rules.till;

import static org.openvpms.archetype.rules.till.TillRuleException.ErrorCode.InvalidTillArchetype;
import static org.openvpms.archetype.rules.till.TillRuleException.ErrorCode.MissingTill;
import static org.openvpms.archetype.rules.till.TillRuleException.ErrorCode.UnclearedTillExists;
import static org.openvpms.archetype.rules.till.TillRuleException.ErrorCode.SavingClearedTill;

import java.util.List;

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
 * Till rules.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class TillRules {

    /**
     * Rule that determines if an <em>act.tillBalance</em> can be saved.
     * One can be saved if:
     * <ul>
     * <li>it has status 'Cleared' and was previously 'Uncleared'</li>
     * <li>it has status 'Uncleared' and there are no other uncleared
     * act.tillBalances for  the till</li>
     * </ul>
     *
     * @param service the archetype service
     * @param act     the till balance act
     * @throws TillRuleException if the act can't be saved
     */
    public static void checkUnclearedTillBalance(IArchetypeService service,
                                                 FinancialAct act)
            throws TillRuleException {
        if (!TypeHelper.isA(act, "act.tillBalance")) {
            throw new TillRuleException(InvalidTillArchetype,
                                        act.getArchetypeId().getShortName());
        }
        
        // Get existing original Act
        ArchetypeQuery existsquery = new ArchetypeQuery("act.tillBalance", false,true);
        existsquery.setFirstRow(0);
        existsquery.setNumOfRows(ArchetypeQuery.ALL_ROWS);
        existsquery.add(new NodeConstraint("uid", RelationalOp.EQ, act.getUid()));
        List<IMObject> existing = service.get(existsquery).getRows();
        if (!existing.isEmpty()) {
            // If have match then we have an original till balance being saved.
            FinancialAct oldAct = (FinancialAct)existing.get(0);
            // If old till balance cleared can't do
            if ("Cleared".equals(oldAct.getStatus())) {
                throw new TillRuleException(SavingClearedTill, act.getArchetypeId());               
            }
        }
        else {
            // Else we have a completely new till balance so if status is cleared check no other uncleared for Till.                
            if ("Uncleared".equals(act.getStatus())) {
                IMObjectBean balance = new IMObjectBean(act);
                List<IMObject> tills = balance.getValues("till");
                if (tills.size() != 1) {
                    throw new TillRuleException(MissingTill, act.getArchetypeId());
                }
                Participation participation = (Participation) tills.get(0);
    
                ArchetypeQuery query = new ArchetypeQuery("act.tillBalance", false,
                                                          true);
                query.setFirstRow(0);
                query.setNumOfRows(ArchetypeQuery.ALL_ROWS);
                query.add(
                        new NodeConstraint("status", RelationalOp.EQ, "Uncleared"));
                CollectionNodeConstraint participations
                        = new CollectionNodeConstraint("till",
                                                       "participation.till",
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
                        IMObject till = ArchetypeQueryHelper.getByObjectReference(
                                service, participation.getEntity());
                        if (till != null) {
                            desc = till.getName();
                        }
                        throw new TillRuleException(UnclearedTillExists, desc);
                    }
                }
            }
        }
    }
}
