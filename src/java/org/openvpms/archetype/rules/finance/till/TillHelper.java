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
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.QueryIterator;

import java.math.BigDecimal;

import static org.openvpms.archetype.rules.finance.till.TillArchetypes.TILL_BALANCE;
import static org.openvpms.archetype.rules.finance.till.TillArchetypes.TILL_PARTICIPATION;
import static org.openvpms.archetype.rules.finance.till.TillRuleException.ErrorCode.MissingTill;
import static org.openvpms.component.system.common.query.Constraints.eq;
import static org.openvpms.component.system.common.query.Constraints.join;

/**
 * Till helper methods.
 *
 * @author Tim Anderson
 */
class TillHelper {

    /**
     * Helper to create a new till balance, associating it with a till.
     *
     * @param till the till
     * @return a new till balance
     */
    public static FinancialAct createTillBalance(Entity till, IArchetypeService service) {
        FinancialAct act = (FinancialAct) service.create(TILL_BALANCE);
        ActBean bean = new ActBean(act, service);
        bean.setStatus(TillBalanceStatus.UNCLEARED);
        bean.setParticipant(TILL_PARTICIPATION, till);

        // Get the till and extract last cash float amount to set balance cash float.
        IMObjectBean tillBean = new IMObjectBean(till, service);
        BigDecimal tillCashFloat = tillBean.getBigDecimal("tillFloat", BigDecimal.ZERO);
        bean.setValue("cashFloat", tillCashFloat);
        return act;
    }

    /**
     * Helper to return the reference to a till associated with an act.
     *
     * @param act the act
     * @return the till reference
     * @throws TillRuleException if no till is found
     */
    public static IMObjectReference getTillRef(Act act, IArchetypeService service) {
        ActBean bean = new ActBean(act, service);
        IMObjectReference ref = bean.getNodeParticipantRef("till");
        if (ref == null) {
            throw new TillRuleException(MissingTill, act.getId());
        }
        return ref;
    }

    /**
     * Helper to return the till associated with an act.
     *
     * @param act the act
     * @return the till
     * @throws TillRuleException if no till is found
     */
    public static Entity getTill(Act act, IArchetypeService service) {
        Entity till = (Entity) service.get(getTillRef(act, service));
        if (till == null) {
            throw new TillRuleException(MissingTill, act.getId());
        }
        return till;
    }

    /**
     * Updates the amount of an <em>act.tillBalance</em>.
     *
     * @param bean    the balance bean
     * @param service the archetype service
     * @return {@code true} if the balance changed
     */
    public static boolean updateBalance(ActBean bean, IArchetypeService service) {
        boolean changed = false;
        ActCalculator calc = new ActCalculator(service);
        BigDecimal current = bean.getBigDecimal("amount", BigDecimal.ZERO);
        BigDecimal total = calc.sum(bean.getAct(), "amount");
        if (current.compareTo(total) != 0) {
            bean.setValue("amount", total);
            changed = true;
        }
        return changed;
    }

    /**
     * Helper to return the uncleared till balance for a till, if it exists.
     *
     * @param till the till
     * @return the uncleared till balance, or {@code null} if none exists
     */
    public static FinancialAct getUnclearedTillBalance(Entity till, IArchetypeService service) {
        return getUnclearedTillBalance(till.getObjectReference(), service);
    }

    /**
     * Helper to return the uncleared till balance for a till, if it exists.
     *
     * @param till a reference to the till
     * @return the uncleared till balance, or {@code null} if none exists
     */
    public static FinancialAct getUnclearedTillBalance(IMObjectReference till, IArchetypeService service) {
        ArchetypeQuery query = new ArchetypeQuery(TILL_BALANCE, false, true);
        query.add(eq("status", TillBalanceStatus.UNCLEARED)).add(join("till").add(eq("entity", till)));
        QueryIterator<FinancialAct> iterator = new IMObjectQueryIterator<FinancialAct>(service, query);
        return (iterator.hasNext()) ? iterator.next() : null;
    }

}
