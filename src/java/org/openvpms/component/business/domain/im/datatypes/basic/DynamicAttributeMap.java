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


package org.openvpms.component.business.domain.im.datatypes.basic;

// java core
import java.io.Serializable;
import java.util.HashMap;


/**
 * This class is used to support dynamic attributes of a class, which is 
 * fundamental for dynamic object models. All dymamic attributes must support
 * the {@link Serializable} interface.
 * <p>
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class DynamicAttributeMap implements Serializable, Cloneable {
    /**
     * Generated SUID
     */
    private static final long serialVersionUID = 1L; 
    
    /**
     * Caches all the dynamic attributes
     */
    HashMap<String, Serializable> attributes =
        new HashMap<String, Serializable>();
    
    /**
     * Default constructor
     */
    public DynamicAttributeMap() {
    }

    /**
     * @return Returns the attributes.
     */
    public HashMap<String, Serializable> getAttributes() {
        return attributes;
    }

    /**
     * @param attributes The attributes to set.
     */
    public void setAttributes(HashMap<String, Serializable> attributes) {
        this.attributes = attributes;
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.common.datatypes.basic.IDynamicAttributes#setAttribute(java.lang.String, java.io.Serializable)
     */
    public void setAttribute(String name, Serializable value) {
        attributes.put(name, value);
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.common.datatypes.basic.IDynamicAttributes#removeAttribute(java.lang.String)
     */
    public Serializable removeAttribute(String name) {
        return attributes.remove(name);
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.common.datatypes.basic.IDynamicAttributes#getAttributeValue(java.lang.String)
     */
    public Serializable getAttribute(String name) {
        return attributes.get(name);
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.common.datatypes.basic.IDynamicAttributes#getAttributeType(java.lang.String)
     */
    public String getAttributeType(String name) {
        return attributes.get(name).getClass().getName();
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.common.datatypes.basic.IDynamicAttributes#getAttributeNames()
     */
    public String[] getAttributeNames() {
        return (String[])attributes.keySet().toArray(
                new String[attributes.size()]);
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.common.datatypes.basic.IDynamicAttributes#getAttributeValues()
     */
    public Serializable[] getAttributeValues() {
        return (Serializable[])attributes.values().toArray(
                new Serializable[attributes.size()]);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        DynamicAttributeMap copy = (DynamicAttributeMap)super.clone();
        
        copy.attributes = new HashMap<String, Serializable>(this.attributes);
        return copy;
    }
}
