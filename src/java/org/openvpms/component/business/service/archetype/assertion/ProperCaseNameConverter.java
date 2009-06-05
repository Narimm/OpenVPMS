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
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ProperCaseNameConverter implements ProperCaseConverter {

    private final ProperCaseRules rules;

    private String[][] exceptions;

    private String[][] startsWith;

    private String[][] endsWith;

    private String[][] contains;

    private int version = -1;

    public ProperCaseNameConverter() {
        this(new LocaleProperCaseRules());
    }

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
            converter = new Converter(text, exceptions, startsWith, endsWith, contains);
        }

        return converter.convert();
    }

    /**
     * Initialises the rules if they haven't yet been initialised, or the version has changed.
     */
    private synchronized void getRules() {
        if (exceptions == null || version != rules.getVersion()) {
            exceptions = getStrings(rules.getExceptions());
            startsWith = getStrings(rules.getStartsWith());
            endsWith = getStrings(rules.getEndsWith());
            contains = getStrings(rules.getContains());
            version = rules.getVersion();
        }
    }

    private String[][] getStrings(String[] exceptions) {
        String[][] result = new String[exceptions.length][];
        Arrays.sort(exceptions, new Comparator<String>() {
            public int compare(String o1, String o2) {
                return -(o1.length() - o2.length());
            }
        });
        for (int i = 0; i < result.length; ++i) {
            result[i] = new String[2];
            result[i][0] = exceptions[i].toLowerCase();
            result[i][1] = exceptions[i];
        }
        return result;
    }

    private class Converter {

        private final String text;

        private final StringBuffer result;

        private int start;

        private final int length;

        private final String[][] exceptions;

        private final String[][] startsWith;

        private final String[][] endsWith;

        private final String[][] contains;

        public Converter(String text, String[][] exceptions, String[][] startsWith,
                         String[][] endsWith, String[][] contains) {
            text = StringUtils.strip(text);
            text = text.replaceAll("\\p{javaWhitespace}", " ");
            text = text.toLowerCase();
            this.text = text;
            length = text.length();
            result = new StringBuffer(text.length());
            this.exceptions = exceptions;
            this.startsWith = startsWith;
            this.endsWith = endsWith;
            this.contains = contains;
        }

        public String convert() {
            start = 0;
            while (start < length) {
                int matchLen = checkExceptions();
                boolean capNext = false;
                if (matchLen == 0) {
                    capNext = true;
                    matchLen = addMatch(startsWith, length);
                }
                start += matchLen;
                int end = text.indexOf(' ', start + 1);
                if (end == -1) {
                    end = length;
                }
                String endMatch = getEndsWith(end);
                if (endMatch != null) {
                    end -= endMatch.length();
                }
                while (start < end && text.charAt(start) != ' ') {
                    if (capNext) {
                        result.append(Character.toTitleCase(text.charAt(start)));
                        ++start;
                        capNext = false;
                        continue;
                    }
                    matchLen = addMatch(contains, end);
                    if (matchLen == 0) {
                        result.append(text.charAt(start));
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
                boolean spaces = false;
                while (start < length && text.charAt(start) == ' ') {
                    spaces = true;
                    ++start;
                }
                if (spaces) {
                    result.append(" ");
                }
            }
            return result.toString();
        }

        private int checkExceptions() {
            int matchLen = 0;
            for (String[] pair : exceptions) {
                int end = start + pair[0].length();
                if (end == length || (end < length && text.charAt(end) == ' ')) {
                    if ((matchLen = addMatch(pair)) != 0) {
                        break;
                    }
                }
            }
            return matchLen;
        }

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

        private int addMatch(String[] pair) {
            if (text.startsWith(pair[0], start)) {
                String proper = pair[1];
                result.append(proper);
                return proper.length();
            }
            return 0;
        }

        private String getEndsWith(int end) {
            for (String[] pair : endsWith) {
                String lower = pair[0];
                if (text.startsWith(lower, end - lower.length())) {
                    return pair[1];
                }
            }
            return null;
        }

    }
}