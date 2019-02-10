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

package org.openvpms.archetype.rules.workflow;

import org.junit.Test;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.model.act.Act;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.entity.Entity;
import org.openvpms.component.model.object.Reference;
import org.openvpms.component.model.party.Party;
import org.openvpms.component.model.user.User;
import org.openvpms.component.system.common.util.PropertySet;
import org.springframework.beans.factory.DisposableBean;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.openvpms.archetype.test.TestHelper.getDate;

/**
 * Base class for {@link ScheduleService} tests.
 *
 * @author Tim Anderson
 */
public abstract class AbstractScheduleServiceTest extends ArchetypeServiceTest {

    /**
     * The schedule service.
     */
    private ScheduleService service;

    /**
     * Cleans up after the test.
     *
     * @throws Exception for any error
     */
    public void tearDown() throws Exception {
        destroyService(service);
    }

    /**
     * Tests addition of an event.
     */
    @Test
    public abstract void testAddEvent();

    /**
     * Tests removal of an event.
     */
    @Test
    public abstract void testRemoveEvent();

    /**
     * Tests moving of an event from one date to another.
     */
    @Test
    public abstract void testChangeEventDate();

    /**
     * Tests moving of an event from one schedule to another.
     */
    @Test
    public void testChangeEventSchedule() {
        Date date = getDate("2008-01-01");

        Entity schedule1 = createSchedule();
        Entity schedule2 = createSchedule();
        Party patient = TestHelper.createPatient();

        initScheduleService(30);

        assertEquals(-1, service.getModHash(schedule1, date)); // not cached

        long hash1 = checkEvents(schedule1, date, 0);
        assertNotEquals(-1, hash1);

        checkEvents(schedule1, date, 0, hash1);
        assertEquals(hash1, service.getModHash(schedule1, date));

        Act act = createEvent(schedule1, date, patient);
        long hash2 = checkEvents(schedule1, date, 1);
        assertNotEquals(hash2, hash1); // Hash should have changed
        assertEquals(hash2, service.getModHash(schedule1, date));

        setSchedule(act, schedule2);
        save(act);

        long hash3 = checkEvents(schedule1, date, 0);
        assertNotEquals(hash2, hash3); // Hash should have changed
        assertEquals(hash3, service.getModHash(schedule1, date));

        long hash4 = checkEvents(schedule2, date, 1);
        assertNotEquals(-1, hash4);
        assertEquals(hash4, service.getModHash(schedule2, date));
    }

    /**
     * Tests changing an event's patient.
     */
    @Test
    public void testChangePatient() {
        Date date = getDate("2013-11-01");

        Entity schedule = createSchedule();
        Party patient1 = TestHelper.createPatient();

        initScheduleService(30);
        long hash1 = checkEvents(schedule, date, 0);
        checkEvents(schedule, date, 0, hash1);  // Hash should be the same

        // create an event with no patient
        Act event = createEvent(schedule, date, null);
        ScheduleEvents events1 = service.getScheduleEvents(schedule, date);
        assertEquals(1, events1.size());
        assertNull(events1.getEvents().get(0).getReference(ScheduleEvent.PATIENT_REFERENCE));
        assertNotEquals(hash1, events1.getModHash());

        // now update it to include the patient
        IMObjectBean bean = getBean(event);
        bean.setTarget("patient", patient1);
        bean.save();

        // verify the patient matches that expected.
        ScheduleEvents events2 = service.getScheduleEvents(schedule, date);
        assertEquals(1, events2.size());
        assertEquals(patient1.getObjectReference(),
                     events2.getEvents().get(0).getReference(ScheduleEvent.PATIENT_REFERENCE));
        assertNotEquals(events1.getModHash(), events2.getModHash()); // Hash should have changed

        // remove the patient
        bean.removeTarget("patient", patient1);
        bean.save();
        ScheduleEvents events3 = service.getScheduleEvents(schedule, date);
        assertEquals(1, events3.size());
        assertNull(events3.getEvents().get(0).getReference(ScheduleEvent.PATIENT_REFERENCE));
        assertNotEquals(events2.getModHash(), events3.getModHash()); // Hash should have changed

        // now change the patient
        Party patient2 = TestHelper.createPatient();
        bean.setTarget("patient", patient2);
        bean.save();

        ScheduleEvents events4 = service.getScheduleEvents(schedule, date);
        assertEquals(1, events4.size());
        assertEquals(patient2.getObjectReference(),
                     events4.getEvents().get(0).getReference(ScheduleEvent.PATIENT_REFERENCE));
        assertNotEquals(events3.getModHash(), events2.getModHash()); // Hash should have changed
    }

