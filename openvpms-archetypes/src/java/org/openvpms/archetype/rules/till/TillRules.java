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
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.RelationalOp;

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
                Party till = TillHelper.getTill(act);
                if (till == null) {
                    throw new TillRuleException(MissingTill,
                                                act.getArchetypeId());
                }
                FinancialAct current = TillHelper.getUnclearedTillBalance(
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
    public static void addToTill(FinancialAct act, IArchetypeService service) {
        if (!TypeHelper.isA(act, "act.customerAccountPayment",
                            "act.customerAccountRefund")) {
            throw new TillRuleException(CantAddActToTill,
                                        act.getArchetypeId().getShortName());
        }
        if (!"Posted".equals(act.getStatus())) {
            return;
        }
        IMObjectReference till = TillHelper.getTillReference(act);
        if (till == null) {
            throw new TillRuleException(MissingTill, act.getArchetypeId());
        }
        FinancialAct balance = TillHelper.getUnclearedTillBalance(till);
        if (balance == null) {
            balance = TillHelper.createTillBalance(till);
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
        TillHelper.addRelationship("actRelationship.tillBalanceItem",
                                   balance, act);
        service.save(balance);
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
    public static void clearTill(FinancialAct balance, BigDecimal amount,
                                 Party account,
                                 IArchetypeService service) {
        Party till = TillHelper.getTill(balance);
        if (till == null) {
            throw new TillRuleException(MissingTill, balance.getArchetypeId());
        }
        IMObjectBean bean = new IMObjectBean(till);
        BigDecimal current = bean.getBigDecimal("tillFloat");
        if (current == null) {
            current = BigDecimal.ZERO;
        }
        BigDecimal diff = amount.subtract(current);
        balance.setStatus("Cleared");
        FinancialAct adjustment = TillHelper.createTillBalanceAdjustment(
                till.getObjectReference(), new Money(diff));
        FinancialAct deposit = TillHelper.createBankDeposit(
                balance, account.getObjectReference());
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
        service.save(till);
    }

}
