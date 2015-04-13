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


package org.openvpms.component.business.domain.im.datatypes.property;

// commons-lang
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

// openvpms-framework
import org.openvpms.component.business.domain.im.common.IMObject;


/**
 * Represents a property that is named.
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public abstract class NamedProperty extends IMObject {

    /**
     * Default SUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * Default constructor 
     */
    protected NamedProperty() {
    }

    /**
     * Return the value of this property
     * 
     * @return Object
     */
    public abstract Object getValue();
    
    /**
     * Set the value of the property. This was needed to resolve a problem
     * in JXPath
     * 
     * @pram value
     */
    public abstract void setValue(Object value);
    
    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.common.IMObject#isNew()
     */
    @Override
    public boolean isNew() {
        return StringUtils.isEmpty(getName());
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, 
                ToStringStyle.MULTI_LINE_STYLE);
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.common.IMObject#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        NamedProperty copy = (NamedProperty)super.clone();
        
        return copy;
    }
}
