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
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.query.ArchetypeSortConstraint;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.SortConstraint;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * Helper routines for sorting {@link IMObject}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class IMObjectSorter {

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(IMObjectSorter.class);


    /**
     * Sorts a collection of objects.
     *
     * @param objects the objects to sort.
     * @param sort    the sort criteria
     */
    public static <T extends IMObject> void sort(List<T> objects,
                                                 SortConstraint[] sort) {
        ComparatorChain comparator = new ComparatorChain();
        for (SortConstraint constraint : sort) {
            if (constraint instanceof NodeSortConstraint) {
                Comparator node
                        = getComparator((NodeSortConstraint) constraint);
                comparator.addComparator(node);
            } else if (constraint instanceof ArchetypeSortConstraint) {
                Comparator type =
                        getComparator((ArchetypeSortConstraint) constraint);
                comparator.addComparator(type);
                break;
            }
        }
        Collections.sort(objects, comparator);
    }

    /**
     * Sort a collection of objects.
     *
     * @param objects     the objects to sort
     * @param sort        the sort criteria
     * @param transformer a transformer to return the underlying IMObject.
     */
    public static <T extends IMObject> void sort(
            List<T> objects, SortConstraint[] sort, Transformer transformer) {
        ComparatorChain comparator = new ComparatorChain();
        for (SortConstraint constraint : sort) {
            if (constraint instanceof NodeSortConstraint) {
                Comparator node = getComparator(
                        (NodeSortConstraint) constraint, transformer);
                comparator.addComparator(node);
            } else if (constraint instanceof ArchetypeSortConstraint) {
                Comparator type =
                        getComparator((ArchetypeSortConstraint) constraint,
                                      transformer);
                comparator.addComparator(type);
                break;
            }
        }
        Collections.sort(objects, comparator);
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
     * Returns a new comparator for a node sort constraint.
     *
     * @param sort the sort criteria
     * @return a new comparator
     */
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
    private static Comparator<Object> getComparator(NodeSortConstraint sort,
                                                    Transformer transformer) {
        Comparator comparator = getComparator(sort.isAscending());
        Transformer transform = ChainedTransformer.getInstance(
                transformer, getTransformer(sort));
        return new TransformingComparator(transform, comparator);
    }


    /**
     * Returns a new comparator for an archetype property.
     *
     * @param sort the sort criteria
     */
    private static Comparator<Object> getComparator(
            ArchetypeSortConstraint sort) {
        Comparator comparator = getComparator(sort.isAscending());
        Transformer transformer = new ArchetypeTransformer();
        return new TransformingComparator(transformer, comparator);
    }

    /**
     * Returns a new comparator for an archetype property.
     *
     * @param sort        the sort criteria
     * @param transformer a transformer to apply
     */
    private static Comparator<Object> getComparator(
            ArchetypeSortConstraint sort, Transformer transformer) {
        Comparator comparator = getComparator(sort.isAscending());
        Transformer transform = ChainedTransformer.getInstance(
                transformer, new ArchetypeTransformer());
        return new TransformingComparator(transform, comparator);
    }

    /**
     * Returns a new comparator.
     *
     * @param ascending if <tt>true</tt> sort in ascending order; otherwise
     *                  sort in descending order
     */
    private static Comparator getComparator(boolean ascending) {
        Comparator comparator = ComparatorUtils.naturalComparator();

        // handle nulls.
        comparator = ComparatorUtils.nullLowComparator(comparator);
        if (!ascending) {
            comparator = ComparatorUtils.reversedComparator(comparator);
        }
        return comparator;
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
            IMObject object = (IMObject) input;
            NodeDescriptor descriptor = getDescriptor(object);
            if (descriptor != null) {
                try {
                    result = descriptor.getValue(object);
                    if (!(result instanceof Comparable)) {
                        // not comparable so null to avoid class cast exceptions
                        result = null;
                    }
                } catch (DescriptorException exception) {
                    log.error(exception);
                }
            }
            return result;
        }

        private NodeDescriptor getDescriptor(IMObject object) {
            if (archetype == null
                    || !archetype.getType().equals(object.getArchetypeId())) {
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
            IMObject object = (IMObject) input;
            return object.getArchetypeId().getShortName();
        }
    }

}
