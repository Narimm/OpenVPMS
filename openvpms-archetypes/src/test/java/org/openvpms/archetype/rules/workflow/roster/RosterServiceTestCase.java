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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.workflow.roster;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.rules.workflow.ScheduleEvent;
import org.openvpms.archetype.rules.workflow.ScheduleEvents;
import org.openvpms.archetype.rules.workflow.ScheduleTestHelper;
import org.openvpms.archetype.rules.workflow.Times;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.service.cache.BasicEhcacheManager;
import org.openvpms.component.model.act.Act;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.entity.Entity;
import org.openvpms.component.model.object.Reference;
import org.openvpms.component.model.party.Party;
import org.openvpms.component.model.user.User;
import org.openvpms.component.system.common.util.PropertySet;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.openvpms.archetype.test.TestHelper.createUser;
import static org.openvpms.archetype.test.TestHelper.getDate;
import static org.openvpms.archetype.test.TestHelper.getDatetime;

/**
 * Tests the {@link RosterService}.
 *
 * @author Tim Anderson
 */
public class RosterServiceTestCase extends ArchetypeServiceTest {

    /**
     * The roster service.
     */
    private RosterService service;

    /**
     * Test roster area.
     */
    private Entity area;

    /**
     * The practice location.
     */
    private Party location;

    /**
     * Sets up the test.
     */
    @Before
    public void setUp() {
        service = new RosterService(getArchetypeService(), new BasicEhcacheManager(30));
        location = TestHelper.createLocation();
        area = createArea();
    }

    /**
     * Cleans up after the test.
     *
     * @throws Exception for any error
     */
    public void tearDown() throws Exception {
        service.destroy();
    }

    /**
     * Tests the {@link RosterService#getSchedules(Reference)} method.
     */
    @Test
    public void testGetSchedules() {
        IMObjectBean bean = getBean(area);
        Party schedule1 = ScheduleTestHelper.createSchedule(location);
        Party schedule2 = ScheduleTestHelper.createSchedule(location);
        bean.addTarget("schedules", schedule1);
        bean.addTarget("schedules", schedule2);
        bean.save();

        List<Reference> schedules1 = service.getSchedules(area.getObjectReference());
        assertEquals(2, schedules1.size());
        assertTrue(schedules1.contains(schedule1.getObjectReference()));
        assertTrue(schedules1.contains(schedule2.getObjectReference()));

        schedule2.setActive(false);
        save(schedule2);

        List<Reference> schedules2 = service.getSchedules(area.getObjectReference());
        assertEquals(1, schedules2.size());
        assertTrue(schedules2.contains(schedule1.getObjectReference()));
    }

    /**
     * Tests addition of an event.
     */
    @Test
    public void testAddEvent() {
        Date date1 = getDate("2019-01-01");
        Date date2 = getDate("2019-01-02");
        Date date3 = getDate("2019-01-03");

        User user = TestHelper.createUser();

        // retrieve the events for date1 and date2 and verify they are empty.
        // This caches the events for each date.
        ScheduleEvents a1 = checkAreaEvents(area, date1, 0);
        ScheduleEvents u1 = checkUserEvents(user, date1, 0);
        checkAreaEvents(area, date2, 0);
        checkUserEvents(user, date2, 0);

        // create and save an event for date1
        Act event = createEvent(area, date1, user);

        ScheduleEvents a2 = checkAreaEvents(area, date1, 1);
        checkEvent(event, a2.getEvents().get(0));
        assertNotEquals(a1.getModHash(), a2.getModHash());  // hash should have changed

        ScheduleEvents u2 = checkUserEvents(user, date1, 1);
        checkEvent(event, u2.getEvents().get(0));
        assertNotEquals(u1.getModHash(), u2.getModHash());  // hash should have changed

        checkAreaEvents(area, date2, 0);
        checkUserEvents(user, date2, 0);
        checkAreaEvents(area, date3, 0);
        checkUserEvents(user, date3, 0);
    }

