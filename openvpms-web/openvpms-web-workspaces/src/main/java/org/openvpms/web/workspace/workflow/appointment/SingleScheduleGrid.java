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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.appointment;

import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.workflow.AppointmentRules;
import org.openvpms.archetype.rules.workflow.ScheduleEvent;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.web.workspace.workflow.scheduling.Schedule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


/**
 * An {@link AppointmentGrid} for a single schedule.
 * <p/>
 * This handles overlapping and double booked appointments by ordering them
 * one after another.
 *
 * @author Tim Anderson
 */
public class SingleScheduleGrid extends AbstractAppointmentGrid {

    /**
     * The schedule.
     */
    private Schedule schedule;

    /**
     * List containing the above schedule.
     */
    private List<Schedule> schedules;

    /**
     * The appointments.
     */
    private List<SlotGroup> appointments = new ArrayList<SlotGroup>();

    /**
     * The slots.
     */
    private List<Slot> slots = new ArrayList<Slot>();


    /**
     * Constructs a {@link SingleScheduleGrid}.
     *
     * @param scheduleView         the schedule view
     * @param date                 the appointment date
     * @param organisationSchedule the schedule
     * @param appointmentSets      the appointments
     * @param rules                the appointment rules
     */
    public SingleScheduleGrid(Entity scheduleView, Date date, Party organisationSchedule,
                              List<PropertySet> appointmentSets, AppointmentRules rules) {
        super(scheduleView, date, -1, -1, rules);
        schedule = createSchedule(organisationSchedule);
        schedules = Arrays.asList(schedule);
        int startMins = schedule.getStartMins();
        int endMins = schedule.getEndMins();
        int slotSize = schedule.getSlotSize();

        // adjust the start and end minutes based on the appointments present
        for (PropertySet set : appointmentSets) {
            Date startTime = set.getDate(ScheduleEvent.ACT_START_TIME);
            Date endTime = set.getDate(ScheduleEvent.ACT_END_TIME);
            int slotStart = getSlotMinutes(startTime, false);
            int slotEnd = getSlotMinutes(endTime, true);
            if (startMins > slotStart) {
                startMins = slotStart;
            }
            if (endMins < slotEnd) {
                endMins = slotEnd;
            }
        }

        setSlotSize(slotSize);
        setStartMins(startMins);
        setEndMins(endMins);

        setAppointments(appointmentSets);
    }

    /**
     * Returns the no. of slots in the grid.
     *
     * @return the no. of slots
     */
    @Override
    public int getSlots() {
        return slots.size();
    }

    /**
     * Returns the schedules.
     *
     * @return the schedules
     */
    public List<Schedule> getSchedules() {
        return schedules;
    }

    /**
     * Returns the appointment for the specified schedule and slot.
     *
     * @param schedule the schedule
     * @param slot     the slot
     * @return the corresponding appointment, or {@code null} if none is found
     */
    public PropertySet getEvent(Schedule schedule, int slot) {
        SlotGroup group = slots.get(slot).getGroup();
        return (group != null) ? group.getAppointment() : null;
    }

    /**
     * Returns the no. of slots at an appointment occupies, from the specified
     * slot.
     * <p/>
     * If the appointment begins prior to the slot, the remaining slots will
     * be returned.
     *
     * @param appointment the appointment
     * @param slot        the starting slot
     * @return the no. of slots that the appointment occupies
     */
    @Override
    public int getSlots(PropertySet appointment, int slot) {
        SlotGroup group = slots.get(slot).getGroup();
        int result = 0;
        if (group != null) {
            result = (group.getStartSlot() + group.getSlots()) - slot;
        }
        return result;
    }

    /**
     * Returns the no. of minutes from midnight that the specified slot starts
     * at.
     *
     * @param slot the slot
     * @return the minutes that the slot starts at
     */
    @Override
    public int getStartMins(int slot) {
        return slots.get(slot).getStartMins();
    }

    /**
     * Returns the hour of the specified slot.
     *
     * @param slot the slot
     * @return the hour, in the range 0..23
     */
    @Override
    public int getHour(int slot) {
        int startMins = slots.get(slot).getStartMins();
        return startMins / 60;
    }

    /**
     * Returns the first slot that has a start time and end time intersecting
     * the specified minutes.
     *
     * @param minutes the minutes
     * @return the first slot that minutes intersects, or {@code -1} if no
     *         slots intersect
     */
    public int getFirstSlot(int minutes) {
        int result = -1;
        for (int i = 0; i < slots.size(); ++i) {
            Slot slot = slots.get(i);
            int startMins = slot.getStartMins();
            int endMins = startMins + getSlotSize();
            if (startMins <= minutes && endMins > minutes) {
                result = i;
                break;
            } else if (startMins > minutes) {
                break;
            }
        }
        return result;
    }

    /**
     * Returns the last slot that has a start time and end time intersecting
     * the specified minutes.
     *
     * @param minutes the minutes
     * @return the last slot that minutes intersects, or {@code -1} if no
     *         slots intersect
     */
    public int getLastSlot(int minutes) {
        int result = -1;
        for (int i = slots.size() - 1; i >= 0; --i) {
            Slot slot = slots.get(i);
            int startMins = slot.getStartMins();
            int endMins = startMins + getSlotSize();
            if (startMins <= minutes && endMins > minutes) {
                result = i;
                break;
            }
        }
        return result;
    }

