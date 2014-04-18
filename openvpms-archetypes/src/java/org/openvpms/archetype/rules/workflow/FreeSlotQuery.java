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

package org.openvpms.archetype.rules.workflow;

import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.functors.AllPredicate;
import org.apache.commons.collections.iterators.FilterIterator;
import org.apache.commons.lang.time.DateUtils;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.MutableDateTime;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.NamedQuery;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.ObjectSetQueryIterator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Queries free appointment slots.
 *
 * @author Tim Anderson
 */
public class FreeSlotQuery {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The date to query from.
     */
    private Date fromDate;

    /**
     * The date to query to.
     */
    private Date toDate;

    /**
     * If set, only return slots to those occurring on or after the specified time.
     */
    private Duration fromTime;

    /**
     * If set, only return slots to those occurring prior to the specified time.
     */
    private Duration toTime;

    private Entity[] schedules = {};

    private long minSlotSize = -1;

    /**
     * Constructs a {@link FreeSlotQuery}.
     *
     * @param service the archetype service
     */
    public FreeSlotQuery(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Sets the date to query slots from.
     *
     * @param from the from date
     */
    public void setFromDate(Date from) {
        this.fromDate = from;
    }

    /**
     * Sets the date to query slots to.
     *
     * @param to the to date
     */
    public void setToDate(Date to) {
        this.toDate = to;
    }

    public void setFromTime(Duration time) {
        this.fromTime = time;
    }

    public void setToTime(Duration time) {
        this.toTime = time;
    }

    public void setSchedules(Entity... schedules) {
        this.schedules = schedules;
    }

    public void setMinSlotSize(int size, DateUnits units) {
        switch (units) {
            case DAYS:
                minSlotSize = size * DateUtils.MILLIS_PER_DAY;
                break;
            case HOURS:
                minSlotSize = size * DateUtils.MILLIS_PER_HOUR;
                break;
            case MINUTES:
                minSlotSize = size * DateUtils.MILLIS_PER_MINUTE;
                break;
            default:
                minSlotSize = 0;
        }
    }

    /**
     * Queries available slots.
     *
     * @return an iterator over the available slots
     */
    public Iterator<Slot> query() {
        if (fromDate != null && toDate != null && schedules.length > 0) {
            NamedQuery query = new NamedQuery("findFreeSlots", Arrays.asList("scheduleId", "startTime", "endTime"));
            query.setParameter("from", fromDate);
            query.setParameter("to", toDate);
            Long[] scheduleIds = new Long[schedules.length];
            for (int i = 0; i < schedules.length; ++i) {
                scheduleIds[i] = schedules[i].getId();
            }
            query.setParameter("scheduleIds", scheduleIds);
            Iterator<Slot> iterator = new SlotIterator(new ObjectSetQueryIterator(service, query));
            Predicate predicate = getPredicate();
            return filter(iterator, predicate);
        }
        return Collections.<Slot>emptyList().iterator();
    }

    private Predicate getPredicate() {
        List<Predicate> predicates = new ArrayList<Predicate>();
        predicates.add(new SchedulePredicate(schedules));
        if (minSlotSize > 0) {
            predicates.add(new SlotSizePredicate());
        }
        if (fromTime != null) {
            predicates.add(new FromTimePredicate());
        }
        if (toTime != null) {
            predicates.add(new ToTimePredicate());
        }
        return AllPredicate.getInstance(predicates);
    }

    @SuppressWarnings("unchecked")
    private Iterator<Slot> filter(Iterator<Slot> iterator, Predicate predicate) {
        return new FilterIterator(iterator, predicate);
    }

    private class SchedulePredicate implements Predicate {

        private class Times {

            final long from;
            final long to;

            public Times(long from, long to) {
                this.from = from;
                this.to = to;
            }
        }

        private Map<Long, Times> map = new HashMap<Long, Times>();

        public SchedulePredicate(Entity[] schedules) {
            for (Entity schedule : schedules) {
                IMObjectBean bean = new IMObjectBean(schedule, service);
                long startTime = getTime(bean.getDate("startTime"));
                long endTime = getTime(bean.getDate("endTime"));
                if (startTime != -1 && endTime != -1) {
                    map.put(schedule.getId(), new Times(startTime, endTime));
                }
            }

        }

        /**
         * Use the specified parameter to perform a test that returns true or false.
         *
         * @param object the object to evaluate, should not be changed
         * @return true or false
         * @throws ClassCastException (runtime) if the input is the wrong class
         */
        @Override
        public boolean evaluate(Object object) {
            Slot slot = (Slot) object;
            Times times = map.get(slot.getSchedule());
            if (times != null) {
                long slotStart = getTime(slot.getStartTime());
                long slotEnd = getTime(slot.getEndTime());
                if ((!(slotStart > times.to || times.to == -1) && (slotEnd < times.from || times.from == -1))) {
                    return false;
                }
                if (slotStart <= times.from) {
                    slot.setStartTime(getDateTime(slot.getStartTime(), times.from));
                }
                if (times.to != -1) {
                    if (slotEnd >= times.to) {
                        slot.setEndTime(getDateTime(slot.getEndTime(), times.to));
                    } else if (slotEnd <= slotStart) {
                        Date endTime = getDateTime(slot.getEndTime(), times.to);
                        endTime = DateRules.getDate(endTime, -1, DateUnits.DAYS);
                        slot.setEndTime(endTime);
                    }
                }
            }
            return true;
        }

        private Date getDateTime(Date date, long time) {
            MutableDateTime dateTime = new MutableDateTime(date);
            dateTime.setMillisOfDay((int) time);
            return dateTime.toDate();
        }

    }

    private class SlotSizePredicate implements Predicate {

        @Override
        public boolean evaluate(Object object) {
            Slot slot = (Slot) object;
            long duration = slot.getEndTime().getTime() - slot.getStartTime().getTime();
            return duration >= minSlotSize;
        }
    }

    private class FromTimePredicate implements Predicate {

        @Override
        public boolean evaluate(Object object) {
            Slot slot = (Slot) object;
            Duration slotStart = getMillisOfDay(slot.getStartTime());
            Duration slotEnd = getMillisOfDay(slot.getEndTime());
            return slotStart.compareTo(slotEnd) >= 0 || slotEnd.compareTo(fromTime) > 0;
        }
    }

    private class ToTimePredicate implements Predicate {

        @Override
        public boolean evaluate(Object object) {
            Slot slot = (Slot) object;
            Duration slotStart = getMillisOfDay(slot.getStartTime());
            Duration slotEnd = getMillisOfDay(slot.getEndTime());
            return slotStart.compareTo(slotEnd) >= 0 || slotStart.compareTo(toTime) < 0;
        }
    }

    private Duration getMillisOfDay(Date date) {
        DateTime time = new DateTime(date);
        return new Duration(time.getMillisOfDay());
    }

    private static long getTime(Date date) {
        if (date != null) {
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(date);
            return (calendar.get(Calendar.HOUR_OF_DAY) * DateUtils.MILLIS_PER_HOUR)
                   + (calendar.get(Calendar.MINUTE) * DateUtils.MILLIS_PER_MINUTE)
                   + (calendar.get(Calendar.SECOND) * DateUtils.MILLIS_PER_SECOND)
                   + calendar.get(Calendar.MILLISECOND);
        }
        return -1;
    }

    private class SlotIterator implements Iterator<Slot> {

        private final Iterator<ObjectSet> iterator;

        public SlotIterator(Iterator<ObjectSet> iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public Slot next() {
            ObjectSet set = iterator.next();
            return new Slot(set.getLong("scheduleId"), set.getDate("startTime"), set.getDate("endTime"));
        }

        @Override
        public void remove() {
            iterator.remove();
        }
    }

}