    /**
     * Tests moving an event from one date to another.
     */
    @Test
    public void testChangeEventDate() {
        Date date1 = getDate("2019-01-01");
        Date date2 = getDate("2019-03-01");
        User user = TestHelper.createUser();

        ScheduleEvents a1 = checkAreaEvents(area, date1, 0);
        ScheduleEvents a2 = checkAreaEvents(area, date2, 0);
        ScheduleEvents u1 = checkUserEvents(user, date1, 0);
        ScheduleEvents u2 = checkUserEvents(user, date2, 0);

        Act event = createEvent(area, date1, user);

        ScheduleEvents a3 = checkAreaEvents(area, date1, 1);
        assertNotEquals(a1.getModHash(), a3.getModHash());
        checkAreaEvents(area, date2, 0, a2.getModHash());

        ScheduleEvents u3 = checkUserEvents(user, date1, 1);
        assertNotEquals(u1.getModHash(), u3.getModHash());
        checkUserEvents(user, date2, 0, u2.getModHash());

        // move it to date2
        event.setActivityStartTime(date2);
        event.setActivityEndTime(DateRules.getDate(date2, 15, DateUnits.MINUTES));
        save(event);

        ScheduleEvents a4 = checkAreaEvents(area, date1, 0);
        ScheduleEvents u4 = checkUserEvents(user, date1, 0);
        ScheduleEvents a5 = checkAreaEvents(area, date2, 1);
        checkEvent(event, a5.getEvents().get(0));
        ScheduleEvents u5 = checkUserEvents(user, date2, 1);
        checkEvent(event, u5.getEvents().get(0));

        assertNotEquals(a3.getModHash(), a4.getModHash());
        assertNotEquals(a2.getModHash(), a5.getModHash());

        assertNotEquals(u3.getModHash(), u4.getModHash());
        assertNotEquals(u2.getModHash(), u5.getModHash());
    }

    /**
     * Tests moving of an event from one roster area to another.
     */
    @Test
    public void testChangeEventArea() {
        Date date = getDate("2019-01-01");

        Entity area1 = createArea();
        Entity area2 = createArea();

        assertEquals(-1, service.getModHash(area1, date)); // not cached

        ScheduleEvents a1 = checkAreaEvents(area1, date, 0);
        assertNotEquals(-1, a1.getModHash());

        ScheduleEvents a2 = checkAreaEvents(area1, date, 0, a1.getModHash());
        assertEquals(a2.getModHash(), service.getModHash(area1, date));

        Act event = createEvent(area1, date, TestHelper.createUser());
        ScheduleEvents a3 = checkAreaEvents(area1, date, 1);
        assertNotEquals(a3.getModHash(), a2.getModHash()); // Hash should have changed
        assertEquals(a3.getModHash(), service.getModHash(area1, date));

        setArea(event, area2);

        ScheduleEvents a4 = checkAreaEvents(area1, date, 0);
        assertNotEquals(a4.getModHash(), a3.getModHash()); // Hash should have changed
        assertEquals(a4.getModHash(), service.getModHash(area1, date));

        ScheduleEvents a5 = checkAreaEvents(area2, date, 1);
        assertNotEquals(-1, a5.getModHash());
        assertEquals(a5.getModHash(), service.getModHash(area2, date));
    }

    /**
     * Tests moving of an event from one user to another.
     */
    @Test
    public void testChangeEventUser() {
        Date date = getDate("2019-01-01");

        User user1 = TestHelper.createUser();
        User user2 = TestHelper.createUser();

        assertEquals(-1, service.getModHash(user1, date)); // not cached

        ScheduleEvents a1 = checkUserEvents(user1, date, 0);
        assertNotEquals(-1, a1.getModHash());

        ScheduleEvents a2 = checkUserEvents(user1, date, 0, a1.getModHash());
        assertEquals(a2.getModHash(), getUserModHash(user1, date));

        Act event = createEvent(area, date, user1);
        ScheduleEvents a3 = checkUserEvents(user1, date, 1);
        assertNotEquals(a3.getModHash(), a2.getModHash()); // Hash should have changed
        assertEquals(a3.getModHash(), getUserModHash(user1, date));

        IMObjectBean bean = getBean(event);
        bean.setTarget("user", user2);
        bean.save();

        ScheduleEvents a4 = checkUserEvents(user1, date, 0);
        assertNotEquals(a4.getModHash(), a3.getModHash()); // Hash should have changed
        assertEquals(a4.getModHash(), getUserModHash(user1, date));

        ScheduleEvents a5 = checkUserEvents(user2, date, 1);
        assertNotEquals(-1, a5.getModHash());
        assertEquals(a5.getModHash(), getUserModHash(user2, date));
    }

