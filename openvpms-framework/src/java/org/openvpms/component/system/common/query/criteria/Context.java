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

package org.openvpms.component.system.common.query.criteria;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.dao.hibernate.im.query.QueryBuilderException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.descriptor.cache.IArchetypeDescriptorCache;
import org.openvpms.component.model.archetype.ArchetypeDescriptor;
import org.openvpms.component.model.archetype.NodeDescriptor;
import org.openvpms.component.model.object.IMObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.openvpms.component.business.dao.hibernate.im.query.QueryBuilderException.ErrorCode.CannotQueryAcrossTypes;
import static org.openvpms.component.business.dao.hibernate.im.query.QueryBuilderException.ErrorCode.NoArchetypeRangeAssertion;
import static org.openvpms.component.business.dao.hibernate.im.query.QueryBuilderException.ErrorCode.NoMatchingArchetypesForShortName;
import static org.openvpms.component.business.dao.hibernate.im.query.QueryBuilderException.ErrorCode.NoNodeDescWithName;
import static org.openvpms.component.business.dao.hibernate.im.query.QueryBuilderException.ErrorCode.NodeDescriptorsDoNotMatch;

/**
 * Criteria context.
 *
 * @author Tim Anderson
 */
public class Context {

    /**
     * The archetype descriptor cache.
     */
    private final IArchetypeDescriptorCache cache;

    /**
     * Constructs a {@link Context}.
     *
     * @param cache the archetype descriptor cache
     */
    public Context(IArchetypeDescriptorCache cache) {
        this.cache = cache;
    }

    /**
     * Creates a new type for an archetype.
     *
     * @param type       the {@link IMObject} type
     * @param archetypes the archetypes to constrain on. May contain wildcards
     * @return a new type
     */
    public <T extends IMObject> Type<T> getType(Class<T> type, String... archetypes) {
        Map<String, ArchetypeDescriptor> descriptors = getDescriptors(archetypes, false);
        Class impl = getClass(descriptors.values());
        if (!type.isAssignableFrom(impl)) {
            throw new IllegalArgumentException("Argument 'type' is not assignable from " + impl.getName()
                                               + ": " + type.getName());
        }
        return new Type<T>(impl, descriptors, null);
    }

    /**
     * Creates a new type for a join.
     *
     * @param from the type being joined on
     * @param name the node name
     * @return a new type
     */
    public <T> Type<T> getTypeForJoin(Type<?> from, String name) {
        List<NodeDescriptor> nodes = getNodes(from, name);
        Map<String, ArchetypeDescriptor> descriptors = getDescriptors(nodes);
        if (descriptors.isEmpty()) {
            throw new IllegalArgumentException("Node " + name + " is not associated with any archetypes");
        }
        return new Type<T>(getClass(descriptors.values()), descriptors, nodes.get(0));
    }

    /**
     * Creates a new type for a join.
     *
     * @param from      the type being joined on
     * @param name      the node name
     * @param archetype the archetype to constrain on. May contain wildcards
     * @return a new type
     */
    public <T> Type<T> getTypeForJoin(Type<?> from, String name, String archetype) {
        Type<T> result;
        List<NodeDescriptor> nodes = getNodes(from, name);
        NodeDescriptor first = nodes.get(0);
        if (first.getArchetypeRange() != null || first.getFilter() != null) {
            Map<String, ArchetypeDescriptor> descriptors = getDescriptors(nodes);
            ArchetypeDescriptor descriptor = descriptors.get(archetype);
            if (descriptor == null) {
                throw new IllegalArgumentException("Archetype=" + archetype + " not present for node=" + name);
            }
            Map<String, ArchetypeDescriptor> map = Collections.singletonMap(archetype, descriptor);
            result = new Type<T>((Class<T>) descriptor.getClassType(), map, first);
        } else {
            throw new IllegalArgumentException("Cannot join on " + name);
        }
        return result;
    }

    /**
     * Returns a {@link Type} for a node.
     *
     * @param parent the parent type
     * @param name   the node name
     * @return the type for the node
     */
    public <T> Type<T> getTypeForNode(Type<?> parent, String name) {
        Type<T> type;
        Map<String, ArchetypeDescriptor> parentArchetypes = parent.getArchetypeDescriptors();
        if (parentArchetypes == null) {
            throw new IllegalArgumentException("Leaf node");
        }
        List<NodeDescriptor> nodes = getNodes(name, parentArchetypes);
        NodeDescriptor first = nodes.get(0);
        if (first.getArchetypeRange().length != 0 || first.getFilter() != null) {
            Map<String, ArchetypeDescriptor> descriptors = getDescriptors(nodes);
            type = new Type<T>((Class<T>) first.getClassType(), descriptors, first);
        } else {
            type = new Type<T>((Class<T>) first.getClassType(), null, first);
        }
        return type;
    }

