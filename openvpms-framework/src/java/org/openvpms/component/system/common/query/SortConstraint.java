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


/**
 * The base class for all sort constraints. A sort constraint defines the order of the returned records.
 *
 * @author Jim Alateras
 * @author Tim Anderson
 */
public abstract class SortConstraint implements IConstraint {

    /**
     * The type alias. May be {@code null}.
     */
    private String alias;

    /**
     * Determines whether to sort in ascending or descending order.
     */
    private boolean ascending;


    /**
     * Construct an instance of this constraint indicating whether to sort
     * in ascending or descending order.
     *
     * @param alias     the type alias. May be {@code null}
     * @param ascending true if sorting in ascending order
     */
    protected SortConstraint(String alias, boolean ascending) {
        this.alias = alias;
        this.ascending = ascending;
    }

    /**
     * Returns the type alias.
     *
     * @return the type alias. May be {@code null}
     */
    public String getAlias() {
        return alias;
    }

    /**
     * Determines if the sort is ascending.
     *
     * @return {@code true} if the sort is ascending; {@code false} if it is descending.
     */
    public boolean isAscending() {
        return ascending;
    }

    /**
     * Determines if the sort is ascending.
     *
     * @param ascending if {@code true} the sort is ascending; if {@code false} it is descending.
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
                .append(alias, rhs.alias)
                .append(ascending, rhs.ascending)
                .isEquals();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("alias", alias)
                .append("ascending", ascending)
                .toString();
    }

}
