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
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionDescriptor;
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
     * Contains a list of {@link AssertionDescriptor} instances
     */
    private Map<String, AssertionDescriptorDO> assertionDescriptors =
            new LinkedHashMap<String, AssertionDescriptorDO>();

    /**
     * This is an option property, which is required for nodes that represent
     * collections. It is the name that denotes the individual elements stored
     * in the collection.
     */
    private String baseName;

    /**
     * The default value
     */
    private String defaultValue;

    /**
     * This is a jxpath expression, which is used to determine the value of the
     * node
     */
    private String derivedValue;

    /**
     * This is the display name, which is only supplied if it is different to
     * the node name
     */
    private String displayName;

    /**
     * The index of this discriptor within the collection
     */
    private int index;

    /**
     * Determine whether the value for this node is derived
     */
    private boolean isDerived = false;

    /**
     * Attribute, which defines whether this node is hidden or can be displayed
     */
    private boolean isHidden = false;

    /**
     * Indicates that the collection type is a parentChild relationship, which
     * is the default for a collection. If this attribute is set to false then
     * the child lifecycle is independent of the parent lifecycle. This
     * attribute is only meaningful for a collection
     */
    private boolean isParentChild = true;

    /**
     * Indicates whether the descriptor is readOnly
     */
    private boolean isReadOnly = false;

    /**
     * Indicates whether the node value represents an array
     */
    private boolean isArray = false;

    /**
     * The maximum cardinality, which defaults to 1
     */
    private int maxCardinality = 1;

    /**
     * The maximum length
     */
    private int maxLength;

    /**
     * The minimum cardinality, which defaults to 0
     */
    private int minCardinality = 0;

    /**
     * The minimum length
     */
    private int minLength;

    /**
     * A node can have other nodeDescriptors to define a nested structure
     */
    private Map<String, NodeDescriptorDO> nodeDescriptors
            = new LinkedHashMap<String, NodeDescriptorDO>();

    /**
     * The XPath/JXPath expression that is used to resolve this node within the
     * associated domain object.
     */
    private String path;

    /**
     * The fully qualified class name that defines the node type
     */
    private String type;

    /**
     * The filter is only valid for collections and defines the subset of
     * the collection that this node refers too.  The filter is an archetype
     * shortName, which can also be in the form of a regular expression
     * <p/>
     * The modeFilter is a regex compliant filter
     */
    private String filter;

    private static final ArchetypeId NODE = new ArchetypeId(
            "descriptor.node.1.0");

    private static final ArchetypeId COLLECTION_NODE = new ArchetypeId(
            "descriptor.collectionNode");

    /**
     * The parent node descriptor. May be <tt>null</tt>.
     */
    private NodeDescriptorDO parent;

    /**
     * The archetype that this descriptor belongs to. May be <tt>null</tt>.
     */
    private ArchetypeDescriptorDO archetype;


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
     * Add an assertion descriptor to this node
     *
     * @param descriptor
     */
    public void addAssertionDescriptor(AssertionDescriptorDO descriptor) {
        assertionDescriptors.put(descriptor.getName(), descriptor);
    }

    /**
     * Add a child node descriptor
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
     * Retrieve the assertion descriptor with the specified type or null if one
     * does not exist.
     *
     * @param type the type of the assertion descriptor
     * @return AssertionDescriptor
     */
    public AssertionDescriptorDO getAssertionDescriptor(String type) {
        return assertionDescriptors.get(type);
    }

    /**
     * Return the assertion descriptors as a map
     *
     * @return Returns the assertionDescriptors.
     */
    public Map<String,AssertionDescriptorDO> getAssertionDescriptors() {
        return assertionDescriptors;
    }

    /**
     * @return Returns the baseName.
     */
    public String getBaseName() {
        return baseName;
    }

    /**
     * @return Returns the defaultValue.
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * @return Returns the derivedValue.
     */
    public String getDerivedValue() {
        return derivedValue;
    }

    /**
     * @return Returns the displayName.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @return Returns the filter.
     */
    public String getFilter() {
        return filter;
    }

    /**
     * @return Returns the index.
     */
    public int getIndex() {
        return index;
    }

    /**
     * @return Returns the maxCardinality.
     */
    public int getMaxCardinality() {
        return maxCardinality;
    }

    /**
     * The getter that returns the max cardinality as a string
     *
     * @return String
     */
    public String getMaxCardinalityAsString() {
        if (maxCardinality == NodeDescriptorDO.UNBOUNDED) {
            return NodeDescriptorDO.UNBOUNDED_AS_STRING;
        } else {
            return Integer.toString(maxCardinality);
        }
    }

    /**
     * @return Returns the maxLength.
     */
    public int getMaxLength() {
        return maxLength <= 0 ? NodeDescriptorDO.DEFAULT_MAX_LENGTH : maxLength;
    }


    /**
     * @return Returns the minCardinality.
     */
    public int getMinCardinality() {
        return minCardinality;
    }

    /**
     * @return Returns the minLength.
     */
    public int getMinLength() {
        return minLength;
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
     * @return Returns the path.
     */
    public String getPath() {
        return path;
    }

    /**
     * @return Returns the typeName.
     */
    public String getType() {
        return type;
    }

    /**
     * Delete the specified assertion descriptor
     *
     * @param descriptor the assertion to delete
     */
    public void removeAssertionDescriptor(AssertionDescriptorDO descriptor) {
        assertionDescriptors.remove(descriptor.getName());
    }

    /**
     * Delete the assertion descriptor with the specified type
     *
     * @param type the type name
     */
    public void removeAssertionDescriptor(String type) {
        assertionDescriptors.remove(type);
    }


    /**
     * @param baseName The baseName to set.
     */
    public void setBaseName(String baseName) {
        this.baseName = baseName;
    }

    /**
     * @param defaultValue The defaultValue to set.
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }


    public boolean isDerived() {
        return isDerived;
    }

    /**
     * @param isDerived The isDerived to set.
     */
    public void setDerived(boolean isDerived) {
        this.isDerived = isDerived;
    }

    /**
     * @param derivedValue The derivedValue to set.
     */
    public void setDerivedValue(String derivedValue) {
        this.derivedValue = derivedValue;
    }

    /**
     * @param displayName The displayName to set.
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * @param filter The filter to set.
     */
    public void setFilter(String filter) {
        this.filter = filter;
    }

    /**
     * @param isHidden The isHidden to set.
     */
    public void setHidden(boolean isHidden) {
        this.isHidden = isHidden;
    }

    public boolean isHidden() {
        return isHidden;
    }

    /**
     * @param index The index to set.
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * @param maxCardinality The maxCardinality to set.
     */
    public void setMaxCardinality(int maxCardinality) {
        this.maxCardinality = maxCardinality;
    }

    /**
     * @param maxLength The maxLength to set.
     */
    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    /**
     * @param minCardinality The minCardinality to set.
     */
    public void setMinCardinality(int minCardinality) {
        this.minCardinality = minCardinality;
    }

    /**
     * @param minLength The minLength to set.
     */
    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    public boolean isParentChild() {
        return isParentChild;
    }

    /**
     * @param parentChild The parentChild to set.
     */
    public void setParentChild(boolean parentChild) {
        this.isParentChild = parentChild;
    }

    /**
     * @param path The path to set.
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Set the value of the readOnly attribute
     *
     * @param value
     */
    public void setReadOnly(boolean value) {
        this.isReadOnly = value;
    }

    public boolean isReadOnly() {
        return isReadOnly;
    }

    /**
     * @param type The type to set.
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

    /*
     * (non-Javadoc)
     *
     * @see org.openvpms.component.business.domain.im.common.IMObject#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this).append("name", getName()).append(
                "displayName", displayName).append("isHidden", isHidden)
                .append("isArray", isArray)
                .append("isDerived", isDerived).append("derivedValue",
                                                       derivedValue).append(
                "path", path).append("type", type)
                .append("defaultValue", defaultValue).append("minCardinality",
                                                             minCardinality)
                .append("maxCardinality", maxCardinality).append("minLength",
                                                                 minLength).append(
                "maxLength", maxLength).append(
                "baseName", baseName).append("isParentChild",
                                             isParentChild).append(
                "index",
                index).append("assertionDescriptors",
                              assertionDescriptors).append("nodeDescriptors",
                                                           nodeDescriptors).toString();
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

}
