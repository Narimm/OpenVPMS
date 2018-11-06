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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.system.common.query.criteria;

import org.openvpms.component.model.object.IMObject;
import org.openvpms.component.query.criteria.CriteriaQuery;
import org.openvpms.component.query.criteria.Root;
import org.openvpms.component.query.criteria.Subquery;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Selection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Default implementation of {@link CriteriaQuery}.
 *
 * @author Tim Anderson
 */
public class CriteriaQueryImpl<T> implements CriteriaQuery<T> {

    /**
     * The result type of the query.
     */
    private final Class<T> resultType;

    /**
     * The context.
     */
    private final Context context;

    /**
     * The selection, if {@link #select(Selection)} is used.
     */
    private Selection<? super T> selection;

    /**
     * The selections, if {@link #multiselect(Selection[])} is used.
     */
    private Selection<?>[] selections;

    /**
     * Determines if duplicate results are excluded.
     */
    private boolean distinct;

    /**
     * The query roots.
     */
    private List<RootImpl<? extends IMObject>> roots = new ArrayList<>();

    /**
     * The subqueries.
     */
    private List<SubqueryImpl<?>> subqueries = new ArrayList<>();

    /**
     * The where clause.
     */
    private Expression<Boolean> where;

    /**
     * The group by clause
     */
    private List<Expression<?>> groupBy;

    /**
     * The having clause.
     */
    private Expression<Boolean> having;

    /**
     * The order by clause.
     */
    private List<Order> orderBy;

    /**
     * Constructs a {@link CriteriaQueryImpl}.
     *
     * @param resultType the result type of the query
     * @param context    the context
     */
    public CriteriaQueryImpl(Class<T> resultType, Context context) {
        this.resultType = resultType;
        this.context = context;
    }

    /**
     * Returns the result type of the query.
     *
     * @return the result type of the query
     */
    @Override
    public Class<T> getResultType() {
        return resultType;
    }

    /**
     * Determines if duplicate results should be excluded.
     * <p>
     * By default, duplicates are included.
     *
     * @param distinct if {@code true}, return duplicate results, otherwise include them
     * @return this query
     */
    @Override
    public CriteriaQuery<T> distinct(boolean distinct) {
        this.distinct = distinct;
        return this;
    }

    /**
     * Determines if duplicate results should be excluded.
     *
     * @return {@code true} if duplicate results should be excluded
     */
    public boolean getDistinct() {
        return distinct;
    }

    /**
     * Specifies the item to be returned in the query results.
     *
     * @param selection the selection. Any existing selection will be replaced
     * @return this query
     */
    @Override
    public CriteriaQuery<T> select(Selection<? super T> selection) {
        this.selection = selection;
        selections = null;
        return this;
    }

    /**
     * Specifies the items to be returned in the query results.
     *
     * @param selections the items corresponding to the results to be returned by the query
     * @return the modified query
     */
    @Override
    public CriteriaQuery<T> multiselect(Selection<?>... selections) {
        this.selections = selections;
        selection = null;
        return this;
    }

    /**
     * Returns the selection.
     *
     * @return the selection. May be {@code null}
     */
    public Selection<? super T> getSelection() {
        return selection;
    }

    /**
     * Returns the selection.
     *
     * @return the selection. May be {@code null}
     */
    public Selection<?>[] getMultiselect() {
        return selections;
    }

    /**
     * Add a query root for the given archetype.
     *
     * @param type       the underlying archetype class
     * @param archetypes the archetypes. May contain wildcards
     * @return the new root
     */
    @Override
    public <X extends IMObject> Root<X> from(Class<X> type, String... archetypes) {
        RootImpl<X> root = new RootImpl<>(context.getType(type, archetypes), context);
        roots.add(root);
        return root;
    }

    /**
     * Returns the roots.
     *
     * @return the roots
     */
    public List<RootImpl<? extends IMObject>> getRoots() {
        return roots;
    }

    /**
     * Create a subquery of the query.
     *
     * @param type the subquery result type
     * @return a new subquery
     */
    @Override
    public <U> Subquery<U> subquery(Class<U> type) {
        SubqueryImpl<U> subquery = new SubqueryImpl<>(type, context);
        subqueries.add(subquery);
        return subquery;
    }

    /**
     * Returns the subqueries.
     *
     * @return the subqueries
     */
    public List<SubqueryImpl<?>> getSubqueries() {
        return subqueries;
    }

    /**
     * Sets the where expression.
     *
     * @param expression the where expression. Any existing expression will be replaced
     * @return this query
     */
    @Override
    public CriteriaQuery<T> where(Expression<Boolean> expression) {
        where = expression;
        return this;
    }

    /**
     * Sets the where expression to a list of predicates that will be ANDed together.
     *
     * @param predicates the predicates to form the where expression. Any existing expression will be replaced
     * @return this query
     */
    @Override
    public CriteriaQuery<T> where(Predicate... predicates) {
        if (predicates.length == 1) {
            where = predicates[0];
        } else {
            where(Arrays.asList(predicates));
        }
        return this;
    }

    /**
     * Sets the where expression to a list of predicates that will be ANDed together.
     *
     * @param predicates the predicates to form the where expression. Any existing expression will be replaced
     * @return this query
     */
    @Override
    public CriteriaQuery<T> where(List<Predicate> predicates) {
        where = new PredicateImpl(context, new ArrayList<>(predicates), Predicate.BooleanOperator.AND);
        return this;
    }

    /**
     * Returns the where expression.
     *
     * @return the where expression. May be {@code null}
     */
    public Expression<Boolean> getWhere() {
        return where;
    }

    /**
     * Specify the expressions that are used to form groups over the query results.
     *
     * @param grouping the grouping expressions. Any existing expression will be replaced
     * @return this query
     */
    @Override
    public CriteriaQuery<T> groupBy(Expression<?>... grouping) {
        return groupBy(Arrays.asList(grouping));
    }

    /**
     * Specify the expressions that are used to form groups over the query results.
     *
     * @param grouping the grouping expressions. Any existing expression will be replaced
     * @return this query
     */
    @Override
    public CriteriaQuery<T> groupBy(List<Expression<?>> grouping) {
        groupBy = grouping;
        return this;
    }

    /**
     * Returns the group by clause.
     *
     * @return the group by clause. May be {@code null}
     */
    public List<Expression<?>> getGroupBy() {
        return groupBy;
    }

    /**
     * Specify a restriction over the groups of the query.
     *
     * @param expression the expression. Any existing expression will be replaced
     * @return this query
     */
    @Override
    public CriteriaQuery<T> having(Expression<Boolean> expression) {
        having = expression;
        return this;
    }

    /**
     * Specify restrictions over the groups of the query.
     *
     * @param predicates the predicates. Any existing predicate will be replaced
     * @return this query
     */
    @Override
    public CriteriaQuery<T> having(Predicate... predicates) {
        if (predicates.length == 1) {
            having = predicates[0];
        } else {
            having = new PredicateImpl(context, Arrays.asList(predicates), Predicate.BooleanOperator.AND);
        }
        return this;
    }

    /**
     * Returns the having clause.
     *
     * @return the having clause. May be {@code null}
     */
    public Expression<Boolean> getHaving() {
        return having;
    }

    /**
     * Determines the ordering of results.
     *
     * @param order the criteria to order by
     * @return this query
     */
    @Override
    public CriteriaQuery<T> orderBy(Order... order) {
        orderBy = Arrays.asList(order);
        return this;
    }

    /**
     * Returns the order by clause.
     *
     * @return the order by clause. May be {@code null}
     */
    public List<Order> getOrderBy() {
        return orderBy;
    }

}
