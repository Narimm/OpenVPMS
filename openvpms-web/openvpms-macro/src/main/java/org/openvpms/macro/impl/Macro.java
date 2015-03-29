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

package org.openvpms.macro.impl;

import org.openvpms.component.business.domain.im.lookup.Lookup;


/**
 * A macro definition.
 *
 * @author Tim Anderson
 */
abstract class Macro {

    /**
     * The macro code.
     */
    private final String code;

    /**
     * The macro name.
     */
    private final String name;

    /**
     * Constructs a {@code Macro} from a lookup.
     *
     * @param lookup the macro lookup
     */
    public Macro(Lookup lookup) {
        this.code = lookup.getCode();
        this.name = lookup.getName();
    }

    /**
     * Returns the macro code.
     *
     * @return the macro code
     */
    public String getCode() {
        return code;
    }

    /**
     * Returns the macro name.
     *
     * @return the macro name
     */
    public String getName() {
        return name;
    }
}
