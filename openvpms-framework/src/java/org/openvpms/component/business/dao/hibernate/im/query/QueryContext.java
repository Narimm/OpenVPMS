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
package org.openvpms.component.business.dao.hibernate.im.query;

import org.apache.commons.lang.WordUtils;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.JoinConstraint;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.RelationalOp;
import org.openvpms.component.system.common.query.SelectConstraint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import static org.openvpms.component.business.dao.hibernate.im.query.QueryBuilderException.ErrorCode.OperatorNotSupported;
import static org.openvpms.component.system.common.query.JoinConstraint.JoinType;


/**
 * This class holds the state of the HQL as it is being built.
 *
 * @author Jim Alateras
 * @author Tim Anderson
 */
public class QueryContext {

    /**
     * The parent context.
     */
    private final QueryContext parent;

    /**
     * The select constraints.
     */
    private List<SelectConstraint> selectConstraints = new ArrayList<SelectConstraint>();

    /**
     * The select clause.
     */
    private StringBuilder selectClause = new StringBuilder("select ");

    /**
     * The default select name, if none are specified.
     */
    private String defaultSelect;

    /**
     * The qualified names in the select clause.
     */
    private List<String> selectNames = new ArrayList<String>();

    /**
     * The names of the object reference being selected.
     */
    private List<String> refSelectNames = new ArrayList<String>();

    /**
     * The where clause part of the hql query
     */
    private Clause whereClause = new Clause();

    /**
     * The from clauses.
     */
    private List<FromClause> fromClauses = new ArrayList<FromClause>();

    /**
     * The from clause stack.
     */
    private Stack<FromClause> fromStack = new Stack<FromClause>();

    /**
     * The ordered clause part of the hql query
     */
    private StringBuilder orderedClause = new StringBuilder(" order by ");

    /**
     * The initial order length.
     */
    private int initOrderedClauseLen = orderedClause.length();

    /**
     * A stack of types while processing the {@link ArchetypeQuery}.
     */
    private Stack<TypeSet> typeStack = new Stack<TypeSet>();

    /**
     * The types, keyed on alias.
     */
    private Map<String, TypeSet> typesets = new LinkedHashMap<String, TypeSet>();

    /**
     * Name allocator for types.
     */
    private final NameAllocator typeNames;

    /**
     * Name allocator for parameters.
     */
    private final NameAllocator paramNames;

    /**
     * a stack of parameters while processing the {@link ArchetypeQuery}.
     */
    private Stack<String> varStack = new Stack<String>();

    /**
     * A stack of join types.
     */
    private Stack<Counter<JoinConstraint.JoinType>> joinStack = new Stack<Counter<JoinConstraint.JoinType>>();

    /**
     * Holds a reference to the parameters and the values used to process
     */
    private final Map<String, Object> params;


    /**
     * Constructs a {@code QueryContext}.
     *
     * @param distinct if {@code true} filter duplicate rows
     * @param parent   the parent context. May be {@code null}
     */
    QueryContext(boolean distinct, QueryContext parent) {
        this.parent = parent;
        if (distinct) {
            selectClause.append("distinct ");
        }
        if (parent != null) {
            typeNames = parent.typeNames;
            paramNames = parent.paramNames;
            params = parent.params;
        } else {
            typeNames = new NameAllocator();
            paramNames = new NameAllocator();
            params = new HashMap<String, Object>();
        }
    }

    /**
     * Returns the HQL query string.
     *
     * @return the HQL query string
     */
    public String getQueryString() {
        StringBuilder result = new StringBuilder(selectClause);
        if (selectNames.isEmpty()) {
            result.append(defaultSelect);
        }
        if (!fromClauses.isEmpty()) {
            result.append(" from ");
            boolean first = true;
            for (FromClause from : fromClauses) {
                if (!first) {
                    if (from.needsComma()) {
                        result.append(", ");
                    } else {
                        result.append(" ");
                    }
                } else {
                    first = false;
                }
                result.append(from);
            }
        }
        if (!whereClause.isEmpty()) {
            result.append(" where ").append(whereClause);
        }
        if (orderedClause.length() != initOrderedClauseLen) {
            result.append(orderedClause);
        }
        return result.toString();
    }

