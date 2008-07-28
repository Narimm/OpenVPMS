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

import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.archetype.descriptor.DescriptorException;

import java.util.Map;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface NodeDescriptorDO extends DescriptorDO {
    /**
     * The default display length if one is not defined in the node definition
     */
    int DEFAULT_DISPLAY_LENGTH = 50;
    /**
     * The default maximum Length if one is not defined in the node definition
     */
    int DEFAULT_MAX_LENGTH = 255;
    /**
     * This is used to identify a max cardinality that is unbounded
     */
    int UNBOUNDED = -1;
    /**
     * Representation of max cardinality as a string
     */
    String UNBOUNDED_AS_STRING = "*";

    /**
     * Returns the archetype Id. For nodes that have child nodes, returns
     * <em>descriptor.collectionNode.1.0</em>, otherwise returns
     * <em>descriptor.node.1.0</em>.
     *
     * @return the archetype Id.
     */
    ArchetypeId getArchetypeId();

    /**
     * Add an assertion descriptor to this node
     *
     * @param descriptor
     */
    void addAssertionDescriptor(AssertionDescriptorDO descriptor);

    /**
     * Add a child node descriptor
     *
     * @param child the child node descriptor to add
     * @throws DescriptorException if the node is a duplicate
     */
    void addNodeDescriptor(NodeDescriptorDO child);

    /**
     * Retrieve the assertion descriptor with the specified type or null if one
     * does not exist.
     *
     * @param type the type of the assertion descriptor
     * @return AssertionDescriptor
     */
    AssertionDescriptorDO getAssertionDescriptor(String type);

    /**
     * Return the assertion descriptors as a map
     *
     * @return Returns the assertionDescriptors.
     */
    Map<String,AssertionDescriptorDO> getAssertionDescriptors();

    /**
     * @return Returns the baseName.
     */
    String getBaseName();

    /**
     * @return Returns the defaultValue.
     */
    String getDefaultValue();

    /**
     * @return Returns the derivedValue.
     */
    String getDerivedValue();

    /**
     * @return Returns the displayName.
     */
    String getDisplayName();

    /**
     * @return Returns the filter.
     */
    String getFilter();

    /**
     * @return Returns the index.
     */
    int getIndex();

    /**
     * @return Returns the maxCardinality.
     */
    int getMaxCardinality();

    /**
     * The getter that returns the max cardinality as a string
     *
     * @return String
     */
    String getMaxCardinalityAsString();

    /**
     * @return Returns the maxLength.
     */
    int getMaxLength();

    /**
     * @return Returns the minCardinality.
     */
    int getMinCardinality();

    /**
     * @return Returns the minLength.
     */
    int getMinLength();

    /**
     * Returns the immediate child node descriptors, keyed on name.
     *
     * @return the node descriptors
     */
    Map<String, NodeDescriptorDO> getNodeDescriptors();

    /**
     * Return all the child node descriptors, keyed on name.
     * <p/>
     * This flattens out the node descriptor heirarchy.
     *
     * @return the node descriptors
     */
    Map<String, NodeDescriptorDO> getAllNodeDescriptors();

    /**
     * @return Returns the path.
     */
    String getPath();

    /**
     * @return Returns the typeName.
     */
    String getType();

    /**
     * Delete the specified assertion descriptor
     *
     * @param descriptor the assertion to delete
     */
    void removeAssertionDescriptor(AssertionDescriptorDO descriptor);

    /**
     * Delete the assertion descriptor with the specified type
     *
     * @param type the type name
     */
    void removeAssertionDescriptor(String type);

    /**
     * @param baseName The baseName to set.
     */
    void setBaseName(String baseName);

    /**
     * @param defaultValue The defaultValue to set.
     */
    void setDefaultValue(String defaultValue);

    boolean isDerived();

    /**
     * @param isDerived The isDerived to set.
     */
    void setDerived(boolean isDerived);

    /**
     * @param derivedValue The derivedValue to set.
     */
    void setDerivedValue(String derivedValue);

    /**
     * @param displayName The displayName to set.
     */
    void setDisplayName(String displayName);

    /**
     * @param filter The filter to set.
     */
    void setFilter(String filter);

    /**
     * @param isHidden The isHidden to set.
     */
    void setHidden(boolean isHidden);

    boolean isHidden();

    /**
     * @param index The index to set.
     */
    void setIndex(int index);

    /**
     * @param maxCardinality The maxCardinality to set.
     */
    void setMaxCardinality(int maxCardinality);

    /**
     * @param maxLength The maxLength to set.
     */
    void setMaxLength(int maxLength);

    /**
     * @param minCardinality The minCardinality to set.
     */
    void setMinCardinality(int minCardinality);

    /**
     * @param minLength The minLength to set.
     */
    void setMinLength(int minLength);

    boolean isParentChild();

    /**
     * @param parentChild The parentChild to set.
     */
    void setParentChild(boolean parentChild);

    /**
     * @param path The path to set.
     */
    void setPath(String path);

    /**
     * Set the value of the readOnly attribute
     *
     * @param value
     */
    void setReadOnly(boolean value);

    boolean isReadOnly();

    /**
     * @param type The type to set.
     */
    void setType(String type);

    /**
     * Returns the archetype descriptor that this is a node of.
     *
     * @return the archetype descriptor that this is a node of. May be
     *         <tt>null</tt>
     */
    ArchetypeDescriptorDO getArchetypeDescriptor();

    /**
     * Sets the archetype descriptor.
     *
     * @param descriptor the archetype descriptor
     */
    void setArchetypeDescriptor(ArchetypeDescriptorDO descriptor);
    /**
     * Returns the parent node descriptor.
     *
     * @return the parent node descriptor or <tt>null</tt>, if this node
     *         has no parent.
     */
    NodeDescriptorDO getParent();

    void setParent(NodeDescriptorDO parent);

}
