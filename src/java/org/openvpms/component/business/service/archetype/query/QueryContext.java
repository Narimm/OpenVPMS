package org.openvpms.component.business.service.archetype.query;

import org.apache.commons.lang.WordUtils;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import static org.openvpms.component.business.service.archetype.query.QueryBuilderException.ErrorCode.OperatorNotSupported;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.CollectionNodeConstraint.JoinType;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.RelationalOp;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * This class holds the state of the HQL as it is being built.
 */
public class QueryContext {

    /**
     * The select clause part of the hql query. This is used if no
     * select constraints are specified.
     */
    private StringBuffer defaultSelectClause = new StringBuffer("select ");
    private int initSelectClauseLen = defaultSelectClause.length();

    private StringBuffer selectClause = new StringBuffer("select ");

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
    private StringBuffer orderedClause = new StringBuffer(" order by ");
    private int initOrderedClauseLen = orderedClause.length();

    /**
     * A stack of types while processing the {@link ArchetypeQuery}.
     */
    private Stack<TypeSet> typeStack
            = new Stack<TypeSet>();

    /**
     * A map of aliased types.
     */
    private Map<String, TypeSet> aliasedTypes
            = new HashMap<String, TypeSet>();

    /**
     * Name allocator for types.
     */
    private NameAllocator typeNames = new NameAllocator();

    /**
     * Name allocator for parameters.
     */
    private NameAllocator paramNames = new NameAllocator();

    /**
     * a stack of parameters while processing the {@link ArchetypeQuery}
     */
    private Stack<String> varStack = new Stack<String>();

    /**
     * A stack of the current logical operator
     */
    private Stack<LogicalOpCounter> opStack = new Stack<LogicalOpCounter>();

    /**
     * Holds a reference to the parameters and the values used to process
     */
    private Map<String, Object> params = new HashMap<String, Object>();

    /**
     * The archetypes being queried.
     */
    private Set<ArchetypeDescriptor> descriptors;


    /**
     * Default constructor. Initialize the operational stack
     */
    QueryContext() {
        this(false);
    }

    /**
     * Constructs a <code>QueryContext</code>.
     *
     * @param distinct if <code>true</code> filter duplicate rows
     */
    QueryContext(boolean distinct) {
        if (distinct) {
            defaultSelectClause.append("distinct ");
            selectClause.append("distinct ");
            initSelectClauseLen = defaultSelectClause.length();
        }
    }

    /**
     * Returns the HQL string.
     *
     * @return String
     *         a valid hql string
     */
    public String getQueryString() {
        return new StringBuffer()
                .append(defaultSelectClause.length() == initSelectClauseLen ? "" : defaultSelectClause + " ")
                .append(fromClause.length() == initFromClauseLen ? "" : fromClause + " ")
                .append(whereClause.length() == initWhereClauseLen ? "" : whereClause)
                .append(orderedClause.length() == initOrderedClauseLen ? "" : orderedClause)
                .toString();
    }

    /**
     * Return the value map for the query.
     *
     * @return Map<String, Object>
     */
    public Map<String, Object> getValueMap() {
        return params;
    }

    /**
     * Returns the descriptors of the archetypes being queried.
     *
     * @return the archetype descriptors
     */
    public Set<ArchetypeDescriptor> getDescriptors() {
        return descriptors;
    }

    /**
     * Push a logical operator on the stack
     *
     * @param op
     * @return QueryContext
     */
    QueryContext pushLogicalOperator(LogicalOperator op) {
        appendLogicalOperator();

        whereClause.append("(");
        opStack.push(new LogicalOpCounter(op));
        return this;
    }

    /**
     * Pop the logical operator on the stack
     *
     * @return LogicalOperator
     */
    LogicalOperator popLogicalOperator() {
        whereClause.append(")");
        return ((LogicalOpCounter) opStack.pop()).operator;
    }

    /**
     * Push the distinct types
     *
     * @param types
     * @return QueryContext
     */
    QueryContext pushTypeSet(TypeSet types) {
        String alias = addTypeSet(types, types.getAlias());

        boolean first = typeStack.empty();
        if (!first) {
            fromClause.append(", ");
        }
        if (first) {
            defaultSelectClause.append(alias);
        }
        fromClause.append(types.getClassName());
        fromClause.append(" as ");
        fromClause.append(alias);

        if (first) {
            descriptors = types.getDescriptors();
        }

        typeStack.push(types);
        varStack.push(alias);
        return this;
    }

