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


package org.openvpms.component.business.domain.im.archetype.descriptor;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.openvpms.component.business.domain.archetype.ArchetypeId;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * The archetype descriptor is used to describe an archetype.
 *
 * @author Jim Alateras
 */
public class ArchetypeDescriptor extends Descriptor
        implements org.openvpms.component.model.archetype.ArchetypeDescriptor {

    /**
     * The archetype id is the type.
     */
    private ArchetypeId type;

    /**
     * The display name of the archetype. If the displayname is empty then
     * simply return the name
     */
    private String displayName;

    /**
     * The full-qualified Java domain class that the archetype constrains
     */
    private String className;

    /**
     * Cache the clazz. Do not access this directly. Use the {@link #getClazz()}
     * method instead.
     */
    private Class clazz;

    /**
     * Indicates that this is the latest version of the archetype descritpor.
     * Note that an archetype can be qualified by a version number.
     */
    private boolean isLatest;

    /**
     * Indicates whether this is a primary or top level archetype. Defaults
     * to true
     */
    private boolean primary = true;

    /**
     * A list of {@link NodeDescriptor} that belong to this archetype
     * descriptor.
     */
    private Map<String, org.openvpms.component.model.archetype.NodeDescriptor> nodeDescriptors = new LinkedHashMap<>();

    /**
     * Serialization version identifier.
     */
    private static final long serialVersionUID = 2L;


    /**
     * Default constructor.
     */
    public ArchetypeDescriptor() {
        setArchetypeId(new ArchetypeId("descriptor.archetype.1.0"));
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.common.IMObject#setName(java.lang.String)
     */
    @Override
    public void setName(String name) {
        super.setName(name);
        if (StringUtils.isEmpty(name)) {
            type = null;
        } else {
            type = new ArchetypeId(name);
        }
    }

    /**
     * Returns the archetype type.
     *
     * @return the archetype type
     */
    @Override
    public String getArchetypeType() {
        return type == null ? null : type.getShortName();
    }

    /**
     * Return the archetype id, which is also the type
     *
     * @return String
     */
    public ArchetypeId getType() {
        return type;
    }

    /**
     * @return Returns the associated Java class name.
     */
    public String getClassName() {
        return className;
    }

    /**
     * @param className the class name.
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * Returns the class for the specified type.
     *
     * @return the class, or <tt>null</tt> if {@link #getType()} returns
     * empty/null
     * @throws DescriptorException if the class can't be loaded
     */
    public Class getClazz() {
        if (clazz == null) {
            synchronized (this) {
                clazz = getClass(className);
            }
        }
        return clazz;
    }

    /**
     * @return Returns the isLatest.
     */
    public boolean isLatest() {
        return isLatest;
    }

    /**
     * @param isLatest The isLatest to set.
     */
    public void setLatest(boolean isLatest) {
        this.isLatest = isLatest;
    }

    /**
     * @return Returns the primary.
     */
    public boolean isPrimary() {
        return primary;
    }

    /**
     * @param primary The primary to set.
     */
    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    /**
     * Add a node descriptor to this archetype descripor
     *
     * @param node the node descriptor to add
     * @throws DescriptorException if we are adding a node descriptor with the same name
     */
    public void addNodeDescriptor(org.openvpms.component.model.archetype.NodeDescriptor node) {
        if (nodeDescriptors.containsKey(node.getName())) {
            throw new DescriptorException(
                    DescriptorException.ErrorCode.DuplicateNodeDescriptor,
                    node.getName(), getName());
        }
        NodeDescriptor descriptor = (NodeDescriptor) node;
        nodeDescriptors.put(node.getName(), descriptor);
        descriptor.setArchetypeDescriptor(this);
    }

    /**
     * Remove the specified node descriptor
     *
     * @param node the node descriptor to remove
     */
    public void removeNodeDescriptor(org.openvpms.component.model.archetype.NodeDescriptor node) {
        NodeDescriptor removed = (NodeDescriptor) nodeDescriptors.remove(node.getName());
        if (removed != null) {
            removed.setArchetypeDescriptor(null);
        }
    }

    /**
     * Remove the specified node descriptor. This will iterate the node
     * descriptor hierarchy and remove the first one with the specified
     * name
     *
     * @param nodeName the name of the node descriptor
     */
    public void removeNodeDescriptor(String nodeName) {
        removeNodeDescriptorWithName(getNodeDescriptors(), nodeName);
    }

    /**
     * Return the top level  node descriptors. The caller must be aware that
     * a {@link NodeDescriptor can contain other node descriptors.
     * <p>
     * TODO Inconsistent return type...change to List
     *
     * @return NodeDescriptor[]
     */
    public NodeDescriptor[] getNodeDescriptorsAsArray() {
        return nodeDescriptors.values().toArray(new NodeDescriptor[nodeDescriptors.size()]);
    }

    /**
     * Return the simple node descriptors. These are node descriptors that do
     * not have an archetypeRange assertion defined.
     *
     * @return List<NodeDescriptor>
     */
    public List<NodeDescriptor> getSimpleNodeDescriptors() {
        List<NodeDescriptor> all = getAllNodeDescriptors();
        List<NodeDescriptor> simple = new ArrayList<>();
        for (NodeDescriptor node : all) {
            if (!node.isComplexNode()) {
                simple.add(node);
            }
        }

        return simple;
    }

    /**
     * Return the comple node descriptors. These are node Descriptors  that
     * have an archetypeRage assertions defined.
     *
     * @return List<NodeDescriptor>
     */
    public List<NodeDescriptor> getComplexNodeDescriptors() {
        List<NodeDescriptor> all = getAllNodeDescriptors();
        List<NodeDescriptor> complex = new ArrayList<>();
        for (NodeDescriptor node : all) {
            if (node.isComplexNode()) {
                complex.add(node);
            }
        }

        return complex;
    }

    /**
     * Return all the {@link NodeDescriptor} for this archetype. This
     * will basically flatten out the hierarchical node descriptor
     * structure
     *
     * @return List<NodeDescriptor>
     */
    public List<NodeDescriptor> getAllNodeDescriptors() {
        List<NodeDescriptor> nodes = new ArrayList<>();
        getAllNodeDescriptors(getNodeDescriptorsAsArray(), nodes);
        return nodes;
    }

    /**
     * Return the {@link NodeDescriptor} instances as a map of name and
     * descriptor
     *
     * @return Returns the nodeDescriptors.
     */
    public Map<String, org.openvpms.component.model.archetype.NodeDescriptor> getNodeDescriptors() {
        return nodeDescriptors;
    }

    /**
     * @param nodes The nodeDescriptors to set.
     */
    public void setNodeDescriptorsAsArray(NodeDescriptor[] nodes) {
        this.nodeDescriptors = new LinkedHashMap<>();
        int index = 0;
        for (NodeDescriptor node : nodes) {
            node.setIndex(index++);
            addNodeDescriptor(node);
        }
    }

    /**
     * Return the archetype short name .
     *
     * @return String
     * the node name
     */
    public String getShortName() {
        return type == null ? null : type.getShortName();
    }

    /**
     * Return the display name. If a display name is not specified then
     * return the archtypes short name
     *
     * @return String
     * the display name
     */
    public String getDisplayName() {
        return StringUtils.isEmpty(displayName) ? getShortName() : displayName;
    }

    /**
     * @param displayName The displayName to set.
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Return the node descriptor for the specified node name
     *
     * @param name the node name
     * @return NodeDescriptor
     */
    public NodeDescriptor getNodeDescriptor(String name) {
        return findNodeDescriptorWithName(getNodeDescriptorsAsArray(), name);
    }

    /**
     * validate the descriptor. The method will return a list of validation
     * errors. An empty list means that the descriptor is valid.
     *
     * @return List<DescriporValidationError>
     */
    public List<DescriptorValidationError> validate() {
        List<DescriptorValidationError> errors = new ArrayList<>();

        if (type == null) {
            errors.add(new DescriptorValidationError(
                    Descriptor.DescriptorType.ArchetypeDescriptor, null,
                    "type", Descriptor.ValidationError.IsRequired));
        }

        if (StringUtils.isEmpty(className)) {
            errors.add(new DescriptorValidationError(
                    Descriptor.DescriptorType.ArchetypeDescriptor, null,
                    "className", Descriptor.ValidationError.IsRequired));
        }

        // validate that there are no duplicate node descriptor names
        List<NodeDescriptor> nodes = getAllNodeDescriptors();
        Map<String, NodeDescriptor> names = new HashMap<>();

        for (NodeDescriptor node : nodes) {
            if (names.containsKey(node.getName())) {
                errors.add(new DescriptorValidationError(
                        Descriptor.DescriptorType.NodeDescriptor,
                        node.getName(), "name",
                        Descriptor.ValidationError.DuplicateNodeDescriptor));
            }
            names.put(node.getName(), node);
        }

        return errors;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return (type != null) ? type.hashCode() : super.hashCode();
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.common.IMObject#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, STYLE)
                .appendSuper(super.toString())
                .append("type", type)
                .append("displayName", displayName)
                .append("className", className)
                .append("isLatest", isLatest)
                .append("primary", primary)
                .append("nodeDescriptors", nodeDescriptors)
                .toString();
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.archetype.descriptor.Descriptor#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        ArchetypeDescriptor copy = (ArchetypeDescriptor) super.clone();
        copy.nodeDescriptors = new LinkedHashMap<>(this.nodeDescriptors);
        copy.primary = this.primary;
        copy.type = (ArchetypeId) (type == null ? null : this.type.clone());

        return copy;
    }

    /**
     * Set the archetype id
     *
     * @param type the archetype id
     */
    protected void setType(ArchetypeId type) {
        this.type = type;
    }

    /**
     * Search the node descriptors recursively searching for the
     * specified name
     *
     * @param nodes the list of NodeDescriptors to search
     * @param name  the name to search for
     * @return NodeDescriptor
     * the node descriptor or null
     */
    private NodeDescriptor findNodeDescriptorWithName(NodeDescriptor[] nodes, String name) {
        for (NodeDescriptor node : nodes) {
            if (node.getName().equals(name)) {
                return node;
            }

            if (node.getNodeDescriptorsAsArray().length > 0) {
                NodeDescriptor result = findNodeDescriptorWithName(
                        node.getNodeDescriptorsAsArray(), name);
                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }

    /**
     * Remove the node the node descriptors recursively searching for the
     * specified name
     *
     * @param nodes    the list of NodeDescriptors to search
     * @param nodeName the name to search for
     */
    @SuppressWarnings("unchecked")
    private boolean removeNodeDescriptorWithName(
            Map<String, org.openvpms.component.model.archetype.NodeDescriptor> nodes,
            String nodeName) {
        if (nodes.remove(nodeName) != null) {
            return true;
        }
        for (org.openvpms.component.model.archetype.NodeDescriptor n: nodes.values()) {
            NodeDescriptor node = (NodeDescriptor) n;
            if (node.getNodeDescriptors().size() > 0) {
                Map map = node.getNodeDescriptors();
                if (removeNodeDescriptorWithName(
                        (Map<String, org.openvpms.component.model.archetype.NodeDescriptor>) map, nodeName)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * This is a recursive function that returns all the nodes in this archetype descriptor.
     *
     * @param nodes  the node descriptors to process
     * @param result the resultant node array
     */
    private void getAllNodeDescriptors(NodeDescriptor[] nodes, List<NodeDescriptor> result) {
        Arrays.sort(nodes, new NodeDescriptorIndexComparator());

        for (NodeDescriptor node : nodes) {
            result.add(node);
            if (node.getNodeDescriptorsAsArray().length > 0) {
                getAllNodeDescriptors(node.getNodeDescriptorsAsArray(), result);
            }
        }
    }

    /**
     * This comparator is used to compare the indices of NodeDescriptors
     */
    private static class NodeDescriptorIndexComparator implements Comparator<NodeDescriptor> {

        /* (non-Javadoc)
         * @see java.util.Comparator#compare(T, T)
         */
        public int compare(NodeDescriptor no1, NodeDescriptor no2) {
            if (no1 == no2) {
                return 0;
            }
            return Integer.compare(no1.getIndex(), no2.getIndex());
        }
    }
}