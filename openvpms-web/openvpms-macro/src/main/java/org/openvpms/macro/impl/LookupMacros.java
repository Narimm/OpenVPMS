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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
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
            try {
                MacroContext context = new MacroContext(macros, factory, object, variables);
                result = context.run(m, token.getNumericPrefix());
            } catch (MacroException exception) {
                throw exception;
            } catch (Throwable exception) {
                throw new MacroException("Failed to evaluate macro=" + macro, exception);
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
        MacroContext context = new MacroContext(macros, factory, object, variables);

        StringTokenizer tokens = new StringTokenizer(text, " \t\n\r", true);
        StringBuilder result = new StringBuilder();
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
     * Adds a lookup to the cache.
     *
     * @param lookup the lookup to add
     */
    private void add(Lookup lookup) {
        macros.put(lookup.getCode(), factory.create(lookup));
    }

    /**
     * Removes a lookup from the cache.
     *
     * @param lookup the lookup to remove
     */
    private void delete(Lookup lookup) {
        macros.remove(lookup.getCode());
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

}
