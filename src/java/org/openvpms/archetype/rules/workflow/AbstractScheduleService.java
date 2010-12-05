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
import org.openvpms.component.business.service.archetype.AbstractArchetypeServiceListener;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.LookupHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.NodeSelectConstraint;
import org.openvpms.component.system.common.query.ObjectRefConstraint;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.ObjectSetQueryIterator;
import org.openvpms.component.system.common.util.PropertySet;

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

    /**
     * The set of acts pending removal from the cache. These will be
     * removed on transaction commit.
     */
    private Map<Long, Act> pending
            = Collections.synchronizedMap(new HashMap<Long, Act>());

    /**
     * Status lookup names, keyed on code.
     */
    private final Map<String, String> statusNames;


    /**
     * Creates a new <tt>AbstractScheduleService</tt>.
     *
     * @param eventShortName the event act archetype short name
     * @param service        the archetype service
     * @param cache          the event cache
     */
    public AbstractScheduleService(String eventShortName,
                                   IArchetypeService service,
                                   Cache cache) {
        this.service = service;
        this.cache = cache;

        statusNames = LookupHelper.getNames(service, eventShortName, "status");

        // add a listener to receive notifications from the archetype service
        service.addListener(eventShortName, new AbstractArchetypeServiceListener() {

            @Override
            public void save(IMObject object) {
                addPending((Act) object);
            }

            @Override
            public void remove(IMObject object) {
                addPending((Act) object);
            }

            @Override
            public void saved(IMObject object) {
                addEvent((Act) object);
            }

            @Override
            public void removed(IMObject object) {
                removeEvent((Act) object);
            }

            @Override
            public void rollback(IMObject object) {
                removePending((Act) object);
            }
        });
    }

    /**
     * Returns all events for the specified schedule and day.
     * Events are represented by {@link PropertySet PropertySets}.
     *
     * @param schedule the schedule
     * @param day      the day
     * @return a list of events
     */
    public List<PropertySet> getEvents(Entity schedule, Date day) {
        List<PropertySet> result;
        day = getStart(day);
        Key key = new Key(schedule.getObjectReference(), day);
        Element element = cache.get(key);
        if (element != null) {
            synchronized (element) {
                Value value = (Value) element.getObjectValue();
                result = copy(value);
            }
        } else {
            List<PropertySet> sets = query(schedule, day);
            Value value = new Value(sets);
            result = copy(value);
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
    public List<PropertySet> getEvents(Entity schedule, Date from, Date to) {
        Date fromDay = getStart(from);
        Date toDay = getStart(to);
        List<PropertySet> results = new ArrayList<PropertySet>();
        while (fromDay.compareTo(toDay) <= 0) {
            for (PropertySet event : getEvents(schedule, fromDay)) {
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

    /**
     * Returns the schedule reference from an event.
     *
     * @param event the event
     * @return a reference to the schedule. May be <tt>null</tt>
     */
    protected abstract IMObjectReference getSchedule(Act event);

    /**
     * Creates a new query to query events for the specified schedule and date
     * range.
     *
     * @param schedule the schedule
     * @param from     the start time
     * @param to       the end time
     * @return a new query
     */
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
        removeEvent(event); // remove the prior instance, if any

        ActBean bean = new ActBean(event, service);
        ObjectSet set = new ObjectSet();
        assemble(set, bean);
        IMObjectReference schedule
                = set.getReference(ScheduleEvent.SCHEDULE_REFERENCE);
        IMObjectReference act = set.getReference(ScheduleEvent.ACT_REFERENCE);
        addEvent(schedule, act, set);
    }

    /**
     * Adds an event to the cache.
     *
     * @param schedule the event schedule
     * @param act      the event act reference
     * @param set      the <tt>PropertySet</tt> representation of the event
     */
    protected void addEvent(IMObjectReference schedule, IMObjectReference act,
                            PropertySet set) {
        Date date = set.getDate(ScheduleEvent.ACT_START_TIME);
        addEvent(schedule, act, date, set);
    }

    /**
     * Adds an event to the cache.
     *
     * @param schedule the event schedule
     * @param act      the event act reference
     * @param date     the date
     * @param set      the <tt>PropertySet</tt> representation of the event
     */
    protected void addEvent(IMObjectReference schedule, IMObjectReference act,
                            Date date, PropertySet set) {
        if (schedule != null && act != null && date != null) {
            Element element = getElement(schedule, date);
            if (element != null) {
                add(element, act, set);
            }
        }
    }

    /**
     * Removes an event from the cache.
     * <p/>
     * This invokes {@link #removeEvent(Act, IMObjectReference)} with the
     * original version of the event, if present.
     *
     * @param event the event to remove
     */
    protected void removeEvent(Act event) {
        Act original = pending.remove(event.getId());
        if (original != null) {
            IMObjectReference schedule = getSchedule(original);
            removeEvent(original, schedule);
        }
    }

    /**
     * Removes an event from the cache.
     *
     * @param event    the event to remove
     * @param schedule the schedule to remove the event from
     */
    protected void removeEvent(Act event, IMObjectReference schedule) {
        IMObjectReference act = event.getObjectReference();
        Date date = getStart(event.getActivityStartTime());
        Element element = getElement(schedule, date);
        if (element != null) {
            remove(element, act);
        }
    }

    /**
     * Adds an act to the cache.
     *
     * @param element the cache element
     * @param act     the act reference
     * @param set     the set representing the act
     */
    protected void add(Element element, IMObjectReference act,
                       PropertySet set) {
        synchronized (element) {
            Value value = (Value) element.getObjectValue();
            value.put(act, set);
        }
    }

    /**
     * Removes an act from the cache.
     *
     * @param element the cache element
     * @param act     the act reference
     * @return <tt>true</tt> if the act was removed
     */
    protected boolean remove(Element element, IMObjectReference act) {
        synchronized (element) {
            Value value = (Value) element.getObjectValue();
            return value.remove(act);
        }
    }

    /**
     * Returns all cache elements for the given schedule and date range.
     *
     * @param schedule the reference to the schedule
     * @param from     the start date
     * @param to       the end date. May be <tt>null</tt>
     * @return cache elements matching the schedule and date range
     */
    protected List<Element> getElements(IMObjectReference schedule,
                                        Date from, Date to) {
        List keys = cache.getKeysNoDuplicateCheck();
        List<Element> result = new ArrayList<Element>();
        for (Object key : keys) {
            Key k = (Key) key;
            if (k.getSchedule().equals(schedule) && k.dayInRange(from, to)) {
                Element element = cache.get(key);
                if (element != null) {
                    result.add(element);
                }
            }
        }
        return result;
    }

    /**
     * Assembles an {@link PropertySet PropertySet} from a source act.
     *
     * @param target the target set
     * @param source the source act
     */
    protected void assemble(PropertySet target, ActBean source) {
        Act event = source.getAct();
        String status = event.getStatus();
        target.set(ScheduleEvent.ACT_REFERENCE, event.getObjectReference());
        target.set(ScheduleEvent.ACT_START_TIME, event.getActivityStartTime());
        target.set(ScheduleEvent.ACT_END_TIME, event.getActivityEndTime());
        target.set(ScheduleEvent.ACT_STATUS, status);
        target.set(ScheduleEvent.ACT_STATUS_NAME, statusNames.get(status));
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

    /**
     * Clears the cache.
     */
    protected void clearCache() {
        cache.removeAll();
    }

    /**
     * Invoked prior to an event being added or removed from the cache.
     * <p/>
     * If the event is already persistent, the persistent instance will be
     * added to the map of acts that need to be removed prior to any new
     * instance being cached.
     *
     * @param event the event
     */
    private void addPending(Act event) {
        if (!event.isNew() && !pending.containsKey(event.getId())) {
            Act original = (Act) service.get(event.getObjectReference());
            if (original != null) {
                pending.put(event.getId(), original);
            }
        }
    }

    /**
     * Invoked on transaction rollback.
     * <p/>
     * This removes the associated event from the map of acts pending removal.
     *
     * @param event the rolled back event
     */
    private void removePending(Act event) {
        pending.remove(event.getId());
    }

    /**
     * Returns the cache element for the specified schedule and date.
     *
     * @param schedule the schedule reference
     * @param date     the date
     * @return the corresponding cache element, or <tt>null</tt> if none is
     *         found
     */
    private Element getElement(IMObjectReference schedule, Date date) {
        date = getStart(date);
        Key key = new Key(schedule, date);
        return cache.get(key);
    }

    /**
     * Returns the start of the specified date-time.
     *
     * @param datetime the date-time
     * @return the date part of <tt>datetime</tt>, zero-ing out any time
     *         component.
     */
    private Date getStart(Date datetime) {
        return DateRules.getDate(datetime);
    }

    /**
     * Returns the end of the day for the specified date-time.
     *
     * @param datetime the date-time
     * @return one millisecond to midnight for the specified date
     */
    private Date getEnd(Date datetime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(datetime);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }

    /**
     * Queries all events for the specified date.
     *
     * @param schedule the schedule
     * @param start    the start date
     * @return all events for the date
     */
    private List<PropertySet> query(Entity schedule, Date start) {
        Date end = getEnd(start);
        ScheduleEventQuery query = createQuery(schedule, start, end);
        IPage<ObjectSet> page = query.query();
        return new ArrayList<PropertySet>(page.getResults());
    }

    /**
     * Returns a shallow copy of the events.
     *
     * @param value the cache value
     * @return a shallow copy of the cached events
     */
    private List<PropertySet> copy(Value value) {
        List<PropertySet> result = new ArrayList<PropertySet>();
        for (PropertySet set : value.getEvents()) {
            result.add(new ObjectSet(set));
        }
        return result;
    }

    /**
     * Cache key.
     */
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

        /**
         * Returns the day.
         *
         * @return the day
         */
        public Date getDay() {
            return day;
        }

        /**
         * Determines if the day falls in the specified date range.
         *
         * @param from the from date
         * @param to   the to date. May be <tt>null</tt>
         * @return <tt>true</tt> if the day falls in the date range
         */
        public boolean dayInRange(Date from, Date to) {
            return (DateRules.compareTo(from, day) <= 0
                    && (to == null || DateRules.compareTo(day, to) <= 0));
        }
    }

    /**
     * Cache value.
     */
    private static class Value implements Serializable {

        private final Map<IMObjectReference, PropertySet> map;

        private List<PropertySet> sorted;

        private static final SetComparator COMPARATOR = new SetComparator();

        public Value(List<PropertySet> events) {
            map = new HashMap<IMObjectReference, PropertySet>();
            for (PropertySet set : events) {
                map.put(set.getReference(ScheduleEvent.ACT_REFERENCE), set);
            }
            sort();
        }

        public void put(IMObjectReference act, PropertySet set) {
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

        public Collection<PropertySet> getEvents() {
            return sorted;
        }

        private void sort() {
            sorted = new ArrayList<PropertySet>(map.values());
            Collections.sort(sorted, COMPARATOR);
        }

    }

    private static class SetComparator implements Comparator<PropertySet> {

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
        public int compare(PropertySet o1, PropertySet o2) {
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
