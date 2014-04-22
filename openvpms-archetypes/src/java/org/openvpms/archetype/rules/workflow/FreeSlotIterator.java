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

import org.apache.commons.collections4.Predicate;
import org.apache.commons.collections4.iterators.AbstractUntypedIteratorDecorator;
import org.apache.commons.collections4.iterators.FilterIterator;
import org.apache.commons.collections4.iterators.IteratorChain;
import org.joda.time.DateTime;
import org.joda.time.MutableDateTime;
import org.joda.time.Period;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.NamedQuery;
import org.openvpms.component.system.common.query.NodeSelectConstraint;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.ObjectSetQueryIterator;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Date;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.openvpms.component.system.common.query.Constraints.shortName;

/**
 * An iterator over free slots for a schedule.
 *
 * @author Tim Anderson
 */
class FreeSlotIterator implements Iterator<Slot> {

    /**
     * The underlying iterator.
     */
    private final Iterator<Slot> iterator;

    /**
     * Constructs an {@link FreeSlotIterator}.
     *
     * @param schedule the schedule
     * @param fromDate the date to query from
     * @param toDate   the date to query to
     * @param fromTime the time to query from. May be {@code null}
     * @param toTime   the time to query to. May be {@code null}
     * @param service  the archetype service
     */
    public FreeSlotIterator(Entity schedule, Date fromDate, Date toDate, Period fromTime, Period toTime,
                            IArchetypeService service) {
        IMObjectBean bean = new IMObjectBean(schedule, service);

        long scheduleStart = getTime(bean.getDate("startTime")); // the time that the schedule starts at
        long scheduleEnd = getTime(bean.getDate("endTime"));     // the time that the schedule ends at

        Iterator<ObjectSet> queryIterator = createFreeSlotIterator(schedule, fromDate, toDate, service);
        Iterator<Slot> slotIterator = createFreeSlotAdapter(queryIterator);
        Iterator<Slot> first = createFirstLastFreeSlotIterator(slotIterator, schedule, fromDate, toDate, service);
        if (scheduleStart != -1 || scheduleEnd != -1) {
            // filter free slots outside the schedule opening and closing times, and split those slots that span
            // multiple opening/closing times
            first = new TimeRangeSlotIterator(first, scheduleStart, scheduleEnd);
        }
        if (fromTime != null || toTime != null) {
            // filter free slots outside the time range
            long from = (fromTime != null) ? fromTime.toStandardDuration().getMillis() : -1;
            long to = (toTime != null) ? toTime.toStandardDuration().getMillis() : -1;
            first = new TimeRangeSlotIterator(first, from, to);
        }
        iterator = first;
    }

    /**
     * Returns {@code true} if the iteration has more elements.
     *
     * @return {@code true} if the iteration has more elements
     */
    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element in the iteration
     * @throws NoSuchElementException if the iteration has no more elements
     */
    @Override
    public Slot next() {
        return iterator.next();
    }

    /**
     * Removes from the underlying collection the last element returned
     * by this iterator (optional operation).
     *
     * @throws UnsupportedOperationException if invoked
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates an iterator to handle the first and last free slot.
     * <p/>
     * These cannot be determined by the findFreeSlots named query without using unions which are slow, so two other
     * queries are issued to find:
     * <ul>
     * <li>the start time of the earliest appointment intersecting the start of the date range (before)</li>
     * <li>the end time of the latest appointment intersecting the end of the date range (after)</li>
     * </ul>
     * If there is an appointment at the start, this is used to add a slot (fromDate, before).
     * <p/>
     * If there is an appointment at the end, this is used to add a slot (after, toDate).
     * <p/>
     * If there are no appointments, then a slot (fromDate, toDate) is added.
     *
     * @param iterator the slot iterator
     * @return an iterator that handles the first free slot in the date range
     */
    private Iterator<Slot> createFirstLastFreeSlotIterator(Iterator<Slot> iterator, Entity schedule, Date fromDate,
                                                           Date toDate, IArchetypeService service) {
        IteratorChain<Slot> result = new IteratorChain<Slot>();
        Date appointmentBefore = getAppointmentBefore(schedule, fromDate, toDate, service);
        Date appointmentAfter = getAppointmentAfter(schedule, fromDate, toDate, service);
        if (appointmentBefore != null || appointmentAfter != null) {
            if (appointmentBefore != null) {
                Slot slot = new Slot(schedule.getId(), fromDate, appointmentBefore);
                result.addIterator(Arrays.asList(slot).iterator());
            }
            result.addIterator(iterator);
            if (appointmentAfter != null) {
                Slot slot = new Slot(schedule.getId(), appointmentAfter, toDate);
                result.addIterator(Arrays.asList(slot).iterator());
            }
        } else {
            // no appointments
            Slot slot = new Slot(schedule.getId(), fromDate, toDate);
            result.addIterator(Arrays.asList(slot).iterator());
        }
        return result;
    }

