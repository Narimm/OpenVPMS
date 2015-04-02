package org.openvpms.web.workspace.workflow.appointment;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.rules.workflow.ScheduleEvent;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.web.workspace.workflow.scheduling.Schedule;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Multiple-day schedule grid.
 *
 * @author Tim Anderson
 */
public class MultiDayScheduleGrid extends AbstractScheduleEventGrid {

    /**
     * The number of days to display.
     */
    private final int days;

    /**
     * Constructs an {@link MultiDayScheduleGrid}.
     *
     * @param scheduleView the schedule view
     * @param date         the date
     * @param days         the number of days to display
     * @param appointments the appointments
     */
    public MultiDayScheduleGrid(Entity scheduleView, Date date, int days, Map<Entity, List<PropertySet>> appointments) {
        super(scheduleView, date, DateRules.getDate(date, days - 1, DateUnits.DAYS));
        this.days = days;
        setAppointments(appointments);
    }

    /**
     * Returns the no. of slots in the grid.
     *
     * @return the no. of slots
     */
    @Override
    public int getSlots() {
        return days;
    }

    /**
     * Returns the event for the specified schedule and slot.
     *
     * @param schedule the schedule
     * @param slot     the slot
     * @return the corresponding event, or {@code null} if none is found
     */
    @Override
    public PropertySet getEvent(Schedule schedule, int slot) {
        Date time = getStartTime(schedule, slot);
        PropertySet result = schedule.getEvent(time, 24 * 60);
        if (result == null && slot == 0) {
            result = schedule.getIntersectingEvent(time);
        }
        return result;
    }

    /**
     * Returns the time that the specified slot starts at.
     *
     * @param schedule the schedule
     * @param slot     the slot
     * @return the start time of the specified slot. May be {@code null}
     */
    @Override
    public Date getStartTime(Schedule schedule, int slot) {
        Date date = DateRules.getDate(getStartDate(), slot, DateUnits.DAYS);
        return DateRules.getDate(date, schedule.getStartMins(), DateUnits.MINUTES);
    }

    /**
     * Determines the availability of a slot for the specified schedule.
     *
     * @param schedule the schedule
     * @param slot     the slot
     * @return the availability of the schedule
     */
    @Override
    public Availability getAvailability(Schedule schedule, int slot) {
        if (getEvent(schedule, slot) != null) {
            return Availability.BUSY;
        }
        return Availability.FREE;
    }

    /**
     * Determines how many slots are unavailable from the specified slot, for
     * a schedule.
     *
     * @param schedule the schedule
     * @param slot     the starting slot
     * @return the no. of concurrent slots that are unavailable
     */
    @Override
    public int getUnavailableSlots(Schedule schedule, int slot) {
        return 0;
    }

    /**
     * Returns the slot that a time falls in.
     *
     * @param time the time
     * @return the slot, or {@code -1} if the time doesn't intersect any slot
     */
    @Override
    public int getSlot(Date time) {
        return Days.daysBetween(new DateTime(getStartDate()), new DateTime(time)).getDays();
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
    public int getSlots(PropertySet appointment, int slot) {
        DateTime endTime = new DateTime(appointment.getDate(ScheduleEvent.ACT_END_TIME));
        int endSlot = Days.daysBetween(new DateTime(getStartDate()), endTime).getDays();
        if (endTime.getHourOfDay() > 0 || endTime.getMinuteOfHour() > 0) {
            ++endSlot;
        }
        return endSlot - slot;
    }

    /**
     * Returns the date of a slot.
     *
     * @param slot the slot
     * @return the start time of the specified slot
     */
    public Date getDate(int slot) {
        return DateRules.getDate(getStartDate(), slot, DateUnits.DAYS);
    }

    /**
     * Sets the appointments.
     *
     * @param appointments the appointments, keyed on schedule
     */
    private void setAppointments(Map<Entity, List<PropertySet>> appointments) {
        List<Schedule> schedules = new ArrayList<Schedule>();
        for (Entity schedule : appointments.keySet()) {
            schedules.add(new Schedule(schedule, 0, 24 * 60, 24 * 60));
        }
        setSchedules(schedules);

        // add the appointments
        for (Map.Entry<Entity, List<PropertySet>> entry : appointments.entrySet()) {
            Party schedule = (Party) entry.getKey();
            List<PropertySet> sets = entry.getValue();

            for (PropertySet set : sets) {
                addAppointment(schedule, set);
            }
        }
    }

    /**
     * Adds an appointment.
     * <p/>
     * If the corresponding Schedule already has an appointment that intersects
     * the appointment, a new Schedule will be created with the same start and
     * end times, and the appointment added to that.
     *
     * @param schedule the schedule to add the appointment to
     * @param set      the appointment
     */
    private void addAppointment(Party schedule, PropertySet set) {
        int index = -1;
        boolean found = false;
        Schedule row = null;
        Schedule match = null;

        // try and find a corresponding Schedule that has no appointment that
        // intersects the supplied one
        List<Schedule> schedules = getSchedules();
        for (int i = 0; i < schedules.size(); ++i) {
            row = schedules.get(i);
            if (row.getSchedule().equals(schedule)) {
                if (row.indexOf(set.getReference(ScheduleEvent.ACT_REFERENCE)) != -1) {
                    return;
                }
                if (row.hasIntersectingEvent(set)) {
                    match = row;
                    index = i;
                } else {
                    found = true;
                    break;
                }
            }
        }
        if (!found) {
            // appointment intersects an existing one, so create a new Schedule
            row = new Schedule(match);
            schedules.add(index + 1, row);
        }
        row.addEvent(set);
    }

}
