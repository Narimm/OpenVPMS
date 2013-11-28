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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */


package org.openvpms.component.system.common.query;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * This is the base archetype constraint that is the root of all archetype queries.
 *
 * @author Jim Alateras
 * @author Tim Anderson
 */
public abstract class BaseArchetypeConstraint extends ConstraintContainer {

    /**
     * The instance state to query.
     */
    public enum State {
        ACTIVE, INACTIVE, BOTH
    }

    /**
     * The type name alias. May be {@code null}.
     */
    private String alias;


    /**
     * Determines if active and/or inactive instances should be returned.
     */
    private State state;

    /**
     * If {@code true}, only query primary instances.
     */
    private boolean primaryOnly;


    /**
     * Constructs a {@link BaseArchetypeConstraint}.
     *
     * @param alias       the type alias. May be {@code null}
     * @param primaryOnly if {@code true} only deal with primary archetypes
     * @param activeOnly  if {@code true} only deal with active entities
     */
    BaseArchetypeConstraint(String alias, boolean primaryOnly, boolean activeOnly) {
        this(alias, primaryOnly, (activeOnly) ? State.ACTIVE : State.BOTH);
    }

    /**
     * Constructs a {@link BaseArchetypeConstraint}.
     *
     * @param alias       the type alias. May be {@code null}
     * @param primaryOnly if {@code true} only deal with primary archetypes
     * @param state       determines if active and/or inactive instances are returned
     */
    BaseArchetypeConstraint(String alias, boolean primaryOnly, State state) {
        this.alias = alias;
        this.primaryOnly = primaryOnly;
        this.state = state;
    }

    /**
     * Sets the type name alias.
     *
     * @param alias the type name alias. May be {@code null}
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * Returns the type name alias.
     *
     * @return the type name alias. May be {@code null}
     */
    public String getAlias() {
        return alias;
    }

    /**
     * Determines if only primary instances will be queried.
     *
     * @return {@code true} if only primary instances will be queried
     */
    public boolean isPrimaryOnly() {
        return primaryOnly;
    }

    /**
     * Determines if only primary instances will be queried.
     *
     * @param primaryOnly if {@code true}, only primary instances will be queried
     */
    public void setPrimaryOnly(boolean primaryOnly) {
        this.primaryOnly = primaryOnly;
    }

    /**
     * Determines if active and/or inactive instances will be queried.
     *
     * @param state the instances to query
     */
    public void setState(State state) {
        this.state = state;
    }

    /**
     * Determines if active and/or inactive instances will be queried.
     *
     * @return the instances to query
     */
    public State getState() {
        return state;
    }

    /**
     * Determines if only active instances will be queried.
     *
     * @return {@code true} if only active instances will be queried.
     */
    public boolean isActiveOnly() {
        return state == State.ACTIVE;
    }

    /**
     * Determines if only active instances will be queried.
     *
     * @param activeOnly if {@code true}, only active instances will be queried
     */
    public void setActiveOnly(boolean activeOnly) {
        state = (activeOnly) ? State.ACTIVE : State.BOTH;
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
                .append(state, rhs.state)
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
                .append("state", state)
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
