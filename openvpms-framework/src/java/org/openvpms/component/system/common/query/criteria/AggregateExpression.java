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

/**
 * Aggregate expression.
 *
 * @author Tim Anderson
 */
public class AggregateExpression<T> extends ExpressionImpl<T> {

    public enum Function {
        COUNT,
        COUNT_DISTINCT,
        SUM,
        MAX,
        MIN,
        GREATEST,
        LEAST
    }

    /**
     * The expression to apply the function to.
     */
    private final Expression<?> expression;

    /**
     * The function.
     */
    private final Function function;

    /**
     * Constructs an {@link AggregateExpression}.
     *
     * @param type       the expression type
     * @param context    the context
     * @param function   the function to apply to the expression
     * @param expression the expression
     */
    public AggregateExpression(Type<T> type, Context context, Function function, Expression<?> expression) {
        super(type, context);
        this.function = function;
        this.expression = expression;
    }

    /**
     * Returns the function.
     *
     * @return the function
     */
    public Function getFunction() {
        return function;
    }

    /**
     * Returns the expression.
     *
     * @return the expression
     */
    public Expression<?> getExpression() {
        return expression;
    }
}