    /**
     * Adds a select constraint.
     *
     * @param select the constraint to add
     */
    public void addSelectConstraint(SelectConstraint select) {
        selectConstraints.add(select);
    }

    /**
     * Returns the select constraints.
     *
     * @return the select constraints
     */
    public List<SelectConstraint> getSelectConstraints() {
        return selectConstraints;
    }

    /**
     * Returns the query parameters.
     *
     * @return the query parameters
     */
    public Map<String, Object> getParameters() {
        return params;
    }

    /**
     * Returns the names in the select clause.
     *
     * @return the select clause names
     */
    public List<String> getSelectNames() {
        return selectNames;
    }

    /**
     * Returns the names of the object reference being selected.
     *
     * @return the object reference names
     */
    public List<String> getRefSelectNames() {
        return refSelectNames;
    }

    /**
     * Returns the types being selected, keyed on type alias.
     *
     * @return a map of type aliases to their corresponding short names
     */
    public Map<String, Set<String>> getSelectTypes() {
        Map<String, Set<String>> result = new HashMap<String, Set<String>>();
        for (String name : selectNames) {
            int index = name.indexOf(".");
            String alias;
            if (index == -1) {
                alias = name;
            } else {
                alias = name.substring(0, index);
            }
            if (result.get(alias) == null) {
                TypeSet set = typesets.get(alias);
                result.put(alias, set.getShortNames());
            }
        }
        return result;
    }

    /**
     * Push a logical operator on the stack.
     *
     * @param op the operator to push
     */
    void pushLogicalOperator(LogicalOperator op) {
        Clause clause = getClause();
        clause.push(op);
    }

    /**
     * Pop the logical operator from the stack.
     */
    void popLogicalOperator() {
        getClause().pop();
    }

    /**
     * Push the type set.
     * <p/>
     * If the type set is already on the stack, a new join constraint will not be added.
     * <p/>
     * The result of the method must be passed to {@link #popTypeSet} to correctly manage the context stack.
     *
     * @param types the type set
     * @return {@code true} if the type set was pushed; or {@code false} if it was already present
     */
    boolean pushTypeSet(TypeSet types) {
        boolean added = addTypeSet(types, types.getAlias());
        String alias = types.getAlias();
        if (added) {
            boolean first = typeStack.isEmpty();
            FromClause fromClause;
            if (first) {
                fromClause = new FromClause(types.getClassName(), alias);
                defaultSelect = alias;
            } else {
                fromClause = new FromClause(JoinType.InnerJoin, types.getClassName(), alias);
            }

            fromClauses.add(fromClause);
            fromStack.push(fromClause);
            joinStack.push(new Counter<JoinType>(JoinType.InnerJoin));
        }

        typeStack.push(types);
        varStack.push(alias);
        return added;
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
                             JoinConstraint.JoinType joinType) {
        if (!addTypeSet(types, property)) {
            throw new QueryBuilderException(QueryBuilderException.ErrorCode.CannotJoinDuplicateAlias,
                                            property, types.getAlias());
        }
        String alias = types.getAlias();
        FromClause fromClause = new FromClause(joinType, varStack.peek(), property, alias);
        fromClauses.add(fromClause);
        fromStack.push(fromClause);
        typeStack.push(types);
        joinStack.push(new Counter<JoinType>(joinType));
        varStack.push(alias);
        return this;
    }

    /**
     * Pop the type set from the stack.
     *
     * @param popJoin if {@code true} pop the join and from constraints
     */
    void popTypeSet(boolean popJoin) {
        varStack.pop();
        typeStack.pop();
        if (popJoin) {
            joinStack.pop();
            fromStack.pop();
        }
    }

    /**
     * Look at the type that is currently on the stack.
     *
     * @return TypeSet
     */
    TypeSet peekTypeSet() {
        return typeStack.peek();
    }

    /**
     * Returns the primary type set. This is the type of the first from clause.
     *
     * @return the primary type, or {@code null} if none is registered
     */
    TypeSet getPrimarySet() {
        return (!typesets.isEmpty()) ? typesets.values().iterator().next() : null;
    }

