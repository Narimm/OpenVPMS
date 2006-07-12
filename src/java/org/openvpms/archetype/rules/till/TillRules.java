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

import org.openvpms.archetype.rules.deposit.DepositHelper;
import static org.openvpms.archetype.rules.till.TillRuleException.ErrorCode.*;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.math.BigDecimal;
import java.util.Date;


/**
 * Till business rules.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class TillRules {

    /**
     * Till balance act short name.
     */
    public static final String TILL_BALANCE = "act.tillBalance";

    /**
     * Till participation short name.
     */
    public static final String TILL_PARTICIPATION = "participation.till";

    /**
     * Cleared act status.
     */
    public static final String CLEARED = "Cleared";

    /**
     * Uncleared act status.
     */
    public static final String UNCLEARED = "Uncleared";


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
    public static void checkCanSaveTillBalance(Act act,
                                               IArchetypeService service) {
        ActBean bean = new ActBean(act, service);
        if (!bean.isA(TILL_BALANCE)) {
            throw new TillRuleException(InvalidTillArchetype,
                                        act.getArchetypeId().getShortName());
        }

        Act oldAct = (Act) ArchetypeQueryHelper.getByUid(
                service, act.getArchetypeId(), act.getUid());
        if (oldAct != null) {
            // If the act already exists, make sure it hasn't been cleared
            if (CLEARED.equals(oldAct.getStatus())) {
                throw new TillRuleException(ClearedTill, act.getUid());
            }
        } else {
            // Else we have a completely new till balance so if status is
            // cleared check no other uncleared for Till.
            if (UNCLEARED.equals(bean.getStatus())) {
                Entity till = bean.getParticipant(TILL_PARTICIPATION);
                if (till == null) {
                    throw new TillRuleException(MissingTill,
                                                act.getArchetypeId());
                }
                Act current = TillHelper.getUnclearedTillBalance(
                        till.getObjectReference());
                if (current != null && current.getUid() != act.getUid()) {
                    throw new TillRuleException(UnclearedTillExists,
                                                till.getName());
                }
            }
        }
    }

    /**
     * Adds a <em>act.customerAccountPayment</em> or
     * <em>act.customerAccountRefund</em> to the associated till's uncleared
     * <em>act.tillBalance</em>, if the act's status is 'Posted'.
     * If no uncleared till balance exists, one will be created.
     *
     * @param act     the till balance act
     * @param service the archetype service
     * @throws TillRuleException         if the act is invalid or the till is
     *                                   missing
     * @throws ArchetypeServiceException for any archetype service error
     */
    public static void addToTill(Act act, IArchetypeService service) {
        ActBean bean = new ActBean(act, service);
        if (!bean.isA("act.customerAccountPayment",
                      "act.customerAccountRefund")) {
            throw new TillRuleException(CantAddActToTill,
                                        act.getArchetypeId().getShortName());
        }
        if (!"Posted".equals(bean.getStatus())) {
            return;
        }
        IMObjectReference till = bean.getParticipantRef(TILL_PARTICIPATION);
        if (till == null) {
            throw new TillRuleException(MissingTill, act.getArchetypeId());
        }
        Act balance = TillHelper.getUnclearedTillBalance(till);
        if (balance == null) {
            balance = TillHelper.createTillBalance(till);
        }
        ActBean balanceBean = new ActBean(balance, service);
        if (balanceBean.getRelationship(act) == null) {
            balanceBean.addRelationship("actRelationship.tillBalanceItem", act);
            balanceBean.save();
        }
    }

    /**
     * Clears a till.
     *
     * @param balance the current till balance
     * @param amount  the amount to clear from the till
     * @param account the account to deposit to
     * @param service the archetype service
     * @throws TillRuleException if the balance doesn't have a till
     */
    public static void clearTill(Act balance, BigDecimal amount, Party account,
                                 IArchetypeService service) {
        ActBean actBean = new ActBean(balance);
        Entity till = actBean.getParticipant(TILL_PARTICIPATION);
        if (till == null) {
            throw new TillRuleException(MissingTill, balance.getUid());
        }
        IMObjectBean bean = new IMObjectBean(till);
        BigDecimal current = bean.getBigDecimal("tillFloat", BigDecimal.ZERO);
        BigDecimal diff = amount.subtract(current);
        balance.setStatus(CLEARED);
        Act adjustment = TillHelper.createTillBalanceAdjustment(
                till.getObjectReference(), new Money(diff));
        Act deposit = DepositHelper.getUndepositedDeposit(account);
        if (deposit == null) {
            deposit = DepositHelper.createBankDeposit(balance, account);
        }

/*
        todo - commented out as workaround for OBF-114
        List<Act> acts = new ArrayList<Act>();
        acts.add(adjustment);
        acts.add(balance);
        acts.add(deposit);
        service.save(acts);
*/
        service.save(adjustment);
        service.save(balance);
        service.save(deposit);

        // need to reload the till to avoid hibernate StaleObjectException
        // caused by saving the balance (which has a reference to the till)
        String tillName = till.getName();
        till = (Party) ArchetypeQueryHelper.getByObjectReference(
                service, till.getObjectReference());
        bean = new IMObjectBean(till);
        if (till == null) {
            throw new TillRuleException(TillNotFound, tillName);
        }
        bean.setValue("lastCleared", new Date());
        bean.setValue("tillFloat", diff);
        bean.save();
    }

    /**
     * Transfers an act from one till to another.
     *
     * @param balance the till balance to transfer from
     * @param act     the act to transfer
     * @param till    the till to transfer to
     * @param service the archetype service
     */
    public static void transfer(Act balance, Act act, Party till,
                                IArchetypeService service) {
        ActBean balanceBean = new ActBean(balance);
        if (!balanceBean.isA(TILL_BALANCE)) {
            throw new TillRuleException(
                    InvalidTillArchetype,
                    balance.getArchetypeId().getShortName());
        }
        ActBean actBean = new ActBean(act);
        if (!actBean.isA("act.customerAccountPayment",
                         "act.customerAccountRefund")) {
            throw new TillRuleException(CantAddActToTill,
                                        act.getArchetypeId().getShortName());
        }
        Entity orig = balanceBean.getParticipant(TILL_PARTICIPATION);
        if (orig == null) {
            throw new TillRuleException(MissingTill, balance.getUid());
        }
        if (orig.equals(till)) {
            throw new TillRuleException(InvalidTransferTill, till.getName());
        }
        if (!UNCLEARED.equals(balance.getStatus())) {
            throw new TillRuleException(ClearedTill, balance.getUid());
        }
        ActRelationship relationship = balanceBean.getRelationship(act);
        if (relationship == null) {
            throw new TillRuleException(MissingRelationship, balance.getUid());
        }
//        balanceBean.removeRelationship(relationship);

        if (actBean.getParticipant(TILL_PARTICIPATION) == null) {
            throw new TillRuleException(MissingTill, act.getUid());
        }

/*
        todo - commented out as workaround for OBF-114
        List<Act> acts = new ArrayList<Act>();
        acts.add(balance);
        acts.add(act);
        service.save(acts);
*/
        // balanceBean.save();
        actBean.setParticipant(TILL_PARTICIPATION, till);
        actBean.removeRelationship(relationship);
        actBean.save();

        // need to reload the act to avoid hibernate StaleStateException
        // due to propagation of removal of relationship
/*
        act = (Act) ArchetypeQueryHelper.getByObjectReference(
                service, act.getObjectReference());
        actBean = new ActBean(act);
        actBean.setParticipant(TILL_PARTICIPATION, till);
        actBean.save();
*/

        // @todo should save all modifications in the one transaction
        addToTill(act, service);
    }

}
