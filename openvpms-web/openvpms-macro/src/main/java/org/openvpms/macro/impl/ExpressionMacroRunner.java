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

import org.openvpms.macro.MacroException;

/**
 * Evaluates expression macros using JXPath.
 *
 * @author Tim Anderson
 */
class ExpressionMacroRunner extends AbstractExpressionMacroRunner {


    /**
     * Constructs an {@link ExpressionMacroRunner}.
     *
     * @param context the macro context
     */
    public ExpressionMacroRunner(MacroContext context) {
        super(context);
    }

    /**
     * Runs a macro.
     *
     * @param macro  the macro to run
     * @param number a numeric expression, used to declare the <em>$number</em> variable. May be empty or {@code null}
     * @return the result of the macro
     * @throws MacroException for any macro error
     */
    @Override
    public String run(Macro macro, String number) {
        Object value = evaluate((ExpressionMacro) macro, number);
        return (value != null) ? value.toString() : null;
    }

}
