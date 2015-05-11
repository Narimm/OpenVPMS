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

package org.openvpms.web.workspace.workflow.appointment.repeat;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.quartz.CronExpression;

import java.text.ParseException;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simplified cron expression.
 *
 * @author Tim Anderson
 */
public class CronRepeatExpression implements RepeatExpression {

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
     * The underlying Cron expression.
     */
    private CronExpression expression;


    /**
     * Constructs a {@link CronRepeatExpression}.
     *
     * @param startTime the time the expression starts
     * @param dayOfWeek the day-of-week
     */
    public CronRepeatExpression(Date startTime, DayOfWeek dayOfWeek) {
        this(startTime, DayOfMonth.NO_VALUE, Month.ALL, dayOfWeek, Type.CUSTOM);
    }

    /**
     * Constructs a {@link CronRepeatExpression}.
     *
     * @param startTime the time the expression starts
     * @param month     the month
     * @param dayOfWeek the day-of-week
     */
    public CronRepeatExpression(Date startTime, Month month, DayOfWeek dayOfWeek) {
        this(startTime, DayOfMonth.NO_VALUE, month, dayOfWeek, Type.CUSTOM);
    }

    /**
     * Constructs a {@link CronRepeatExpression}.
     *
     * @param startTime  the time the expression starts
     * @param dayOfMonth the day-of-month
     * @param month      the month
     * @param dayOfWeek  the day-of-week
     * @param type       the expression type
     */
    private CronRepeatExpression(Date startTime, DayOfMonth dayOfMonth, Month month, DayOfWeek dayOfWeek, Type type) {
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
     * Constructs a {@link CronRepeatExpression}.
     *
     * @param minute     the minute the expression starts
     * @param hour       the hour the expression starts
     * @param dayOfMonth the day-of-month
     * @param month      the month
     * @param dayOfWeek  the day-of-week
     * @param type       the expression type
     */
    private CronRepeatExpression(int minute, int hour, DayOfMonth dayOfMonth, Month month, DayOfWeek dayOfWeek,
                                 Type type) {
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
    public DayOfMonth getDayOfMonth() {
        return dayOfMonth;
    }

    /**
     * Returns the month field.
     *
     * @return the month field
     */
    public Month getMonth() {
        return month;
    }

    /**
     * Returns the day-of-week field.
     *
     * @return the the day-of-week field
     */
    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
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
     * Returns the next repeat time after the specified time.
     *
     * @param time      the time
     * @param condition the condition to evaluate for each date
     * @return the next repeat time, or {@code null} if there are no more repeats, or the predicate returns
     *         {@code false}
     */
    @Override
    public Date getRepeatAfter(Date time, Predicate<Date> condition) {
        Date result = null;
        if (expression == null) {
            try {
                expression = new CronExpression(getExpression());
            } catch (ParseException exception) {
                // do nothing
            }
        }
        if (expression != null) {
            time = expression.getNextValidTimeAfter(time);
            result = (time != null && condition.evaluate(time)) ? time : null;
        }
        return result;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof CronRepeatExpression) {
            CronRepeatExpression other = (CronRepeatExpression) obj;
            return ObjectUtils.equals(minutes, other.minutes) && ObjectUtils.equals(hours, other.hours)
                   && ObjectUtils.equals(dayOfMonth, other.dayOfMonth) && ObjectUtils.equals(month, other.month)
                   && ObjectUtils.equals(dayOfWeek, other.dayOfWeek);
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
        HashCodeBuilder builder = new HashCodeBuilder();
        return builder.append(minutes).append(hours).append(dayOfMonth).append(month).append(dayOfWeek).hashCode();
    }

    /**
     * Parses a Cron expression.
     *
     * @param expression the expression
     * @return the parsed expression, or {@code null} if the expression is invalid
     */
    public static CronRepeatExpression parse(String expression) {
        CronRepeatExpression result;
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
     * Helper to create a weekday Cron expression.
     *
     * @param startTime the expression start time
     * @return a new Cron expression
     */
    public static RepeatExpression weekdays(Date startTime) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(startTime);
        return new CronRepeatExpression(startTime, DayOfMonth.NO_VALUE, Month.ALL, DayOfWeek.WEEKDAYS, Type.WEEKDAYS);
    }

    private static CronRepeatExpression parse(int minute, int hour, String dayOfMonth, String month, String dayOfWeek)
            throws ParseException {
        CronRepeatExpression result;
        DayOfMonth dom = new DayOfMonth(dayOfMonth);
        Month m = Month.parse(month);
        DayOfWeek dow = DayOfWeek.parse(dayOfWeek);
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
        result = new CronRepeatExpression(minute, hour, dom, m, dow, type);
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

    public static class DayOfMonth extends Field {

        public static DayOfMonth ALL = new DayOfMonth("*");

        public static DayOfMonth NO_VALUE = new DayOfMonth("?");

        public DayOfMonth(String value) {
            super(value);
        }

        public boolean singleDay() {
            return StringUtils.isNumeric(value);
        }
    }

    public static class Month extends Field {

        public static Month ALL = new Month("*");
        private int interval = -1;

        private static final Pattern INTERVAL_PATTERN = Pattern.compile("\\*\\/(\\d+)");

        public Month(int interval) {
            this("*/" + interval);
            this.interval = interval;
        }

        public Month(String value) {
            super(value);
        }

        public boolean singleMonth() {
            return StringUtils.isNumeric(value);
        }

        public int getInterval() {
            return interval;
        }

        public static Month every(int interval) {
            return new Month(interval);
        }

        public static Month parse(String value) {
            Matcher matcher = INTERVAL_PATTERN.matcher(value);
            if (matcher.matches()) {
                int interval = Integer.parseInt(matcher.group(1));
                return new Month(interval);
            }
            return new Month(value);
        }

    }

    public static class DayOfWeek extends Field {

        public static final DayOfWeek NO_VALUE;
        public static final DayOfWeek ALL;
        public static final DayOfWeek WEEKDAYS;

        public static final int LAST = -1;

        private final int ordindal;

        private static final String SUN = "SUN";

        private static final String MON = "MON";

        private static final String TUE = "TUE";

        private static final String WED = "WED";

        private static final String THU = "THU";

        private static final String FRI = "FRI";

        private static final String SAT = "SAT";

        private static final BidiMap<String, Integer> DAY_ID = new DualHashBidiMap<String, Integer>();

        private static final BidiMap<Integer, String> ID_DAY;

        static {
            DAY_ID.put(SUN, Calendar.SUNDAY);
            DAY_ID.put(MON, Calendar.MONDAY);
            DAY_ID.put(TUE, Calendar.TUESDAY);
            DAY_ID.put(WED, Calendar.WEDNESDAY);
            DAY_ID.put(THU, Calendar.THURSDAY);
            DAY_ID.put(FRI, Calendar.FRIDAY);
            DAY_ID.put(SAT, Calendar.SATURDAY);
            ID_DAY = DAY_ID.inverseBidiMap();
            NO_VALUE = new DayOfWeek("?");
            ALL = new DayOfWeek("*");
            WEEKDAYS = new DayOfWeek("MON-FRI");

        }

        private final BitSet set;


        private DayOfWeek(String value) {
            this(value, new BitSet(Calendar.SATURDAY));
        }


        private DayOfWeek(String value, BitSet set) {
            super(value);
            this.set = set;
            ordindal = 0;
        }

        public DayOfWeek(List<String> days) {
            super(StringUtils.join(days.iterator(), ","));
            set = new BitSet(Calendar.SATURDAY);
            for (String day : days) {
                set.set(getDay(day), true);
            }
            ordindal = 0;
        }

        public DayOfWeek(String day, int ordinal) {
            super(ordinal == LAST ? day + "L" : day + "#" + ordinal);
            set = new BitSet(Calendar.SATURDAY);
            set.set(getDay(day));
            this.ordindal = ordinal;
        }

        public int getOrdindal() {
            return ordindal;
        }

        public String getDay() {
            if (set.cardinality() == 1) {
                return ID_DAY.get(set.nextSetBit(0));
            }
            return null;
        }

        public static DayOfWeek lastDay(String day) {
            return new DayOfWeek(day + "L");
        }

        public static DayOfWeek parse(String value) throws ParseException {
            if (value.contains("-")) {
                BitSet set = new BitSet(Calendar.SATURDAY);
                String[] parts = value.split("-");
                if (parts.length == 2) {
                    int start = getDay(parts[0]);
                    int end = getDay(parts[1]);
                    set.set(start, end, true);
                } else {
                    throw new IllegalArgumentException("Invalid day of week: " + value);
                }
                return new DayOfWeek(value, set);
            } else if (value.contains(",")) {
                BitSet set = new BitSet(Calendar.SATURDAY);
                String[] parts = value.split(",");
                for (String part : parts) {
                    set.set(getDay(part), true);
                }
                return new DayOfWeek(value, set);
            } else if (value.contains("#")) {
                String[] parts = value.split("#");
                if (parts.length == 2) {
                    int nth = parseInt(parts[1], 1, 4, "count");
                    return new DayOfWeek(parts[0], nth);
                } else {
                    throw new IllegalArgumentException("Invalid day of week: " + value);
                }
            } else if (value.endsWith("L")) {
                value = value.substring(0, value.length() - 1);
                return new DayOfWeek(value, LAST);
            }
            return new DayOfWeek(value);
        }


        /**
         * Returns the Cron day name for a day of week.
         *
         * @param dayOfWeek the day name
         * @return the day name
         */
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

        public boolean weekDays() {
            return set.get(Calendar.MONDAY, Calendar.FRIDAY + 1).cardinality() == 5 &&
                   !(set.get(Calendar.SUNDAY) || set.get(Calendar.SATURDAY));
        }


        @Override
        public boolean isAll() {
            return super.isAll() || set.cardinality() == 7;
        }

        public boolean isSelected(int day) {
            return set.get(day);
        }

        public boolean singleDay() {
            return set.cardinality() == 1;
        }

        private static int getDay(String value) {
            Integer result = DAY_ID.get(value.toUpperCase());
            if (result == null) {
                throw new IllegalArgumentException("Invalid day: " + value);
            }
            return result;
        }

        public boolean isOrdinal() {
            return ordindal > 0 || ordindal == LAST;
        }
    }

}
