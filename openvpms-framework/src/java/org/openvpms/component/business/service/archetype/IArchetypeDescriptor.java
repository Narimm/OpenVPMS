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

import java.util.List;

import org.openvpms.component.business.domain.archetype.ArchetypeId;

/**
* This descriptor interface provides methods to allow the application developer to
* get presentation specific information about a specific archetype.
* 
* @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
* @version  $LastChangedDate$
*/

public interface IArchetypeDescriptor extends IDescriptor
{
    /**
     * Method to return the {&link ArchetypeId } associated with this Descriptor. 
     * @return the {&link ArchetypeId }
     */
    public ArchetypeId getArchetypeId();

    /**
     * Method to return the Archetype shortname associated with this Descriptor. 
     * @return the {&link ArchetypeId }
     */
    public String getArchetypeName();

    /**
     * Method to return a List of INodeDescriptors.  These Descriptors represent presentation
     * information for each node in the archetype.  
     * Please Note it will also include an extra Node descriptor representing the object identifier.
     * 
     * @return Returns a List of { &link INodeDescriptors }.
     */
    public List getNodeDescriptors();

    /**
     * Method that allows the Node Descriptors to be set from a provided List of
     * Node Descriptors.  
     * @param propertyDescriptors
     *            The propertyDescriptors to set.
     */
    public void setNodeDescriptors(List nodeDescriptors);

    /**
     * Method to return the {&link INodeDescriptor} for the specified name.  The name provided is 
     * the archetype node name.  
     * @param string
     * @return Returns a {&link INodeDescriptor}
     */
    public INodeDescriptor getNodeDescriptor(String name);

    /**
     * Mthod to return a List of { &link INodeDescriptors } for the specified array of Node names.
     * 
     * @return List of { &link INodeDescriptor }
     */
    public List getPropertyDescriptors(String[] strings);
}