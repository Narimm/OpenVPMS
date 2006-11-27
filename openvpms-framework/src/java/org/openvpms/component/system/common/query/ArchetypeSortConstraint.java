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
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ArchetypeSortConstraint extends SortConstraint {

    /**
     * Default SUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * Holds the archetype sort property type
     */
    private ArchetypeProperty property;


    /**
     * Construct an instance of this class with the specified parameters.
     *
     * @param property  the archetype sort property
     * @param ascending whether to sort in ascending or descending order
     */
    public ArchetypeSortConstraint(ArchetypeProperty property,
                                   boolean ascending) {
        this(null, property, ascending);
    }

    /**
     * Construct an instance of this class with the specified parameters.
     *
     * @param alias     the type alias. May be <code>null</code>
     * @param property  the archetype sort property
     * @param ascending whether to sort in ascending or descending order
     */
    public ArchetypeSortConstraint(String alias, ArchetypeProperty property,
                                   boolean ascending) {
        super(alias, ascending);
        this.property = property;
    }

    /**
     * Returns the archetype sort property.
     *
     * @return the sort property
     */
    public ArchetypeProperty getProperty() {
        return property;
    }

    /**
     * Sets the archetype sort property.
     *
     * @param property the sort property
     */
    public void setProperty(ArchetypeProperty property) {
        this.property = property;
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
