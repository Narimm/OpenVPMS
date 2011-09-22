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

package org.openvpms.web.component.im.util;

import org.apache.commons.collections.ComparatorUtils;
import org.apache.commons.collections.FunctorException;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.comparators.ComparatorChain;
import org.apache.commons.collections.comparators.TransformingComparator;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.DescriptorException;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.LookupHelper;
import org.openvpms.component.system.common.query.ArchetypeSortConstraint;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.system.ServiceHelper;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


/**
 * Helper routines for sorting {@link IMObject}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 * @see VirtualNodeSortConstraint
 */
public class IMObjectSorter {

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(IMObjectSorter.class);

    /**
     * Sorts a collection of objects.
     *
     * @param objects   the objects
     * @param node      the node name
     * @param ascending if <tt>true</tt> sort ascending, otherwise sort descending
     */
    public static <T extends IMObject> void sort(List<T> objects, String node, boolean ascending) {
        sort(objects, new NodeSortConstraint(node, ascending));
    }

    /**
     * Sorts a collection of objects.
     *
     * @param objects the objects to sort.
     * @param sort    the sort criteria
     */
    @SuppressWarnings("unchecked")
    public static <T extends IMObject> void sort(List<T> objects, SortConstraint... sort) {
        ComparatorChain comparator = new ComparatorChain();
        for (SortConstraint constraint : sort) {
            if (constraint instanceof VirtualNodeSortConstraint) {
                Comparator node = getComparator((VirtualNodeSortConstraint) constraint);
                comparator.addComparator(node);
            } else if (constraint instanceof NodeSortConstraint) {
                Comparator node = getComparator((NodeSortConstraint) constraint);
                comparator.addComparator(node);
            } else if (constraint instanceof ArchetypeSortConstraint) {
                Comparator type = getComparator((ArchetypeSortConstraint) constraint);
                comparator.addComparator(type);
                break;
            }
        }
        comparator.addComparator(IdentityComparator.INSTANCE); // ensure re-runs return the same ordering
        Collections.sort(objects, comparator);
    }

    /**
     * Sort a collection of objects.
     *
     * @param objects     the objects to sort
     * @param sort        the sort criteria
     * @param transformer a transformer to return the underlying IMObject.
     */
    @SuppressWarnings("unchecked")
    public static <T> void sort(List<T> objects, SortConstraint[] sort, Transformer transformer) {
        ComparatorChain comparator = new ComparatorChain();
        for (SortConstraint constraint : sort) {
            if (constraint instanceof VirtualNodeSortConstraint) {
                Comparator node = getComparator((VirtualNodeSortConstraint) constraint);
                comparator.addComparator(node);
            } else if (constraint instanceof NodeSortConstraint) {
                Comparator node = getComparator((NodeSortConstraint) constraint, transformer);
                comparator.addComparator(node);
            } else if (constraint instanceof ArchetypeSortConstraint) {
                Comparator type = getComparator((ArchetypeSortConstraint) constraint, transformer);
                comparator.addComparator(type);
                break;
            }
        }
        comparator.addComparator(IdentityComparator.INSTANCE);
        Collections.sort(objects, comparator);
    }

    /**
     * Returns a comparator that sorts objects on name.
     * <p/>
     * This comparator handles nulls.
     *
     * @param ascending if <tt>true</tt> sort in ascending order; otherwise sort in descending order
     * @return a new comparator
     */
    @SuppressWarnings("unchecked")
    public static <T extends IMObject> Comparator<T> getNameComparator(boolean ascending) {
        final Comparator comparator = getComparator(ascending);
        return new Comparator<T>() {
            public int compare(T o1, T o2) {
                return comparator.compare(o1.getName(), o2.getName());
            }
        };
    }

    /**
     * Returns a new comparator to sort in ascending or descending order.
     * <p/>
     * This comparator handles nulls.
     *
     * @param ascending if <tt>true</tt> sort in ascending order; otherwise sort in descending order
     * @return a new comparator
     */
    public static Comparator getComparator(boolean ascending) {
        Comparator comparator = ComparatorUtils.naturalComparator();

        // handle nulls.
        comparator = ComparatorUtils.nullLowComparator(comparator);
        if (!ascending) {
            comparator = ComparatorUtils.reversedComparator(comparator);
        }
        return comparator;
    }

    /**
     * Returns a new transformer for a node sort constraint.
     *
     * @param sort the sort constraint
     * @return a new transformer
     */
    private static Transformer getTransformer(NodeSortConstraint sort) {
        return new NodeTransformer(sort.getNodeName());
    }

    /**
     * Returns a new comparator for a virtual node sort constraint.
     *
     * @param sort the sort criteria
     * @return a new comparator
     */
    @SuppressWarnings("unchecked")
    private static Comparator<Object> getComparator(VirtualNodeSortConstraint sort) {
        Comparator comparator = getComparator(sort.isAscending());
        Transformer transformer = sort.getTransformer();
        if (transformer == null) {
            transformer = getTransformer(sort);
        }
        return new TransformingComparator(transformer, comparator);
    }

