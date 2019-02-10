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

import org.openvpms.booking.api.UserService;
import org.openvpms.booking.domain.Location;
import org.openvpms.booking.domain.ScheduleRange;
import org.openvpms.booking.domain.User;
import org.openvpms.booking.domain.UserFreeBusy;
import org.openvpms.component.model.party.Party;
import org.springframework.stereotype.Component;

import javax.ws.rs.NotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * Appointment user service.
 *
 * @author Tim Anderson
 */
@Component
public class UserServiceImpl implements UserService {

    /**
     * The booking calendar service.
     */
    private final BookingCalendar calendar;

    /**
     * The booking locations.
     */
    private final BookingLocations locations;

    /**
     * The booking users.
     */
    private final BookingUsers users;

    /**
     * Constructs a {@link UserServiceImpl}.
     *
     * @param calendar  the booking calendar
     * @param locations the booking locations
     * @param users     the booking users
     */
    public UserServiceImpl(BookingCalendar calendar, BookingLocations locations, BookingUsers users) {
        this.calendar = calendar;
        this.locations = locations;
        this.users = users;
    }

    /**
     * Returns a user given its identifier.
     *
     * @param userId the user identifier
     * @return the user
     */
    @Override
    public User getUser(long userId) {
        org.openvpms.component.model.user.User user = getUserEntity(userId);
        return new User(user.getId(), user.getName());
    }

    /**
     * Returns the locations where are user may work.
     *
     * @param userId the user identifier
     * @return the locations where the user may work
     */
    @Override
    public List<Location> getLocations(long userId) {
        List<Location> result = new ArrayList<>();
        org.openvpms.component.model.user.User user = getUserEntity(userId);
        String timeZone = locations.getTimeZone().getID();
        for (Party location : users.getLocations(user)) {
            result.add(new Location(location.getId(), location.getName(), timeZone));
        }
        return result;
    }

    /**
     * Returns the location for a user, given the user and location identifiers.
     *
     * @param userId     the user identifier
     * @param locationId the location identifier
     * @return the location
     * @throws NotFoundException if the user or location is not found
     */
    @Override
    public Location getLocation(long userId, long locationId) {
        org.openvpms.component.model.user.User user = getUserEntity(userId);
        Party location = getLocation(user, locationId);
        String timeZone = locations.getTimeZone().getID();
        return new Location(location.getId(), location.getName(), timeZone);
    }

    /**
     * Returns free time ranges for a user between two dates, at the specified location.
     *
     * @param userId     the user identifier
     * @param locationId the location identifier
     * @param from       the start of the date range, in ISO date/time format
     * @param to         the end of the date range, in ISO date/time format
     * @return the free time ranges
     */
    @Override
    public List<ScheduleRange> getFree(long userId, long locationId, String from, String to) {
        org.openvpms.component.model.user.User user = getUserEntity(userId);
        Party location = getLocation(user, locationId);
        return calendar.getFree(user, location, from, to);
    }

    /**
     * Returns busy time ranges for a user between two dates, at the specified location.
     *
     * @param userId     the user identifier
     * @param locationId the location identifier
     * @param from       the start of the date range, in ISO date/time format
     * @param to         the end of the date range, in ISO date/time format
     * @return the busy time ranges
     */
    @Override
    public List<ScheduleRange> getBusy(long userId, long locationId, String from, String to) {
        org.openvpms.component.model.user.User user = getUserEntity(userId);
        Party location = getLocation(user, locationId);
        return calendar.getBusy(user, location, from, to);
    }

    /**
     * Returns free and busy time ranges for a user between two dates, at the specified location.
     *
     * @param userId     the user identifier
     * @param locationId the location identifier
     * @param from       the start of the date range, in ISO date/time format
     * @param to         the end of the date range, in ISO date/time format
     * @return the free and busy time ranges
     */
    @Override
    public UserFreeBusy getFreeBusy(long userId, long locationId, String from, String to) {
        org.openvpms.component.model.user.User user = getUserEntity(userId);
        Party location = getLocation(user, locationId);
        return calendar.getFreeBusy(user, location, from, to);
    }

    /**
     * Returns a user entity given a user id.
     *
     * @param id the user identifier
     * @return the corresponding entity
     * @throws NotFoundException if the user cannot be found or isn't configured for online booking
     */
    private org.openvpms.component.model.user.User getUserEntity(long id) {
        org.openvpms.component.model.user.User user = users.getUser(id);
        if (user == null) {
            throw new NotFoundException("User not found");
        }
        return user;
    }

    /**
     * Returns a practice location for a user, given its identifier, iff it supports online booking.
     *
     * @param user       the user
     * @param locationId the location identifier
     * @return the practice location
     * @throws NotFoundException if the location is not found
     */
    private Party getLocation(org.openvpms.component.model.user.User user, long locationId) {
        Party location = users.getLocation(user, locationId);
        if (location == null) {
            throw new NotFoundException("Location not found");
        }
        return location;
    }

}
