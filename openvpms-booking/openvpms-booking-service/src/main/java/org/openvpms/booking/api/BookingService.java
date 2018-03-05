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

package org.openvpms.booking.api;

import org.openvpms.booking.domain.Booking;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 * Appointment booking service.
 *
 * @author Tim Anderson
 */
@Path("bookings")
public interface BookingService {

    /**
     * Creates a new appointment from a booking request.
     *
     * @param booking the booking
     * @return the appointment reference
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    Response create(Booking booking, @Context UriInfo uriInfo);

    /**
     * Returns a booking given its reference.
     *
     * @param reference the booking reference
     * @return the booking
     * @throws BadRequestException if the booking reference is invalid
     * @throws NotFoundException   if the booking cannot be found
     */
    @GET
    @Path("/{reference}")
    @Produces(MediaType.APPLICATION_JSON)
    Booking getBooking(@PathParam("reference") String reference);

    /**
     * Cancels a booking.
     *
     * @param reference the booking reference
     * @return a 204 response on success
     * @throws BadRequestException if the booking doesn't exist, or the associated appointment isn't pending
     */
    @DELETE
    @Path("/{reference}")
    Response cancel(@PathParam("reference") String reference);

}
