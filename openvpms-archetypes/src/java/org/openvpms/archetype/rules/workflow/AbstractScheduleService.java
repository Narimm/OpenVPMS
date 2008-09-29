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
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.IArchetypeServiceListener;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.NodeSelectConstraint;
import org.openvpms.component.system.common.query.ObjectRefConstraint;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.ObjectSetQueryIterator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * Abstract implementation of the {@link ScheduleService}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractScheduleService implements ScheduleService {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The cache.
     */
    private final Cache cache;


    public AbstractScheduleService(String eventShortName,
                                   IArchetypeService service,
                                   Cache cache) {
        service.addListener(
                eventShortName, new IArchetypeServiceListener() {
            public void saved(IMObject object) {
                addEvent((Act) object);
            }

            public void removed(IMObject object) {
                removeEvent((Act) object);
            }
        });

        this.service = service;
        this.cache = cache;
    }

    /**
     * Returns all events for the specified schedule and day.
     * Events are represented by {@link ObjectSet ObjectSets}.
     *
     * @param schedule the schedule
     * @param day      the day
     * @return a list of events
     */
    public List<ObjectSet> getEvents(Entity schedule, Date day) {
        List<ObjectSet> result;
        day = getStart(day);
        Key key = new Key(schedule.getObjectReference(), day);
        Element element = cache.get(key);
        if (element != null) {
            synchronized (element) {
                Value value = (Value) element.getObjectValue();
                result = new ArrayList<ObjectSet>(value.getEvents());
            }
        } else {
            List<ObjectSet> sets = query(schedule, day);
            Value value = new Value(sets);
            result = new ArrayList<ObjectSet>(value.getEvents());
            cache.put(new Element(key, value));
        }
        return result;
    }

    /**
     * Returns all events for the specified schedule, and time range.
     *
     * @param schedule the schedule
     * @return a list of events
     */
    public List<ObjectSet> getEvents(Entity schedule, Date from, Date to) {
        Date fromDay = getStart(from);
        Date toDay = getStart(to);
        List<ObjectSet> results = new ArrayList<ObjectSet>();
        while (fromDay.compareTo(toDay) <= 0) {
            for (ObjectSet event : getEvents(schedule, fromDay)) {
                Date startTime = event.getDate(
                        ScheduleEvent.ACT_START_TIME);
                Date endTime = event.getDate(ScheduleEvent.ACT_END_TIME);
                if (DateRules.intersects(startTime, endTime, from, to)) {
                    results.add(event);
                } else if (DateRules.compareTo(startTime, to) >= 0) {
                    break;
                }
            }
            fromDay = DateRules.getDate(fromDay, 1, DateUnits.DAYS);
        }

        return results;
    }


    protected abstract IMObjectReference getSchedule(Act event);


    protected abstract ScheduleEventQuery createQuery(Entity schedule,
                                                      Date from, Date to);

    /**
     * Returns the archetype service.
     *
     * @return the archetype service
     */
    protected IArchetypeService getService() {
        return service;
    }

    /**
     * Adds an event to the cache.
     *
     * @param event the event to add
     */
    protected void addEvent(Act event) {
        removeEvent(event);

        ActBean bean = new ActBean(event, service);
        ObjectSet set = new ObjectSet();
        assemble(set, bean);
        IMObjectReference schedule
                = set.getReference(ScheduleEvent.SCHEDULE_REFERENCE);
        IMObjectReference act = set.getReference(ScheduleEvent.ACT_REFERENCE);
        Date date = set.getDate(ScheduleEvent.ACT_START_TIME);
        if (schedule != null && act != null && date != null) {
            Element element = getElement(schedule, date);
            if (element != null) {
                synchronized (element) {
                    Value value = (Value) element.getObjectValue();
                    value.put(act, set);
                }
            }
        }
    }

    /**
     * Removes an event from the cache.
     *
     * @param event the event to remove
     */
    protected void removeEvent(Act event) {
        Date date = getStart(event.getActivityStartTime());
        IMObjectReference act = event.getObjectReference();
        IMObjectReference schedule = getSchedule(event);
        boolean removed = false;
        Element element = getElement(schedule, date);
        if (element != null && remove(element, act)) {
            removed = true;
        }
        if (!removed) {
            List keys = cache.getKeysNoDuplicateCheck();
            for (Object key : keys) {
                Key k = (Key) key;
                if (k.getSchedule().equals(schedule)) {
                    element = cache.get(key);
                    if (element != null && remove(element, act)) {
                        break;
                    }
                }
            }
        }
    }

    protected void assemble(ObjectSet target, ActBean source) {
        Act event = source.getAct();
        target.set(ScheduleEvent.ACT_REFERENCE, event.getObjectReference());
        target.set(ScheduleEvent.ACT_START_TIME, event.getActivityStartTime());
        target.set(ScheduleEvent.ACT_END_TIME, event.getActivityEndTime());
        target.set(ScheduleEvent.ACT_STATUS, event.getStatus());
        target.set(ScheduleEvent.ACT_REASON, event.getReason());
        target.set(ScheduleEvent.ACT_DESCRIPTION, event.getDescription());

        IMObjectReference customerRef
                = source.getNodeParticipantRef("customer");
        String customerName = getName(customerRef);
        target.set(ScheduleEvent.CUSTOMER_REFERENCE, customerRef);
        target.set(ScheduleEvent.CUSTOMER_NAME, customerName);

        IMObjectReference patientRef = source.getNodeParticipantRef("patient");
        String patientName = getName(patientRef);
        target.set(ScheduleEvent.PATIENT_REFERENCE, patientRef);
        target.set(ScheduleEvent.PATIENT_NAME, patientName);


        IMObjectReference clinicianRef
                = source.getNodeParticipantRef("clinician");
        String clinicianName = getName(clinicianRef);
        target.set(ScheduleEvent.CLINICIAN_REFERENCE, clinicianRef);
        target.set(ScheduleEvent.CLINICIAN_NAME, clinicianName);
    }

    /**
     * Returns the name of an object, given its reference.
     *
     * @param reference the object reference. May be <tt>null</tt>
     * @return the name or <tt>null</tt> if none exists
     */
    protected String getName(IMObjectReference reference) {
        if (reference != null) {
            ObjectRefConstraint constraint
                    = new ObjectRefConstraint("o", reference);
            ArchetypeQuery query = new ArchetypeQuery(constraint);
            query.add(new NodeSelectConstraint("o.name"));
            query.setMaxResults(1);
            Iterator<ObjectSet> iter = new ObjectSetQueryIterator(service,
                                                                  query);
            if (iter.hasNext()) {
                ObjectSet set = iter.next();
                return set.getString("o.name");
            }
        }
        return null;
    }

    private boolean remove(Element element, IMObjectReference act) {
        synchronized (element) {
            Value value = (Value) element.getObjectValue();
            return value.remove(act);
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

    private List<ObjectSet> query(Entity schedule, Date start) {
        Date end = getEnd(start);
        ScheduleEventQuery query = createQuery(schedule, start, end);
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

        /**
         * Returns the schedule reference.
         *
         * @return the schedule reference
         */
        public IMObjectReference getSchedule() {
            return schedule;
        }
    }

    private static class Value implements Serializable {
        private final Map<IMObjectReference, ObjectSet> map;
        private List<ObjectSet> sorted;
        private static final SetComparator COMPARATOR = new SetComparator();

        public Value(List<ObjectSet> events) {
            map = new HashMap<IMObjectReference, ObjectSet>();
            for (ObjectSet set : events) {
                map.put(set.getReference(ScheduleEvent.ACT_REFERENCE), set);
            }
            sort();
        }

        public void put(IMObjectReference act, ObjectSet set) {
            map.put(act, set);
            sort();
        }

        public boolean remove(IMObjectReference act) {
            boolean result = (map.remove(act) != null);
            if (result) {
                sort();
            }
            return result;
        }

        public Collection<ObjectSet> getEvents() {
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
            Date startTime1 = o1.getDate(ScheduleEvent.ACT_START_TIME);
            Date startTime2 = o2.getDate(ScheduleEvent.ACT_START_TIME);
            int result = DateRules.compareTo(startTime1, startTime2);
            if (result == 0) {
                IMObjectReference ref1 = o1.getReference(
                        ScheduleEvent.ACT_REFERENCE);
                IMObjectReference ref2 = o2.getReference(
                        ScheduleEvent.ACT_REFERENCE);
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
