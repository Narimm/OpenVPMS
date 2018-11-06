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
 * This interface defines functionality common to top-level queries and subqueries.<br/>
 * It corresponds to the JPA interface of the same name.
 *
 * @param <T> the type of the result
 * @author Tim Anderson
 */
public interface AbstractQuery<T> {

    /**
     * Add a query root for the given archetypes.
     *
     * @param type       the underlying archetype class
     * @param archetypes the archetypes. May contain wildcards
     * @return the new root
     */
    <X extends IMObject> Root<X> from(Class<X> type, String... archetypes);

}
