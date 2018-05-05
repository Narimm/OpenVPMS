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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.layout;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.lang.ObjectUtils;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.filter.NodeFilter;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


/**
 * Examines an {@link ArchetypeDescriptor} to determine which nodes to include during layout by an
 * {@link AbstractLayoutStrategy}.
 * <p>
 * Nodes may be treated as simple or complex nodes. Simple nodes can be typically rendered inline,
 * whereas complex nodes may require more screen.
 *
 * @author Tim Anderson
 */
public class ArchetypeNodes {

    /**
     * The name of the first node.
     */
    private String first;

    /**
     * The name of the second node.
     */
    private String second;

    /**
     * Determines if all simple nodes should be included.
     */
    private boolean allSimpleNodes;

    /**
     * Determines if all complex nodes should be included.
     */
    private boolean allComplexNodes;

    /**
     * Include the specified nodes as simple nodes.
     */
    private Set<String> includeSimpleNodes = new LinkedHashSet<>();

    /**
     * Include the specified nodes as complex nodes.
     */
    private Set<String> includeComplexNodes = new LinkedHashSet<>();

    /**
     * Exclude the specified nodes.
     */
    private Set<String> exclude = new HashSet<>();

    /**
     * Exclude nodes if they are empty.
     */
    private Set<String> excludeIfEmpty = new HashSet<>();

    /**
     * If >= 0, exclude any string nodes with a maximum length greater than than that specified.
     */
    private long excludeStringLongerThan = -1;

    /**
     * Determines if password nodes should be excluded.
     */
    private boolean excludePassword = false;

    /**
     * Determines if hidden nodes should be included.
     */
    private boolean hidden;

    /**
     * Used to order nodes. The n-th element is placed before the n-th+1 element.
     */
    private List<String> order = new ArrayList<>();


    /**
     * Default constructor.
     * <p>
     * Includes all simple and complex nodes.
     */
    public ArchetypeNodes() {
        this(true, true);
    }

    /**
     * Constructs an {@link ArchetypeNodes}.
     *
     * @param includeAllSimple  if {@code true}, include all simple nodes, otherwise exclude them
     * @param includeAllComplex if {@code true}, include all complex nodes, otherwise exclude them
     */
    public ArchetypeNodes(boolean includeAllSimple, boolean includeAllComplex) {
        this.allSimpleNodes = includeAllSimple;
        this.allComplexNodes = includeAllComplex;
    }

    /**
     * Constructs an {@link ArchetypeNodes}.
     *
     * @param nodes the nodes to copy
     */
    public ArchetypeNodes(ArchetypeNodes nodes) {
        this.allSimpleNodes = nodes.allSimpleNodes;
        this.allComplexNodes = nodes.allSimpleNodes;
        this.first = nodes.first;
        this.second = nodes.second;
        this.includeSimpleNodes = new LinkedHashSet<>(nodes.includeSimpleNodes);
        this.includeComplexNodes = new LinkedHashSet<>(nodes.includeComplexNodes);
        this.exclude = new HashSet<>(nodes.exclude);
        this.excludeIfEmpty = new HashSet<>(nodes.excludeIfEmpty);
        this.excludeStringLongerThan = nodes.excludeStringLongerThan;
        this.hidden = nodes.hidden;
        this.excludePassword = nodes.excludePassword;
        this.order = new ArrayList<>(nodes.order);
    }

    /**
     * Sets the name of the first node to render.
     *
     * @param first the name of the first node
     * @return this instance
     */
    public ArchetypeNodes first(String first) {
        this.first = first;
        return this;
    }

    /**
     * Sets the name of the second node to render.
     *
     * @param second the name of the second node
     * @return this instance
     */
    public ArchetypeNodes second(String second) {
        this.second = second;
        return this;
    }

    /**
     * Includes the specified nodes, treating them as simple nodes.
     *
     * @param nodes the node names
     * @return this instance
     */
    public ArchetypeNodes simple(String... nodes) {
        includeSimpleNodes.addAll(Arrays.asList(nodes));
        return this;
    }

    /**
     * Includes the specified nodes, treating them as complex nodes.
     *
     * @param nodes the node names
     * @return this instance
     */
    public ArchetypeNodes complex(String... nodes) {
        includeComplexNodes.addAll(Arrays.asList(nodes));
        return this;
    }

    /**
     * Excludes the specified nodes.
     *
     * @param nodes the node names
     * @return this instance
     */
    public ArchetypeNodes exclude(String... nodes) {
        return exclude(Arrays.asList(nodes));
    }