    /**
     * Tests changing an event's customer.
     */
    @Test
    public void testChangeCustomer() {
        Date date = getDate("2013-11-01");

        Entity schedule = createSchedule();
        Party patient = TestHelper.createPatient();

        initScheduleService(30);

        long hash1 = checkEvents(schedule, date, 0);
        checkEvents(schedule, date, 0, hash1);

        Act task = createEvent(schedule, date, patient);
        IMObjectBean bean = getBean(task);
        Reference customer1 = bean.getTargetRef("customer");
        assertNotNull(customer1);

        // verify the customer matches that expected.
        ScheduleEvents events1 = service.getScheduleEvents(schedule, date);
        assertEquals(1, events1.size());
        assertEquals(customer1, events1.getEvents().get(0).getReference(ScheduleEvent.CUSTOMER_REFERENCE));
        assertNotEquals(hash1, events1.getModHash());

        Party customer2 = TestHelper.createCustomer();
        bean.setTarget("customer", customer2);
        bean.save();

        ScheduleEvents events2 = service.getScheduleEvents(schedule, date);
        assertEquals(1, events2.size());
        assertEquals(customer2.getObjectReference(),
                     events2.getEvents().get(0).getReference(ScheduleEvent.CUSTOMER_REFERENCE));
        assertNotEquals(events1.getModHash(), events2.getModHash());
    }

    /**
     * Tests changing an event's clinician.
     */
    @Test
    public void testChangeClinician() {
        Date date = getDate("2013-11-01");

        Entity schedule = createSchedule();
        Party patient = TestHelper.createPatient();

        initScheduleService(30);
        long hash1 = checkEvents(schedule, date, 0);
        checkEvents(schedule, date, 0, hash1);

        Act task = createEvent(schedule, date, patient);
        IMObjectBean bean = getBean(task);
        Reference clinician = bean.getTargetRef("clinician");
        assertNotNull(clinician);

        // verify the clinician matches that expected.
        ScheduleEvents events1 = service.getScheduleEvents(schedule, date);
        assertEquals(1, events1.size());
        assertEquals(clinician, events1.getEvents().get(0).getReference(ScheduleEvent.CLINICIAN_REFERENCE));

        User clinician2 = TestHelper.createClinician();
        bean.setTarget("clinician", clinician2);
        bean.save();

        ScheduleEvents events2 = service.getScheduleEvents(schedule, date);
        assertEquals(1, events2.size());
        assertEquals(clinician2.getObjectReference(),
                     events2.getEvents().get(0).getReference(ScheduleEvent.CLINICIAN_REFERENCE));
        assertNotEquals(events1.getModHash(), events2.getModHash());
    }

    /**
     * Reads a schedule for two separate dates in two threads, whilst changing an event's schedule in a third.
     * <p>
     * Verifies that the schedules contains the expected result.
     */
    @Test
    public void testConcurrentChangeSchedule() throws Exception {
        for (int i = 0; i < 100; i++) {
            System.out.println("Concurrent read/write run: " + i + " ");
            ScheduleService service = createScheduleService(30);
            try {
                checkConcurrentChangeSchedule(service);
                System.out.println("OK");
            } finally {
                destroyService(service);
            }
        }
    }

