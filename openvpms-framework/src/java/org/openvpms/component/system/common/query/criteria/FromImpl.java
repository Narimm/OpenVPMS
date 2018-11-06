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
import org.openvpms.component.query.criteria.From;
import org.openvpms.component.query.criteria.Join;

import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of {@link From}.
 *
 * @author Tim Anderson
 */
public class FromImpl<Z extends IMObject, X extends IMObject> extends PathImpl<X> implements From<Z, X> {

    /**
     * The joins on this instance.
     */
    private List<JoinImpl<X, ? extends IMObject>> joins = new ArrayList<>();

    /**
     * Constructs a {@link FromImpl}.
     *
     * @param type    the from type
     * @param parent  the parent. May be {@code null}
     * @param context the context
     */
    public FromImpl(Type<X> type, PathImpl<?> parent, Context context) {
        super(type, parent, context);
    }

    /**
     * Creates an inner join on an archetype node.
     *
     * @param name the archetype node name
     * @return a new join
     */
    @Override
    public <Y extends IMObject> Join<X, Y> join(String name) {
        return join(name, JoinImpl.JoinType.INNER);
    }

    /**
     * Creates an inner join on an archetype node, restricted to the specified archetype.
     *
     * @param name      the archetype node name
     * @param archetype the archetype. May contain wildcards
     * @return a new join
     */
    @Override
    public <Y extends IMObject> Join<X, Y> join(String name, String archetype) {
        return join(name, archetype, JoinImpl.JoinType.INNER);
    }

    /**
     * Creates a left join on an archetype node.
     *
     * @param name the archetype node name
     * @return a new join
     */
    @Override
    public <Y extends IMObject> Join<X, Y> leftJoin(String name) {
        return join(name, JoinImpl.JoinType.LEFT);
    }

    /**
     * Creates an inner join on an archetype node, restricted to the specified archetype.
     *
     * @param name      the archetype node name
     * @param archetype the archetype. May contain wildcards
     * @return a new join
     */
    @Override
    public <Y extends IMObject> Join<X, Y> leftJoin(String name, String archetype) {
        return join(name, archetype, JoinImpl.JoinType.LEFT);
    }

    /**
     * Returns the joins on this instance.
     *
     * @return the joins
     */
    public List<JoinImpl<X, ? extends IMObject>> getJoins() {
        return joins;
    }

    /**
     * Creates a join on an archetype node.
     *
     * @param name     the archetype node name
     * @param joinType the join type
     * @return a new join
     */
    private <Y extends IMObject> Join<X, Y> join(String name, JoinImpl.JoinType joinType) {
        Context context = getContext();
        Type<Y> type = context.getTypeForJoin(this.getType(), name);
        JoinImpl<X, Y> join = new JoinImpl<>(type, this, context, joinType);
        joins.add(join);
        return join;
    }

    /**
     * Creates an inner join on an archetype node, restricted to the specified archetype.
     *
     * @param name      the archetype node name
     * @param joinType  the join type
     * @param archetype the archetype. May contain wildcards
     * @return a new join
     */
    private <Y extends IMObject> Join<X, Y> join(String name, String archetype, JoinImpl.JoinType joinType) {
        Context context = getContext();
        Type<Y> type = context.getTypeForJoin(this.getType(), name, archetype);
        JoinImpl<X, Y> join = new JoinImpl<>(type, this, context, joinType);
        joins.add(join);
        return join;
    }

}
