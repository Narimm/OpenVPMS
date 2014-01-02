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

package org.openvpms.macro.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.AbstractArchetypeServiceListener;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.IArchetypeServiceListener;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.macro.MacroException;
import org.openvpms.macro.Macros;
import org.openvpms.macro.Variables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.StringTokenizer;


/**
 * An implementation of {@link Macros} that obtains macro definitions from <em>lookup.macro</em> and
 * <em>lookup.macroReport</em> lookups.
 * <p/>
 * These are monitored for updates to ensure that the macros reflect those in the database.
 *
 * @author Tim Anderson
 */
public class LookupMacros implements Macros {

    /**
     * The macros, keyed on code
     */
    private final Map<String, Macro> macros = Collections.synchronizedMap(new HashMap<String, Macro>());

    /**
     * The macro factory.
     */
    private final MacroFactory factory;


    /**
     * The per-thread variables. These are required so that variables may be supplied to nested macros when they
     * are invoked via macro:eval().
     */
    private final ThreadLocal<ScopedVariables> scopedVariables = new ThreadLocal<ScopedVariables>();

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(LookupMacros.class);


    /**
     * Constructs a {@code LookupMacros}.
     *
     * @param service          the lookup service
     * @param archetypeService the archetype service
     * @param handlers         the document handlers
     * @throws ArchetypeServiceException for any archetype service error
     */
    public LookupMacros(ILookupService service, IArchetypeService archetypeService, DocumentHandlers handlers) {
        factory = new MacroFactory(archetypeService, handlers);

        for (String shortName : MacroArchetypes.LOOKUP_MACROS) {
            addMacros(shortName, service);
        }
        IArchetypeServiceListener listener = new AbstractArchetypeServiceListener() {
            public void saved(IMObject object) {
                onSaved((Lookup) object);
            }

            public void removed(IMObject object) {
                delete((Lookup) object);
            }
        };
        for (String shortName : MacroArchetypes.LOOKUP_MACROS) {
            archetypeService.addListener(shortName, listener);
        }
    }

    /**
     * Determines if a macro exists.
     *
     * @param macro the macro name
     * @return {@code true} if the macro exists
     */
    public boolean exists(String macro) {
        return macros.containsKey(macro);
    }

    /**
     * Runs a macro.
     * <p/>
     * If the macro code is preceded by a numeric expression, the value will be declared as a variable <em>$number</em>.
     *
     * @param macro  the macro code
     * @param object the object to evaluate the macro against. May be {@code null}
     * @return the result of the macro. May be {@code null} if the macro doesn't exist, or evaluates {@code null}
     * @throws MacroException for any error
     */
    public String run(String macro, Object object) {
        return run(macro, object, null);
    }

    /**
     * Runs a macro.
     * <p/>
     * If the macro code is preceded by a numeric expression, the value will be declared as a variable <em>$number</em>.
     *
     * @param macro     the macro code
     * @param object    the object to evaluate the macro against
     * @param variables variables to supply to the macro. May be {@code null}
     * @return the result of the macro. May be {@code null} if the macro doesn't exist, or evaluates {@code null}
     * @throws MacroException for any error
     */
    public String run(String macro, Object object, Variables variables) {
        String result = null;
        Token token = Token.parse(macro);
        Macro m = macros.get(token.getToken());
        if (m != null) {
            ScopedVariables scoped = pushVariables(variables);
            try {
                MacroContext context = new MacroContext(macros, factory, object, scoped);
                result = context.run(m, token.getNumericPrefix());
            } catch (MacroException exception) {
                throw exception;
            } catch (Throwable exception) {
                throw new MacroException("Failed to evaluate macro=" + macro, exception);
            } finally {
                popVariables(variables, scoped);
            }
        }
        return result;
    }

    /**
     * Runs all macros in the supplied text.
     * <p/>
     * When a macro is encountered, it will be replaced with the macro value.
     * <p/>
     * If a macro is preceded by a numeric expression, the value will be declared as a variable <em>$number</em>.
     *
     * @param text   the text to parse
     * @param object the object to evaluate macros against
     * @return the text will macros substituted for their values
     */
    public String runAll(String text, Object object) {
        return runAll(text, object, null);
    }

    /**
     * Runs all macros in the supplied text.
     * <p/>
     * When a macro is encountered, it will be replaced with the macro value.
     * <p/>
     * If a macro is preceded by a numeric expression, the value will be declared as a variable <em>$number</em>.
     *
     * @param text      the text to parse
     * @param object    the object to evaluate macros against
     * @param variables variables to supply to macros. May be {@code null}
     * @return the text will macros substituted for their values
     */
    public String runAll(String text, Object object, Variables variables) {
        StringBuilder result = new StringBuilder();
        ScopedVariables scoped = pushVariables(variables);
        try {
            MacroContext context = new MacroContext(macros, factory, object, scoped);

            StringTokenizer tokens = new StringTokenizer(text, " \t\n\r", true);
            while (tokens.hasMoreTokens()) {
                Token token = Token.parse(tokens.nextToken());
                Macro macro = macros.get(token.getToken());
                if (macro != null) {
                    try {
                        String value = context.run(macro, token.getNumericPrefix());
                        if (value != null) {
                            result.append(value);
                        }
                    } catch (Throwable exception) {
                        log.warn(exception, exception);
                        result.append(token.getText());
                    }
                } else {
                    result.append(token.getText());
                }
            }
        } finally {
            popVariables(variables, scoped);
        }
        return result.toString();
    }

