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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.openvpms.component.business.domain.archetype.ArchetypeId;


/**
 * Defines a constraint on a collection node, where the elements of the
 * collection are archetyped instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class CollectionNodeConstraint implements IConstraintContainer {

    /**
     * How to join with the outer table.
     */
    public enum JoinType {
        None,
        InnerJoin,
        LeftOuterJoin,
        RightOuterJoin
    }

    /**
     * Default SUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The node name.
     */
    private final String nodeName;

    /**
     * The join type. Defaults to <code>InnerJoin</code>.
     */
    private JoinType joinType = JoinType.InnerJoin;


    /**
     * The archetype constraint associated with this collection
     * node.
     */
    private BaseArchetypeConstraint archetypeConstraint;


    /**
     * Constructor to initialize the node name.
     *
     * @param nodeName   the node name, optionaly prefixed by the type alias
     * @param activeOnly constraint to active only instances
     */
    public CollectionNodeConstraint(String nodeName, boolean activeOnly) {
        this(nodeName, new ArchetypeConstraint(activeOnly));
    }

    /**
     * Constructor to initialize the node name. It implies that activeOnly is
     * false.
     *
     * @param nodeName the node name, optionaly prefixed by the type alias
     */
    public CollectionNodeConstraint(String nodeName) {
        this(nodeName, false);
    }

    /**
     * Create a query for the specified archetype id.
     *
     * @param nodeName    the node name, optionaly prefixed by the type alias
     * @param archetypeId a valid archetype identity
     * @param activeOnly  constraint to active only objects
     */
    public CollectionNodeConstraint(String nodeName, ArchetypeId archetypeId,
                                    boolean activeOnly) {
        this(nodeName, new ArchetypeIdConstraint(archetypeId, activeOnly));
    }

    /**
     * Create an instance of this query specifying one or more elements.
     * Any of the parameters can be null or may  include the wild card
     * character.
     *
     * @param nodeName    the node name, optionaly prefixed by the type alias
     * @param rmName      the reference model name (optional)
     * @param entityName  the entity name (optional)
     * @param conceptName the concept name (optional)
     * @param primaryOnly only deal with primary archetypes
     * @param activeOnly  constraint to active only objects
     */
    public CollectionNodeConstraint(String nodeName, String rmName,
                                    String entityName, String conceptName,
                                    boolean primaryOnly, boolean activeOnly) {
        this(nodeName, new LongNameConstraint(rmName, entityName, conceptName,
                                              primaryOnly, activeOnly));
    }

    /**
     * Create an instance of this constraint with the specified short name.
     *
     * @param nodeName    the node name, optionaly prefixed by the type alias
     * @param shortName   the short name
     * @param primaryOnly only deal with primary archetypes
     * @param activeOnly  constraint to active only objects
     */
    public CollectionNodeConstraint(String nodeName, String shortName,
                                    boolean primaryOnly, boolean activeOnly) {
        this(nodeName,
             new ShortNameConstraint(shortName, primaryOnly, activeOnly));
    }

    /**
     * Create an instance of this class with the specified archetype
     * short names.
     *
     * @param nodeName    the node name, optionaly prefixed by the type alias
     * @param shortNames  an array of archetype short names
     * @param primaryOnly only deal with primary archetypes
     * @param activeOnly  constraint to active only objects
     */
    public CollectionNodeConstraint(String nodeName, String[] shortNames,
                                    boolean primaryOnly, boolean activeOnly) {
        this(nodeName, new ShortNameConstraint(shortNames, primaryOnly,
                                               activeOnly));
    }

    /**
     * Construct a constraint on a collection node with the specified
     * constraint.
     *
     * @param nodeName   the node name, optionaly prefixed by the type alias
     * @param constraint the archetype constraint to use for the collection node
     */
    public CollectionNodeConstraint(String nodeName,
                                    BaseArchetypeConstraint constraint) {
        if (StringUtils.isEmpty(nodeName)) {
            throw new ArchetypeQueryException(
                    ArchetypeQueryException.ErrorCode.MustSpecifyNodeName);
        }
        this.nodeName = nodeName;
        this.archetypeConstraint = constraint;
    }

    /**
     * Returns the archetype constraint.
     *
     * @return the archetype constraint
     */
    public BaseArchetypeConstraint getArchetypeConstraint() {
        return archetypeConstraint;
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.system.common.query.IConstraintContainer#add(org.openvpms.component.system.common.query.IConstraint)
     */
    public CollectionNodeConstraint add(IConstraint constraint) {
        this.archetypeConstraint.add(constraint);
        return this;
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.system.common.query.IConstraintContainer#remove(org.openvpms.component.system.common.query.IConstraint)
     */
    public CollectionNodeConstraint remove(IConstraint constraint) {
        this.archetypeConstraint.remove(constraint);
        return this;
    }

    /**
     * Returns the node name.
     *
     * @return the node name
     */
    public String getNodeName() {
        return nodeName;
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
     * @param joinType the join type
     */
    public CollectionNodeConstraint setJoinType(JoinType joinType) {
        this.joinType = joinType;
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

        if (!(obj instanceof CollectionNodeConstraint)) {
            return false;
        }

        CollectionNodeConstraint rhs = (CollectionNodeConstraint) obj;
        return new EqualsBuilder()
                .appendSuper(super.equals(rhs))
                .append(nodeName, rhs.nodeName)
                .append(archetypeConstraint, rhs.archetypeConstraint)
                .isEquals();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("nodeName", nodeName)
                .append("archetypeConstraint", archetypeConstraint).toString();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        CollectionNodeConstraint copy = (CollectionNodeConstraint) super.clone();
        copy.archetypeConstraint = (BaseArchetypeConstraint) this.archetypeConstraint.clone();

        return copy;
    }
}
