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


package org.openvpms.component.system.common.query;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * This class is used to specify a sort constraint on a portion of the 
 * archetype such as the entity name or the concept name.
 * 
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class ArchetypeSortConstraint extends SortConstraint {

    /**
     * Default SUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * An enumeration of the different archetype properties
     */
    public enum ArchetypeSortProperty {
        ReferenceModelName,
        EntityName,
        ConceptName
    }
    
    /**
     * Holds the archetype sort property type
     */
    private ArchetypeSortProperty property;
    
    
    /**
     * Construct an instance of this class with the specified parameters.
     * 
     * @param property
     *            the archetype sort property
     * @param ascending
     *            whether to sort in ascending order            
     */
    public ArchetypeSortConstraint(ArchetypeSortProperty property, boolean ascending) {
        super(ascending);
        this.property = property;
    }

    /**
     * @return Returns the property.
     */
    public ArchetypeSortProperty getProperty() {
        return property;
    }

    /**
     * @param property The property to set.
     */
    public void setProperty(ArchetypeSortProperty property) {
        this.property = property;
    }
    
    /* (non-Javadoc)
     * @see org.openvpms.component.system.common.query.SortConstraint#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        ArchetypeSortConstraint copy = (ArchetypeSortConstraint)super.clone();
        copy.property = this.property;
        
        return copy;
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.system.common.query.SortConstraint#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof ArchetypeSortConstraint)) {
            return false;
        }
        
        ArchetypeSortConstraint rhs = (ArchetypeSortConstraint) obj;
        return new EqualsBuilder()
            .appendSuper(super.equals(obj))
            .append(property, rhs.property)
            .isEquals();
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.system.common.query.SortConstraint#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .appendSuper(super.toString())
            .append("property", property)
            .toString();
    }
}
