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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.system.common.query;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * A join constraint.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class JoinConstraint implements IConstraintContainer {

    /**
     * How to join with the outer table.
     */
    public enum JoinType {
        InnerJoin,
        LeftOuterJoin,
        RightOuterJoin
    }

    /**
     * Serialisation version identifier.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The join type.
     */
    private JoinType joinType;

    /**
     * The archetype constraint associated with this join constraint.
     */
    private BaseArchetypeConstraint archetypeConstraint;


    /**
     * Constructs a new <code>JoinConstraint</code>, using an inner join.
     *
     * @param constraint the archetype constraint
     */
    public JoinConstraint(BaseArchetypeConstraint constraint) {
        this(constraint, JoinType.InnerJoin);
    }

    /**
     * Constructs a new <code>JoinConstraint</code>.
     *
     * @param constraint the archetype constraint
     * @param join       the join type
     */
    public JoinConstraint(BaseArchetypeConstraint constraint, JoinType join) {
        archetypeConstraint = constraint;
        joinType = join;
    }

    /**
     * Returns the archetype constraint.
     *
     * @return the archetype constraint
     */
    public BaseArchetypeConstraint getArchetypeConstraint() {
        return archetypeConstraint;
    }

    /**
     * Add the specified constraint to the container.
     *
     * @param constraint the constraint to add
     * @return this constraint
     */
    public JoinConstraint add(IConstraint constraint) {
        this.archetypeConstraint.add(constraint);
        return this;
    }

    /**
     * Remove the specified constraint from the container.
     *
     * @param constraint the constraint to remove
     * @return this constraint
     */
    public JoinConstraint remove(IConstraint constraint) {
        this.archetypeConstraint.remove(constraint);
        return this;
    }

    /**
     * Returns the join type.
     *
     * @return the join type
     */
    public JoinType getJoinType() {
        return joinType;
    }

    /**
     * Sets the join type.
     *
     * @param join the join type
     */
    public JoinConstraint setJoinType(JoinType join) {
        joinType = join;
        return this;
    }

    /**
     * Returns the type name alias.
     *
     * @return the type name alias. May be <tt>null</tt>
     */
    public String getAlias() {
        return getArchetypeConstraint().getAlias();
    }

    /**
     * Sets the type name alias.
     *
     * @param alias the alias. May be <tt>null</tt>
     */
    public void setAlias(String alias) {
        getArchetypeConstraint().setAlias(alias);
    }

    /* (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof JoinConstraint)) {
            return false;
        }

        JoinConstraint rhs = (JoinConstraint) obj;
        return new EqualsBuilder()
                .appendSuper(super.equals(rhs))
                .append(archetypeConstraint, rhs.archetypeConstraint)
                .isEquals();
    }

    /**
     * (non-Javadoc)
     *
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("archetypeConstraint", archetypeConstraint).toString();
    }

    /**
     * (non-Javadoc)
     *
     * @see Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        JoinConstraint copy = (JoinConstraint) super.clone();
        copy.archetypeConstraint = (BaseArchetypeConstraint) this.archetypeConstraint.clone();

        return copy;
    }

}
