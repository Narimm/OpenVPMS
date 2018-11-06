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

import org.openvpms.component.business.service.archetype.descriptor.cache.IArchetypeDescriptorCache;
import org.openvpms.component.query.criteria.CriteriaBuilder;
import org.openvpms.component.query.criteria.CriteriaQuery;
import org.openvpms.component.query.criteria.Subquery;

import javax.persistence.Tuple;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import java.util.Arrays;

/**
 * Default implementation of {@link CriteriaBuilder}.
 *
 * @author Tim Anderson
 */
public class CriteriaBuilderImpl implements CriteriaBuilder {

    /**
     * The context.
     */
    private final Context context;

    /**
     * Constructs a {@link CriteriaBuilderImpl}.
     *
     * @param cache the archetype descriptor cache
     */
    public CriteriaBuilderImpl(IArchetypeDescriptorCache cache) {
        context = new Context(cache);
    }

    /**
     * Creates a new {@link CriteriaQuery}.
     *
     * @param type the type of the objects to query
     * @return a new query
     */
    @Override
    public <T> CriteriaQuery<T> createQuery(Class<T> type) {
        return new CriteriaQueryImpl<>(type, context);
    }

    /**
     * Creates a {@link CriteriaQuery} that returns {@link Tuple} results.
     *
     * @return a new query
     */
    @Override
    public CriteriaQuery<Tuple> createTupleQuery() {
        return new CriteriaQueryImpl<>(Tuple.class, context);
    }

    /**
     * Creates a count expression.
     *
     * @param expression expression the expression to count
     * @return count expression
     */
    @Override
    public Expression<Long> count(Expression<?> expression) {
        return new AggregateExpression<>(Type.LONG, context, AggregateExpression.Function.COUNT, expression);
    }

    /**
     * Creates a count distinct expression.
     *
     * @param expression expression the expression to count
     * @return count distinct expression
     */
    @Override
    public Expression<Long> countDistinct(Expression<?> expression) {
        return new AggregateExpression<>(Type.LONG, context, AggregateExpression.Function.COUNT_DISTINCT, expression);
    }

    /**
     * Create a predicate testing the existence of a subquery result.
     *
     * @param subquery subquery whose result is to be tested
     * @return an exists predicate
     */
    @Override
    public Predicate exists(Subquery<?> subquery) {
        return new ExistsPredicate(context, subquery);
    }

    /**
     * Creates a predicate that evaluates {@code true} if both arguments evaluate true.
     *
     * @param x a boolean expression
     * @param y a boolean expression
     * @return a new 'and' predicate
     */
    @Override
    public Predicate and(Expression<Boolean> x, Expression<Boolean> y) {
        return new PredicateImpl(context, Arrays.asList(x, y), Predicate.BooleanOperator.AND);
    }

    /**
     * Creates a predicate that evaluates {@code true} if all arguments evaluate true.
     *
     * @param predicates the predicates. If zero are supplied, the predicate always evaluates {@code true}
     * @return a new 'and' predicate
     */
    @Override
    public Predicate and(Predicate... predicates) {
        return new PredicateImpl(context, Arrays.asList(predicates), Predicate.BooleanOperator.AND);
    }

    /**
     * Creates a predicate that evaluates {@code true} if one argument evaluates true.
     *
     * @param x a boolean expression
     * @param y a boolean expression
     * @return a new 'or' predicate
     */
    @Override
    public Predicate or(Expression<Boolean> x, Expression<Boolean> y) {
        return new PredicateImpl(context, Arrays.asList(x, y), Predicate.BooleanOperator.OR);
    }

    /**
     * Creates a predicate that evaluates {@code true} if one argument evaluates true.
     *
     * @param predicates the predicates. If zero are supplied, the predicate always evaluates {@code false}
     * @return a new 'or' predicate
     */
    @Override
    public Predicate or(Predicate... predicates) {
        return new PredicateImpl(context, Arrays.asList(predicates), Predicate.BooleanOperator.OR);
    }

    /**
     * Creates a predicate to test if an expression is null.
     *
     * @param x expression
     * @return a new is-null predicate
     */
    public Predicate isNull(Expression<?> x) {
        return new NullPredicate(context, x);
    }

    /**
     * Creates a predicate to test if an expression is not null.
     *
     * @param x expression
     * @return a new is-not-null predicate
     */
    public Predicate isNotNull(Expression<?> x) {
        return new NullPredicate(context, x, true);
    }

