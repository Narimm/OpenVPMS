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
 * Runs a macro.
 *
 * @author Tim Anderson
 */
abstract class MacroRunner {

    /**
     * The macro context.
     */
    private final MacroContext context;


    /**
     * Constructs an {@link MacroRunner}.
     *
     * @param context the macro context
     */
    public MacroRunner(MacroContext context) {
        this.context = context;
    }

    /**
     * Runs a macro.
     *
     * @param macro  the macro to run
     * @param number a numeric expression, used to declare the <em>$number</em> variable. May be empty or {@code null}
     * @return the result of the macro
     * @throws MacroException for any macro error
     */
    public abstract String run(Macro macro, String number);

    /**
     * Returns the context to execute macros in.
     *
     * @return the macro context
     */
    public MacroContext getContext() {
        return context;
    }

    /**
     * Returns the object to evaluate macros against.
     *
     * @return the object to evaluate macros against. May be {@code null}
     */
    protected Object getObject() {
        return context.getObject();
    }

    /**
     * Determines if a macro exists.
     *
     * @param macro the macro code
     * @return {@code true} if the macro exists, otherwise {@code false}
     */
    protected boolean exists(String macro) {
        return context.exists(macro);
    }


}
