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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.appointment;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.text.ParseException;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Simplified cron expression.
 *
 * @author Tim Anderson
 */
public class CronExpression {

    public enum Type {
        DAILY, WEEKDAYS, WEEKLY, MONTHLY, YEARLY, CUSTOM
    }

    /**
     * The minutes field.
     */
    private final String minutes;

    /**
     * The hours field.
     */
    private final String hours;

    /**
     * The day-of-month field.
     */
    private final DayOfMonth dayOfMonth;

    /**
     * The month field.
     */
    private final Month month;

    /**
     * The day-of-week field.
     */
    private final DayOfWeek dayOfWeek;

    /**
     * The type of the expression.
     */
    private final Type type;

    /**
     * Constructs a {@link CronExpression}.
     *
     * @param startTime  the time the expression starts
     * @param dayOfMonth the day-of-month
     * @param month      the month
     * @param dayOfWeek  the day-of-week
     * @param type       the expression type
     */
    private CronExpression(Date startTime, DayOfMonth dayOfMonth, Month month, DayOfWeek dayOfWeek, Type type) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(startTime);
        minutes = "" + calendar.get(Calendar.MINUTE);
        hours = "" + calendar.get(Calendar.HOUR_OF_DAY);
        this.dayOfMonth = dayOfMonth;
        this.month = month;
        this.dayOfWeek = dayOfWeek;
        this.type = type;
    }

    /**
     * Constructs a {@link CronExpression}.
     *
     * @param minute     the minute the expression starts
     * @param hour       the hour the expression starts
     * @param dayOfMonth the day-of-month
     * @param month      the month
     * @param dayOfWeek  the day-of-week
     * @param type       the expression type
     */
    private CronExpression(int minute, int hour, DayOfMonth dayOfMonth, Month month, DayOfWeek dayOfWeek, Type type) {
        this.minutes = "" + minute;
        this.hours = "" + hour;
        this.dayOfMonth = dayOfMonth;
        this.month = month;
        this.dayOfWeek = dayOfWeek;
        this.type = type;
    }

    /**
     * Returns the day-of-month field.
     *
     * @return the day-of-month field
     */
    public String getDayOfMonth() {
        return dayOfMonth.value;
    }

    /**
     * Returns the month field.
     *
     * @return the month field
     */
    public String getMonth() {
        return month.value;
    }

    /**
     * Returns the day-of-week field.
     *
     * @return the the day-of-week field
     */
    public String getDayOfWeek() {
        return dayOfWeek.value;
    }

    /**
     * Returns the type of the expression.
     *
     * @return the type
     */
    public Type getType() {
        return type;
    }

    /**
     * Returns the Cron expression.
     *
     * @return the Cron expression
     */
    public String getExpression() {
        StringBuilder result = new StringBuilder();
        result.append("0 ");
        result.append(minutes);
        result.append(" ");
        result.append(hours);
        result.append(" ");
        result.append(dayOfMonth.value);
        result.append(" ");
        result.append(month.value);
        result.append(" ");
        result.append(dayOfWeek.value);
        return result.toString();
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof CronExpression) {
            CronExpression other = (CronExpression) obj;
            return ObjectUtils.equals(minutes, other.minutes) && ObjectUtils.equals(hours, other.hours)
                   && ObjectUtils.equals(dayOfMonth, other.dayOfMonth) && ObjectUtils.equals(month, other.month)
                   && ObjectUtils.equals(dayOfWeek, other.dayOfWeek);
        }
        return false;
    }

    /**
     * Returns a hash code value for the object. This method is
     * supported for the benefit of hash tables such as those provided by
     * {@link HashMap}.
     *
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder();
        return builder.append(minutes).append(hours).append(dayOfMonth).append(month).append(dayOfWeek).hashCode();
    }

    /**
     * Parses a Cron expression.
     *
     * @param expression the expression
     * @return the parsed expression, or {@code null} if the expression is invalid
     */
    public static CronExpression parse(String expression) {
        CronExpression result;
        String[] parts = expression.split(" ");
        if (parts.length < 5) {
            return null;
        }
        try {
            int minute = parseInt(parts[1], 0, 59, "minutes");
            int hour = parseInt(parts[2], 0, 23, "hours");
            String dayOfMonth = parts[3];
            String month = parts[4];
            String dayOfWeek = parts[5];
            result = parse(minute, hour, dayOfMonth, month, dayOfWeek);
        } catch (ParseException exception) {
            return null;
        }
        return result;
    }

    /**
     * Parses a Cron expression.
     *
     * @param startTime  the expression start time
     * @param dayOfMonth the day-of-month field
     * @param month      the month field
     * @param dayOfWeek  the day-of-week field
     * @return the parsed expression, or {@code null} if the expression is invalid
     */
    public static CronExpression parse(Date startTime, String dayOfMonth, String month, String dayOfWeek)
            throws ParseException {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(startTime);
        int minute = calendar.get(Calendar.MINUTE);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        return parse(minute, hour, dayOfMonth, month, dayOfWeek);
    }

    /**
     * Helper to create a daily Cron expression.
     *
     * @param startTime the expression start time
     * @return a new Cron expression
     */
    public static CronExpression daily(Date startTime) {
        return new CronExpression(startTime, DayOfMonth.ALL, Month.ALL, DayOfWeek.NO_VALUE, Type.DAILY);
    }

    /**
     * Helper to create a weekday Cron expression.
     *
     * @param startTime the expression start time
     * @return a new Cron expression
     */
    public static CronExpression weekdays(Date startTime) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(startTime);
        return new CronExpression(startTime, DayOfMonth.NO_VALUE, Month.ALL, DayOfWeek.WEEKDAYS, Type.WEEKDAYS);
    }

    /**
     * Helper to create a weekly Cron expression.
     *
     * @param startTime the expression start time
     * @return a new Cron expression
     */
    public static CronExpression weekly(Date startTime) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(startTime);
        String dayOfWeek = DayOfWeek.getDay(calendar.get(Calendar.DAY_OF_WEEK));
        return new CronExpression(startTime, DayOfMonth.NO_VALUE, Month.ALL, new DayOfWeek(dayOfWeek), Type.WEEKLY);
    }

    /**
     * Helper to create a monthly Cron expression.
     *
     * @param startTime the expression start time
     * @return a new Cron expression
     */
    public static CronExpression monthly(Date startTime) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(startTime);
        String dayOfMonth = "" + calendar.get(Calendar.DAY_OF_MONTH);
        return new CronExpression(startTime, new DayOfMonth(dayOfMonth), Month.ALL, DayOfWeek.NO_VALUE, Type.MONTHLY);
    }

    /**
     * Helper to create a yearly Cron expression.
     *
     * @param startTime the expression start time
     * @return a new Cron expression
     */
    public static CronExpression yearly(Date startTime) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(startTime);
        String dayOfMonth = "" + calendar.get(Calendar.DAY_OF_MONTH);
        String month = "" + calendar.get(Calendar.MONTH) + 1;
        return new CronExpression(startTime, new DayOfMonth(dayOfMonth), new Month(month), DayOfWeek.NO_VALUE,
                                  Type.YEARLY);
    }

    private static CronExpression parse(int minute, int hour, String dayOfMonth, String month, String dayOfWeek) {
        CronExpression result;
        DayOfMonth dom = new DayOfMonth(dayOfMonth);
        Month m = new Month(month);
        DayOfWeek dow = new DayOfWeek(dayOfWeek);
        boolean allDayOfMonth = dom.isAll();
        boolean allMonth = m.isAll();
        boolean allDayOfWeek = dow.isAll();

        Type type;
        if (allDayOfMonth && allMonth && allDayOfWeek) {
            type = Type.DAILY;
        } else if (allDayOfMonth && allMonth && dow.weekDays()) {
            type = Type.WEEKDAYS;
        } else if (allDayOfMonth && allMonth && dow.singleDay()) {
            type = Type.WEEKLY;
        } else if (dom.singleDay() && allMonth && allDayOfWeek) {
            type = Type.MONTHLY;
        } else if (dom.singleDay() && m.singleMonth() && allDayOfWeek) {
            type = Type.YEARLY;
        } else {
            type = Type.CUSTOM;
        }
        result = new CronExpression(minute, hour, dom, m, dow, type);
        return result;
    }

    private static int parseInt(String string, int min, int max, String displayName) throws ParseException {
        int value;
        try {
            value = Integer.valueOf(string);
            if (value < min || value > max) {
                throw new ParseException("Invalid " + displayName + ": " + string, 0);
            }
        } catch (NumberFormatException exception) {
            throw new ParseException("Invalid " + displayName + ": " + string, 0);
        }
        return value;
    }

    private static class Field {
        protected final String value;

        public Field(String value) {
            this.value = value;
        }

        public boolean isAll() {
            return "*".equals(value) || "?".equals(value);
        }

        /**
         * Indicates whether some other object is "equal to" this one.
         *
         * @param obj the reference object with which to compare.
         * @return {@code true} if this object is the same as the obj
         *         argument; {@code false} otherwise.
         * @see #hashCode()
         * @see HashMap
         */
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Field) {
                Field other = (Field) obj;
                return ObjectUtils.equals(value, other.value);
            }
            return false;
        }

        /**
         * Returns a hash code value for the object.
         *
         * @return a hash code value for this object.
         */
        @Override
        public int hashCode() {
            return value.hashCode();
        }
    }

    private static class DayOfMonth extends Field {

        public static DayOfMonth ALL = new DayOfMonth("*");

        public static DayOfMonth NO_VALUE = new DayOfMonth("?");

        public DayOfMonth(String value) {
            super(value);
        }

        public boolean singleDay() {
            return StringUtils.isNumeric(value);
        }
    }

    private static class Month extends Field {

        public static Month ALL = new Month("*");

        public Month(String value) {
            super(value);
        }

        public boolean singleMonth() {
            return StringUtils.isNumeric(value);
        }
    }

    private static class DayOfWeek extends Field {

        public static final DayOfWeek NO_VALUE;
        public static final DayOfWeek ALL;
        public static final DayOfWeek WEEKDAYS;

        private static final String SUN = "SUN";

        private static final String MON = "MON";

        private static final String TUE = "TUE";

        private static final String WED = "WED";

        private static final String THU = "THU";

        private static final String FRI = "FRI";

        private static final String SAT = "SAT";

        private static final Map<String, Integer> DAYS = new HashMap<String, Integer>(60);

        static {
            DAYS.put(SUN, Calendar.SUNDAY);
            DAYS.put(MON, Calendar.MONDAY);
            DAYS.put(TUE, Calendar.TUESDAY);
            DAYS.put(WED, Calendar.WEDNESDAY);
            DAYS.put(THU, Calendar.THURSDAY);
            DAYS.put(FRI, Calendar.FRIDAY);
            DAYS.put(SAT, Calendar.SATURDAY);
            NO_VALUE = new DayOfWeek("?");
            ALL = new DayOfWeek("*");
            WEEKDAYS = new DayOfWeek("MON-FRI");
        }

        private BitSet set = new BitSet(Calendar.SATURDAY);


        public static String getDay(int dayOfWeek) {
            switch (dayOfWeek) {
                case Calendar.SUNDAY:
                    return SUN;
                case Calendar.MONDAY:
                    return MON;
                case Calendar.TUESDAY:
                    return TUE;
                case Calendar.WEDNESDAY:
                    return WED;
                case Calendar.THURSDAY:
                    return THU;
                case Calendar.FRIDAY:
                    return FRI;
                case Calendar.SATURDAY:
                    return SAT;
            }
            return null;
        }

        public DayOfWeek(String value) {
            super(value);
            if (value.contains("-")) {
                String[] parts = value.split("-");
                if (parts.length == 2) {
                    int start = getDay(parts[0]);
                    int end = getDay(parts[1]);
                    set.set(start, end, true);
                } else {
                    throw new IllegalArgumentException("Invalid day of week: " + value);
                }
            } else if (value.contains(",")) {
                String[] parts = value.split(",");
                for (String part : parts) {
                    set.set(getDay(part), true);
                }
            }
        }

        private int getDay(String value) {
            Integer result = DAYS.get(value.toUpperCase());
            if (result == null) {
                throw new IllegalArgumentException("Invalid day: " + value);
            }
            return result;
        }

        public boolean weekDays() {
            return set.get(Calendar.MONDAY, Calendar.FRIDAY + 1).cardinality() == 5 &&
                   !(set.get(Calendar.SUNDAY) || set.get(Calendar.SATURDAY));
        }


        @Override
        public boolean isAll() {
            return super.isAll() || set.cardinality() == 7;
        }

        public boolean singleDay() {
            return set.cardinality() == 1;
        }
    }

}
