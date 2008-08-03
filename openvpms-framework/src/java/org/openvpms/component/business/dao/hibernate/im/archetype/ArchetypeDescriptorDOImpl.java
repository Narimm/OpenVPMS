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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.dao.hibernate.im.archetype;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.archetype.descriptor.DescriptorException;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;

import java.util.LinkedHashMap;
import java.util.Map;


/**
 * Implementation of the {@link ArchetypeDescriptorDO} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2007-08-24 16:54:12 +1000 (Fri, 24 Aug 2007) $
 */
public class ArchetypeDescriptorDOImpl extends DescriptorDOImpl
        implements ArchetypeDescriptorDO {

    /**
     * The type of the archetype.
     */
    private ArchetypeId type;

    /**
     * The display name of the archetype.
     */
    private String displayName;

    /**
     * The full-qualified Java domain class.
     */
    private String className;

    /**
     * Determines if this is the latest version of the archetype descriptor.
     */
    private boolean latest;

    /**
     * Indicates whether this is a primary or top level archetype. Defaults
     * to <tt>true</tt>.
     */
    private boolean primary = true;

    /**
     * A list of {@link NodeDescriptor}s that belong to this archetype
     * descriptor.
     */
    private Map<String, NodeDescriptorDO> nodeDescriptors =
            new LinkedHashMap<String, NodeDescriptorDO>();


    /**
     * Default constructor.
     */
    public ArchetypeDescriptorDOImpl() {
        setArchetypeId(new ArchetypeId("descriptor.archetype.1.0"));
    }

    /**
     * Sets the object name.
     *
     * @param name the object name. May be <tt>null</tt>
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
     * @return the archetype id
     */
    public ArchetypeId getType() {
        return type;
    }

    /**
     * Returns the java class name.
     *
     * @return the class name
     */
    public String getClassName() {
        return className;
    }

    /**
     * Sets the java class name.
     *
     * @param className the class name
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * Determines if this is the latest version of the archetype.
     *
     * @return <tt>true</tt> if this is the latest version
     */
    public boolean isLatest() {
        return latest;
    }

    /**
     * Determines if this is the latest version of the archetype.
     *
     * @param latest <tt>true</tt> if this is the latest version
     */
    public void setLatest(boolean latest) {
        this.latest = latest;
    }

    /**
     * Determines if this is a primary archetype.
     *
     * @return <tt>true</tt> if this is a primary archetype
     */
    public boolean isPrimary() {
        return primary;
    }

    /**
     * Determines if this is a primary archetype.
     *
     * @param primary <tt>true</tt> if this is a primary archetype
     */
    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    /**
     * Add a node descriptor to this archetype descripor.
     *
     * @param node the node descriptor to add
     * @throws DescriptorException if a node descriptor exists with the same
     *                             name
     */
    public void addNodeDescriptor(NodeDescriptorDO node) {
        if (nodeDescriptors.containsKey(node.getName())) {
            throw new DescriptorException(
                    DescriptorException.ErrorCode.DuplicateNodeDescriptor,
                    node.getName(), getName());
        }
        nodeDescriptors.put(node.getName(), node);
        node.setArchetypeDescriptor(this);
    }

    /**
     * Removes the specified node descriptor.
     *
     * @param node the node descriptor to remove
     */
    public void removeNodeDescriptor(NodeDescriptorDO node) {
        NodeDescriptorDO removed = nodeDescriptors.remove(node.getName());
        if (removed != null) {
            removed.setArchetypeDescriptor(null);
        }
    }

    /**
     * Return the top-level node descriptors, keyed on name.
     *
     * @return the top-level node descriptors
     */
    public Map<String, NodeDescriptorDO> getNodeDescriptors() {
        return this.nodeDescriptors;
    }

    /**
     * Return all the node descriptors for this archetype, keyed on name.
     * <p/>
     * This flattens out the node descriptor heirarchy.
     *
     * @return the node descriptors
     */
    public Map<String, NodeDescriptorDO> getAllNodeDescriptors() {
        Map<String, NodeDescriptorDO> result
                = new LinkedHashMap<String, NodeDescriptorDO>();
        for (NodeDescriptorDO descriptor : nodeDescriptors.values()) {
            result.put(descriptor.getName(), descriptor);
            result.putAll(descriptor.getNodeDescriptors());
        }
        return result;
    }

    /**
     * Returns the named node descriptor.
     *
     * @param name the node descriptor name
     * @return the corresponding node descriptor, or <tt>null</tt> if none
     *         is found
     */
    public NodeDescriptorDO getNodeDescriptor(String name) {
        return getAllNodeDescriptors().get(name);
    }

    /**
     * Returns the display name.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the display name.
     *
     * @param displayName the display name
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }


    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value for this object
     */
    @Override
    public int hashCode() {
        return (type != null) ? type.hashCode() : super.hashCode();
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param obj the reference object with which to compare.
     * @return <tt>true</tt> if this object is the same as the obj
     *         argument; <tt>false</tt> otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ArchetypeDescriptorDO)) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        ArchetypeDescriptorDO desc = (ArchetypeDescriptorDO) obj;
        return type.equals(desc.getType());
    }

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("name", getName())
                .append("type", type)
                .append("displayName", displayName)
                .append("className", className)
                .append("isLatest", latest)
                .append("primary", primary)
                .append("nodeDescriptors", nodeDescriptors)
                .toString();
    }

    /**
     * Set the archetype type.
     *
     * @param type the archetype type
     */
    protected void setType(ArchetypeId type) {
        this.type = type;
    }

    /**
     * Sets the node descriptors.
     *
     * @param descriptors the node descriptors
     */
    protected void setNodeDescriptors(
            Map<String, NodeDescriptorDO> descriptors) {
        nodeDescriptors = descriptors;
    }
}
