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


/**
 * Used to construct a query constraint on a particular node of the enclosed
 * {@link ArchetypeConstraint}. A constraint required a node name, a relational
 * operator and one or more values. The number of values will depend on the 
 * select operator type. For example a 'GT' operator requires a single parameter
 * where an 'BTW' operator requires two parameters.
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class NodeConstraint extends AbstractNodeConstraint {

    /**
     * Default SUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * Construct a constraint on the specified node with the equal relational
     * operator and the specified value
     * 
     * @param nodeName
     *            the name of the node descriptor
     * @param parameter
     *            the parameter that is used to constrain the value of the
     *            node                         
     */
    public NodeConstraint(String nodeName, Object parameters) {
        super(nodeName, RelationalOp.EQ, new Object[] {parameters});
    }

    /**
     * Construct a constraint on the specified node with the associated relational
     * operator and parameter
     * 
     * @param nodeName
     *            the name of the node descriptor
     * @param operator
     *            the relational operator
     * @param parameter
     *            the parameter that is used to constrain the value of the
     *            node                         
     */
    public NodeConstraint(String nodeName, RelationalOp operator, Object parameters) {
        super(nodeName, operator, new Object[] {parameters});
    }

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
    public NodeConstraint(String nodeName, RelationalOp operator, Object[] parameters) {
        super(nodeName, operator, parameters);
    }

    /**
     * @param sortOrder The sortOrder to set.
     */
    public NodeConstraint setSortOrder(SortOrder sortOrder) {
        this.sortOrder = sortOrder;
        return this;
    }

    /**
     * @param operator The operator to set.
     */
    public NodeConstraint setOperator(RelationalOp operator) {
        this.operator = operator;
        return this;
    }

    /**
     * @param parameters The parameters to set.
     */
    public NodeConstraint setParameters(Object[] parameters) {
        this.parameters = parameters;
        return this;
    }
}