    /**
     * Returns all archetypes associated with a set of of node descriptors.
     *
     * @param nodes the node descriptors
     * @return the corresponding archetype descriptors
     */
    private Map<String, ArchetypeDescriptor> getDescriptors(List<NodeDescriptor> nodes) {
        Set<String> matches = new HashSet<>();
        for (NodeDescriptor descriptor : nodes) {
            mergeArchetypeRange(matches, descriptor);
        }
        String[] archetypes = matches.toArray(new String[matches.size()]);
        Map<String, ArchetypeDescriptor> descriptors = getDescriptors(archetypes, false);
        if (descriptors.isEmpty()) {
            throw new QueryBuilderException(NoMatchingArchetypesForShortName, ArrayUtils.toString(archetypes));
        }
        return descriptors;
    }

    /**
     * Returns all node descriptors with the specified name from a set of archetypes.
     *
     * @param name        the node name
     * @param descriptors the archetype descriptors
     * @return the node descriptors
     */
    private List<NodeDescriptor> getNodes(String name, Map<String, ArchetypeDescriptor> descriptors) {
        List<NodeDescriptor> result = new ArrayList<>();
        NodeDescriptor matching = null;

        // ensure the property is defined in all archetypes
        NodeDescriptor node;

        for (ArchetypeDescriptor descriptor : descriptors.values()) {
            node = descriptor.getNodeDescriptor(name);
            if (node == null) {
                throw new QueryBuilderException(NoNodeDescWithName, descriptor.getName(), name);
            }

            // now check against the matching node descriptor
            if (matching == null) {
                matching = node;
            } else if (!node.getPath().equals(matching.getPath()) || !node.getType().equals(matching.getType())) {
                throw new QueryBuilderException(NodeDescriptorsDoNotMatch, name);
            }
            result.add(node);
        }
        if (result.isEmpty()) {
            throw new IllegalArgumentException("Argument '" + name + "' is not a node of "
                                               + StringUtils.join(descriptors.values(), ","));
        }

        return result;
    }

    /**
     * Returns all node descriptors with the same name from a set of archetypes.
     *
     * @param type the type
     * @param name the node name
     * @return the node descriptors
     */
    private List<NodeDescriptor> getNodes(Type<?> type, String name) {
        Map<String, ArchetypeDescriptor> descriptors = type.getArchetypeDescriptors();
        if (descriptors == null) {
            throw new IllegalArgumentException("Leaf node");
        }
        return getNodes(name, descriptors);
    }

    /**
     * Returns the common base class for a set of archetype descriptors.
     *
     * @param descriptors the archetype descriptors
     * @return the common base class
     * @throws QueryBuilderException if the classes don't have a common base class
     */
    private Class getClass(Collection<ArchetypeDescriptor> descriptors) {
        Class<?> superType = null;
        for (ArchetypeDescriptor descriptor : descriptors) {
            Class<?> type = descriptor.getClassType();
            if (superType == null) {
                superType = type;
            } else if (type.isAssignableFrom(superType)) {
                superType = type;
            } else if (!superType.isAssignableFrom(type)) {
                // doesn't allow > 1 level of subclasses but good enough for now
                throw new QueryBuilderException(CannotQueryAcrossTypes, superType, descriptor.getClassName());
            }
        }
        return superType;
    }

    /**
     * Returns all archetype descriptors matching the short names.
     *
     * @param archetypes  a list of short names to search against
     * @param primaryOnly determines whether to restrict processing to primary only
     * @return matching archetypes
     * @throws ArchetypeServiceException for any archetype service error
     */
    private Map<String, ArchetypeDescriptor> getDescriptors(String[] archetypes, boolean primaryOnly) {
        Map<String, ArchetypeDescriptor> result = new HashMap<>();
        // expand any wildcards
        Set<String> expanded = new HashSet<>();
        for (String archetype : archetypes) {
            List<String> matches = cache.getArchetypeShortNames(archetype, primaryOnly);
            expanded.addAll(matches);
        }
        for (String archetype : expanded) {
            ArchetypeDescriptor descriptor = cache.getArchetypeDescriptor(archetype);
            result.put(archetype, descriptor);
        }
        return result;
    }

    /**
     * Merges the archetype range from a node descriptor with the supplied matches.
     *
     * @param matches    the matches
     * @param descriptor the descriptor
     * @throws QueryBuilderException if there is no archetype range assertion or filter for the descriptor
     */
    private void mergeArchetypeRange(Set<String> matches, NodeDescriptor descriptor) {
        String[] archetypes = descriptor.getArchetypeRange();
        if (archetypes == null || archetypes.length == 0) {
            if (descriptor.getFilter() == null) {
                ArchetypeDescriptor archetype = descriptor.getArchetypeDescriptor();
                String name = (archetype != null) ? archetype.getArchetypeType() : "unknown";
                throw new QueryBuilderException(NoArchetypeRangeAssertion, name, descriptor.getName());
            }
            matches.add(descriptor.getFilter());
        } else {
            matches.addAll(Arrays.asList(archetypes));
        }
    }

}
