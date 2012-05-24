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

package org.openvpms.archetype.rules.act;

import org.openvpms.archetype.rules.math.MathRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


/**
 * Act calculations.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ActCalculator {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The scale of the returned values, or <tt>0</tt> to indicate no scale.
     */
    private final int scale;


    /**
     * Construct a new <code>ActCalculator</code>.
     *
     * @param service the archetype service
     */
    public ActCalculator(IArchetypeService service) {
        this(service, 2);
    }

    /**
     * Construct a new <code>ActCalculator</code>.
     *
     * @param service the archetype service
     * @param scale   the scale of the returned values. Use <tt>0</tt> to
     *                indicate no scale
     */
    public ActCalculator(IArchetypeService service, int scale) {
        this.service = service;
        this.scale = scale;
    }

    /**
     * Sums a node in a list of act items, negating the result if the act
     * is a credit act.
     *
     * @param act  the parent act
     * @param node the node to sum
     * @return the summed total
     * @throws ArchetypeServiceException for any archetype service error
     */
    public BigDecimal sum(Act act, String node) {
        ActBean bean = new ActBean(act, service);
        List<Act> acts = bean.getActs();

        return sum(act, acts, node);
    }

    /**
     * Sums a node in a list of acts, negating the result if the act
     * is a credit act.
     *
     * @param act  the parent act
     * @param acts the act items
     * @param node the node to sum
     * @return the summed total
     * @throws ArchetypeServiceException for any archetype service error
     */
    public <T extends Act> BigDecimal sum(Act act, Collection<T> acts, String node) {
        BigDecimal result = sum(acts, node);
        if (isCredit(act)) {
            result = result.negate();
        }
        return result;
    }

    /**
     * Sums a node in a collection of acts, taking into account any credit node.
     *
     * @param acts an iterator over the act collection
     * @param node the node to sum
     * @return the summed total
     * @throws ArchetypeServiceException for any archetype service error
     */
    public <T extends Act> BigDecimal sum(Iterator<T> acts, String node) {
        BigDecimal result = BigDecimal.ZERO;
        while (acts.hasNext()) {
            Act act = acts.next();
            BigDecimal amount = getAmount(act, node);
            result = result.add(amount);
        }
        return round(result);
    }

    /**
     * Returns an amount, taking into account any credit node.
     *
     * @param act  the act
     * @param node the amount node
     * @return the amount corresponding to <code>node</code>
     * @throws ArchetypeServiceException for any archetype service error
     */
    public BigDecimal getAmount(Act act, String node) {
        IMObjectBean bean = new IMObjectBean(act, service);
        BigDecimal result = BigDecimal.ZERO;
        if (bean.hasNode(node)) {
            BigDecimal value = bean.getBigDecimal(node);
            if (value != null) {
                result = addAmount(result, value, isCredit(bean));
            }
        }

        return result;
    }

    /**
     * Returns the act total, taking into account the credit flag.
     *
     * @param act the act
     * @return the total, negated if it is a credit act
     */
    public BigDecimal getTotal(FinancialAct act) {
        BigDecimal result = act.getTotal();
        if (result == null) {
            result = BigDecimal.ZERO;
        } else if (act.isCredit()) {
            result = result.negate();
        }
        return result;
    }

    /**
     * Adds an amount to a total.
     *
     * @param total    the total
     * @param amount   the amount to add
     * @param isCredit if <code>true</code> the amount is a credit, otherwise
     *                 its a debit
     */
    public BigDecimal addAmount(BigDecimal total, BigDecimal amount,
                                boolean isCredit) {
        BigDecimal result = (isCredit) ? total.subtract(amount) : total.add(
                amount);
        return round(result);
    }

    /**
     * Determines if an act is a credit or a debit.
     *
     * @param act the act
     * @return <code>true</code> if the act has node <code>'credit'=true</code>;
     *         otherwise <code>false</code>
     * @throws ArchetypeServiceException for any archetype service error
     */
    public boolean isCredit(Act act) {
        IMObjectBean bean = new IMObjectBean(act, service);
        return isCredit(bean);
    }

    /**
     * Sums a node in a collection of acts.
     *
     * @param acts the acts
     * @param node the node to sum
     * @return the summed total
     * @throws ArchetypeServiceException for any archetype service error
     */
    private <T extends Act> BigDecimal sum(Collection<T> acts, String node) {
        return sum(acts.iterator(), node);
    }

    /**
     * Determines if an act is a credit or a debit.
     *
     * @param bean the act bean
     * @return <code>true</code> if the act has node <code>'credit'=true</code>;
     *         otherwise <code>false</code>
     * @throws ArchetypeServiceException for any archetype service error
     */
    private boolean isCredit(IMObjectBean bean) {
        return bean.hasNode("credit") && bean.getBoolean("credit");
    }

    /**
     * Rounds an amount to the scale supplied at construction, if it is
     * non-zero.
     *
     * @param amount the amount to round
     * @return the rounded amount
     */
    private BigDecimal round(BigDecimal amount) {
        if (scale != 0) {
            return MathRules.round(amount, scale);
        }
        return amount;
    }

}
