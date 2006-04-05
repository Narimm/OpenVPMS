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
 * This is the base archetype constraint that is the root of all archetype
 * queries. Currently it is a marker class.
 * 
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public abstract class ArchetypeConstraint extends ConstraintContainer {

    /**
     * Constraint to active only entities
     */
    private boolean activeOnly;
    
    /**
     * Whether to search for primary instances only
     */
    private boolean primaryOnly;
    
    /**
     * Base class constructor
     * 
     * @param primaryOnly
     *            only process primary archetypes
     * @param activeOnly
     *            only process active archetypes            
     */
    ArchetypeConstraint(boolean primaryOnly, boolean activeOnly) {
        this.primaryOnly = primaryOnly;
        this.activeOnly = activeOnly;
    }
    
    /**
     * @return Returns the primaryOnly.
     */
    public boolean isPrimaryOnly() {
        return primaryOnly;
    }
    
    /**
     * @param primaryOnly The primaryOnly to set.
     */
    public void setPrimaryOnly(boolean primaryOnly) {
        this.primaryOnly = primaryOnly;
    }
    
    /**
     * @return Returns the activeOnly.
     */
    public boolean isActiveOnly() {
        return activeOnly;
    }
    
    /**
     * @param activeOnly The activeOnly to set.
     */
    public void setActiveOnly(boolean activeOnly) {
        this.activeOnly = activeOnly;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof ArchetypeConstraint)) {
            return false;
        }
        
        ArchetypeConstraint rhs = (ArchetypeConstraint) obj;
        return new EqualsBuilder()
            .appendSuper(super.equals(rhs))
            .append(activeOnly, rhs.activeOnly)
            .append(primaryOnly, rhs.primaryOnly)
            .isEquals();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .appendSuper(super.toString())
            .append("activeOnly", activeOnly)
            .append("primaryOnly", primaryOnly)
            .toString();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        ArchetypeConstraint copy = (ArchetypeConstraint)super.clone();
        copy.activeOnly = this.activeOnly;
        copy.primaryOnly = this.primaryOnly;
        
        return copy;
    }
}
