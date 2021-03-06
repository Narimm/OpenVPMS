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

import org.openvpms.booking.api.v1.LocationServiceV1;
import org.openvpms.booking.domain.User;

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
public interface LocationService extends LocationServiceV1 {

    /**
     * Returns the users associated with a location.
     *
     * @param locationId the location id
     * @return the users
     */
    @GET
    @Produces({"application/json"})
    @Path("/{id}/users")
    List<User> getUsers(@PathParam("id") long locationId);

}