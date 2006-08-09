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

import org.openvpms.archetype.rules.act.ActCalculator;
import org.openvpms.archetype.rules.deposit.DepositHelper;
import static org.openvpms.archetype.rules.till.TillRuleException.ErrorCode.*;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


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
     * Customer account payment act short name.
     */
    private static final String ACCOUNT_PAYMENT = "act.customerAccountPayment";

    /**
     * Customer account refund act short name.
     */
    private static final String ACCOUNT_REFUND = "act.customerAccountRefund";


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
     * Adds a <em>act.customerAccountPayment</em>,
     * <em>act.customerAccountRefund</em>, or <em>act.tillBalanceAdjustment</em>
     * to the associated till's uncleared <em>act.tillBalance</em>.
     * For <em>act.customerAccount*</em> acts, this only occurs if the act's
     * status is 'Posted'. If no uncleared till balance exists, one will be
     * created.
     *
     * @param act     the till balance act
     * @param service the archetype service
     * @throws TillRuleException         if the act is invalid or the till is
     *                                   missing
     * @throws ArchetypeServiceException for any archetype service error
     */
    public static void addToTill(Act act, IArchetypeService service) {
        ActBean bean = new ActBean(act, service);
        boolean isAccount = bean.isA(ACCOUNT_PAYMENT, ACCOUNT_REFUND);
        boolean isAdjust = bean.isA("act.tillBalanceAdjustment");
        if (!isAccount && !isAdjust) {
            throw new TillRuleException(CantAddActToTill,
                                        act.getArchetypeId().getShortName());
        }
        if (isAccount && !"Posted".equals(bean.getStatus())) {
            return;
        }
        IMObjectReference till = bean.getParticipantRef(TILL_PARTICIPATION);
        if (till == null) {
            throw new TillRuleException(MissingTill, act.getArchetypeId());
        }
        FinancialAct balance = TillHelper.getUnclearedTillBalance(till);
        if (balance == null) {
            balance = TillHelper.createTillBalance(till);
        }
        ActBean balanceBean = new ActBean(balance, service);
        if (balanceBean.getRelationship(act) == null) {
            balanceBean.addRelationship("actRelationship.tillBalanceItem", act);
            updateBalance(balanceBean, service);
            balanceBean.save();
        } else if (isAdjust) {
            updateBalance(balanceBean, service);
            balanceBean.save();
        }
    }

    /**
     * Clears a till.
     *
     * @param balance   the current till balance
     * @param cashFloat the amount remaining in the till
     * @param account   the account to deposit to
     * @param service   the archetype service
     * @return the updated balance
     * @throws TillRuleException if the balance doesn't have a till
     */
    public static FinancialAct clearTill(
            FinancialAct balance, BigDecimal cashFloat, Party account,
            IArchetypeService service) {
        ActBean balanceBean = new ActBean(balance);
        Entity till = balanceBean.getParticipant(TILL_PARTICIPATION);
        if (till == null) {
            throw new TillRuleException(MissingTill, balance.getUid());
        }
        IMObjectBean tillBean = new IMObjectBean(till);
        BigDecimal lastCashFloat = tillBean.getBigDecimal("tillFloat",
                                                          BigDecimal.ZERO);

        BigDecimal diff = cashFloat.subtract(lastCashFloat);
        if (diff.compareTo(BigDecimal.ZERO) != 0) {
            // need to generate an adjustment, and associate it with the balance
            boolean credit = (lastCashFloat.compareTo(cashFloat) > 0);
            Act adjustment = TillHelper.createTillBalanceAdjustment(
                    till.getObjectReference(), diff.abs(), credit);
            service.save(adjustment); // triggers addToTill()
            balance = (FinancialAct) reload(balance, service);
            if (balance == null) {
                throw new TillRuleException(BalanceNotFound);
            }
        }
        balance.setStatus(CLEARED);


        Act deposit = DepositHelper.getUndepositedDeposit(account);
        if (deposit == null) {
            deposit = DepositHelper.createBankDeposit(account);
        }
        ActBean depositBean = new ActBean(deposit);
        depositBean.addRelationship("actRelationship.bankDepositItem", balance);
        updateDepositTotal(depositBean, service);

/*
        todo - commented out as workaround for OBF-114
        List<Act> acts = new ArrayList<Act>();
        acts.add(adjustment);
        acts.add(balance);
        acts.add(deposit);
        service.save(acts);
*/
        service.save(balance);
        service.save(deposit);

        // need to reload the till to avoid hibernate StaleObjectException
        // caused by saving the balance (which has a reference to the till)
        String tillName = till.getName();
        till = (Party) reload(till, service);
        if (till == null) {
            throw new TillRuleException(TillNotFound, tillName);
        }
        tillBean = new IMObjectBean(till);
        tillBean.setValue("lastCleared", new Date());
        tillBean.setValue("tillFloat", cashFloat);
        tillBean.save();
        return balance;
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
        if (!actBean.isA(ACCOUNT_PAYMENT, ACCOUNT_REFUND)) {
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
        if (actBean.getParticipant(TILL_PARTICIPATION) == null) {
            throw new TillRuleException(MissingTill, act.getUid());
        }

        ActRelationship relationship = balanceBean.getRelationship(act);
        if (relationship == null) {
            throw new TillRuleException(MissingRelationship, balance.getUid());
        }
        balanceBean.removeRelationship(relationship);
        updateBalance(balanceBean, service);
        balanceBean.save();

/*
        todo - commented out as workaround for OBF-114
        List<Act> acts = new ArrayList<Act>();
        acts.add(balance);
        acts.add(act);
        service.save(acts);
*/
//        actBean.setParticipant(TILL_PARTICIPATION, till);
//        actBean.removeRelationship(relationship);
//        actBean.save();

        // need to reload the act to avoid hibernate StaleStateException
        // due to propagation of removal of relationship
        act = (Act) ArchetypeQueryHelper.getByObjectReference(
                service, act.getObjectReference());
        actBean = new ActBean(act);
        actBean.setParticipant(TILL_PARTICIPATION, till);
        actBean.save();

        // @todo should save all modifications in the one transaction
        addToTill(act, service);
    }

    /**
     * Updates the balance of an <em>act.tillBalance</em>.
     *
     * @param balanceBean the balance bean
     * @param service     the archetype service
     */
    private static void updateBalance(ActBean balanceBean,
                                      IArchetypeService service) {
        List<Act> items = balanceBean.getActs();
        ActCalculator calc = new ActCalculator(service);
        BigDecimal total = calc.sum(items, "amount").negate();
        balanceBean.setValue("balance", total);
    }

    /**
     * Calculates the total of an <em>act.bankDeposit</em>.
     *
     * @param depositBean the deposit bean
     * @param service     the archetype service
     */
    private static void updateDepositTotal(ActBean depositBean,
                                           IArchetypeService service) {
        List<Act> items = depositBean.getActs();
        ActCalculator calc = new ActCalculator(service);
        BigDecimal total = calc.sum(items, "balance");
        depositBean.setValue("total", total);
    }

    /**
     * Reloads an object from the archetype service.
     *
     * @param object  the object to reload
     * @param service the archetype service
     * @return the reloaded object
     */
    private static IMObject reload(IMObject object, IArchetypeService service) {
        return ArchetypeQueryHelper.getByObjectReference(
                service, object.getObjectReference());
    }

}