    /**
     * Tests removal of an event.
     */
    @Test
    public void testRemoveEvent() {
        Date date1 = getDate("2019-01-01");
        Date date2 = getDate("2019-01-02");
        Date date3 = getDate("2019-01-03");

        User user = TestHelper.createUser();

        // retrieve the appointments for date1 and date2 and verify they are empty.
        // This caches the appointments for each date.

        ScheduleEvents a1 = checkAreaEvents(area, date1, 0);
        ScheduleEvents a2 = checkAreaEvents(area, date2, 0);
        ScheduleEvents a3 = checkAreaEvents(area, date3, 0);
        assertNotEquals(a1.getModHash(), a2.getModHash());
        assertNotEquals(a1.getModHash(), a3.getModHash());

        // create and save an event for date1
        Act event = createEvent(area, date1, user);
        ScheduleEvents a4 = checkAreaEvents(area, date1, 1);
        assertNotEquals(a1.getModHash(), a4.getModHash());
        checkAreaEvents(area, date2, 0, a2.getModHash());
        checkAreaEvents(area, date3, 0, a3.getModHash());

        // now remove it
        remove(event);

        // verify it has been removed
        ScheduleEvents a5 = checkAreaEvents(area, date1, 0);
        assertNotEquals(a1.getModHash(), a5.getModHash());

        checkAreaEvents(area, date2, 0, a2.getModHash());
        checkAreaEvents(area, date3, 0, a3.getModHash());
    }

    /**
     * Verifies that events that start or end on a date boundary are returned in the correct days.
     */
    @Test
    public void testAddEventOnDayBoundary() {
        Date date1 = getDate("2019-01-01");
        Date date2 = getDate("2019-01-02");
        Date date3 = getDate("2019-01-03");
        Date date4 = getDate("2019-01-04");
        User user = createUser();
        Act event1 = createEvent(area, "2019-01-02 00:00", "2019-01-02 08:00", user);
        Act event2 = createEvent(area, "2019-01-03 14:00", "2019-01-04 00:00", user);

        checkAreaEvents(area, date1, 0);
        checkUserEvents(user, date1, 0);

        ScheduleEvents a2 = checkAreaEvents(area, date2, 1);
        checkEvent(event1, a2.getEvents().get(0));
        ScheduleEvents u2 = checkUserEvents(user, date2, 1);
        checkEvent(event1, u2.getEvents().get(0));

        ScheduleEvents a3 = checkAreaEvents(area, date3, 1);
        checkEvent(event2, a3.getEvents().get(0));
        ScheduleEvents u3 = checkUserEvents(user, date3, 1);
        checkEvent(event2, u3.getEvents().get(0));

        checkAreaEvents(area, date4, 0);
        checkUserEvents(user, date4, 0);
    }

    /**
     * Verifies that events that span multiple days are returned in the correct days.
     */
    @Test
    public void testMultiDayEvent() {
        Date date1 = getDate("2019-01-01");
        Date date2 = getDate("2019-01-02");
        Date date3 = getDate("2019-01-03");
        Date date4 = getDate("2019-01-04");
        User user = createUser();
        Act event = createEvent(area, "2019-01-02 18:00", "2019-01-03 06:00", user);

        checkAreaEvents(area, date1, 0);
        checkUserEvents(user, date1, 0);

        ScheduleEvents a2 = checkAreaEvents(area, date2, 1);
        checkEvent(event, a2.getEvents().get(0));
        ScheduleEvents u2 = checkUserEvents(user, date2, 1);
        checkEvent(event, u2.getEvents().get(0));

        ScheduleEvents a3 = checkAreaEvents(area, date3, 1);
        checkEvent(event, a3.getEvents().get(0));
        ScheduleEvents u3 = checkUserEvents(user, date3, 1);
        checkEvent(event, u3.getEvents().get(0));

        checkAreaEvents(area, date4, 0);
        checkUserEvents(user, date4, 0);

        // now remove the event and verify it is no longer returned
        remove(event);

        checkAreaEvents(area, date2, 0);
        checkUserEvents(user, date2, 0);

        checkAreaEvents(area, date3, 0);
        checkUserEvents(user, date3, 0);
    }

