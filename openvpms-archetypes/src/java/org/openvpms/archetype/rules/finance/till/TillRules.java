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

package org.openvpms.archetype.rules.finance.till;

import org.openvpms.archetype.rules.act.ActCalculator;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.finance.deposit.DepositHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.CollectionNodeConstraint;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.ObjectRefNodeConstraint;
import org.openvpms.component.system.common.query.QueryIterator;
import org.openvpms.component.system.common.query.RelationalOp;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;

import static org.openvpms.archetype.rules.act.ActStatus.POSTED;
import static org.openvpms.archetype.rules.finance.till.TillArchetypes.TILL_BALANCE;
import static org.openvpms.archetype.rules.finance.till.TillArchetypes.TILL_BALANCE_ITEM;
import static org.openvpms.archetype.rules.finance.till.TillArchetypes.TILL_PARTICIPATION;
import static org.openvpms.archetype.rules.finance.till.TillRuleException.ErrorCode.BalanceNotFound;
import static org.openvpms.archetype.rules.finance.till.TillRuleException.ErrorCode.CantAddActToTill;
import static org.openvpms.archetype.rules.finance.till.TillRuleException.ErrorCode.ClearedTill;
import static org.openvpms.archetype.rules.finance.till.TillRuleException.ErrorCode.InvalidTillArchetype;
import static org.openvpms.archetype.rules.finance.till.TillRuleException.ErrorCode.InvalidTransferTill;
import static org.openvpms.archetype.rules.finance.till.TillRuleException.ErrorCode.MissingRelationship;
import static org.openvpms.archetype.rules.finance.till.TillRuleException.ErrorCode.MissingTill;
import static org.openvpms.archetype.rules.finance.till.TillRuleException.ErrorCode.TillNotFound;
import static org.openvpms.archetype.rules.finance.till.TillRuleException.ErrorCode.UnclearedTillExists;


/**
 * Till business rules.
 *
 * @author Tim Anderson
 */
public class TillRules {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Constructs a new <code>TillRules</code>.
     */
    public TillRules() {
        this(ArchetypeServiceHelper.getArchetypeService());
    }

