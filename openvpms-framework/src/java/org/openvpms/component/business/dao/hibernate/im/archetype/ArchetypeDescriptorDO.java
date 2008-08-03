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
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;

import java.util.Map;


/**
 * Data object interface corresponding to the {@link ArchetypeDescriptor} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface ArchetypeDescriptorDO extends DescriptorDO {

    /**
     * Returns the archetype type.
     *
     * @return the archetype id
     */
    ArchetypeId getType();

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
     * @return <tt>true</tt> if this is the latest version
     */
    boolean isLatest();

    /**
     * Determines if this is the latest version of the archetype.
     *
     * @param latest <tt>true</tt> if this is the latest version
     */
    void setLatest(boolean latest);

    /**
     * Determines if this is a primary archetype.
     *
     * @return <tt>true</tt> if this is a primary archetype
     */
    boolean isPrimary();

    /**
     * Determines if this is a primary archetype.
     *
     * @param primary <tt>true</tt> if this is a primary archetype
     */
    void setPrimary(boolean primary);

    /**
     * Add a node descriptor to this archetype descripor.
     *
     * @param node the node descriptor to add
     * @throws DescriptorException if a node descriptor exists with the same
     *                             name
     */
    void addNodeDescriptor(NodeDescriptorDO node);

    /**
     * Removes the specified node descriptor.
     *
     * @param node the node descriptor to remove
     */
    void removeNodeDescriptor(NodeDescriptorDO node);

    /**
     * Return the top-level node descriptors, keyed on name.
     *
     * @return the top-level node descriptors
     */
    Map<String, NodeDescriptorDO> getNodeDescriptors();

    /**
     * Return all the node descriptors for this archetype, keyed on name.
     * <p/>
     * This flattens out the node descriptor heirarchy.
     *
     * @return the node descriptors
     */
    Map<String, NodeDescriptorDO> getAllNodeDescriptors();

    /**
     * Returns the named node descriptor.
     *
     * @param name the node descriptor name
     * @return the corresponding node descriptor, or <tt>null</tt> if none
     *         is found
     */
    NodeDescriptorDO getNodeDescriptor(String name);

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
