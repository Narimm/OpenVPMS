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

package org.openvpms.web.component.property;

import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.resource.i18n.format.DateFormatter;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


/**
 * Handler for time nodes.
 *
 * @author Tim Anderson
 */
public class TimePropertyTransformer extends AbstractDateTimePropertyTransformer {

    /**
     * The default minimum: 00:00.
     */
    public static final Date MIN_DATE;

    /**
     * The default maximum: 24:00.
     */
    public static final Date MAX_DATE;

    /**
     * The date component of the time. May be {@code null}
     */
    private Date date;

    static {
        Calendar calendar = new GregorianCalendar();
        calendar.clear();         // get rid of date, timezone
        MIN_DATE = calendar.getTime();
        MAX_DATE = DateRules.getDate(MIN_DATE, 1, DateUnits.DAYS);
    }

    /**
     * Construct a {@link TimePropertyTransformer} with no minimum or maximum date.
     *
     * @param property the property
     */
    public TimePropertyTransformer(Property property) {
        this(property, null, null);
    }

    /**
     * Constructs a {@code AbstractDateTimePropertyTransformer}.
     *
     * @param property the property
     * @param min      the minimum value for the date. If {@code null}, the date has no minimum
     * @param max      the maximum value for the date. If {@code null}, the date has no maximum
     */
    public TimePropertyTransformer(Property property, Date min, Date max) {
        super(property, min, max, Format.DATE_TIME);
    }

    /**
     * Sets the date part of the time.
     *
     * @param date the date. May be {@code null}
     */
    public void setDate(Date date) {
        this.date = (date != null) ? DateRules.getDate(date) : null;
    }

    /**
     * Returns the supplied value as a date/time.
     *
     * @param value the time
     * @return the date/time
     */
    protected Date getDateTime(Date value) {
        Date result;
        if (date != null) {
            result = DateRules.addDateTime(date, value);
        } else {
            result = value;
        }
        return result;
    }

    /**
     * Converts the supplied value to a date/time.
     * <p/>
     * This implementation expects the value to be a time, which is added to the current date, using
     * {@link #addTime}.
     *
     * @param value the time string
     * @return the date/time
     * @throws ParseException if the value can't be parsed as a date/time
     */
    protected Date getDateTime(String value) throws ParseException {
        return addTime(value);
    }

    /**
     * Returns the date.
     *
     * @return the date. May be {@code null}
     */
    protected Date getDate() {
        return date;
    }

    /**
     * Parses a time string.
     *
     * @param value the value to parse
     * @return the date/time
     * @throws ParseException if the value can't be parsed as a time
     */
    @Override
    protected Date parseTime(String value) throws ParseException {
        return DateFormatter.parseTime(value, true);
    }

    /**
     * Verifies that the date falls into the acceptable date range.
     *
     * @param date the date to check
     * @param min  the minimum date, or {@code null} if there is no minimum
     * @param max  the maximum date, or {@code null} if there is no maximum
     */
    protected void checkDateRange(Date date, Date min, Date max) {
        if (min != null && date.getTime() < min.getTime()) {
            String formatDate = DateFormatter.formatTimeDiff(MIN_DATE, date);
            String formatMin = DateFormatter.formatTimeDiff(MIN_DATE, min);
            String msg = Messages.format("property.error.minTime", formatDate, formatMin);
            throw new PropertyException(getProperty(), msg);
        }
        if (max != null && date.getTime() > max.getTime()) {
            String formatDate = DateFormatter.formatTimeDiff(MIN_DATE, date);
            String formatMax = DateFormatter.formatTimeDiff(MIN_DATE, max);
            String msg = Messages.format("property.error.maxTime", formatDate, formatMax);
            throw new PropertyException(getProperty(), msg);
        }
    }

}
