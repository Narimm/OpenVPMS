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


public interface IPropertyDescriptor extends IDescriptor
{
    public static final int UNDEFINED_INDEX = -1;
    
    public static final int DEFAULT_LENGTH = 255;
        
    public void setIndex(int index);
    
    public int getIndex();
    
    /**
     * @return
     */
    public Class getPropertyType();

    /**
     * @return
     */
    public boolean isNumeric();

    public boolean isBoolean();

    /**
     * @return
     */
    public boolean isDate();

    /**
     * @return
     */
    public boolean isString();

    /**
     * @return
     */
    public boolean isObjectReference();

    /**
     * @return Returns the required.
     */
    public boolean isRequired();

    /**
     * @param required The required to set.
     */
    public void setRequired(boolean required);

    /**
     * @return
     */
    public boolean isReadOnly();

    /**
     * @param readOnly The readOnly to set.
     */
    public void setReadOnly(boolean readOnly);

    /**
     * @return
     */
    public String getName();
    
    public void setName(String name);

    /**
     * @return
     */
    public String getShortDescription();
    
    public void setShortDescription(String shortDescription);
    
    public int getLength();
    
    public void setLength(int length);   
}