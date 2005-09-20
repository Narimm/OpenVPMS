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

import org.apache.commons.lang.builder.EqualsBuilder;

/**
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class OvpmsPropertyDescriptor extends OvpmsDescriptor implements IPropertyDescriptor
{
    private boolean required;
    
    private boolean readOnly;
    
    private String name;
    
    private int index = UNDEFINED_INDEX;
    
    private int length = DEFAULT_LENGTH;
    
    private boolean large;
    
    public int getIndex()
    {
        return index;
    }

    public void setIndex(int index)
    {
        this.index = index;
    }

    /** 
     * It's kinda like an old-skool C++ copy constructor
     *
     */
    public OvpmsPropertyDescriptor(IPropertyDescriptor descriptor)
    {
        this(descriptor.getPropertyType());
        copyFrom(descriptor);
    }
    
    public OvpmsPropertyDescriptor(Class type)
    {
        super(type);
    }

    public OvpmsPropertyDescriptor(String name, Class type)
    {
        this(type);
        this.name = name;
        setDisplayName(name);
    }
    
    /**
     * @return
     */
    public Class getPropertyType()
    {
        return getType();
    }

    /**
     * @return
     */
    public boolean isNumeric()
    {
        return getPropertyType().getName().endsWith("Double") ||
        getPropertyType().getName().endsWith("Integer") ||
        getPropertyType().getName().endsWith("Float") ||
        getPropertyType().getName().endsWith("double") ||
        getPropertyType().getName().endsWith("int") ||
        getPropertyType().getName().endsWith("float") ||
        getPropertyType().getName().endsWith("BigDecimal");
    }

    public boolean isBoolean()
    {
        return getPropertyType().getName().endsWith("boolean") ||
        	getPropertyType().getName().endsWith("Boolean");
    }
    /**
     * @return
     */
    public boolean isDate()
    {
        // TODO Auto-generated method stub
        return getPropertyType().getName().endsWith("Date");
    }

    /**
     * @return
     */
    public boolean isString()
    {
        // TODO Auto-generated method stub
        return getPropertyType().getName().endsWith("String");
    }

    /**
     * @return
     */
    public boolean isObjectReference()
    {
        return false;
    }
    
    /**
     * @return Returns the required.
     */
    public boolean isRequired()
    {
        return required;
    }
    
    /**
     * @param required The required to set.
     */
    public void setRequired(boolean required)
    {
        this.required = required;
    }

    /**
     * @return
     */
    public boolean isReadOnly()
    {
        return readOnly;
    }
    
    /**
     * @param readOnly The readOnly to set.
     */
    public void setReadOnly(boolean readOnly)
    {
        this.readOnly = readOnly;
    }
    
    /**
     * @return
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return Returns the identifier.
     */
    public boolean isIdentifier()
    {
        return false;
    }

    /**
     * @return Returns the collection.
     */
    public boolean isCollection()
    {
        return false;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public Object clone()
    {
        return new OvpmsPropertyDescriptor(this);
    }

    public boolean equals(Object obj)
    {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    public int getLength()
    {
        return length;
    }

    public void setLength(int length)
    {
        this.length = length;
    }

    public boolean isLarge()
    {
        return large;
    }

    public void setLarge(boolean large)
    {
        this.large = large;
    }
}
