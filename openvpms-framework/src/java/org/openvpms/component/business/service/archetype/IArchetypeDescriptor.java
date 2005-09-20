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

import org.openvpms.component.business.domain.archetype.Archetype;

/**
*
* @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
* @version  $LastChangedDate$
*/

public interface IArchetypeDescriptor extends IDescriptor
{
    public Archetype getArchetype();

    /**
     * @return Returns the propertyDescriptors.
     */
    public List getPropertyDescriptors();

    /**
     * @param propertyDescriptors
     *            The propertyDescriptors to set.
     */
    public void setPropertyDescriptors(List propertyDescriptors);

    public IPropertyDescriptor getIdentifierDescriptor();

    /**
     * @return
     */
    public String getDisplayName();
    
    public void setDisplayName(String displayName);

    /**
     * @return
     */
    public String getShortDescription();
    
    public void setShortDescription(String shortDescription);

    /**
     * @param string
     * @return
     */
    public IPropertyDescriptor getPropertyDescriptor(String name);

    /**
     * @return
     */
    public String getPluralDisplayName();

    public List getPropertyDescriptors(String[] strings);
}