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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.workflow;

import org.openvpms.component.model.entity.Entity;
import org.openvpms.component.system.common.util.PropertySet;

import java.util.Date;
import java.util.List;


/**
 * Schedule service.
 *
 * @author Tim Anderson
 */
public interface ScheduleService {

    /**
     * Returns all events for the specified schedule and day.
     * Events are represented by {@link PropertySet PropertySets}.
     *
     * @param schedule the schedule
     * @param day      the day
     * @return a list of events
     */
    List<PropertySet> getEvents(Entity schedule, Date day);

    /**
     * Returns all events for the specified schedule and day.
     * Events are represented by {@link PropertySet PropertySets}.
     *
     * @param schedule the schedule
     * @param day      the day
     * @return the events
     */
    ScheduleEvents getScheduleEvents(Entity schedule, Date day);

    /**
     * Returns the modification hash for the specified schedule and day.
     * <p>
     * This can be used to determine if any of the events have changed.
     *
     * @param schedule the schedule
     * @param day      the schedule day
     * @return the modification hash, or {@code -1} if the schedule and day are not cached
     */
    long getModHash(Entity schedule, Date day);

    /**
     * Returns all events for the specified schedule, and time range.
     *
     * @param schedule the schedule
     * @param from     the from time
     * @param to       the to time
     * @return a list of events
     */
    List<PropertySet> getEvents(Entity schedule, Date from, Date to);

    /**
     * Returns all events for the specified schedule, and time range.
     *
     * @param schedule the schedule
     * @param from     the from time
     * @param to       the to time
     * @return the events
     */
    ScheduleEvents getScheduleEvents(Entity schedule, Date from, Date to);

    /**
     * Returns the modification hash for the specified schedule and date range.
     * <p>
     * This can be used to determine if any of the events have changed.
     *
     * @param schedule the schedule
     * @param from     the from time
     * @param to       the to time
     * @return the modification hash, or {@code -1} if the schedule and range are not cached
     */
    long getModHash(Entity schedule, Date from, Date to);
}
