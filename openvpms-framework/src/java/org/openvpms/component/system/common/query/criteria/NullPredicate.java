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
 * Predicate to test an expression for null.
 *
 * @author Tim Anderson
 */
public class NullPredicate extends SimplePredicate {

    /**
     * Determines if the test is negated (i.e. not-null).
     */
    private final boolean negated;

    /**
     * Constructs a {@link NullPredicate}.
     *
     * @param context    the context
     * @param expression the expression to test for null
     */
    public NullPredicate(Context context, Expression<?> expression) {
        this(context, expression, false);
    }

    /**
     * Constructs a {@link NullPredicate}.
     *
     * @param context    the context
     * @param expression the expression to test for null
     * @param negated    if {@code true}, test for not-null
     */
    public NullPredicate(Context context, Expression<?> expression, boolean negated) {
        super(context, expression);
        this.negated = negated;
    }

    /**
     * Determines if the expression is being tested for 'null' or 'not-null'.
     *
     * @return {@code false} if it is being tested for 'null', {@code true} if it is being tested for 'not-null'
     */
    public boolean isNegated() {
        return negated;
    }
}
