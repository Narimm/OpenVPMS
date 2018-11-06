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

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of {@link Predicate}.
 *
 * @author Tim Anderson
 */
public class PredicateImpl extends ExpressionImpl<Boolean> implements Predicate {

    /**
     * The expressions.
     */
    private final List<Expression<Boolean>> expressions;

    /**
     * The operator.
     */
    private final BooleanOperator operator;

    /**
     * Constructs a {@link PredicateImpl}.
     *
     * @param context     the context
     * @param expressions the expressions
     * @param operator    the operator
     */
    public PredicateImpl(Context context, List<Expression<Boolean>> expressions, BooleanOperator operator) {
        super(Type.BOOLEAN, context);
        this.expressions = new ArrayList<>(expressions);
        this.operator = operator;
    }

    /**
     * Return the top-level conjuncts or disjuncts of the predicate.
     * Returns empty list if there are no top-level conjuncts or
     * disjuncts of the predicate.
     * Modifications to the list do not affect the query.
     *
     * @return list of boolean expressions forming the predicate
     */
    @Override
    public List<Expression<Boolean>> getExpressions() {
        return expressions;
    }

    /**
     * Return the boolean operator for the predicate.
     * If the predicate is simple, this is <code>AND</code>.
     *
     * @return boolean operator for the predicate
     */
    @Override
    public BooleanOperator getOperator() {
        return operator;
    }

    /**
     * Whether the predicate has been created from another
     * predicate by applying the <code>Predicate.not()</code> method
     * or the <code>CriteriaBuilder.not()</code> method.
     *
     * @return boolean indicating if the predicate is a negated predicate
     */
    @Override
    public boolean isNegated() {
        return false;
    }

    /**
     * Create a negation of the predicate.
     *
     * @return negated predicate
     */
    @Override
    public Predicate not() {
        return new NotPredicate(getContext(), this);
    }
}
