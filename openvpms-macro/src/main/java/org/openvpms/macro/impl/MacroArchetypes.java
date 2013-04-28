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


/**
 * Macro archetypes.
 *
 * @author Tim Anderson
 */
class MacroArchetypes {

    /**
     * Expression macro archetype.
     */
    public static final String EXPRESSION_MACRO = "lookup.macro";

    /**
     * Report macro archetype.
     */
    public static final String REPORT_MACRO = "lookup.macroReport";

    /**
     * Macro archetype short names.
     */
    public static final String[] LOOKUP_MACROS = {EXPRESSION_MACRO, REPORT_MACRO};

    /**
     * Default constructor.
     */
    private MacroArchetypes() {

    }
}
