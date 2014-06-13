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

package org.openvpms.web.component.im.query;

import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.BaseArchetypeConstraint;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IConstraint;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.JoinConstraint;
import org.openvpms.component.system.common.query.ShortNameConstraint;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


/**
 * Query helper.
 *
 * @author Tim Anderson
 */
public class QueryHelper {

    /**
     * Determines if a node is a participation node.
     *
     * @param descriptor the node descriptor
     * @return {@code true} if the node is a participation node
     */
    public static boolean isParticipationNode(NodeDescriptor descriptor) {
        return descriptor.isCollection() && "/participations".equals(descriptor.getPath());
    }

    /**
     * Helper to return the descriptor referred to by a constraint.
     *
     * @param archetypes the archetype constraint
     * @param node       the node name
     * @return the corresponding descriptor or {@code null}
     */
    public static NodeDescriptor getDescriptor(ShortNameConstraint archetypes,
                                               String node) {
        String[] shortNames = archetypes.getShortNames();
        if (shortNames.length > 0) {
            ArchetypeDescriptor archetype
                    = DescriptorHelper.getArchetypeDescriptor(shortNames[0]);
            if (archetype != null) {
                return archetype.getNodeDescriptor(node);
            }
        }
        return null;
    }

    /**
     * Adds a sort constraint on a participation node.
     *
     * @param acts       the act short names constraint. Must specify an alias
     * @param query      the query. Must reference {@code acts}
     * @param descriptor the participation node descriptor
     * @param ascending  if {@code true} sort ascending
     */
    public static void addSortOnParticipation(ShortNameConstraint acts,
                                              ArchetypeQuery query,
                                              NodeDescriptor descriptor,
                                              boolean ascending) {
        JoinConstraint particJoin = Constraints.leftJoin(descriptor.getName(), getAlias(descriptor.getName(), query));
        JoinConstraint entityJoin = Constraints.leftJoin("entity", getAlias("entity", query));

        particJoin.add(entityJoin);
        acts.add(particJoin);
        query.add(Constraints.sort(entityJoin.getAlias(), "name", ascending));
    }

