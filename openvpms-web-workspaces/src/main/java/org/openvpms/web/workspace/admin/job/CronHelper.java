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

package org.openvpms.web.workspace.admin.job;

import java.util.regex.Pattern;

/**
 * Cron helper.
 *
 * @author Tim Anderson
 */
public class CronHelper {

    /**
     * The regular expression for the Cron minutes field.
     */
    public static final Pattern MINUTES = getPattern("[0-5]?\\d");

    /**
     * The regular expression for the Cron hours field.
     */
    public static final Pattern HOURS = getPattern("[01]?\\d|2[0-3]");

    /**
     * The regular expression for the Cron day-of-month field.
     */
    public static final Pattern DAY_OF_MONTH = getPattern("\\?|", "0?[1-9]|[12]\\d|3[01]", null);

    /**
     * The regular expression for the Cron month field.
     */
    public static final Pattern MONTH = getPattern(
            null, "[1-9]|1[012]", "|" + createRange("jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec"));


    /**
     * The regular expression for the Cron day-of-week field.
     */
    public static final Pattern DAY_OF_WEEK
            = getPattern("\\?|", "[1-7]", "|" + createRange("mon|tue|wed|thu|fri|sat|sun"));

    /**
     * Helper to compile a Cron pattern.
     *
     * @param prefix a regular expression to prepend to the pattern. May be {@code null}
     * @param value  the value used to construct a range expression
     * @param suffix a regular expression to append to the pattern
     * @return the compiled pattern
     */
    private static Pattern getPattern(String prefix, String value, String suffix) {
        StringBuilder pattern = new StringBuilder();
        if (prefix != null) {
            pattern.append(prefix);
        }
        pattern.append(createRange(value));
        if (suffix != null) {
            pattern.append(suffix);
        }
        return Pattern.compile(pattern.toString(), Pattern.CASE_INSENSITIVE);
    }

    /**
     * Helper to compile a Cron pattern.
     *
     * @param value the value used to construct a range expression
     * @return the compiled pattern
     */
    private static Pattern getPattern(String value) {
        return getPattern(null, value, null);
    }

    /**
     * Helper to create a range pattern.
     *
     * @param value the value to create the range for
     * @return the range pattern
     */
    private static String createRange(String value) {
        String range = "(" + value + ")(-(" + value + ")(\\/\\d+)?)?";
        return "\\*(\\/\\d+)?|" + range + "(," + range + ")*";
    }

}
