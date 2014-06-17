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
 * This class is used to help define constraints on part of the archetype,
 * whether it be the entity name, the concept name or some other part. This class
 * can be used instead of the classes derived from {@link BaseArchetypeConstraint}
 * in the query.  In particular it is usedful to use with {@link CollectionNodeConstraint}.
 *
 * @author Jim Alateras
 * @author Tim Anderson
 */
public class ArchetypeNodeConstraint implements IConstraint {

    /**
     * Default SUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The operator.
     */
    private RelationalOp operator;

    /**
     * The parameter to use with the relational operator
     */
    private Object parameter;


    /**
     * Construct an instance of this class specifying the archetype property
     * you wish to constrain and the associated operator. In some instance, such
     * as an 'is null' constraint a parameter is not required.
     *
     * @param property the archetype property to constrain
     * @param operator the operator to use.
     * @deprecated ArchetypeProperty no longer supported
     */
    @Deprecated
    public ArchetypeNodeConstraint(ArchetypeProperty property,
                                   RelationalOp operator) {
        this(property, operator, null);
    }

    /**
     * Construct an instance of this class specifying the archetype property,
     * the relational operator and the associated parameter.
     *
     * @param property the archetype property to constraint
     * @param operator the operator to use
     * @param param    the parameter using in conjunction with the operator
     * @deprecated ArchetypeProperty no longer supported
     */
    @Deprecated
    public ArchetypeNodeConstraint(ArchetypeProperty property,
                                   RelationalOp operator, Object param) {
        this(operator, param);
    }

    /**
     * Construct an instance of this class specifying the relational operator
     * and the associated parameter.
     *
     * @param operator the operator to use
     */
    public ArchetypeNodeConstraint(RelationalOp operator) {
        this(operator, null);
    }

    /**
     * Construct an instance of this class specifying the relational operator
     * and the associated parameter.
     *
     * @param operator the operator to use
     * @param param    the parameter used in conjunction with the operator. May be <tt>null</tt>
     */
    public ArchetypeNodeConstraint(RelationalOp operator, Object param) {
        if (operator == RelationalOp.BTW) {
            throw new ArchetypeQueryException(
                    ArchetypeQueryException.ErrorCode.BtwInvalidForArchetypeNodeConstraint);
        }

        this.operator = operator;
        this.parameter = param;
    }

    /**
     * @return Returns the operator.
     */
    public RelationalOp getOperator() {
        return operator;
    }

    /**
     * @return Returns the parameter.
     */
    public Object getParameter() {
        return parameter;
    }

    /**
     * @return Returns the property.
     */
    @Deprecated
    public ArchetypeProperty getProperty() {
        return null;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof ArchetypeNodeConstraint)) {
            return false;
        }

        ArchetypeNodeConstraint rhs = (ArchetypeNodeConstraint) obj;
        return new EqualsBuilder()
                .append(operator, rhs.operator)
                .append(parameter, rhs.parameter)
                .isEquals();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("operator", operator)
                .append("parameter", parameter)
                .toString();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        ArchetypeNodeConstraint copy = (ArchetypeNodeConstraint) super.clone();
        copy.operator = this.operator;
        copy.parameter = this.parameter;

        return copy;
    }
}
