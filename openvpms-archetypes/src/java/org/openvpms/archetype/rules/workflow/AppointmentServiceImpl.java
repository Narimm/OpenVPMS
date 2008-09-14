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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.workflow;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.IArchetypeServiceListener;
import org.openvpms.component.system.common.query.ObjectSet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class AppointmentServiceImpl implements AppointmentService {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The cache.
     */
    private final Cache cache;


    /**
     * Creates a new <tt>CachingLookupService</tt>.
     *
     * @param service the archetype service
     * @param cache   the cache
     */
    public AppointmentServiceImpl(IArchetypeService service, Cache cache) {
        this.service = service;
        this.cache = cache;
        service.addListener(
                "act.customerAppointment", new IArchetypeServiceListener() {
            public void saved(IMObject object) {
                addAppointment((Act) object);
            }

            public void removed(IMObject object) {
                removeAppointment((Act) object);
            }
        });
    }

    /**
     * Returns all appointments for the specified schedule and day.
     *
     * @param schedule an <em>party.organisationSchedule</em>
     * @param day      the day
     * @return a list of appointments
     */
    public List<ObjectSet> getAppointments(Party schedule, Date day) {
        List<ObjectSet> result;
        day = getStart(day);
        Key key = new Key(schedule.getObjectReference(), day);
        Element element = cache.get(key);
        if (element != null) {
            synchronized (element) {
                Value value = (Value) element.getObjectValue();
                result = new ArrayList<ObjectSet>(value.getAppointments());
            }
        } else {
            List<ObjectSet> sets = query(schedule, day);
            Value value = new Value(sets);
            result = new ArrayList<ObjectSet>(value.getAppointments());
            cache.put(new Element(key, value));
        }
        return result;
    }

    /**
     * Adds an appointment to the cache.
     *
     * @param appointment the appointment to add
     */
    private void addAppointment(Act appointment) {
        updateCache(appointment, true);
    }

    /**
     * Removes an appointment from the cache.
     *
     * @param appointment the appointment to remove
     */
    private void removeAppointment(Act appointment) {
        updateCache(appointment, false);
    }

    private void updateCache(Act appointment, boolean add) {
        ObjectSet set = Appointment.createObjectSet(appointment, service);
        IMObjectReference schedule
                = set.getReference(Appointment.SCHEDULE_REFERENCE);
        IMObjectReference act
                = set.getReference(Appointment.ACT_REFERENCE);
        Date date = set.getDate(Appointment.ACT_START_TIME);
        if (schedule != null && act != null && date != null) {
            Element element = getElement(schedule, date);
            if (element != null) {
                synchronized (element) {
                    Value value = (Value) element.getObjectValue();
                    if (add) {
                        value.put(act, set);
                    } else {
                        value.remove(act);
                    }
                }
            }
        }
    }

    private Element getElement(IMObjectReference schedule, Date date) {
        date = getStart(date);
        Key key = new Key(schedule, date);
        return cache.get(key);
    }

    private Date getStart(Date day) {
        return DateRules.getDate(day);
    }

    private Date getEnd(Date day) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(day);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }

    private List<ObjectSet> query(Party schedule, Date start) {
        Date end = getEnd(start);
        AppointmentQuery query = new AppointmentQuery(service);
        query.setDateRange(start, end);
        query.setSchedule(schedule);
        return query.query().getResults();
    }

    private static class Key implements Serializable {

        private IMObjectReference schedule;

        private Date day;

        private int hashCode;

        public Key(IMObjectReference schedule, Date day) {
            this.schedule = schedule;
            this.day = day;
            hashCode = schedule.hashCode() + day.hashCode();
        }

        /**
         * Indicates whether some other object is "equal to" this one.
         *
         * @param obj the reference object with which to compare.
         * @return <code>true</code> if this object is the same as the obj
         *         argument; <code>false</code> otherwise.
         */
        @Override
        public boolean equals(Object obj) {
            Key other = (Key) obj;
            return (schedule.equals(other.schedule)) && day.equals(other.day);
        }

        /**
         * Returns a hash code value for the object.
         *
         * @return a hash code value for this object.
         */
        @Override
        public int hashCode() {
            return hashCode;
        }
    }

    private static class Value implements Serializable {
        private final Map<IMObjectReference, ObjectSet> map;
        private List<ObjectSet> sorted;
        private static final SetComparator COMPARATOR = new SetComparator();

        public Value(List<ObjectSet> appointments) {
            map = new HashMap<IMObjectReference, ObjectSet>();
            for (ObjectSet set : appointments) {
                map.put(set.getReference(Appointment.ACT_REFERENCE), set);
            }
            sort();
        }

        public void put(IMObjectReference act, ObjectSet set) {
            map.put(act, set);
            sort();
        }

        public void remove(IMObjectReference act) {
            map.remove(act);
            sort();
        }

        public Collection<ObjectSet> getAppointments() {
            return sorted;
        }

        private void sort() {
            sorted = new ArrayList<ObjectSet>(map.values());
            Collections.sort(sorted, COMPARATOR);
        }

    }

    private static class SetComparator implements Comparator<ObjectSet> {
        /**
         * Compares its two arguments for order.  Returns a negative integer,
         * zero, or a positive integer as the first argument is less than, equal
         * to, or greater than the second.<p>
         * <p/>
         *
         * @param o1 the first object to be compared.
         * @param o2 the second object to be compared.
         * @return a negative integer, zero, or a positive integer as the
         *         first argument is less than, equal to, or greater than the
         *         second.
         * @throws ClassCastException if the arguments' types prevent them from
         *                            being compared by this Comparator.
         */
        public int compare(ObjectSet o1, ObjectSet o2) {
            Date startTime1 = o1.getDate(Appointment.ACT_START_TIME);
            Date startTime2 = o2.getDate(Appointment.ACT_START_TIME);
            int result = DateRules.compareTo(startTime1, startTime2);
            if (result == 0) {
                IMObjectReference ref1 = o1.getReference(
                        Appointment.ACT_REFERENCE);
                IMObjectReference ref2 = o2.getReference(
                        Appointment.ACT_REFERENCE);
                if (ref1.getId() < ref2.getId()) {
                    result = -1;
                } else if (ref1.getId() == ref2.getId()) {
                    result = 0;
                } else {
                    result = 1;
                }
            }
            return result;
        }
    }
}
