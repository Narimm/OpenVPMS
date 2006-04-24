package org.openvpms.component.business.service.archetype.query;

// java core
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

// openvpms-framework
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.RelationalOp;
import org.openvpms.component.system.common.query.CollectionNodeConstraint.JoinType;
import org.openvpms.component.business.service.archetype.query.QueryBuilder.DistinctTypesResultSet;

/**
 * This class holds the state of the HQL as it is beign built
 */
public class QueryContext {
    /**
     * The select clause part of the hql query
     */
    private StringBuffer selectClause = new StringBuffer("select ");
    private int initSelectClauseLen = selectClause.length();
    
    /**
     * The from clause part of the hql query
     */
    private StringBuffer fromClause = new StringBuffer("from ");
    private int initFromClauseLen = fromClause.length();
    
    /**
     * The where clause part of the hql query
     */
    private StringBuffer whereClause = new StringBuffer("where ");
    private int initWhereClauseLen = whereClause.length();
    
    /**
     * The ordered clause part of the hql query
     */
    private StringBuffer orderedClause = new StringBuffer("order by ");
    private int initOrderedClauseLen = orderedClause.length();
    
    /**
     * a stack of types while processing the {@link ArchetypeQuery}
     */
    private Stack<DistinctTypesResultSet> typeStack = new Stack<DistinctTypesResultSet>();
    
    /**
     * a stack of parameters while processing the {@link ArchetypeQuery}
     */
    private  Stack<String> varStack = new Stack<String>();
    
    /**
     * A stack of the current logical operator
     */
    private Stack<LogicalOpCounter> opStack = new Stack<LogicalOpCounter>();
    
    /**
     * Holds a reference to the parameters and the values used to process
     */
    private Map<String, Object> params = new HashMap<String, Object>();
    
    
    /**
     * Used to manage dynamic variable names
     */
    private String varName = "var";
    
    /**
     * The index that is appended to the varName
     */
    private int varIndex = 0;
    
    /**
     * Default constructor. Initialize the operational stack
     */
    QueryContext() {
    }
    
    /**
     * Retrieve a new variable name
     */
    String getVariableName() {
        return varName + Integer.toString(varIndex++);
    }
    
    /**
     * Push a logical operator on the stack
     * 
     * @param operator
     * @return QueryContext
     */
    QueryContext pushLogicalOperator(LogicalOperator op) {
        appendLogicalOperator();
        
        whereClause.append(" (");
        opStack.push(new LogicalOpCounter(op));
        return this;
    }
    
    /**
     * Pop the logical operator on the stack
     * 
     * @return LogicalOperator
     */
    LogicalOperator popLogicalOperator() {
        whereClause.append(") ");
        return ((LogicalOpCounter)opStack.pop()).operator;
    }
    
    /**
     * Push the distinct types
     * 
     * @param operator
     * @return QueryContext
     */
    QueryContext pushDistinctTypes(DistinctTypesResultSet types) {
        String varName = getVariableName();
        
        // check the select clause
        if (typeStack.size() == 0) {
            selectClause.append(varName)
                .append(" ");
            
            fromClause.append(types.type)
            .append("  as ")
            .append(varName)
            .append(" ");
        }
        
        typeStack.push(types);
        varStack.push(varName);
        return this;
    }
    
    /**
     * Push the distinct type given the specified joinTypw
     * 
     * @param types
     *            the types to be pushed on the stack
     * @param property
     *            the property to join on            
     * @param joinType
     *            the joinType to use
     * @return QueryContext                        
     */
    QueryContext pushDistinctTypes(DistinctTypesResultSet types, String property,
            JoinType joinType) {
        String varName = getVariableName();
        
        switch (joinType) {
        case InnerJoin:
            fromClause.append(" inner join ")
                .append(varStack.peek())
                .append(".")
                .append(property)
                .append(" as ")
            .append(varName)
            .append(" ");
            break;
            
        case LeftOuterJoin:
            fromClause.append(" left outer join ")
            .append(varStack.peek())
            .append(".")
            .append(property)
            .append(" as ")
            .append(varName)
            .append(" ");
            break;
            
        case RightOuterJoin:
            fromClause.append(" right outer  join ")
            .append(varStack.peek())
            .append(".")
            .append(property)
            .append(" as ")
            .append(varName);
            break;
            
        case None:
        default:
            // throw an exception
            break;
        }
        
        typeStack.push(types);
        varStack.push(varName);
        return this;
    }
    
