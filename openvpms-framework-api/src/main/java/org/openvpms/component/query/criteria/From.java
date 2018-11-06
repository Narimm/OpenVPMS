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

/**
 * Represents an entity that can appear in a from clause.
 *
 * @author Tim Anderson
 */
public interface From<Z extends IMObject, X extends IMObject> extends Path<X> {

    /**
     * Creates an inner join on an archetype node.
     *
     * @param name the archetype node name
     * @return a new join
     */
    <Y extends IMObject> Join<X, Y> join(String name);

    /**
     * Creates an inner join on an archetype node, restricted to the specified archetype.
     *
     * @param name      the archetype node name
     * @param archetype the archetype. May contain wildcards
     * @return a new join
     */
    <Y extends IMObject> Join<X, Y> join(String name, String archetype);

    /**
     * Creates a left join on an archetype node.
     *
     * @param name the archetype node name
     * @return a new join
     */
    <Y extends IMObject> Join<X, Y> leftJoin(String name);

    /**
     * Creates an inner join on an archetype node, restricted to the specified archetype.
     *
     * @param name      the archetype node name
     * @param archetype the archetype. May contain wildcards
     * @return a new join
     */
    <Y extends IMObject> Join<X, Y> leftJoin(String name, String archetype);
}
