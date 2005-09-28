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

import org.apache.commons.beanutils.BeanUtils;
import org.openvpms.component.presentation.tapestry.component.Utils;

/**
 * 
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class OpenVpmsDescriptor implements IDescriptor
{

    private String displayName;
    private String shortDescription;
    protected Class type;
    private boolean hidden;

    public OpenVpmsDescriptor(IDescriptor descriptor)
    {
        copyFrom(descriptor);
    }
    
    public OpenVpmsDescriptor(Class type)
    {
        this.type = type;
    }
    
    
    @Override
    public Object clone()
    {
        return new OpenVpmsDescriptor(this);
    }

     public String getDisplayName()
    {
        return Utils.unCamelCase(displayName);
    }

    public void setDisplayName(String displayName)
    {
        this.displayName = displayName;
    }

    public String getShortDescription()
    {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription)
    {
        this.shortDescription = shortDescription;
    }

    protected void copyFrom(IDescriptor descriptor)
    {
        try
        {
            BeanUtils.copyProperties(this, descriptor);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public boolean isHidden()
    {
        return hidden;
    }

    public void setHidden(boolean hidden)
    {
        this.hidden = hidden;
    }

    public Class getType()
    {
        return type;
    }

    public void setType(Class type)
    {
        this.type = type;
    }

}
