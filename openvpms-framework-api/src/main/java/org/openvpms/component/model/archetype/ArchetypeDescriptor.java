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
 * The archetype descriptor is used to describe an archetype.
 *
 * @author Tim Anderson
 */
public interface ArchetypeDescriptor extends IMObject {

    /**
     * Returns the archetype type.
     *
     * @return the archetype type
     */
    String getArchetypeType();

    /**
     * Returns the java class name.
     *
     * @return the class name
     */
    String getClassName();

    /**
     * Sets the java class name.
     *
     * @param className the class name
     */
    void setClassName(String className);

    /**
     * Determines if this is the latest version of the archetype.
     *
     * @return {@code true} if this is the latest version
     */
    boolean isLatest();

    /**
     * Determines if this is the latest version of the archetype.
     *
     * @param latest {@code true} if this is the latest version
     */
    void setLatest(boolean latest);

    /**
     * Determines if this is a primary archetype.
     *
     * @return {@code true} if this is a primary archetype
     */
    boolean isPrimary();

    /**
     * Determines if this is a primary archetype.
     *
     * @param primary {@code true} if this is a primary archetype
     */
    void setPrimary(boolean primary);

    /**
     * Add a node descriptor to this archetype descripor.
     *
     * @param node the node descriptor to add
     */
    void addNodeDescriptor(NodeDescriptor node);

    /**
     * Removes the specified node descriptor.
     *
     * @param node the node descriptor to remove
     */
    void removeNodeDescriptor(NodeDescriptor node);

    /**
     * Return the top-level node descriptors, keyed on name.
     *
     * @return the top-level node descriptors
     */
    Map<String, NodeDescriptor> getNodeDescriptors();

    /**
     * Returns the named node descriptor.
     *
     * @param name the node descriptor name
     * @return the corresponding node descriptor, or {@code null} if none is found
     */
    NodeDescriptor getNodeDescriptor(String name);

    /**
     * Returns the display name.
     *
     * @return the display name
     */
    String getDisplayName();

    /**
     * Sets the display name.
     *
     * @param displayName the display name
     */
    void setDisplayName(String displayName);
}
