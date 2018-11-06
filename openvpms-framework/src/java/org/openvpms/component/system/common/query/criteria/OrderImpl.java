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
import javax.persistence.criteria.Order;

/**
 * Default implementation of {@link Order}.
 *
 * @author Tim Anderson
 */
public class OrderImpl implements Order {

    /**
     * Determines if the expression is ordered on ascending value, or descending value.
     */
    private final boolean ascending;

    /**
     * The expression.
     */
    private final Expression<?> expression;

    /**
     * Constructs an {@link OrderImpl}.
     *
     * @param ascending  determines if the expression is ordered on ascending value, or descending value
     * @param expression the expression
     */
    public OrderImpl(boolean ascending, Expression<?> expression) {
        this.ascending = ascending;
        this.expression = expression;
    }

    /**
     * Reverses the order.
     *
     * @return a new order with the reverse ordering
     */
    @Override
    public Order reverse() {
        return new OrderImpl(!ascending, expression);
    }

    /**
     * Determines if the expression is ordered on ascending or descending value.
     *
     * @return {@code true} if the expression is ordered on ascending value, {@code false} if ordered on descending
     * value
     */
    @Override
    public boolean isAscending() {
        return ascending;
    }

    /**
     * Returns the expression to order on.
     *
     * @return the expression to order on
     */
    @Override
    public Expression<?> getExpression() {
        return expression;
    }
}