    /**
     * Pop the logical operator on the stack
     * 
     * @return DistinctTypesResultSet
     */
    DistinctTypesResultSet popDistinctTypes() {
        varStack.pop();
        return typeStack.pop();
    }
    
    /**
     * Look at the rype that is currently on the stack
     * 
     * @return DistinctTypesResultSet
     */
    DistinctTypesResultSet peekDistinctTypes() {
        return typeStack.peek();
    }
    
    /**
     * Push a variable name on the stack
     * 
     * @param varName
     * @return QueryContext
     */
    QueryContext pushVariable(String varName) {
        varStack.push(varName);
        return this;
    }
    
    /**
     * Pop the variable name on the stack
     * 
     * @return String
     */
    String popVariable() {
        return varStack.pop();
    }
    
    /**
     * Add a where constraint.  
     * 
     * @param property
     *            the property
     * @param c
     *            the relational operator to apply            
     * @param value
     *            the object value
     * @return QueryContext                        
     */
    QueryContext addWhereConstraint(String property, RelationalOp op, Object value) {
        
        appendLogicalOperator();
        whereClause.append(varStack.peek())
            .append(".")
            .append(property)
            .append(" ")
            .append(getOperator(op, value));
        
        // check if we need a parameter
        if (value != null) {
            String varName = getVariableName();
            whereClause.append(" :")
                .append(varName);
            params.put(varName, getValue(op, value));
        }
        opStack.peek().count++;
        return this;
    }
    
    /**
     * Add the specified sort constraint
     * 
     * @param property
     *            the property to be sorted.
     * @param ascending
     *            whether it is ascending or not
     * return QueryContext                        
     */
    QueryContext addSortConstraint(String property, boolean ascending) {
        if (orderedClause.length() > initOrderedClauseLen) {
            orderedClause.append(", ");
        }
        
        orderedClause.append(varStack.peek())
                .append(".")
                .append(property);
        if (ascending) {
            orderedClause.append(" asc");
        } else {
            orderedClause.append(" desc");
        }

        return this;
    }
    
    /**
     * Add a where constraint given the property and the node constraint
     * 
     * @param property
     *            the attribue to constrain
     * @param constraint
     *            the node constraint
     * @return QueryContext                        
     */
    QueryContext addWhereConstraint(String property, NodeConstraint constraint) {
        String varName = null;
        
        switch (constraint.getOperator()) {
        case BTW:
            if ((constraint.getParameters()[0] != null) ||
                (constraint.getParameters()[1] != null)) {
                // process left hand side
                pushLogicalOperator(LogicalOperator.And);
                if (constraint.getParameters()[0] != null) {
                    appendLogicalOperator();
                    varName = getVariableName();
                    whereClause.append(varStack.peek())
                        .append(".")
                        .append(property)
                        .append(getOperator(RelationalOp.GTE, 
                                constraint.getParameters()[0]))
                        .append(" :")
                        .append(varName);
                    params.put(varName, getValue(RelationalOp.GTE, 
                            constraint.getParameters()[0]));
                }
                
                // process right hand side
                if (constraint.getParameters()[1] != null) {
                    appendLogicalOperator();
                    varName = getVariableName();
                    whereClause.append(varStack.peek())
                        .append(".")
                        .append(property)
                        .append(getOperator(RelationalOp.LTE, 
                                constraint.getParameters()[1]))
                        .append(" :")
                        .append(varName);
                    params.put(varName, getValue(RelationalOp.LTE, 
                            constraint.getParameters()[1]));
                }
                popLogicalOperator();
            }
            break;
            
        case EQ:
        case GT:
        case GTE:
        case LT:
        case LTE:
        case NE:
            appendLogicalOperator();

            varName = getVariableName();
            whereClause.append(varStack.peek())
                .append(".")
                .append(property)
                .append(" ")
                .append(getOperator(constraint.getOperator(), constraint.getParameters()[0]))
                .append(" :")
                .append(varName);
            params.put(varName, getValue(constraint.getOperator(), constraint.getParameters()[0]));
            break;

        case IsNULL:
            appendLogicalOperator();
            whereClause.append(varStack.peek())
                .append(".")
                .append(property)
                .append(" ")
                .append(getOperator(constraint.getOperator(), null));
            break;

        default:
            throw new QueryBuilderException(
                    QueryBuilderException.ErrorCode.OperatorNotSupported,
                    new Object[] {constraint.getOperator()});
        }
        
        return this;
    }

