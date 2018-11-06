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
 * A predicate that operates on a single expression.
 *
 * @author Tim Anderson
 */
public abstract class SimplePredicate extends PredicateImpl {

    /**
     * The expression.
     */
    private final Expression<?> expression;

    /**
     * Constructs a {@link SimplePredicate}.
     *
     * @param context    the context
     * @param expression the expression
     */
    public SimplePredicate(Context context, Expression<?> expression) {
        super(context, Collections.emptyList(), BooleanOperator.AND);
        this.expression = expression;
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
