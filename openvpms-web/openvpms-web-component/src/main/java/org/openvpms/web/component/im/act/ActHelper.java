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

package org.openvpms.web.component.im.act;

import org.apache.commons.collections4.comparators.ReverseComparator;
import org.openvpms.archetype.rules.act.ActCalculator;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.web.system.ServiceHelper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Act helper.
 *
 * @author Tim Anderson
 */
public class ActHelper {

    /**
     * Comparator to order acts on start time, oldest first.
     */
    public static final Comparator<Act> ASCENDING = new Comparator<Act>() {
        @Override
        public int compare(Act o1, Act o2) {
            int result = DateRules.compareTo(o1.getActivityStartTime(), o2.getActivityStartTime());
            if (result == 0) {
                result = o1.getId() < o2.getId() ? -1 : ((o1.getId() == o2.getId()) ? 0 : 1);
            }
            return result;
        }
    };

    /**
     * Comparator to order acts on start time, most recent first.
     */
    public static final ReverseComparator<Act> DESCENDING = new ReverseComparator<Act>(ASCENDING);

    /**
     * Returns a comparator to sort acts on ascending start time.
     *
     * @return the comparator
     */
    @SuppressWarnings("unchecked")
    public static <T extends Act> Comparator<T> ascending() {
        return (Comparator<T>) ASCENDING;
    }

    /**
     * Returns a comparator to sort acts on descending start time.
     *
     * @return the comparator
     */
    @SuppressWarnings("unchecked")
    public static <T extends Act> Comparator<T> descending() {
        return (Comparator<T>) DESCENDING;
    }

    /**
     * Sorts acts on ascending timestamp.
     *
     * @param acts the acts to sort. Note: this list is modified
     * @return the sorted acts
     */
    public static <T extends Act> List<T> sort(List<T> acts) {
        Collections.sort(acts, ascending());
        return acts;
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

    /**
     * Returns acts given their references in a map.
     * <p/>
     * This uses a single archetype query, to improve performance.
     *
     * @param references the act references
     * @return the associated acts
     */
    public static Map<IMObjectReference, Act> getActMap(Collection<IMObjectReference> references) {
        Map<IMObjectReference, Act> result = new HashMap<IMObjectReference, Act>();
        for (Act act : getActs(references)) {
            result.put(act.getObjectReference(), act);
        }
        return result;
    }

    /**
     * Creates a sorted list of identifiers suitable for use in an {@link Constraints#in} constraint.
     *
     * @param references the object references. Must refer to the same base type
     * @return a sorted list of identifiers
     */
    public static Long[] getIds(List<IMObjectReference> references) {
        Long[] result = new Long[references.size()];
        for (int i = 0; i < references.size(); ++i) {
            result[i] = references.get(i).getId();
        }
        Arrays.sort(result);
        return result;
    }

}
