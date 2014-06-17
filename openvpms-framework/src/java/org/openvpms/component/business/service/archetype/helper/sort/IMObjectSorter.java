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

package org.openvpms.component.business.service.archetype.helper.sort;

import org.apache.commons.collections4.ComparatorUtils;
import org.apache.commons.collections4.FunctorException;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.collections4.comparators.ReverseComparator;
import org.apache.commons.collections4.comparators.TransformingComparator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.DescriptorException;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.NodeSelectConstraint;
import org.openvpms.component.system.common.query.ObjectRefConstraint;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.ObjectSetQueryIterator;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Sorts {@link IMObject}s.
 *
 * @author Tim Anderson
 */
public class IMObjectSorter {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The lookup service.
     */
    private final ILookupService lookups;

    /**
     * Default id comparator.
     */
    private static final Comparator ID_COMPARATOR = new IdComparator();

    /**
     * Default name comparator.
     */
    private static final Comparator NAME_COMPARATOR = new NameComparator();

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(IMObjectSorter.class);


    /**
     * Constructs an {@link IMObjectSorter}.
     *
     * @param service the archetype service
     * @param lookups the lookup service
     */
    public IMObjectSorter(IArchetypeService service, ILookupService lookups) {
        this.service = service;
        this.lookups = lookups;
    }

    /**
     * Sorts objects on a node name, in ascending order.
     *
     * @param list the list to sort. This list is modified
     * @param name the name of the node to sort on
     * @return {@code list}
     */
    public <T extends IMObject> List<T> sort(List<T> list, String name) {
        return sort(list, name, true);
    }

    /**
     * Sorts objects on a node name.
     *
     * @param list the list to sort. This list is modified
     * @param name the name of the node to sort on
     * @return {@code list}
     */
    public <T extends IMObject> List<T> sort(List<T> list, String name, boolean ascending) {
        Comparator<T> comparator;
        if ("id".equals(name)) {
            comparator = getIdComparator();
        } else if ("name".equals(name)) {
            comparator = getNameComparator();
        } else {
            comparator = new TransformingComparator<T, Object>(new NodeTransformer<T>(name));
        }
        if (!ascending) {
            comparator = new ReverseComparator<T>(comparator);
        }
        Collections.sort(list, comparator);
        return list;
    }

    /**
     * Returns a comparator to sort {@link IMObject}s on {@link IMObject#getId()}.
     *
     * @return the comparator
     */
    @SuppressWarnings("unchecked")
    public static <T extends IMObject> Comparator<T> getIdComparator() {
        return (Comparator<T>) ID_COMPARATOR;
    }

    /**
     * Returns a comparator to sort {@link IMObject}s on {@link IMObject#getName()}.
     * <p/>
     * This treats nulls as high.
     *
     * @return the comparator
     */
    @SuppressWarnings("unchecked")
    public static <T extends IMObject> Comparator<T> getNameComparator() {
        return (Comparator<T>) NAME_COMPARATOR;
    }

    private static class IdComparator<T extends IMObject> implements Comparator<T> {

        /**
         * Compares its two arguments for order.  Returns a negative integer, zero, or a positive integer as the first
         * argument is less than, equal to, or greater than the second.
         *
         * @param o1 the first object to be compared.
         * @param o2 the second object to be compared.
         * @return a negative integer, zero, or a positive integer as the first argument is less than, equal to, or
         *         greater than the second.
         * @throws NullPointerException if an argument is null
         */
        @Override
        public int compare(T o1, T o2) {
            // TODO - use Long.compare() from Java 1.7 when demo site updated
            long x = o1.getId();
            long y = o2.getId();
            return (x < y) ? -1 : (x == y) ? 0 : 1;
        }
    }

    private static class NameComparator<T extends IMObject> implements Comparator<T> {

        private final Comparator<String> comparator = ComparatorUtils.nullLowComparator(
                ComparatorUtils.<String>naturalComparator());

        /**
         * Compares its two arguments for order.  Returns a negative integer,
         * zero, or a positive integer as the first argument is less than, equal
         * to, or greater than the second.<p>
         *
         * @param o1 the first object to be compared.
         * @param o2 the second object to be compared.
         * @return a negative integer, zero, or a positive integer as the first argument is less than, equal to, or
         *         greater than the second.
         * @throws NullPointerException if an argument is null
         */
        @Override
        public int compare(T o1, T o2) {
            return comparator.compare(o1.getName(), o2.getName());
        }
    }

    private class NodeTransformer<T extends IMObject> implements Transformer<T, Object> {

        /**
         * The node name.
         */
        private final String node;

        /**
         * Cached archetype descriptor.
         */
        private ArchetypeDescriptor archetype;

        /**
         * Cached node descriptor.
         */
        private NodeDescriptor descriptor;


        /**
         * Constructs a {@link NodeTransformer}.
         *
         * @param node the node name
         */
        public NodeTransformer(String node) {
            this.node = node;
        }

        /**
         * Transforms the input object (leaving it unchanged) into some output object.
         *
         * @param input the object to be transformed, should be left unchanged
         * @return a transformed object
         * @throws FunctorException (runtime) if the transform cannot be completed
         */
        public Object transform(T input) {
            Object result = null;
            if (input != null) {
                NodeDescriptor descriptor = getDescriptor(input);
                if (descriptor != null) {
                    try {
                        if (descriptor.isCollection()) {
                            List<IMObject> objects = descriptor.getChildren(input);
                            if (objects.size() == 1) {
                                result = objects.get(0);
                            }
                        } else if (descriptor.isLookup()) {
                            result = lookups.getName(input, node);
                        } else {
                            result = descriptor.getValue(input);
                        }
                        if (result instanceof Participation) {
                            // sort on participation entity name
                            Participation p = (Participation) result;
                            result = getName(p.getEntity());
                        } else if (!(result instanceof Comparable)) {
                            // not comparable so null to avoid class cast
                            // exceptions
                            result = null;
                        } else if (result instanceof Timestamp) {
                            // convert all Timestamps to Dates to avoid class
                            // cast exceptions comparing dates and timestamps
                            result = new Date(((Timestamp) result).getTime());
                        }
                    } catch (DescriptorException exception) {
                        log.error(exception);
                    }
                }
            }
            return result;
        }

        private NodeDescriptor getDescriptor(IMObject object) {
            if (archetype == null || !archetype.getType().equals(object.getArchetypeId())) {
                archetype = DescriptorHelper.getArchetypeDescriptor(object, service);
                if (archetype == null) {
                    throw new FunctorException(
                            "No archetype descriptor found for object, id=" + object.getId() + ", archetypeId="
                            + object.getArchetypeIdAsString());
                }
                descriptor = archetype.getNodeDescriptor(node);
            }
            return descriptor;
        }
    }

    /**
     * Returns the name of an object, given its reference.
     *
     * @param reference the object reference. May be {@code null}
     * @return the name or {@code null} if none exists
     */
    private String getName(IMObjectReference reference) {
        if (reference != null) {
            ArchetypeQuery query = new ArchetypeQuery(new ObjectRefConstraint("o", reference));
            query.add(new NodeSelectConstraint("o.name"));
            query.setMaxResults(1);
            Iterator<ObjectSet> iter = new ObjectSetQueryIterator(service, query);
            if (iter.hasNext()) {
                ObjectSet set = iter.next();
                return set.getString("o.name");
            }
        }
        return null;
    }
}

