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

package org.openvpms.component.model.archetype;

import java.util.Map;

/**
 * A map of named properties.
 *
 * @author Tim Anderson
 */
public interface PropertyMap extends NamedProperty {

    /**
     * Returns the properties.
     *
     * @return the properties
     */
    Map<String, NamedProperty> getProperties();

    /**
     * Add a property to the map.
     *
     * @param property the property to add
     */
    void addProperty(NamedProperty property);

    /**
     * Removes a specified property from the map.
     *
     * @param property the property to remove
     */
    void removeProperty(NamedProperty property);

}