    /**
     * Sets the appointments.
     *
     * @param sets the appointments
     */
    private void setAppointments(List<PropertySet> sets) {
        for (PropertySet set : sets) {
            // add the appointment, and create a new SlotGroup for it
            addAppointment(set);
        }
        int startMins = getStartMins();
        int endMins = getEndMins();
        int slotSize = getSlotSize();

        // create Slot instances for every slot in the grid, associating them
        // with SlotGroups as required.
        for (SlotGroup appointment : appointments) {
            if (startMins < appointment.getStartMins()) {
                // add empty slots prior to the appointment
                while (startMins < appointment.getStartMins()) {
                    slots.add(new Slot(startMins));
                    startMins += slotSize;
                }
            } else {
                // handle double bookings and overlapping appointments
                startMins = appointment.getStartMins();
            }
            slots.add(new Slot(startMins, appointment));
            appointment.setStartSlot(slots.size() - 1);
            startMins += slotSize;
            while (startMins < appointment.getEndMins()) {
                // add slots associated with the appointment
                slots.add(new Slot(startMins, appointment));
                startMins += slotSize;
            }
        }

        // add empty slots up to the end of the grid
        while (startMins < endMins) {
            slots.add(new Slot(startMins));
            startMins += slotSize;
        }
    }

    /**
     * Adds an appointment.
     * <p/>
     * This adds it to the schedule, and creates a new SlotGroup for it.
     *
     * @param set the appointment
     */
    private void addAppointment(PropertySet set) {
        schedule.addEvent(set);

        Date startTime = set.getDate(ScheduleEvent.ACT_START_TIME);
        Date endTime = set.getDate(ScheduleEvent.ACT_END_TIME);
        Date startDate = DateRules.getDate(startTime);
        Date endDate = DateRules.getDate(endTime);
        int startMins = startDate.compareTo(getDate()) < 0 ? 0 : getSlotMinutes(startTime, false);
        int endMins = endDate.compareTo(getDate()) > 0 ? 24 * 60 : getSlotMinutes(endTime, true);

        int slotSize = getSlotSize();
        int size = (endMins - startMins) / slotSize;

        int i = 0;
        for (; i < appointments.size(); ++i) {
            SlotGroup s = appointments.get(i);
            if (s.getStartMins() > startMins) {
                break;
            }
        }
        appointments.add(i, new SlotGroup(set, startMins, endMins, size));
    }

    /**
     * Represents a slot in the schedule.
     */
    private static class Slot {

        /**
         * The start time of the slot, as minutes since midnight.
         */
        private final int startMins;

        /**
         * The related slots, or {@code null} if this slot isn't related to
         * any slots.
         */
        private final SlotGroup group;

        /**
         * Creates a new {@code Slot}.
         *
         * @param startMins the start time, as minutes since midnight
         */
        public Slot(int startMins) {
            this(startMins, null);
        }

        /**
         * Creates a new {@code Slot}.
         *
         * @param startMins the start time, as minutes since midnight
         * @param group     the related slots. May be {@code null}
         */
        public Slot(int startMins, SlotGroup group) {
            this.startMins = startMins;
            this.group = group;
        }

        /**
         * Returns the start time, as minutes since midnight.
         *
         * @return the start time
         */
        public int getStartMins() {
            return startMins;
        }

        /**
         * Returns the related slots.
         *
         * @return the related slots. May be {@code null}
         */
        public SlotGroup getGroup() {
            return group;
        }
    }

    /**
     * A group of slots, associated with a single appointment.
     */
    private static class SlotGroup {

        /**
         * The appointment.
         */
        private final PropertySet appointment;

        /**
         * The no. of slots that the appointment occupies.
         */
        private final int slots;

        /**
         * The start time of the group, as minutes from midnight.
         */
        private final int startMins;

        /**
         * The end time of the group, as minutes from midnight.
         */
        private final int endMins;

        /**
         * The slot that the group starts at.
         */
        private int startSlot;

        /**
         * Constructs a {@link SlotGroup}.
         *
         * @param appointment the appointment
         * @param startMins   the start time, as minutes from midnight
         * @param endMins     the end time, as minutes from midnight
         * @param slots       the no. of slots that the appointment occupies
         */
        public SlotGroup(PropertySet appointment, int startMins, int endMins,
                         int slots) {
            this.appointment = appointment;
            this.startMins = startMins;
            this.endMins = endMins;
            this.slots = slots;
        }

        /**
         * Returns the appointment.
         *
         * @return the appointment
         */
        public PropertySet getAppointment() {
            return appointment;
        }

        /**
         * Returns the group start time, as minutes from midnight.
         *
         * @return the start time
         */
        public int getStartMins() {
            return startMins;
        }

        /**
         * Returns the group end time, as minutes from midnight.
         *
         * @return the end time
         */
        public int getEndMins() {
            return endMins;
        }

        /**
         * Sets the slot that the group starts at.
         *
         * @param startSlot the start slot
         */
        public void setStartSlot(int startSlot) {
            this.startSlot = startSlot;
        }

        /**
         * Returns the slot that the group starts at.
         *
         * @return the start slot
         */
        public int getStartSlot() {
            return startSlot;
        }

        /**
         * Returns the no. of slots that the appointment occupies.
         *
         * @return the no. of slots
         */
        public int getSlots() {
            return slots;
        }
    }

}