    /**
     * Reads different areas in two threads, whilst changing an event's area in a third.
     * <p>
     * Verifies that the areas contains the expected result.
     */
    @Test
    public void testConcurrentChangeArea() throws Exception {
        for (int i = 0; i < 100; i++) {
            System.out.println("Concurrent read/write run: " + i + " ");
            Entity area1 = createArea();
            Entity area2 = createArea();
            User user = TestHelper.createUser();
            Date date = getDate("2019-02-01");

            Act event = createEvent(area1, date, user);

            Callable<PropertySet> read1 = () -> {
                System.err.println("Read date1 thread=" + Thread.currentThread().getName());
                List<PropertySet> events = service.getEvents(area1, date);
                assertFalse(events.size() > 1);
                checkUserEvents(user, date, 1);
                return events.isEmpty() ? null : events.get(0);
            };
            Callable<PropertySet> read2 = () -> {
                System.err.println("Read date2 thread=" + Thread.currentThread().getName());
                List<PropertySet> events = service.getEvents(area2, date);
                assertFalse(events.size() > 1);
                checkUserEvents(user, date, 1);
                return events.isEmpty() ? null : events.get(0);
            };
            Callable<PropertySet> write = () -> {
                System.err.println("Writer thread=" + Thread.currentThread().getName());
                setArea(event, area2);
                return null;
            };

            runConcurrent(read1, read2, write);

            checkAreaEvents(area1, date, 0);
            checkAreaEvents(area2, date, 1);
            checkUserEvents(user, date, 1);
            System.out.println("OK");
        }
    }

    /**
     * Reads different users in two threads, whilst changing an event's user in a third.
     * <p>
     * Verifies that the areas contains the expected result.
     */
    @Test
    public void testConcurrentChangeUser() throws Exception {
        for (int i = 0; i < 100; i++) {
            System.out.println("Concurrent read/write run: " + i + " ");
            Entity area = createArea(); // need a different area each run
            User user1 = TestHelper.createUser();
            User user2 = TestHelper.createUser();
            Date date = getDate("2019-02-01");
            Date next = DateRules.getNextDate(date);

            Act event = createEvent(area, date, user1);

            Callable<PropertySet> read1 = () -> {
                System.err.println("Read date1 thread=" + Thread.currentThread().getName());
                List<PropertySet> events = service.getUserEvents(user1, date, next).getEvents();
                assertFalse(events.size() > 1);
                checkAreaEvents(area, date, 1);
                return events.isEmpty() ? null : events.get(0);
            };
            Callable<PropertySet> read2 = () -> {
                System.err.println("Read date2 thread=" + Thread.currentThread().getName());
                List<PropertySet> events = service.getUserEvents(user2, date, next).getEvents();
                assertFalse(events.size() > 1);
                checkAreaEvents(area, date, 1);
                return events.isEmpty() ? null : events.get(0);
            };
            Callable<PropertySet> write = () -> {
                System.err.println("Writer thread=" + Thread.currentThread().getName());
                IMObjectBean bean = getBean(event);
                bean.setTarget("user", user2);
                bean.save();
                return null;
            };

            runConcurrent(read1, read2, write);

            checkUserEvents(user1, date, 0);
            checkUserEvents(user2, date, 1);
            checkAreaEvents(area, date, 1);
            System.out.println("OK");
        }
    }

