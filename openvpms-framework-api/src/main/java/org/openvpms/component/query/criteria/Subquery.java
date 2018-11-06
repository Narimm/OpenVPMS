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
import javax.persistence.criteria.Predicate;
import java.util.List;

/**
 * Represents a subquery.
 *
 * @author Tim Anderson
 */
public interface Subquery<T> extends AbstractQuery<T>, Expression<T> {

    /**
     * Specifies the item to be returned in the query results.
     *
     * @param expression the expression. Any existing expression will be replaced
     * @return this query
     */
    Subquery<T> select(Expression<T> expression);

    /**
     * Sets the where expression.
     *
     * @param expression the where expression. Any existing expression will be replaced
     * @return this query
     */
    Subquery<T> where(Expression<Boolean> expression);

    /**
     * Sets the where expression to a list of predicates that will be ANDed together.
     *
     * @param predicates the predicates to form the where expression. Any existing expression will be replaced
     * @return this query
     */
    Subquery<T> where(Predicate... predicates);

    /**
     * Sets the where expression to a list of predicates that will be ANDed together.
     *
     * @param predicates the predicates to form the where expression. Any existing expression will be replaced
     * @return this query
     */
    Subquery<T> where(List<Predicate> predicates);

    /**
     * Determines if duplicate results should be excluded.
     * <p>
     * By default, duplicates are included.
     *
     * @param distinct if {@code true}, return duplicate results, otherwise include them
     * @return this query
     */
    Subquery<T> distinct(boolean distinct);

}
