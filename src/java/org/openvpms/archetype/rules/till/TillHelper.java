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

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.CollectionNodeConstraint;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.ObjectRefNodeConstraint;
import org.openvpms.component.system.common.query.RelationalOp;

import java.util.List;


/**
 * Till helper.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class TillHelper {

    /**
     * Helper to get a till reference from an act.
     *
     * @param act the act
     * @return a reference to the till, or <code>null</code> if no till is found
     */
    public static IMObjectReference getTillReference(FinancialAct act) {
        Participation participation = getTillParticipation(act);
        if (participation != null) {
            return participation.getEntity();
        }
        return null;
    }

    /**
     * Helper to get a till from an act.
     *
     * @param act the act
     * @return a reference to the till, or <code>null</code> if no till is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public static Party getTill(FinancialAct act) {
        Party till = null;
        IMObjectReference ref = getTillReference(act);
        if (ref != null) {
            IArchetypeService service
                    = ArchetypeServiceHelper.getArchetypeService();
            till = (Party) ArchetypeQueryHelper.getByObjectReference(service,
                                                                     ref);
        }
        return till;
    }

    /**
     * Helper to get a till participation from an act.
     *
     * @param act the act
     * @return a reference to the participation, or <code>null</code> if none
     *         is found
     */
    public static Participation getTillParticipation(FinancialAct act) {
        IMObjectBean balance = new IMObjectBean(act);
        List<IMObject> tills = balance.getValues("till");
        if (!tills.isEmpty()) {
            return (Participation) tills.get(0);
        }
        return null;
    }

    /**
     * Helper to return the uncleared till balance for a till, if it exists.
     *
     * @param till a reference to the till
     * @return the uncleared till balance, or <code>null</code> if none exists
     */
    public static FinancialAct getUnclearedTillBalance(IMObjectReference till) {
        FinancialAct act = null;
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
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
     * Helper to create a new till balance, associating it with a till.
     *
     * @param till the till
     * @return a new till balance
     */
    public static FinancialAct createTillBalance(IMObjectReference till) {
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        FinancialAct balance = (FinancialAct) service.create("act.tillBalance");
        balance.setStatus("Uncleared");
        addTill(balance, till);
        return balance;
    }


    /**
     * Creates a new till balance adjustment, associating it with a till.
     *
     * @param till   the till
     * @param amount the amount
     * @return a new till balance adjustment
     */
    public static FinancialAct createTillBalanceAdjustment(
            IMObjectReference till, Money amount) {
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        FinancialAct act = (FinancialAct) service.create(
                "act.tillBalanceAdjustment");
        act.setTotal(amount);
        addTill(act, till);
        return act;
    }

    /**
     * Creates a new bank deposit, associating it with a till balance.
     *
     * @param balance the till balance
     * @param account the account to deposit to
     * @return a new bank deposit
     */
    public static FinancialAct createBankDeposit(FinancialAct balance,
                                                 IMObjectReference account) {
        IArchetypeService service = ArchetypeServiceHelper.getArchetypeService();
        FinancialAct act = (FinancialAct) service.create("act.bankDeposit");
        addParticipation("participation.deposit", act, account);
        addRelationship("actRelationship.bankDepositItem", act, balance);
        return act;
    }

    /**
     * Associates a till with an act.
     *
     * @param act  the act
     * @param till the till
     */
    public static void addTill(FinancialAct act, IMObjectReference till) {
        addParticipation("participation.till", act, till);
    }

    /**
     * Adds a participation to an act.
     *
     * @param shortName the participation short name
     * @param act       the act
     * @param entity    the participation entity
     */
    public static Participation addParticipation(String shortName, Act act,
                                                 IMObjectReference entity) {
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        Participation p = (Participation) service.create(shortName);
        p.setAct(act.getObjectReference());
        p.setEntity(entity);
        act.addParticipation(p);
        return p;
    }

    /**
     * Adds an act relationship.
     *
     * @param shortName the relationship short name
     * @param source    the source act
     * @param target    the target act
     */
    public static void addRelationship(String shortName, FinancialAct source,
                                       FinancialAct target) {
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        ActRelationship relationship
                = (ActRelationship) service.create(shortName);
        relationship.setSource(source.getObjectReference());
        relationship.setTarget(target.getObjectReference());
        source.addActRelationship(relationship);
    }

    /**
     * Returns the first act relationship found between two acts.
     *
     * @param source the source act
     * @param target the target act
     * @return the act relationship or <code>null</code> if none is found
     */
    public static ActRelationship getRelationship(Act source, Act target) {
        IMObjectReference ref = target.getObjectReference();
        for (ActRelationship relationship
                : source.getSourceActRelationships()) {
            if (ref.equals(relationship.getTarget())) {
                return relationship;
            }
        }
        return null;
    }

}
