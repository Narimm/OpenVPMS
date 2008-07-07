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
 * The archetype descriptor is used to describe an archetype.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2007-08-24 16:54:12 +1000 (Fri, 24 Aug 2007) $
 */
public class ArchetypeDescriptorDO extends DescriptorDO {

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
    private Map<String, NodeDescriptorDO> nodeDescriptors =
            new LinkedHashMap<String, NodeDescriptorDO>();

    /**
     * Default constructor
     */
    public ArchetypeDescriptorDO() {
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
     * Return the archetype id, which is also the type
     *
     * @return String
     */
    public ArchetypeId getType() {
        return type;
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
     * Remove the specified node descriptor
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
        Map<String, NodeDescriptorDO> result = new LinkedHashMap<String, NodeDescriptorDO>();
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
     * @param displayName The displayName to set.
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }


    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return (type != null) ? type.hashCode() : super.hashCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        // is it of the correct type
        if (!(obj instanceof ArchetypeDescriptorDO)) {
            return false;
        }

        // are they the same object
        if (this == obj) {
            return true;
        }

        ArchetypeDescriptorDO desc = (ArchetypeDescriptorDO) obj;
        return type.equals(desc.type);
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.common.IMObject#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("name", getName())
                .append("type", type)
                .append("displayName", displayName)
                .append("className", className)
                .append("isLatest", isLatest)
                .append("primary", primary)
                .append("nodeDescriptors", nodeDescriptors)
                .toString();
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