    /**
     * Push the distinct type given the specified joinType.
     *
     * @param types    the types to be pushed on the stack
     * @param property the property to join on
     * @param joinType the joinType to use
     * @return QueryContext
     */
    QueryContext pushTypeSet(TypeSet types, String property,
                             JoinType joinType) {
        String alias = addTypeSet(types, property);
        switch (joinType) {
            case InnerJoin:
                fromClause.append(" inner join ")
                        .append(varStack.peek())
                        .append(".")
                        .append(property)
                        .append(" as ")
                        .append(alias);
                break;
            case LeftOuterJoin:
                fromClause.append(" left outer join ")
                        .append(varStack.peek())
                        .append(".")
                        .append(property)
                        .append(" as ")
                        .append(alias);
                break;
            case RightOuterJoin:
                fromClause.append(" right outer  join ")
                        .append(varStack.peek())
                        .append(".")
                        .append(property)
                        .append(" as ")
                        .append(alias);
                break;
            case None:
            default:
                // todo throw an exception
                break;
        }

        typeStack.push(types);
        varStack.push(alias);
        return this;
    }

    private String addTypeSet(TypeSet types,
                              String alias) {
        if (types.getAlias() != null) {
            typeNames.reserve(types.getAlias());
            alias = types.getAlias();
        } else {
            if (alias == null) {
                alias = types.getClassName();
            }
            alias = typeNames.getName(alias);
            types.setAlias(alias);
        }
        aliasedTypes.put(types.getAlias(), types);
        return alias;
    }

    /**
     * Pop the logical operator on the stack
     *
     * @return TypeSet
     */
    TypeSet popTypeSet() {
        varStack.pop();
        return typeStack.pop();
    }

    /**
     * Look at the type that is currently on the stack
     *
     * @return TypeSet
     */
    TypeSet peekTypeSet() {
        return typeStack.peek();
    }

