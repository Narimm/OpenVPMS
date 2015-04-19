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

package org.openvpms.web.echo.date;

import org.apache.commons.lang.StringUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Relative date parser.
 * <p/>
 * Recognises strings of the form: <code>({+|-}(0..9)+(d|m|w|q|y)+{s|e})</code>
 * as being relative to the current date, where d=day, w=week, m=month, q=quarter
 * (i.e. 3 months), and y=year. The optional s or e means start or end. For weeks,
 * start means Monday, end Friday
 * E.g:
 * <ul>
 * <li><em>-10w3d<em> sets the date to 10 weeks and 3 days before the current
 * date</li>
 * <li><em>5y3m</em> sets the date to 5 years and 3 months from the current
 * date</li>
 * <li><em>-1qs</em> sets the date to the start of the quarter before the current
 * one</li>
 * <li><em>-1ms+3d</em> sets the date to 3 days after the start of the previous month</li>
 * <li><em>0ys</em> sets the date to the start (1 Jan) of the current year</li>
 * <li><em>0ye-1m</em> sets the date to one month prior to the end of the current year</li>
 * </ul>
 *
 * @author Tim Anderson
 * @author Tim Gething
 */
public class RelativeDateParser {

    /**
     * The pattern.
     */
    private final Pattern pattern = Pattern.compile("\\s*([\\+-]?\\d+)([dmwyq])([se]?)\\s*");

    /**
     * Parses a date relative to the current time.
     *
     * @param source the relative date string
     * @return the parsed date, or {@code null} if the source is invalid
     */
    public Date parse(String source) {
        return parse(source, null);
    }

    /**
     * Parses a date relative to the specified date.
     *
     * @param source the relative date string
     * @param date   the date. If {@code null}, the current date/time is used
     * @return the relative date, or {@code null} if the source is invalid
     */
    public Date parse(String source, Date date) {
        if (StringUtils.isEmpty(source)) {
            return null;
        }
        if (date == null) {
            date = new Date();
        }
        Matcher matcher = pattern.matcher(source.toLowerCase());
        Calendar calendar;
        calendar = new GregorianCalendar();
        calendar.setTime(date);
        int start = 0;
        boolean neg = false;
        boolean first = true;
        while (start < source.length() && matcher.find(start)) {
            if (start != matcher.start()) {
                return null;
            }
            String valueGroup = matcher.group(1);
            int value = Integer.parseInt(valueGroup);
            if (first) {
                if (value < 0) {
                    neg = true;
                }
                first = false;
            } else if (value >= 0 && valueGroup.charAt(0) != '+' && neg) {
                // if there is a leading sign, and the current value has no explicit +, it propagates to all
                // other patterns where no sign is specified
                value = -value;
            }
            String field = matcher.group(2);
            String se = matcher.group(3); // get any s=start or e=end
            if (field.equals("d")) {
                calendar.add(Calendar.DAY_OF_MONTH, value);
                // se ignored if day
            } else if (field.equals("m")) {
                calendar.add(Calendar.MONTH, value);
                if (se.equals("s")) {
                    calendar.set(Calendar.DAY_OF_MONTH, 1); // set 1st of month
                } else if (se.equals("e")) {
                    calendar.set(Calendar.DAY_OF_MONTH, 1);  // set 1st of month
                    calendar.add(Calendar.MONTH, 1);         // go to next month
                    calendar.add(Calendar.DAY_OF_MONTH, -1); // back one to to get end of month
                }
            } else if (field.equals("w")) {
                calendar.add(Calendar.WEEK_OF_YEAR, value);
                if (se.equals("s")) {
                    calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY); //set Monday
                } else if (se.equals("e")) {
                    calendar.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY); //set Friday
                }
            } else if (field.equals("q")) {                 // quarter
                calendar.add(Calendar.MONTH, 3 * value);    // move by 3 month blocks
                if (se.equals("s")) {
                    calendar.set(Calendar.DAY_OF_MONTH, 1); // set 1st of month
                    int k = calendar.get(Calendar.MONTH);   // get month (0 = Jan)
                    k = (k / 3) * 3;                        // get month number at start of quarter (0,3,6,9)
                    calendar.set(Calendar.MONTH, k);        // set that month
                } else if (se.equals("e")) {
                    calendar.set(Calendar.DAY_OF_MONTH, 1);  // set 1st of month
                    int k = calendar.get(Calendar.MONTH);    // get month (0 = Jan)
                    k = (k / 3) * 3;                         // get month number at start of quarter (0,3,6,9)
                    calendar.set(Calendar.MONTH, k);         // set that month
                    calendar.add(Calendar.MONTH, 3);         // go to next quarter
                    calendar.add(Calendar.DAY_OF_MONTH, -1); // back one day to to get end of quarter
                }
            } else {
                calendar.add(Calendar.YEAR, value);
                if (se.equals("s")) {
                    calendar.set(Calendar.DAY_OF_MONTH, 1);         // set 1st of month
                    calendar.set(Calendar.MONTH, Calendar.JANUARY); // set January
                } else if (se.equals("e")) {
                    calendar.set(Calendar.DAY_OF_MONTH, 1);         // set 1st of month
                    calendar.set(Calendar.MONTH, Calendar.JANUARY); // set January
                    calendar.add(Calendar.YEAR, 1);                 // go to next year
                    calendar.add(Calendar.DAY_OF_MONTH, -1);        // back one to to get 31 Dec
                }
            }

            start = matcher.end();
        }
        return (start == source.length()) ? calendar.getTime() : null;
    }

}
