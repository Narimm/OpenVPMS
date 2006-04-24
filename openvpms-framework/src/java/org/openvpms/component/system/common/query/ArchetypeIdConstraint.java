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

// commons-lang
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

// openvpms-framework
import org.openvpms.component.business.domain.archetype.ArchetypeId;

/**
 * A constraint based on a single {@link ArchetypeId}
 * 
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class ArchetypeIdConstraint extends BaseArchetypeConstraint {
    /**
     * Default SUID
     */
    private static final long serialVersionUID = 1L; 
    
    /**
     * The archetypeId
     */
    private ArchetypeId archetypeId;
    
    /**
     * Create a constraint for the specified archetype id
     * 
     * @param archetypeId 
     *            a valid archetype identity
     * @param activeOnly
     *            constraint to active only            
     */
    public ArchetypeIdConstraint(ArchetypeId archetypeId, boolean activeOnly) {
        super(false, activeOnly);
        this.archetypeId = archetypeId;
    }

    /**
     * @return Returns the archetypeId.
     */
    public ArchetypeId getArchetypeId() {
        return archetypeId;
    }

    /**
     * @param archetypeId The archetypeId to set.
     */
    public void setArchetypeId(ArchetypeId archetypeId) {
        this.archetypeId = archetypeId;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof ArchetypeIdConstraint)) {
            return false;
        }
        
        ArchetypeIdConstraint rhs = (ArchetypeIdConstraint) obj;
        return new EqualsBuilder()
            .appendSuper(super.equals(rhs))
            .append(archetypeId, rhs.archetypeId)
            .isEquals();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .appendSuper(super.toString())
            .append("archetypeId", archetypeId)
            .toString();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        ArchetypeIdConstraint copy = (ArchetypeIdConstraint)super.clone();
        copy.archetypeId = new ArchetypeId(this.archetypeId.toString());
        
        return copy;
    }
}
