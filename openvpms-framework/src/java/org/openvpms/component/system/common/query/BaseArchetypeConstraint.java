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
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class BaseArchetypeConstraint extends ConstraintContainer {

    /**
     * The type name alias. May be <code>null</code>.
     */
    private String alias;

    /**
     * If <code>true</code> only query active instances.
     */
    private boolean activeOnly;

    /**
     * If <code>true</code>, only query primary instances.
     */
    private boolean primaryOnly;


    /**
     * Constructs a new <code>BaseArchetypeConstraint</code>.
     *
     * @param alias       the type alias. May be <code>null</code>
     * @param primaryOnly if <code>true</code> only deal with primary archetypes
     * @param activeOnly  if <code>true</code> only deal with active entities
     */
    BaseArchetypeConstraint(String alias, boolean primaryOnly,
                            boolean activeOnly) {
        this.alias = alias;
        this.primaryOnly = primaryOnly;
        this.activeOnly = activeOnly;
    }

    /**
     * Sets the type name alias.
     *
     * @param alias the type name alias. May be <code>null</code>
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * Returns the type name alias.
     *
     * @return the type name alias. May be <code>null</code>
     */
    public String getAlias() {
        return alias;
    }

    /**
     * Determines if only primary instances will be queried.
     *
     * @return <code>true</code> if only primary instances will be queried
     */
    public boolean isPrimaryOnly() {
        return primaryOnly;
    }

    /**
     * Determines if only primary instances will be queried.
     *
     * @param primaryOnly if <code>true</code>, only primary instances will be
     *                    queried
     */
    public void setPrimaryOnly(boolean primaryOnly) {
        this.primaryOnly = primaryOnly;
    }

    /**
     * Determines if only active instances will be queried.
     *
     * @return </code>true</code> if only active instances will be queried.
     */
    public boolean isActiveOnly() {
        return activeOnly;
    }

    /**
     * Determines if only active instances will be queried.
     *
     * @param activeOnly if <code>true</code>, only active instances will be
     *                   queried
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

        if (!(obj instanceof BaseArchetypeConstraint)) {
            return false;
        }

        BaseArchetypeConstraint rhs = (BaseArchetypeConstraint) obj;
        return new EqualsBuilder()
                .appendSuper(super.equals(rhs))
                .append(alias, rhs.alias)
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
                .append("alias", alias)
                .append("activeOnly", activeOnly)
                .append("primaryOnly", primaryOnly)
                .toString();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
