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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.layout;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


/**
 * Examines an {@link ArchetypeDescriptor} to determine which nodes to include during layout by an
 * {@link AbstractLayoutStrategy}.
 * <p/>
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
    private Set<String> includeSimpleNodes = new LinkedHashSet<String>();

    /**
     * Include the specified nodes as complex nodes.
     */
    private Set<String> includeComplexNodes = new LinkedHashSet<String>();

    /**
     * Exclude the specified nodes.
     */
    private Set<String> exclude = new HashSet<String>();

    /**
     * Used to order nodes. The n-th element is placed before the n-th+1 element.
     */
    private List<String> order = new ArrayList<String>();


    /**
     * Default constructor.
     * <p/>
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
        this.includeSimpleNodes = new LinkedHashSet<String>(nodes.includeSimpleNodes);
        this.includeComplexNodes = new LinkedHashSet<String>(nodes.includeComplexNodes);
        this.exclude = new HashSet<String>(exclude);
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
        return getNodes(archetype, object, includeSimpleNodes, new SimplePredicate(), filter);
    }

    /**
     * Returns the simple node properties.
     *
     * @param archetype the archetype descriptor
     * @param object    the object to return nodes for
     * @param filter    a filter to exclude nodes according to some criteria. May be {@code null}
     * @return the simple node properties
     */
    public List<Property> getSimpleNodes(PropertySet properties, ArchetypeDescriptor archetype, IMObject object,
                                         NodeFilter filter) {
        List<Property> result = new ArrayList<Property>();
        for (NodeDescriptor descriptor : getSimpleNodes(archetype, object, filter)) {
            result.add(properties.get(descriptor));
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
        return getNodes(archetype, object, includeComplexNodes, new ComplexPredicate(), filter);
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
        List<Property> result = new ArrayList<Property>();
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
                   && exclude.equals(nodes.exclude);
        }
        return false;
    }

    /**
     * Filters the properties, only including those matching the specified names.
     *
     * @param properties the properties
     * @param names      the names to include
     * @return the matching properties
     */
    public static List<Property> include(List<Property> properties, String... names) {
        List<Property> result = new ArrayList<Property>();
        List<String> values = Arrays.asList(names);
        for (Property property : properties) {
            if (values.contains(property.getName())) {
                result.add(property);
            }
        }
        return result;
    }

    /**
     * Filters the descriptors, excluding those matching the specified names.
     *
     * @param properties the descriptors
     * @param names      the names to exclude
     * @return the descriptors excluding those matching the specified names
     */
    public static List<Property> exclude(List<Property> properties, String... names) {
        List<Property> result = new ArrayList<Property>(properties);
        List<String> values = Arrays.asList(names);
        for (Property property : properties) {
            if (values.contains(property.getName())) {
                result.remove(property);
            }
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
        List<NodeDescriptor> result = new ArrayList<NodeDescriptor>();
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
            result = new ArrayList<NodeDescriptor>();
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
     * @param descriptors the node descriptors
     */
    private void reorder(List<NodeDescriptor> descriptors) {
        if (first != null) {
            move(first, 0, descriptors);
        }
        if (second != null) {
            move(second, 1, descriptors);
        }
        for (int i = 0; i < order.size(); i += 2) {
            String node1 = order.get(i);
            String node2 = order.get(i + 1);
            int index = indexOf(node1, descriptors);
            if (index != -1) {
                move(node2, index + 1, descriptors);
            }
        }
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

    private class SimplePredicate implements Predicate {

        /**
         * Determines if a node is an included simple node.
         *
         * @param object the descriptor to evaluate
         * @return true or false
         */
        @Override
        public boolean evaluate(Object object) {
            NodeDescriptor descriptor = (NodeDescriptor) object;
            String name = descriptor.getName();
            boolean simple = !descriptor.isComplexNode();
            return ((allSimpleNodes && simple) || (!simple && includeSimpleNodes.contains(name)))
                   && !exclude.contains(name) && !includeComplexNodes.contains(name);
        }
    }

    private class ComplexPredicate implements Predicate {

        /**
         * Determines if a node is an included complex node.
         *
         * @param object the descriptor to evaluate
         * @return true or false
         */
        @Override
        public boolean evaluate(Object object) {
            NodeDescriptor descriptor = (NodeDescriptor) object;
            String name = descriptor.getName();
            boolean complex = descriptor.isComplexNode();
            return ((allComplexNodes && complex) || (!complex && includeComplexNodes.contains(name)))
                   && !exclude.contains(name) && !includeSimpleNodes.contains(name);
        }
    }
}
