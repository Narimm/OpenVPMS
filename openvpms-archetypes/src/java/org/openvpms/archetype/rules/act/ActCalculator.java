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

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.math.BigDecimal;
import java.util.Collection;
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
    private final IArchetypeService _service;


    /**
     * Construct a new <code>ActCalculator</code>.
     *
     * @param service the archetype service
     */
    public ActCalculator(IArchetypeService service) {
        _service = service;
    }

    /**
     * Suma a node in a list of act items.
     *
     * @param act  the parent act
     * @param node the node to sum
     * @return the summed total
     * @throws ArchetypeServiceException for any archetype service error
     */
    public BigDecimal sum(Act act, String node) {
        ActBean bean = new ActBean(act, _service);
        List<Act> acts = bean.getActs();
        return sum(acts, node);
    }

    /**
     * Sums a node in a list of acts.
     *
     * @param acts the acts
     * @param node the node to sum
     * @return the summed total
     * @throws ArchetypeServiceException for any archetype service error
     */
    public BigDecimal sum(Collection<Act> acts, String node) {
        return sum(BigDecimal.ZERO, acts, node);
    }

    /**
     * Sums a node in a list of acts.
     *
     * @param initial the initial value
     * @param acts    the acts
     * @param node    the node to sum
     * @return the summed total
     * @throws ArchetypeServiceException for any archetype service error
     */
    public BigDecimal sum(BigDecimal initial, Collection<Act> acts,
                          String node) {
        BigDecimal result = initial;
        for (Act act : acts) {
            BigDecimal amount = getAmount(act, node);
            result = result.add(amount);
        }
        return result;
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
        IMObjectBean bean = new IMObjectBean(act, _service);
        BigDecimal result = BigDecimal.ZERO;
        if (bean.hasNode(node)) {
            BigDecimal value = bean.getBigDecimal(node);
            if (value != null) {
                boolean credit = false;
                if (bean.hasNode("credit")) {
                    credit = bean.getBoolean("credit");
                }
                if (credit) {
                    result = result.subtract(value);
                } else {
                    result = result.add(value);
                }
            }
        }

        return result;
    }

}