    /**
     * Returns the type associated with an alias, or the type on type top
     * of the stack if the alias is null.
     *
     * @param alias the type alias. May be <code>null</code>
     * @return the associated result set or <code>null</code> if none is found
     */
    TypeSet getTypeSet(String alias) {
        TypeSet result = null;
        if (alias != null) {
            result = aliasedTypes.get(alias);
        } else if (!typeStack.isEmpty()) {
            result = typeStack.peek();
        }
        return result;
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
     * Adds a select constraint.
     *
     * @param alias    the type alias. May be <code>null</code>
     * @param property the property. May be <code>null</code>
     */
    void addSelectConstraint(String alias, String property) {
        if (alias != null) {
            selectClause.append(alias);
            if (property != null) {
                selectClause.append('.');
                selectClause.append(property);
            }
        } else if (property != null) {
            selectClause.append(property);
        }
    }

    /**
     * Adds a where constraint.
     *
     * @param alias    the type alias. May be <code>null</code>
     * @param property the property
     * @param op       the relational operator to apply
     * @param value    the object value
     * @return this context
     */
    QueryContext addWhereConstraint(String alias, String property,
                                    RelationalOp op, Object value) {
        if (alias == null) {
            alias = varStack.peek();
        }
        addWhereConstraint(alias + "." + property, op, value);
        return this;
    }

    /**
     * Adds a where constraint.
     *
     * @param property the fully qualified property
     * @param op       the relational operator to apply
     * @param value    the object value
     * @return this context
     */
    QueryContext addWhereConstraint(String property, RelationalOp op,
                                    Object value) {

        appendLogicalOperator();
        whereClause.append(property)
                .append(" ")
                .append(getOperator(op, value));

        // check if we need a parameter
        if (value != null) {
            String varName = paramNames.getName(property);
            whereClause.append(" :")
                    .append(varName);
            params.put(varName, getValue(op, value));
        }
        opStack.peek().count++;
        return this;
    }

    /**
     * Add the specified sort constraint.
     *
     * @param alias     the type alias. May be <code>null</code>
     * @param property  the property to be sorted.
     * @param ascending whether it is ascending or not
     * @return this context
     */
    QueryContext addSortConstraint(String alias, String property,
                                   boolean ascending) {
        if (orderedClause.length() > initOrderedClauseLen) {
            orderedClause.append(", ");
        }
        if (alias == null) {
            alias = varStack.peek();
        }
        orderedClause.append(alias)
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
     * Add a where constraint given the property and the node constraint.
     *
     * @param property   the attribute to constrain
     * @param constraint the node constraint
     * @return this context
     */
    QueryContext addWhereConstraint(String property,
                                    NodeConstraint constraint) {
        String varName;
        switch (constraint.getOperator()) {
            case BTW:
                if (constraint.getParameters()[0] != null
                        || constraint.getParameters()[1] != null) {
                    // process left hand side
                    pushLogicalOperator(LogicalOperator.And);
                    if (constraint.getParameters()[0] != null) {
                        appendLogicalOperator();
                        varName = paramNames.getName(property);
                        whereClause.append(getQualifiedPropertyName(property))
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
                        varName = paramNames.getName(property);
                        whereClause.append(getQualifiedPropertyName(property))
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

                varName = paramNames.getName(property);
                whereClause.append(getQualifiedPropertyName(property))
                        .append(" ")
                        .append(getOperator(constraint.getOperator(),
                                            constraint.getParameters()[0]))
                        .append(" :")
                        .append(varName);
                params.put(varName, getValue(constraint.getOperator(),
                                             constraint.getParameters()[0]));
                break;

            case IsNULL:
                appendLogicalOperator();
                whereClause.append(getQualifiedPropertyName(property))
                        .append(" ")
                        .append(getOperator(constraint.getOperator(), null));
                break;

            default:
                throw new QueryBuilderException(
                        OperatorNotSupported, constraint.getOperator());
        }

        return this;
    }

    /**
     * Add a where constraint between two properties.
     *
     * @param lhs      the left-hand property. Must be fully qualified
     * @param operator the operator
     * @param rhs      the right hand property. Must be fully qualified
     * @return this context
     */
    QueryContext addPropertyWhereConstraint(String lhs, RelationalOp operator,
                                            String rhs) {
        switch (operator) {
            case EQ:
            case GT:
            case GTE:
            case LT:
            case LTE:
            case NE:
                appendLogicalOperator();
                whereClause.append(lhs)
                        .append(" ")
                        .append(getOperator(operator, rhs))
                        .append(" ")
                        .append(rhs);
                break;
            default:
                throw new QueryBuilderException(OperatorNotSupported, operator);
        }

        return this;
    }

    /**
     * Return the HQL operator.
     *
     * @param operator the operator type
     * @param param    the value to associated with the operator
     * @return String
     *         the fragement
     */
    private String getOperator(RelationalOp operator, Object param) {
        switch (operator) {
            case EQ:
                if (param instanceof String) {
                    String sparam = (String) param;
                    if (sparam.contains("%") || sparam.contains("*")) {
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
                throw new QueryBuilderException(OperatorNotSupported, operator);
        }
    }

    /**
     * Returns the value.
     *
     * @param operator the operator type
     * @param param    the value to associated with the operator
     * @return the value
     */
    private Object getValue(RelationalOp operator, Object param) {
        switch (operator) {
            case EQ:
                if (param instanceof String) {
                    return ((String) param).replace("*", "%");
                }
                return param;

            default:
                return param;
        }
    }

    /**
     * Append the logical operator to the where clause.
     */
    private void appendLogicalOperator() {
        if (opStack.size() > 0) {
            if (opStack.peek().count > 0) {
                whereClause.append(opStack.peek().operator.getValue());
            }
            opStack.peek().count++;
        }
    }

    private String getQualifiedPropertyName(String property) {
        int index = property.indexOf('.');
        if (index == -1) {
            return varStack.peek() + "." + property;
        }
        String prefix = property.substring(0, index);
        if (aliasedTypes.get(prefix) == null) {
            return varStack.peek() + "." + property;
        }
        return property;
    }

    /**
     * This is used to track the logical operator.
     */
    enum LogicalOperator {
        And(" and "),
        Or(" or ");

        /**
         * Holds the string value.
         */
        private String value;

        /**
         * Constructor that takes string value.
         *
         * @param value
         */
        LogicalOperator(String value) {
            this.value = value;
        }

        /**
         * Return the value.
         *
         * @return String
         */
        public String getValue() {
            return value;
        }
    }


    /**
     * Private anonymous class to track the usage of the logical operator.
     */
    class LogicalOpCounter {

        /**
         * The relational operator.
         */
        LogicalOperator operator;

        /**
         * Counts the number of terms applied to this operator.
         */
        int count;

        /**
         * Create an instance using the specified operator.
         *
         * @param operator
         */
        LogicalOpCounter(LogicalOperator operator) {
            this.operator = operator;
        }
    }

    private static class NameAllocator {

        private Set<String> names = new HashSet<String>();

        public void reserve(String name) {
            names.add(name);
        }

        public String getName(String name) {
            int index = name.lastIndexOf(".");
            if (index != -1) {
                name = name.substring(index + 1);
            }
            name = WordUtils.uncapitalize(name);
            int i = 0;
            String result = name + i;
            while (names.contains(result)) {
                ++i;
                result = name + i;
            }
            names.add(result);
            return result;
        }

    }

}