    /**
     * Excludes the specified nodes.
     *
     * @param nodes the node names
     * @return this instance
     */
    public ArchetypeNodes exclude(Collection<String> nodes) {
        exclude.addAll(nodes);
        return this;
    }

    /**
     * Excludes the specified nodes if they are null or empty.
     * <p>
     * <ul>
     * <li>string nodes are excluded if the string is null or empty</li>
     * <li>non-collection nodes if they are null</li>
     * <li>collection nodes are excluded if they are empty</li>
     * </ul>
     *
     * @param nodes the node names
     * @return this instance
     */
    public ArchetypeNodes excludeIfEmpty(String... nodes) {
        Collections.addAll(excludeIfEmpty, nodes);
        return this;
    }

    /**
     * Excludes string nodes whose max length is greater than that specified.
     *
     * @param length the string length, or {@code -1} to disable
     * @return this instance
     */
    public ArchetypeNodes excludeStringLongerThan(long length) {
        this.excludeStringLongerThan = length;
        return this;
    }

    /**
     * Determines if hidden nodes should be included.
     *
     * @param hidden if {@code true} include hidden nodes, otherwise exclude them
     * @return this instance
     */
    public ArchetypeNodes hidden(boolean hidden) {
        this.hidden = hidden;
        return this;
    }

    /**
     * Determines if password nodes should be excluded.
     *
     * @param exclude if {@code true} exclude password nodes, otherwise include them
     * @return this instance
     */
    public ArchetypeNodes excludePassword(boolean exclude) {
        this.excludePassword = exclude;
        return this;
    }

    /**
     * Places a node before another.
     *
     * @param node1 the first node
     * @param node2 the second node
     * @return this instance
     */
    public ArchetypeNodes order(String node1, String node2) {
        order.add(node1);
        order.add(node2);
        return this;
    }

    /**
     * Determines the common nodes is a group of archetypes, according to the criteria.
     * <p>
     * To include nodes that don't appear in all archetypes, use {@link #simple} and {@link #complex} to explicitly
     * refer to them. To ensure they are returned in the correct order, use {@link #order}.
     *
     * @param archetypes the archetypes
     * @return the common node names
     */
    public List<String> getNodeNames(List<ArchetypeDescriptor> archetypes) {
        List<NodeDescriptor> result = new ArrayList<>();
        All all = new All();
        for (ArchetypeDescriptor archetype : archetypes) {
            List<NodeDescriptor> nodes = getNodes(archetype, all);
            if (result.isEmpty()) {
                result = nodes;
            } else {
                result = merge(result, nodes);
            }
        }
        return getNames(reorder(result));
    }

    /**
     * Returns the simple nodes.
     *
     * @param archetype the archetype descriptor
     * @return the simple nodes
     */
    public List<NodeDescriptor> getSimpleNodes(ArchetypeDescriptor archetype) {
        return getNodes(archetype, new SimplePredicate());
    }

    /**
     * Returns the simple nodes.
     *
     * @param archetype the archetype descriptor
     * @param object    the object to return nodes for
     * @param filter    a filter to exclude nodes according to some criteria. May be {@code null}
     * @return the simple nodes
     */
    public List<NodeDescriptor> getSimpleNodes(ArchetypeDescriptor archetype, IMObject object, NodeFilter filter) {
        return getNodes(archetype, object, includeSimpleNodes, new SimplePredicate(object), filter);
    }

    /**
     * Returns the simple node properties.
     *
     * @param properties the properties corresponding to the nodes
     * @param archetype  the archetype descriptor
     * @param object     the object to return nodes for
     * @param filter     a filter to exclude nodes according to some criteria. May be {@code null}
     * @return the simple node properties
     */
    public List<Property> getSimpleNodes(PropertySet properties, ArchetypeDescriptor archetype, IMObject object,
                                         NodeFilter filter) {
        List<Property> result = new ArrayList<>();
        for (NodeDescriptor descriptor : getSimpleNodes(archetype, object, filter)) {
            Property property = properties.get(descriptor);
            if (property != null) {
                result.add(property);
            }
        }
        return result;
    }

    /**
     * Returns the complex nodes.
     *
     * @param archetype the archetype descriptor
     * @return the simple nodes
     */
    public List<NodeDescriptor> getComplexNodes(ArchetypeDescriptor archetype) {
        return getNodes(archetype, new ComplexPredicate());
    }

    /**
     * Returns the complex nodes.
     *
     * @param archetype the archetype descriptor
     * @param object    the object to return nodes for
     * @param filter    a filter to exclude nodes according to some criteria
     * @return the complex nodes
     */
    public List<NodeDescriptor> getComplexNodes(ArchetypeDescriptor archetype, IMObject object, NodeFilter filter) {
        return getNodes(archetype, object, includeComplexNodes, new ComplexPredicate(object), filter);
    }