    /**
     * Creates an iterator that adapts {@link ObjectSet}s returned by the <em>findFreeSlots</em> query,
     * to {@link Slot}s.
     *
     * @param queryIterator the query iterator to adapt
     * @return the iterator adapter
     */
    private Iterator<Slot> createFreeSlotAdapter(final Iterator<ObjectSet> queryIterator) {
        return new AbstractUntypedIteratorDecorator<ObjectSet, Slot>(queryIterator) {
            @Override
            public Slot next() {
                ObjectSet set = getIterator().next();
                return new Slot(set.getLong("scheduleId"), set.getDate("startTime"), set.getDate("endTime"));
            }
        };
    }

    /**
     * Creates an iterator that uses the <em>findFreeSlots</em> named query, to find free slots.
     *
     * @param schedule the schedule
     * @param fromDate the date to query from
     * @param toDate   the date to query to
     * @param service  the archetype service
     * @return a new iterator over the free slots
     */
    private Iterator<ObjectSet> createFreeSlotIterator(Entity schedule, Date fromDate, Date toDate,
                                                       IArchetypeService service) {
        NamedQuery query = new NamedQuery("findFreeSlots", "scheduleId", "startTime", "endTime");
        query.setParameter("from", fromDate);
        query.setParameter("to", toDate);
        query.setParameter("scheduleId", schedule.getId());
        return new ObjectSetQueryIterator(service, query);
    }

    /**
     * Returns the start time of the earliest appointment intersecting the start of the date range.
     *
     * @param schedule the schedule
     * @param fromDate the start of the date range
     * @param toDate   the end of the date range
     * @param service  the archetype service
     * @return the start time of the appointment at the start of the date range, or {@code null} if none exists
     */
    private Date getAppointmentBefore(Entity schedule, Date fromDate, Date toDate, IArchetypeService service) {
        Date result = null;
        ArchetypeQuery query = createAppointmentQuery(schedule, fromDate, toDate);
        query.add(Constraints.sort("startTime"));
        Iterator<ObjectSet> iterator = new ObjectSetQueryIterator(service, query);
        if (iterator.hasNext()) {
            ObjectSet set = iterator.next();
            Date startTime = set.getDate("a.startTime");
            if (DateRules.compareTo(startTime, fromDate) > 0) {
                result = startTime;
            }
        }
        return result;
    }

    /**
     * Returns the end time of the latest appointment intersecting the end of the date range.
     *
     * @param schedule the schedule
     * @param fromDate the start of the date range
     * @param toDate   the end of the date range
     * @param service  the archetype service
     * @return the end time of the appointment after the date range, or {@code null} if none exists
     */
    private Date getAppointmentAfter(Entity schedule, Date fromDate, Date toDate, IArchetypeService service) {
        Date result = null;
        ArchetypeQuery query = createAppointmentQuery(schedule, fromDate, toDate);
        query.add(Constraints.sort("endTime", false));
        Iterator<ObjectSet> iterator = new ObjectSetQueryIterator(service, query);
        if (iterator.hasNext()) {
            ObjectSet set = iterator.next();
            Date endTime = set.getDate("a.endTime");
            if (DateRules.compareTo(endTime, toDate) < 0) {
                result = endTime;
            }
        }
        return result;
    }

