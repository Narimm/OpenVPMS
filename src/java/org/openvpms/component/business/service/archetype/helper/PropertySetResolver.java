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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.business.service.archetype.helper;

import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.util.PropertySet;


/**
 * Resolves property values given a root {@code PropertySet} and a name of
 * the form <em>propertyName.node1.node2.nodeN</em>.
 * <p/>
 * The <em>propertyName</em> is used to resolve the object in set.
 * If the object is an {@code IMObject} or {@code IMObjectReference}, the
 * <em>node*</em> names may be specified to resolve any nodes in the object.
 * The nodes naming follows the convention used by {@link NodeResolver}.
 * <p/>
 * The property name may include '.' characters.
 *
 * @author Tim Anderson
 */
public class PropertySetResolver extends AbstractPropertyResolver {

    /**
     * The property set.
     */
    private final PropertySet set;


    /**
     * Constructs a {@code PropertySetResolver}.
     *
     * @param set     the property set
     * @param service the archetype service
     */
    public PropertySetResolver(PropertySet set, IArchetypeService service) {
        super(service);
        this.set = set;
    }

    /**
     * Returns the value of a property.
     *
     * @param name the property name
     * @return the value of the property
     * @throws PropertyResolverException if the property doesn't exist
     */
    @Override
    protected Object get(String name) {
        return set.get(name);
    }

    /**
     * Determines if a property exists.
     *
     * @param name the property name
     * @return {@code true} if the property exists
     */
    @Override
    protected boolean exists(String name) {
        return set.exists(name);
    }
}
