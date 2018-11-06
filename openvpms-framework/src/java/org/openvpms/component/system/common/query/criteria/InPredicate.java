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
import java.util.List;

/**
 * Determines if the value of an expression is in a range of values.
 *
 * @author Tim Anderson
 */
public class InPredicate extends SimplePredicate {

    /**
     * The values to test against.
     */
    private final Object[] values;

    /**
     * The expressions to test against.
     */
    private final List<Expression<?>> expressions;

    /**
     * Constructs an {@link InPredicate}.
     *
     * @param context    the context
     * @param expression the expression
     * @param values     the values to test against
     */
    public InPredicate(Context context, Expression<?> expression, Object[] values) {
        super(context, expression);
        this.values = values;
        this.expressions = null;
    }

    /**
     * Constructs an {@link InPredicate}.
     *
     * @param context     the context
     * @param expression  the expression
     * @param expressions the expressions to test against
     */
    public InPredicate(Context context, Expression<?> expression, List<Expression<?>> expressions) {
        super(context, expression);
        this.expressions = expressions;
        this.values = null;
    }

    /**
     * Returns the values to test the expression against.
     *
     * @return the values, or {@code null} if expressions are being used
     */
    public Object[] getValues() {
        return values;
    }

    /**
     * Returns the expression values to test against.
     *
     * @return the expressions, or {@code null} if values are being used
     */
    public List<Expression<?>> getExpressionValues() {
        return expressions;
    }
}