    /**
     * Tests the {@link RosterService#getOverlappingEvents(List, User, int)} method.
     */
    @Test
    public void testGetOverlappingEvents() {
        Date start1 = getDatetime("2019-02-14 09:00:00");
        Date end1 = getDatetime("2019-02-14 09:15:00");
        Date start2 = getDatetime("2019-02-15 09:00:00");
        Date end2 = getDatetime("2019-02-15 09:15:00");
        Date beforeStart = getDatetime("2019-02-15 08:45:00");
        Date beforeEnd = getDatetime("2019-02-15 09:00:00");
        Date afterStart = getDatetime("2019-02-15 09:30:00");
        Date afterEnd = getDatetime("2019-02-15 09:45:00");
        Date overlap1Start = getDatetime("2019-02-15 09:05:00");
        Date overlap1End = getDatetime("2019-02-15 09:20:00");
        Date overlap2Start = getDatetime("2019-02-15 09:10:00");
        Date overlap2End = getDatetime("2019-02-15 09:25:00");

        User user = TestHelper.createUser();

        Times times1 = new Times(start1, end1);
        Times times2 = new Times(start2, end2);
        List<Times> list = Arrays.asList(times1, times2);
        assertNull(service.getOverlappingEvents(list, user, 1));

        // overlaps time1 exactly
        Act event1 = createEvent(area, start1, end1, user);
        checkTimes(service.getOverlappingEvents(list, user, 1), times1);
        remove(event1);

        // overlaps time2 exactly
        Act event2 = createEvent(area, start2, end2, user);
        checkTimes(service.getOverlappingEvents(list, user, 1), times2);
        remove(event2);

        // before time2
        createEvent(area, beforeStart, beforeEnd, user);
        assertNull(service.getOverlappingEvents(list, user, 1));

        // after time2
        createEvent(area, afterStart, afterEnd, user);
        assertNull(service.getOverlappingEvents(list, user, 1));

        // intersects start of time2
        Act event5 = createEvent(area, overlap1Start, overlap1End, user);
        checkTimes(service.getOverlappingEvents(list, user, 1), Times.create(event5));
        remove(event5);

        // intersects end of time2
        Act event6 = createEvent(area, overlap2Start, overlap2End, user);
        checkTimes(service.getOverlappingEvents(list, user, 1), Times.create(event6));
    }

    /**
     * Verifies that event times match those expected.
     *
     * @param events the overlapping events
     * @param times  the expected times
     */
    private void checkTimes(List<Times> events, Times... times) {
        assertEquals(times.length, events.size());
        for (int i = 0; i < times.length; ++i) {
            Times expected = times[i];
            Times actual = events.get(i);
            assertEquals(expected.getStartTime(), actual.getStartTime());
            assertEquals(expected.getEndTime(), actual.getEndTime());
        }
    }

    /**
     * Helper to runs tasks concurrently.
     *
     * @param tasks the tasks to run
     * @throws Exception for any error
     */
    @SafeVarargs
    private final void runConcurrent(Callable<PropertySet>... tasks) throws Exception {
        List<Callable<PropertySet>> list = Arrays.asList(tasks);
        ExecutorService executorService = Executors.newFixedThreadPool(list.size());
        List<Future<PropertySet>> futures = executorService.invokeAll(list);

        assertEquals(tasks.length, futures.size());
        for (Future future : futures) {
            future.get();
        }
    }

    private void setArea(Act event, Entity area) {
        IMObjectBean bean = getBean(event);
        bean.setTarget("schedule", area);
        bean.save();
    }


    /**
     * Verifies events match those expected for a user and date.
     *
     * @param user    the user
     * @param date    the date
     * @param size    the expected no. of events
     * @param modHash the expected modification hash
     * @return the events
     */
    private ScheduleEvents checkUserEvents(User user, Date date, int size, long modHash) {
        ScheduleEvents events = checkUserEvents(user, date, size);
        assertEquals(modHash, events.getModHash());
        return events;
    }

    /**
     * Creates a new roster area.
     *
     * @return a new roster area
     */
    private Entity createArea() {
        Entity result = (Entity) create(RosterArchetypes.ROSTER_AREA);
        result.setName("Z Roster Area");
        IMObjectBean bean = getBean(result);
        bean.setTarget("location", location);
        bean.save();
        return result;
    }

