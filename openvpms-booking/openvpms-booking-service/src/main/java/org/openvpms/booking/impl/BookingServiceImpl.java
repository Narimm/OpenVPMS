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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.booking.impl;

import org.apache.commons.lang.StringUtils;
import org.joda.time.Period;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.customer.CustomerArchetypes;
import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.archetype.rules.party.Contacts;
import org.openvpms.archetype.rules.party.CustomerRules;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.user.UserRules;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.workflow.AppointmentRules;
import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.archetype.rules.workflow.Times;
import org.openvpms.archetype.rules.workflow.WorkflowStatus;
import org.openvpms.booking.api.BookingService;
import org.openvpms.booking.domain.Booking;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.model.party.Contact;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.Date;
import java.util.Iterator;

/**
 * Appointment booking service.
 *
 * @author Tim Anderson
 */
@Component
public class BookingServiceImpl implements BookingService {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Contact rules.
     */
    private final Contacts contacts;

    /**
     * The customer rules.
     */
    private final CustomerRules customerRules;

    /**
     * The appointment rules
     */
    private final AppointmentRules appointmentRules;

    /**
     * The user rules.
     */
    private final UserRules userRules;

    /**
     * The transaction manager.
     */
    private final PlatformTransactionManager transactionManager;

    /**
     * Constructs a {@link BookingServiceImpl}.
     *
     * @param service            the archetype service
     * @param customerRules      the customer rules
     * @param appointmentRules   the appointment rules
     * @param userRules          the user rules
     * @param transactionManager the transaction manager
     */
    public BookingServiceImpl(IArchetypeService service, CustomerRules customerRules, AppointmentRules appointmentRules,
                              UserRules userRules, PlatformTransactionManager transactionManager) {
        this.service = service;
        contacts = new Contacts(service);
        this.customerRules = customerRules;
        this.appointmentRules = appointmentRules;
        this.userRules = userRules;
        this.transactionManager = transactionManager;
    }

    /**
     * Creates a new appointment from a booking request.
     *
     * @param booking the booking
     * @return the appointment reference
     */
    @Override
    public Response create(Booking booking, UriInfo uriInfo) {
        if (booking == null) {
            throw new BadRequestException("Booking is required");
        }
        Date start = getRequired("start", booking.getStart());
        Date end = getRequired("end", booking.getEnd());
        Date now = new Date();
        if (start.compareTo(now) < 0) {
            throw new BadRequestException("Cannot make a booking in the past");
        }
        if (end.compareTo(start) <= 0) {
            throw new BadRequestException("Booking start must be less than end");
        }
        Entity schedule = getSchedule(booking);
        Entity appointmentType = getAppointmentType(booking);

        int slotSize = appointmentRules.getSlotSize(schedule);
        Date slotStart = appointmentRules.getSlotTime(start, slotSize, false);
        if (slotStart.compareTo(start) != 0) {
            throw new BadRequestException("Booking start is not on a slot boundary");
        }
        Date slotEnd = appointmentRules.getSlotTime(end, slotSize, true);
        if (slotEnd.compareTo(end) != 0) {
            throw new BadRequestException("Booking end is not on a slot boundary");
        }

        StringBuilder notes = new StringBuilder();
        Party customer = getCustomer(booking);
        Party patient = null;
        if (customer == null) {
            append(notes, "Title", booking.getTitle());
            append(notes, "First Name", booking.getFirstName());
            append(notes, "Last Name", booking.getLastName());
            if (!StringUtils.isEmpty(booking.getPhone())) {
                append(notes, "Phone", booking.getPhone());
            }
            if (!StringUtils.isEmpty(booking.getMobile())) {
                append(notes, "Mobile", booking.getMobile());
            }
            if (!StringUtils.isEmpty(booking.getEmail())) {
                append(notes, "Email", booking.getEmail());
            }
        } else {
            patient = getPatient(customer, booking);
        }
        if (patient == null && !StringUtils.isEmpty(booking.getPatientName())) {
            append(notes, "Patient", booking.getPatientName());
        }
        if (!StringUtils.isEmpty(booking.getNotes())) {
            append(notes, "Notes", booking.getNotes());
        }

        Act act = (Act) service.create(ScheduleArchetypes.APPOINTMENT);
        ActBean bean = new ActBean(act, service);
        bean.setValue("startTime", start);
        bean.setValue("endTime", end);
        bean.setNodeParticipant("schedule", schedule);
        if (customer != null) {
            bean.setNodeParticipant("customer", customer);
        }
        bean.setNodeParticipant("appointmentType", appointmentType);
        if (patient != null) {
            bean.setNodeParticipant("patient", patient);
        }
        bean.setValue("onlineBooking", true);
        String bookingNotes = StringUtils.abbreviate(notes.toString(), 5000);
        if (!bookingNotes.isEmpty()) {
            bean.setValue("bookingNotes", bookingNotes);
        }
        if (customer != null && appointmentRules.isRemindersEnabled(schedule)
            && appointmentRules.isRemindersEnabled(appointmentType) && contacts.canSMS(customer)) {
            Period noReminderPeriod = appointmentRules.getNoReminderPeriod();
            if (noReminderPeriod != null) {
                Date date = DateRules.plus(new Date(), noReminderPeriod);
                if (start.after(date)) {
                    bean.setValue("sendReminder", true);
                }
            }
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            User author = userRules.getUser(authentication);
            if (author != null) {
                bean.setNodeParticipant("author", author);
            }
        }

        save(act, schedule);
        String reference = act.getId() + ":" + act.getLinkId();
        // require both the id, customer id and linkId, to make it harder to cancel appointments not created through the
        // service
        UriBuilder builder = uriInfo.getAbsolutePathBuilder();
        builder.path(reference);
        return Response.created(builder.build()).type(MediaType.TEXT_PLAIN).entity(reference).build();
    }

