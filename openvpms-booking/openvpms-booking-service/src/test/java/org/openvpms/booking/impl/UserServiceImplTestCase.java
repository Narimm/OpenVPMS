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

package org.openvpms.booking.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openvpms.archetype.rules.practice.PracticeService;
import org.openvpms.archetype.rules.workflow.AppointmentService;
import org.openvpms.archetype.rules.workflow.ScheduleTestHelper;
import org.openvpms.archetype.rules.workflow.roster.RosterService;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.booking.api.UserService;
import org.openvpms.booking.domain.Location;
import org.openvpms.booking.domain.Range;
import org.openvpms.booking.domain.ScheduleRange;
import org.openvpms.booking.domain.UserFreeBusy;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.cache.BasicEhcacheManager;
import org.openvpms.component.model.bean.IMObjectBean;

import javax.ws.rs.NotFoundException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests the {@link UserServiceImpl}.
 *
 * @author Tim Anderson
 */
public class UserServiceImplTestCase extends AbstractBookingServiceTest {

    /**
     * Test location 1.
     */
    private Party location1;

    /**
     * Test location 2.
     */
    private Party location2;

    /**
     * Test location3, with {@code onlineBooking=false}.
     */
    private Party location3;

    /**
     * The roster service.
     */
    private RosterService rosterService;

    /**
     * The appointment service.
     */
    private AppointmentService appointmentService;

    /**
     * The test user.
     */
    private User clinician;

    /**
     * The user service.
     */
    private UserService service;


    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        IArchetypeService service = getArchetypeService();
        rosterService = new RosterService(service, new BasicEhcacheManager(30));
        appointmentService = new AppointmentService(service, getLookupService(),
                                                    new BasicEhcacheManager(30));

        location1 = createLocation(true);
        location2 = createLocation(true);
        location3 = createLocation(false);