    /**
     * Constructa a new <code>TillRules</code>.
     *
     * @param service the archetype service
     */
    public TillRules(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Rule that determines if an <em>act.tillBalance</em> can be saved.
     * One can be saved if:
     * <ul>
     * <li>it has status 'Cleared' and was previously 'Uncleared'</li>
     * <li>it has status 'Uncleared' and there are no other uncleared
     * act.tillBalances for  the till</li>
     * </ul>
     *
     * @param act the till balance act
     * @throws TillRuleException         if the act can't be saved
     * @throws ArchetypeServiceException for any archetype service error
     */
    public void checkCanSaveTillBalance(Act act) {
        ActBean bean = new ActBean(act, service);
        if (!bean.isA(TILL_BALANCE)) {
            throw new TillRuleException(InvalidTillArchetype, act.getArchetypeId().getShortName());
        }

        Act oldAct = (Act) service.get(act.getObjectReference());
        if (oldAct != null) {
            // If the act already exists, make sure it hasn't been cleared
            if (TillBalanceStatus.CLEARED.equals(oldAct.getStatus())) {
                throw new TillRuleException(ClearedTill, act.getId());
            }
        } else {
            // Else we have a completely new till balance so if status is
            // cleared check no other uncleared for Till.
            if (TillBalanceStatus.UNCLEARED.equals(bean.getStatus())) {
                Entity till = bean.getParticipant(TILL_PARTICIPATION);
                if (till == null) {
                    throw new TillRuleException(MissingTill, act.getArchetypeId());
                }
                Act current = getUnclearedTillBalance(till.getObjectReference());
                if (current != null && current.getId() != act.getId()) {
                    throw new TillRuleException(UnclearedTillExists, till.getName());
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
     * @param act the till balance act
     * @throws TillRuleException         if the act is invalid or the till is
     *                                   missing
     * @throws ArchetypeServiceException for any archetype service error
     */
    public void addToTill(Act act) {
        ActBean bean = new ActBean(act, service);
        boolean add = true;
        boolean isAccount = bean.isA(CustomerAccountArchetypes.PAYMENT, CustomerAccountArchetypes.REFUND);
        boolean isAdjust = bean.isA("act.tillBalanceAdjustment");
        if (!isAccount && !isAdjust) {
            throw new TillRuleException(CantAddActToTill,
                                        act.getArchetypeId().getShortName());
        }
        if (isAccount && !POSTED.equals(act.getStatus())) {
            add = false;
        } else if (!bean.getRelationships(TILL_BALANCE_ITEM).isEmpty()) {
            // already associated with a balance
            add = false;
        }
        if (add) {
            doAddToTill(act);
        }
    }

    /**
     * `
     * Clears a till.
     *
     * @param balance   the current till balance
     * @param cashFloat the amount remaining in the till
     * @param account   the account to deposit to
     * @return the updated balance
     * @throws TillRuleException if the balance doesn't have a till
     */
    public FinancialAct clearTill(FinancialAct balance, BigDecimal cashFloat,
                                  Party account) {
        ActBean balanceBean = new ActBean(balance, service);
        Entity till = balanceBean.getParticipant(TILL_PARTICIPATION);
        if (till == null) {
            throw new TillRuleException(MissingTill, balance.getId());
        }
        IMObjectBean tillBean = new IMObjectBean(till, service);
        BigDecimal lastCashFloat = tillBean.getBigDecimal("tillFloat",
                                                          BigDecimal.ZERO);

        BigDecimal diff = cashFloat.subtract(lastCashFloat);
        if (diff.compareTo(BigDecimal.ZERO) != 0) {
            // need to generate an adjustment, and associate it with the balance
            boolean credit = (lastCashFloat.compareTo(cashFloat) > 0);
            Act adjustment = createTillBalanceAdjustment(till, diff.abs(),
                                                         credit);
            service.save(adjustment); // triggers addToTill()
            balance = (FinancialAct) reload(balance);
            if (balance == null) {
                throw new TillRuleException(BalanceNotFound);
            }
        }
        balance.setStatus(TillBalanceStatus.CLEARED);
        balance.setActivityEndTime(new Date());

        Act deposit = DepositHelper.getUndepositedDeposit(account);
        if (deposit == null) {
            deposit = DepositHelper.createBankDeposit(account);
        }
        ActBean depositBean = new ActBean(deposit, service);
        depositBean.addRelationship("actRelationship.bankDepositItem", balance);
        updateDepositTotal(depositBean);

/*
        todo - commented out as workaround for OBF-114
        List<Act> acts = new ArrayList<Act>();
        acts.add(adjustment);
        acts.add(balance);
        acts.add(deposit);
        service.save(acts);
*/
        service.save(Arrays.asList(balance, deposit));

        // need to reload the till to avoid hibernate StaleObjectException
        // caused by saving the balance (which has a reference to the till)
        String tillName = till.getName();
        till = (Party) reload(till);
        if (till == null) {
            throw new TillRuleException(TillNotFound, tillName);
        }
        tillBean = new IMObjectBean(till, service);
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
     */
    public void transfer(Act balance, Act act, Party till) {
        ActBean balanceBean = new ActBean(balance, service);
        if (!balanceBean.isA(TILL_BALANCE)) {
            throw new TillRuleException(
                    InvalidTillArchetype,
                    balance.getArchetypeId().getShortName());
        }
        ActBean actBean = new ActBean(act, service);
        if (!actBean.isA(CustomerAccountArchetypes.PAYMENT, CustomerAccountArchetypes.REFUND)) {
            throw new TillRuleException(CantAddActToTill, act.getArchetypeId().getShortName());
        }
        Entity orig = balanceBean.getParticipant(TILL_PARTICIPATION);
        if (orig == null) {
            throw new TillRuleException(MissingTill, balance.getId());
        }
        if (orig.equals(till)) {
            throw new TillRuleException(InvalidTransferTill, till.getName());
        }
        if (!TillBalanceStatus.UNCLEARED.equals(balance.getStatus())) {
            throw new TillRuleException(ClearedTill, balance.getId());
        }
        if (actBean.getParticipant(TILL_PARTICIPATION) == null) {
            throw new TillRuleException(MissingTill, act.getId());
        }

        ActRelationship relationship = balanceBean.getRelationship(act);
        if (relationship == null) {
            throw new TillRuleException(MissingRelationship, balance.getId()
            );
        }
        balanceBean.removeRelationship(relationship);
        actBean.removeRelationship(relationship);
        actBean.setParticipant(TILL_PARTICIPATION, till);
        updateBalance(balanceBean);
        service.save(Arrays.asList(balance, act));

        // @todo should save all modifications in the one transaction
        doAddToTill(act);
    }

    /**
     * Helper to return the uncleared till balance for a till, if it exists.
     *
     * @param till a reference to the till
     * @return the uncleared till balance, or <code>null</code> if none exists
     */
    public FinancialAct getUnclearedTillBalance(IMObjectReference till) {
        ArchetypeQuery query = new ArchetypeQuery(TILL_BALANCE,
                                                  false,
                                                  true);
        query.add(new NodeConstraint("status", RelationalOp.EQ,
                                     TillBalanceStatus.UNCLEARED));
        CollectionNodeConstraint participations
                = new CollectionNodeConstraint("till",
                                               TILL_PARTICIPATION,
                                               false, true);
        participations.add(new ObjectRefNodeConstraint("entity", till));
        query.add(participations);
        QueryIterator<FinancialAct> iterator
                = new IMObjectQueryIterator<FinancialAct>(service, query);
        return (iterator.hasNext()) ? iterator.next() : null;
    }

    /**
     * Helper to create a new till balance, associating it with a till.
     *
     * @param till the till
     * @return a new till balance
     */
    public FinancialAct createTillBalance(Entity till) {
        return createTillBalance(till.getObjectReference());
    }

    /**
     * Creates a new till balance adjustment, associating it with a till.
     *
     * @param till   the till
     * @param amount the amount
     * @param credit if <code>true</code> this is a credit adjustment,
     *               otherwise its a debit adjustment
     * @return a new till balance adjustment
     */
    public FinancialAct createTillBalanceAdjustment(Entity till,
                                                    BigDecimal amount,
                                                    boolean credit) {
        FinancialAct act = (FinancialAct) service.create(
                "act.tillBalanceAdjustment");
        ActBean bean = new ActBean(act, service);
        bean.setValue("amount", amount);
        bean.setValue("credit", credit);
        bean.setParticipant(TILL_PARTICIPATION, till);
        return act;
    }

    /**
     * Adds an act to a till.
     *
     * @param act the act to add
     * @throws TillRuleException         if the act is invalid or the till is
     *                                   missing
     * @throws ArchetypeServiceException for any archetype service error
     */
    private void doAddToTill(Act act) {
        ActBean bean = new ActBean(act, service);
        boolean isAdjust = TypeHelper.isA(act, "act.tillBalanceAdjustment");
        IMObjectReference till = bean.getParticipantRef(TILL_PARTICIPATION);
        if (till == null) {
            throw new TillRuleException(MissingTill, act.getArchetypeId());
        }
        FinancialAct balance = getUnclearedTillBalance(till);
        if (balance == null) {
            balance = createTillBalance(till);
        }
        ActBean balanceBean = new ActBean(balance, service);
        if (balanceBean.getRelationship(act) == null) {
            balanceBean.addRelationship(TILL_BALANCE_ITEM, act);
            updateBalance(balanceBean);
            service.save(Arrays.asList(act, balance));
        } else if (isAdjust) {
            updateBalance(balanceBean);
            balanceBean.save();
        }
    }

    /**
     * Helper to create a new till balance, associating it with a till.
     *
     * @param till the till
     * @return a new till balance
     */
    private FinancialAct createTillBalance(IMObjectReference till) {
        FinancialAct act = (FinancialAct) service.create(
                TILL_BALANCE);
        ActBean bean = new ActBean(act, service);
        bean.setStatus(TillBalanceStatus.UNCLEARED);
        bean.setParticipant(TILL_PARTICIPATION, till);

        // Get the Till and extract last cash float amount to set balance cash float.  
        Entity theTill = bean.getParticipant(TILL_PARTICIPATION);
        BigDecimal tillCashFloat;
        if (theTill != null) {
            IMObjectBean tillBean = new IMObjectBean(theTill, service);
            tillCashFloat = tillBean.getBigDecimal("tillFloat",
                                                   BigDecimal.ZERO);
            bean.setValue("cashFloat", tillCashFloat);
        }

        return act;
    }

    /**
     * Updates the balance of an <em>act.tillBalance</em>.
     *
     * @param balanceBean the balance bean
     */
    private void updateBalance(ActBean balanceBean) {
        ActCalculator calc = new ActCalculator(service);
        BigDecimal total = calc.sum(balanceBean.getAct(), "amount");
        balanceBean.setValue("amount", total);
    }

    /**
     * Calculates the total of an <em>act.bankDeposit</em>.
     *
     * @param depositBean the deposit bean
     */
    private void updateDepositTotal(ActBean depositBean) {
        ActCalculator calc = new ActCalculator(service);
        BigDecimal total = calc.sum(depositBean.getAct(), "amount");
        depositBean.setValue("amount", total);
    }

    /**
     * Reloads an object from the archetype service.
     *
     * @param object the object to reload
     * @return the reloaded object
     */
    private IMObject reload(IMObject object) {
        return service.get(object.getObjectReference());
    }


}
