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

package org.openvpms.booking.api;

import org.openvpms.booking.domain.Location;
import org.openvpms.booking.domain.ScheduleRange;
import org.openvpms.booking.domain.User;
import org.openvpms.booking.domain.UserFreeBusy;

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Service for querying users that may take bookings.
 *
 * @author Tim Anderson
 */
@Path("users")
public interface UserService {

    /**
     * Returns a user given its identifier.
     *
     * @param userId the user identifier
     * @return the user
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/{id}")
    User getUser(@PathParam("id") long userId);

    /**
     * Returns the locations where are user may work.
     *
     * @param userId the user identifier
     * @return the locations where the user may work
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/{id}/locations")
    List<Location> getLocations(@PathParam("id") long userId);

    /**
     * Returns the location for a user, given the user and location identifiers.
     *
     * @param userId     the user identifier
     * @param locationId the location identifier
     * @return the location
     * @throws NotFoundException if the user or location is not found
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/{id}/locations/{locationId}")
    Location getLocation(@PathParam("id") long userId, @PathParam("locationId") long locationId);

    /**
     * Returns free time ranges for a user between two dates, at the specified location.
     *
     * @param userId     the user identifier
     * @param locationId the location identifier
     * @param from       the start of the date range, in ISO date/time format
     * @param to         the end of the date range, in ISO date/time format
     * @return the free time ranges
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/{id}/locations/{locationId}/free")
    List<ScheduleRange> getFree(@PathParam("id") long userId, @PathParam("locationId") long locationId,
                                @QueryParam("from") String from, @QueryParam("to") String to);

    /**
     * Returns busy time ranges for a user between two dates, at the specified location.
     *
     * @param userId     the user identifier
     * @param locationId the location identifier
     * @param from       the start of the date range, in ISO date/time format
     * @param to         the end of the date range, in ISO date/time format
     * @return the busy time ranges
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/{id}/locations/{locationId}/busy")
    List<ScheduleRange> getBusy(@PathParam("id") long userId, @PathParam("locationId") long locationId,
                                @QueryParam("from") String from, @QueryParam("to") String to);

    /**
     * Returns free and busy time ranges for a user between two dates, at the specified location.
     *
     * @param userId     the user identifier
     * @param locationId the location identifier
     * @param from       the start of the date range, in ISO date/time format
     * @param to         the end of the date range, in ISO date/time format
     * @return the free and busy time ranges
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/{id}/locations/{locationId}/freebusy")
    UserFreeBusy getFreeBusy(@PathParam("id") long userId, @PathParam("locationId") long locationId,
                             @QueryParam("from") String from, @QueryParam("to") String to);
}
