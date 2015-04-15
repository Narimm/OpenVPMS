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
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;

import java.util.Map;


/**
 * Data object interface corresponding to the {@link NodeDescriptor} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface NodeDescriptorDO extends DescriptorDO {

    /**
     * The default display length if one is not defined in the node definition.
     */
    int DEFAULT_DISPLAY_LENGTH = 50;

    /**
     * The default maximum length if one is not defined in the node definition.
     */
    int DEFAULT_MAX_LENGTH = 255;

    /**
     * Used to identify a max cardinality that is unbounded.
     */
    int UNBOUNDED = -1;


    /**
     * Returns the archetype identifier.
     * <p/>
     * For nodes that have child nodes, returns
     * <em>descriptor.collectionNode.1.0</em>, otherwise returns
     * <em>descriptor.node.1.0</em>.
     *
     * @return the archetype identifier
     */
    ArchetypeId getArchetypeId();

    /**
     * Returns the default value.
     *
     * @return the default value. May be <tt>null</tt>
     */
    String getDefaultValue();

    /**
     * Sets the default value.
     *
     * @param defaultValue the default value. May be <tt>null</tt>
     */
    void setDefaultValue(String defaultValue);

    /**
     * Determines if this is a derived node.
     *
     * @return <tt>true</tt> if this is a derived node
     */
    boolean isDerived();

    /**
     * Determines if this is a derived node.
     *
     * @param derived if <tt>true</tt> indicates the node is derived
     */
    void setDerived(boolean derived);

    /**
     * Returns the derived value.
     *
     * @return the derived value
     */
    String getDerivedValue();

    /**
     * Sets the derived value.
     *
     * @param derivedValue the derived value. May be <tt>null</tt>
     */
    void setDerivedValue(String derivedValue);

    /**
     * Returns the display name.
     *
     * @return the display name
     */
    String getDisplayName();

    /**
     * Sets the display name.
     *
     * @param displayName the display name. May be <tt>null</tt>
     */
    void setDisplayName(String displayName);

    /**
     * Returns the filter.
     *
     * @return the filter
     */
    String getFilter();

    /**
     * Sets the filter.
     *
     * @param filter the filter. May be <tt>null</tt>
     */
    void setFilter(String filter);

    /**
     * Returns the node index.
     *
     * @return the node index
     */
    int getIndex();

    /**
     * Sets the node index.
     *
     * @param index the index
     */
    void setIndex(int index);

    /**
     * Returns the maximum cardinality.
     *
     * @return the maximum cardinality, or {@link #UNBOUNDED} if it is unbounded
     */
    int getMaxCardinality();

    /**
     * Sets the maximum cardinality.
     *
     * @param cardinality the maximum cardinality. Use {@link #UNBOUNDED} to
     *                    indicate an unbounded cardinality
     */
    void setMaxCardinality(int cardinality);

    /**
     * Returns the minimum cardinality.
     *
     * @return the minimum cardinality
     */
    int getMinCardinality();

    /**
     * Sets the minimum cardinality.
     *
     * @param cardinality the minimum cardinality
     */
    void setMinCardinality(int cardinality);

    /**
     * Returns the maximum length.
     *
     * @return the maximum length
     */
    int getMaxLength();

    /**
     * Sets the maximum length.
     *
     * @param length the maximum length
     */
    void setMaxLength(int length);

    /**
     * Returns the minimum length.
     *
     * @return the minimum length
     */
    int getMinLength();

    /**
     * Sets the minimum length.
     *
     * @param length the minimum length
     */
    void setMinLength(int length);

    /**
     * Returns the path.
     *
     * @return the path
     */
    String getPath();

    /**
     * Sets the node path.
     *
     * @param path the path
     */
    void setPath(String path);

    /**
     * Returns the type name.
     *
     * @return the type name
     */
    String getType();

    /**
     * Sets the type name.
     *
     * @param type the type name
     */
    void setType(String type);

    /**
     * Determines if the node is hidden.
     *
     * @return <tt>true</tt> if the node is hidden
     */
    boolean isHidden();

    /**
     * Determines if the node is hidden.
     *
     * @param hidden if <tt>true</tt>, indicates the node is hidden
     */
    void setHidden(boolean hidden);

    /**
     * Determines if the node is read-only.
     *
     * @param readOnly if <tt>true</tt>, indicates the node is read-only
     */
    void setReadOnly(boolean readOnly);

    /**
     * Determines if the node is read-only.
     *
     * @return <tt>true</tt> if the node is read-only
     */
    boolean isReadOnly();

    /**
     * Returns the base name.
     *
     * @return the base name. May be <tt>null</tt>
     */
    String getBaseName();

    /**
     * Sets the base name.
     *
     * @param baseName the base name to set
     */
    void setBaseName(String baseName);

    /**
     * Determines if this node is a parent-child node.
     *
     * @return <tt>true</tt> if this a parent-child node
     */
    boolean isParentChild();

    /**
     * Determines if this node is a parent-child node.
     *
     * @param parentChild if <tt>true</tt>, indicates this is a parent-child
     *                    node
     */
    void setParentChild(boolean parentChild);

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

    /**
     * Sets the parent node deszcriptor.
     *
     * @param parent the parent node descriptor. May be <tt>null</tt>
     */
    void setParent(NodeDescriptorDO parent);

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
     * Adds a child node descriptor.
     *
     * @param child the child node descriptor to add
     * @throws DescriptorException if the node is a duplicate
     */
    void addNodeDescriptor(NodeDescriptorDO child);

    /**
     * Returns the assertion descriptor with the specified name.
     *
     * @param name the name of the assertion descriptor
     * @return the corresponding descriptor, or <tt>null</tt> if it doesn't
     *         exist
     */
    AssertionDescriptorDO getAssertionDescriptor(String name);

    /**
     * Returns the assertion descriptors.
     *
     * @return the assertion descriptors, keyed on name
     */
    Map<String, AssertionDescriptorDO> getAssertionDescriptors();

    /**
     * Adds an assertion descriptor.
     *
     * @param descriptor the assertion descriptor to add
     */
    void addAssertionDescriptor(AssertionDescriptorDO descriptor);

    /**
     * Removes an assertion descriptor.
     *
     * @param descriptor the assertion to remove
     */
    void removeAssertionDescriptor(AssertionDescriptorDO descriptor);

    /**
     * Removes an assertion descriptor with the specified name.
     *
     * @param name the assertion descriptor name
     */
    void removeAssertionDescriptor(String name);

}
