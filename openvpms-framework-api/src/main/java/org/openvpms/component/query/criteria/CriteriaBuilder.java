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

package org.openvpms.component.query.criteria;

import javax.persistence.Tuple;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;

/**
 * Builder for queries, expressions and predicates.
 *
 * @author Tim Anderson
 */
public interface CriteriaBuilder {

    /**
     * Creates a new {@link CriteriaQuery}.
     *
     * @param type the type of the objects to query
     * @return a new query
     */
    <T> CriteriaQuery<T> createQuery(Class<T> type);

    /**
     * Creates a {@link CriteriaQuery} that returns {@link Tuple} results.
     *
     * @return a new query
     */
    CriteriaQuery<Tuple> createTupleQuery();

    /**
     * Creates a count expression.
     *
     * @param expression expression the expression to count
     * @return count expression
     */
    Expression<Long> count(Expression<?> expression);

    /**
     * Creates a count distinct expression.
     *
     * @param expression expression the expression to count
     * @return count distinct expression
     */
    Expression<Long> countDistinct(Expression<?> expression);

    /**
     * Create a predicate testing the existence of a subquery result.
     *
     * @param subquery subquery whose result is to be tested
     * @return an exists predicate
     */
    Predicate exists(Subquery<?> subquery);

    /**
     * Creates a predicate that evaluates {@code true} if both arguments evaluate true.
     *
     * @param x a boolean expression
     * @param y a boolean expression
     * @return a new 'and' predicate
     */
    Predicate and(Expression<Boolean> x, Expression<Boolean> y);

    /**
     * Creates a predicate that evaluates {@code true} if all arguments evaluate true.
     *
     * @param predicates the predicates. If zero are supplied, the predicate always evaluates {@code true}
     * @return a new 'and' predicate
     */
    Predicate and(Predicate... predicates);

    /**
     * Creates a predicate that evaluates {@code true} if one argument evaluates true.
     *
     * @param x a boolean expression
     * @param y a boolean expression
     * @return a new 'or' predicate
     */
    Predicate or(Expression<Boolean> x, Expression<Boolean> y);

    /**
     * Creates a predicate that evaluates {@code true} if one argument evaluates true.
     *
     * @param predicates the predicates. If zero are supplied, the predicate always evaluates {@code false}
     * @return a new 'or' predicate
     */
    Predicate or(Predicate... predicates);

    /**
     * Creates a predicate to test if an expression is null.
     *
     * @param x expression
     * @return a new is-null predicate
     */
    Predicate isNull(Expression<?> x);

    /**
     * Creates a predicate to test if an expression is not null.
     *
     * @param x expression
     * @return a new is-not-null predicate
     */
    Predicate isNotNull(Expression<?> x);

    /**
     * Creates a predicate that tests the two arguments for equality.
     *
     * @param x an expression
     * @param y an expression
     * @return a new predicate to test if x is equal to y
     */
    Predicate equal(Expression<?> x, Expression<?> y);

    /**
     * Creates a predicate that tests the two arguments for equality.
     *
     * @param x an expression
     * @param y an object
     * @return a new predicate to test if x is equal to y
     */
    Predicate equal(Expression<?> x, Object y);

    /**
     * Creates a predicate that tests the two arguments for inequality.
     *
     * @param x an expression
     * @param y an expression
     * @return a new predicate to test if x is not equal to y
     */
    Predicate notEqual(Expression<?> x, Expression<?> y);

    /**
     * Creates a predicate that tests the two arguments for inequality.
     *
     * @param x an expression
     * @param y an object
     * @return a new predicate to test if x is not equal to y
     */
    Predicate notEqual(Expression<?> x, Object y);

    /**
     * Creates a predicate that tests if one argument is greater than another.
     *
     * @param x an expression
     * @param y an expression
     * @return a new predicate to test if x is greater than y
     */
    <Y extends Comparable<? super Y>> Predicate greaterThan(Expression<? extends Y> x, Expression<? extends Y> y);

    /**
     * Creates a predicate that tests if one argument is greater than another.
     *
     * @param x an expression
     * @param y an object
     * @return a new predicate to test if x is greater than y
     */
    <Y extends Comparable<? super Y>> Predicate greaterThan(Expression<? extends Y> x, Y y);

    /**
     * Creates a predicate that tests if one argument is greater than or equal to another.
     *
     * @param x an expression
     * @param y an expression
     * @return a new predicate to test if x is greater than or equal to y
     */
    <Y extends Comparable<? super Y>> Predicate greaterThanOrEqualTo(Expression<? extends Y> x,
                                                                     Expression<? extends Y> y);

