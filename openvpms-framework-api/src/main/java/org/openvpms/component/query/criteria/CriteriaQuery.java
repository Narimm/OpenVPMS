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

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Selection;
import java.util.List;

/**
 * Representation of an archetype query.
 *
 * @param <T> the type of the result
 * @author Tim Anderson
 */
public interface CriteriaQuery<T> extends AbstractQuery<T> {

    /**
     * Specifies the item to be returned in the query results.
     *
     * @param selection the selection. Any existing selection will be replaced
     * @return this query
     */
    CriteriaQuery<T> select(Selection<? super T> selection);

    /**
     * Specifies the items to be returned in the query results.
     *
     * @param selections the selections. Any existing selection will be replaced
     * @return the modified query
     */
    CriteriaQuery<T> multiselect(Selection<?>... selections);

    /**
     * Determines if duplicate results should be excluded.
     * <p>
     * By default, duplicates are included.
     *
     * @param distinct if {@code true}, return duplicate results, otherwise include them
     * @return this query
     */
    CriteriaQuery<T> distinct(boolean distinct);

    /**
     * Sets the where expression.
     *
     * @param expression the where expression. Any existing expression will be replaced
     * @return this query
     */
    CriteriaQuery<T> where(Expression<Boolean> expression);

    /**
     * Sets the where expression to a list of predicates that will be ANDed together.
     *
     * @param predicates the predicates to form the where expression. Any existing expression will be replaced
     * @return this query
     */
    CriteriaQuery<T> where(Predicate... predicates);

    /**
     * Sets the where expression to a list of predicates that will be ANDed together.
     *
     * @param predicates the predicates to form the where expression. Any existing expression will be replaced
     * @return this query
     */
    CriteriaQuery<T> where(List<Predicate> predicates);

    /**
     * Specify the expressions that are used to form groups over the query results.
     *
     * @param grouping the grouping expressions. Any existing expression will be replaced
     * @return this query
     */
    CriteriaQuery<T> groupBy(Expression<?>... grouping);

    /**
     * Specify the expressions that are used to form groups over the query results.
     *
     * @param grouping the grouping expressions. Any existing expression will be replaced
     * @return this query
     */
    CriteriaQuery<T> groupBy(List<Expression<?>> grouping);

    /**
     * Specify a restriction over the groups of the query.
     *
     * @param expression the expression. Any existing expression will be replaced
     * @return this query
     */
    CriteriaQuery<T> having(Expression<Boolean> expression);

    /**
     * Specify restrictions over the groups of the query.
     *
     * @param predicates the predicates. Any existing predicate will be replaced
     * @return this query
     */
    CriteriaQuery<T> having(Predicate... predicates);

    /**
     * Determines the ordering of results.
     *
     * @param order the criteria to order by
     * @return this query
     */
    CriteriaQuery<T> orderBy(Order... order);

    /**
     * Returns the result type of the query.
     *
     * @return the result type of the query
     */
    Class<T> getResultType();

    /**
     * Create a subquery of the query.
     *
     * @param type the subquery result type
     * @return a new subquery
     */
    <U> Subquery<U> subquery(Class<U> type);

}
