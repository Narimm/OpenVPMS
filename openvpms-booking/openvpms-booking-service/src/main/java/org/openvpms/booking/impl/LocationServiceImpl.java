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

package org.openvpms.booking.impl;

import org.openvpms.archetype.rules.practice.PracticeArchetypes;
import org.openvpms.archetype.rules.practice.PracticeService;
import org.openvpms.archetype.rules.workflow.AppointmentRules;
import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.booking.api.LocationService;
import org.openvpms.booking.domain.Location;
import org.openvpms.booking.domain.Schedule;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.springframework.stereotype.Component;

import javax.ws.rs.NotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

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
     * The practice service.
     */
    private final PracticeService practiceService;

    /**
     * The appointment rules.
     */
    private final AppointmentRules rules;


    /**
     * Constructs a {@link PracticeService}.
     *
     * @param service         the archetype service
     * @param practiceService the practice service
     * @param rules           the appointment rules
     */
    public LocationServiceImpl(IArchetypeService service, PracticeService practiceService, AppointmentRules rules) {
        this.service = service;
        this.practiceService = practiceService;
        this.rules = rules;
    }

    /**
     * Returns the practice locations.
     *
     * @return the practice locations
     */
    @Override
    public List<Location> getLocations() {
        List<Location> result = new ArrayList<>();
        String timeZone = getTimeZone().getID();
        for (Party location : practiceService.getLocations()) {
            IMObjectBean bean = new IMObjectBean(location, service);
            if (bean.getBoolean("onlineBooking")) {
                result.add(new Location(location.getId(), location.getName(), timeZone));
            }
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
        for (Party location : practiceService.getLocations()) {
            if (location.getId() == locationId) {
                IMObjectBean bean = new IMObjectBean(location, service);
                if (bean.getBoolean("onlineBooking")) {
                    return new Location(location.getId(), location.getName(), getTimeZone().getID());
                }
            }
        }
        throw new NotFoundException("Practice location not found");
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
        ArchetypeQuery query = new ArchetypeQuery(ScheduleArchetypes.ORGANISATION_SCHEDULE);
        IMObjectReference location = new IMObjectReference(PracticeArchetypes.LOCATION, locationId);
        query.add(Constraints.join("location").add(Constraints.eq("target", location)));
        Iterator<IMObject> iterator = new IMObjectQueryIterator<>(service, query);
        while (iterator.hasNext()) {
            IMObject schedule = iterator.next();
            IMObjectBean bean = new IMObjectBean(schedule, service);
            if (bean.getBoolean("onlineBooking")) {
                int slotSize = rules.getSlotSize((Party) schedule);
                result.add(new Schedule(schedule.getId(), schedule.getName(), slotSize));
            }
        }
        return result;
    }

    /**
     * Returns the practice time zone.
     *
     * @return the time zone
     */
    protected TimeZone getTimeZone() {
        return TimeZone.getDefault();
    }

}
