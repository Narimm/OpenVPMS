package org.openvpms.web.component.im.query;

import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;

import java.util.Date;

/**
 * The date navigator.
 *
 * @author Tim Anderson
 */
public class DateNavigator {

    public static final DateNavigator DAY = new DateNavigator();

    public static final DateNavigator WEEK = new DateNavigator() {

        @Override
        public Date getNext(Date date) {
            return DateRules.getDate(date, 1, DateUnits.WEEKS);
        }

        @Override
        public Date getPrevious(Date date) {
            return DateRules.getDate(date, -1, DateUnits.WEEKS);
        }

        @Override
        public Date getNextTerm(Date date) {
            return DateRules.getDate(date, 1, DateUnits.MONTHS);
        }

        @Override
        public Date getPreviousTerm(Date date) {
            return DateRules.getDate(date, -1, DateUnits.MONTHS);
        }
    };

    public static final DateNavigator MONTH = new DateNavigator() {
        @Override
        public Date getNext(Date date) {
            return DateRules.getDate(date, 1, DateUnits.MONTHS);
        }

        @Override
        public Date getPrevious(Date date) {
            return DateRules.getDate(date, -1, DateUnits.MONTHS);
        }

        @Override
        public Date getNextTerm(Date date) {
            return DateRules.getDate(date, 1, DateUnits.YEARS);
        }

        @Override
        public Date getPreviousTerm(Date date) {
            return DateRules.getDate(date, -1, DateUnits.YEARS);
        }
    };

    public Date getCurrent(Date date) {
        return DateRules.getToday();
    }

    public Date getNext(Date date) {
        return DateRules.getNextDate(date);
    }

    public Date getPrevious(Date date) {
        return DateRules.getPreviousDate(date);
    }

    public Date getNextTerm(Date date) {
        return DateRules.getDate(date, 1, DateUnits.WEEKS);
    }

    public Date getPreviousTerm(Date date) {
        return DateRules.getDate(date, 1, DateUnits.WEEKS);
    }
}
