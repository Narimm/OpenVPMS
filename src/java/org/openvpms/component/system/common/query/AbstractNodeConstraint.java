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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;


/**
 * This is the base class for all node contraints. It contains the node name,
 * the operator and the set of parameters,
 *
 * @author Jim Alateras
 * @author Tim Anderson
 */
public abstract class AbstractNodeConstraint implements IConstraint {

    /**
     * Default SUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The type alias. May be <code>null</code>.
     */
    private String alias;

    /**
     * The name of the node descriptor.
     */
    private String nodeName;

    /**
     * The operator to apply
     */
    private RelationalOp operator;

    /**
     * The parameters for the constraint.
     */
    private Object[] parameters;


    /**
     * Construct a constraint on the specified node with the associated
     * relational operator and parameters.
     *
     * @param nodeName   the name of the node descriptor. May be prefixed by the
     *                   type alias
     * @param operator   the relational operator
     * @param parameters the parameters that are used to constrain the value of
     *                   the node
     */
    AbstractNodeConstraint(String nodeName, RelationalOp operator,
                           Object[] parameters) {
        if (StringUtils.isEmpty(nodeName)) {
            throw new ArchetypeQueryException(
                    ArchetypeQueryException.ErrorCode.MustSpecifyNodeName);
        }
        int index = nodeName.indexOf(".");
        if (index != -1) {
            alias = nodeName.substring(0, index);
            this.nodeName = nodeName.substring(index + 1);
        } else {
            this.nodeName = nodeName;
        }

        if (operator == null) {
            throw new ArchetypeQueryException(
                    ArchetypeQueryException.ErrorCode.MustSpecifyOperator);
        }
        this.operator = operator;

        int size = parameters != null ? parameters.length : 0;
        int expected = operator.getParamCount();
        if ((operator == RelationalOp.IN && size < 1)
            || (operator != RelationalOp.IN && size != expected)) {
            throw new ArchetypeQueryException(
                    ArchetypeQueryException.ErrorCode.ParameterCountMismatch,
                    operator, operator.getParamCount(), size);
        }
        this.parameters = parameters;
    }

    /**
     * Returns the type alias.
     *
     * @return the type alias. May be <code>null</code>
     */
    public String getAlias() {
        return alias;
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
     * @return Returns the operator.
     */
    public RelationalOp getOperator() {
        return operator;
    }

    /**
     * @return Returns the parameters.
     */
    public Object[] getParameters() {
        return parameters;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof AbstractNodeConstraint)) {
            return false;
        }

        AbstractNodeConstraint rhs = (AbstractNodeConstraint) obj;
        return new EqualsBuilder()
                .appendSuper(super.equals(rhs))
                .append(nodeName, rhs.nodeName)
                .append(operator, rhs.operator)
                .append(parameters, rhs.parameters)
                .isEquals();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append("alias", alias)
                .append("nodeName", nodeName)
                .append("operator", operator)
                .append("parameters", parameters)
                .toString();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        AbstractNodeConstraint copy = (AbstractNodeConstraint) super.clone();
        copy.nodeName = this.nodeName;
        copy.operator = this.operator;

        copy.parameters = new Object[this.parameters.length];
        System.arraycopy(this.parameters, 0, copy.parameters, 0,
                         this.parameters.length);

        return copy;
    }

}
