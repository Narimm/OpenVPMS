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

// java core
import java.util.ArrayList;
import java.util.List;

// commons-lang
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * This class manages {@link IConstraint} instances.
 * 
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public abstract class ConstraintContainer implements IConstraintContainer {
    /**
     * Default SUID
     */
    private static final long serialVersionUID = 1L; 
    
    /**
     * Maintain an list of {@link IConstraint}
     */
    private List<IConstraint> constraints = new ArrayList<IConstraint>();

    /**
     * @return Returns the constraints.
     */
    public List<IConstraint> getConstraints() {
        return constraints;
    }

    /**
     * @param constraints The constraints to set.
     */
    public void setConstraints(List<IConstraint> constraints) {
        this.constraints = constraints;
    }
    
    /* (non-Javadoc)
     * @see org.openvpms.component.system.common.query.IConstraintContainer#add(org.openvpms.component.system.common.query.IConstraint)
     */
    public IConstraintContainer add(IConstraint constraint) {
        constraints.add(constraint);
        return this;
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.system.common.query.IConstraintContainer#remove(org.openvpms.component.system.common.query.IConstraint)
     */
    public IConstraintContainer remove(IConstraint constraint) {
        constraints.add(constraint);
        return this;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof BaseArchetypeConstraint)) {
            return false;
        }
        
        ConstraintContainer rhs = (ConstraintContainer) obj;
        return new EqualsBuilder()
            .append(constraints.toArray(), rhs.constraints.toArray())
            .isEquals();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .appendSuper(super.toString())
            .append("constraints", constraints.toArray())
            .toString();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        ConstraintContainer copy = (ConstraintContainer)super.clone();
        copy.constraints = new ArrayList<IConstraint>(this.constraints);
        
        return copy;
    }
}
