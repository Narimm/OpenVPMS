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

import org.openvpms.component.model.object.IMObject;

import java.util.Set;

/**
 * This is used to define the assertion type. It is used to map an assertion to its type information.
 *
 * @author Jim Alateras
 * @author Tim Anderson
 */
public interface AssertionTypeDescriptor extends IMObject {

    /**
     * Returns the action types.
     *
     * @return the action types.
     */
    Set<ActionTypeDescriptor> getActionTypes();

    /**
     * Returns an action type, given its name.
     *
     * @param name the action type name
     * @return the action type, or {@code null} if none is found
     */
    ActionTypeDescriptor getActionType(String name);
}
