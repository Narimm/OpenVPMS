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

import java.util.LinkedHashMap;
import java.util.Map;


/**
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2008-01-24 16:47:48 +1100 (Thu, 24 Jan 2008) $
 */
public class NodeDescriptorDOImpl
        extends DescriptorDOImpl implements NodeDescriptorDO {

    /**
     * The default value.
     */
    private String defaultValue;

    /**
     * Determines whether the value for this node is derived.
     */
    private boolean derived = false;

    /**
     * This is a jxpath expression, which is used to determine the value of the
     * node
     */
    private String derivedValue;

    /**
     * This is the display name, which is only supplied if it is different to
     * the node name.
     */
    private String displayName;

    /**
     * The filter is only valid for collections and defines the subset of
     * the collection that this node refers to.  The filter is an archetype
     * shortName, which may contain wildcards.
     */
    private String filter;

    /**
     * The index of this descriptor within the collection.
     */
    private int index;

    /**
     * Determines whether the node is hidden or can be displayed,
     */
    private boolean hidden = false;

    /**
     * Indicates whether the descriptor is readOnly
     */
    private boolean readOnly = false;

    /**
     * This is an optional property, which is required for nodes that represent
     * collections. It is the name that denotes the individual elements stored
     * in the collection.
     */
    private String baseName;

    /**
     * Indicates that the collection type is a parentChild relationship, which
     * is the default for a collection. If this attribute is set to false then
     * the child lifecycle is independent of the parent lifecycle. This
     * attribute is only meaningful for a collection.
     */
    private boolean isParentChild = true;

    /**
     * Indicates whether the node value represents an array.
     */
    private boolean isArray = false;

    /**
     * The maximum cardinality. Defaults to 1,
     */
    private int maxCardinality = 1;

    /**
     * The minimum cardinality.
     */
    private int minCardinality = 0;

    /**
     * The maximum length.
     */
    private int maxLength;

    /**
     * The minimum length.
     */
    private int minLength;

    /**
     * The parent node descriptor. May be <tt>null</tt>.
     */
    private NodeDescriptorDO parent;

    /**
     * The archetype that this descriptor belongs to. May be <tt>null</tt>.
     */
    private ArchetypeDescriptorDO archetype;

    /**
     * A node can have other nodeDescriptors to define a nested structure.
     */
    private Map<String, NodeDescriptorDO> nodeDescriptors
            = new LinkedHashMap<String, NodeDescriptorDO>();

    /**
     * The assertion descriptors, keyed on name.
     */
    private Map<String, AssertionDescriptorDO> assertionDescriptors =
            new LinkedHashMap<String, AssertionDescriptorDO>();

    /**
     * The XPath/JXPath expression that is used to resolve this node within the
     * associated domain object.
     */
    private String path;

    /**
     * The fully qualified class name that defines the node type
     */
    private String type;

    private static final ArchetypeId NODE = new ArchetypeId(
            "descriptor.node.1.0");

    private static final ArchetypeId COLLECTION_NODE = new ArchetypeId(
            "descriptor.collectionNode");


    /**
     * Default constructor.
     */
    public NodeDescriptorDOImpl() {
    }

    /**
     * Returns the archetype Id. For nodes that have child nodes, returns
     * <em>descriptor.collectionNode.1.0</em>, otherwise returns
     * <em>descriptor.node.1.0</em>.
     *
     * @return the archetype Id.
     */
    @Override
    public ArchetypeId getArchetypeId() {
        return (nodeDescriptors == null || nodeDescriptors.isEmpty())
                ? NodeDescriptorDOImpl.NODE
                : NodeDescriptorDOImpl.COLLECTION_NODE;
    }

    /**
     * Returns the default value.
     *
     * @return the default value. May be <tt>null</tt>
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * Sets the default value.
     *
     * @param defaultValue the default value. May be <tt>null</tt>
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * Determines if this is a derived node.
     *
     * @return <tt>true</tt> if this is a derived node
     */
    public boolean isDerived() {
        return derived;
    }

    /**
     * Determines if this is a derived node.
     *
     * @param derived if <tt>true</tt> indicates the node is derived
     */
    public void setDerived(boolean derived) {
        this.derived = derived;
    }

    /**
     * Returns the derived value.
     *
     * @return the derived value
     */
    public String getDerivedValue() {
        return derivedValue;
    }

    /**
     * Sets the derived value.
     *
     * @param derivedValue the derived value. May be <tt>null</tt>
     */
    public void setDerivedValue(String derivedValue) {
        this.derivedValue = derivedValue;
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
     * @param displayName the display name. May be <tt>null</tt>
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the filter.
     *
     * @return the filter
     */
    public String getFilter() {
        return filter;
    }

    /**
     * Sets the filter.
     *
     * @param filter the filter. May be <tt>null</tt>
     */
    public void setFilter(String filter) {
        this.filter = filter;
    }

    /**
     * Returns the node index.
     *
     * @return the node index
     */
    public int getIndex() {
        return index;
    }

    /**
     * Sets the node index.
     *
     * @param index the index
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * Returns the base name.
     *
     * @return the base name. May be <tt>null</tt>
     */
    public String getBaseName() {
        return baseName;
    }

    /**
     * Sets the base name.
     *
     * @param baseName the base name to set
     */
    public void setBaseName(String baseName) {
        this.baseName = baseName;
    }

    /**
     * Determines if this node is a parent-child node.
     *
     * @return <tt>true</tt> if this a parent-child node
     */
    public boolean isParentChild() {
        return isParentChild;
    }

    /**
     * Determines if this node is a parent-child node.
     *
     * @param parentChild if <tt>true</tt>, indicates this is a parent-child
     *                    node
     */
    public void setParentChild(boolean parentChild) {
        this.isParentChild = parentChild;
    }

    /**
     * Returns the maximum cardinality.
     *
     * @return the maximum cardinality, or {@link #UNBOUNDED} if it is unbounded
     */
    public int getMaxCardinality() {
        return maxCardinality;
    }

    /**
     * Sets the maximum cardinality.
     *
     * @param cardinality the maximum cardinality. Use {@link #UNBOUNDED} to
     *                    indicate an unbounded cardinality
     */
    public void setMaxCardinality(int cardinality) {
        this.maxCardinality = cardinality;
    }

    /**
     * Returns the minimum cardinality.
     *
     * @return the minimum cardinality
     */
    public int getMinCardinality() {
        return minCardinality;
    }

    /**
     * Sets the minimum cardinality.
     *
     * @param cardinality the minimum cardinality
     */
    public void setMinCardinality(int cardinality) {
        this.minCardinality = cardinality;
    }

    /**
     * Returns the maximum length.
     *
     * @return the maximum length
     */
    public int getMaxLength() {
        return maxLength <= 0 ? NodeDescriptorDO.DEFAULT_MAX_LENGTH : maxLength;
    }

    /**
     * Sets the maximum length.
     *
     * @param length the maximum length
     */
    public void setMaxLength(int length) {
        this.maxLength = length;
    }

    /**
     * Returns the minimum length.
     *
     * @return the minimum length
     */
    public int getMinLength() {
        return minLength;
    }

    /**
     * Sets the minimum length.
     *
     * @param length the minimum length
     */
    public void setMinLength(int length) {
        this.minLength = length;
    }

    /**
     * Returns the path.
     *
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * Sets the node path.
     *
     * @param path the path
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Returns the type name.
     *
     * @return the type name
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type name.
     *
     * @param type the type name
     */
    public void setType(String type) {
        if (StringUtils.isEmpty(type)) {
            this.type = null;
        } else {
            if (type.endsWith("[]")) {
                this.isArray = true;
                this.type = type.substring(0, type.indexOf("[]"));
            } else {
                this.type = type;
            }
        }
    }

    /**
     * Determines if the node is hidden.
     *
     * @return <tt>true</tt> if the node is hidden
     */
    public boolean isHidden() {
        return hidden;
    }

    /**
     * Determines if the node is hidden.
     *
     * @param hidden if <tt>true</tt>, indicates the node is hidden
     */
    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    /**
     * Determines if the node is read-only.
     *
     * @return <tt>true</tt> if the node is read-only
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * Determines if the node is read-only.
     *
     * @param readOnly if <tt>true</tt>, indicates the node is read-only
     */
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    /**
     * Returns the archetype descriptor that this is a node of.
     *
     * @return the archetype descriptor that this is a node of. May be
     *         <code>null</code>
     */
    public ArchetypeDescriptorDO getArchetypeDescriptor() {
        return archetype;
    }

    /**
     * Sets the archetype descriptor.
     *
     * @param descriptor the archetype descriptor
     */
    public void setArchetypeDescriptor(ArchetypeDescriptorDO descriptor) {
        archetype = descriptor;
    }

    /**
     * Returns the parent node descriptor.
     *
     * @return the parent node descriptor or <code>null</code>, if this node
     *         has no parent.
     */
    public NodeDescriptorDO getParent() {
        return parent;
    }

    /**
     * Sets the parent node descriptor.
     *
     * @param parent the parent node descriptor, or <code>null</code> if this
     *               node has no parent
     */
    public void setParent(NodeDescriptorDO parent) {
        this.parent = parent;
    }

    /**
     * Returns the immediate child node descriptors, keyed on name.
     *
     * @return the node descriptors
     */
    public Map<String, NodeDescriptorDO> getNodeDescriptors() {
        return nodeDescriptors;
    }

    /**
     * Return all the child node descriptors, keyed on name.
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
     * Adds a child node descriptor.
     *
     * @param child the child node descriptor to add
     * @throws DescriptorException if the node is a duplicate
     */
    public void addNodeDescriptor(NodeDescriptorDO child) {
        if (nodeDescriptors.containsKey(child.getName())) {
            throw new DescriptorException(
                    DescriptorException.ErrorCode.DuplicateNodeDescriptor,
                    child.getName(), getName());
        }
        nodeDescriptors.put(child.getName(), child);
        child.setParent(this);
    }

    /**
     * Returns the assertion descriptor with the specified name.
     *
     * @param name the name of the assertion descriptor
     * @return the corresponding descriptor, or <tt>null</tt> if it doesn't
     *         exist
     */
    public AssertionDescriptorDO getAssertionDescriptor(String name) {
        return assertionDescriptors.get(name);
    }

    /**
     * Returns the assertion descriptors.
     *
     * @return the assertion descriptors, keyed on name
     */
    public Map<String, AssertionDescriptorDO> getAssertionDescriptors() {
        return assertionDescriptors;
    }

    /**
     * Adds an assertion descriptor.
     *
     * @param descriptor the assertion descriptor to add
     */
    public void addAssertionDescriptor(AssertionDescriptorDO descriptor) {
        assertionDescriptors.put(descriptor.getName(), descriptor);
    }

    /**
     * Removes an assertion descriptor.
     *
     * @param descriptor the assertion to remove
     */
    public void removeAssertionDescriptor(AssertionDescriptorDO descriptor) {
        assertionDescriptors.remove(descriptor.getName());
    }

    /**
     * Removes an assertion descriptor with the specified name.
     *
     * @param name the assertion descriptor name
     */
    public void removeAssertionDescriptor(String name) {
        assertionDescriptors.remove(name);
    }

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this).append("name", getName())
                .append("displayName", displayName).append("isHidden", hidden)
                .append("isArray", isArray).append("isDerived", derived)
                .append("derivedValue", derivedValue).append("path", path)
                .append("type", type).append("defaultValue", defaultValue)
                .append("minCardinality", minCardinality)
                .append("maxCardinality", maxCardinality)
                .append("minLength", minLength).append("maxLength", maxLength)
                .append("baseName", baseName)
                .append("isParentChild", isParentChild).append("index", index)
                .append("assertionDescriptors", assertionDescriptors)
                .append("nodeDescriptors", nodeDescriptors).toString();
    }

}