    /**
     * Creates a predicate that tests the two arguments for equality.
     *
     * @param x an expression
     * @param y an expression
     * @return a new predicate to test if x is equal to y
     */
    @Override
    public Predicate equal(Expression<?> x, Expression<?> y) {
        return new ComparisonPredicate(context, x, y, ComparisonPredicate.Operator.EQ);
    }

    /**
     * Creates a predicate that tests the two arguments for equality.
     *
     * @param x an expression
     * @param y an object
     * @return a new predicate to test if x is equal to y
     */
    @Override
    public Predicate equal(Expression<?> x, Object y) {
        return new ComparisonPredicate(context, x, y, ComparisonPredicate.Operator.EQ);
    }

    /**
     * Creates a predicate that tests the two arguments for inequality.
     *
     * @param x an expression
     * @param y an expression
     * @return a new predicate to test if x is not equal to y
     */
    @Override
    public Predicate notEqual(Expression<?> x, Expression<?> y) {
        return new ComparisonPredicate(context, x, y, ComparisonPredicate.Operator.NE);
    }

    /**
     * Creates a predicate that tests the two arguments for inequality.
     *
     * @param x an expression
     * @param y an object
     * @return a new predicate to test if x is not equal to y
     */
    @Override
    public Predicate notEqual(Expression<?> x, Object y) {
        return new ComparisonPredicate(context, x, y, ComparisonPredicate.Operator.NE);
    }

    /**
     * Creates a predicate that tests if one argument is greater than another.
     *
     * @param x an expression
     * @param y an expression
     * @return a new predicate to test if x is greater than y
     */
    @Override
    public <Y extends Comparable<? super Y>> Predicate greaterThan(Expression<? extends Y> x,
                                                                   Expression<? extends Y> y) {
        return new ComparisonPredicate(context, x, y, ComparisonPredicate.Operator.GT);
    }

    /**
     * Creates a predicate that tests if one argument is greater than another.
     *
     * @param x an expression
     * @param y an object
     * @return a new predicate to test if x is greater than y
     */
    @Override
    public <Y extends Comparable<? super Y>> Predicate greaterThan(Expression<? extends Y> x, Y y) {
        return new ComparisonPredicate(context, x, y, ComparisonPredicate.Operator.GT);
    }

    /**
     * Creates a predicate that tests if one argument is greater than or equal to another.
     *
     * @param x an expression
     * @param y an expression
     * @return a new predicate to test if x is greater than or equal to y
     */
    @Override
    public <Y extends Comparable<? super Y>> Predicate greaterThanOrEqualTo(Expression<? extends Y> x,
                                                                            Expression<? extends Y> y) {
        return new ComparisonPredicate(context, x, y, ComparisonPredicate.Operator.GTE);
    }

    /**
     * Creates a predicate that tests if one argument is greater than or equal to another.
     *
     * @param x an expression
     * @param y an object
     * @return a new predicate to test if x is greater than or equal to y
     */
    @Override
    public <Y extends Comparable<? super Y>> Predicate greaterThanOrEqualTo(Expression<? extends Y> x, Y y) {
        return new ComparisonPredicate(context, x, y, ComparisonPredicate.Operator.GTE);
    }

    /**
     * Creates a predicate that tests if one argument is less than another.
     *
     * @param x an expression
     * @param y an expression
     * @return a new predicate to test if x is less than y
     */
    @Override
    public <Y extends Comparable<? super Y>> Predicate lessThan(Expression<? extends Y> x, Expression<? extends Y> y) {
        return new ComparisonPredicate(context, x, y, ComparisonPredicate.Operator.LT);
    }

    /**
     * Creates a predicate that tests if one argument is less than another.
     *
     * @param x an expression
     * @param y an object
     * @return a new predicate to test if x is less than y
     */
    @Override
    public <Y extends Comparable<? super Y>> Predicate lessThan(Expression<? extends Y> x, Y y) {
        return new ComparisonPredicate(context, x, y, ComparisonPredicate.Operator.LT);
    }

    /**
     * Creates a predicate that tests if one argument is less than or equal to another.
     *
     * @param x an expression
     * @param y an expression
     * @return a new predicate to test if x is less than or equal to y
     */
    @Override
    public <Y extends Comparable<? super Y>> Predicate lessThanOrEqualTo(Expression<? extends Y> x,
                                                                         Expression<? extends Y> y) {
        return new ComparisonPredicate(context, x, y, ComparisonPredicate.Operator.LTE);
    }

