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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
     * Cron day-of-month field.
     */
    public static class DayOfMonth extends Field {

        /**
         * Pattern denoting no specified value.
         */
        public static DayOfMonth NO_VALUE = new DayOfMonth("?");

        /**
         * The selected days.
         */
        private final BitSet set;

        /**
         * Determines if the last day of the month is selected.
         */
        private final boolean last;

        /**
         * Constructs a {@link DayOfMonth}.
         *
         * @param value the field value
         */
        private DayOfMonth(String value) {
            this(value, new BitSet(), false);
        }

        /**
         * Constructs a {@link DayOfMonth}.
         *
         * @param value the field value
         * @param set   the selected days
         * @param last  if {@code true}, the last day of the month is selected
         */
        private DayOfMonth(String value, BitSet set, boolean last) {
            super(value);
            this.set = set;
            this.last = last;
        }

        /**
         * Constructs a {@link DayOfMonth}.
         *
         * @param day the selected day
         */
        private DayOfMonth(int day) {
            this("" + day, new BitSet(), false);
            set.set(day, true);
        }

        /**
         * Constructs a {@link DayOfMonth}.
         *
         * @param days the selected days
         * @param last if {@code true}, the last day of the month is selected
         */
        private DayOfMonth(List<Integer> days, boolean last) {
            this(format(days, last), new BitSet(), last);
            for (int day : days) {
                set.set(day, true);
            }
        }

        /**
         * Determines if the field refers to a single day.
         *
         * @return {@code true} if the field refers to a single day
         */
        public boolean singleDay() {
            return set.cardinality() == 1;
        }

        /**
         * Returns the first selected day.
         *
         * @return the first selected day, or {@code -1} if no day is selected
         */
        public int day() {
            return set.nextSetBit(0);
        }

        /**
         * Determines if a day is selected.
         *
         * @param day the day
         * @return {@code true} if the day is selected
         */
        public boolean isSelected(int day) {
            return set.get(day);
        }

        /**
         * Determines if the last day of the month is selected.
         *
         * @return {@code true} if the last of the month is selected
         */
        public boolean hasLast() {
            return last;
        }

        /**
         * Creates a day of month field.
         *
         * @param days the selected days
         * @param last if {@code true}, select the last day of the month
         * @return a new day of month field
         */
        public static DayOfMonth days(List<Integer> days, boolean last) {
            return new DayOfMonth(days, last);
        }

        /**
         * Creates a day of month field for a single day.
         *
         * @param day the selected day
         * @return a new day of month field
         */
        public static DayOfMonth day(int day) {
            return new DayOfMonth(day);
        }

        /**
         * Parses a {@link DayOfMonth} from a Cron day-of-month pattern.
         *
         * @param value the pattern to parse
         * @return a new {@link DayOfMonth}
         */
        public static DayOfMonth parse(String value) {
            boolean last = false;
            BitSet set = new BitSet();
            if (value.contains("-")) {
                String[] parts = value.split("-");
                if (parts.length == 2) {
                    int start = Integer.parseInt(parts[0]);
                    int end = Integer.parseInt(parts[1]);
                    set.set(start, end + 1, true);
                } else {
                    throw new IllegalArgumentException("Invalid day of month: " + value);
                }
            } else if (value.contains(",")) {
                String[] parts = value.split(",");
                for (String part : parts) {
                    if ("L".equals(part)) {
                        last = true;
                    } else {
                        set.set(Integer.parseInt(part), true);
                    }
                }
            } else if (StringUtils.isNumeric(value)) {
                set.set(Integer.parseInt(value), true);
            } else if ("L".equals(value)) {
                last = true;
            }
            return new DayOfMonth(value, set, last);
        }

        /**
         * Formats a field for a list of selected days.
         *
         * @param days the selected days
         * @param last if {@code true}, the last day of the month is selected
         * @return a formatted field
         */
        private static String format(List<Integer> days, boolean last) {
            String result = StringUtils.join(days.iterator(), ',');
            if (last) {
                if (result.length() != 0) {
                    result = result + ",";
                }
                result = result + "L";
            }
            return result;
        }
    }

    /**
     * Cron month field.
     */
    public static class Month extends Field {

        /**
         * Pattern denoting all months.
         */
        public static Month ALL = new Month("*");

        /**
         * The selected months.
         */
        private final BitSet set;

        /**
         * The repeat interval, or {@code -1} if none is specified
         */
        private final int interval;

        /**
         * Pattern to match month intervals.
         */
        private static final Pattern MONTH_INTERVAL = Pattern.compile("(\\d+)\\/(\\d+)");


        /**
         * Constructs a {@link Month}.
         *
         * @param month    the month
         * @param interval the interval, or {@code -1} if there is no interval
         */
        private Month(int month, int interval) {
            this((interval != -1) ? "" + month + "/" + interval : "" + month, interval);
            if (month != -1) {
                set.set(month, true);
            }
        }

        /**
         * Constructs a {@link Month}.
         *
         * @param value the pattern
         */
        private Month(String value) {
            this(value, -1);
        }

        /**
         * Constructs an {@link Month}.
         *
         * @param value    the pattern
         * @param interval the interval, or {@code -1} if there is no interval
         */
        private Month(String value, int interval) {
            super(value);
            this.set = new BitSet();
            this.interval = interval;
        }

        /**
         * Determines if the pattern is for a single month.
         *
         * @return {@code true} if the pattern is for a single month
         */
        public boolean singleMonth() {
            return StringUtils.isNumeric(value);
        }

        /**
         * Returns the interval.
         *
         * @return the interval, or {@code -1} if none is specified
         */
        public int getInterval() {
            return interval;
        }

        /**
         * Returns the first month.
         *
         * @return the first month, in the range 1..12, or {@code -1} if no month is selected
         */
        public int month() {
            return set.nextSetBit(0);
        }

        /**
         * Creates a new month.
         *
         * @param month the month, in the range 1..12.
         * @return a new month
         */
        public static Month month(int month) {
            return new Month(month, -1);
        }

        /**
         * Creates a new month interval.
         *
         * @param month    the start month
         * @param interval the interval
         * @return a new month interval
         */
        public static Month every(int month, int interval) {
            return new Month(month, interval);
        }

        /**
         * Parses a {@link Month} from a Cron month pattern.
         *
         * @param value the pattern to parse
         * @return a new {@link Month}
         */
        public static Month parse(String value) {
            Matcher matcher = MONTH_INTERVAL.matcher(value);
            if (matcher.matches()) {
                int month = Integer.parseInt(matcher.group(1));
                int interval = Integer.parseInt(matcher.group(2));
                return new Month(month, interval);
            } else if (StringUtils.isNumeric(value)) {
                return new Month(Integer.parseInt(value), -1);
            }
            return new Month(value);
        }
    }

    /**
     * Cron day-of-week field.
     */
    public static class DayOfWeek extends Field {

        /**
         * Pattern denoting no specified day-of-week.
         */
        public static final DayOfWeek NO_VALUE;

        /**
         * Pattern denoting all days of the week.
         */
        public static final DayOfWeek ALL;

        /**
         * Pattern denoting weekdays.
         */
        public static final DayOfWeek WEEKDAYS;

        /**
         * Constant indicating the last occurrence of a day in the month.
         */
        public static final int LAST = -1;

        /**
         * The selected days.
         */
        private final BitSet set;

        /**
         * Indicates the first..fifth or last occurrence of a day.
         */
        private final int ordindal;

        /**
         * Cron constant for Sunday.
         */
        private static final String SUN = "SUN";

        /**
         * Cron constant for Monday.
         */
        private static final String MON = "MON";

        /**
         * Cron constant for Tuesday.
         */
        private static final String TUE = "TUE";

        /**
         * Cron constant for Wednesday.
         */
        private static final String WED = "WED";

        /**
         * Cron constant for Thursday.
         */
        private static final String THU = "THU";

        /**
         * Cron constant for Friday.
         */
        private static final String FRI = "FRI";

        /**
         * Cron constant for Saturday.
         */
        private static final String SAT = "SAT";

        /**
         * Map from cron day constants to their Calendar equivalents.
         */
        private static final BidiMap<String, Integer> DAY_ID = new DualHashBidiMap<String, Integer>();

        /**
         * Map Calendar day constants to their Cron equivalents.
         */
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
            BitSet set = new BitSet(Calendar.SATURDAY);
            set.set(Calendar.MONDAY, Calendar.SATURDAY, true);
            WEEKDAYS = new DayOfWeek("MON-FRI", set);
        }

        /**
         * Constructs a {@link DayOfWeek}.
         *
         * @param value the field value
         */
        private DayOfWeek(String value) {
            this(value, new BitSet());
        }

        /**
         * Constructs a {@link DayOfWeek}.
         *
         * @param value the field value
         * @param set   the selected days
         */
        private DayOfWeek(String value, BitSet set) {
            super(value);
            this.set = set;
            ordindal = 0;
        }

        /**
         * Constructs a {@link DayOfWeek}.
         *
         * @param days the selected Cron day names
         */
        private DayOfWeek(List<String> days) {
            super(StringUtils.join(days.iterator(), ","));
            set = new BitSet();
            for (String day : days) {
                set.set(getDay(day), true);
            }
            ordindal = 0;
        }

        /**
         * Constructs a {@link DayOfWeek}.
         *
         * @param day     the selected day
         * @param ordinal the first..fifth, or {@link #LAST} instance of the day in the month
         */
        private DayOfWeek(String day, int ordinal) {
            super(ordinal == LAST ? day + "L" : day + "#" + ordinal);
            set = new BitSet(Calendar.SATURDAY);
            set.set(getDay(day));
            this.ordindal = ordinal;
        }

        /**
         * Returns the ordinal value.
         *
         * @return the ordinal value, or {@link #LAST} for the last instance of the day, or {@code 0} if there is no
         *         ordinal value specified
         */
        public int getOrdinal() {
            return ordindal;
        }

        /**
         * Determines if only weekdays are selected.
         *
         * @return {@code true} if only weekdays are selected
         */
        public boolean weekdays() {
            return set.get(Calendar.MONDAY, Calendar.SATURDAY).cardinality() == 5 &&
                   !(set.get(Calendar.SUNDAY) || set.get(Calendar.SATURDAY));
        }

        /**
         * Determines if only weekends are selected.
         *
         * @return {@code true} if only weekends are selected
         */
        public boolean weekends() {
            return set.get(Calendar.MONDAY, Calendar.SATURDAY).cardinality() == 0 &&
                   set.get(Calendar.SUNDAY) && set.get(Calendar.SATURDAY);
        }

        /**
         * Determines if all days are selected.
         *
         * @return {@code true} if all days are selected
         */
        @Override
        public boolean isAll() {
            return super.isAll() || set.cardinality() == 7;
        }

        /**
         * Determines if a day is selected.
         *
         * @param day the day
         * @return {@code true} if the day is selected
         */
        public boolean isSelected(int day) {
            return set.get(day);
        }

        /**
         * Determines if the pattern is for a single day.
         *
         * @return {@code true} if the pattern is for a single day
         */
        public boolean singleDay() {
            return set.cardinality() == 1;
        }

        /**
         * Returns the first day.
         *
         * @return the first day, in the range 1..31, or {@code -1} if no day is selected
         */
        public int day() {
            return set.nextSetBit(0);
        }

        /**
         * Determines if the field refers to the nth or last instance of a day.
         *
         * @return {@code true} if the field species an ordinal value
         */
        public boolean isOrdinal() {
            return ordindal > 0 || ordindal == LAST;
        }

        /**
         * Returns the selected day, as a Cron day name.
         *
         * @return the selected day, or {@code null} if none is selected
         */
        public String getDay() {
            if (set.cardinality() == 1) {
                return ID_DAY.get(set.nextSetBit(0));
            }
            return null;
        }

        /**
         * Creates a day of week for a list of days.
         *
         * @param days the selected Cron day names
         * @return a new day of week
         */
        public static DayOfWeek days(List<String> days) {
            return new DayOfWeek(days);
        }

        /**
         * Creates a day of week for the nth day.
         *
         * @param day     the Cron day name
         * @param ordinal the ordinal value
         * @return a new day of week
         */
        public static DayOfWeek nth(String day, int ordinal) {
            return new DayOfWeek(day, ordinal);
        }

        /**
         * Creates a day of week for the last instance of a day in the month.
         *
         * @param day the Cron day name
         * @return a new day of week
         */
        public static DayOfWeek last(String day) {
            return new DayOfWeek(day + "L");
        }

        /**
         * Parses a Cron day-of-week field.
         * <p/>
         * Parses a {@link DayOfWeek} from a Cron day-of-week pattern.
         *
         * @param value the pattern to parse
         * @return a new {@link DayOfWeek}
         */
        public static DayOfWeek parse(String value) {
            BitSet set = new BitSet();
            if (value.contains("-")) {
                String[] parts = value.split("-");
                if (parts.length == 2) {
                    int start = getDay(parts[0]);
                    int end = getDay(parts[1]);
                    set.set(start, end + 1, true);
                } else {
                    throw new IllegalArgumentException("Invalid day of week: " + value);
                }
                return new DayOfWeek(value, set);
            } else if (value.contains(",")) {
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
            } else {
                Integer day = getCalendarDay(value);
                if (day != null) {
                    set.set(day);
                    return new DayOfWeek(value, set);
                }
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

        /**
         * Returns the calendar value of a day, given a Cron day name.
         *
         * @param day the Cron day name
         * @return the corresponding calendar day
         * @throws IllegalArgumentException if the day name is invalid
         */
        private static int getDay(String day) {
            Integer result = getCalendarDay(day);
            if (result == null) {
                throw new IllegalArgumentException("Invalid day: " + day);
            }
            return result;
        }

        /**
         * Returns the calendar value of a day, given a Cron day name.
         *
         * @param day the Cron day name
         * @return the corresponding calendar day
         * @throws IllegalArgumentException if the day name is invalid
         */
        private static Integer getCalendarDay(String day) {
            return DAY_ID.get(day.toUpperCase());
        }
    }

    /**
     * Cron year field.
     */
    public static class Year extends Field {

        /**
         * The start year, or {@code -1} if none is specified
         */
        private final int year;

        /**
         * The repeat interval, or {@code -1} if none is specified
         */
        private final int interval;

        /**
         * Denotes all years.
         */
        public static final Year ALL = new Year("*");

        /**
         * Pattern to match a year and interval.
         */
        private static final Pattern YEAR_INTERVAL = Pattern.compile("(\\d+)\\/(\\d+)");

        /**
         * Constructs a {@link Year}.
         *
         * @param value the field pattern
         */
        private Year(String value) {
            super(value);
            year = -1;
            interval = -1;
        }

        /**
         * Constructs a {@link Year}.
         *
         * @param year     the start year
         * @param interval the repeat interval
         */
        private Year(int year, int interval) {
            super(year + "/" + interval);
            this.year = year;
            this.interval = interval;
        }

        /**
         * Returns the year.
         *
         * @return the year, or {@code -1} if none is specified
         */
        public int year() {
            return year;
        }

        /**
         * Returns the interval.
         *
         * @return the interval, or {@code -1} if none is specified
         */
        public int getInterval() {
            return interval;
        }

        /**
         * Creates a new interval starting at the specified year.
         *
         * @param year     the start year
         * @param interval the interval
         * @return a new year interval
         */
        public static Year every(int year, int interval) {
            return new Year(year, interval);
        }

        /**
         * Parses a {@link Year} from a Cron year pattern.
         *
         * @param value the pattern to parse
         * @return a new {@link Year}
         */
        public static Year parse(String value) {
            Matcher matcher = YEAR_INTERVAL.matcher(value);
            if (matcher.matches()) {
                int year = Integer.parseInt(matcher.group(1));
                int interval = Integer.parseInt(matcher.group(2));
                return new Year(year, interval);
            }
            return new Year(value);
        }
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
     * The year field.
     */
    private final Year year;

    /**
     * The type of the expression.
     */
    private final Type type;

    /**
     * The underlying Cron expression.
     */
    private CronExpression expression;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(CronRepeatExpression.class);

    /**
     * Constructs a {@link CronRepeatExpression}.
     *
     * @param startTime the time the expression starts
     * @param dayOfWeek the day-of-week
     */
    public CronRepeatExpression(Date startTime, DayOfWeek dayOfWeek) {
        this(startTime, DayOfMonth.NO_VALUE, Month.ALL, dayOfWeek, Year.ALL, Type.CUSTOM);
    }

    /**
     * Constructs a {@link CronRepeatExpression}.
     *
     * @param startTime the time the expression starts
     * @param month     the month
     * @param dayOfWeek the day-of-week
     */
    public CronRepeatExpression(Date startTime, Month month, DayOfWeek dayOfWeek) {
        this(startTime, DayOfMonth.NO_VALUE, month, dayOfWeek, Year.ALL, Type.CUSTOM);
    }

    /**
     * Constructs a {@link CronRepeatExpression}.
     *
     * @param startTime  the time the expression starts
     * @param dayOfMonth the day-of-month
     * @param month      the month
     */
    public CronRepeatExpression(Date startTime, DayOfMonth dayOfMonth, Month month) {
        this(startTime, dayOfMonth, month, DayOfWeek.NO_VALUE, Year.ALL, Type.CUSTOM);
    }

    /**
     * Constructs a {@link CronRepeatExpression}.
     *
     * @param startTime the time the expression starts
     * @param month     the month
     * @param dayOfWeek the day of week
     * @param year      the year
     */
    public CronRepeatExpression(Date startTime, Month month, DayOfWeek dayOfWeek, Year year) {
        this(startTime, DayOfMonth.NO_VALUE, month, dayOfWeek, year, Type.CUSTOM);
    }

    /**
     * Constructs a {@link CronRepeatExpression}.
     *
     * @param startTime  the time the expression starts
     * @param dayOfMonth the day of month
     * @param month      the month
     * @param year       the year
     */
    public CronRepeatExpression(Date startTime, DayOfMonth dayOfMonth, Month month, Year year) {
        this(startTime, dayOfMonth, month, DayOfWeek.NO_VALUE, year, Type.CUSTOM);
    }

    /**
     * Constructs a {@link CronRepeatExpression}.
     *
     * @param startTime  the time the expression starts
     * @param dayOfMonth the day-of-month
     * @param month      the month
     * @param dayOfWeek  the day-of-week
     * @param year       the year
     * @param type       the expression type
     */
    private CronRepeatExpression(Date startTime, DayOfMonth dayOfMonth, Month month, DayOfWeek dayOfWeek, Year year,
                                 Type type) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(startTime);
        minutes = "" + calendar.get(Calendar.MINUTE);
        hours = "" + calendar.get(Calendar.HOUR_OF_DAY);
        this.dayOfMonth = dayOfMonth;
        this.month = month;
        this.dayOfWeek = dayOfWeek;
        this.year = year;
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
                                 Year year, Type type) {
        this.minutes = "" + minute;
        this.hours = "" + hour;
        this.dayOfMonth = dayOfMonth;
        this.month = month;
        this.dayOfWeek = dayOfWeek;
        this.type = type;
        this.year = year;
    }

    /**
     * Returns the minutes field.
     *
     * @return the minutes field
     */
    public String getMinutes() {
        return minutes;
    }

    /**
     * Returns the hours field.
     *
     * @return the hours field
     */
    public String getHours() {
        return hours;
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
     * Returns the year field.
     *
     * @return the year field
     */
    public Year getYear() {
        return year;
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
        result.append(" ");
        result.append(year.value);
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
                log.error(exception, exception);
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
                   && ObjectUtils.equals(dayOfWeek, other.dayOfWeek)
                   && ObjectUtils.equals(year, other.year);
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
        return builder.append(minutes).append(hours).append(dayOfMonth).append(month).append(dayOfWeek).append(year)
                .hashCode();
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
        if (parts.length < 6) {
            return null;
        }
        try {
            int minute = parseInt(parts[1], 0, 59, "minutes");
            int hour = parseInt(parts[2], 0, 23, "hours");
            String dayOfMonth = parts[3];
            String month = parts[4];
            String dayOfWeek = parts[5];
            String year = parts.length > 6 ? parts[6] : Year.ALL.value;
            result = parse(minute, hour, dayOfMonth, month, dayOfWeek, year);
        } catch (Throwable exception) {
            log.warn(exception, exception);
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
        return new CronRepeatExpression(startTime, DayOfMonth.NO_VALUE, Month.ALL, DayOfWeek.WEEKDAYS, Year.ALL,
                                        Type.WEEKDAYS);
    }

    /**
     * Parses an expression.
     *
     * @param minute     the minute field
     * @param hour       the hour field
     * @param dayOfMonth the day-of-month field
     * @param month      the month field
     * @param dayOfWeek  the day-of-week field
     * @param year       the year field
     * @return the parsed expression
     * @throws IllegalArgumentException if the expression cannot be parsed
     */
    private static CronRepeatExpression parse(int minute, int hour, String dayOfMonth, String month, String dayOfWeek,
                                              String year) {
        CronRepeatExpression result;
        DayOfMonth dom = DayOfMonth.parse(dayOfMonth);
        Month m = Month.parse(month);
        DayOfWeek dow = DayOfWeek.parse(dayOfWeek);
        Year y = Year.parse(year);
        boolean allDayOfMonth = dom.isAll();
        boolean allMonth = m.isAll();
        boolean allDayOfWeek = dow.isAll();

        Type type;
        if (allDayOfMonth && allMonth && allDayOfWeek) {
            type = Type.DAILY;
        } else if (allDayOfMonth && allMonth && dow.weekdays()) {
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
        result = new CronRepeatExpression(minute, hour, dom, m, dow, y, type);
        return result;
    }

    /**
     * Parses an integer.
     *
     * @param value       the value to parse
     * @param min         the minimum allowed value
     * @param max         the maximum allowed value
     * @param displayName the display name, used to report a parse error
     * @return the parsed value
     * @throws IllegalArgumentException if {@code value} is invalid
     */
    private static int parseInt(String value, int min, int max, String displayName) {
        int result;
        try {
            result = Integer.valueOf(value);
            if (result < min || result > max) {
                throw new IllegalArgumentException("Invalid " + displayName + ": " + value);
            }
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid " + displayName + ": " + value);
        }
        return result;
    }

    /**
     * Cron expression field.
     */
    private static abstract class Field {

        /**
         * The field pattern.
         */
        protected final String value;

        /**
         * Constructs a {@link Field}.
         *
         * @param value the field pattern
         */
        public Field(String value) {
            this.value = value;
        }

        /**
         * Determines if the field represents all possible values.
         *
         * @return {@code true} if the field represents all possible values
         */
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


}
