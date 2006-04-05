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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * This is the base class for all node descriptors. It contains the sort 
 * order, the node name, the operator and the set of parameters,
 * 
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public abstract class AbstractNodeConstraint implements IConstraint {

    /**
     * Default SUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * The name of the node descriptor
     */
    protected String nodeName;
    
    /**
     * The sort order
     */
    protected SortOrder sortOrder = SortOrder.None;
    
    /**
     * The operator to apply
     */
    protected RelationalOp operator;
    
    /**
     * The parameters for the constraint
     */
    protected Object[] parameters;
    
    
    /**
     * Construct a constraint on the specified node with the associated relational
     * operator and parameters
     * 
     * @param nodeName
     *            the name of the node descriptor
     * @param operator
     *            the relational operator
     * @param parameters
     *            the parameters that are used to constrain the value of the
     *            node                         
     */
    AbstractNodeConstraint(String nodeName, RelationalOp operator, Object[] parameters) {
        if (StringUtils.isEmpty(nodeName)) {
            throw new ArchetypeQueryException(
                    ArchetypeQueryException.ErrorCode.MustSpecifyNodeName);
        }
        this.nodeName = nodeName;
        
        if (operator == null) {
            throw new ArchetypeQueryException(
                    ArchetypeQueryException.ErrorCode.MustSpecifyOperator);
        }
        this.operator = operator;
        
        if ((parameters == null) ||
            (parameters.length != operator.getParamCount())) {
            throw new ArchetypeQueryException(
                    ArchetypeQueryException.ErrorCode.ParameterCountMismatch,
                    new Object[] {operator, operator.getParamCount(), 
                            (parameters == null ? 0 : parameters.length)});
        }
        this.parameters = parameters;
    }

    /**
     * @return Returns the nodeName.
     */
    public String getNodeName() {
        return nodeName;
    }
    
    /**
     * @return Returns the sortOrder.
     */
    public SortOrder getSortOrder() {
        return sortOrder;
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
            .append(sortOrder, rhs.sortOrder)
            .append(operator, rhs.operator)
            .append(parameters, rhs.parameters)
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
            .append("sortOrder", sortOrder)
            .append("operator", operator)
            .append("parameters", parameters)
            .toString();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        AbstractNodeConstraint copy = (AbstractNodeConstraint)super.clone();
        copy.nodeName = this.nodeName;
        copy.operator = this.operator;
        copy.sortOrder = this.sortOrder;
        
        copy.parameters = new Object[this.parameters.length];
        System.arraycopy(this.parameters, 0, copy.parameters, 0, 
                this.parameters.length);
        
        return copy;
    }

}
