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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.macro;


/**
 * Provides support for executing macros.
 *
 * @author Tim Anderson
 */
public interface Macros {

    /**
     * Determines if a macro exists.
     *
     * @param macro the macro name
     * @return {@code true} if the macro exists
     */
    boolean exists(String macro);

    /**
     * Runs a macro.
     * <p/>
     * If the macro is preceded by a numeric expression, the value will be declared as a variable <em>$number</em>.
     *
     * @param macro  the macro code
     * @param object the object to evaluate the macro against. May be {@code null}
     * @return the result of the macro. May be {@code null} if the macro doesn't exist, or evaluates {@code null}
     * @throws MacroException for any error
     */
    String run(String macro, Object object);

    /**
     * Runs a macro.
     * <p/>
     * If the macro is preceded by a numeric expression, the value will be declared as a variable <em>$number</em>.
     *
     * @param macro     the macro code
     * @param object    the object to evaluate the macro against. May be {@code null}
     * @param variables variables to supply to the macro. May be {@code null}
     * @return the result of the macro. May be {@code null} if the macro doesn't exist, or evaluates {@code null}
     * @throws MacroException for any error
     */
    String run(String macro, Object object, Variables variables);

    /**
     * Runs all macros in the supplied text.
     * <p/>
     * When a macro is encountered, it will be replaced with the macro value.
     * <p/>
     * If a macro is preceded by a numeric expression, the value will be declared as a variable <em>$number</em>.
     *
     * @param text   the text to parse
     * @param object the object to evaluate macros against. May be {@code null}
     * @return the text will macros substituted for their values
     */
    String runAll(String text, Object object);

    /**
     * Runs all macros in the supplied text.
     * <p/>
     * When a macro is encountered, it will be replaced with the macro value.
     * <p/>
     * If a macro is preceded by a numeric expression, the value will be declared as a variable <em>$number</em>.
     *
     * @param text      the text to parse
     * @param object    the object to evaluate macros against. May be {@code null}
     * @param variables variables to supply to macros. May be {@code null}
     * @param position  tracks the cursor position. The cursor position will be moved if macros before it are expanded.
     *                  May be {@code null}
     * @return the text will macros substituted for their values
     */
    String runAll(String text, Object object, Variables variables, Position position);

}