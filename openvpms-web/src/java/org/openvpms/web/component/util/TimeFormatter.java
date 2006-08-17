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

import nextapp.echo2.app.ApplicationInstance;
import org.apache.commons.lang.StringUtils;
import org.openvpms.web.resource.util.Messages;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * Helper to format times.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-04-11 04:09:07Z $
 */
public class TimeFormatter {

    /**
     * Time edit pattern.
     */
    private static final String EDIT_PATTERN;

    /**
     * Time view pattern.
     */
    private static final String VIEW_PATTERN;

    /**
     * Format a date.
     *
     * @param time the date to format
     * @param edit if <code>true</code> format the number for editing
     * @return the formatted date
     */
    public static String format(Date time, boolean edit) {
        return getFormat(edit).format(time);
    }

    /**
     * Returns a time format.
     *
     * @param edit if <code>true</code> return a format for editing otherwise
     *             return a format for viewing dates
     * @return a date format
     */
    public static DateFormat getFormat(boolean edit) {
        DateFormat format;
        Locale locale = ApplicationInstance.getActive().getLocale();
        String pattern = (edit) ? EDIT_PATTERN : VIEW_PATTERN;
        if (pattern == null) {
            if (edit) {
                // specify SHORT style when parsing, so that 2 digit years
                // are handled correctly
                format = DateFormat.getTimeInstance(DateFormat.SHORT, locale);
            } else {
                format = DateFormat.getTimeInstance(DateFormat.MEDIUM, locale);
            }
        } else {
            format = new SimpleDateFormat(pattern, locale);
        }
        return format;
    }

    static {
        String edit = Messages.get("time.format.edit", true);
        EDIT_PATTERN = (!StringUtils.isEmpty(edit)) ? edit : null;

        String view = Messages.get("time.format.view", true);
        VIEW_PATTERN = (!StringUtils.isEmpty(view)) ? view : null;
    }
}