    /**
     * Reads a schedule for two separate dates in two threads, whilst changing an event's schedule in a third.
     * <p>
     * Verifies that the schedules contains the expected result.
     */
    @Test
    public void testConcurrentChangePatient() throws Exception {
        for (int i = 0; i < 100; i++) {
            System.out.println("Concurrent read/write run: " + i + " ");
            ScheduleService service = createScheduleService(30);
            try {
                checkConcurrentChangePatient(service);
                System.out.println("OK");
            } finally {
                destroyService(service);
            }
        }
    }

    /**
     * Initialises the {@link ScheduleService}.
     * <p>
     * This will be destroyed by {@link #tearDown()}.
     *
     * @param scheduleCacheSize the maximum number of schedule days to cache
     * @return the new service
     */
    protected ScheduleService initScheduleService(int scheduleCacheSize) {
        service = createScheduleService(scheduleCacheSize);
        return service;
    }

    /**
     * Returns the service.
     *
     * @return the service, or {@code null} if {@link #initScheduleService(int)} has not been invoked
     */
    protected ScheduleService getScheduleService() {
        return service;
    }

    /**
     * Creates a new {@link ScheduleService}.
     *
     * @param scheduleCacheSize the maximum number of schedule days to cache
     * @return the new service
     */
    protected abstract ScheduleService createScheduleService(int scheduleCacheSize);

    /**
     * Creates a new schedule.
     *
     * @return the new schedule
     */
    protected abstract Entity createSchedule();

    /**
     * Creates a new event for the specified schedule and date.
     *
     * @param schedule the schedule
     * @param date     the date
     * @param patient  the patient. May be {@code null}
     * @return the new event act
     */
    protected abstract Act createEvent(Entity schedule, Date date, Party patient);

    /**
     * Sets an event's schedule.
     *
     * @param act      the event act
     * @param schedule the schedule
     */
    protected void setSchedule(Act act, Entity schedule) {
        IMObjectBean bean = getBean(act);
        if (bean.isA(ScheduleArchetypes.APPOINTMENT)) {
            bean.setTarget("schedule", schedule);
        } else {
            bean.setTarget("worklist", schedule);
        }
    }

    /**
     * Helper to return the event ids for a schedule and date.
     *
     * @param date     the date
     * @param schedule the schedule
     * @param service  the schedule service
     * @return the event ids
     */
    protected Set<Long> getIds(Date date, Entity schedule, ScheduleService service) {
        Set<Long> result = new HashSet<>();
        List<PropertySet> events = service.getEvents(schedule, date);
        for (PropertySet event : events) {
            result.add(event.getReference(ScheduleEvent.ACT_REFERENCE).getId());
        }
        return result;
    }

    /**
     * Helper to runs tasks concurrently.
     *
     * @param tasks the tasks to run
     * @throws Exception for any error
     */
    @SafeVarargs
    protected final void runConcurrent(Callable<PropertySet>... tasks) throws Exception {
        List<Callable<PropertySet>> list = Arrays.asList(tasks);
        ExecutorService executorService = Executors.newFixedThreadPool(list.size());
        List<Future<PropertySet>> futures = executorService.invokeAll(list);

        assertEquals(tasks.length, futures.size());
        for (Future future : futures) {
            future.get();
        }
    }

    /**
     * Destroys an {@link ScheduleService}.
     *
     * @param service the service. May be {@code null}
     * @throws Exception for any error
     */
    protected void destroyService(ScheduleService service) throws Exception {
        if (service != null && service instanceof DisposableBean) {
            ((DisposableBean) service).destroy();
        }
    }

    /**
     * Verifies events match those expected for a schedule and date.
     *
     * @param schedule the schedule
     * @param date     the date
     * @param size     the expected no. of events
     * @param modHash  the expected modification hash
     */
    protected void checkEvents(Entity schedule, Date date, int size, long modHash) {
        long actualETag = checkEvents(schedule, date, size);
        assertEquals(modHash, actualETag);
    }

