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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.booking.impl;

import org.glassfish.jersey.uri.internal.JerseyUriBuilder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.party.CustomerRules;
import org.openvpms.archetype.rules.user.UserRules;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.rules.workflow.AppointmentRules;
import org.openvpms.archetype.rules.workflow.AppointmentStatus;
import org.openvpms.archetype.rules.workflow.ScheduleTestHelper;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.booking.api.BookingService;
import org.openvpms.booking.domain.Booking;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests the {@link BookingServiceImpl}.
 *
 * @author Tim Anderson
 */
public class BookingServiceImplTestCase extends ArchetypeServiceTest {

    /**
     * The customer rules.
     */
    @Autowired
    private CustomerRules customerRules;

    /**
     * The transaction manager.
     */
    @Autowired
    private PlatformTransactionManager transactionManager;

    /**
     * The user rules.
     */
    private UserRules userRules;

    /**
     * The test location.
     */
    private Party location;

    /**
     * The appointment type.
     */
    private Entity appointmentType;

    /**
     * The schedule.
     */
    private Entity schedule;

    /**
     * The appointment rules.
     */
    private AppointmentRules appointmentRules;

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        appointmentRules = new AppointmentRules(getArchetypeService());
        location = TestHelper.createLocation();
        appointmentType = ScheduleTestHelper.createAppointmentType();
        userRules = new UserRules(getArchetypeService());
        IMObjectBean appointmentTypeBean = new IMObjectBean(appointmentType);
        appointmentTypeBean.setValue("sendReminders", true);
        appointmentTypeBean.save();
        schedule = ScheduleTestHelper.createSchedule(15, DateUnits.MINUTES.toString(), 1, appointmentType, location);
        IMObjectBean scheduleBean = new IMObjectBean(schedule);
        scheduleBean.setValue("onlineBooking", true);
        scheduleBean.setValue("sendReminders", true);
        scheduleBean.save();
        configureSMSJob();
    }

    /**
     * Tests making a booking.
     */
    @Test
    public void testBooking() {
        Party customer = TestHelper.createCustomer();
        customer.addContact(TestHelper.createEmailContact("foo@bar.com"));
        Party patient = TestHelper.createPatient(customer, false);
        patient.setName("Fido");
        save(customer, patient);
        IMObjectBean customerBean = new IMObjectBean(customer);
        Booking booking = new Booking();
        booking.setLocation(location.getId());
        booking.setSchedule(schedule.getId());
        booking.setAppointmentType(appointmentType.getId());
        Date startTime = DateRules.getTomorrow();
        Date endTime = DateRules.getDate(startTime, 15, DateUnits.MINUTES);
        booking.setStart(startTime);
        booking.setEnd(endTime);
        booking.setTitle("Mr");
        booking.setFirstName(customerBean.getString("firstName"));
        booking.setLastName(customerBean.getString("lastName"));
        booking.setEmail("foo@bar.com");
        booking.setPatientName(patient.getName());
        booking.setNotes("Some notes");

        Act appointment = createBooking(booking);
        checkAppointment(appointment, startTime, endTime, customer, patient, false, "Notes: " + booking.getNotes());
    }

    /**
     * Test booking for a new customer. The appointment will be created without a customer participation.
     */
    @Test
    public void testBookingForNewCustomer() {
        Booking booking = new Booking();
        booking.setLocation(location.getId());
        booking.setSchedule(schedule.getId());
        booking.setAppointmentType(appointmentType.getId());
        Date startTime = DateRules.getTomorrow();
        Date endTime = DateRules.getDate(startTime, 15, DateUnits.MINUTES);
        booking.setStart(startTime);
        booking.setEnd(endTime);
        booking.setTitle("Mr");
        booking.setFirstName("Foo");
        booking.setLastName("Bar" + System.currentTimeMillis());
        booking.setEmail("foo@bar.com");
        booking.setPatientName("Fido");

        Act appointment = createBooking(booking);
        String bookingNotes = "Title: Mr\n" +
                              "First Name: Foo\n" +
                              "Last Name: " + booking.getLastName() + "\n" +
                              "Email: foo@bar.com\n" +
                              "Patient: Fido";
        checkAppointment(appointment, startTime, endTime, null, null, false, bookingNotes);
    }

    /**
     * Verifies that the send reminder flag is set if the appointment if the customer can receive SMS.
     */
    @Test
    public void testSendReminder() {
        Party customer = TestHelper.createCustomer();
        String phoneNumber = "04123456789";
        customer.addContact(createSMSContact(phoneNumber));
        Party patient = TestHelper.createPatient(customer, false);
        patient.setName("Fido");
        save(customer, patient);
        IMObjectBean customerBean = new IMObjectBean(customer);
        Booking booking = new Booking();
        booking.setLocation(location.getId());
        booking.setSchedule(schedule.getId());
        booking.setAppointmentType(appointmentType.getId());
        Date startTime = DateRules.getNextDate(DateRules.getTomorrow());
        Date endTime = DateRules.getDate(startTime, 30, DateUnits.MINUTES);
        booking.setStart(startTime);
        booking.setEnd(endTime);
        booking.setTitle("Mr");
        booking.setFirstName(customerBean.getString("firstName"));
        booking.setLastName(customerBean.getString("lastName"));
        booking.setMobile(phoneNumber);
        booking.setPatientName(patient.getName());

        Act appointment = createBooking(booking);
        checkAppointment(appointment, startTime, endTime, customer, patient, true, null);
    }

    /**
     * Tests booking cancellation.
     */
    @Test
    public void testCancel() {
        Date startTime = DateRules.getTomorrow();
        Date endTime = DateRules.getDate(startTime, 45, DateUnits.MINUTES);
        Booking booking = createBooking(startTime, endTime);

        ArrayList<Act> acts = new ArrayList<>();
        BookingService service = createBookingService(acts);
        Response response1 = service.create(booking, createUriInfo());
        assertEquals(201, response1.getStatus());
        assertEquals("text/plain", response1.getHeaderString(HttpHeaders.CONTENT_TYPE));
        assertEquals(1, acts.size());
        Act appointment = acts.get(0);
        assertEquals(AppointmentStatus.PENDING, appointment.getStatus());

        String reference = (String) response1.getEntity();
        assertEquals(appointment.getId() + ":" + appointment.getLinkId(), reference);
        Response response2 = service.cancel(reference);
        assertEquals(204, response2.getStatus());

        appointment = get(appointment);
        assertEquals(ActStatus.CANCELLED, appointment.getStatus());

        try {
            service.cancel(reference);
            fail("Expected cancellation to fail");
        } catch (NotFoundException exception) {
            assertEquals("Booking not found", exception.getMessage());
        }
    }

    /**
     * Verifies that trying to create an appointment in the past throws {@link BadRequestException}.
     */
    @Test
    public void testBackDatedAppointment() {
        Date startTime = DateRules.getYesterday();
        Date endTime = DateRules.getDate(startTime, 45, DateUnits.MINUTES);
        Booking booking = createBooking(startTime, endTime);
        BookingService service = createBookingService();

        try {
            service.create(booking, createUriInfo());
            fail("Expected back-dated create to fail");
        } catch (BadRequestException exception) {
            assertEquals("Cannot make a booking in the past", exception.getMessage());
        }
    }

    /**
     * Verifies that trying to create an appointment with a start less than end throws {@link BadRequestException}.
     */
    @Test
    public void testStartLessThanEnd() {
        Date startTime = DateRules.getTomorrow();
        Date endTime = DateRules.getDate(startTime, 45, DateUnits.MINUTES);
        Booking booking = createBooking(endTime, startTime);
        BookingService service = createBookingService();

        try {
            service.create(booking, createUriInfo());
            fail("Expected create to fail");
        } catch (BadRequestException exception) {
            assertEquals("Booking start must be less than end", exception.getMessage());
        }
    }

    /**
     * Verifies that trying to create an appointment that doesn't start or end on a slot boundary throws
     * {@link BadRequestException}.
     */
    @Test
    public void testInvalidSlotBoundary() {
        BookingService service = createBookingService();
        Date start1 = DateRules.getDate(DateRules.getTomorrow(), 5, DateUnits.MINUTES);
        Date end1 = DateRules.getDate(start1, 45, DateUnits.MINUTES);
        Booking booking1 = createBooking(start1, end1);

        try {
            service.create(booking1, createUriInfo());
            fail("Expected create to fail");
        } catch (BadRequestException exception) {
            assertEquals("Booking start is not on a slot boundary", exception.getMessage());
        }

        Date start2 = DateRules.getTomorrow();
        Date end2 = DateRules.getDate(start1, 20, DateUnits.MINUTES);
        Booking booking2 = createBooking(start2, end2);

        try {
            service.create(booking2, createUriInfo());
            fail("Expected create to fail");
        } catch (BadRequestException exception) {
            assertEquals("Booking end is not on a slot boundary", exception.getMessage());
        }
    }

    /**
     * Helper to create a booking.
     *
     * @param startTime the booking start time
     * @param endTime   the booking end time
     * @return a new booking
     */
    private Booking createBooking(Date startTime, Date endTime) {
        Booking booking = new Booking();
        booking.setLocation(location.getId());
        booking.setSchedule(schedule.getId());
        booking.setAppointmentType(appointmentType.getId());
        booking.setStart(startTime);
        booking.setEnd(endTime);
        booking.setTitle("Mr");
        booking.setFirstName("Foo");
        booking.setLastName("Bar" + System.currentTimeMillis());
        booking.setEmail("foo@bar.com");
        booking.setPatientName("Fido");
        return booking;
    }

    /**
     * Verifies an appointment matches that expected.
     *
     * @param appointment  the appointment to check
     * @param startTime    the expected start time
     * @param endTime      the expected end time
     * @param customer     the expected customer
     * @param patient      the expected patient
     * @param sendReminder the expected 'send reminder' flag
     * @param bookingNotes the expected booking notes
     */
    private void checkAppointment(Act appointment, Date startTime, Date endTime, Party customer, Party patient,
                                  boolean sendReminder, String bookingNotes) {
        ActBean bean = new ActBean(appointment);
        assertEquals(startTime, bean.getDate("startTime"));
        assertEquals(endTime, bean.getDate("endTime"));
        assertEquals(customer, bean.getNodeParticipant("customer"));
        assertEquals(patient, bean.getNodeParticipant("patient"));
        assertEquals(schedule, bean.getNodeParticipant("schedule"));
        assertEquals(appointmentType, bean.getNodeParticipant("appointmentType"));
        assertEquals(sendReminder, bean.getBoolean("sendReminder"));
        assertEquals(bookingNotes, bean.getString("bookingNotes"));
        assertTrue(bean.getBoolean("onlineBooking"));
    }

    private void configureSMSJob() {
        IMObjectBean bean;
        ArchetypeQuery query = new ArchetypeQuery("entity.jobAppointmentReminder", true);
        IMObjectQueryIterator<Entity> iterator = new IMObjectQueryIterator<>(query);
        if (!iterator.hasNext()) {
            bean = new IMObjectBean(create("entity.jobAppointmentReminder"));
            bean.addNodeTarget("runAs", TestHelper.createUser());
        } else {
            bean = new IMObjectBean(iterator.next());
        }
        bean.setValue("smsFrom", 3);
        bean.setValue("smsFromUnits", DateUnits.DAYS);
        bean.setValue("smsTo", 1);
        bean.setValue("smsToUnits", DateUnits.DAYS);
        bean.setValue("noReminder", 1);
        bean.setValue("noReminderUnits", DateUnits.DAYS);
        bean.save();
    }

    /**
     * Creates an SMS contact.
     *
     * @param phoneNumber the phone number
     * @return a new contact
     */
    private Contact createSMSContact(String phoneNumber) {
        Contact contact = TestHelper.createPhoneContact(null, phoneNumber);
        IMObjectBean bean = new IMObjectBean(contact);
        bean.setValue("sms", true);
        return contact;
    }

    /**
     * Creates a booking.
     *
     * @param booking the booking
     * @return the corresponding appointment
     */
    private Act createBooking(Booking booking) {
        final List<Act> acts = new ArrayList<>();
        BookingService service = createBookingService(acts);
        Response response = service.create(booking, createUriInfo());
        assertEquals("text/plain", response.getHeaderString(HttpHeaders.CONTENT_TYPE));
        assertEquals(201, response.getStatus());
        assertEquals(1, acts.size());
        return acts.get(0);
    }

    /**
     * Creates a booking service.
     *
     * @return the booking service
     */
    private BookingServiceImpl createBookingService() {
        return new BookingServiceImpl(getArchetypeService(), customerRules, appointmentRules,
                                      userRules, transactionManager);
    }

    /**
     * Creates a booking service that collects appointments.
     *
     * @param acts the list to collect appointment acts
     * @return the booking service
     */
    private BookingService createBookingService(final List<Act> acts) {
        return new BookingServiceImpl(getArchetypeService(), customerRules, appointmentRules,
                                      userRules, transactionManager) {
            @Override
            protected void save(Act act, Entity schedule) {
                super.save(act, schedule);
                acts.add(act);
            }
        };
    }

    /**
     * Helper to create a {@code UriInfo}.
     *
     * @return a new {@code UriInfo}
     */
    private UriInfo createUriInfo() {
        UriInfo uriInfo = Mockito.mock(UriInfo.class);
        try {
            URI uri = new URI("http://localhost:8080/openvpms/ws/booking/v1/bookings");
            Mockito.when(uriInfo.getAbsolutePath()).thenReturn(uri);
            JerseyUriBuilder builder = new JerseyUriBuilder();
            builder.uri(uri);
            Mockito.when(uriInfo.getAbsolutePathBuilder()).thenReturn(builder);
        } catch (Exception exception) {
            fail(exception.getMessage());
        }
        return uriInfo;
    }

}