    /**
     * Returns a new comparator for a node sort constraint.
     *
     * @param sort the sort criteria
     * @return a new comparator
     */
    @SuppressWarnings("unchecked")
    private static Comparator<Object> getComparator(NodeSortConstraint sort) {
        Comparator comparator = getComparator(sort.isAscending());
        Transformer transformer = getTransformer(sort);
        return new TransformingComparator(transformer, comparator);
    }

    /**
     * Returns a new comparator for a node sort constraint.
     *
     * @param sort        the sort criteria
     * @param transformer a transformer to apply
     * @return a new comparator
     */
    @SuppressWarnings("unchecked")
    private static Comparator<Object> getComparator(NodeSortConstraint sort, Transformer transformer) {
        Comparator comparator = getComparator(sort.isAscending());
        Transformer transform = ChainedTransformer.getInstance(transformer, getTransformer(sort));
        return new TransformingComparator(transform, comparator);
    }


    /**
     * Returns a new comparator for an archetype property.
     *
     * @param sort the sort criteria
     * @return a new comparator
     */
    @SuppressWarnings("unchecked")
    private static Comparator<Object> getComparator(ArchetypeSortConstraint sort) {
        Comparator comparator = getComparator(sort.isAscending());
        Transformer transformer = new ArchetypeTransformer();
        return new TransformingComparator(transformer, comparator);
    }

    /**
     * Returns a new comparator for an archetype property.
     *
     * @param sort        the sort criteria
     * @param transformer a transformer to apply
     * @return a new comparator
     */
    @SuppressWarnings("unchecked")
    private static Comparator<Object> getComparator(ArchetypeSortConstraint sort, Transformer transformer) {
        Comparator comparator = getComparator(sort.isAscending());
        Transformer transform = ChainedTransformer.getInstance(transformer, new ArchetypeTransformer());
        return new TransformingComparator(transform, comparator);
    }

    private static class NodeTransformer implements Transformer {

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
         * Construct a new <tt>NodeTransformer</tt>.
         *
         * @param node the node name
         */
        public NodeTransformer(String node) {
            this.node = node;
        }

        /**
         * Transforms the input object (leaving it unchanged) into some output
         * object.
         *
         * @param input the object to be transformed, should be left unchanged
         * @return a transformed object
         * @throws ClassCastException       (runtime) if the input is the wrong
         *                                  class
         * @throws IllegalArgumentException (runtime) if the input is invalid
         * @throws FunctorException         (runtime) if the transform cannot be
         *                                  completed
         */
        public Object transform(Object input) {
            Object result = null;
            if (input != null) {
                IMObject object = (IMObject) input;
                NodeDescriptor descriptor = getDescriptor(object);
                if (descriptor != null) {
                    try {
                        if (descriptor.isCollection()) {
                            List<IMObject> objects = descriptor.getChildren(object);
                            if (objects.size() == 1) {
                                result = objects.get(0);
                            }
                        } else if (descriptor.isLookup()) {
                            Lookup lookup = LookupHelper.getLookup(ServiceHelper.getArchetypeService(), descriptor,
                                                                   object);
                            if (lookup != null) {
                                result = lookup.getName();
                            }
                        } else {
                            result = descriptor.getValue(object);
                        }
                        if (result instanceof Participation) {
                            // sort on participation entity name
                            Participation p = (Participation) result;
                            result = IMObjectHelper.getName(p.getEntity());
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
                archetype = DescriptorHelper.getArchetypeDescriptor(object);
                descriptor = archetype.getNodeDescriptor(node);
            }
            return descriptor;
        }

    }

    private static class ArchetypeTransformer implements Transformer {

        /**
         * Transforms the input object (leaving it unchanged) into some output
         * object.
         *
         * @param input the object to be transformed, should be left unchanged
         * @return a transformed object
         * @throws ClassCastException       (runtime) if the input is the wrong
         *                                  class
         * @throws IllegalArgumentException (runtime) if the input is invalid
         * @throws FunctorException         (runtime) if the transform cannot be
         *                                  completed
         */
        public Object transform(Object input) {
            if (input != null) {
                IMObject object = (IMObject) input;
                return object.getArchetypeId().getShortName();
            }
            return null;
        }
    }

    /**
     * Helper to compare two objects based on their in-memory identity.
     */
    private static class IdentityComparator implements Comparator {

        private static Comparator INSTANCE = new IdentityComparator();

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
         */
        public int compare(Object o1, Object o2) {
            int hash1 = System.identityHashCode(o1);
            int hash2 = System.identityHashCode(o2);
            return hash1 - hash2;
        }
    }

}