    /**
     * Creates a predicate that tests if one argument is less than or equal to another.
     *
     * @param x an expression
     * @param y an object
     * @return a new predicate to test if x is less than or equal to y
     */
    @Override
    public <Y extends Comparable<? super Y>> Predicate lessThanOrEqualTo(Expression<? extends Y> x, Y y) {
        return new ComparisonPredicate(context, x, y, ComparisonPredicate.Operator.LTE);
    }

    /**
     * Create a predicate for testing whether the value is between the lower and upper bounds.
     *
     * @param value the value expression
     * @param lower the lower bound expression, inclusive
     * @param upper the upper bound expression, inclusive
     * @return a new predicate to test if the value is between the lower and upper bound, inclusive
     */
    @Override
    public <Y extends Comparable<? super Y>> Predicate between(Expression<? extends Y> value,
                                                               Expression<? extends Y> lower,
                                                               Expression<? extends Y> upper) {
        return new BetweenPredicate(context, value, lower, upper);
    }

    /**
     * Create a predicate for testing whether the value is between the lower and upper bounds.
     *
     * @param value the value expression
     * @param lower the lower bound, inclusive
     * @param upper the upper bound, inclusive
     * @return a new predicate to test if the value is between the lower and upper bound, inclusive
     */
    @Override
    public <Y extends Comparable<? super Y>> Predicate between(Expression<? extends Y> value, Y lower, Y upper) {
        return new BetweenPredicate(context, value, lower, upper);
    }

    /**
     * Creates a predicate that tests if an expression matches a pattern.
     *
     * @param x       an expression
     * @param pattern the pattern
     * @return a new 'like' predicate
     */
    @Override
    public Predicate like(Expression<String> x, Expression<String> pattern) {
        return new ComparisonPredicate(context, x, pattern, ComparisonPredicate.Operator.LIKE);
    }

    /**
     * Creates a predicate that tests if an expression matches a pattern.
     *
     * @param x       an expression
     * @param pattern the pattern
     * @return a new 'like' predicate
     */
    @Override
    public Predicate like(Expression<String> x, String pattern) {
        return new ComparisonPredicate(context, x, pattern, ComparisonPredicate.Operator.LIKE);
    }

    /**
     * Orders an expression on ascending value.
     *
     * @param expression the expression
     * @return a new order clause
     */
    @Override
    public Order asc(Expression<?> expression) {
        return new OrderImpl(true, expression);
    }

    /**
     * Orders an expression on descending value.
     *
     * @param expression the expression
     * @return a new order clause
     */
    @Override
    public Order desc(Expression<?> expression) {
        return new OrderImpl(false, expression);
    }

    /**
     * Creates an aggregate expression applying the sum operation.
     *
     * @param expression the expression to sum
     * @return a new sum expression
     */
    @Override
    public <N extends Number> Expression<N> sum(Expression<?> expression) {
        return aggregate(AggregateExpression.Function.SUM, expression);
    }

    /**
     * Create an aggregate expression applying the numerical max operation.
     *
     * @param expression the expression to supply to the max operation
     * @return a new max expression
     */
    @Override
    public <N extends Number> Expression<N> max(Expression<?> expression) {
        return aggregate(AggregateExpression.Function.MAX, expression);
    }

    /**
     * Creates an aggregate expression applying the numerical min operation.
     *
     * @param expression the expression to supply to the max operation
     * @return a new min expression
     */
    @Override
    public <N extends Number> Expression<N> min(Expression<?> expression) {
        return aggregate(AggregateExpression.Function.MIN, expression);
    }

    /**
     * Creates an aggregate expression for finding the greatest of the values (strings, dates, etc).
     *
     * @param expression the expression to supply to the greatest operation
     * @return a new greatest expression
     */
    @Override
    public <X extends Comparable<? super X>> Expression<X> greatest(Expression<?> expression) {
        return aggregate(AggregateExpression.Function.GREATEST, expression);
    }

    /**
     * Creates an aggregate expression for finding the least of the values (strings, dates, etc).
     *
     * @param expression the expression to supply to the least operation
     * @return a new least expression
     */
    @Override
    public <X extends Comparable<? super X>> Expression<X> least(Expression<?> expression) {
        return aggregate(AggregateExpression.Function.LEAST, expression);
    }

    /**
     * Creates an aggregate expression.
     *
     * @param function   the function
     * @param expression the expression to apply the function to
     * @return a new aggregate expression
     */
    @SuppressWarnings("unchecked")
    private <N> AggregateExpression<N> aggregate(AggregateExpression.Function function, Expression<?> expression) {
        Type type = ((ExpressionImpl) expression).getType();
        return new AggregateExpression<>(type, context, function, expression);
    }
}