    /**
     * Determines if a result set selects an object, using a linear search.
     *
     * @param set       the query
     * @param reference the object reference to check
     * @return {@code true} if the query selects the reference; otherwise {@code false}
     */
    public static <T extends IMObject> boolean selects(ResultSet<T> set, IMObjectReference reference) {
        boolean result = false;
        Iterator<T> iter = new ResultSetIterator<T>(set);
        while (iter.hasNext()) {
            if (iter.next().getObjectReference().equals(reference)) {
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * Returns all objects matching the specified query in a list.
     *
     * @param query the query
     * @return the matching objects
     */
    public static <T extends IMObject> List<T> query(ArchetypeQuery query) {
        Iterator<T> iterator = new IMObjectQueryIterator<T>(query);
        List<T> matches = new ArrayList<T>();
        while (iterator.hasNext()) {
            matches.add(iterator.next());
        }
        return matches;
    }

    /**
     * Returns all objects matching the specified short names, sorted on the specified nodes.
     *
     * @param shortNames the archetype short names
     * @param sortNodes  the sort nodes
     * @return the matching objects
     */
    public static <T extends IMObject> List<T> query(String[] shortNames, String... sortNodes) {
        ArchetypeQuery query = new ArchetypeQuery(shortNames, false, true);
        for (String sort : sortNodes) {
            query.add(Constraints.sort(sort));
        }
        return query(query);
    }

    /**
     * Helper to create a date range constraint for acts, on a particular date.
     *
     * @param date the date
     * @return a new constraint
     */
    public static IConstraint createDateRangeConstraint(Date date) {
        return Constraints.and(Constraints.lte("startTime", date),
                               Constraints.or(Constraints.gte("endTime", date), Constraints.isNull("endTime")));
    }

    /**
     * Helper to create a date range constraint for acts of the form:<br/>
     * {@code act.startTime <= from && (act.endTime >= from || act.endTime == null)}
     *
     * @param from the from date
     * @param to   the to date. May be {@code null}
     * @return a new constraint
     */
    public static IConstraint createDateRangeConstraint(Date from, Date to) {
        IConstraint result;
        if (to != null) {
            result = Constraints.or(createDateRangeConstraint(from), createDateRangeConstraint(to));
        } else {
            result = Constraints.or(Constraints.gte("startTime", from), createDateRangeConstraint(from));
        }
        return result;
    }

    /**
     * Helper to create a constraint on a date node.
     * <p/>
     * NOTE: any time component is stripped from the date.
     * <p/>
     * If:
     * <ul>
     * <li>{@code from} and {@code to} are {@code null} no constraint is created</li>
     * <li>{@code from} is non-null and {@code to} is {@code null}, a constraint {@code startTime >= from}
     * is returned
     * <li>{@code from} is null and {@code to} is {@code null}, a constraint {@code startTime <= to}
     * is returned
     * <li>{@code from} is non-null and {@code to} is {@code non-null}, a constraint
     * {@code startTime >= from && startTime <= to} is returned
     * </ul>
     *
     * @param from the act from date. May be {@code null}
     * @param to   the act to date, inclusive. May be {@code null}
     * @return a new constraint, or {@code null} if both dates are null
     */
    public static IConstraint createDateConstraint(String node, Date from, Date to) {
        IConstraint result;
        if (from == null && to == null) {
            result = null;
        } else if (from != null && to == null) {
            from = DateRules.getDate(from);
            result = Constraints.gte(node, from);
        } else if (from == null) {
            to = DateRules.getNextDate(to);
            result = Constraints.lt(node, to);
        } else {
            from = DateRules.getDate(from);
            to = DateRules.getNextDate(to);
            result = Constraints.and(Constraints.gte(node, from), Constraints.lt(node, to));
        }
        return result;
    }

    /**
     * Determines if a node is an entityLink node.
     *
     * @param descriptor the node descriptor
     * @return {@code true} if the node is a participation node
     */
    public static boolean isEntityLinkNode(NodeDescriptor descriptor) {
        return descriptor.isCollection() && "/entityLinks".equals(descriptor.getPath());
    }

    /**
     * Adds a sort constraint on an entityLink node.
     *
     * @param acts       the act short names constraint. Must specify an alias
     * @param query      the query. Must reference {@code acts}
     * @param descriptor the participation node descriptor
     * @param ascending  if {@code true} sort ascending
     */
    public static void addSortOnEntityLink(ShortNameConstraint acts, ArchetypeQuery query, NodeDescriptor descriptor,
                                           boolean ascending) {
        JoinConstraint linkJoin = Constraints.leftJoin(descriptor.getName(), getAlias(descriptor.getName(), query));
        JoinConstraint targetJoin = Constraints.leftJoin("target", getAlias("target", query));
        linkJoin.add(targetJoin);
        acts.add(linkJoin);
        query.add(Constraints.sort(targetJoin.getAlias(), "name", ascending));
    }

    /**
     * Returns a unique alias for an entity constraint.
     *
     * @param prefix the alias prefix
     * @param query  the archetype query
     * @return the alias
     */
    private static String getAlias(String prefix, ArchetypeQuery query) {
        return prefix + getAliasSuffix(prefix, query.getArchetypeConstraint(), 0);
    }

    private static int getAliasSuffix(String prefix, IConstraint constraint, int maxId) {
        if (constraint instanceof BaseArchetypeConstraint) {
            BaseArchetypeConstraint arch = (BaseArchetypeConstraint) constraint;
            String alias = arch.getAlias();
            if (alias != null && alias.startsWith(prefix)) {
                String suffix = alias.substring(prefix.length());
                if (suffix != null) {
                    try {
                        int id = Integer.valueOf(suffix);
                        if (id > maxId) {
                            maxId = id + 1;
                        }
                    } catch (NumberFormatException ignore) {
                        // do nothing
                    }
                }
                for (IConstraint child : arch.getConstraints()) {
                    maxId = getAliasSuffix(prefix, child, maxId);
                }
            }
        }
        return maxId;
    }

}
