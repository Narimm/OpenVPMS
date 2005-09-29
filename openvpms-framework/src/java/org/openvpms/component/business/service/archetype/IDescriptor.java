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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.service.archetype;

import java.io.Serializable;

/**
 * This interface defines methods applicable to all descriptors and is the
 * parent of all descriptors. Descriptors provide information to the
 * presentation layers to allow automated rendering of presentation components.
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public interface IDescriptor extends Serializable, Cloneable {
    /**
     * Method returns the Descriptor name which is the archetype shortname in
     * the case of an Archetype Descriptor and the Node name for a Node
     * Descriptor.
     * Note: For NodeDescriptors this name must be able to be used to retrieve a property value 
     * via a ognl expression i.e model[descriptor.name]
     * 
     * @return Returns the name.
     */
    public String getName();

    /**
     * Method to get the display name. The Display Name is used by the
     * presentation layer in place of the actual object, archetype or node name.
     * Note: This can be formatted from the name above.
     * 
     * @return Returns the display name.
     */
    public String getDisplayName();

    /**
     * Method to set the Display Name.
     * @param displayName
     *            The display name to set.
     */
    public void setDisplayName(String displayName);

    /**
     * Method to get the type. The type is the rmentity class for Archetypes and
     * the class name for nodes.
     * 
     * @return Class the type.
     */
    public Class getType();

    /**
     * @return Cloned Object.
     */
    public Object clone();
}