    /**
     * Verifies events match those expected for a roster area and date.
     *
     * @param area    the roster area
     * @param date    the date
     * @param size    the expected no. of events
     * @param modHash the expected modification hash
     * @return the events
     */
    private ScheduleEvents checkAreaEvents(Entity area, Date date, int size, long modHash) {
        ScheduleEvents events = checkAreaEvents(area, date, size);
        assertEquals(modHash, events.getModHash());
        return events;
    }

    /**
     * Verifies events match those expected for a roster area and date.
     *
     * @param area the roster area
     * @param date the date
     * @param size the expected no. of events
     * @return the events
     */
    private ScheduleEvents checkAreaEvents(Entity area, Date date, int size) {
        ScheduleEvents events = service.getScheduleEvents(area, date);
        assertEquals(size, events.size());
        return events;
    }

    /**
     * Verifies events match those expected for a user and date.
     *
     * @param user the user
     * @param date the date
     * @param size the expected no. of events
     * @return the modification hash
     */
    private ScheduleEvents checkUserEvents(User user, Date date, int size) {
        ScheduleEvents events = service.getUserEvents(user, date, DateRules.getNextDate(date));
        assertEquals(size, events.size());
        return events;
    }

    private void checkUserModHash(User user, Date date, long expected) {
        long hash = getUserModHash(user, date);
        assertEquals(expected, hash);
    }

    private long getUserModHash(User user, Date date) {
        return service.getUserModHash(user, date, DateRules.getNextDate(date));
    }

    /**
     * Verifies that a roster event matches the {@link PropertySet} representing it.
     *
     * @param act the roster event
     * @param set the set
     */
    private void checkEvent(Act act, PropertySet set) {
        IMObjectBean bean = getBean(act);
        assertEquals(act.getObjectReference(), set.get(ScheduleEvent.ACT_REFERENCE));
        assertEquals(act.getActivityStartTime(), set.get(ScheduleEvent.ACT_START_TIME));
        assertEquals(act.getActivityEndTime(), set.get(ScheduleEvent.ACT_END_TIME));
        assertEquals(bean.getTargetRef("schedule"), set.get(ScheduleEvent.SCHEDULE_REFERENCE));
        assertEquals(bean.getTarget("schedule").getName(), set.get(ScheduleEvent.SCHEDULE_NAME));
        assertEquals(bean.getTargetRef("location"), set.get(RosterEvent.LOCATION_REFERENCE));
        assertEquals(bean.getTarget("location").getName(), set.get(RosterEvent.LOCATION_NAME));
        assertEquals(bean.getTargetRef("user"), set.get(RosterEvent.USER_REFERENCE));
        assertEquals(bean.getTarget("user").getName(), set.get(RosterEvent.USER_NAME));
    }

    /**
     * Helper to create an event.
     *
     * @param area the area
     * @param date the date
     * @param user the user
     * @return a new event
     */
    private Act createEvent(Entity area, Date date, User user) {
        Date startTime = DateRules.getDate(date, 8, DateUnits.HOURS);
        Date endTime = DateRules.getDate(date, 17, DateUnits.HOURS);
        return createEvent(area, startTime, endTime, user);
    }

    /**
     * Helper to create an event.
     *
     * @param area      the area
     * @param startTime the start time
     * @param endTime   the end time
     * @param user      the user
     * @return a new event
     */
    private Act createEvent(Entity area, String startTime, String endTime, User user) {
        return createEvent(area, getDatetime(startTime), getDatetime(endTime), user);
    }

    /**
     * Helper to create an event.
     *
     * @param area      the area
     * @param startTime the start time
     * @param endTime   the end time
     * @param user      the user
     * @return a new event
     */
    private Act createEvent(Entity area, Date startTime, Date endTime, User user) {
        Act event = (Act) create(RosterArchetypes.ROSTER_EVENT);
        event.setActivityStartTime(startTime);
        event.setActivityEndTime(endTime);
        IMObjectBean bean = getBean(event);
        bean.setTarget("schedule", area);
        bean.setTarget("user", user);
        bean.setTarget("location", location);
        bean.save();
        return event;
    }
}
