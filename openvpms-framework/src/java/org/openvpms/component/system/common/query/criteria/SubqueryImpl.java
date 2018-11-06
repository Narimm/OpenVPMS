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
import org.openvpms.component.query.criteria.Root;
import org.openvpms.component.query.criteria.Subquery;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Default implementation of {@link Subquery}.
 *
 * @author Tim Anderson
 */
public class SubqueryImpl<T> extends ExpressionImpl<T> implements Subquery<T> {

    /**
     * The selection.
     */
    private Expression<T> selection;

    /**
     * Determines if duplicate results are excluded.
     */
    private boolean distinct;

    /**
     * The query roots.
     */
    private List<RootImpl<? extends IMObject>> roots = new ArrayList<>();

    /**
     * The where clause.
     */
    private Expression<Boolean> where;

    /**
     * Constructs a {@link SubqueryImpl}.
     *
     * @param type    the result type
     * @param context the context
     */
    public SubqueryImpl(Class<T> type, Context context) {
        super(new Type<T>(type), context);
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
        Context context = getContext();
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
     * Specifies the item to be returned in the query results.
     *
     * @param expression the expression. Any existing expression will be replaced
     * @return this query
     */
    @Override
    public Subquery<T> select(Expression<T> expression) {
        selection = expression;
        return this;
    }

    /**
     * Returns the select expression.
     *
     * @return the select expression, or {@code null} if none is specified
     */
    public Expression<T> getSelect() {
        return selection;
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
    public Subquery<T> distinct(boolean distinct) {
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
     * Sets the where expression.
     *
     * @param expression the where expression. Any existing expression will be replaced
     * @return this query
     */
    @Override
    public Subquery<T> where(Expression<Boolean> expression) {
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
    public Subquery<T> where(Predicate... predicates) {
        return where(Arrays.asList(predicates));
    }

    /**
     * Sets the where expression to a list of predicates that will be ANDed together.
     *
     * @param predicates the predicates to form the where expression. Any existing expression will be replaced
     * @return this query
     */
    @Override
    public Subquery<T> where(List<Predicate> predicates) {
        where = new PredicateImpl(getContext(), new ArrayList<>(predicates), Predicate.BooleanOperator.AND);
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

}