    /**
     * Cache macros of the specified archetype short name.
     *
     * @param shortName the archetype short name
     * @param service   the lookup service
     */
    private void addMacros(String shortName, ILookupService service) {
        Collection<Lookup> macros = service.getLookups(shortName);
        for (Lookup lookup : macros) {
            if (lookup.isActive()) {
                add(lookup);
            }
        }
    }

    /**
     * Invoked when a lookup is saved.
     *
     * @param lookup the lookup
     */
    private void onSaved(Lookup lookup) {
        if (lookup.isActive()) {
            add(lookup);
        } else {
            delete(lookup);
        }
    }

    /**
     * Adds a macro to the cache.
     *
     * @param lookup the macro definition
     */
    private void add(Lookup lookup) {
        try {
            macros.put(lookup.getCode(), factory.create(lookup));
        } catch (Throwable exception) {
            log.error(exception.getMessage(), exception);
        }
    }

    /**
     * Removes a macro from the cache.
     *
     * @param lookup the macro definition
     */
    private void delete(Lookup lookup) {
        macros.remove(lookup.getCode());
    }

    private ScopedVariables pushVariables(Variables variables) {
        ScopedVariables scoped = scopedVariables.get();
        if (scoped == null && variables != null) {
            scoped = new ScopedVariables();
            scopedVariables.set(scoped);
        }
        if (scoped != null && variables != null) {
            scoped.push(variables);
        }
        return scoped;
    }

    private void popVariables(Variables variables, ScopedVariables scoped) {
        if (scoped != null && variables != null) {
            scoped.pop();
            if (scoped.isEmpty()) {
                scopedVariables.set(null);
            }
        }
    }

    /**
     * Helper to parse text into a token and optional number.
     */
    private static class Token {

        /**
         * The original text.
         */
        private final String text;

        /**
         * The numeric prefix parsed from the text. May be {@code null}
         */
        private final String numericPrefix;

        /**
         * The token parsed from the text.
         */
        private final String token;

        /**
         * Constructs a {@code Token}.
         *
         * @param text          the original text
         * @param token         the parsed token
         * @param numericPrefix the numeric prefix. May be {@code null}
         */
        public Token(String text, String token, String numericPrefix) {
            this.text = text;
            this.token = token;
            this.numericPrefix = numericPrefix;
        }

        /**
         * Returns the original text.
         *
         * @return the text
         */
        public String getText() {
            return text;
        }

        /**
         * Returns the token.
         *
         * @return the token
         */
        public String getToken() {
            return token;
        }

        /**
         * Returns any numeric-like prefix before the token.
         *
         * @return the numeric prefix, or {@code null} if none was found
         */
        public String getNumericPrefix() {
            return numericPrefix;
        }

        /**
         * Parses a token from text.
         *
         * @param text the text to parse
         * @return a new {@link Token}
         */
        public static Token parse(String text) {
            // If the text starts with numbers strip numbers and create a number
            // variable. If any left pass token to test for macro.
            int index = 0;
            while (index < text.length() && isNumeric(text.charAt(index))) {
                ++index;
            }
            String token = text;
            String numeric = null;
            if (index != 0) {
                numeric = text.substring(0, index);
                token = token.substring(index);
            }
            return new Token(text, token, numeric);
        }

        /**
         * Determines if a character is numeric. This supports no.s in decimal
         * and fraction format.
         *
         * @param ch the character
         * @return {@code true} if {@code ch} is one of '0'..'9','.' or '/'
         */
        private static boolean isNumeric(char ch) {
            return Character.isDigit(ch) || ch == '.' || ch == '/';
        }
    }

    private static class ScopedVariables implements Variables {

        private final List<Variables> stack = new ArrayList<Variables>();

        public void push(Variables variables) {
            stack.add(variables);
        }

        public void pop() {
            stack.remove(stack.size() - 1);
        }

        /**
         * Returns a variable value.
         *
         * @param name the variable name
         * @return the variable value. May be {@code null}
         */
        @Override
        public Object get(String name) {
            for (ListIterator<Variables> iterator = stack.listIterator(stack.size()); iterator.hasPrevious(); ) {
                Variables variables = iterator.previous();
                if (variables.exists(name)) {
                    return variables.get(name);
                }
            }
            return null;
        }

        /**
         * Determines if a variable exists.
         *
         * @param name the variable name
         * @return {@code true} if the variable exists
         */
        @Override
        public boolean exists(String name) {
            for (ListIterator<Variables> iterator = stack.listIterator(stack.size()); iterator.hasPrevious(); ) {
                Variables variables = iterator.previous();
                if (variables.exists(name)) {
                    return true;
                }
            }
            return false;
        }

        public boolean isEmpty() {
            return stack.isEmpty();
        }
    }

}
