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
 * .
 *
 * @author Tim Anderson
 */
public interface PropertyMap extends NamedProperty {

    /**
     * @return Returns the properties.
     */
    public Map<String, NamedProperty> getProperties();

    /**
     * Add the specified property to the list
     *
     * @param property the property to add
     */
    public void addProperty(NamedProperty property);

    /**
     * Remove the specified property from the list
     *
     * @param property the property to remove
     */
    public void removeProperty(NamedProperty property);

}
