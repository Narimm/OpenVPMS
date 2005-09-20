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
public class OvpmsReferenceDescriptor extends OvpmsPropertyDescriptor
{
    private Class actualType;

    public OvpmsReferenceDescriptor(IPropertyDescriptor descriptor,
            Class actualType)
    {
        this(descriptor.getPropertyType(), actualType);
        copyFrom(descriptor);
    }
    
    /**
     * @param realDescriptor
     */
    public OvpmsReferenceDescriptor(
        Class declaredType, Class actualType)
    {
        super(declaredType);
        this.actualType = actualType;
    }

    /* (non-Javadoc)
     * @see org.trails.descriptor.PropertyDescriptor#getPropertyType()
     */
    public Class getPropertyType()
    {
        return actualType;
    }

    /* (non-Javadoc)
     * @see org.trails.descriptor.PropertyDescriptor#isObjectReference()
     */
    public boolean isObjectReference()
    {
        return true;
    }

    public Object clone()
    {
        return new OvpmsReferenceDescriptor(this, getPropertyType());
    }
}
