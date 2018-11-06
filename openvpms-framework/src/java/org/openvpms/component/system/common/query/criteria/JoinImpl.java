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
import org.openvpms.component.query.criteria.Join;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import java.util.Arrays;


/**
 * Default implementation of {@link Join}.
 *
 * @author Tim Anderson
 */
public class JoinImpl<Z extends IMObject, X extends IMObject> extends FromImpl<Z, X> implements Join<Z, X> {

    public enum JoinType {
        INNER,
        LEFT
    }

    /**
     * The join type.
     */
    private final JoinType joinType;

    /**
     * The 'on' expression.
     */
    private Expression<Boolean> expression;

    /**
     * Constructs a {@link JoinImpl}.
     *
     * @param parent   the parent path
     * @param context  the context
     * @param joinType the join type
     */
    public JoinImpl(Type<X> type, PathImpl<?> parent, Context context, JoinType joinType) {
        super(type, parent, context);
        this.joinType = joinType;
    }

    /**
     * Returns the join type.
     *
     * @return the join type
     */
    public JoinType getJoinType() {
        return joinType;
    }

    /**
     * Restricts the join on the specified expression.
     *
     * @param expression the expression
     * @return this join
     */
    @Override
    public Join<Z, X> on(Expression<Boolean> expression) {
        this.expression = expression;
        return this;
    }

    /**
     * Restricts the join on the specified predicates.
     *
     * @param predicates the predicates
     * @return this join
     */
    @Override
    public Join<Z, X> on(Predicate... predicates) {
        expression = new PredicateImpl(getContext(), Arrays.asList(predicates), Predicate.BooleanOperator.AND);
        return this;
    }

    /**
     * Sets the selection alias.
     *
     * @param alias the alias
     * @return the selection
     */
    @Override
    public Join<Z, X> alias(String alias) {
        super.alias(alias);
        return this;
    }

    /**
     * Returns the expression to join on.
     *
     * @return the expression to join on. May be {@code null}
     */
    public Expression<Boolean> getExpression() {
        return expression;
    }
}
