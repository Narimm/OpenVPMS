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

package org.openvpms.booking.impl.v1;

import org.openvpms.booking.api.BookingService;
import org.openvpms.booking.api.v1.BookingServiceV1;
import org.openvpms.booking.domain.v1.Booking;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 * Implementation of the booking service for V1 of the booking API.
 *
 * @author Tim Anderson
 */
public class BookingServiceImplV1 implements BookingServiceV1 {

    /**
     * The service to delegate to.
     */
    private final BookingService service;

    /**
     * Constructs a {@link BookingServiceImplV1}.
     *
     * @param service the service to delegate to
     */
    public BookingServiceImplV1(BookingService service) {
        this.service = service;
    }

    /**
     * Creates a new appointment from a booking request.
     *
     * @param booking the booking
     * @param uriInfo the URI info
     * @return the appointment reference
     */
    @Override
    public Response create(Booking booking, UriInfo uriInfo) {
        return service.create(mapFromV1(booking), uriInfo);
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
        return mapToV1(service.getBooking(reference));
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
        return service.cancel(reference);
    }

    private org.openvpms.booking.domain.Booking mapFromV1(Booking booking) {
        org.openvpms.booking.domain.Booking result = new org.openvpms.booking.domain.Booking();
        result.setLocation(booking.getLocation());
        result.setSchedule(booking.getSchedule());
        result.setAppointmentType(booking.getAppointmentType());
        result.setStart(booking.getStart());
        result.setEnd(booking.getEnd());
        result.setTitle(booking.getTitle());
        result.setFirstName(booking.getFirstName());
        result.setLastName(booking.getLastName());
        result.setPhone(booking.getPhone());
        result.setMobile(booking.getMobile());
        result.setPatientName(booking.getPatientName());
        result.setNotes(booking.getNotes());
        result.setEmail(booking.getEmail());
        return result;
    }

    private Booking mapToV1(org.openvpms.booking.domain.Booking booking) {
        Booking result = new Booking();
        result.setLocation(booking.getLocation());
        result.setSchedule(booking.getSchedule());
        result.setAppointmentType(booking.getAppointmentType());
        result.setStart(booking.getStart());
        result.setEnd(booking.getEnd());
        result.setTitle(booking.getTitle());
        result.setFirstName(booking.getFirstName());
        result.setLastName(booking.getLastName());
        result.setPhone(booking.getPhone());
        result.setMobile(booking.getMobile());
        result.setPatientName(booking.getPatientName());
        result.setNotes(booking.getNotes());
        result.setEmail(booking.getEmail());
        return result;
    }
}
