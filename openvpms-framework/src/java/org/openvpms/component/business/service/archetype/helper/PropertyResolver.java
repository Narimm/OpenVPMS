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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.business.service.archetype.helper;


import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.util.PropertyState;

import java.util.List;

/**
 * Resolves values from an object given a naming scheme of the form
 * <tt>property1.property2.propertyN</tt>.
 * <p/>
 * Where a property refers to an {@code IMObjectReference}, or a collection
 * of 0..1 IMObject instances, these will be treated as an {@code IMObject}.
 * e.g. given an {@code Act} object, with archetype act.customerEstimation,
 * the field name <em>customer.entity.name</em>, this will:
 * <ul>
 * <li>get the {@code Participation} instance corresponding to the
 * "customer" node</li>
 * <li>get the Entity instance corresponding to the "entity" node of the
 * Participation</li>
 * <li>get the value of the "name" node of the entity.</li>
 * </ul>
 * <p/>
 * When an {@link IMObject} is resolved, several special property names are defined:
 * <ul>
 * <li><em>shortName</em> - returns the value of the archetypes short name</li>
 * <li><em>displayName</em> - returns the value of the archetypes display
 * name</li>
 * </ul>
 * These are only evaluated when they appear as leaf nodes and the archetype
 * corresponding to the leaf has doesn't define the node.
 *
 * @author Tim Anderson
 */
public interface PropertyResolver {

    /**
     * Resolves the named property.
     *
     * @param name the property name
     * @return the corresponding property
     * @throws PropertyResolverException if the name is invalid
     */
    Object getObject(String name);

    /**
     * Returns all objects matching the named property.
     * <p/>
     * Unlike {@link #getObject(String)}, this method handles collections of arbitrary size.
     *
     * @param name the property name
     * @return the corresponding property values
     * @throws PropertyResolverException if the name is invalid
     */
    List<Object> getObjects(String name);

    /**
     * Resolves the state corresponding to a property.
     *
     * @param name the property name
     * @return the resolved state
     * @throws PropertyResolverException if the name is invalid
     */
    PropertyState resolve(String name);
}