        PracticeService practiceService = Mockito.mock(PracticeService.class);
        Mockito.when(practiceService.getLocations()).thenReturn(Arrays.asList(location1, location2, location3));
        clinician = (User) createClinician(true);
        BookingLocations locations = new BookingLocations(service, practiceService);
        BookingCalendar calendar = new BookingCalendar(rosterService, appointmentService);
        BookingUsers users = new BookingUsers(service, locations);
        this.service = new UserServiceImpl(calendar, locations, users);
    }

    /**
     * Cleans up after the test.
     *
     * @throws Exception for any error
     */
    @After
    public void tearDown() throws Exception {
        rosterService.destroy();
        appointmentService.destroy();
    }

    /**
     * Verifies that if a user has no roster, no time ranges are returned.
     */
    @Test
    public void testNoRoster() {
        User user = (User) createClinician(true);
        List<ScheduleRange> free = service.getFree(user.getId(), location1.getId(), getISODate("2018-11-25"),
                                                   getISODate("2018-11-26"));
        List<ScheduleRange> busy = service.getBusy(user.getId(), location1.getId(), getISODate("2018-11-25"),
                                                   getISODate("2018-11-26"));
        UserFreeBusy freeBusy = service.getFreeBusy(user.getId(), location1.getId(), getISODate("2018-11-25"),
                                                    getISODate("2018-11-26"));
        checkRanges(free);
        checkRanges(busy);
        checkRanges(freeBusy.getFree());
        checkRanges(freeBusy.getBusy());
    }

    /**
     * Test behaviour if a roster has a 24 hour event.
     */
    @Test
    public void testFullDayRosterEvent() {
        Party schedule = ScheduleTestHelper.createSchedule(location1);
        Entity area = ScheduleTestHelper.createRosterArea(location1, schedule);
        createEvent("2018-11-28 00:00", "2018-11-29 00:00", clinician, area, location1);
        List<ScheduleRange> free = service.getFree(clinician.getId(), location1.getId(), getISODate("2018-11-27"),
                                                   getISODate("2018-11-30"));
        List<ScheduleRange> busy = service.getBusy(clinician.getId(), location1.getId(), getISODate("2018-11-27"),
                                                   getISODate("2018-11-30"));
        UserFreeBusy freeBusy = service.getFreeBusy(clinician.getId(), location1.getId(), getISODate("2018-11-27"),
                                                    getISODate("2018-11-30"));
        Range free1 = createRange("2018-11-28 00:00", "2018-11-29 00:00");
        Range busy1 = createRange("2018-11-27 00:00", "2018-11-28 00:00");
        Range busy2 = createRange("2018-11-29 00:00", "2018-11-30 00:00");

        checkRanges(free, free1);
        checkRanges(busy, busy1, busy2);
        checkRanges(freeBusy.getFree(), free1);
        checkRanges(freeBusy.getBusy(), busy1, busy2);
    }

    /**
     * Test behaviour if a roster has a 2 events in a day.
     */
    @Test
    public void test2RosterEvents() {
        Party schedule = ScheduleTestHelper.createSchedule(location1);
        Entity area = ScheduleTestHelper.createRosterArea(location1, schedule);
        createEvent("2018-11-28 09:00", "2018-11-28 12:00", clinician, area, location1);
        createEvent("2018-11-28 13:00", "2018-11-28 15:00", clinician, area, location1);
        List<ScheduleRange> free = service.getFree(clinician.getId(), location1.getId(), getISODate("2018-11-27"),
                                                   getISODate("2018-11-29"));
        List<ScheduleRange> busy = service.getBusy(clinician.getId(), location1.getId(), getISODate("2018-11-27"),
                                                   getISODate("2018-11-29"));
        UserFreeBusy freeBusy = service.getFreeBusy(clinician.getId(), location1.getId(), getISODate("2018-11-27"),
                                                    getISODate("2018-11-29"));
        Range free1 = createRange("2018-11-28 09:00", "2018-11-28 12:00");
        Range free2 = createRange("2018-11-28 13:00", "2018-11-28 15:00");
        Range busy1 = createRange("2018-11-27 00:00", "2018-11-28 00:00");
        Range busy2 = createRange("2018-11-28 00:00", "2018-11-28 09:00");
        Range busy3 = createRange("2018-11-28 12:00", "2018-11-28 13:00");
        Range busy4 = createRange("2018-11-28 15:00", "2018-11-29 00:00");
        checkRanges(free, free1, free2);
        checkRanges(busy, busy1, busy2, busy3, busy4);
        checkRanges(freeBusy.getFree(), free1, free2);
        checkRanges(freeBusy.getBusy(), busy1, busy2, busy3, busy4);
    }

    /**
     * Verifies that appointments are reflected in the free and busy times.
     */
    @Test
    public void testAppointments() {
        Entity schedule = createSchedule(null, null);
        Entity area = ScheduleTestHelper.createRosterArea(location1, schedule);
        createEvent("2018-11-28 09:00", "2018-11-28 12:00", clinician, area, location1);
        createEvent("2018-11-28 13:00", "2018-11-28 15:00", clinician, area, location1);
        createAppointment("2018-11-28 09:30", "2018-11-28 10:00", schedule, clinician); // within 1st range
        createAppointment("2018-11-28 10:00", "2018-11-28 10:30", schedule, clinician);
        createAppointment("2018-11-28 12:45", "2018-11-28 13:15", schedule, clinician); // overlaps start of 2nd range
        createAppointment("2018-11-28 15:30", "2018-11-28 15:45", schedule, clinician); // outside range

        List<ScheduleRange> free = service.getFree(clinician.getId(), location1.getId(), getISODate("2018-11-28"),
                                                   getISODate("2018-11-29"));
        List<ScheduleRange> busy = service.getBusy(clinician.getId(), location1.getId(), getISODate("2018-11-28"),
                                                   getISODate("2018-11-29"));
        UserFreeBusy freeBusy = service.getFreeBusy(clinician.getId(), location1.getId(), getISODate("2018-11-28"),
                                                    getISODate("2018-11-29"));
        Range free1 = createRange("2018-11-28 09:00", "2018-11-28 09:30");
        Range free2 = createRange("2018-11-28 10:30", "2018-11-28 12:00");
        Range free3 = createRange("2018-11-28 13:15", "2018-11-28 15:00");
        Range busy1 = createRange("2018-11-28 00:00", "2018-11-28 09:00");
        Range busy2 = createRange("2018-11-28 09:30", "2018-11-28 10:30");
        Range busy3 = createRange("2018-11-28 12:00", "2018-11-28 13:15");
        Range busy4 = createRange("2018-11-28 15:00", "2018-11-29 00:00");
        checkRanges(free, free1, free2, free3);
        checkRanges(busy, busy1, busy2, busy3, busy4);
        checkRanges(freeBusy.getFree(), free1, free2, free3);
        checkRanges(freeBusy.getBusy(), busy1, busy2, busy3, busy4);
    }

    /**
     * Tests the {@link UserServiceImpl#getUser(long)} method.
     */
    @Test
    public void testGetUser() {
        org.openvpms.booking.domain.User user = service.getUser(clinician.getId());
        assertNotNull(user);
        assertEquals(clinician.getName(), user.getName());
        assertEquals(clinician.getId(), user.getId());

        // now for a non-existent user
        try {
            service.getUser(0);
            fail("Expected getUser() to fail");
        } catch (NotFoundException expected) {
            // do nothing
        }
    }

    /**
     * Tests the {@link UserServiceImpl#getLocations(long)} method.
     */
    @Test
    public void testGetLocations() {
        // user not associated with an locations, so will return all online booking locations.
        List<Location> locations1 = service.getLocations(clinician.getId());
        assertEquals(2, locations1.size());

        locations1.sort(Comparator.comparingLong(o -> o.getId()));
        assertEquals(location1.getId(), locations1.get(0).getId());
        assertEquals(location2.getId(), locations1.get(1).getId());

        // associate user with location 1. Only it should now be returned
        IMObjectBean bean = getBean(clinician);
        bean.addTarget("locations", location1);
        bean.save();

        List<Location> locations2 = service.getLocations(clinician.getId());
        assertEquals(1, locations2.size());
        assertEquals(location1.getId(), locations2.get(0).getId());

        // create a user associated with a non-booking location.
        User clinician2 = (User) createClinician(true);
        bean = getBean(clinician2);
        bean.addTarget("locations", location3);
        bean.save();

        List<Location> locations3 = service.getLocations(clinician2.getId());
        assertTrue(locations3.isEmpty());
    }

    /**
     * Tests the {@link UserServiceImpl#getLocation(long, long)} method for a user with no location relationships.
     * All online booking locations should be returned.
     */
    @Test
    public void testGetLocationForUserWithNoLocationRelationships() {
        Location l1 = service.getLocation(clinician.getId(), location1.getId());
        assertEquals(location1.getId(), l1.getId());

        Location l2 = service.getLocation(clinician.getId(), location2.getId());
        assertEquals(location2.getId(), l2.getId());

        try {
            service.getLocation(clinician.getId(), location3.getId());
            fail("Expected getLocation() to fail for onlineBooking=false location");
        } catch (NotFoundException expected) {
            assertEquals("Location not found", expected.getMessage());
        }
    }

    /**
     * Tests the {@link UserServiceImpl#getLocation(long, long)} method.
     */
    @Test
    public void testGetLocationForUserWithLocationRelationship() {
        // associate user with location 1. Only it should be returned
        IMObjectBean bean = getBean(clinician);
        bean.addTarget("locations", location1);
        bean.save();

        Location l1 = service.getLocation(clinician.getId(), location1.getId());
        assertEquals(location1.getId(), l1.getId());

        try {
            service.getLocation(clinician.getId(), location2.getId());
            fail("Expected getLocation() to fail when location not associated with user");
        } catch (NotFoundException expected) {
            assertEquals("Location not found", expected.getMessage());
        }

        try {
            service.getLocation(clinician.getId(), location3.getId());
            fail("Expected getLocation() to fail for onlineBooking=false location");
        } catch (NotFoundException expected) {
            assertEquals("Location not found", expected.getMessage());
        }
    }

    /**
     * Verifies that the {@link UserServiceImpl#getLocation(long, long)} method throws {@code NotFoundException}
     * for invalid inputs.
     */
    @Test
    public void testGetLocationForInvalidInputs() {
        try {
            service.getLocation(0, location1.getId());
            fail("Expected getLocation() to fail for non-existent user");
        } catch (NotFoundException expected) {
            assertEquals("User not found", expected.getMessage());
        }

        try {
            service.getLocation(clinician.getId(), 0);
            fail("Expected getLocation() to fail for non-existent location");
        } catch (NotFoundException expected) {
            assertEquals("Location not found", expected.getMessage());
        }
    }

    /**
     * Creates a roster event.
     *
     * @param startTime the appointment start time
     * @param endTime   the appointment end time
     * @param user      the rostered user. May be {@code null}
     * @param area      the area
     * @param location  the location
     * @return a new event
     */
    private Act createEvent(String startTime, String endTime, User user, Entity area, Party location) {
        Act event = ScheduleTestHelper.createRosterEvent(TestHelper.getDatetime(startTime),
                                                         TestHelper.getDatetime(endTime), user, area, location);
        save(event);
        return event;
    }

}
