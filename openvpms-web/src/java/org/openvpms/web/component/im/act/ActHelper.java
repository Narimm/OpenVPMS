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

package org.openvpms.web.component.im.act;

import org.openvpms.archetype.rules.act.ActCalculator;
import org.openvpms.archetype.rules.act.FinancialActStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.ShortNameConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.query.ActResultSet;
import org.openvpms.web.component.im.query.ParticipantConstraint;

import java.math.BigDecimal;
import java.util.Collection;


/**
 * Act helper.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ActHelper {

    /**
     * Returns an account balance for a supplier.
     *
     * @param supplier the supplier
     * @return the account balance for <code>supplier</code>
     */
    public static BigDecimal getSupplierAccountBalance(Party supplier) {
        String[] shortNames = {"act.supplierAccountCharges*",
                               "act.supplierAccountPayment"};
        return getAccountBalance(supplier.getObjectReference(), "supplier",
                                 "participation.supplier", shortNames,
                                 "act.supplierAccountOpeningBalance",
                                 "act.supplierAccountClosingBalance");
    }

    /**
     * Returns an account balance for any entity.
     *
     * @param entity             the entity
     * @param participant        the participant node name
     * @param participation      the participation short name
     * @param shortNames         the act short names
     * @param openingBalanceName the opening blance shortname
     */
    public static BigDecimal getAccountBalance(IMObjectReference entity,
                                               String participant,
                                               String participation,
                                               String[] shortNames,
                                               String openingBalanceName,
                                               String closingBalanceName) {
        String[] statuses = {FinancialActStatus.POSTED};
        ShortNameConstraint archetypes = new ShortNameConstraint(
                shortNames, true, true);
        ParticipantConstraint constraint = new ParticipantConstraint(
                participant, participation, entity);
        SortConstraint[] sort = {new NodeSortConstraint("startTime", false)};
        ActResultSet<Act> set = new ActResultSet<Act>(archetypes, constraint,
                                                      null, null, statuses, 50,
                                                      sort);
        set.setNodes(new String[]{"amount"});
        BigDecimal balance = BigDecimal.ZERO;
        // Add up amounts until find first opening balance ignoring first closing balances.
        boolean finished = false;
        while (set.hasNext()) {
            IPage<Act> acts = set.next();
            for (Act act : acts.getResults()) {
                //Ignore first closing balance
                if (act.getArchetypeId().getShortName().equalsIgnoreCase(
                        closingBalanceName))
                    continue;
                BigDecimal amount = getAmount(act, "amount");
                balance = balance.add(amount);
                if (act.getArchetypeId().getShortName().equalsIgnoreCase(
                        openingBalanceName)) {
                    finished = true;
                    break;
                }
            }
            if (finished)
                break;
        }
        return balance;
    }

    /**
     * Sums a node in a list of act items, negating the result if the act
     * is a credit act.
     *
     * @param act  the parent act
     * @param node the node to sum
     * @return the summed total
     */
    public static BigDecimal sum(Act act, String node) {
        ActCalculator calc = new ActCalculator(
                ArchetypeServiceHelper.getArchetypeService());
        return calc.sum(act, node);
    }

    /**
     * Sums a node in a list of acts, negating the result if the act
     * is a credit act.
     *
     * @param act  the parent act
     * @param acts the child acts
     * @param node the node to sum
     * @return the summed total
     */
    public static BigDecimal sum(Act act, Collection<Act> acts,
                                 String node) {
        ActCalculator calc = new ActCalculator(
                ArchetypeServiceHelper.getArchetypeService());
        return calc.sum(act, acts, node);
    }

    /**
     * Returns an amount, taking into account any credit node.
     *
     * @param act  the act
     * @param node the amount node
     * @return the amount corresponding to <code>node</code>
     */
    public static BigDecimal getAmount(Act act, String node) {
        ActCalculator calc = new ActCalculator(
                ArchetypeServiceHelper.getArchetypeService());
        return calc.getAmount(act, node);
    }

    /**
     * Returns the valid short names for the target of a set of act
     * relationships.
     *
     * @param relationshipTypes the relationship types
     */
    public static String[] getTargetShortNames(String ... relationshipTypes) {
        return DescriptorHelper.getNodeShortNames(relationshipTypes, "target");
    }

}
