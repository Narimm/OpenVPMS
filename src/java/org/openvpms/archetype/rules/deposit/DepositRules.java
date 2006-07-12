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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.deposit;

import static org.openvpms.archetype.rules.deposit.DepositRuleException.ErrorCode.*;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.util.Date;


/**
 * Deposit rules.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class DepositRules {

    /**
     * Deposited act status.
     */
    public static final String DEPOSITED = "Deposited";

    /**
     * Undeposited act status.
     */
    public static final String UNDEPOSITED = "UnDeposited";

    /**
     * Bank deposit act short name.
     */
    public static final String BANK_DEPOSIT = "act.bankDeposit";

    /**
     * Deposit participation short name.
     */
    public static final String DEPOSIT_PARTICIPATION = "participation.deposit";


    /**
     * Rule that determines if an <em>act.bankDeposit</em> can be saved.
     * One can be saved if:
     * <ul>
     * <li>it has status 'Deposited' and was previously 'UnDeposited'</li>
     * <li>it has status 'UnDeposited' and there are no other undeposited
     * act.bankDeposits for the deposit account</li>
     * </ul>
     *
     * @param act     the deposit act
     * @param service the archetype service
     * @throws DepositRuleException if the act can't be saved
     */
    public static void checkCanSaveBankDeposit(FinancialAct act,
                                               IArchetypeService service)
            throws DepositRuleException {
        ActBean bean = new ActBean(act);
        if (!bean.isA(BANK_DEPOSIT)) {
            throw new DepositRuleException(InvalidDepositArchetype,
                                           act.getArchetypeId().getShortName());
        }

        Act oldAct = (Act) ArchetypeQueryHelper.getByUid(
                service, act.getArchetypeId(), act.getUid());
        if (oldAct != null) {
            // If the act already exists, make sure it hasn't been deposited
            if (DEPOSITED.equals(oldAct.getStatus())) {
                throw new DepositRuleException(DepositAlreadyDeposited,
                                               act.getUid());
            }
        } else {
            // its a new bank deposit so if status is undeposited
            // check no other undeposited act exists.
            if (UNDEPOSITED.equals(act.getStatus())) {
                Entity account = bean.getParticipant(DEPOSIT_PARTICIPATION);
                if (account == null) {
                    throw new DepositRuleException(MissingAccount,
                                                   act.getUid());
                }

                Act match = DepositHelper.getUndepositedDeposit(account);
                if (match != null) {
                    if (match.getUid() != act.getUid()) {
                        throw new DepositRuleException(UndepositedDepositExists,
                                                       account.getName());
                    }
                }
            }
        }
    }

    /**
     * Processes an <em>act.bankDeposit</em>.
     *
     * @param act     the deposit act
     * @param service the archetype service
     */
    public static void deposit(Act act, IArchetypeService service) {
        ActBean bean = new ActBean(act, service);
        if (!bean.isA(BANK_DEPOSIT)) {
            throw new DepositRuleException(InvalidDepositArchetype,
                                           act.getArchetypeId().getShortName());
        }
        if (DEPOSITED.equals(bean.getStatus())) {
            throw new DepositRuleException(DepositAlreadyDeposited,
                                           bean.getStatus());
        }
        bean.setStatus(DEPOSITED);
        IMObjectReference accountRef = bean.getParticipantRef(
                DEPOSIT_PARTICIPATION);
        if (accountRef == null) {
            throw new DepositRuleException(MissingAccount,
                                           act.getUid());
        }

        bean.save();
        // @todo - need to save in the same transaction when OBF-114 fixed

        IMObject account = ArchetypeQueryHelper.getByObjectReference(
                service, accountRef);
        IMObjectBean accBean = new IMObjectBean(account, service);
        accBean.setValue("lastDeposit", new Date());

        accBean.save();
    }
}