    /**
     * Creates a query for the start and end times of an appointment falling in the specified date range.
     *
     * @param schedule the appointment schedule
     * @param fromDate the date to query from
     * @param toDate   the date to query to
     * @return a new query
     */
    private ArchetypeQuery createAppointmentQuery(Entity schedule, Date fromDate, Date toDate) {
        ArchetypeQuery query = new ArchetypeQuery(shortName("a", ScheduleArchetypes.APPOINTMENT));
        query.add(new NodeSelectConstraint("a.startTime"));
        query.add(new NodeSelectConstraint("a.endTime"));
        query.add(Constraints.join("schedule").add(Constraints.eq("entity", schedule.getObjectReference())));
        query.add(Constraints.or(Constraints.between("startTime", fromDate, toDate),
                                 Constraints.between("endTime", fromDate, toDate)));
        query.setMaxResults(1);
        return query;
    }

    /**
     * Returns the time portion of a date/time, in milliseconds.
     *
     * @param date the date/time
     * @return the time, in milliseconds, or {@code -1} if the date is {@code null}
     */
    private long getTime(Date date) {
        if (date != null) {
            return new DateTime(date).getMillisOfDay();
        }
        return -1;
    }

    /**
     * An iterator that filters slots that fall outside a time range, and splits slots that overlap the time range.
     */
    private class TimeRangeSlotIterator implements Iterator<Slot> {

        /**
         * The filtering iterator. This filters out slots that aren't in the time range.
         */
        private final Iterator<Slot> filter;

        /**
         * The underlying iterator. A push-back iterator is used to handle the case where a free slot spans multiple
         * time ranges. In this case, the slot is split into two or more slots, and pushed back onto the iterator.
         */
        private final Deque<Slot> slots = new ArrayDeque<Slot>();

        /**
         * The start of the time range, in milliseconds, or {@code -1} if there is no start (in which case, the range
         * effectively starts at 12 AM).
         */
        private final long rangeStart;

        /**
         * The end of the time range, in milliseconds, or {@code -1} if there is no end ((in which case, the range
         * effectively ends at 12AM the following day).
         */
        private final long rangeEnd;

        /**
         * Constructs a {@link TimeRangeSlotIterator}.
         *
         * @param iterator   the underlying slot iterator
         * @param rangeStart the start of the time range
         * @param rangeEnd   the end of the time range
         */
        public TimeRangeSlotIterator(Iterator<Slot> iterator, long rangeStart, long rangeEnd) {
            filter = new FilterIterator<Slot>(iterator, new TimeRangePredicate());
            this.rangeStart = rangeStart;
            this.rangeEnd = rangeEnd;
        }

        /**
         * Returns {@code true} if the iteration has more elements.
         * (In other words, returns {@code true} if {@link #next} would
         * return an element rather than throwing an exception.)
         *
         * @return {@code true} if the iteration has more elements
         */
        @Override
        public boolean hasNext() {
            while (slots.isEmpty() && filter.hasNext()) {
                Slot slot = filter.next();
                add(slot);
            }
            return !slots.isEmpty();
        }

        /**
         * Returns the next element in the iteration.
         *
         * @return the next element in the iteration
         * @throws NoSuchElementException if the iteration has no more elements
         */
        @Override
        public Slot next() {
            return slots.pop();
        }

