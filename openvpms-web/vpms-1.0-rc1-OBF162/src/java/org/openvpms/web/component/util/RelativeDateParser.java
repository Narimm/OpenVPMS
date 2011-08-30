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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.util;

import org.apache.commons.lang.StringUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Relative date parser.
 * <p/>
 * Recognises strings of the form: <code>([-](0..9)+(d|m|w|y))+</code>
 * as being relative to the current date. E.g:
 * <ul>
 * <li><em>-10w3d<em> sets the date to 10 weeks and 3 days before the current
 * date</li>
 * <li><em>5y3m</em> sets the date to 5 year sand 3 montsh from the current
 * date</li>
 * </ul>
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class RelativeDateParser {

    /**
     * The pattern.
     */
    private final Pattern pattern
            = Pattern.compile("\\s*(-?\\d+)([dmwy])\\s*");


    /**
     * Parses a date relative to the current time.
     *
     * @param source the relative date string
     * @return the parsed date, or <code>null</code> if the source is invalid
     */
    public Date parse(String source) {
        return parse(source, new Date());
    }

    /**
     * Parses a date relative to the specified date.
     *
     * @param source the relative date string
     * @param date   the date
     * @return the relative date, or <code>null</code> if the source is invalid
     */
    public Date parse(String source, Date date) {
        if (StringUtils.isEmpty(source)) {
            return null;
        }
        Matcher matcher = pattern.matcher(source.toLowerCase());
        Calendar calendar;
        calendar = new GregorianCalendar();
        calendar.setTime(date);
        int start = 0;
        while (start < source.length() && matcher.find(start)) {
            if (start != matcher.start()) {
                return null;
            }
            int value = Integer.parseInt(matcher.group(1));
            String field = matcher.group(2);
            if (field.equals("d")) {
                calendar.add(Calendar.DAY_OF_MONTH, value);
            } else if (field.equals("m")) {
                calendar.add(Calendar.MONTH, value);
            } else if (field.equals("w")) {
                calendar.add(Calendar.WEEK_OF_YEAR, value);
            } else {
                calendar.add(Calendar.YEAR, value);
            }

            start = matcher.end();
        }
        return (start == source.length()) ? calendar.getTime() : null;
    }

}
