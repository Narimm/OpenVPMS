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

package org.openvpms.web.workspace.workflow.appointment;

import org.openvpms.archetype.rules.workflow.CalendarBlock;
import org.openvpms.archetype.rules.workflow.CalendarBlocks;
import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.archetype.rules.workflow.Times;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.model.act.Act;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.entity.Entity;
import org.openvpms.component.model.lookup.Lookup;
import org.openvpms.component.model.object.Reference;
import org.openvpms.component.model.party.Party;
import org.openvpms.component.system.common.cache.IMObjectCache;
import org.openvpms.component.system.common.cache.MapIMObjectCache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Overlapping events.
 *
 * @author Tim Anderson
 */
public class OverlappingEvents {

    /**
     * The schedule.
     */
    private final Entity schedule;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The events.
     */
    private List<Times> events;

    /**
     * Constructs an {@link OverlappingEvents}.
     *
     * @param schedule the schedule
     * @param events   the events
     * @param service  the service
     */
    public OverlappingEvents(Entity schedule, List<Times> events, IArchetypeService service) {
        this.schedule = schedule;
        this.events = events;
        this.service = service;
    }

    /**
     * Returns all overlapping events.
     *
     * @return the events
     */
    public List<Times> getEvents() {
        return events;
    }

    /**
     * Returns the first overlapping event.
     *
     * @return the first overlapping event, or {@code null} if there is none
     */
    public Times getFirstOverlap() {
        return !events.isEmpty() ? events.get(0) : null;
    }

    /**
     * Returns the first overlapping appointment.
     *
     * @return the first overlapping appointment, or {@code null} if there is none
     */
    public Times getFirstAppointment() {
        for (Times event : events) {
            if (event.getReference().isA(ScheduleArchetypes.APPOINTMENT)) {
                return event;
            }
        }
        return null;
    }

    /**
     * Returns all overlapping appointments.
     *
     * @return the overlapping appointments
     */
    public List<Times> getAppointments() {
        List<Times> result = new ArrayList<>();
        for (Times event : events) {
            if (event.getReference().isA(ScheduleArchetypes.APPOINTMENT)) {
                result.add(event);
            }
        }
        return result;
    }

    /**
     * Returns the calendar blocks, if any.
     * <p>
     * This evaluates the blocks against a customer. It returns on the first reserved block with classifications that
     * the customer doesn't have.
     *
     * @param customer the customer to evaluate the blocks against.
     * @return the calendar blocks, or {@code null} if there are none
     */
    public CalendarBlocks getCalendarBlocks(Party customer) {
        CalendarBlock reserved = null;
        List<CalendarBlock> unreserved = new ArrayList<>();
        IMObjectCache cache = new MapIMObjectCache(service);
        Map<Reference, Boolean> flags = new HashMap<>();
        for (Times event : new ArrayList<>(events)) {
            if (event.getReference().isA(ScheduleArchetypes.CALENDAR_BLOCK)) {
                Act act = (Act) service.get(event.getReference());
                if (act != null) {
                    CalendarBlock block = getCalendarBlock(act, customer, cache, flags);
                    if (block != null) {
                        if (block.isReserved()) {
                            reserved = block;
                            break;
                        } else {
                            unreserved.add(block);
                        }
                    }
                }
            }
        }
        return (reserved == null && unreserved.isEmpty()) ? null : new CalendarBlocks(reserved, unreserved);
    }

    /**
     * Determines if the schedule allows double booking.
     *
     * @return {@code true} if the schedule allows double booking
     */
    public boolean allowDoubleBooking() {
        IMObjectBean bean = service.getBean(schedule);
        return bean.getBoolean("allowDoubleBooking");
    }

    /**
     * Returns a {@link CalendarBlock} for an <em>act.calendarBlock</em>.
     *
     * @param act            the act
     * @param customer       the customer
     * @param cache          a cache of <em>entity.calendarBlockType</em>
     * @param reservedBlocks cache of reserved flags, keyed on block type reference
     * @return a new calendar block, or {@code null} if there is no block type
     */
    private CalendarBlock getCalendarBlock(Act act, Party customer, IMObjectCache cache,
                                           Map<Reference, Boolean> reservedBlocks) {
        CalendarBlock result = null;
        IMObjectBean bean = service.getBean(act);
        Reference type = bean.getTargetRef("type");
        boolean reserved = false;
        if (type != null) {
            Entity blockType = (Entity) cache.get(type);
            if (blockType != null) {
                Boolean isReserved = reservedBlocks.get(type);
                if (isReserved == null) {
                    // haven't seen this type before, so need to check
                    IMObjectBean typeBean = service.getBean(blockType);
                    List<Lookup> accountTypes = typeBean.getValues("customerAccountTypes", Lookup.class);
                    List<Lookup> customerTypes = typeBean.getValues("customerTypes", Lookup.class);
                    if ((!accountTypes.isEmpty() || !customerTypes.isEmpty())
                        && !hasClassification(customer, accountTypes, customerTypes)) {
                        reserved = true;
                    }
                    reservedBlocks.put(type, reserved);
                }
                result = new CalendarBlock(act, blockType, reserved, service);
            }
        }
        return result;
    }

    /**
     * Determines if a customer has at least one of the specified account types or types.
     *
     * @param customer      the customer
     * @param accountTypes  the account types
     * @param customerTypes the customer types
     * @return {@code true} if the customer has one of the types
     */
    private boolean hasClassification(Party customer, List<Lookup> accountTypes, List<Lookup> customerTypes) {
        IMObjectBean customerBean = service.getBean(customer);
        return hasClassification(customerBean, "type", accountTypes)
               || hasClassification(customerBean, "classifications", customerTypes);
    }

    /**
     * Determines if a customer node has at least one matching classification.
     *
     * @param customer        the customer
     * @param name            the classification node name
     * @param classifications the classifications to compare against
     * @return if {@code true} if the customer node has at least one of the classifications
     */
    private boolean hasClassification(IMObjectBean customer, String name, List<Lookup> classifications) {
        if (!classifications.isEmpty()) {
            for (Lookup lookup : customer.getValues(name, Lookup.class)) {
                if (classifications.contains(lookup)) {
                    return true;
                }
            }
        }
        return false;
    }

}
