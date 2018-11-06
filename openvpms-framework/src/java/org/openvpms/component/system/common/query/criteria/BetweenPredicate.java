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
 * Between predicate.
 *
 * @author Tim Anderson
 */
public class BetweenPredicate extends PredicateImpl {

    /**
     * The value expression.
     */
    private final Expression<?> value;

    /**
     * The lower value.
     */
    private final Object lower;

    /**
     * The upper value.
     */
    private final Object upper;

    /**
     * Constructs a {@link BetweenPredicate}.
     *
     * @param context the context
     * @param value the value expression
     * @param lower the lower bound expression
     * @param upper the upper bound expression
     */
    public BetweenPredicate(Context context, Expression<?> value, Expression<?> lower, Expression<?> upper) {
        this(context, value, (Object) lower, upper);
    }

    /**
     * Constructs a {@link BetweenPredicate}.
     *
     * @param context the context
     * @param value the value expression
     * @param lower the lower bound
     * @param upper the upper bound
     */
    public BetweenPredicate(Context context, Expression<?> value, Object lower, Object upper) {
        super(context, Collections.emptyList(), BooleanOperator.AND);
        this.value = value;
        this.lower = lower;
        this.upper = upper;
    }

    /**
     * Returns the value expression.
     *
     * @return the value expression
     */
    public Expression<?> getValue() {
        return value;
    }

    /**
     * Returns the lower bound.
     *
     * @return the lower bound
     */
    public Object getLowerBound() {
        return lower;
    }

    /**
     * Returns the upper bound.
     *
     * @return the upper bound
     */
    public Object getUpperBound() {
        return upper;
    }
}
