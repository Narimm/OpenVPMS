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

import org.openvpms.archetype.rules.practice.PracticeService;
import org.openvpms.archetype.rules.workflow.AppointmentRules;
import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.booking.api.LocationService;
import org.openvpms.booking.domain.Location;
import org.openvpms.booking.domain.Schedule;
import org.openvpms.booking.domain.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.entity.Entity;
import org.openvpms.component.model.object.IMObject;
import org.openvpms.component.model.party.Party;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.springframework.stereotype.Component;

import javax.ws.rs.NotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Practice location query service.
 *
 * @author Tim Anderson
 */
@Component
public class LocationServiceImpl implements LocationService {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The booking locations.
     */
    private final BookingLocations locations;

    /**
     * The booking users.
     */
    private final BookingUsers users;

    /**
     * The appointment rules.
     */
    private final AppointmentRules appointmentRules;

    /**
     * Constructs a {@link PracticeService}.
     *
     * @param service          the archetype service
     * @param locations        the booking locations
     * @param users            the booking users
     * @param appointmentRules the appointment rules
     */
    public LocationServiceImpl(IArchetypeService service, BookingLocations locations,
                               BookingUsers users, AppointmentRules appointmentRules) {
        this.service = service;
        this.locations = locations;
        this.users = users;
        this.appointmentRules = appointmentRules;
    }

    /**
     * Returns the practice locations.
     *
     * @return the practice locations
     */
    @Override
    public List<Location> getLocations() {
        List<Location> result = new ArrayList<>();
        String timeZone = locations.getTimeZone().getID();
        for (Party location : locations.getLocations()) {
            result.add(new Location(location.getId(), location.getName(), timeZone));
        }
        return result;
    }

    /**
     * Returns a practice location given its identifier.
     *
     * @param locationId the location identifier
     * @return the practice location
     */
    @Override
    public Location getLocation(long locationId) {
        Party location = getLocationEntity(locationId);
        String timeZone = locations.getTimeZone().getID();
        return new Location(location.getId(), location.getName(), timeZone);
    }

    /**
     * Returns the schedules associated with a location.
     *
     * @param locationId the location id
     * @return the location
     */
    @Override
    public List<Schedule> getSchedules(long locationId) {
        List<Schedule> result = new ArrayList<>();
        Party location = getLocationEntity(locationId);
        ArchetypeQuery query = new ArchetypeQuery(ScheduleArchetypes.ORGANISATION_SCHEDULE);
        query.add(Constraints.join("location").add(Constraints.eq("target", location)));
        Iterator<Entity> iterator = new IMObjectQueryIterator<>(service, query);
        while (iterator.hasNext()) {
            IMObject schedule = iterator.next();
            IMObjectBean bean = service.getBean(schedule);
            if (bean.getBoolean("onlineBooking")) {
                int slotSize = appointmentRules.getSlotSize((Party) schedule);
                result.add(new Schedule(schedule.getId(), schedule.getName(), slotSize));
            }
        }
        return result;
    }

    /**
     * Returns the users associated with a location.
     *
     * @param locationId the location id
     * @return the users
     */
    @Override
    public List<User> getUsers(long locationId) {
        List<User> result = new ArrayList<>();
        Party location = getLocationEntity(locationId);
        for (IMObject object : users.getUsers(location)) {
            result.add(new User(object.getId(), object.getName()));
        }
        return result;
    }

    /**
     * Returns a location given its id.
     *
     * @param locationId the location identifier
     * @return the corresponding location
     * @throws NotFoundException if the location is not found or is not available for online booking
     */
    private Party getLocationEntity(long locationId) {
        Party location = locations.getLocation(locationId);
        if (location == null) {
            throw new NotFoundException("Practice location not found");
        }
        return location;
    }

}
