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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.finance.till;

import org.openvpms.archetype.rules.act.ActCalculator;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.finance.deposit.DepositHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.NodeSelectConstraint;
import org.openvpms.component.system.common.query.ObjectSetQueryIterator;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.openvpms.archetype.rules.finance.till.TillArchetypes.TILL_BALANCE;
import static org.openvpms.archetype.rules.finance.till.TillArchetypes.TILL_PARTICIPATION;
import static org.openvpms.archetype.rules.finance.till.TillHelper.getTill;
import static org.openvpms.archetype.rules.finance.till.TillHelper.getTillRef;
import static org.openvpms.archetype.rules.finance.till.TillRuleException.ErrorCode.CantAddActToTill;
import static org.openvpms.archetype.rules.finance.till.TillRuleException.ErrorCode.ClearInProgress;
import static org.openvpms.archetype.rules.finance.till.TillRuleException.ErrorCode.ClearedTill;
import static org.openvpms.archetype.rules.finance.till.TillRuleException.ErrorCode.DifferentTills;
import static org.openvpms.archetype.rules.finance.till.TillRuleException.ErrorCode.InvalidStatusForClear;
import static org.openvpms.archetype.rules.finance.till.TillRuleException.ErrorCode.InvalidStatusForStartClear;
import static org.openvpms.archetype.rules.finance.till.TillRuleException.ErrorCode.InvalidTillArchetype;
import static org.openvpms.archetype.rules.finance.till.TillRuleException.ErrorCode.InvalidTransferTill;
import static org.openvpms.archetype.rules.finance.till.TillRuleException.ErrorCode.MissingRelationship;
import static org.openvpms.archetype.rules.finance.till.TillRuleException.ErrorCode.MissingTill;
import static org.openvpms.component.system.common.query.Constraints.eq;
import static org.openvpms.component.system.common.query.Constraints.join;


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
     * The transaction manager.
     */
    private final PlatformTransactionManager transactionManager;

    /**
     * The till balance rules.
     */
    private final TillBalanceRules rules;

    /**
     * Constructs a {@link TillRules}.
     *
     * @param service            the archetype service
     * @param transactionManager the transaction manager
     */
    public TillRules(IArchetypeService service, PlatformTransactionManager transactionManager) {
        this.service = service;
        this.transactionManager = transactionManager;
        this.rules = new TillBalanceRules(service);
    }

    /**
     * Determines if a till is in the process of being cleared.
     *
     * @param till the till
     * @return {@code true} if the till has a balance with IN_PROGRESS status
     */
    public boolean isClearInProgress(Entity till) {
        IMObjectReference reference = till.getObjectReference();
        return isClearInProgress(reference);
    }

    /**
     * Determines if a till is in the process of being cleared.
     *
     * @param till the till reference
     * @return {@code true} if the till has a balance with IN_PROGRESS status
     */
    public boolean isClearInProgress(IMObjectReference till) {
        ArchetypeQuery query = new ArchetypeQuery(TILL_BALANCE, false, true);
        query.add(eq("status", TillBalanceStatus.IN_PROGRESS)).add(join("till").add(eq("entity", till)));
        query.add(new NodeSelectConstraint("id"));
        query.setMaxResults(1);
        return new ObjectSetQueryIterator(service, query).hasNext();
    }

    /**
     * Start clearing the till.
     * <p/>
     * This sets the status of the till balance to IN_PROGRESS, so that any new payments or refunds don't affect it.
     * <p/>
     * If the cash float is different to the existing cash float for the till, an adjustment will be created.
     *
     * @param balance   the till balance
     * @param cashFloat the amount remaining in the till
     * @throws TillRuleException         if the balance is not UNCLEARED
     * @throws ArchetypeServiceException for any archetype service error
     */
    public void startClearTill(final FinancialAct balance, final BigDecimal cashFloat) {
        if (!balance.getStatus().equals(TillBalanceStatus.UNCLEARED)) {
            throw new TillRuleException(InvalidStatusForStartClear, balance.getStatus());
        }
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                if (isClearInProgress(getTillRef(balance, service))) {
                    throw new TillRuleException(ClearInProgress);
                }
                balance.setStatus(TillBalanceStatus.IN_PROGRESS);
                Till till = getTillBean(balance);
                addAdjustment(balance, cashFloat, till);
                service.save(balance);
                till.setTillFloat(cashFloat);
                till.setLastCleared(new Date());
                till.save();
            }
        });
    }

    /**
     * Adds an item to a balance.
     *
     * @param balance the balance
     * @param item    the balance item
     * @throws TillRuleException if the balance is CLEARED, or if an act doesn't have a till, or they are different
     */
    public void addToBalance(FinancialAct balance, FinancialAct item) {
        IMObjectReference balanceTill = getTillRef(balance, service);
        IMObjectReference itemTill = getTillRef(balance, service);
        if (!balanceTill.equals(itemTill)) {
            throw new TillRuleException(DifferentTills, DescriptorHelper.getDisplayName(item, service));
        }
        if (TillBalanceStatus.CLEARED.equals(balance.getStatus())) {
            throw new TillRuleException(ClearedTill, balance.getId());
        }
        ActBean bean = new ActBean(balance, service);
        bean.addNodeRelationship("item", item);
        service.save(Arrays.asList(balance, item));
    }

    /**
     * Clears a till for an IN_PROGRESS balance.
     *
     * @param balance the current till balance
     * @param account the account to deposit to
     * @throws TillRuleException if the balance doesn't have a till
     */
    public void clearTill(final FinancialAct balance, final Party account) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                clearInProgressTill(balance, account);
            }
        });
    }

    /**
     * Clears a till for an UNCLEARED balance.
     * <p/>
     * If the cash float is different to the existing cash float for the till, an adjustment will be created.
     *
     * @param balance   the current till balance
     * @param cashFloat the amount remaining in the till
     * @param account   the account to deposit to
     * @throws TillRuleException if the balance doesn't have a till
     */
    public void clearTill(final FinancialAct balance, final BigDecimal cashFloat, final Party account) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                clearUnclearedTill(balance, cashFloat, account);
            }
        });
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
            throw new TillRuleException(InvalidTillArchetype, balance.getArchetypeId().getShortName());
        }
        ActBean actBean = new ActBean(act, service);
        if (!actBean.isA(CustomerAccountArchetypes.PAYMENT, CustomerAccountArchetypes.REFUND)) {
            throw new TillRuleException(CantAddActToTill, act.getArchetypeId().getShortName());
        }
        Entity orig = getTill(balance, service);
        if (orig.equals(till)) {
            throw new TillRuleException(InvalidTransferTill, till.getName());
        }
        if (TillBalanceStatus.CLEARED.equals(balance.getStatus())) {
            throw new TillRuleException(ClearedTill, balance.getId());
        }
        if (actBean.getParticipant(TILL_PARTICIPATION) == null) {
            throw new TillRuleException(MissingTill, act.getId());
        }

        ActRelationship relationship = balanceBean.getRelationship(act);
        if (relationship == null) {
            throw new TillRuleException(MissingRelationship, balance.getId());
        }
        balanceBean.removeRelationship(relationship);
        actBean.removeRelationship(relationship);
        actBean.setParticipant(TILL_PARTICIPATION, till);
        TillHelper.updateBalance(balanceBean, service);

        List<Act> toSave = rules.addToBalance(act);
        toSave.add(balance);
        service.save(toSave);
    }

    /**
     * Updates the amount of an UNCLEARED or IN_PROGRESS <em>act.tillBalance</em>.
     * <p/>
     * If updated, the balance is saved.
     *
     * @param balance the balance
     * @return {@code true if the balance was updated, otherwise {@code false}
     */
    public boolean updateBalance(FinancialAct balance) {
        return rules.updateBalance(balance);
    }

    /**
     * Clears the till, performing an adjustment if the new cash float is different to the old one.
     * <p/>
     * The till must be UNCLEARED to perform the adjustment.
     * <p/>
     * This should be invoked within a transaction.
     *
     * @param balance   the current till balance
     * @param cashFloat the amount remaining in the till
     * @param account   the account to deposit to
     * @throws TillRuleException if the balance doesn't have a till
     */
    private void clearUnclearedTill(FinancialAct balance, BigDecimal cashFloat, Party account) {
        String status = balance.getStatus();
        if (!TillBalanceStatus.UNCLEARED.equals(status)) {
            throw new TillRuleException(InvalidStatusForClear, status);
        }
        Till till = getTillBean(balance);
        addAdjustment(balance, cashFloat, till);
        depositBalance(balance, account);
        till.setLastCleared(new Date());
        till.setTillFloat(cashFloat);
        till.save();
    }

    /**
     * Adds an adjustment to a till, if required.
     *
     * @param balance   the till balance act
     * @param cashFloat the new cash float
     */
    private void addAdjustment(FinancialAct balance, BigDecimal cashFloat, Till till) {
        BigDecimal lastCashFloat = till.getTillFloat();

        BigDecimal diff = cashFloat.subtract(lastCashFloat);
        if (diff.compareTo(BigDecimal.ZERO) != 0) {
            // need to generate an adjustment, and associate it with the balance
            boolean credit = (lastCashFloat.compareTo(cashFloat) > 0);
            Act adjustment = createTillBalanceAdjustment(till.getEntity(), diff.abs(), credit);
            ActBean balanceBean = new ActBean(balance);
            balanceBean.addNodeRelationship("items", adjustment);
            service.save(adjustment); // NOTE that this will trigger TillBalanceRules.addToTill(), but will have no effect
            TillHelper.updateBalance(balanceBean, service);
        }
    }

    /**
     * Clears the till.
     * <p/>
     * The till must be IN_PROGRESS.
     * <p/>
     * This should be invoked within a transaction.
     *
     * @param balance the current till balance
     * @param account the account to deposit to
     * @throws TillRuleException if the balance doesn't have a till
     */
    private void clearInProgressTill(FinancialAct balance, Party account) {
        String status = balance.getStatus();
        if (!TillBalanceStatus.IN_PROGRESS.equals(status)) {
            throw new TillRuleException(InvalidStatusForClear, status);
        }
        depositBalance(balance, account);
    }

    private void depositBalance(FinancialAct balance, Party account) {
        balance.setStatus(TillBalanceStatus.CLEARED);
        balance.setActivityEndTime(new Date());

        Act deposit = DepositHelper.getUndepositedDeposit(account);
        if (deposit == null) {
            deposit = DepositHelper.createBankDeposit(account);
        }
        ActBean depositBean = new ActBean(deposit, service);
        depositBean.addRelationship("actRelationship.bankDepositItem", balance);
        service.save(balance); // need to save so its visible to the deposit when updating total
        updateDepositTotal(depositBean);

        service.save(deposit);
    }

    /**
     * Creates a new till balance adjustment, associating it with a till.
     *
     * @param till   the till
     * @param amount the amount
     * @param credit if {@code true} this is a credit adjustment, otherwise its a debit adjustment
     * @return a new till balance adjustment
     */
    private FinancialAct createTillBalanceAdjustment(Entity till, BigDecimal amount, boolean credit) {
        FinancialAct act = (FinancialAct) service.create("act.tillBalanceAdjustment");
        ActBean bean = new ActBean(act, service);
        bean.setValue("amount", amount);
        bean.setValue("credit", credit);
        bean.setParticipant(TILL_PARTICIPATION, till);
        return act;
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
     * Returns a till associated with an act.
     *
     * @param act the act
     * @return the corresponding till
     */
    private Till getTillBean(Act act) {
        return new Till(getTill(act, service), service);
    }

    /**
     * Helper to manipulate a till.
     */
    private static class Till {

        private final IMObjectBean till;

        private Till(Entity entity, IArchetypeService service) {
            till = new IMObjectBean(entity, service);
        }

        public BigDecimal getTillFloat() {
            return till.getBigDecimal("tillFloat", BigDecimal.ZERO);
        }

        public void setTillFloat(BigDecimal value) {
            till.setValue("tillFloat", value);
        }

        public void setLastCleared(Date date) {
            till.setValue("lastCleared", date);
        }

        public Entity getEntity() {
            return (Entity) till.getObject();
        }

        public void save() {
            till.save();
        }
    }
}