    /**
     * Return the HQL string
     * 
     * @return String
     *            a valid hql string
     */
    public String getQueryString() {
        return new StringBuffer()
            .append(selectClause.length() == initSelectClauseLen ? "" : selectClause)
            .append(fromClause.length() == initFromClauseLen ? "" : fromClause)
            .append(whereClause.length() == initWhereClauseLen ? "" : whereClause)
            .append(orderedClause.length() == initOrderedClauseLen ? "" : orderedClause)
            .toString();
    }
    
    /**
     * Return the value map for the query
     * 
     * @return Map<String, Object>
     */
    public Map<String, Object> getValueMap() {
        return params;
    }

    /**
     * Return the HQL operator 
     * 
     * @param operator
     *            the operator type
     * @param param
     *            the value to associated with the operator            
     * @return String
     *            the fragement
     */
    private String getOperator(RelationalOp operator, Object param) {
        switch (operator) {
        case EQ:
            if (param instanceof String) {
                String sparam = (String)param;
                if ((sparam.contains("%")) || (sparam.contains("*"))) {
                    return "like";
                }
            }
            return "=";
            
        case GT:
            return ">";
            
        case GTE:
            return ">=";
            
        case LT:
            return "<";
            
        case LTE:
            return "<=";

        case NE:
            return "!=";

        case IsNULL:
            return "is NULL";

        default:
            throw new QueryBuilderException(
                    QueryBuilderException.ErrorCode.OperatorNotSupported,
                    new Object[] {operator});
        }
    }
    
    /**
     * Return the value
     * 
     * @param operator
     *            the operator type
     * @param param
     *            the value to associated with the operator            
     * @return Object
     *            the value
     */
    private Object getValue(RelationalOp operator, Object param) {
        switch (operator) {
        case EQ:
            if (param instanceof String) {
                return ((String)param).replace("*", "%");
            }
            return param;

        default:
            return param;
        }
    }
    
    /**
     * Append the logical operator to the where class
     */
    private void appendLogicalOperator() {
        if (opStack.size() > 0) {
            if (opStack.peek().count > 0) {
                whereClause.append(opStack.peek().operator.getValue());
            }
            opStack.peek().count++;
        }
    }

    /**
     * This is used to track the logical operator
     */
    enum LogicalOperator {
        And(" and "),
        Or(" or ");
        
        /**
         * Holds the string value
         */
        private String value;
        
        /**
         * Constructor that takes string value
         * 
         * @param  value
         */
        LogicalOperator(String value) {
            this.value = value;
        }
        
        /**
         * Return the value
         * 
         * @return String
         */
        public String getValue() {
            return value;
        }
    }

    
    /**
     * Private anonymous class to track the usage of the logical operator
     */
    class LogicalOpCounter {
        /**
         * The relational operator
         */
        LogicalOperator operator;
        
        /**
         * Counts the number of terms applied to this operator  
         */
        int count;
        
        /**
         * Create an instance using the specified operator
         * 
         * @param operator
         */
        LogicalOpCounter(LogicalOperator operator) {
            this.operator = operator;
        }
    }
    
}