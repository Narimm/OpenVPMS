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

package org.openvpms.component.query.criteria;

import org.openvpms.component.model.object.IMObject;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

/**
 * Represents a join to an archetype.
 *
 * @author Tim Anderson
 */
public interface Join<Z extends IMObject, X extends IMObject> extends From<Z, X> {

    /**
     * Restricts the join on the specified expression.
     *
     * @param expression the expression
     * @return this join
     */
    Join<Z, X> on(Expression<Boolean> expression);

    /**
     * Restricts the join on the specified predicates.
     *
     * @param predicates the predicates
     * @return this join
     */
    Join<Z, X> on(Predicate... predicates);

    /**
     * Sets the join alias.
     *
     * @param alias the alias
     * @return this join
     */
    @Override
    Join<Z, X> alias(String alias);
}
