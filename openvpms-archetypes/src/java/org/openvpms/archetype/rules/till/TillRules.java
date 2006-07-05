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

import static org.openvpms.archetype.rules.till.TillRuleException.ErrorCode.*;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.CollectionNodeConstraint;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.ObjectRefNodeConstraint;
import org.openvpms.component.system.common.query.RelationalOp;

import java.util.List;


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
     * @param act     the till balance act
     * @param service the archetype service
     * @throws TillRuleException         if the act can't be saved
     * @throws ArchetypeServiceException for any archetype service error
     */
    public static void checkCanSaveTillBalance(FinancialAct act,
                                                 IArchetypeService service) {
        if (!TypeHelper.isA(act, "act.tillBalance")) {
            throw new TillRuleException(InvalidTillArchetype,
                                        act.getArchetypeId().getShortName());
        }

        // Get existing original Act
        ArchetypeQuery existsquery = new ArchetypeQuery("act.tillBalance",
                                                        false, true);
        existsquery.setFirstRow(0);
        existsquery.setNumOfRows(ArchetypeQuery.ALL_ROWS);
        existsquery.add(
                new NodeConstraint("uid", RelationalOp.EQ, act.getUid()));
        List<IMObject> existing = service.get(existsquery).getRows();
        if (!existing.isEmpty()) {
            // If have match then we have an original till balance being saved.
            FinancialAct oldAct = (FinancialAct) existing.get(0);
            // If old till balance cleared can't do
            if ("Cleared".equals(oldAct.getStatus())) {
                throw new TillRuleException(SavingClearedTill,
                                            act.getArchetypeId());
            }
        } else {
            // Else we have a completely new till balance so if status is
            // cleared check no other uncleared for Till.
            if ("Uncleared".equals(act.getStatus())) {
                IMObjectReference till = getTill(act);
                if (till == null) {
                    throw new TillRuleException(MissingTill,
                                                act.getArchetypeId());
                }
                FinancialAct current = getUnclearedTillBalance(till, service);
                if (current != null && current.getUid() != act.getUid()) {
                    Object desc = till;
                    IMObject t = ArchetypeQueryHelper.getByObjectReference(
                            service, till);
                    if (t != null) {
                        desc = t.getName();
                    }
                    throw new TillRuleException(UnclearedTillExists, desc);
                }
            }
        }
    }

    /**
     * Adds a <em>act.customerAccountPayment</em> or
     * <em>act.customerAccountRefund</em> to the associated till's uncleared
     * <em>act.tillBalance</em>. If no uncleared till balance exists, one will
     * be created.
     *
     * @param act     the till balance act
     * @param service the archetype service
     * @throws TillRuleException         if the act can't be saved
     * @throws ArchetypeServiceException for any archetype service error
     */
    public static void addToTill(FinancialAct act, IArchetypeService service) {
        if (!TypeHelper.isA(act, "act.customerAccountPayment",
                            "act.customerAccountRefund")) {
            throw new TillRuleException(CantAddActToTill,
                                        act.getArchetypeId().getShortName());
        }
        IMObjectReference till = getTill(act);
        if (till != null) {
            FinancialAct balance = getUnclearedTillBalance(till, service);
            if (balance == null) {
                balance = createTillBalance(till, service);
            } else {
                // determine if the act is present in the till balance
                for (ActRelationship relationship
                        : balance.getSourceActRelationships()) {
                    IMObjectReference target = relationship.getTarget();
                    if (act.getObjectReference().equals(target)) {
                        // act already in till balance, so ignore.
                        return;
                    }
                }
            }
            ActRelationship relationship = (ActRelationship) service.create(
                    "actRelationship.tillBalanceItem");
            relationship.setSource(balance.getObjectReference());
            relationship.setTarget(act.getObjectReference());
            balance.addActRelationship(relationship);
            service.save(balance);
        }
    }

    /**
     * Helper to return the uncleared till balance for a till, if it exists.
     *
     * @param till    a reference to the till
     * @param service the archetype service
     * @return the uncleared till balance, or <code>null</code> if none exists
     */
    public static FinancialAct getUnclearedTillBalance(
            IMObjectReference till, IArchetypeService service) {
        FinancialAct act = null;
        ArchetypeQuery query = new ArchetypeQuery("act.tillBalance",
                                                  false,
                                                  true);
        query.setFirstRow(0);
        query.setNumOfRows(ArchetypeQuery.ALL_ROWS);
        query.add(new NodeConstraint("status", RelationalOp.EQ, "Uncleared"));
        CollectionNodeConstraint participations
                = new CollectionNodeConstraint("till",
                                               "participation.till",
                                               false, true);
        participations.add(new ObjectRefNodeConstraint("entity", till));
        query.add(participations);
        List<IMObject> matches = service.get(query).getRows();
        if (!matches.isEmpty()) {
            act = (FinancialAct) matches.get(0);
        }
        return act;
    }

    /**
     * Helper to create a new till balance.
     *
     * @param till the till
     * @param service the archetype service
     * @return a new till balance
     */
    private static FinancialAct createTillBalance(IMObjectReference till,
                                                  IArchetypeService service) {
        FinancialAct balance = (FinancialAct) service.create("act.tillBalance");
        balance.setStatus("Uncleared");
        Participation p = (Participation) service.create("participation.till");
        p.setAct(balance.getObjectReference());
        p.setEntity(till);
        balance.addParticipation(p);
        return balance;
    }

    /**
     * Helper to get a till from an act.
     *
     * @param act the act
     * @return a reference to the till, or <code>null</code> if no till is found
     */
    private static IMObjectReference getTill(FinancialAct act) {
        IMObjectReference till = null;
        IMObjectBean balance = new IMObjectBean(act);
        List<IMObject> tills = balance.getValues("till");
        if (!tills.isEmpty()) {
            Participation participation = (Participation) tills.get(0);
            till = participation.getEntity();
        }
        return till;
    }
}
