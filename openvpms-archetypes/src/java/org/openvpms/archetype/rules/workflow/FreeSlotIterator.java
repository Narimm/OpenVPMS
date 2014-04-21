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
import org.apache.commons.collections4.iterators.PushbackIterator;
import org.apache.commons.lang.time.DateUtils;
import org.joda.time.MutableDateTime;
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

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
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
    private final PushbackIterator<Slot> iterator;

    /**
     * The time in milliseconds from midnight that the schedule starts at. Corresponds to the schedule
     * <em>startTime</em> node. A value of {@code -1} indicates the <em>startTime</em> is unset.
     */
    private final long scheduleStart;

    /**
     * The time in milliseconds from midnight that the schedule ends at. Corresponds to the schedule
     * <em>endTime</em> node. A value of {@code -1} indicates the <em>endTime</em> is unset.
     */
    private final long scheduleEnd;

    private static class SplitSlot extends Slot {

        /**
         * Constructs a {@link Slot}.
         *
         * @param schedule  the schedule id
         * @param startTime the slot start time
         * @param endTime   the slot end time
         */
        public SplitSlot(long schedule, Date startTime, Date endTime) {
            super(schedule, startTime, endTime);
        }
    }

    /**
     * Constructs an {@link FreeSlotIterator}.
     *
     * @param schedule the schedule
     * @param fromDate the date to query from
     * @param toDate   the date to query to
     * @param service  the archetype service
     */
    public FreeSlotIterator(Entity schedule, Date fromDate, Date toDate, IArchetypeService service) {
        IMObjectBean bean = new IMObjectBean(schedule, service);
        scheduleStart = getTime(bean.getDate("startTime"));
        scheduleEnd = getTime(bean.getDate("endTime"));

        Iterator<ObjectSet> queryIterator = createFreeSlotIterator(schedule, fromDate, toDate, service);
        Iterator<Slot> slotIterator = createFreeSlotAdapter(queryIterator);
        Iterator<Slot> first = createFirstFreeSlotIterator(slotIterator, schedule, fromDate, toDate, service);
        if (scheduleStart != -1 && scheduleEnd != -1) {
            // filter free slots outside the schedule opening and closing times
            first = new FilterIterator<Slot>(first, new SchedulePredicate());
        }
        iterator = new PushbackIterator<Slot>(first);
    }

    /**
     * Creates an iterator to handle the first and last free slot.
     * <p/>
     * This cannot be done in the database layer without using slow unions.
     *
     * @param iterator the slot iterator
     * @return an iterator that handles the first free slot in the date range
     */
    private Iterator<Slot> createFirstFreeSlotIterator(Iterator<Slot> iterator, Entity schedule, Date fromDate,
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
    private Iterator<ObjectSet> createFreeSlotIterator(Entity schedule, Date fromDate, Date toDate, IArchetypeService service) {
        NamedQuery query = new NamedQuery("findFreeSlots", "scheduleId", "startTime", "endTime");
        query.setParameter("from", fromDate);
        query.setParameter("to", toDate);
        query.setParameter("scheduleId", schedule.getId());
        return new ObjectSetQueryIterator(service, query);
    }

    private Date getAppointmentBefore(Entity schedule, Date fromDate, Date toDate, IArchetypeService service) {
        Date result = null;
        ArchetypeQuery query = new ArchetypeQuery(shortName("a", ScheduleArchetypes.APPOINTMENT));
        query.add(new NodeSelectConstraint("a.startTime"));
        query.add(new NodeSelectConstraint("a.endTime"));
        query.add(Constraints.join("schedule").add(Constraints.eq("entity", schedule.getObjectReference())));
        query.add(Constraints.or(Constraints.between("startTime", fromDate, toDate),
                                 Constraints.between("endTime", fromDate, toDate)));
        query.add(Constraints.sort("startTime"));
        query.setMaxResults(1);
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

    private Date getAppointmentAfter(Entity schedule, Date fromDate, Date toDate, IArchetypeService service) {
        Date result = null;
        ArchetypeQuery query = new ArchetypeQuery(shortName("a", ScheduleArchetypes.APPOINTMENT));
        query.add(new NodeSelectConstraint("a.startTime"));
        query.add(new NodeSelectConstraint("a.endTime"));
        query.add(Constraints.join("schedule").add(Constraints.eq("entity", schedule.getObjectReference())));
        query.add(Constraints.or(Constraints.between("startTime", fromDate, toDate),
                                 Constraints.between("endTime", fromDate, toDate)));
        query.add(Constraints.sort("endTime", false));
        query.setMaxResults(1);
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
        Slot slot = iterator.next();
        if (slot instanceof SplitSlot) {
            return slot;
        }
        if (scheduleStart != -1 && scheduleEnd != -1) {
            slot = split(slot);
        }
        return slot;
    }

    /**
     * Removes from the underlying collection the last element returned
     * by this iterator (optional operation).
     *
     * @throws UnsupportedOperationException if the {@code remove} operation is not supported by this iterator
     * @throws IllegalStateException         if the {@code next} method has not yet been called, or the {@code remove}
     *                                       method has already been called after the last call to the {@code next}
     *                                       method
     */
    @Override
    public void remove() {
        iterator.remove();
    }

    private Slot split(Slot slot) {
        long slotStart = -1;
        if (scheduleStart != -1) {
            slotStart = getTime(slot.getStartTime());
            if (slotStart < scheduleStart) {
                slot.setStartTime(getDateTime(slot.getStartTime(), scheduleStart));
            }
        }
        if (scheduleEnd != -1) {
            long slotEnd = getTime(slot.getEndTime());
            if (slotEnd >= scheduleEnd) {
                slot.setEndTime(getDateTime(slot.getEndTime(), scheduleStart));
            } else {
                if (slotStart == -1) {
                    slotStart = getTime(slot.getStartTime());
                }
                if (slotEnd <= slotStart) {
                    Date endTime = getDateTime(slot.getEndTime(), scheduleEnd);
                    endTime = DateRules.getDate(endTime, -1, DateUnits.DAYS);
                    slot.setEndTime(endTime);
                }
            }
        }
        return slot;
    }

    private long getTime(Date date) {
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

    private Date getDateTime(Date date, long time) {
        MutableDateTime dateTime = new MutableDateTime(date);
        dateTime.setMillisOfDay((int) time);
        return dateTime.toDate();
    }


    private class SchedulePredicate implements Predicate<Slot> {

        /**
         * Use the specified parameter to perform a test that returns true or false.
         *
         * @param slot the object to evaluate
         * @return true or false
         */
        @Override
        public boolean evaluate(Slot slot) {
            long slotStart = getTime(slot.getStartTime());
            long slotEnd = getTime(slot.getEndTime());
            if ((!(slotStart > scheduleEnd || scheduleEnd == -1) && (slotEnd < scheduleStart || scheduleStart == -1))) {
                return false;
            }
            return true;
        }
    }

}
