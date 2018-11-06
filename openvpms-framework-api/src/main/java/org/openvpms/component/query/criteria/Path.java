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

import org.openvpms.component.model.object.Reference;

import javax.persistence.criteria.Expression;

/**
 * An expression that provides access to archetype nodes.
 *
 * @author Tim Anderson
 */
public interface Path<X> extends Expression<X> {

    /**
     * Sets the path alias.
     *
     * @param alias the alias
     * @return the path
     */
    @Override
    Path<X> alias(String alias);

    /**
     * Returns a path corresponding to an archetype node.
     *
     * @param name the node name
     * @return a path corresponding to the node
     */
    <Y> Path<Y> get(String name);

    /**
     * Returns a path corresponding to an archetype node.
     *
     * @param name the node name
     * @param type the type
     * @return a path corresponding to the node
     */
    <Y> Path<Y> get(String name, Class<Y> type);

    /**
     * Returns a path that is the reference to the instance.
     *
     * @return the path
     */
    Path<Reference> reference();
}
