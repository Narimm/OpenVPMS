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

/**
 * A named property for an {@link AssertionDescriptor} that includes the type of the property.
 *
 * @author Jim Alateras
 * @author Tim Anderson
 */
public interface AssertionProperty extends NamedProperty {

    /**
     * Returns the property type.
     *
     * @return a fully qualified class name indicating the type of the property
     */
    String getType();

    /**
     * Sets the property type.
     *
     * @param type a fully qualified class name indicating the type of the property
     */
    void setType(String type);

}
