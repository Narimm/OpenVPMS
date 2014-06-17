/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */


package org.openvpms.component.system.common.query;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.openvpms.component.business.domain.archetype.ArchetypeId;


/**
 * A constraint based on a single {@link ArchetypeId};
 *
 * @author Jim Alateras
 * @author Tim Anderson
 */
public class ArchetypeIdConstraint extends BaseArchetypeConstraint {

    /**
     * Default SUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The archetypeId.
     */
    private ArchetypeId archetypeId;


    /**
     * Create a constraint for the specified archetype id
     *
     * @param archetypeId a valid archetype identity
     * @param activeOnly  constraint to active only
     */
    public ArchetypeIdConstraint(ArchetypeId archetypeId, boolean activeOnly) {
        this(null, archetypeId, activeOnly);
    }

    /**
     * Create a constraint for the specified archetype id.
     *
     * @param alias       the type alias. May be <code>null</code>
     * @param archetypeId a valid archetype identity
     * @param activeOnly  constraint to active only
     */
    public ArchetypeIdConstraint(String alias, ArchetypeId archetypeId,
                                 boolean activeOnly) {
        super(alias, false, activeOnly);
        this.archetypeId = archetypeId;
    }

    /**
     * Returns the archetype id.
     *
     * @return the archetype id.
     */
    public ArchetypeId getArchetypeId() {
        return archetypeId;
    }

    /**
     * Sets the archetype id.
     *
     * @param archetypeId the archetype id
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
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append("archetypeId", archetypeId)
                .toString();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        ArchetypeIdConstraint copy = (ArchetypeIdConstraint) super.clone();
        copy.archetypeId = new ArchetypeId(this.archetypeId.getQualifiedName());

        return copy;
    }
}
