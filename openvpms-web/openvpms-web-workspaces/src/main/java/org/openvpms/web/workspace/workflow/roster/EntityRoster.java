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

package org.openvpms.web.workspace.workflow.roster;

import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.workflow.ScheduleEvent;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.system.common.util.PropertySet;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Associates roster events with an entity in a grid.
 *
 * @author Tim Anderson
 */
class EntityRoster<T extends Entity> {

    /**
     * The start date.
     */
    private final LocalDate startDate;

    /**
     * The number of days.
     */
    private final int days;

    /**
     * The entity.
     */
    private final T entity;

    /**
     * The events.
     */
    private final List<List<PropertySet>> events = new ArrayList<>();

    /**
     * Constructs an {@link EntityRoster}.
     *
     * @param entity the entity
     * @param start  the start date
     * @param days   the number of days
     * @param events the roster events
     */
    EntityRoster(T entity, Date start, int days, List<PropertySet> events) {
        this.startDate = DateRules.toLocalDate(start);
        this.days = days;
        this.entity = entity;
        if (events.isEmpty()) {
            this.events.add(Collections.emptyList());
        } else {
            for (PropertySet event : events) {
                addEvent(event);
            }
            this.events.add(Collections.emptyList());
        }
    }

    /**
     * Returns the entity.
     *
     * @return the entity
     */
    public T getEntity() {
        return entity;
    }

    /**
     * Returns the events.
     *
     * @return the events
     */
    public List<List<PropertySet>> getEvents() {
        return events;
    }

    /**
     * Adds an event.
     *
     * @param event the event
     */
    private void addEvent(PropertySet event) {
        int startDay = getStartDay(event);
        int endDay = getEndDay(event);
        List<PropertySet> row = null;
        for (int i = startDay; i <= endDay && i < days; ++i) {
            for (List<PropertySet> list : events) {
                if (list.get(i) == null) {
                    row = list;
                    break;
                }
            }
            if (row == null) {
                row = new ArrayList<>(Collections.nCopies(days, null));
                events.add(row);
            }
            row.set(i, event);
        }
    }

    /**
     * Returns the day offset relative to the start date that an event starts on.
     *
     * @param event the event
     * @return the start day
     */
    private int getStartDay(PropertySet event) {
        LocalDate startTime = DateRules.toLocalDate(event.getDate(ScheduleEvent.ACT_START_TIME));
        return (int) ChronoUnit.DAYS.between(startDate, startTime);
    }

    /**
     * Returns the day offset relative to the start date that an event end on.
     *
     * @param event the event
     * @return the end day
     */
    private int getEndDay(PropertySet event) {
        LocalDate endTime = DateRules.toLocalDate(event.getDate(ScheduleEvent.ACT_END_TIME));
        return (int) ChronoUnit.DAYS.between(startDate, endTime);
    }
}
