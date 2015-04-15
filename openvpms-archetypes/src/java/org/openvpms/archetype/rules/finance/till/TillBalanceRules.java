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
import java.util.List;

import static org.openvpms.archetype.rules.act.ActStatus.POSTED;
import static org.openvpms.archetype.rules.finance.till.TillArchetypes.TILL_BALANCE;
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
     *
     * @param act the till balance act
     * @throws TillRuleException         if the act is invalid or the till is missing
     * @throws ArchetypeServiceException for any archetype service error
     */
    public void addToTill(Act act) {
        ActBean bean = new ActBean(act, service);
        boolean add = true;
        boolean isAccount = bean.isA(CustomerAccountArchetypes.PAYMENT, CustomerAccountArchetypes.REFUND);
        boolean isAdjust = bean.isA(TillArchetypes.TILL_BALANCE_ADJUSTMENT);
        if (!isAccount && !isAdjust) {
            throw new TillRuleException(CantAddActToTill, act.getArchetypeId().getShortName());
        }
        if (isAccount && !POSTED.equals(act.getStatus())) {
            add = false;
        } else if (!bean.getRelationships(TILL_BALANCE_ITEM).isEmpty()) {
            // already associated with a balance
            add = false;
        }
        if (add) {
            List<Act> acts = doAddToTill(act);
            service.save(acts);
        }
    }

    /**
     * Adds an act to a till.
     *
     * @param act the act to add
     * @return the changed acts
     * @throws TillRuleException         if the act is invalid or the till is missing
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected List<Act> doAddToTill(Act act) {
        List<Act> result = new ArrayList<Act>();
        IMObjectReference tillRef = TillHelper.getTillRef(act, service);
        FinancialAct balance = TillHelper.getUnclearedTillBalance(tillRef, service);
        if (balance == null) {
            Entity till = (Entity) service.get(tillRef);
            if (till == null) {
                throw new TillRuleException(TillNotFound, tillRef.getId());
            }
            balance = TillHelper.createTillBalance(till, service);
        }
        boolean isAdjust = TypeHelper.isA(act, TillArchetypes.TILL_BALANCE_ADJUSTMENT);
        ActBean balanceBean = new ActBean(balance, service);
        if (!balanceBean.hasNodeTarget("items", act)) {
            balanceBean.addNodeRelationship("items", act);
            result.add(act);
            result.add(balance);
            TillHelper.updateBalance(balanceBean, service);
        } else if (isAdjust) {
            if (TillHelper.updateBalance(balanceBean, service)) {
                result.add(balance);
            }
        }
        return result;
    }


}
