/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.service.archetype.helper;


/**
 * Resolves values from an object given a naming scheme of the form
 * <tt>property1.property2.propertyN</tt>.
 * <p/>
 * Where a property refers to an <tt>IMObjectReference</tt>, or a collection
 * of 0..1 IMObject instances, these will be treated as an <tt>IMObject</tt>.
 * e.g. given an <tt>Act</tt> object, with archetype act.customerEstimation,
 * the field name <em>customer.entity.name</em>, this will:
 * <ul>
 * <li>get the <tt>Participation</tt> instance corresponding to the
 * "customer" node</li>
 * <li>get the Entity instance corresponding to the "entity" node of the
 * Participation</li>
 * <li>get the value of the "name" node of the entity.</li>
 * </ul>
 * <p/>
 * When an <tt>IMObject</tt> is resolved, several special property names are
 * defined:
 * <ul>
 * <li><em>shortName</em> - returns the value of the archetypes short name</li>
 * <li><em>displayName</em> - returns the value of the archetypes display
 * name</li>
 * </ul>
 * These are only evaluated when they appear as leaf nodes and the archetype
 * corresponding to the leaf has doesn't define the node.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
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
}
