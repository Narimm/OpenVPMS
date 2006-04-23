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
 * The base class for all sort constraints. A sort constraint defines the
 * order of the returned records.
 * 
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public abstract class SortConstraint implements IConstraint {

    /**
     * Defines whether to sort in asending order.
     */
    private boolean ascending;
    
    /**
     * Construct an instance of this class specifying whether it should be 
     * sorted in ascending order.
     * 
     * @param ascending
     *            should be in ascending order
     */
    protected SortConstraint() {
    }
    
    /**
     * Construct an instance of this constraint indicating whether to sort
     * in ascending order.
     * 
     * @param ascending
     *            true if sorting in ascending order
     */
    protected SortConstraint(boolean ascending) {
        this.ascending = ascending;
    }

    /**
     * @return Returns the ascending.
     */
    public boolean isAscending() {
        return ascending;
    }

    /**
     * @param ascending The ascending to set.
     */
    public void setAscending(boolean ascending) {
        this.ascending = ascending;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof SortConstraint)) {
            return false;
        }
        
        SortConstraint rhs = (SortConstraint) obj;
        return new EqualsBuilder()
            .append(ascending, rhs.ascending)
            .isEquals();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("ascending", ascending)
            .toString();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        SortConstraint copy = (SortConstraint)super.clone();
        copy.ascending = this.ascending;
        
        return copy;
    }
}