        /**
         * Removes from the underlying collection the last element returned
         * by this iterator (optional operation).
         *
         * @throws UnsupportedOperationException if invoked
         */
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        /**
         * Adds a slot.
         * <p/>
         * For slots that don't span multiple days, this ensures that the slot start and end times don't exceed those
         * of the range.
         * <p/>
         * Slots that span multiple days are split into a slot per day.
         *
         * @param slot the slot to add
         */
        private void add(Slot slot) {
            Date from = DateRules.getDate(slot.getStartTime());
            Date to = DateRules.getDate(slot.getEndTime());
            if (from.compareTo(to) != 0) {
                add(slot.getSchedule(), getSlotStart(slot.getStartTime()), getRangeEnd(from));
                while ((from = DateRules.getDate(from, 1, DateUnits.DAYS)).compareTo(to) < 0) {
                    add(slot.getSchedule(), getRangeStart(from), getRangeEnd(from));
                }
                add(slot.getSchedule(), getRangeStart(to), getSlotEnd(slot.getEndTime()));
            } else {
                add(slot.getSchedule(), getSlotStart(slot.getStartTime()), getSlotEnd(slot.getEndTime()));
            }
        }

        /**
         * Adds a slot, if it is valid.
         *
         * @param scheduleId the schedule identifier.
         * @param slotStart  the slot start time
         * @param slotEnd    the slot end time
         */
        private void add(long scheduleId, Date slotStart, Date slotEnd) {
            if (slotStart.compareTo(slotEnd) < 0) {
                slots.add(new Slot(scheduleId, slotStart, slotEnd));
            }
        }

        /**
         * Returns the time range start for a particular date.
         *
         * @param date the date
         * @return the schedule start for the date
         */
        private Date getRangeStart(Date date) {
            if (rangeStart == -1) {
                return date;
            }
            return getDateTime(date, rangeStart);
        }

        /**
         * Returns the time range end for a particular date.
         *
         * @param date the date
         * @return the schedule end for the date
         */
        private Date getRangeEnd(Date date) {
            if (rangeEnd == -1) {
                return DateRules.getDate(date, 1, DateUnits.DAYS);
            }
            return getDateTime(date, rangeEnd);
        }

        /**
         * Adjusts a slot start time, if it is before the range start time.
         *
         * @param startTime the slot start time
         * @return the adjusted start time, or {@code startTime}  if it didn't need adjusting
         */
        private Date getSlotStart(Date startTime) {
            Date result = startTime;
            if (rangeStart != -1) {
                long slotStart = getTime(startTime);
                if (slotStart < rangeStart) {
                    result = getDateTime(startTime, rangeStart);
                }
            }
            return result;
        }

        /**
         * Adjusts a slot end time, if it is after the range end time.
         *
         * @param endTime the slot end time
         * @return the adjusted end time, or {@code endTime}  if it didn't need adjusting
         */
        private Date getSlotEnd(Date endTime) {
            Date result = endTime;
            if (rangeEnd != -1) {
                long slotEnd = getTime(endTime);
                if (slotEnd >= rangeEnd) {
                    result = getDateTime(endTime, rangeEnd);
                }
            }
            return result;
        }

        /**
         * Returns a new date/time from the specified date and time
         *
         * @param date the date
         * @param time the time, in milliseconds
         * @return a new date/time
         */
        private Date getDateTime(Date date, long time) {
            MutableDateTime dateTime = new MutableDateTime(date);
            dateTime.setMillisOfDay((int) time);
            return dateTime.toDate();
        }


        /**
         * Predicate to exclude slots outside a time range.
         */
        private class TimeRangePredicate implements Predicate<Slot> {

            /**
             * Use the specified parameter to perform a test that returns true or false.
             *
             * @param slot the object to evaluate
             * @return true or false
             */
            @Override
            public boolean evaluate(Slot slot) {
                Date slotStart = slot.getStartTime();
                Date slotEnd = slot.getEndTime();
                Date start = rangeStart != -1 ? getRangeStart(slotStart) : DateRules.getDate(slotStart);
                Date end = rangeEnd != -1 ? getRangeEnd(slotEnd)
                                          : DateRules.getDate(DateRules.getDate(slotStart), 1, DateUnits.DAYS);
                return DateRules.intersects(slotStart, slotEnd, start, end);
            }
        }

    }

}
