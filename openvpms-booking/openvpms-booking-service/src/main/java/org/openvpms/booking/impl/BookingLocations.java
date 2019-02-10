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
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.party.Party;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

/**
 * Practice locations for online bookings.
 *
 * @author Tim Anderson
 */
public class BookingLocations {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The practice service.
     */
    private final PracticeService practiceService;

    /**
     * Constructs a {@link BookingLocations}.
     *
     * @param service         the archetype service
     * @param practiceService the practice service
     */
    public BookingLocations(IArchetypeService service, PracticeService practiceService) {
        this.service = service;
        this.practiceService = practiceService;
    }

    /**
     * Returns the practice locations that support online booking.
     *
     * @return the practice locations
     */
    public List<Party> getLocations() {
        List<Party> result = new ArrayList<>();
        for (Party location : practiceService.getLocations()) {
            IMObjectBean bean = service.getBean(location);
            if (bean.getBoolean("onlineBooking")) {
                result.add(location);
            }
        }
        return result;
    }

    /**
     * Returns a practice location given its identifier.
     *
     * @param locationId the location identifier
     * @return the practice location, or {@code null} if it is not found or does not support online booking
     */
    public Party getLocation(long locationId) {
        for (Party location : practiceService.getLocations()) {
            if (location.getId() == locationId) {
                IMObjectBean bean = service.getBean(location);
                if (bean.getBoolean("onlineBooking")) {
                    return location;
                }
            }
        }
        return null;
    }


    /**
     * Returns the practice time zone.
     *
     * @return the time zone
     */
    public TimeZone getTimeZone() {
        return TimeZone.getDefault();
    }
}
