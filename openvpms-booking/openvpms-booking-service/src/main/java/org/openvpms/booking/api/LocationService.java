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

import org.openvpms.booking.domain.Location;
import org.openvpms.booking.domain.Schedule;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.util.List;

/**
 * Practice location query service.
 *
 * @author Tim Anderson
 */
@Path("locations")
public interface LocationService {

    /**
     * Returns the practice locations.
     *
     * @return the practice locations
     */
    @GET
    @Produces({"application/json"})
    List<Location> getLocations();

    /**
     * Returns a practice location given its identifier.
     *
     * @param locationId the location identifier
     * @return the practice location
     */
    @GET
    @Produces({"application/json"})
    @Path("/{id}")
    Location getLocation(@PathParam("id") long locationId);

    /**
     * Returns the schedules associated with a location.
     *
     * @param locationId the location id
     * @return the location
     */
    @GET
    @Produces({"application/json"})
    @Path("/{id}/schedules")
    List<Schedule> getSchedules(@PathParam("id") long locationId);
}