    /**
     * Returns the complex node properties.
     *
     * @param archetype the archetype descriptor
     * @param object    the object to return nodes for
     * @param filter    a filter to exclude nodes according to some criteria. May be {@code null}
     * @return the simple node properties
     */
    public List<Property> getComplexNodes(PropertySet properties, ArchetypeDescriptor archetype, IMObject object,
                                          NodeFilter filter) {
        List<Property> result = new ArrayList<>();
        for (NodeDescriptor descriptor : getComplexNodes(archetype, object, filter)) {
            result.add(properties.get(descriptor));
        }
        return result;
    }

    /**
     * Determines if this instance is equal to another.
     *
     * @param other the instance to compare
     * @return {@code true} if they are equal, otherwise {@code false}
     */
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (other instanceof ArchetypeNodes) {
            ArchetypeNodes nodes = (ArchetypeNodes) other;
            return ObjectUtils.equals(first, nodes.first)
                   && ObjectUtils.equals(second, nodes.second)
                   && allSimpleNodes == nodes.allSimpleNodes
                   && allComplexNodes == nodes.allComplexNodes
                   && includeSimpleNodes.equals(nodes.includeSimpleNodes)
                   && includeComplexNodes.equals(nodes.includeComplexNodes)
                   && exclude.equals(nodes.exclude)
                   && excludeIfEmpty.equals(nodes.excludeIfEmpty)
                   && excludeStringLongerThan == nodes.excludeStringLongerThan
                   && hidden == nodes.hidden
                   && excludePassword == nodes.excludePassword;

        }
        return false;
    }

    /**
     * Creates a new instance that selects all nodes.
     *
     * @return a new instance
     */
    public static ArchetypeNodes all() {
        return new ArchetypeNodes();
    }

    /**
     * Creates a new instance that selects only the specified simple nodes.
     *
     * @param nodes the nodes
     * @return a new instance
     */
    public static ArchetypeNodes onlySimple(String... nodes) {
        return new ArchetypeNodes(false, false).simple(nodes);
    }

    /**
     * Creates a new instance that selects all simple nodes.
     *
     * @return a new instance
     */
    public static ArchetypeNodes allSimple() {
        return new ArchetypeNodes(true, false);
    }

    /**
     * Creates a new instance that selects all complex nodes.
     *
     * @return a new instance
     */
    public static ArchetypeNodes allComplex() {
        return new ArchetypeNodes(false, true);
    }

    /**
     * Creates a new instance that selects no nodes.
     * <p>
     * Nodes must be explicitly added.
     *
     * @return a new instance
     */
    public static ArchetypeNodes none() {
        return new ArchetypeNodes(false, false);
    }

    /**
     * Returns the named property.
     *
     * @param properties the properties to search
     * @param name       the property name
     * @return the property, or {@code null} if none is found
     */
    public static Property find(List<Property> properties, String name) {
        for (Property property : properties) {
            if (property.getName().equals(name)) {
                return property;
            }
        }
        return null;
    }

    /**
     * Removes and returns the named property from the supplied list.
     *
     * @param properties the properties to search
     * @param name       the property name
     * @return the property, or {@code null} if none is found
     */
    public static Property remove(List<Property> properties, String name) {
        Iterator<Property> iterator = properties.iterator();
        while (iterator.hasNext()) {
            Property property = iterator.next();
            if (property.getName().equals(name)) {
                iterator.remove();
                return property;
            }
        }
        return null;
    }

    /**
     * Removes and returns the named properties from the supplied list.
     *
     * @param properties the properties to search
     * @param names      the names to remove
     * @return the property, or {@code null} if none is found
     */
    public static List<Property> removeAll(List<Property> properties, String... names) {
        List<Property> result = new ArrayList<>();
        List<String> keys = new ArrayList<>(Arrays.asList(names));
        Iterator<Property> iterator = properties.iterator();
        while (iterator.hasNext()) {
            Property property = iterator.next();
            String name = property.getName();
            if (keys.remove(name)) {
                result.add(property);
                iterator.remove();
                if (keys.isEmpty()) {
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Filters the properties, only including those matching the specified names.
     *
     * @param properties the properties
     * @param names      the names to include
     * @return the matching properties
     */
    public static List<Property> include(List<Property> properties, String... names) {
        List<Property> result = new ArrayList<>();
        List<String> values = Arrays.asList(names);
        for (Property property : properties) {
            if (values.contains(property.getName())) {
                result.add(property);
            }
        }
        return result;
    }

    /**
     * Helper to insert properties into a list after the named property.
     *
     * @param list       the property list to insert into
     * @param after      the property to insert after
     * @param properties the properties to insert
     * @return {@code list}
     */
    public static List<Property> insert(List<Property> list, String after, Property... properties) {
        int i = 0;
        for (; i < list.size(); ++i) {
            if (after.equals(list.get(i).getName())) {
                ++i;
                break;
            }
        }

        list.addAll(i, Arrays.asList(properties));
        return list;
    }

    /**
     * Filters the properties, excluding those matching the specified names.
     *
     * @param properties the properties
     * @param names      the names to exclude
     * @return the descriptors excluding those matching the specified names
     */
    public static List<Property> exclude(List<Property> properties, String... names) {
        List<Property> result = new ArrayList<>(properties);
        List<String> values = Arrays.asList(names);
        for (Property property : properties) {
            if (values.contains(property.getName())) {
                result.remove(property);
            }
        }
        return result;
    }

    /**
     * Helper to merge two lists of descriptors, maintaining insertion order.
     * <p>
     * If no nodes are explicitly referred to, this returns the intersection of the two lists.
     * If nodes are designated simple or complex nodes, these will always be included in the result.
     *
     * @param first  the first list
     * @param second the second list
     * @return the merged result
     */
    private List<NodeDescriptor> merge(List<NodeDescriptor> first, List<NodeDescriptor> second) {
        List<NodeDescriptor> result = new ArrayList<>();
        List<NodeDescriptor> left = new ArrayList<>(second);
        for (NodeDescriptor node : first) {
            String name = node.getName();
            if (indexOf(name, second) != -1 || includeSimpleNodes.contains(name)
                || includeComplexNodes.contains(name)) {
                result.add(node);
            }
            int index = indexOf(name, left);
            if (index != -1) {
                left.remove(index);
            }
        }
        for (NodeDescriptor node : left) {
            String name = node.getName();
            if (includeSimpleNodes.contains(name) || includeComplexNodes.contains(name)) {
                boolean found = false;
                int index = indexOf(name, second) - 1;
                while (index > 0) {
                    NodeDescriptor prior = second.get(index);
                    int pos = indexOf(prior.getName(), result);
                    if (pos != -1) {
                        result.add(pos + 1, node);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    result.add(0, node);
                }
            }
        }
        return result;
    }

    /**
     * Returns the names of of each of the descriptors.
     *
     * @param descriptors the descriptors
     * @return the descriptor names
     */
    private List<String> getNames(List<NodeDescriptor> descriptors) {
        List<String> result = new ArrayList<>();
        for (NodeDescriptor descriptor : descriptors) {
            result.add(descriptor.getName());
        }
        return result;
    }

    /**
     * Returns all nodes matching a predicate, in appropriate order.
     *
     * @param archetype the archetype
     * @param predicate the predicate to select nodes
     * @return the matching nodes
     */
    private List<NodeDescriptor> getNodes(ArchetypeDescriptor archetype, Predicate predicate) {
        List<NodeDescriptor> result = new ArrayList<>();
        CollectionUtils.select(archetype.getAllNodeDescriptors(), predicate, result);
        reorder(result);
        return result;
    }

    /**
     * Returns all nodes matching a predicate and filter, in appropriate order.
     *
     * @param archetype the archetype
     * @param object    the object to return nodes for
     * @param includes  nodes to explicitly include. These override any filter settings
     * @param predicate the predicate to select nodes
     * @param filter    the filter. May be {@code null}
     * @return the matching nodes
     */
    private List<NodeDescriptor> getNodes(ArchetypeDescriptor archetype, IMObject object, Set<String> includes,
                                          Predicate predicate, NodeFilter filter) {
        List<NodeDescriptor> nodes = getNodes(archetype, predicate);
        List<NodeDescriptor> result;
        if (filter != null) {
            result = new ArrayList<>();
            for (NodeDescriptor node : nodes) {
                if (includes.contains(node.getName()) || filter.include(node, object)) {
                    result.add(node);
                }
            }

        } else {
            result = nodes;
        }
        return result;
    }

    /**
     * Reorders nodes so that any {@link #first} and {@link #second} node is in the correct order.
     *
     * @param descriptors the node descriptors. This list is modified
     * @return {@code descriptors}
     */
    private List<NodeDescriptor> reorder(List<NodeDescriptor> descriptors) {
        if (first != null) {
            move(first, 0, descriptors);
        }
        if (second != null) {
            move(second, 1, descriptors);
        }
        for (int i = 0; i < order.size(); i += 2) {
            String node1 = order.get(i);
            String node2 = order.get(i + 1);
            int index = indexOf(node2, descriptors);
            if (index != -1) {
                move(node1, index, descriptors);
            }
        }
        return descriptors;
    }

    /**
     * Returns the index of a node in a list.
     *
     * @param node        the node name
     * @param descriptors the list of descriptors to search
     * @return the index of the node, or {@code -1} if none is found
     */
    private int indexOf(String node, List<NodeDescriptor> descriptors) {
        for (int i = 0; i < descriptors.size(); ++i) {
            NodeDescriptor descriptor = descriptors.get(i);
            if (descriptor.getName().equals(node)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Moves a node to its correct position in a list.
     *
     * @param node        the node name
     * @param index       the expected index
     * @param descriptors the list of descriptors
     */
    private void move(String node, int index, List<NodeDescriptor> descriptors) {
        int pos = indexOf(node, descriptors);
        if (pos != -1 && pos != index) {
            NodeDescriptor descriptor = descriptors.remove(pos);
            if (pos > index) {
                descriptors.add(index, descriptor);
            } else {
                descriptors.add(index - 1, descriptor);
            }
        }
    }

    /**
     * Determines if a node is empty.
     *
     * @param object     the parent object
     * @param descriptor the node descriptor
     * @return {@code true} if the node is empty
     */
    private boolean isEmpty(IMObject object, NodeDescriptor descriptor) {
        boolean result;
        Object value = descriptor.getValue(object);
        if (value instanceof String) {
            result = ((String) value).length() == 0;
        } else if (value instanceof Collection) {
            result = ((Collection) value).isEmpty();
        } else {
            result = value == null;
        }
        return result;
    }

    private abstract class AbstractPredicate implements Predicate<NodeDescriptor> {

        private final IMObject object;

        public AbstractPredicate() {
            this(null);
        }

        public AbstractPredicate(IMObject object) {
            this.object = object;
        }

        protected boolean include(NodeDescriptor descriptor) {
            boolean result = false;
            String name = descriptor.getName();
            if ((hidden || !descriptor.isHidden()) && !exclude.contains(name)) {
                result = !excludeIfEmpty(descriptor) && (!descriptor.isString() || includeString(descriptor))
                         && (!excludePassword || !descriptor.containsAssertionType("password"));
            }
            return result;
        }

        private boolean excludeIfEmpty(NodeDescriptor descriptor) {
            return object != null && excludeIfEmpty.contains(descriptor.getName()) && isEmpty(object, descriptor);
        }

        private boolean includeString(NodeDescriptor descriptor) {
            return excludeStringLongerThan == -1 || descriptor.getMaxLength() <= excludeStringLongerThan;
        }
    }

    private class SimplePredicate extends AbstractPredicate {

        public SimplePredicate() {
        }

        public SimplePredicate(IMObject object) {
            super(object);
        }

        /**
         * Determines if a node is an included simple node.
         *
         * @param descriptor the descriptor to evaluate
         * @return true or false
         */
        @Override
        public boolean evaluate(NodeDescriptor descriptor) {
            boolean include = false;
            String name = descriptor.getName();
            boolean simple = !descriptor.isComplexNode();
            if (((allSimpleNodes && simple) || includeSimpleNodes.contains(name))
                && !includeComplexNodes.contains(name)) {
                include = include(descriptor);
            }
            return include;
        }
    }

    private class ComplexPredicate extends AbstractPredicate {

        public ComplexPredicate() {
        }

        public ComplexPredicate(IMObject object) {
            super(object);
        }

        /**
         * Determines if a node is an included complex node.
         *
         * @param descriptor the descriptor to evaluate
         * @return true or false
         */
        @Override
        public boolean evaluate(NodeDescriptor descriptor) {
            boolean include = false;
            String name = descriptor.getName();
            boolean complex = descriptor.isComplexNode();
            if (((allComplexNodes && complex) || includeComplexNodes.contains(name))
                && !includeSimpleNodes.contains(name)) {
                include = include(descriptor);
            }
            return include;
        }
    }

    private class All extends AbstractPredicate {

        private SimplePredicate simple = new SimplePredicate();

        private ComplexPredicate complex = new ComplexPredicate();

        /**
         * Determines if a node is an included node.
         *
         * @param object the descriptor to evaluate
         * @return true or false
         */
        @Override
        public boolean evaluate(NodeDescriptor object) {
            return simple.evaluate(object) || complex.evaluate(object);
        }
    }
}
