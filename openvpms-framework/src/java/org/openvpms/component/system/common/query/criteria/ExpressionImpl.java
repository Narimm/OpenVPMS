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
import java.util.Collection;
import java.util.Set;

/**
 * Default implementation of {@link Expression}.
 *
 * @author Tim Anderson
 */
public class ExpressionImpl<T> extends SelectionImpl<T> implements Expression<T> {

    /**
     * The context.
     */
    private final Context context;

    /**
     * Constructs an {@link ExpressionImpl}.
     *
     * @param type    the type of the expression
     * @param context the context
     */
    public ExpressionImpl(Type<T> type, Context context) {
        super(type);
        this.context = context;
    }

    /**
     * Create a predicate to test whether the expression is null.
     *
     * @return a new predicate testing whether the expression is null
     */
    @Override
    public Predicate isNull() {
        return new NullPredicate(context, this, false);
    }

    /**
     * Create a predicate to test whether the expression is not null.
     *
     * @return a new predicate testing whether the expression is not null
     */
    @Override
    public Predicate isNotNull() {
        return new NullPredicate(context, this, true);
    }

    /**
     * Create a predicate to test whether the value of the expression is in a list of values.
     *
     * @param values values
     * @return a new 'in' predicate
     */
    @Override
    public Predicate in(Object... values) {
        return new InPredicate(context, this, values);
    }

    /**
     * Create a predicate to test whether the expression is a member of the argument list.
     *
     * @param values expressions to be tested against
     * @return predicate testing for membership
     */
    @Override
    public Predicate in(Expression<?>... values) {
        return null;
    }

    /**
     * Create a predicate to test whether the expression is a member
     * of the collection.
     *
     * @param values collection of values to be tested against
     * @return predicate testing for membership
     */
    @Override
    public Predicate in(Collection<?> values) {
        return new InPredicate(context, this, values.toArray());
    }

    /**
     * Create a predicate to test whether the expression is a member
     * of the collection.
     *
     * @param values expression corresponding to collection to be
     *               tested against
     * @return predicate testing for membership
     */
    @Override
    public Predicate in(Expression<Collection<?>> values) {
        return null;
    }

    /**
     * Perform a typecast upon the expression, returning a new
     * expression object.
     * This method does not cause type conversion:
     * the runtime type is not changed.
     * Warning: may result in a runtime failure.
     *
     * @param type intended type of the expression
     * @return new expression of the given type
     */
    @Override
    public <X> Expression<X> as(Class<X> type) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the archetypes of this expression.
     *
     * @return the archetypes, or {@code null} if the type is not an archetype
     */
    public Set<String> getArchetypes() {
        return getType().getArchetypes();
    }

    /**
     * Returns the context.
     *
     * @return the context
     */
    public Context getContext() {
        return context;
    }

}
