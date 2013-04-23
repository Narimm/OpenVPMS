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

package org.openvpms.archetype.util;

import java.util.HashMap;
import java.util.Map;


/**
 * Default implementation of the {@link Variables} interface.
 *
 * @author Tim Anderson
 */
public class DefaultVariables implements Variables {

    /**
     * The variables.
     */
    private final Map<String, Object> variables = new HashMap<String, Object>();


    /**
     * Adds a variable.
     *
     * @param name  the variable name
     * @param value the variable value
     */
    public void add(String name, Object value) {
        variables.put(name, value);
    }

    /**
     * Returns a variable value.
     *
     * @param name the variable name
     * @return the variable value. May be {@code null}
     */
    public Object get(String name) {
        return variables.get(name);
    }

    /**
     * Determines if a variable exists.
     *
     * @param name the variable name
     * @return {@code true} if the variable exists
     */
    public boolean exists(String name) {
        return variables.containsKey(name);
    }
}
