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

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.openvpms.archetype.rules.act.ActStatus.POSTED;
import static org.openvpms.archetype.rules.finance.till.TillArchetypes.TILL_BALANCE;
import static org.openvpms.archetype.rules.finance.till.TillArchetypes.TILL_BALANCE_ADJUSTMENT;
import static org.openvpms.archetype.rules.finance.till.TillArchetypes.TILL_BALANCE_ITEM;
import static org.openvpms.archetype.rules.finance.till.TillHelper.getTill;
import static org.openvpms.archetype.rules.finance.till.TillRuleException.ErrorCode.CantAddActToTill;
import static org.openvpms.archetype.rules.finance.till.TillRuleException.ErrorCode.ClearedTill;
import static org.openvpms.archetype.rules.finance.till.TillRuleException.ErrorCode.InvalidTillArchetype;
import static org.openvpms.archetype.rules.finance.till.TillRuleException.ErrorCode.TillNotFound;
import static org.openvpms.archetype.rules.finance.till.TillRuleException.ErrorCode.UnclearedTillExists;

/**
 * Till Balance rules.
 *
 * @author Tim Anderson
 */
public class TillBalanceRules {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Constructs an {@link TillBalanceRules}.
     *
     * @param service the archetype service
     */
    public TillBalanceRules(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Helper to return the uncleared till balance for a till, if it exists.
     *
     * @param till the till
     * @return the uncleared till balance, or {@code null} if none exists
     */
    public FinancialAct getUnclearedBalance(Entity till) {
        return TillHelper.getUnclearedTillBalance(till, service);
    }

    /**
     * Rule that determines if an <em>act.tillBalance</em> can be saved.
     * One can be saved if:
     * <ul>
     * <li>it has status 'Cleared' and was previously 'Uncleared' or 'Clear In Progress'</li>
     * <li>it has status 'Uncleared' and there are no other uncleared act.tillBalances for the till</li>
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
                Entity till = getTill(act, service);
                Act current = TillHelper.getUnclearedTillBalance(till, service);
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
     * <p/>
     * <strong>NOTE: </strong> callers invoking this directly are responsible for calling
     * {@link #updateBalance(FinancialAct)} if {@code act} has not already been saved.
     *
     * @param act the till balance act
     * @throws TillRuleException         if the act is invalid or the till is missing
     * @throws ArchetypeServiceException for any archetype service error
     */
    public void addToTill(Act act) {
        List<Act> acts = addToBalance(act);
        if (!acts.isEmpty()) {
            service.save(acts);
        }
    }

    /**
     * Adds an act to a till balance.
     * <p/>
     * <strong>NOTE: </strong> callers invoking this directly are responsible for calling
     * {@link #updateBalance(FinancialAct)} if {@code act} has not already been saved.
     *
     * @param act the act
     * @return the changed acts
     */
    public List<Act> addToBalance(Act act) {
        List<Act> result;
        if (checkAdd(act)) {
            FinancialAct balance = getBalance(act);
            result = doAddToBalance(act, balance);
        } else if (TypeHelper.isA(act, TILL_BALANCE_ADJUSTMENT)) {
            result = updateBalanceForTillAdjustment(act);
        } else {
            result = Collections.emptyList();
        }
        return result;
    }

    /**
     * Adds an act to a till balance.
     * <p/>
     * <strong>NOTE: </strong> callers invoking this directly are responsible for calling
     * {@link #updateBalance(FinancialAct)} if {@code act} has not already been saved.
     *
     * @param act     the act
     * @param balance the balance
     * @return the changed acts
     */
    public List<Act> addToBalance(Act act, Act balance) {
        List<Act> result;
        if (checkAdd(act)) {
            result = doAddToBalance(act, balance);
        } else if (TypeHelper.isA(act, TILL_BALANCE_ADJUSTMENT)) {
            result = updateBalanceForTillAdjustment(act);
        } else {
            result = Collections.emptyList();
        }
        return result;
    }

    /**
     * Updates the amount of an <em>act.tillBalance</em>.
     *
     * @param balance the till balance
     * @return {@code true} if the balance changed
     */
    public boolean updateBalance(FinancialAct balance) {
        boolean result = false;
        if (TillBalanceStatus.CLEARED.equals(balance.getStatus())) {
            throw new TillRuleException(ClearedTill, balance.getId());
        }
        ActBean bean = new ActBean(balance, service);
        if (TillHelper.updateBalance(bean, service)) {
            bean.save();
            result = true;
        }
        return result;
    }

    /**
     * Adds an act to a till balance.
     * <p/>
     * <strong>NOTE: </strong> callers invoking this directly are responsible for calling
     * {@link #updateBalance(FinancialAct)} if {@code act} has not already been saved.
     *
     * @param act     the act
     * @param balance the balance
     * @return the changed acts
     */
    private List<Act> doAddToBalance(Act act, Act balance) {
        List<Act> result = new ArrayList<Act>();
        if (TillBalanceStatus.CLEARED.equals(balance.getStatus())) {
            throw new IllegalStateException("Till balance cannot be " + TillBalanceStatus.CLEARED);
        }
        ActBean bean = new ActBean(act);
        ActBean balanceBean = new ActBean(balance, service);
        IMObjectReference till = bean.getNodeParticipantRef("till");
        if (till == null) {
            throw new IllegalStateException(act.getObjectReference() + "  has no till");
        }
        if (!ObjectUtils.equals(till, balanceBean.getNodeParticipantRef("till"))) {
            throw new IllegalStateException(act.getObjectReference() + "  has as different till to "
                                            + balance.getObjectReference());
        }
        if (!balanceBean.hasNodeTarget("items", act)) {
            balanceBean.addNodeRelationship("items", act);
            result.add(act);
            result.add(balance);
            TillHelper.updateBalance(balanceBean, service);
        }
        return result;
    }

    /**
     * Verifies an act can be added to a till balance.
     *
     * @param act the act
     * @return {@code true} if it can be added, otherwise {@code false}
     * @throws TillRuleException if the act is not a valid act to add
     */
    private boolean checkAdd(Act act) {
        boolean add = true;
        ActBean bean = new ActBean(act, service);
        boolean isAccount = bean.isA(CustomerAccountArchetypes.PAYMENT, CustomerAccountArchetypes.REFUND);
        boolean isAdjust = bean.isA(TillArchetypes.TILL_BALANCE_ADJUSTMENT);
        if (!isAccount && !isAdjust) {
            throw new TillRuleException(CantAddActToTill, act.getArchetypeId().getShortName());
        }
        if (isAccount && !POSTED.equals(act.getStatus())) {
            add = false;
        } else if (!bean.getRelationships(TILL_BALANCE_ITEM).isEmpty()) {
            // already associated with a balance.
            add = false;
        }
        return add;
    }

    /**
     * Returns an uncleared till balance to add an act to.
     * <p/>
     * This will create one if none exists.
     *
     * @param act the act
     * @return an uncleared till balance
     */
    private FinancialAct getBalance(Act act) {
        IMObjectReference tillRef = TillHelper.getTillRef(act, service);
        FinancialAct balance = TillHelper.getUnclearedTillBalance(tillRef, service);
        if (balance == null) {
            Entity till = (Entity) service.get(tillRef);
            if (till == null) {
                throw new TillRuleException(TillNotFound, tillRef.getId());
            }
            balance = TillHelper.createTillBalance(till, service);
        }
        return balance;
    }

    /**
     * Due to an historical oversight, till balance adjustments don't get POSTED. This means that they can be changed
     * until the till is CLEARED.
     *
     * @param act the till balance adjustment
     * @return the updated acts, if any
     */
    private List<Act> updateBalanceForTillAdjustment(Act act) {
        List<Act> result = Collections.emptyList();
        ActBean bean = new ActBean(act, service);
        FinancialAct balance = (FinancialAct) bean.getSourceAct(TILL_BALANCE_ITEM);
        if (balance != null) {
            if (updateBalance(balance)) {
                result = new ArrayList<Act>();
                result.add(balance);
            }
        }
        return result;
    }

}
