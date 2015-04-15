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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package org.openvpms.archetype.i18n.time;

import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


/**
 * An {@link DurationFormatter} that allows multiple formatters to be registered, to handle different durations.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class CompositeDurationFormatter implements DurationFormatter {

    /**
     * The formatters.
     */
    private List<Formatter> formatters = new ArrayList<Formatter>();

    /**
     * The default formatter to use if no formatter is registered.
     */
    private DurationFormatter defaultFormatter = DateDurationFormatter.YEAR;


    /**
     * Sets the default formatter.
     * <p/>
     * This is used if no formatter is registered for a duration.
     *
     * @param formatter the default formatter
     */
    public void setDefaultFormatter(DurationFormatter formatter) {
        if (formatter == null) {
            throw new IllegalArgumentException("Argument 'formatter' is null");
        }
        defaultFormatter = formatter;
    }

    /**
     * Adds a formatter for the specified duration.
     *
     * @param interval  the interval
     * @param units     the interval units
     * @param formatter the formatter
     */
    public void add(int interval, DateUnits units, DurationFormatter formatter) {
        formatters.add(new Formatter(interval, units, formatter));
    }

    /**
     * Formats the duration between two timestamps.
     *
     * @param from the starting time
     * @param to   the ending time
     * @return the formatted duration
     */
    public String format(final Date from, Date to) {
        DurationFormatter formatter = null;
        List<Formatter> l = new ArrayList<Formatter>(formatters);
        Collections.sort(l, new Comparator<Formatter>() {
            public int compare(Formatter o1, Formatter o2) {
                return o1.getTo(from).compareTo(o2.getTo(from));
            }
        });
        for (Formatter f : l) {
            formatter = f.formatter;
            if (f.getTo(from).compareTo(to) >= 0) {
                break;
            }
        }
        if (formatter == null) {
            formatter = defaultFormatter;
        }
        return formatter.format(from, to);
    }

    private static class Formatter {

        private final int interval;
        private final DateUnits units;
        private final DurationFormatter formatter;

        public Formatter(int interval, DateUnits units, DurationFormatter formatter) {
            this.interval = interval;
            this.units = units;
            this.formatter = formatter;
        }

        public Date getTo(Date from) {
            return DateRules.getDate(from, interval, units);
        }

    }
}