    /**
     * Determines if the current constraint is an outer join.
     *
     * @return {@code true} if there is a join
     */
    boolean isOuterJoin() {
        return !joinStack.isEmpty() && joinStack.peek().operator != JoinType.InnerJoin;
    }

    /**
     * Returns the type associated with an alias, or the type on type top
     * of the stack if the alias is null.
     *
     * @param alias the type alias. May be {@code null}
     * @return the associated result set or {@code null} if none is found
     */
    TypeSet getTypeSet(String alias) {
        TypeSet result = null;
        if (alias != null && parent != null) {
            result = parent.getTypeSet(alias);
        }
        if (result == null) {
            if (alias != null) {
                result = typesets.get(alias);
            } else if (!typeStack.isEmpty()) {
                result = typeStack.peek();
            }
        }
        return result;
    }

    /**
     * Adds a select constraint.
     *
     * @param alias    the type alias. May be {@code null}
     * @param node     the node name. May be {@code null}
     * @param property the property. May be {@code null}
     */
    void addSelectConstraint(String alias, String node, String property) {
        if (alias == null) {
            alias = varStack.peek();
        }
        if (!selectNames.isEmpty()) {
            selectClause.append(", ");
        }

        selectClause.append(alias);
        if (property != null) {
            selectClause.append('.');
            selectClause.append(property);
        }
        if (node == null) {
            selectNames.add(alias);
        } else {
            selectNames.add(alias + "." + node);
        }
    }

    /**
     * Adds an object reference select constraint.
     *
     * @param alias    the type alias. May be {@code null}
     * @param nodeName the node name. May ve {@code null}
     */
    void addObjectRefSelectConstraint(String alias, String nodeName) {
        if (alias == null) {
            alias = varStack.peek();
        }
        if (!selectNames.isEmpty()) {
            selectClause.append(", ");
        }

        String prefix = (nodeName != null) ? alias + "." + nodeName : alias;
        selectClause.append(prefix);
        selectClause.append(".archetypeId, ");
        selectClause.append(prefix);
        selectClause.append(".id, ");
        selectClause.append(prefix);
        selectClause.append(".linkId");

        selectNames.add(prefix + ".archetypeId");
        selectNames.add(prefix + ".id");
        selectNames.add(prefix + ".linkId");
        refSelectNames.add(prefix);
    }

    /**
     * Adds a constraint to the current from or where clause.
     *
     * @param alias    the type alias. If {@code null} the current alias will be used
     * @param property the property name
     * @param op       the operator
     * @param value    the value
     * @return this context
     */
    QueryContext addConstraint(String alias, String property, RelationalOp op, Object value) {
        if (alias == null) {
            alias = varStack.peek();
        }
        addConstraint(alias + "." + property, op, value);
        return this;
    }

    /**
     * Adds a constraint to the current from or where clause.
     *
     * @param property the property name
     * @param op       the operator
     * @param value    the value
     * @return this context
     */
    QueryContext addConstraint(String property, RelationalOp op, Object value) {
        Clause clause = getClause();
        clause.appendOperator();
        clause.append(property)
                .append(" ")
                .append(getOperator(op, value));

        // check if we need a parameter
        if (value != null) {
            String varName = paramNames.getName(property);
            clause.append(" :").append(varName);
            params.put(varName, getValue(op, value));
        }
        return this;
    }

