/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2009 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.component.business.service.archetype.assertion;

import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.Comparator;


/**
 * Implementation of the {@link ProperCaseConverter} interface for proper-casing names.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ProperCaseNameConverter implements ProperCaseConverter {

    /**
     * The case rules to use.
     */
    private final ProperCaseRules rules;

    /**
     * Strings that must appear surround by spaces.
     * Each element is a list of two strings, the first being a lowercase version used to match text, the second the
     * proper case version.
     */
    private String[][] space;

    /**
     * Strings that must appear with a space before them.
     * Each element is a list of two strings, the first being a lowercase version used to match text, the second the
     * proper case version.
     */
    private String[][] spaceBefore;

    /**
     * Strings that must appear with a space after them.
     * Each element is a list of two strings, the first being a lowercase version used to match text, the second the
     * proper case version.
     */
    private String[][] spaceAfter;

    /**
     * A list of exceptions. Any word(s) in the text matching these are replaced with the exceptions, ignoring other
     * case rules. Each element is a list of two strings, the first being a lowercase version used to match text, the
     * second the proper case version.
     */
    private String[][] exceptions;

    /**
     * Strings that must appear with the specified case at the start of a word. Where they appear, they force
     * capitalisation of the next character in the word.
     * Each element is a list of two strings, the first being a lowercase version used to match text, the second the
     * proper case version.
     */
    private String[][] startsWith;

    /**
     * Strings that must appear with the specified case anywhere in a word. Where they appear, rhey force capitalisation
     * of the next character in the word.
     * Each element is a list of two strings, the first being a lowercase version used to match text, the second the
     * proper case version.
     */
    private String[][] contains;

    /**
     * Strings that must appear with the specified case at the end of a word.
     * Each element is a list of two strings, the first being a lowercase version used to match text, the second the
     * proper case version.
     */
    private String[][] endsWith;

    /**
     * Keeps track of the rules version, to detect updates.
     */
    private int version = -1;


    /**
     * Creates a new <tt>ProperCaseNameConverter</tt/> using an {@link LocaleProperCaseRules} as the rules.
     */
    public ProperCaseNameConverter() {
        this(new LocaleProperCaseRules());
    }

    /**
     * Creates a new <tt>ProperCaseNameConverter</tt>.
     *
     * @param rules the proper-case rules
     */
    public ProperCaseNameConverter(ProperCaseRules rules) {
        this.rules = rules;
    }

    /**
     * Converts the case of the supplied text.
     *
     * @param text the text to convert
     * @return the converted text
     */
    public String convert(String text) {
        if (text == null) {
            return "";
        }
        Converter converter;
        synchronized (this) {
            getRules();
            converter = new Converter(text, space, spaceBefore, spaceAfter, exceptions, startsWith, endsWith, contains);
        }

        return converter.convert();
    }

    /**
     * Initialises the rules if they haven't yet been initialised, or the version has changed.
     */
    private synchronized void getRules() {
        if (exceptions == null || version != rules.getVersion()) {
            space = getStrings(rules.getSpace());
            spaceBefore = getStrings(rules.getSpaceBefore());
            spaceAfter = getStrings(rules.getSpaceAfter());
            exceptions = getStrings(rules.getExceptions());
            startsWith = getStrings(rules.getStartsWith());
            endsWith = getStrings(rules.getEndsWith());
            contains = getStrings(rules.getContains());
            version = rules.getVersion();
        }
    }

    /**
     * Returns a list of string pairs for the supplied strings. The first element is the lower case version of the
     * string, the second the proper case version.
     *
     * @param propercase the proper case strings
     * @return strings pairs corresponding to the proper case strings
     */
    private String[][] getStrings(String[] propercase) {
        String[][] result = new String[propercase.length][];
        Arrays.sort(propercase, new Comparator<String>() {
            public int compare(String o1, String o2) {
                return -(o1.length() - o2.length());
            }
        });
        for (int i = 0; i < result.length; ++i) {
            result[i] = new String[2];
            result[i][0] = propercase[i].toLowerCase();
            result[i][1] = propercase[i];
        }
        return result;
    }

    /**
     * Converts text to proper case.
     */
    private class Converter {

        /**
         * The text to convert.
         */
        private final String text;

        /**
         * The converted text
         */
        private final StringBuffer result;

        /**
         * The starting index into the text.
         */
        private int start;

        /**
         * The text length.
         */
        private final int length;

        /**
         * Exception rules.
         */
        private final String[][] exceptions;

        /**
         * Starts-with rules.
         */
        private final String[][] startsWith;

        /**
         * Ends-with rules.
         */
        private final String[][] endsWith;

        /**
         * Contains rules.
         */
        private final String[][] contains;

        /**
         * Creates a new <tt>Converter</tt>.
         *
         * @param text        the text to convert
         * @param space       space rules
         * @param spaceBefore space-before rules
         * @param spaceAfter  space-after rules
         * @param exceptions  exception rules
         * @param startsWith  starts-with rules
         * @param endsWith    ends-with rules
         * @param contains    contains rules
         */
        public Converter(String text, String[][] space, String[][] spaceBefore, String[][] spaceAfter,
                         String[][] exceptions, String[][] startsWith, String[][] endsWith, String[][] contains) {
            // lowercase in order to match with rules
            text = text.toLowerCase();

            // apply space rules
            for (String[] rule : space) {      //
                text = text.replace(rule[0], " " + rule[1] + " ");
            }

            for (String[] rule : spaceBefore) {
                text = text.replace(rule[0], " " + rule[1]);
            }

            for (String[] rule : spaceAfter) {
                text = text.replace(rule[0], rule[1] + " ");
            }

            // strip leading and trailing whitespace
            text = StringUtils.strip(text);

            // collapse consecutive spaces
            text = text.replaceAll(" +", " ");

            this.text = text;
            length = text.length();
            result = new StringBuffer(text.length());
            this.exceptions = exceptions;
            this.startsWith = startsWith;
            this.endsWith = endsWith;
            this.contains = contains;
        }

        /**
         * Converts the text to proper case.
         *
         * @return the converted text
         */
        public String convert() {
            start = 0;
            while (start < length) {
                int matchLen = addExceptionMatch();
                boolean capNext = false;
                if (matchLen == 0) {
                    capNext = true;
                    matchLen = addMatch(startsWith, length);
                }
                start += matchLen;
                int end = nextWhitespace();
                String endMatch = getEndsWith(end);
                if (endMatch != null) {
                    end -= endMatch.length();
                }
                while (start < end && !Character.isWhitespace(text.charAt(start))) {
                    matchLen = addMatch(contains, end);
                    if (matchLen == 0) {
                        if (capNext) {
                            result.append(Character.toTitleCase(text.charAt(start)));
                            capNext = false;
                        } else {
                            result.append(text.charAt(start));
                        }
                        ++start;
                    } else {
                        start += matchLen;
                        capNext = true;
                    }
                }
                if (endMatch != null) {
                    result.append(endMatch);
                    start += endMatch.length();
                }
                while (start < length && Character.isWhitespace(text.charAt(start))) {
                    result.append(text.charAt(start));
                    ++start;
                }
            }
            return result.toString();
        }

        /**
         * Tries to adds the proper case text of one of the exception rules if there is a match on a word boundary
         * (i.e the next character is whitespace or end of string).
         *
         * @return the length of the match, or <tt>0</tt> if there is no match
         */
        private int addExceptionMatch() {
            int matchLen = 0;
            for (String[] pair : exceptions) {
                int end = start + pair[0].length();
                if (end == length || (end < length && Character.isWhitespace(text.charAt(end)))) {
                    if ((matchLen = addMatch(pair)) != 0) {
                        break;
                    }
                }
            }
            return matchLen;
        }

        /**
         * Tries to adds the proper case text of one of the specified pairs if there is a match prior to or including
         * the end index.
         *
         * @param pairs the pairs to match
         * @param end   the end index
         * @return the length of the match, or <tt>0</tt> if there is no match
         */
        private int addMatch(String[][] pairs, int end) {
            int matchLen = 0;
            for (String[] pair : pairs) {
                if (start + pair[0].length() <= end) {
                    if ((matchLen = addMatch(pair)) != 0) {
                        break;
                    }
                }
            }
            return matchLen;
        }

        /**
         * Adds the proper case text if the lowercase version matches the text at <tt>start</tt>.
         *
         * @param pair the lower and proper case pair
         * @return the length of the match, or <tt>0</tt> if there is no match
         */
        private int addMatch(String[] pair) {
            if (text.startsWith(pair[0], start)) {
                String proper = pair[1];
                result.append(proper);
                return proper.length();
            }
            return 0;
        }

        /**
         * Determines if any ends-with rule matches the text prior to the specified offset.
         *
         * @param end the end offset
         * @return the proper case text for the first match, or <tt>null</tt> if none is found
         */
        private String getEndsWith(int end) {
            for (String[] pair : endsWith) {
                String lower = pair[0];
                if (text.startsWith(lower, end - lower.length())) {
                    return pair[1];
                }
            }
            return null;
        }

        /**
         * Returns the index of the next whitespace character after <tt>start</tt>.
         *
         * @return the index of the next whitespace, or <tt>length</tt> if none is found
         */
        private int nextWhitespace() {
            for (int i = start + 1; i < length; ++i) {
                if (Character.isWhitespace(text.charAt(i))) {
                    return i;
                }
            }
            return length;
        }

    }
}