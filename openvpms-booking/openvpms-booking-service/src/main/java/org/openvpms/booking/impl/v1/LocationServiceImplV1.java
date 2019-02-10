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

import org.openvpms.booking.api.LocationService;
import org.openvpms.booking.api.v1.LocationServiceV1;
import org.openvpms.booking.domain.Location;
import org.openvpms.booking.domain.Schedule;

import java.util.List;

/**
 * Implementation of the location service for V1 of the booking API.
 *
 * @author Tim Anderson
 */
public class LocationServiceImplV1 implements LocationServiceV1 {

    /**
     * The service to delegate to.
     */
    private final LocationService service;

    /**
     * Constructs a {@link LocationServiceImplV1}.
     *
     * @param service the location service to delegate to
     */
    public LocationServiceImplV1(LocationService service) {
        this.service = service;
    }

    /**
     * Returns the practice locations.
     *
     * @return the practice locations
     */
    @Override
    public List<Location> getLocations() {
        return service.getLocations();
    }

    /**
     * Returns a practice location given its identifier.
     *
     * @param locationId the location identifier
     * @return the practice location
     */
    @Override
    public Location getLocation(long locationId) {
        return service.getLocation(locationId);
    }

    /**
     * Returns the schedules associated with a location.
     *
     * @param locationId the location id
     * @return the location
     */
    @Override
    public List<Schedule> getSchedules(long locationId) {
        return service.getSchedules(locationId);
    }
}