    /**
     * Add the specified sort constraint.
     *
     * @param alias     the type alias. May be {@code null}
     * @param property  the property to be sorted.
     * @param ascending whether it is ascending or not
     * @return this context
     */
    QueryContext addSortConstraint(String alias, String property, boolean ascending) {
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
    QueryContext addNodeConstraint(String property, NodeConstraint constraint) {
        Clause clause = getClause();
        String varName;
        RelationalOp op = constraint.getOperator();
        String qname = getQualifiedPropertyName(property);
        Object[] parameters = constraint.getParameters();
        switch (op) {
            case BTW:
                if (parameters[0] != null || parameters[1] != null) {
                    // process left hand side
                    clause.push(LogicalOperator.AND);
                    if (parameters[0] != null) {
                        clause.appendOperator();
                        varName = paramNames.getName(property);
                        clause.append(qname)
                                .append(getOperator(RelationalOp.GTE, parameters[0]))
                                .append(" :")
                                .append(varName);
                        params.put(varName, getValue(RelationalOp.GTE, parameters[0]));
                    }

                    // process right hand side
                    if (parameters[1] != null) {
                        clause.appendOperator();
                        varName = paramNames.getName(property);
                        clause.append(qname)
                                .append(getOperator(RelationalOp.LTE, parameters[1]))
                                .append(" :")
                                .append(varName);
                        params.put(varName, getValue(RelationalOp.LTE, parameters[1]));
                    }
                    clause.pop();
                }
                break;

            case EQ:
            case GT:
            case GTE:
            case LT:
            case LTE:
            case NE:
                clause.appendOperator();

                varName = paramNames.getName(property);
                clause.append(qname)
                        .append(" ")
                        .append(getOperator(op, parameters[0]))
                        .append(" :")
                        .append(varName);
                params.put(varName, getValue(op, parameters[0]));
                break;

            case IsNULL:
            case IS_NULL:
            case NOT_NULL:
                clause.appendOperator();
                String opNull = " " + getOperator(op, null);
                if (isReference(property)) {
                    clause.append(qname).append(".id").append(opNull);
                } else {
                    clause.append(qname).append(opNull);
                }
                break;

            case IN:
                clause.appendOperator();
                boolean ref = isReference(property);
                clause.append(qname);
                if (ref) {
                    clause.append(".id");
                }
                clause.append(" ")
                        .append(getOperator(op, null))
                        .append(" (");

                for (int i = 0; i < parameters.length; ++i) {
                    if (i > 0) {
                        clause.append(", ");
                    }
                    varName = paramNames.getName(property);
                    clause.append(":").append(varName);
                    params.put(varName, getValue(op, parameters[i]));
                }
                clause.append(")");
                break;
            default:
                throw new QueryBuilderException(OperatorNotSupported, op);
        }
        return this;
    }

    /**
     * Adds a constraint between two properties.
     *
     * @param lhs      the left-hand property. Must be fully qualified
     * @param operator the operator
     * @param rhs      the right hand property. Must be fully qualified
     * @return this context
     */
    QueryContext addPropertyConstraint(String lhs, RelationalOp operator, String rhs) {
        Clause clause = getClause();
        switch (operator) {
            case EQ:
            case GT:
            case GTE:
            case LT:
            case LTE:
            case NE:
                clause.appendOperator();
                clause.append(lhs)
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
     * Adds a not constraint.
     *
     * @return this context
     */
    QueryContext addNotConstraint() {
        whereClause.appendOperator();
        whereClause.append("not ");
        return this;
    }

    /**
     * Adds an exists constraint.
     *
     * @param query the constraint
     * @return this context
     */
    QueryContext addExistsConstraint(String query) {
        whereClause.append("exists (");
        whereClause.append(query);
        whereClause.append(")");
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
            case IS_NULL:
                return "is NULL";
            case NOT_NULL:
                return "is NOT NULL";
            case IN:
                return "in";
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
                if (param instanceof IMObjectReference) {
                    return ((IMObjectReference) param).getId();
                }
                return param;
        }
    }

    /**
     * Qualifies a property with the current alias.
     *
     * @param property the property
     * @return the qualified property
     */
    private String getQualifiedPropertyName(String property) {
        int index = property.indexOf('.');
        if (index == -1) {
            return varStack.peek() + "." + property;
        }
        String prefix = property.substring(0, index);
        if (typesets.get(prefix) == null) {
            return varStack.peek() + "." + property;
        }
        return property;
    }

    /**
     * Adds a type set, creating an alias for it if one is not specified.
     * <p/>
     * If the type doesn't have an alias, it will be given one.
     * <p/>
     * If the type set already exists with the alias, but with different archetypes, an exception will be thrown.
     *
     * @param types the type set
     * @param alias an alias for the type if it doesn't have one. May be {@code null}
     * @return {@code true} if the type set was pushed; or {@code false} if it was already present
     */
    private boolean addTypeSet(TypeSet types, String alias) {
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
        TypeSet existing = typesets.get(alias);
        if (existing != null) {
            if (!existing.contains(types)) {
                throw new QueryBuilderException(QueryBuilderException.ErrorCode.DuplicateAlias, alias);
            }
            return false; // already present
        }

        typesets.put(types.getAlias(), types);
        return true;
    }

    /**
     * Determines if a property is an object reference.
     *
     * @param property the property
     * @return {@code true} if the property is an object reference
     */
    private boolean isReference(String property) {
        int index = property.indexOf('.');
        String node;
        String alias;
        if (index == -1) {
            alias = varStack.peek();
            node = property;
        } else {
            alias = property.substring(0, index);
            node = property.substring(index + 1);
        }
        TypeSet set = getTypeSet(alias);
        if (set == null) {
            return false;
        }
        for (ArchetypeDescriptor archetype : set.getDescriptors()) {
            NodeDescriptor descriptor = archetype.getNodeDescriptor(node);
            if (descriptor == null || !descriptor.isObjectReference()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the current clause.
     *
     * @return the current clause
     */
    private Clause getClause() {
        return (isOuterJoin()) ? getFromClause() : whereClause;
    }

    /**
     * Returns the current from clause.
     *
     * @return the current from clause
     */
    private FromClause getFromClause() {
        return fromStack.peek();
    }

    /**
     * This is used to track the logical operator.
     */
    enum LogicalOperator {

        AND(" and "),
        OR(" or ");

        /**
         * Holds the string value.
         */
        private String value;

        /**
         * Constructor that takes string value.
         *
         * @param value the operator
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

    private static class Clause {

        StringBuilder clause = new StringBuilder();

        Stack<Counter<LogicalOperator>> stack = new Stack<Counter<LogicalOperator>>();

        public Counter<LogicalOperator> push(LogicalOperator operator) {
            appendOperator();
            Counter<LogicalOperator> result = new Counter<LogicalOperator>(operator);
            stack.push(result);
            append("(");
            return result;
        }

        public void pop() {
            stack.pop();
            append(")");
        }

        /**
         * Append the logical operator if required.
         */
        public void appendOperator() {
            if (!stack.isEmpty()) {
                if (stack.peek().count > 0) {
                    String op = stack.peek().operator.getValue();
                    clause.append(op);
                }
                stack.peek().count++;
            }
        }

        public boolean isEmpty() {
            return clause.length() == 0;
        }

        public String toString() {
            return clause.toString();
        }

        public Clause append(String value) {
            clause.append(value);
            return this;
        }
    }

    private static class FromClause extends Clause {

        private boolean with;

        private final boolean needsComma;

        public FromClause(String type, String alias) {
            this(null, type, alias);

        }

        public FromClause(JoinType join, String type, String alias) {
            this(join, null, type, alias);
        }

        public FromClause(JoinType join, String variable, String property, String alias) {
            if (join == JoinType.InnerJoin && variable == null) {
                needsComma = true;
            } else {
                needsComma = false;
                appendJoin(join);
            }
            if (variable != null) {
                super.append(variable);
                super.append(".");
            }
            super.append(property);
            super.append(" as ");
            super.append(alias);
            with = false;
        }

        public boolean needsComma() {
            return needsComma;
        }

        private void appendJoin(JoinType join) {
            if (join == JoinType.InnerJoin) {
                super.append("inner join ");
            } else if (join == JoinType.LeftOuterJoin) {
                super.append("left outer join ");
            } else if (join == JoinType.RightOuterJoin) {
                super.append("right outer join ");
            }
        }

        public Clause append(String value) {
            if (!with) {
                super.append(" with ");
                with = true;
            }
            super.append(value);
            return this;
        }
    }

    private static class Counter<T> {

        /**
         * The operator.
         */
        T operator;

        /**
         * Counts the number of terms applied to this operator.
         */
        int count;

        /**
         * Create an instance using the specified operator.
         *
         * @param operator the operator
         */
        Counter(T operator) {
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
            if (name.endsWith("DO")) {
                // strip off the DO suffix to make generated HQL a little
                // easier to read
                name = name.substring(0, name.length() - 2);
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