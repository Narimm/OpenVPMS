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
import org.openvpms.component.business.service.archetype.CachingReadOnlyArchetypeService;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.LookupHelper;
import org.openvpms.component.business.service.archetype.helper.NodeResolver;
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
 * @author Tim Anderson
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
     * @param ascending if {@code true} sort ascending, otherwise sort descending
     */
    public static <T extends IMObject> void sort(List<T> objects, String node, boolean ascending) {
        sort(objects, new NodeSortConstraint(node, ascending));
    }

    /**
     * Sorts a collection of objects.
     *
     * @param objects the objects to sort.
     * @param nodes   the nodes to sort on
     */
    public static <T extends IMObject> void sort(List<T> objects, String... nodes) {
        SortConstraint[] constraints = new SortConstraint[nodes.length];
        for (int i = 0; i < nodes.length; ++i) {
            constraints[i] = new NodeSortConstraint(nodes[i]);
        }
        sort(objects, constraints);
    }

    /**
     * Sorts a collection of objects.
     *
     * @param objects the objects to sort.
     * @param sort    the sort criteria
     */
    @SuppressWarnings("unchecked")
    public static <T extends IMObject> void sort(List<T> objects, SortConstraint... sort) {
        ComparatorFactory factory = new ComparatorFactory();
        Comparator comparator = factory.create(sort);
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
        ComparatorFactory factory = new ComparatorFactory();
        Comparator comparator = factory.create(sort, transformer);
        Collections.sort(objects, comparator);
    }

    /**
     * Returns a comparator that sorts objects on name.
     * <p/>
     * This comparator handles nulls.
     *
     * @param ascending if {@code true} sort in ascending order; otherwise sort in descending order
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
     * @param ascending if {@code true} sort in ascending order; otherwise sort in descending order
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
     * Helper to convert a value to one that may be compared.
     *
     * @param value   the value. May be {@code null}
     * @param service the archetype service
     * @return the value, converted if required. May be {@code null}
     */
    protected static Object convert(Object value, IArchetypeService service) {
        if (value instanceof Participation) {
            // sort on participation entity name
            Participation p = (Participation) value;
            value = IMObjectHelper.getName(p.getEntity(), service);
        } else if (!(value instanceof Comparable)) {
            // not comparable so null to avoid class cast exceptions
            value = null;
        } else if (value instanceof Timestamp) {
            // convert all Timestamps to Dates to avoid class
            // cast exceptions comparing dates and timestamps
            value = new Date(((Timestamp) value).getTime());
        }
        return value;
    }

    /**
     * A factory for comparators that emulate {@link SortConstraint}s.
     * <p/>
     * For those comparators using the archetype service, this creates a shared {@link CachingReadOnlyArchetypeService}
     * to reduce database access.
     */
    private static class ComparatorFactory {

        /**
         * The archetype service. This is created on demand.
         */
        private IArchetypeService service;

        /**
         * Creates a comparator for a set of sort constraints.
         *
         * @param sort the sort constraints
         * @return the corresponding comparator
         */
        public Comparator create(SortConstraint[] sort) {
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
            return comparator;
        }

        /**
         * Creates a comparator for a set of sort constraints.
         *
         * @param sort        the sort constraints
         * @param transformer a transformer to return the underlying IMObject
         * @return the corresponding comparator
         */
        public Comparator create(SortConstraint[] sort, Transformer transformer) {
            ComparatorChain comparator = new ComparatorChain();
            for (SortConstraint constraint : sort) {
                if (constraint instanceof VirtualNodeSortConstraint) {
                    Comparator node = getComparator((VirtualNodeSortConstraint) constraint, transformer);
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
            return comparator;
        }

        /**
         * Returns a new comparator for a virtual node sort constraint.
         *
         * @param sort the sort criteria
         * @return a new comparator
         */
        @SuppressWarnings("unchecked")
        private Comparator<Object> getComparator(VirtualNodeSortConstraint sort) {
            Comparator comparator = IMObjectSorter.getComparator(sort.isAscending());
            Transformer transformer = sort.getTransformer();
            if (transformer == null) {
                transformer = getTransformer(sort);
            }
            return new TransformingComparator(transformer, comparator);
        }

        /**
         * Returns a new comparator for a virtual node sort constraint.
         * <p/>
         * If the sort constraint provides its own transformer, this will be used instead of {@code transformer}.
         *
         * @param sort        the sort criteria
         * @param transformer a transformer to apply
         * @return a new comparator
         */
        @SuppressWarnings("unchecked")
        private Comparator<Object> getComparator(VirtualNodeSortConstraint sort, Transformer transformer) {
            Comparator<Object> comparator = getComparator(sort);
            return sort.getTransformer() == null ? new TransformingComparator(transformer, comparator) : comparator;
        }

        /**
         * Returns a new comparator for a node sort constraint.
         *
         * @param sort the sort criteria
         * @return a new comparator
         */
        @SuppressWarnings("unchecked")
        private Comparator<Object> getComparator(NodeSortConstraint sort) {
            Comparator comparator = IMObjectSorter.getComparator(sort.isAscending());
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
        private Comparator<Object> getComparator(NodeSortConstraint sort, Transformer transformer) {
            Comparator comparator = IMObjectSorter.getComparator(sort.isAscending());
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
        private Comparator<Object> getComparator(ArchetypeSortConstraint sort) {
            Comparator comparator = IMObjectSorter.getComparator(sort.isAscending());
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
        private Comparator<Object> getComparator(ArchetypeSortConstraint sort, Transformer transformer) {
            Comparator comparator = IMObjectSorter.getComparator(sort.isAscending());
            Transformer transform = ChainedTransformer.getInstance(transformer, new ArchetypeTransformer());
            return new TransformingComparator(transform, comparator);
        }

        /**
         * Returns a new transformer for a node sort constraint.
         *
         * @param sort the sort constraint
         * @return a new transformer
         */
        private Transformer getTransformer(NodeSortConstraint sort) {
            IArchetypeService service = getService();
            String name = sort.getNodeName();
            if (name.contains(".")) {
                return new NodeResolverTransformer(name, service);
            }
            // fall back to the pre-NodeResolver version.
            return new NodeTransformer(name, service);
        }

        /**
         * Returns the archetype service.
         *
         * @return the archetype service
         */
        private IArchetypeService getService() {
            if (service == null) {
                service = new CachingReadOnlyArchetypeService(100, ServiceHelper.getArchetypeService());
            }
            return service;
        }
    }

    /**
     * A transformer that access the value of node using {@link NodeResolver}.
     */
    private static class NodeResolverTransformer implements Transformer {

        /**
         * The node name.
         */
        private final String node;

        /**
         * The archetype service.
         */
        private final IArchetypeService service;

        /**
         * Constructs an {@link NodeResolverTransformer}.
         *
         * @param node    the node name
         * @param service the archetype service
         */
        public NodeResolverTransformer(String node, IArchetypeService service) {
            this.node = node;
            this.service = service;
        }

        /**
         * Transforms the input object (leaving it unchanged) into some output object.
         *
         * @param input the object to be transformed, should be left unchanged
         * @return a transformed object
         * @throws ClassCastException       (runtime) if the input is the wrong class
         * @throws IllegalArgumentException (runtime) if the input is invalid
         * @throws FunctorException         (runtime) if the transform cannot be completed
         */
        @Override
        public Object transform(Object input) {
            IMObject object = (IMObject) input;
            if (object != null) {
                NodeResolver resolver = new NodeResolver(object, service);
                Object value = resolver.getObject(node);
                return convert(value, service);
            }
            return null;
        }
    }

    private static class NodeTransformer implements Transformer {

        /**
         * The node name.
         */
        private final String node;

        /**
         * The archetype service.
         */
        private final IArchetypeService service;

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
        public NodeTransformer(String node, IArchetypeService service) {
            this.node = node;
            this.service = service;
        }

        /**
         * Transforms the input object (leaving it unchanged) into some output object.
         *
         * @param input the object to be transformed, should be left unchanged
         * @return a transformed object
         * @throws ClassCastException       (runtime) if the input is the wrong class
         * @throws IllegalArgumentException (runtime) if the input is invalid
         * @throws FunctorException         (runtime) if the transform cannot be completed
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
                            result = LookupHelper.getName(service, ServiceHelper.getLookupService(), object,
                                                          descriptor.getName());
                        } else {
                            result = descriptor.getValue(object);
                        }
                        result = convert(result, service);
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
                if (archetype == null) {
                    throw new IllegalStateException(
                            "No archetype descriptor found for object, id=" + object.getId() + ", archetypeId="
                            + object.getArchetypeIdAsString());
                }
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
         * first argument is less than, equal to, or greater than the
         * second.
         */
        public int compare(Object o1, Object o2) {
            int hash1 = System.identityHashCode(o1);
            int hash2 = System.identityHashCode(o2);
            return hash1 - hash2;
        }
    }

}
