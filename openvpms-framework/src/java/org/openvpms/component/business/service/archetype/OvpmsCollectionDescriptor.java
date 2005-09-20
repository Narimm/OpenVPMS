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


/**
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class OvpmsCollectionDescriptor extends OvpmsPropertyDescriptor
{
    private Class elementType;
    
    private boolean childRelationship;

    public OvpmsCollectionDescriptor(IPropertyDescriptor descriptor)
    {
        super(descriptor);
    }
    
    /**
     * @param realDescriptor
     */
    public OvpmsCollectionDescriptor(Class type)
    {
        super(type);

        // TODO Auto-generated constructor stub
    }

    public OvpmsCollectionDescriptor(String name, Class type)
    {
        this(type);
        this.setName(name);
    }
    
    /* (non-Javadoc)
     * @see org.trails.descriptor.PropertyDescriptor#isCollection()
     */
    public boolean isCollection()
    {
        return true;
    }

    /**
     * @return Returns the elementType.
     */
    public Class getElementType()
    {
        return elementType;
    }

    /**
     * @param elementType The elementType to set.
     */
    public void setElementType(Class elementType)
    {
        this.elementType = elementType;
    }
    /**
     * @return Returns the childRelationship.
     */
    public boolean isChildRelationship()
    {
        return childRelationship;
    }
    /**
     * @param childRelationship The childRelationship to set.
     */
    public void setChildRelationship(boolean childRelationship)
    {
        this.childRelationship = childRelationship;
    }

    public Object clone()
    {
        return new OvpmsCollectionDescriptor(this);
    }
    
    
}