    /**
     * Cancels a booking.
     *
     * @param reference the booking reference
     * @return a 204 response on success
     * @throws BadRequestException if the booking doesn't exist, or the associated appointment isn't pending
     */
    @Override
    public Response cancel(String reference) {
        Act act = getAppointment(reference);
        if (!WorkflowStatus.PENDING.equals(act.getStatus())) {
            throw new BadRequestException("The booking must be pending to cancel");
        }
        act.setStatus(ActStatus.CANCELLED);
        service.save(act);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    /**
     * Returns a booking given its reference.
     *
     * @param reference the booking reference
     * @return the booking
     * @throws BadRequestException if the booking reference is invalid
     * @throws NotFoundException   if the booking cannot be found
     */
    @Override
    public Booking getBooking(String reference) {
        if (reference == null) {
            throw new BadRequestException("Invalid booking reference");
        }
        Act act = getAppointment(reference);
        ActBean bean = new ActBean(act, service);
        Entity schedule = bean.getNodeParticipant("schedule");
        Party customer = (Party) bean.getNodeParticipant("customer");
        IMObjectReference appointmentType = bean.getNodeParticipantRef("appointmentType");
        Booking booking = new Booking();
        if (schedule != null) {
            IMObjectBean scheduleBean = new IMObjectBean(schedule, service);
            booking.setSchedule(schedule.getId());
            IMObjectReference location = scheduleBean.getNodeTargetObjectRef("location");
            if (location != null) {
                booking.setLocation(location.getId());
            }
        }
        booking.setStart(act.getActivityStartTime());
        booking.setEnd(act.getActivityEndTime());
        if (customer != null) {
            IMObjectBean customerBean = new IMObjectBean(customer, service);
            booking.setTitle(customerBean.getString("title"));
            booking.setFirstName(customerBean.getString("firstName"));
            booking.setLastName(customerBean.getString("lastName"));

            // todo - the mobile, phone and email could be different to those requested
            booking.setMobile(customerRules.getMobileTelephone(customer));
            booking.setPhone(customerRules.getTelephone(customer));
            booking.setEmail(customerRules.getEmailAddress(customer));
        }
        if (appointmentType != null) {
            booking.setAppointmentType(appointmentType.getId());
        }
        Party patient = (Party) bean.getNodeParticipant("patient");
        if (patient != null) {
            booking.setPatientName(patient.getName());
        }
        return booking;
    }

    /**
     * Returns an appointment given the booking reference.
     *
     * @param reference the booking reference
     * @return the appointment
     * @throws BadRequestException if the appointment cannot be found
     */
    protected Act getAppointment(String reference) {
        String[] parts = reference.split(":");
        if (parts.length != 2) {
            throw new BadRequestException("Invalid booking reference");
        }
        long appointmentId;
        try {
            appointmentId = Long.valueOf(parts[0]);
        } catch (NumberFormatException exception) {
            throw new BadRequestException("Invalid booking reference");
        }
        Act act = (Act) service.get(new IMObjectReference(ScheduleArchetypes.APPOINTMENT, appointmentId));
        if (act == null || !act.getLinkId().equals(parts[1]) || ActStatus.CANCELLED.equals(act.getStatus())) {
            throw new NotFoundException("Booking not found");
        }
        return act;
    }

    /**
     * Returns the schedule.
     *
     * @param booking the booking request
     * @return the schedule
     * @throws BadRequestException if the schedule is invalid
     */
    protected Entity getSchedule(Booking booking) {
        Entity schedule = getRequired("Schedule", booking.getSchedule(), ScheduleArchetypes.ORGANISATION_SCHEDULE);
        IMObjectBean bean = new IMObjectBean(schedule, service);
        IMObjectReference locationRef = bean.getNodeTargetObjectRef("location");
        if (locationRef == null || locationRef.getId() != booking.getLocation()) {
            throw new BadRequestException("Schedule is not available at location " + booking.getLocation());
        }
        return schedule;
    }

    /**
     * Returns the customer.
     *
     * @param booking the booking request
     * @return the customer, or {@code null} if the customer can't be found
     * @throws BadRequestException if the customer cannot be found
     */
    protected Party getCustomer(Booking booking) {
        String firstName = getRequired("firstName", booking.getFirstName());
        String lastName = getRequired("lastName", booking.getLastName());
        String name = lastName + "," + firstName;
        ArchetypeQuery query = new ArchetypeQuery(CustomerArchetypes.PERSON, true);
        query.add(Constraints.eq("name", name + "*"));
        Iterator<IMObject> iterator = new IMObjectQueryIterator<>(service, query);
        Party match = null;
        while (iterator.hasNext()) {
            Party customer = (Party) iterator.next();
            if (name.equalsIgnoreCase(customer.getName())) {
                if (matchesContacts(customer, booking)) {
                    if (match != null) {
                        match = null;
                        break;
                    }
                    match = customer;
                }
            }
        }
        return match;
    }

    /**
     * Returns the patient associated with a customer in a booking request.
     *
     * @param customer the customer
     * @param booking  the booking request
     * @return the patient, or {@code null} if no patient was specified or the patient could not be found
     */
    protected Party getPatient(Party customer, Booking booking) {
        Party match = null;
        String name = booking.getPatientName();
        if (!StringUtils.isBlank(name)) {
            ArchetypeQuery query = new ArchetypeQuery(PatientArchetypes.PATIENT, true);
            query.add(Constraints.eq("name", name + "*"));
            query.add(Constraints.join("customers").add(Constraints.eq("source", customer)));
            Iterator<IMObject> iterator = new IMObjectQueryIterator<>(service, query);
            while (iterator.hasNext()) {
                Party patient = (Party) iterator.next();
                if (name.equalsIgnoreCase(patient.getName())) {
                    match = patient;
                    break;
                }
            }
        }
        return match;
    }

    /**
     * Determines if a booking matches the contacts for a customer.
     *
     * @param customer the customer
     * @param booking  the booking
     * @return {@code true} if at least one of the contacts (phone, mobilePhone, email) match
     */
    private boolean matchesContacts(Party customer, Booking booking) {
        boolean match = false;
        String bookingPhone = Contacts.getPhone(booking.getPhone());
        String bookingMobile = Contacts.getPhone(booking.getMobile());
        for (Contact contact : customer.getContacts()) {
            if (TypeHelper.isA(contact, ContactArchetypes.PHONE)) {
                String phone = contacts.getPhone(contact);
                if (!StringUtils.isBlank(phone) && (phone.equals(bookingPhone) || phone.equals(bookingMobile))) {
                    match = true;
                    break;
                }
            } else if (TypeHelper.isA(contact, ContactArchetypes.EMAIL)) {
                if (!StringUtils.isEmpty(booking.getEmail())) {
                    IMObjectBean bean = new IMObjectBean(contact, service);
                    String email = bean.getString("emailAddress");
                    if (!StringUtils.isBlank(email) && email.equals(booking.getEmail())) {
                        match = true;
                        break;
                    }
                }
            }
        }
        return match;
    }

    /**
     * Returns the appointment type.
     *
     * @param booking the booking request
     * @return the customer
     * @throws BadRequestException if the customer cannot be found
     */
    protected Entity getAppointmentType(Booking booking) {
        return getRequired("Appointment Type", booking.getAppointmentType(), ScheduleArchetypes.APPOINTMENT_TYPE);
    }

    /**
     * Saves the appointment after ensuring there is no double booking.
     *
     * @param act      the appointment
     * @param schedule the schedule
     */
    protected void save(final Act act, final Entity schedule) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                Times existing = appointmentRules.getOverlap(act.getActivityStartTime(), act.getActivityEndTime(),
                                                             schedule);
                if (existing != null) {
                    throw new BadRequestException("An appointment is already scheduled for "
                                                  + existing.getStartTime() + "-" + existing.getEndTime());
                }
                service.save(act);
            }
        });
    }

    /**
     * Helper to append a key and value pair to booking notes, separated by new lines.
     *
     * @param notes the booking notes
     * @param key   the key
     * @param value the value
     */
    private void append(StringBuilder notes, String key, String value) {
        if (notes.length() != 0) {
            notes.append('\n');
        }
        notes.append(key).append(": ").append(value);
    }

    private Entity getRequired(String name, long id, String shortName) {
        Entity result = (Entity) service.get(new IMObjectReference(shortName, id));
        if (result == null) {
            throw new BadRequestException(name + " not found: " + id);
        }
        return result;
    }

    private Date getRequired(String name, Date value) {
        if (value == null) {
            throw new BadRequestException("'" + name + "' is a required field");
        }
        return value;
    }

    private String getRequired(String name, String value) {
        if (StringUtils.isBlank(value)) {
            throw new BadRequestException("'" + name + "' is a required field");
        }
        return value;
    }

}
