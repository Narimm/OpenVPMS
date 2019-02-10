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

import org.openvpms.archetype.rules.user.UserArchetypes;
import org.openvpms.archetype.rules.user.UserQueryFactory;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.object.Reference;
import org.openvpms.component.model.party.Party;
import org.openvpms.component.model.user.User;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Users for online bookings.
 *
 * @author Tim Anderson
 */
public class BookingUsers {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The booking locations.
     */
    private final BookingLocations locations;

    /**
     * Constructs a {@link BookingUsers}.
     *
     * @param service   the archetype service
     * @param locations the booking locations
     */
    public BookingUsers(IArchetypeService service, BookingLocations locations) {
        this.service = service;
        this.locations = locations;
    }

    /**
     * Returns the users associated with a location.
     *
     * @param location the location
     * @return the users
     */
    public List<User> getUsers(Party location) {
        List<User> result = new ArrayList<>();
        ArchetypeQuery query = UserQueryFactory.createUserQuery(location, "name", "id");
        Iterator<User> iterator = new IMObjectQueryIterator<>(service, query);
        while (iterator.hasNext()) {
            User user = iterator.next();
            if (canBook(user)) {
                result.add(user);
            }

        }
        return result;
    }

    /**
     * Returns a user given its identifier.
     *
     * @param userId the user identifier
     * @return the user, or {@code null} if it is not found or does not support online booking
     */
    public User getUser(long userId) {
        User result = null;
        IMObjectReference userRef = new IMObjectReference(UserArchetypes.USER, userId);
        User user = (User) service.get(userRef, true);
        if (user != null && canBook(user)) {
            result = user;
        }
        return result;
    }

    /**
     * Returns the practice locations for a user that support online bookings.
     *
     * @param user the user
     * @return the practice locations
     */
    public List<Party> getLocations(User user) {
        List<Party> result;
        IMObjectBean bean = service.getBean(user);
        List<Reference> references = bean.getTargetRefs("locations");
        if (references.isEmpty()) {
            result = locations.getLocations();
        } else {
            result = new ArrayList<>();
            for (Reference reference : references) {
                Party location = locations.getLocation(reference.getId());
                if (location != null) {
                    result.add(location);
                }
            }
        }
        return result;
    }

    /**
     * Returns a practice location for a user, given its identifier, iff it supports online booking.
     *
     * @param user       the user
     * @param locationId the location identifier
     * @return the practice location, or {@code null} if none is found
     */
    public Party getLocation(User user, long locationId) {
        Party result = null;
        IMObjectBean bean = service.getBean(user);
        List<Reference> references = bean.getTargetRefs("locations");
        if (references.isEmpty()) {
            // user works at all locations
            result = locations.getLocation(locationId);
        } else {
            for (Reference reference : references) {
                if (reference.getId() == locationId) {
                    result = locations.getLocation(locationId);
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Determines if a user is can be used for online booking.
     *
     * @param user the user
     * @return {@code true} if the user can be used for online booking
     */
    private boolean canBook(User user) {
        return service.getBean(user).getBoolean("onlineBooking");
    }

}
