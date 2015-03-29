package org.openvpms.web.workspace.workflow.appointment;

import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.web.workspace.workflow.scheduling.Schedule;
import org.openvpms.web.workspace.workflow.scheduling.ScheduleEventGrid;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Abstract implementation of the {@link ScheduleEventGrid} interface.
 *
 * @author Tim Anderson
 */
public abstract class AbstractScheduleEventGrid implements ScheduleEventGrid {

    /**
     * The schedule view.
     */
    private final Entity scheduleView;

    /**
     * The schedules.
     */
    private List<Schedule> schedules = Collections.emptyList();

    /**
     * The grid date.
     */
    private Date date;


    /**
     * Constructs an {@link AbstractScheduleEventGrid}.
     *
     * @param scheduleView the schedule view
     * @param date         the schedule date
     */
    public AbstractScheduleEventGrid(Entity scheduleView, Date date) {
        this.scheduleView = scheduleView;
        setDate(date);
    }

    /**
     * Returns the schedule view associated with this grid.
     *
     * @return the schedule view
     */
    @Override
    public Entity getScheduleView() {
        return scheduleView;
    }

    /**
     * Returns the schedules.
     *
     * @return the schedules
     */
    @Override
    public List<Schedule> getSchedules() {
        return schedules;
    }

    /**
     * Returns the schedule date.
     *
     * @return the date, excluding any time
     */
    @Override
    public Date getDate() {
        return date;
    }

    /**
     * Sets the schedule date.
     * <p/>
     * Any time is removed.
     *
     * @param date the schedule date
     */
    public void setDate(Date date) {
        this.date = DateRules.getDate(date);
    }

    /**
     * Sets the schedules.
     *
     * @param schedules the schedules
     */
    protected void setSchedules(List<Schedule> schedules) {
        this.schedules = schedules;
    }

}