    /**
     * Creates a predicate that tests if one argument is greater than or equal to another.
     *
     * @param x an expression
     * @param y an object
     * @return a new predicate to test if x is greater than or equal to y
     */
    <Y extends Comparable<? super Y>> Predicate greaterThanOrEqualTo(Expression<? extends Y> x, Y y);

    /**
     * Creates a predicate that tests if one argument is less than another.
     *
     * @param x an expression
     * @param y an expression
     * @return a new predicate to test if x is less than y
     */
    <Y extends Comparable<? super Y>> Predicate lessThan(Expression<? extends Y> x, Expression<? extends Y> y);

    /**
     * Creates a predicate that tests if one argument is less than another.
     *
     * @param x an expression
     * @param y an object
     * @return a new predicate to test if x is less than y
     */
    <Y extends Comparable<? super Y>> Predicate lessThan(Expression<? extends Y> x, Y y);

    /**
     * Creates a predicate that tests if one argument is less than or equal to another.
     *
     * @param x an expression
     * @param y an expression
     * @return a new predicate to test if x is less than or equal to y
     */
    <Y extends Comparable<? super Y>> Predicate lessThanOrEqualTo(Expression<? extends Y> x,
                                                                  Expression<? extends Y> y);

    /**
     * Creates a predicate that tests if one argument is less than or equal to another.
     *
     * @param x an expression
     * @param y an object
     * @return a new predicate to test if x is less than or equal to y
     */
    <Y extends Comparable<? super Y>> Predicate lessThanOrEqualTo(Expression<? extends Y> x, Y y);

    /**
     * Create a predicate for testing whether the value is between the lower and upper bounds.
     *
     * @param value the value expression
     * @param lower the lower bound expression, inclusive
     * @param upper the upper bound expression, inclusive
     * @return a new predicate to test if the value is between the lower and upper bound, inclusive
     */
    <Y extends Comparable<? super Y>> Predicate between(Expression<? extends Y> value, Expression<? extends Y> lower,
                                                        Expression<? extends Y> upper);

    /**
     * Create a predicate for testing whether the value is between the lower and upper bounds.
     *
     * @param value the value expression
     * @param lower the lower bound, inclusive
     * @param upper the upper bound, inclusive
     * @return a new predicate to test if the value is between the lower and upper bound, inclusive
     */
    <Y extends Comparable<? super Y>> Predicate between(Expression<? extends Y> value, Y lower, Y upper);

    /**
     * Creates a predicate that tests if an expression matches a pattern.
     *
     * @param x       an expression
     * @param pattern the pattern
     * @return a new 'like' predicate
     */
    Predicate like(Expression<String> x, Expression<String> pattern);

    /**
     * Creates a predicate that tests if an expression matches a pattern.
     *
     * @param x       an expression
     * @param pattern the pattern
     * @return a new 'like' predicate
     */
    Predicate like(Expression<String> x, String pattern);

    /**
     * Orders an expression on ascending value.
     *
     * @param expression the expression
     * @return a new order clause
     */
    Order asc(Expression<?> expression);

    /**
     * Orders an expression on descending value.
     *
     * @param expression the expression
     * @return a new order clause
     */
    Order desc(Expression<?> expression);

    /**
     * Creates an aggregate expression applying the sum operation.
     *
     * @param expression the expression to sum
     * @return a new sum expression
     */
    <N extends Number> Expression<N> sum(Expression<?> expression);

    /**
     * Create an aggregate expression applying the numerical max operation.
     *
     * @param expression the expression to supply to the max operation
     * @return a new max expression
     */
    <N extends Number> Expression<N> max(Expression<?> expression);

    /**
     * Creates an aggregate expression applying the numerical min operation.
     *
     * @param expression the expression to supply to the max operation
     * @return a new min expression
     */
    <N extends Number> Expression<N> min(Expression<?> expression);

    /**
     * Creates an aggregate expression for finding the greatest of the values (strings, dates, etc).
     *
     * @param expression the expression to supply to the greatest operation
     * @return a new greatest expression
     */
    <X extends Comparable<? super X>> Expression<X> greatest(Expression<?> expression);

    /**
     * Creates an aggregate expression for finding the least of the values (strings, dates, etc).
     *
     * @param expression the expression to supply to the least operation
     * @return a new least expression
     */
    <X extends Comparable<? super X>> Expression<X> least(Expression<?> expression);

}
