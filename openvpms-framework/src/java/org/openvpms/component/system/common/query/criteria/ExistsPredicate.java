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

import org.openvpms.component.query.criteria.Subquery;

import javax.persistence.criteria.Expression;
import java.util.Collections;

/**
 * A predicate to test the existence of the result of a subquery.
 *
 * @author Tim Anderson
 */
public class ExistsPredicate extends PredicateImpl {

    /**
     * The subquery.
     */
    private final Subquery<?> subquery;

    /**
     * Constructs an {@link ExistsPredicate}.
     *
     * @param context  the context
     * @param subquery the subquery
     */
    public ExistsPredicate(Context context, Subquery<?> subquery) {
        super(context, Collections.<Expression<Boolean>>emptyList(), BooleanOperator.AND);
        this.subquery = subquery;
    }

    /**
     * Returns the subquery.
     *
     * @return the subquery
     */
    public Subquery<?> getSubquery() {
        return subquery;
    }
}
