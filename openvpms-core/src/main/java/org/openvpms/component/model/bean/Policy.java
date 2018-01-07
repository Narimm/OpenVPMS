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

package org.openvpms.component.model.bean;

import org.openvpms.component.model.object.Relationship;

import java.util.Comparator;
import java.util.function.Predicate;

/**
 * The policy for selecting relationships and retrieving objects when operating on nodes with an {@link IMObjectBean}.
 *
 * @author Tim Anderson
 */
public interface Policy<R extends Relationship> {

    enum State {
        ACTIVE,     // the object must be active
        INACTIVE,   // the object must be inactive
        ANY         // the object may by either active or inactive
    }

    /**
     * Returns the predicate to select relationships.
     *
     * @return the predicate to select relationships, or {@code null} if all relationships should be selected
     */
    Predicate<R> getPredicate();

    /**
     * Returns the active state that the objects have.
     *
     * @return the active state
     */
    State getState();

    /**
     * Returns the comparator to order relationships.
     *
     * @return the comparator, or {@code null} if no ordering is required
     */
    Comparator<R> getComparator();

    /**
     * Returns the expected relationship type.
     *
     * @return the relationship type
     */
    Class<R> getType();

}
