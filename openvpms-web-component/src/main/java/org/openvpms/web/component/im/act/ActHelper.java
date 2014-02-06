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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.act;

import org.openvpms.archetype.rules.act.ActCalculator;
import org.openvpms.archetype.rules.act.FinancialActStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.ShortNameConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.query.ActResultSet;
import org.openvpms.web.component.im.query.ParticipantConstraint;
import org.openvpms.web.system.ServiceHelper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Act helper.
 *
 * @author Tim Anderson
 */
public class ActHelper {

    /**
     * Returns an account balance for a supplier.
     *
     * @param supplier the supplier
     * @return the account balance for {@code supplier}
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
                        closingBalanceName)) {
                    continue;
                }
                BigDecimal amount = getAmount(act, "amount");
                balance = balance.add(amount);
                if (act.getArchetypeId().getShortName().equalsIgnoreCase(
                        openingBalanceName)) {
                    finished = true;
                    break;
                }
            }
            if (finished) {
                break;
            }
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
    public static <T extends Act> BigDecimal sum(Act act, Collection<T> acts, String node) {
        ActCalculator calc = new ActCalculator(
                ArchetypeServiceHelper.getArchetypeService());
        return calc.sum(act, acts, node);
    }

    /**
     * Returns an amount, taking into account any credit node.
     *
     * @param act  the act
     * @param node the amount node
     * @return the amount corresponding to {@code node}
     */
    public static BigDecimal getAmount(Act act, String node) {
        ActCalculator calc = new ActCalculator(
                ArchetypeServiceHelper.getArchetypeService());
        return calc.getAmount(act, node);
    }

    /**
     * Returns the target acts in a list of relationships.
     * <p/>
     * This uses a single archetype query, to improve performance.
     *
     * @param relationships the relationships
     * @return the target acts in the relationships
     */
    public static List<Act> getTargetActs(Collection<ActRelationship> relationships) {
        List<IMObjectReference> refs = new ArrayList<IMObjectReference>();
        for (ActRelationship relationship : relationships) {
            IMObjectReference target = relationship.getTarget();
            if (target != null) {
                refs.add(target);
            }
        }

        return getActs(refs);
    }

    /**
     * Returns acts given their references
     * <p/>
     * This uses a single archetype query, to improve performance.
     *
     * @param references the act references
     * @return the associated acts
     */
    public static List<Act> getActs(Collection<IMObjectReference> references) {
        List<Act> result = new ArrayList<Act>();
        if (!references.isEmpty()) {
            Set<String> shortNames = new HashSet<String>();
            List<Long> ids = new ArrayList<Long>();
            for (IMObjectReference ref : references) {
                ids.add(ref.getId());
                shortNames.add(ref.getArchetypeId().getShortName());
            }
            ArchetypeQuery query = new ArchetypeQuery(shortNames.toArray(new String[shortNames.size()]), false, false);
            query.setMaxResults(ArchetypeQuery.ALL_RESULTS);
            Collections.sort(ids);
            query.add(Constraints.in("id", ids.toArray()));
            for (IMObject match : ServiceHelper.getArchetypeService().get(query).getResults()) {
                result.add((Act) match);
            }
        }
        return result;
    }

}
