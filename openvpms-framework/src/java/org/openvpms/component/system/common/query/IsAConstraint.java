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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.system.common.query;

/**
 * A constraint that checks if an alias or node is one of a set of archetypes.
 *
 * @author Tim Anderson
 */
public class IsAConstraint implements IConstraint {

    /**
     * The alias/node name.
     */
    private final String name;

    /**
     * The archetype short names.
     */
    private final String[] shortNames;

    /**
     * Constructs an {@link IsAConstraint}.
     *
     * @param name       the alias or qualified node name
     * @param shortNames the archetype short names
     */
    public IsAConstraint(String name, String[] shortNames) {
        this.name = name;
        this.shortNames = shortNames;
    }

    /**
     * Returns the alias or qualified node name.
     *
     * @return the alias/node name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the archetype short names to compare with.
     *
     * @return the archetype short names
     */
    public String[] getShortNames() {
        return shortNames;
    }
}
