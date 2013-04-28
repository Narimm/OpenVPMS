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

import org.apache.commons.lang.StringUtils;
import org.openvpms.macro.MacroException;
import org.openvpms.macro.Variables;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * Context to run macros in.
 * <p/>
 * This is intended to be used to evaluate macros against a single object and a set of user defined variables.
 * <p/>
 * A macro may refer to other macros; this class ensures that a macro doesn't call itself recursively, either directly
 * or indirectly.
 * <p/>
 * NOTE: it will not handle the case where a macro recursively calls itself via a macro:eval() xpath function.
 *
 * @author Tim Anderson
 */
class MacroContext {

    /**
     * The macros, keyed on code.
     */
    private final Map<String, Macro> macros;

    /**
     * The factory for creating {@link MacroRunner}s.
     */
    private final MacroFactory factory;

    /**
     * The object to evaluate macros against. May be {@code null}
     */
    private final Object object;

    /**
     * The user variables. May be {@code null}.
     */
    private final Variables variables;

    /**
     * The macro runners.
     */
    private Map<Class, MacroRunner> runners = new HashMap<Class, MacroRunner>();

    /**
     * Tracks currently running macros to avoid recursion.
     */
    private Deque<String> running = new ArrayDeque<String>();


    /**
     * Constructs a {@code MacroContext}.
     *
     * @param macros    the macros, keyed on code
     * @param factory   the macro factory
     * @param object    the object to evaluate macros against. May be {@code null}
     * @param variables the user variables. May be {@code null}
     */
    public MacroContext(Map<String, Macro> macros, MacroFactory factory, Object object, Variables variables) {
        this.macros = macros;
        this.factory = factory;
        this.object = object;
        this.variables = variables;
    }

    /**
     * Returns the object to evaluate macros against.
     *
     * @return the object to evaluate macros against. May be {@code null}
     */
    public Object getObject() {
        return object;
    }

    /**
     * Returns the user variables.
     *
     * @return the user variables. May be {@code null}
     */
    public Variables getVariables() {
        return variables;
    }

    /**
     * Returns a runner to run the specified macro.
     * <p/>
     * Runners are created on demand, and cached for subsequent requests.
     *
     * @param macro the macro
     * @return a runner for the macro
     */
    public MacroRunner getRunner(Macro macro) {
        MacroRunner runner = runners.get(macro.getClass());
        if (runner == null) {
            runner = factory.create(macro, this);
            runners.put(macro.getClass(), runner);
        }
        return runner;
    }

    /**
     * Determines if a macro exists.
     *
     * @param macro the macro code
     * @return {@code true} if the macro exists, otherwise {@code false}
     */
    public boolean exists(String macro) {
        return macros.containsKey(macro);
    }

    /**
     * Runs a macro.
     *
     * @param macro  the macro to run
     * @param number a numeric expression, used to declare the <em>$number</em> variable. May be empty or {@code null}
     * @return the result of the macro
     */
    public String run(Macro macro, String number) {
        String result = null;
        String code = macro.getCode();
        if (running.contains(code)) {
            throw new MacroException(
                "Macro " + code + " called recursively from " + StringUtils.join(running.descendingIterator(), " => "));
        }
        running.push(code);
        try {
            MacroRunner runner = getRunner(macro);
            result = runner.run(macro, number);
        } finally {
            running.pop();
        }
        return result;
    }

    /**
     * Runs a macro.
     *
     * @param macro  the macro code
     * @param number a numeric expression, used to declare the <em>$number</em> variable. May be empty or {@code null}
     * @return the result of the macro
     */
    public String run(String macro, String number) {
        String result = null;
        Macro m = macros.get(macro);
        if (m != null) {
            result = run(m, number);
        }
        return result;
    }
}
