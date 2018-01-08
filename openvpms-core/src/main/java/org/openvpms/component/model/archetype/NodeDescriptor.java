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

package org.openvpms.component.model.archetype;

import org.openvpms.component.model.object.IMObject;

import java.util.Map;

/**
 * Describes a node in an archetype.
 *
 * @author Tim Anderson
 */
public interface NodeDescriptor extends IMObject {

    /**
     * The default maximum length if one is not defined in the node definition.
     */
    int DEFAULT_MAX_LENGTH = 255;

    /**
     * Used to identify a maximum cardinality that is unbounded.
     */
    int UNBOUNDED = -1;

    /**
     * Returns the default value.
     *
     * @return the default value. May be {@code null}
     */
    String getDefaultValue();

    /**
     * Sets the default value.
     *
     * @param defaultValue the default value. May be {@code null}
     */
    void setDefaultValue(String defaultValue);

    /**
     * Determines if this is a derived node.
     *
     * @return {@code true} if this is a derived node
     */
    boolean isDerived();

    /**
     * Determines if this is a derived node.
     *
     * @param derived if {@code true} indicates the node is derived
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
     * @param derivedValue the derived value. May be {@code null}
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
     * @param displayName the display name. May be {@code null}
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
     * @param filter the filter. May be {@code null}
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
     * @param cardinality the maximum cardinality. Use {@link #UNBOUNDED} to indicate an unbounded cardinality
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
     * Returns the node path.
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
     * @return {@code true} if the node is hidden
     */
    boolean isHidden();

    /**
     * Determines if the node is hidden.
     *
     * @param hidden if {@code true}, indicates the node is hidden
     */
    void setHidden(boolean hidden);

    /**
     * Determines if the node is read-only.
     *
     * @param readOnly if {@code true}, indicates the node is read-only
     */
    void setReadOnly(boolean readOnly);

    /**
     * Determines if the node is read-only.
     *
     * @return {@code true} if the node is read-only
     */
    boolean isReadOnly();

    /**
     * Returns the base name.
     *
     * @return the base name. May be {@code null}
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
     * @return {@code true} if this a parent-child node
     */
    boolean isParentChild();

    /**
     * Determines if this node is a parent-child node.
     *
     * @param parentChild if {@code true}, indicates this is a parent-child
     *                    node
     */
    void setParentChild(boolean parentChild);

    /**
     * Returns the assertion descriptor with the specified name.
     *
     * @param name the name of the assertion descriptor
     * @return the corresponding descriptor, or {@code null} if it doesn't
     * exist
     */
    AssertionDescriptor getAssertionDescriptor(String name);

    /**
     * Returns the assertion descriptors.
     *
     * @return the assertion descriptors
     */
    Map<String, AssertionDescriptor> getAssertionDescriptors();

    /**
     * Adds an assertion descriptor.
     *
     * @param descriptor the assertion descriptor to add
     */
    void addAssertionDescriptor(AssertionDescriptor descriptor);

    /**
     * Removes an assertion descriptor.
     *
     * @param descriptor the assertion to remove
     */
    void removeAssertionDescriptor(AssertionDescriptor descriptor);

    /**
     * Removes an assertion descriptor with the specified name.
     *
     * @param name the assertion descriptor name
     */
    void removeAssertionDescriptor(String name);

    /**
     * Check whether this assertion type is defined for this node
     *
     * @param type the assertion type
     * @return boolean
     */
    boolean containsAssertionType(String type);

    /**
     * Return an array of short names or short name regular expression that are
     * associated with the archetypeRange assertion. If the node does not have
     * such an assertion then return a zero length string array
     * <p>
     * TODO Should we more this into a utility class TODO Change return type to
     * List
     *
     * @return String[] the array of short names
     */
    String[] getArchetypeRange();

    /**
     * Returns the class for the specified type.
     *
     * @return the class, or {@code null} if {@link #getType()} returns empty/null
     */
    Class getClazz();

    /**
     * Return the minimum value of the node. If no minimum defined for node then
     * return 0. Only valid for numeric nodes.
     *
     * @return Number the minimum value
     */
    Number getMinValue();

    /**
     * Return the maximum value of the node. If no maximum defined for node then
     * return 0. Only valid for numeric nodes.
     *
     * @return Number the minimum value
     */
    Number getMaxValue();


    /**
     * Return the regular expression associated with the node. Only valid for
     * string nodes.
     *
     * @return String regular expression pattern
     */
    String getStringPattern();

    /**
     * Check whether this node is a boolean type.
     *
     * @return boolean
     */
    boolean isBoolean();

    /**
     * Check whether this node is a collection
     *
     * @return boolean
     */
    boolean isCollection();

    /**
     * Indicates if this node is acomplex node. If the node has an
     * archetypeRange assertion or the node has a cardinality > 1 then the node
     * is deemed to be a complex node
     *
     * @return boolean true if complex
     */
    boolean isComplexNode();

    /**
     * Check whether this node a date type.
     *
     * @return boolean
     */
    boolean isDate();

    /**
     * Check whether this node is a lookup
     *
     * @return boolean
     */
    boolean isLookup();

    /**
     * Check whether this ia a money type
     *
     * @return boolean
     */
    boolean isMoney();

    /**
     * Check whether this node is a numeric type.
     *
     * @return boolean
     */
    boolean isNumeric();

    /**
     * Check whether this node is an object reference. An object reference is a
     * node that references another oject subclassed from IMObject.
     *
     * @return boolean
     */
    boolean isObjectReference();

    /**
     * Check whether this node is mandatory.
     *
     * @return boolean
     */
    boolean isRequired();

    /**
     * Check whether this node is a string
     *
     * @return boolean
     */
    boolean isString();

}
