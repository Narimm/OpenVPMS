package org.openvpms.web.component.im.layout;

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.filter.FilterHelper;
import org.openvpms.web.component.im.filter.NodeFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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
     * Returns the simple nodes.
     *
     * @param archetype the archetype descriptor
     * @return the simple nodes
     */
    public List<NodeDescriptor> getSimpleNodes(ArchetypeDescriptor archetype) {
        LinkedHashMap<String, NodeDescriptor> map = new LinkedHashMap<String, NodeDescriptor>();
        if (allSimpleNodes) {
            include(archetype.getSimpleNodeDescriptors(), map);
        }
        include(includeSimpleNodes, archetype, map);
        exclude(exclude, map);
        exclude(includeComplexNodes, map);  // if the node is being include as a complex node, don't duplicate here
        return reorder(map);
    }

    /**
     * Returns the simple nodes.
     *
     * @param archetype the archetype descriptor
     * @param object    the object to return nodes for
     * @param filter    a filter to exclude nodes according to some criteria
     * @return the simple nodes
     */
    public List<NodeDescriptor> getSimpleNodes(ArchetypeDescriptor archetype, IMObject object, NodeFilter filter) {
        return FilterHelper.filter(object, filter, getSimpleNodes(archetype));
    }

    /**
     * Returns the complex nodes.
     *
     * @param archetype the archetype descriptor
     * @return the simple nodes
     */
    public List<NodeDescriptor> getComplexNodes(ArchetypeDescriptor archetype) {
        LinkedHashMap<String, NodeDescriptor> map = new LinkedHashMap<String, NodeDescriptor>();
        if (allComplexNodes) {
            include(archetype.getComplexNodeDescriptors(), map);
        }
        include(includeComplexNodes, archetype, map);
        exclude(exclude, map);
        exclude(includeSimpleNodes, map);  // if the node is being include as a simple node, don't duplicate here
        return reorder(map);
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
        return FilterHelper.filter(object, filter, getComplexNodes(archetype));
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
     * Filters the descriptors, only including those matching the specified names.
     *
     * @param descriptors the descriptors
     * @param names       the names to include
     * @return the matching descriptors
     */
    public static List<NodeDescriptor> include(List<NodeDescriptor> descriptors, String... names) {
        List<NodeDescriptor> result = new ArrayList<NodeDescriptor>();
        List<String> values = Arrays.asList(names);
        for (NodeDescriptor descriptor : descriptors) {
            if (values.contains(descriptor.getName())) {
                result.add(descriptor);
            }
        }
        return result;
    }

    /**
     * Filters the descriptors, excluding those matching the specified names.
     *
     * @param descriptors the descriptors
     * @param names       the names to exclude
     * @return the descriptors excluding those matching the specified names
     */
    public static List<NodeDescriptor> exclude(List<NodeDescriptor> descriptors, String... names) {
        List<NodeDescriptor> result = new ArrayList<NodeDescriptor>(descriptors);
        List<String> values = Arrays.asList(names);
        for (NodeDescriptor descriptor : descriptors) {
            if (values.contains(descriptor.getName())) {
                result.remove(descriptor);
            }
        }
        return result;
    }

    /**
     * Adds all nodes to the map.
     *
     * @param descriptors the node descriptors
     * @param map         the map to add to
     */
    private void include(List<NodeDescriptor> descriptors, Map<String, NodeDescriptor> map) {
        for (NodeDescriptor descriptor : descriptors) {
            map.put(descriptor.getName(), descriptor);
        }
    }

    /**
     * Includes all those nodes specified, adding them to the supplied map.
     *
     * @param include   the names of the nodes to include
     * @param archetype the archetype descriptor
     * @param map       the map to add matching nodes to
     */
    private void include(Set<String> include, ArchetypeDescriptor archetype, Map<String, NodeDescriptor> map) {
        for (String name : include) {
            if (!map.containsKey(name)) {
                NodeDescriptor descriptor = archetype.getNodeDescriptor(name);
                if (descriptor != null) {
                    map.put(name, descriptor);
                }
            }
        }
    }

    /**
     * Removes all nodes specified from the supplied map.
     *
     * @param exclude the nodes to exclude
     * @param map     the map to remove nodes from
     */
    private void exclude(Set<String> exclude, Map<String, NodeDescriptor> map) {
        map.keySet().removeAll(exclude);
    }

    /**
     * Reorders nodes so that any {@link #first} and {@link #second} node is in the correct order.
     *
     * @param map the node map
     * @return the reordered nodes
     */
    private List<NodeDescriptor> reorder(Map<String, NodeDescriptor> map) {
        List<NodeDescriptor> result = new ArrayList<NodeDescriptor>(map.values());
        if (first != null) {
            move(first, 0, result);
        }
        if (second != null) {
            move(second, 1, result);
        }
        return result;
    }

    /**
     * Moves a node to its correct position in a list.
     *
     * @param node        the node name
     * @param index       the expected index
     * @param descriptors the list of descriptors
     */
    private void move(String node, int index, List<NodeDescriptor> descriptors) {
        for (int i = 0; i < descriptors.size(); ++i) {
            NodeDescriptor descriptor = descriptors.get(i);
            if (descriptor.getName().equals(node)) {
                if (i != index) {
                    descriptors.remove(i);
                    descriptors.add(index, descriptor);
                }
                break;
            }
        }

    }

}