    /**
     * Verifies events match those expected for a schedule and date.
     *
     * @param schedule the schedule
     * @param date     the date
     * @param size     the expected no. of events
     * @return the modification hash
     */
    protected long checkEvents(Entity schedule, Date date, int size) {
        ScheduleEvents events = service.getScheduleEvents(schedule, date);
        assertEquals(size, events.size());
        return events.getModHash();
    }

    /**
     * Reads two schedules in two separate threads, while moving an event from one schedule to another in a third
     * thread.
     *
     * @param service the schedule service
     * @throws Exception for any error
     */
    private void checkConcurrentChangeSchedule(final ScheduleService service) throws Exception {
        final Entity schedule1 = createSchedule();
        final Entity schedule2 = createSchedule();
        Party patient = TestHelper.createPatient();
        final Date date = getDate("2007-01-01");

        final Act event = createEvent(schedule1, date, patient);

        Callable<PropertySet> readSchedule1 = () -> {
            System.err.println("Read date1 thread=" + Thread.currentThread().getName());
            List<PropertySet> events = service.getEvents(schedule1, date);
            assertFalse(events.size() > 1);
            return events.isEmpty() ? null : events.get(0);
        };
        Callable<PropertySet> readSchedule2 = () -> {
            System.err.println("Read date2 thread=" + Thread.currentThread().getName());
            List<PropertySet> events = service.getEvents(schedule2, date);
            assertFalse(events.size() > 1);
            return events.isEmpty() ? null : events.get(0);
        };
        Callable<PropertySet> write = () -> {
            System.err.println("Writer thread=" + Thread.currentThread().getName());
            setSchedule(event, schedule2);
            save(event);
            return null;
        };

        runConcurrent(readSchedule1, readSchedule2, write);

        List<PropertySet> events = service.getEvents(schedule1, date);
        assertEquals(0, events.size());
        events = service.getEvents(schedule2, date);
        assertEquals(1, events.size());
    }

    /**
     * Reads a schedules in two separate threads, while changing an event's patient in a third thread.
     *
     * @param service the schedule service
     * @throws Exception for any error
     */
    private void checkConcurrentChangePatient(final ScheduleService service) throws Exception {
        final Entity schedule = createSchedule();
        final Date date = getDate("2007-01-01");
        Party patient = TestHelper.createPatient();

        final Act event = createEvent(schedule, date, patient);
        final Party patient2 = TestHelper.createPatient();

        Callable<PropertySet> read1 = () -> {
            System.err.println("Read 1 thread=" + Thread.currentThread().getName());
            List<PropertySet> events = service.getEvents(schedule, date);
            assertFalse(events.size() > 1);
            return events.isEmpty() ? null : events.get(0);
        };
        Callable<PropertySet> read2 = () -> {
            System.err.println("Read 2 thread=" + Thread.currentThread().getName());
            List<PropertySet> events = service.getEvents(schedule, date);
            assertFalse(events.size() > 1);
            return events.isEmpty() ? null : events.get(0);
        };
        Callable<PropertySet> write = () -> {
            System.err.println("Writer thread=" + Thread.currentThread().getName());
            IMObjectBean bean = getBean(event);
            bean.setTarget("patient", patient2);
            save(event);
            return null;
        };

        List<PropertySet> events = service.getEvents(schedule, date);
        assertEquals(1, events.size());
        assertNotEquals(patient2.getObjectReference(), events.get(0).getReference(ScheduleEvent.PATIENT_REFERENCE));

        runConcurrent(read1, read2, write);

        events = service.getEvents(schedule, date);
        assertEquals(1, events.size());
        assertEquals(patient2.getObjectReference(), events.get(0).getReference(ScheduleEvent.PATIENT_REFERENCE));
    }


}
