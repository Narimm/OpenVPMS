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

import org.apache.commons.collections4.ComparatorUtils;
import org.apache.commons.collections4.comparators.ComparatorChain;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.NodeSelectConstraint;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.ObjectSet;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

/**
 * Helper to locate a page that an object falls on for a given {@link ArchetypeQuery}.
 * <p/>
 * This performs a linear search, but only queries as much data as is required to locate the object.
 *
 * @author Tim Anderson
 */
public class PageLocator {

    /**
     * The object to locate.
     */
    private final IMObjectBean bean;

    /**
     * The query.
     */
    private final ArchetypeQuery query;

    /**
     * The page size.
     */
    private final int pageSize;

    /**
     * The keys to compare.
     */
    private final ObjectSet keySet;

    /**
     * The key comparators.
     */
    private ComparatorChain<ObjectSet> comparators = new ComparatorChain<ObjectSet>();

    /**
     * Determines if the comparator chain has been initialised.
     */
    private boolean initialised;

    /**
     * The id comparator.
     */
    private final NodeComparator<Long> ID = new NodeComparator<Long>("a.id", ComparatorUtils.<Long>naturalComparator());


    /**
     * A date comparator.
     */
    public static final Comparator<Date> DATE_COMPARATOR = new Comparator<Date>() {
        @Override
        public int compare(Date o1, Date o2) {
            return DateRules.compareTo(o1, o2);
        }
    };

    /**
     * Constructs a {@link PageLocator}.
     *
     * @param object   the object to locate
     * @param query    the query
     * @param pageSize the page size
     */
    public PageLocator(IMObject object, ArchetypeQuery query, int pageSize) {
        bean = new IMObjectBean(object);
        this.query = query;
        this.pageSize = pageSize;
        query.getArchetypeConstraint().setAlias("a");
        query.add(new NodeSelectConstraint("id"));

        keySet = new ObjectSet();
        keySet.set("a.id", object.getId());
    }

    /**
     * Adds a key to sort on.
     * <p/>
     * This:
     * <ul><li>adds a select constraint for the specified node</li>
     * <li>adds a sort constraint</li>
     * <li>registers a comparator to locate the object within pages</li>
     * </ul>
     * <p/>
     * Note that all queries will be sorted on ascending "id" by default.
     *
     * @param node       the node name
     * @param ascending  determines if the node is sorted in ascending or descending order
     * @param comparator the comparator. If {@code ascending} is {@code false}, it will be reversed
     */
    public <T extends Comparable> void addKey(String node, boolean ascending, Comparator<T> comparator) {
        query.add(new NodeSelectConstraint(node));
        query.add(new NodeSortConstraint(node, ascending));
        if (!ascending) {
            comparator = ComparatorUtils.reversedComparator(comparator);
        }
        comparators.addComparator(new NodeComparator<T>("a." + node, comparator));
        keySet.set("a." + node, bean.getValue(node));
    }

    /**
     * Returns the page that an object would fall on.
     *
     * @return the page that an object would fall on, if present
     */
    public int getPage() {
        if (!initialised) {
            query.add(new NodeSortConstraint("id"));
            comparators.addComparator(ID);
            initialised = true;
        }
        int result = 0;
        ArchetypeQueryResultSet<ObjectSet> set = new ArchetypeQueryResultSet<ObjectSet>(query, pageSize,
                                                                                        new ObjectSetQueryExecutor());

        while (set.hasNext()) {
            IPage<ObjectSet> page = set.next();
            int index = Collections.binarySearch(page.getResults(), keySet, comparators);
            if (index >= 0) {
                break;
            } else {
                index = -index - 1;
                if (index < page.getResults().size()) {
                    // item would be on this page, but is not present
                    break;
                }
            }
            ++result;
        }
        return result;
    }


    private static class NodeComparator<T> implements Comparator<ObjectSet> {

        /**
         * The node name.
         */
        private final String name;

        /**
         * The comparator.
         */
        private final Comparator<T> comparator;

        /**
         * Constructs an {@link NodeComparator}.
         *
         * @param name       the node name
         * @param comparator the comparator
         */
        public NodeComparator(String name, Comparator<T> comparator) {
            this.name = name;
            this.comparator = comparator;
        }

        /**
         * Compares its two arguments for order.  Returns a negative integer,
         * zero, or a positive integer as the first argument is less than, equal
         * to, or greater than the second.<p>
         *
         * @param o1 the first object to be compared.
         * @param o2 the second object to be compared.
         * @return a negative integer, zero, or a positive integer as the
         *         first argument is less than, equal to, or greater than the
         *         second.
         * @throws NullPointerException if an argument is null and this comparator does not permit null arguments
         * @throws ClassCastException   if the arguments' types prevent them from being compared by this comparator.
         */
        @SuppressWarnings("unchecked")
        @Override
        public int compare(ObjectSet o1, ObjectSet o2) {
            T v1 = (T) o1.get(name);
            T v2 = (T) o2.get(name);
            return comparator.compare(v1, v2);
        }
    }

}
