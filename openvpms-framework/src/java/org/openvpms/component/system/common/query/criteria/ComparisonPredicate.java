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
import java.util.Collections;

/**
 * Comparison predicate.
 *
 * @author Tim Anderson
 */
public class ComparisonPredicate extends PredicateImpl {

    public enum Operator {
        EQ,
        NE,
        GT,
        GTE,
        LT,
        LTE,
        LIKE
    }

    /**
     * The left hand side of the comparison.
     */
    private final Expression<?> lhs;

    /**
     * The right hand side of the comparison.
     */
    private final Object rhs;

    /**
     * The comparison operator.
     */
    private final Operator operator;

    /**
     * Constructs a {@link ComparisonPredicate}.
     *
     * @param context  the context.
     * @param lhs      the left hand side of the comparison
     * @param rhs      the right hand side of the comparison
     * @param operator the comparison operator
     */
    public ComparisonPredicate(Context context, Expression<?> lhs, Expression<?> rhs, Operator operator) {
        this(context, lhs, (Object) rhs, operator);
    }

    /**
     * Constructs a {@link ComparisonPredicate}.
     *
     * @param context  the context.
     * @param lhs      the left hand side of the comparison
     * @param rhs      the right hand side of the comparison
     * @param operator the comparison operator
     */
    public ComparisonPredicate(Context context, Expression<?> lhs, Object rhs, Operator operator) {
        super(context, Collections.emptyList(), BooleanOperator.AND);
        this.lhs = lhs;
        this.rhs = rhs;
        this.operator = operator;
    }

    /**
     * Returns the left hand side of the expression.
     *
     * @return the left hand side of the expression
     */
    public Expression<?> getLHS() {
        return lhs;
    }

    /**
     * Returns the right hand side of the expression.
     *
     * @return the right hand side of the expression
     */
    public Object getRHS() {
        return rhs;
    }

    /**
     * Returns the comparison operator.
     *
     * @return the comparison operator
     */
    public Operator getComparisonOperator() {
        return operator;
    }
}
