/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.workflow.scheduling;

import org.openvpms.archetype.rules.workflow.Appointment;
import org.openvpms.archetype.rules.workflow.AppointmentRules;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.ObjectSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class SingleScheduleGrid extends AbstractAppointmentGrid {

    private Schedule schedule;
    private List<Schedule> schedules;

    private List<SlotGroup> appointments = new ArrayList<SlotGroup>();

    private List<Slot> slots = new ArrayList<Slot>();


    public SingleScheduleGrid(Date date, Party organisationSchedule,
                              List<ObjectSet> appointmentSets) {
        super(date, -1, -1);
        AppointmentRules rules = new AppointmentRules();
        schedule = new Schedule(organisationSchedule, rules);
        schedules = Arrays.asList(schedule);
        int startMins = schedule.getStartMins();
        int endMins = schedule.getEndMins();
        int slotSize = schedule.getSlotSize();

        for (ObjectSet set : appointmentSets) {
            Date startTime = set.getDate(Appointment.ACT_START_TIME);
            Date endTime = set.getDate(Appointment.ACT_END_TIME);
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

    public List<Schedule> getSchedules() {
        return schedules;
    }

    public ObjectSet getAppointment(Schedule schedule, int slot) {
        SlotGroup group = slots.get(slot).getGroup();
        return (group != null) ? group.getAppointment() : null;
    }

    @Override
    public int getStartMins(int slot) {
        return slots.get(slot).getStartMins();
    }

    @Override
    public int getHour(int slot) {
        int startMins = slots.get(slot).getStartMins();
        return startMins / 60;
    }

    @Override
    public int getSlots() {
        return slots.size();
    }

    @Override
    public int getSlots(ObjectSet appointment, int slot) {
        SlotGroup group = slots.get(slot).getGroup();
        int result = 0;
        if (group != null) {
            result = (group.getStartSlot() + group.getSlots()) - slot;
        }
        return result;
    }

    public int getFirstSlot(int mins) {
        int result = -1;
        for (int i = 0; i < slots.size(); ++i) {
            Slot slot = slots.get(i);
            int startMins = slot.getStartMins();
            int endMins = startMins + getSlotSize();
            if (startMins <= mins && endMins > mins) {
                result = i;
                break;
            } else if (startMins > mins) {
                break;
            }

        }
        return result;
    }

    public int getLastSlot(int mins) {
        int result = -1;
        for (int i = slots.size() - 1; i >= 0; --i) {
            Slot slot = slots.get(i);
            int startMins = slot.getStartMins();
            int endMins = startMins + getSlotSize();
            if (startMins <= mins && endMins > mins) {
                result = i;
                break;
            } else if (startMins > mins) {
                break;
            }

        }
        return result;
    }

    private void setAppointments(List<ObjectSet> sets) {
        for (ObjectSet set : sets) {
            addAppointment(set);
        }
        int startMins = getStartMins();
        int endMins = getEndMins();
        int slotSize = getSlotSize();

        for (SlotGroup appointment : appointments) {
            if (startMins < appointment.getStartMins()) {
                while (startMins < appointment.getStartMins()) {
                    slots.add(new Slot(startMins));
                    startMins += slotSize;
                }
            } else {
                startMins = appointment.getStartMins();
            }
            slots.add(new Slot(startMins, appointment));
            appointment.setStartSlot(slots.size() - 1);
            startMins += slotSize;
            while (startMins < appointment.getEndMins()) {
                slots.add(new Slot(startMins, appointment));
                startMins += slotSize;
            }
        }
        while (startMins < endMins) {
            slots.add(new Slot(startMins));
            startMins += slotSize;
        }
    }

    private void addAppointment(ObjectSet set) {
        Date startTime = set.getDate(
                org.openvpms.archetype.rules.workflow.Appointment.ACT_START_TIME);
        Date endTime = set.getDate(
                org.openvpms.archetype.rules.workflow.Appointment.ACT_END_TIME);
        int startMins = getSlotMinutes(startTime, false);
        int endMins = getSlotMinutes(endTime, true);
        schedule.addAppointment(set);
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

    private static class Slot {

        private final int startMins;
        private final SlotGroup group;

        public Slot(int startMins) {
            this(startMins, null);
        }

        public Slot(int startMins, SlotGroup group) {
            this.startMins = startMins;
            this.group = group;
        }

        public int getStartMins() {
            return startMins;
        }

        public SlotGroup getGroup() {
            return group;
        }
    }

    private static class SlotGroup {
        private final ObjectSet appointment;
        private final int slots;
        private final int startMins;
        private final int endMins;
        private int startSlot;

        public SlotGroup(ObjectSet appointment, int startMins, int endMins,
                         int slots) {
            this.appointment = appointment;
            this.startMins = startMins;
            this.endMins = endMins;
            this.slots = slots;
        }

        public ObjectSet getAppointment() {
            return appointment;
        }

        public int getStartMins() {
            return startMins;
        }

        public int getEndMins() {
            return endMins;
        }

        public void setStartSlot(int startSlot) {
            this.startSlot = startSlot;
        }

        public int getStartSlot() {
            return startSlot;
        }

        public int getSlots() {
            return slots;
        }

    }

}
